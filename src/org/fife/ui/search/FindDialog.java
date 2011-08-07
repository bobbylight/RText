/*
 * 11/14/2003
 *
 * FindDialog - Dialog for finding text in a GUI.
 * Copyright (C) 2003 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
 *
 * This file is a part of RText.
 *
 * RText is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.ui.search;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.rtext.AssistanceIconPanel;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;


/**
 * A "Find" dialog similar to those found in most Windows text editing
 * applications.  Contains many search options, including:<br>
 * <ul>
 *   <li>Match Case
 *   <li>Match Whole Word
 *   <li>Use Regular Expressions
 *   <li>Search Forwards or Backwards
 *   <li>Mark all
 * </ul>
 * The dialog also remembers your previous several selections in a combo box.
 * <p>An application can use a <code>FindDialog</code> as follows.  It is
 * suggested that you create an <code>Action</code> or something similar to
 * facilitate "bringing up" the Find dialog.  Have the main application contain
 * an object that implements both <code>PropertyChangeListener</code> and
 * <code>ActionListener</code>.  This object will receive the following events
 * from the Find dialog:
 * <ul>
 *   <li>"FindNext" action when the user clicks the "Find" button.
 *   <li>"SearchDialog.MatchCase" property change when the user checks/unchecks
 *       the Match Case checkbox.
 *   <li>"SearchDialog.MatchWholeWord" property change when the user
 *       checks/unchecks the Whole Word checkbox.
 *   <li>"SearchDialog.UseRegularExpressions" property change when the user
 *       checks/unchecks the "Regular Expressions" checkbox.
 *   <li>"SearchDialog.SearchDownward" property change when the user clicks
 *       either the Up or Down direction radio button.
 * </ul>
 * The property events listed can all be ignored in a simple case; the Find
 * dialog will remember the state of its checkboxes between invocations.
 * However, if your application has both a Find and Replace dialog, you may wish
 * to use these messages to synchronize the two search dialogs' options.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FindDialog extends AbstractFindReplaceDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	// This helps us work around the "bug" where JComboBox eats the first Enter
	// press.
	private String lastSearchString;


	/**
	 * Creates a new <code>FindDialog</code>.
	 *
	 * @param owner The main window that owns this dialog.
	 * @param actionListener The component that listens for "Find" actions.
	 */
	public FindDialog(Frame owner, ActionListener actionListener) {
		this(owner, actionListener,
			ResourceBundle.getBundle("org.fife.ui.search.Search"));
	}


	/**
	 * Creates a new <code>FindDialog</code>.
	 *
	 * @param owner The main window that owns this dialog.
	 * @param actionListener The component that listens for "Find" actions.
	 * @param resources The resource bundle from which to get strings, etc.
	 */
	public FindDialog(Frame owner, ActionListener actionListener,
								ResourceBundle resources) {

		// Let it be known who the owner of this dialog is.
		super(owner, resources, true);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		// Make a panel containing the "Find" edit box.
		JPanel enterTextPane = new JPanel(new SpringLayout());
		enterTextPane.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
		JTextComponent textField = getTextComponent(findTextCombo);
		textField.addFocusListener(new FindFocusAdapter());
		textField.addKeyListener(new FindKeyListener());
		textField.getDocument().addDocumentListener(new FindDocumentListener());
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(findTextCombo);
		AssistanceIconPanel aip = new AssistanceIconPanel(findTextCombo);
		temp.add(aip, BorderLayout.LINE_START);
		if (orientation.isLeftToRight()) {
			enterTextPane.add(findFieldLabel);
			enterTextPane.add(temp);
		}
		else {
			enterTextPane.add(temp);
			enterTextPane.add(findFieldLabel);
		}

		UIUtil.makeSpringCompactGrid(enterTextPane, 1, 2,	//rows, cols
											0,0,		//initX, initY
											6, 6);	//xPad, yPad

		// Make a panel containing the inherited search direction radio
		// buttons and the inherited search options.
		JPanel bottomPanel = new JPanel(new BorderLayout());
		temp = new JPanel(new BorderLayout());
		bottomPanel.setBorder(UIUtil.getEmpty5Border());
		temp.add(searchConditionsPanel, BorderLayout.LINE_START);
		temp.add(dirPanel);
		bottomPanel.add(temp, BorderLayout.LINE_START);

		// Now, make a panel containing all the above stuff.
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.add(enterTextPane);
		leftPanel.add(bottomPanel);

		// Make a panel containing the action buttons.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2,1, 5,5));
		buttonPanel.add(findNextButton);
		buttonPanel.add(cancelButton);
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(buttonPanel, BorderLayout.NORTH);

		// Put everything into a neat little package.
		JPanel contentPane = new JPanel(new BorderLayout());
		if (orientation.isLeftToRight()) {
			contentPane.setBorder(BorderFactory.createEmptyBorder(5,0,0,5));
		}
		else {
			contentPane.setBorder(BorderFactory.createEmptyBorder(5,5,0,0));
		}
		contentPane.add(leftPanel);
		contentPane.add(rightPanel, BorderLayout.LINE_END);
		temp = new ResizableFrameContentPane(new BorderLayout());
		temp.add(contentPane, BorderLayout.NORTH);
		setContentPane(temp);
		getRootPane().setDefaultButton(findNextButton);
		setTitle(resources.getString("FindDialogTitle"));
		setResizable(true);
		pack();
		setLocationRelativeTo(owner);

		applyComponentOrientation(orientation);

	}


	// Listens for an action in this Find dialog.
	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		if (actionCommand.equals("FindNext")) {

			// Add the item to the combo box's list, if it isn't already there.
			findTextCombo.addItem(getTextComponent(findTextCombo).getText());

			// If they just searched for an item that's already in the list
			// other than the first, move it to the first position.
			if (findTextCombo.getSelectedIndex()>0) {
				Object item = findTextCombo.getSelectedItem();
				findTextCombo.removeItem(item);
				findTextCombo.insertItemAt(item, 0);
				findTextCombo.setSelectedIndex(0);
			}

		} // End of if (actionCommand.equals("FindNext")).

		// Otherwise, let the superclass handle the action.
		else {
			super.actionPerformed(e);
		}

	}


	/**
	 * Adds an <code>ActionListener</code> to this dialog.  The listener will
	 * receive notification when the user clicks the "Find" button with an
	 * actionCommand string of "FindNext".
	 *
	 * @param l The listener to add.
	 * @see #removeActionListener
	 */
	public void addActionListener(ActionListener l) {
		findNextButton.addActionListener(l);
	}


	/**
	 * Removes an <code>ActionListener</code> from this dialog.
	 *
	 * @param l The listener to remove
	 * @see #addActionListener
	 */
	public void removeActionListener(ActionListener l) {
		findNextButton.removeActionListener(l);
	}


	/**
	 * Overrides <code>JDialog</code>'s <code>setVisible</code> method; decides
	 * whether or not buttons are enabled.
	 *
	 * @param visible Whether or not the dialog should be visible.
	 */
	public void setVisible(boolean visible) {

		if (visible) {

			String selectedItem = (String)findTextCombo.getSelectedItem();
			findNextButton.setEnabled(selectedItem!=null);

			// Call JDialog's setVisible.
			super.setVisible(true);

			// Make the "Find" text field active.
			JTextComponent textField = getTextComponent(findTextCombo);
			textField.requestFocusInWindow();
			textField.selectAll();

		}

		else
			super.setVisible(false);

	}

	/**
	 * Called whenever the user changes the Look and Feel, etc.
	 * This is overridden so we can reinstate the listeners that are evidently
	 * lost on the JTextField portion of our combo box.
	 */
	public void updateUI() {
		JTextComponent textField = getTextComponent(findTextCombo);
		textField.addFocusListener(new FindFocusAdapter());
		textField.addKeyListener(new FindKeyListener());
		textField.getDocument().addDocumentListener(new FindDocumentListener());
	}


	/**
	 * Listens for changes in the text field (find search field).
	 */
	private class FindDocumentListener implements DocumentListener {

		public void insertUpdate(DocumentEvent e) {
			handleToggleButtons();
		}

		public void removeUpdate(DocumentEvent e) {
			JTextComponent comp = getTextComponent(findTextCombo);
			if (comp.getDocument().getLength()==0) {
				findNextButton.setEnabled(false);
			}
			else {
				handleToggleButtons();
			}
		}

		public void changedUpdate(DocumentEvent e) {
		}

	}


	/**
	 * Listens for the text field gaining focus.  All it does is select all
	 * text in the combo box's text area.
	 */
	private class FindFocusAdapter extends FocusAdapter {

		public void focusGained(FocusEvent e) {
			getTextComponent(findTextCombo).selectAll();
			// Remember what it originally was, in case they tabbed out.
			lastSearchString = (String)findTextCombo.getSelectedItem();
		}

	}


	/**
	 * Listens for key presses in the find dialog.
	 */
	private class FindKeyListener implements KeyListener {

		// Listens for the user pressing a key down.
		public void keyPressed(KeyEvent e) {
		}

		// Listens for a user releasing a key.
		public void keyReleased(KeyEvent e) {

			// This is an ugly hack to get around JComboBox's
			// insistence on eating the first Enter keypress
			// it receives when it has focus and its selected item
			// has changed since the last time it lost focus.
			if (e.getKeyCode()==KeyEvent.VK_ENTER && isPreJava6JRE()) {
				String searchString = (String)findTextCombo.getSelectedItem();
				if (!searchString.equals(lastSearchString)) {
					findNextButton.doClick(0);
					lastSearchString = searchString;
					getTextComponent(findTextCombo).selectAll();
				}
			}

		}

		// Listens for a key being typed.
		public void keyTyped(KeyEvent e) {
		}

	}


}