/*
 * 11/14/2003
 *
 * ReplaceDialog.java - Dialog for replacing text in a GUI.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.fife.rtext.AssistanceIconPanel;
import org.fife.ui.MaxWidthComboBox;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;


/**
 * A "Replace" dialog similar to those found in most Windows text editing
 * applications.  Contains many search options, including:<br>
 * <ul>
 *   <li>Match Case
 *   <li>Match Whole Word
 *   <li>Use Regular Expressions
 *   <li>Search Forwards or Backwards
 * </ul>
 * The dialog also remembers your previous several selections in a combo box.
 * <p>An application can use a <code>ReplaceDialog</code> as follows.  It is suggested
 * that you create an <code>Action</code> or something similar to facilitate
 * "bringing up" the Replace dialog.  Have the main application contain an object
 * that implements both <code>PropertyChangeListener</code> and
 * <code>ActionListener</code>.  This object will receive the following events from
 * the Replace dialog:
 * <ul>
 *   <li>"Replace" action when the user clicks the "Replace" button.
 *   <li>"Replace All" action when the user clicks the "Replace All" button.
 *   <li>"SearchDialog.MatchCase" property change when the user checks/unchecks the
 *       Match Case checkbox.
 *   <li>"SearchDialog.MatchWholeWord" property change when the user checks/unchecks
 *       the Whole Word checkbox.
 *   <li>"SearchDialog.UseRegularExpressions" property change when the user checks/unchecks
 *       the "Regular Expressions" checkbox.
 *   <li>"SearchDialog.SearchDownward" property change when the user clicks either
 *       the Up or Down direction radio button.
 * </ul>
 * <p>The property events listed can all be ignored in a simple case; the Replace dialog will
 * remember the state of its checkboxes between invocations.  However, if your application
 * has both a Find and Replace dialog, you may wish to use these messages to synchronize
 * the two search dialogs' options.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ReplaceDialog extends AbstractFindReplaceDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JButton replaceButton;
	private JButton replaceAllButton;
	private JLabel replaceFieldLabel;

	private MaxWidthComboBox replaceWithCombo;

	// This helps us work around the "bug" where JComboBox eats the first Enter
	// press.
	private String lastSearchString;
	private String lastReplaceString;


	/**
	 * Creates a new <code>ReplaceDialog</code>.
	 *
	 * @param owner The main window that owns this dialog.
	 * @param actionListener The component that listens for "Replace" actions.
	 */
	public ReplaceDialog(Frame owner, ActionListener actionListener) {
		this(owner, actionListener,
			ResourceBundle.getBundle("org.fife.ui.search.Search"));
	}


	/**
	 * Creates a new <code>ReplaceDialog</code>.
	 *
	 * @param owner The main window that owns this dialog.
	 * @param actionListener The component that listens for "Replace" actions.
	 * @param resources The resource bundle from which to get strings, etc.
	 */
	public ReplaceDialog(Frame owner, ActionListener actionListener,
									ResourceBundle resources) {

		// Let it be known who the owner of this dialog is.
		super(owner, resources, true);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		// Create a panel for the "Find what" and "Replace with" text fields.
		JPanel searchPanel = new JPanel(new SpringLayout());

		// Create listeners for the combo boxes.
		ReplaceFocusAdapter replaceFocusAdapter = new ReplaceFocusAdapter();
		ReplaceKeyListener replaceKeyListener = new ReplaceKeyListener();
		ReplaceDocumentListener replaceDocumentListener = new ReplaceDocumentListener();

		// Create the "Find what" text field.
		JTextComponent textField = getTextComponent(findTextCombo);
		textField.addFocusListener(replaceFocusAdapter);
		textField.addKeyListener(replaceKeyListener);
		textField.getDocument().addDocumentListener(replaceDocumentListener);

		// Create the "Replace with" text field.
		replaceWithCombo = createSearchComboBox(true);
		textField = getTextComponent(replaceWithCombo);
		textField.addFocusListener(replaceFocusAdapter);
		textField.addKeyListener(replaceKeyListener);
		textField.getDocument().addDocumentListener(replaceDocumentListener);

		// Create the "Replace with" label.
		replaceFieldLabel = createLabel(resources, "ReplaceWith",
									replaceWithCombo);

		JPanel temp = new JPanel(new BorderLayout());
		temp.add(findTextCombo);
		AssistanceIconPanel aip = new AssistanceIconPanel(findTextCombo);
		temp.add(aip, BorderLayout.LINE_START);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(replaceWithCombo);
		AssistanceIconPanel aip2 = new AssistanceIconPanel(replaceWithCombo);
		temp2.add(aip2, BorderLayout.LINE_START);

		// Orient things properly.
		if (orientation.isLeftToRight()) {
			searchPanel.add(findFieldLabel);
			searchPanel.add(temp);
			searchPanel.add(replaceFieldLabel);
			searchPanel.add(temp2);
		}
		else {
			searchPanel.add(temp);
			searchPanel.add(findFieldLabel);
			searchPanel.add(temp2);
			searchPanel.add(replaceFieldLabel);
		}

		UIUtil.makeSpringCompactGrid(searchPanel, 2, 2,	//rows, cols
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
		leftPanel.add(searchPanel);
		leftPanel.add(bottomPanel);

		// Make a panel containing the action buttons.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(4,1, 5,5));
		replaceButton = UIUtil.createRButton(resources, "Replace", "ReplaceMnemonic");
		replaceButton.setActionCommand("Replace");
		replaceButton.addActionListener(this);
		replaceButton.setEnabled(false);
		replaceButton.setIcon(null);
		replaceButton.setToolTipText(null);
		replaceAllButton = UIUtil.createRButton(resources, "ReplaceAll",
											"ReplaceAllMnemonic");
		replaceAllButton.setActionCommand("ReplaceAll");
		replaceAllButton.addActionListener(this);
		replaceAllButton.setEnabled(false);
		replaceAllButton.setIcon(null);
		replaceAllButton.setToolTipText(null);
		buttonPanel.add(findNextButton);
		buttonPanel.add(replaceButton);
		buttonPanel.add(replaceAllButton);
		buttonPanel.add(cancelButton);		// Defined in superclass.
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(buttonPanel, BorderLayout.NORTH);

		// Put it all together!
		JPanel contentPane = new JPanel(new BorderLayout());
//		contentPane.setBorder(UIUtilities.getEmpty5Border());
contentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,0,5));
		contentPane.add(leftPanel);
		contentPane.add(rightPanel, BorderLayout.LINE_END);
		temp = new ResizableFrameContentPane(new BorderLayout());
		temp.add(contentPane, BorderLayout.NORTH);
		setContentPane(temp);
		getRootPane().setDefaultButton(findNextButton);
		setTitle(resources.getString("ReplaceDialogTitle"));
		setResizable(true);
		pack();
		setLocationRelativeTo(owner);

		applyComponentOrientation(orientation);

	}


	// Listens for an action in this Replace dialog.  Note that we don't have to check
	// the actionCommand since we do the same thing for all things we listen to.
	public void actionPerformed(ActionEvent e) {

		super.actionPerformed(e);

		findTextCombo.addItem(getTextComponent(findTextCombo).getText());

		// If they just searched for an item that's already in the list other
		// than the first, move it to the first position.
		if (findTextCombo.getSelectedIndex()>0) {
			Object item = findTextCombo.getSelectedItem();
			findTextCombo.removeItem(item);
			findTextCombo.insertItemAt(item, 0);
			findTextCombo.setSelectedIndex(0);
		}

		String replaceWithText =getTextComponent(replaceWithCombo).getText();
		if (!replaceWithText.equals(""))
			replaceWithCombo.addItem(replaceWithText);

		// If they just searched for an item that's already in the list other
		// than the first, move it to the first position.
		if (replaceWithCombo.getSelectedIndex()>0) {
			Object item = replaceWithCombo.getSelectedItem();
			replaceWithCombo.removeItem(item);
			replaceWithCombo.insertItemAt(item, 0);
			replaceWithCombo.setSelectedIndex(0);
		}

	}


	/**
	 * Adds an <code>ActionListener</code> to this dialog.  The listener will
	 * receive notification when the user clicks the "Find" button with an
	 * actionCommand string of "FindNext", an actionCommand string of "Replace"
	 * when the user clicks the "Replace" button, and an actionCommand string
	 * of "ReplaceAll" when the user clicks the "Replace All" button.
	 *
	 * @param l The listener to add.
	 * @see #removeActionListener
	 */
	public void addActionListener(ActionListener l) {
		findNextButton.addActionListener(l);
		replaceButton.addActionListener(l);
		replaceAllButton.addActionListener(l);
	}


	protected void escapePressed() {
		if (replaceWithCombo instanceof RegexAwareComboBox) {
			RegexAwareComboBox racb = (RegexAwareComboBox)replaceWithCombo;
			// Workaround for the strange behavior (Java bug?) that sometimes
			// the Escape keypress "gets through" from the AutoComplete's
			// registered key Actions, and gets to this EscapableDialog, which
			// hides the entire dialog.  Reproduce by doing the following:
			//   1. In an empty find field, press Ctrl+Space
			//   2. Type "\\".
			//   3. Press Escape.
			// The entire dialog will hide, instead of the completion popup.
			// Further, bringing the Find dialog back up, the completion popup
			// will still be visible.
			if (racb.hideAutoCompletePopups()) {
				return;
			}
		}
		super.escapePressed();
	}


	/**
	 * Returns the text on the "Replace" button.
	 *
	 * @return The text on the Replace button.
	 * @see #setReplaceButtonText
	 */
	public final String getReplaceButtonText() {
		return replaceButton.getText();
	}


	/**
	 * Returns the text on the "Replace All" button.
	 *
	 * @return The text on the Replace All button.
	 * @see #setReplaceAllButtonText
	 */
	public final String getReplaceAllButtonText() {
		return replaceAllButton.getText();
	}


	/**
	 * Returns the <code>java.lang.String</code> to replace with.
	 *
	 * @return The <code>java.lang.String</code> the user wants to replace
	 *         the text to find with.
	 */
	public String getReplaceString() {
		String text = (String)replaceWithCombo.getSelectedItem();
		if (text==null) { // possible from JComboBox
			text = "";
		}
		return text;
	}


	/**
	 * Returns the label on the "Replace with" text field.
	 *
	 * @return The text on the "Replace with" text field.
	 * @see #setReplaceWithLabelText
	 */
	public final String getReplaceWithLabelText() {
		return replaceFieldLabel.getText();
	}


	/**
	 * Called when the regex checkbox is clicked.  Subclasses can override
	 * to add custom behavior, but should call the super implementation.
	 */
	protected void handleRegExCheckBoxClicked() {

		super.handleRegExCheckBoxClicked();

		// "Content assist" support
		boolean b = regExpCheckBox.isSelected();
		// Always true except when debugging.  findTextCombo done in parent
		if (replaceWithCombo instanceof RegexAwareComboBox) {
			RegexAwareComboBox racb = (RegexAwareComboBox)replaceWithCombo;
			racb.setAutoCompleteEnabled(b);
		}

	}


	protected EnableResult handleToggleButtons() {
		EnableResult er = super.handleToggleButtons();
		replaceButton.setEnabled(er.getEnable());
		replaceAllButton.setEnabled(er.getEnable());
		return er;
	}


	/**
	 * Removes an <code>ActionListener</code> from this dialog.
	 *
	 * @param l The listener to remove
	 * @see #addActionListener
	 */
	public void removeActionListener(ActionListener l) {
		findNextButton.removeActionListener(l);
		replaceButton.removeActionListener(l);
		replaceAllButton.removeActionListener(l);
	}


	/**
	 * Sets the text on the "Replace" button.
	 *
	 * @param text The text for the Replace button.
	 * @see #getReplaceButtonText
	 */
	public final void setReplaceButtonText(String text) {
		replaceButton.setText(text);
	}


	/**
	 * Sets the text on the "Replace All" button.
	 *
	 * @param text The text for the Replace All button.
	 * @see #getReplaceAllButtonText
	 */
	public final void setReplaceAllButtonText(String text) {
		replaceAllButton.setText(text);
	}


	/**
	 * Sets the label on the "Replace with" text field.
	 *
	 * @param text The text for the "Replace with" text field's label.
	 * @see #getReplaceWithLabelText
	 */
	public final void setReplaceWithLabelText(String text) {
		replaceFieldLabel.setText(text);
	}


	/**
	 * Sets the <code>java.lang.String</code> to replace with
	 *
	 * @param newReplaceString The <code>java.lang.String</code> to put into
	 *        the replace field.
	 */
	public void setReplaceString(String newReplaceString) {
		replaceWithCombo.addItem(newReplaceString);
		replaceWithCombo.setSelectedIndex(0);
	}


	/**
	 * Overrides <code>JDialog</code>'s <code>setVisible</code> method; decides
	 * whether or not buttons are enabled.
	 *
	 * @param visible Whether or not the dialog should be visible.
	 */
	public void setVisible(boolean visible) {

		if (visible) {

			// Make sure content assist is enabled (regex check box might have
			// been checked in a different search dialog).
			if (visible) {
				boolean regexEnabled = regExpCheckBox.isSelected();
				// Always true except when debugging.  findTextCombo done in parent
				if (replaceWithCombo instanceof RegexAwareComboBox) {
					RegexAwareComboBox racb = (RegexAwareComboBox)replaceWithCombo;
					racb.setAutoCompleteEnabled(regexEnabled);
				}
			}

			String selectedItem = (String)findTextCombo.getSelectedItem();
			if (selectedItem==null) {
				findNextButton.setEnabled(false);
				replaceButton.setEnabled(false);
				replaceAllButton.setEnabled(false);
			}
			else {
				handleToggleButtons();
			}

			// Call JDialog's setVisible().
			super.setVisible(true);

			// Make the "Find" text field active.
			JTextComponent textField = getTextComponent(findTextCombo);
			textField.requestFocusInWindow();
			textField.selectAll();

		}

		else {
			super.setVisible(false);
		}

	}


	/**
	 * Called whenever the user changes the Look and Feel, etc.
	 * This is overridden so we can reinstate the listeners that are evidently
	 * lost on the JTextField portion of our combo box.
	*/
	public void updateUI() {

		// Create listeners for the combo boxes.
		ReplaceFocusAdapter replaceFocusAdapter = new ReplaceFocusAdapter();
		ReplaceKeyListener replaceKeyListener = new ReplaceKeyListener();
		ReplaceDocumentListener replaceDocumentListener = new ReplaceDocumentListener();

		// Fix the Find What combo box's listeners.
		JTextComponent textField = getTextComponent(findTextCombo);
		textField.addFocusListener(replaceFocusAdapter);
		textField.addKeyListener(replaceKeyListener);
		textField.getDocument().addDocumentListener(replaceDocumentListener);

		// Fix the Replace With combo box's listeners.
		textField = getTextComponent(replaceWithCombo);
		textField.addFocusListener(replaceFocusAdapter);
		textField.addKeyListener(replaceKeyListener);
		textField.getDocument().addDocumentListener(replaceDocumentListener);

	}


	/**
	 * Listens for changes in the text field (find search field).
	 */
	private class ReplaceDocumentListener implements DocumentListener {

		public void insertUpdate(DocumentEvent e) {
			JTextComponent findWhatTextField = getTextComponent(findTextCombo);
			if (e.getDocument().equals(findWhatTextField.getDocument())) {
				handleToggleButtons();
			}
		}

		public void removeUpdate(DocumentEvent e) {
			JTextComponent findWhatTextField = getTextComponent(findTextCombo);
			if (e.getDocument().equals(findWhatTextField.getDocument()) && e.getDocument().getLength()==0) {
				findNextButton.setEnabled(false);
				replaceButton.setEnabled(false);
				replaceAllButton.setEnabled(false);
			}
			else {
				handleToggleButtons();
			}
		}

		public void changedUpdate(DocumentEvent e) {
		}

	}


	/**
	 * Listens for the text fields gaining focus.
	 */
	private class ReplaceFocusAdapter extends FocusAdapter {

		public void focusGained(FocusEvent e) {

			JTextComponent textField = (JTextComponent)e.getSource();
			textField.selectAll();

			if (textField==getTextComponent(findTextCombo)) {
				// Remember what it originally was, in case they tabbed out.
				lastSearchString = (String)findTextCombo.getSelectedItem();
			}
			else { // if (textField==getTextComponent(replaceWithComboBox)).
				// Remember what it originally was, in case they tabbed out.
				lastReplaceString = (String)replaceWithCombo.getSelectedItem();
			}

		}

	}


	/**
	 * Listens for key presses in the replace dialog.
	 */
	private class ReplaceKeyListener implements KeyListener {

		// Listens for the user pressing a key down.
		public void keyPressed(KeyEvent e) {
		}

		// Listens for a user releasing a key.
		public void keyReleased(KeyEvent e) {

			// This is an ugly hack to get around JComboBox's
			// insistance on eating the first Enter keypress
			// it receives when it has focus.
			if (e.getKeyCode()==KeyEvent.VK_ENTER && isPreJava6JRE()) {
				if (e.getSource()==getTextComponent(findTextCombo)) {
					String replaceString = (String)replaceWithCombo.getSelectedItem();
					lastReplaceString = replaceString;	// Just in case it changed too.
					String searchString = (String)findTextCombo.getSelectedItem();
					if (!searchString.equals(lastSearchString)) {
						findNextButton.doClick(0);
						lastSearchString = searchString;
						getTextComponent(findTextCombo).selectAll();
					}
				}
				else { // if (e.getSource()==getTextComponent(replaceWithComboBox)) {
					String searchString = (String)findTextCombo.getSelectedItem();
					lastSearchString = searchString;	// Just in case it changed too.
					String replaceString = (String)replaceWithCombo.getSelectedItem();
					if (!replaceString.equals(lastReplaceString)) {
						findNextButton.doClick(0);
						lastReplaceString = replaceString;
						getTextComponent(replaceWithCombo).selectAll();
					}
				}
			}

		}

		// Listens for a key being typed.
		public void keyTyped(KeyEvent e) {
		}

	}


}