/*
 * 04/08/2004
 *
 * AbstractFindReplaceSearchDialog.java - Base class for FindDialog and
 * ReplaceDialog.
 * Copyright (C) 2004 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.Vector;
import java.awt.Color;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.fife.ui.UIUtil;


/**
 * This is the base class for {@link org.fife.ui.search.FindDialog} and
 * {@link org.fife.ui.search.ReplaceDialog}. It is basically all of the features
 * common to the two dialogs that weren't taken care of in
 * {@link org.fife.ui.search.AbstractSearchDialog}.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public abstract class AbstractFindReplaceDialog extends AbstractSearchDialog
										implements ActionListener {

	public static final String MARK_ALL_PROPERTY			= "SearchDialog.MarkAll";
	public static final String SEARCH_DOWNWARD_PROPERTY	= "SearchDialog.SearchDownward";

	// The radio buttons for changing the search direction.
	protected JRadioButton upButton;
	protected JRadioButton downButton;
	protected JPanel dirPanel;
	private String dirPanelTitle;
	protected JLabel findFieldLabel;
	protected JButton findNextButton;

	/**
	 * The "mark all" check box.
	 */
	protected JCheckBox markAllCheckBox;


	/**
	 * Constructor.  Does initializing for parts common to
	 * <code>FindDialog</code> and <code>ReplaceDialog</code> that isn't
	 * taken care of in <code>AbstractSearchDialog</code>'s constructor.
	 *
	 * @param owner The window that owns this search dialog.
	 * @param msg The resource bundle to use for labels, etc.
	 * @param useRButtons If <code>true</code>, then
	 *        <code>org.fife.ui.RButton</code>s will be used for all buttons
	 *        defined here (currently just the Cancel button).  Otherwise,
	 *        regular <code>JButton</code>s are used.
	 */
	public AbstractFindReplaceDialog(Frame owner, ResourceBundle msg,
								boolean useRButtons) {

		super(owner, msg, useRButtons);

		// Make a panel containing the "search up/down" radio buttons.
		dirPanel = new JPanel();
		dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.LINE_AXIS));
		setSearchButtonsBorderText(msg.getString("Direction"));
		ButtonGroup bg = new ButtonGroup();
		upButton = new JRadioButton(msg.getString("Up"), false);
		upButton.setMnemonic((int)msg.getString("UpMnemonic").charAt(0));
		downButton = new JRadioButton(msg.getString("Down"), true);
		downButton.setMnemonic((int)msg.getString("DownMnemonic").charAt(0));
		upButton.setActionCommand("UpRadioButtonClicked");
		upButton.addActionListener(this);
		downButton.setActionCommand("DownRadioButtonClicked");
		downButton.addActionListener(this);
		bg.add(upButton);
		bg.add(downButton);
		dirPanel.add(upButton);
		dirPanel.add(downButton);

		// Initilialize the "mark all" button.
		markAllCheckBox = new JCheckBox(msg.getString("MarkAll"));
		markAllCheckBox.setMnemonic((int)msg.getString("MarkAllMnemonic").charAt(0));
		markAllCheckBox.setActionCommand("MarkAll");
		markAllCheckBox.addActionListener(this);

		// Rearrange the search conditions panel.
		searchConditionsPanel.removeAll();
		searchConditionsPanel.setLayout(new BorderLayout());
		JPanel temp = new JPanel();
		temp.setLayout(new BoxLayout(temp, BoxLayout.PAGE_AXIS));
		temp.add(caseCheckBox);
		temp.add(wholeWordCheckBox);
		searchConditionsPanel.add(temp, BorderLayout.LINE_START);
		temp = new JPanel();
		temp.setLayout(new BoxLayout(temp, BoxLayout.PAGE_AXIS));
		temp.add(regExpCheckBox);
		temp.add(markAllCheckBox);
		searchConditionsPanel.add(temp, BorderLayout.LINE_END);

		// Create the "Find what" label.
		findFieldLabel = createLabel(msg, "FindWhat", findTextCombo);

		// Create a "Find Next" button.
		findNextButton= UIUtil.createRButton(msg, "Find", "FindMnemonic");
		findNextButton.setActionCommand("FindNext");
		findNextButton.addActionListener(this);
		findNextButton.setDefaultCapable(true);
		findNextButton.setEnabled(false);	// Initially, nothing to look for.

	}


	/**
	 * Listens for action events in this dialog.
	 *
	 * @param e The event that occurred.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals("UpRadioButtonClicked")) {
			firePropertyChange(SEARCH_DOWNWARD_PROPERTY, true, false);
		}

		else if (command.equals("DownRadioButtonClicked")) {
			firePropertyChange(SEARCH_DOWNWARD_PROPERTY, false, true);
		}

		else if (command.equals("MarkAll")) {
			boolean checked = markAllCheckBox.isSelected();
			firePropertyChange(MARK_ALL_PROPERTY, !checked, checked);
		}

		else {
			super.actionPerformed(e);
		}

	}


	/**
	 * Adds an <code>ActionListener</code> to this dialog.  This method should
	 * be overridden so that search actions are sent to listeners.  For
	 * example, for a Replace dialog, all listeners should receive notification
	 * when the user clicks "Find", "Replace", or "Replace All".
	 *
	 * @param l The listener to add.
	 * @see #removeActionListener
	 */
	public abstract void addActionListener(ActionListener l);


	/**
	 * Changes the action listener from one component to another.
	 *
	 * @param fromPanel The old <code>ActionListener</code> to remove.
	 * @param toPanel The new <code>ActionListener</code> to add as an action
	 *        listener.
	 */
	public void changeActionListener(ActionListener fromPanel,
								ActionListener toPanel) {
		this.removeActionListener(fromPanel);
		this.addActionListener(toPanel);
	}


	/**
	 * Returns a label for a component.
	 *
	 * @param msg The resource bundle to use for localizations.
	 * @param key The root key into the resource bundle.
	 * @param comp The component this will be a label for.
	 * @return The label.
	 */
	protected JLabel createLabel(ResourceBundle msg, String key,
							JComponent comp) {
		JLabel label = new JLabel(msg.getString(key));
		int mnemonic = msg.getString(key + "Mnemonic").charAt(0);
		label.setDisplayedMnemonic(mnemonic);
		label.setLabelFor(comp);
		return label;
	}


	/**
	 * Returns the text for the "Down" radio button.
	 *
	 * @return The text for the "Down" radio button.
	 * @see #setDownRadioButtonText
	 */
	public final String getDownRadioButtonText() {
		return downButton.getText();
	}


	/**
	 * Returns the text on the "Find" button.
	 *
	 * @return The text on the Find button.
	 * @see #setFindButtonText
	 */
	public final String getFindButtonText() {
		return findNextButton.getText();
	}


	/**
	 * Returns the label on the "Find what" text field.
	 *
	 * @return The text on the "Find what" text field.
	 * @see #setFindWhatLabelText
	 */
	public final String getFindWhatLabelText() {
		return findFieldLabel.getText();
	}


	/**
	 * Returns the text for the search direction's radio buttons' border.
	 *
	 * @return The text for the search radio buttons' border.
	 * @see #setSearchButtonsBorderText
	 */
	public final String getSearchButtonsBorderText() {
		return dirPanelTitle;
	}


	/**
	 * Returns the text for the "Up" radio button.
	 *
	 * @return The text for the "Up" radio button.
	 * @see #setUpRadioButtonText
	 */
	public final String getUpRadioButtonText() {
		return upButton.getText();
	}


	protected EnableResult handleToggleButtons() {

		EnableResult er = super.handleToggleButtons();
		boolean enable = er.getEnable();

		findNextButton.setEnabled(enable);

		// setBackground doesn't show up with XP Look and Feel!
		//findTextComboBox.setBackground(enable ?
		//		UIManager.getColor("ComboBox.background") : Color.PINK);
		JTextComponent tc = getTextComponent(findTextCombo);
		tc.setForeground(enable ? UIManager.getColor("TextField.foreground") :
									Color.RED);

		String tooltip = er.getToolTip();
		if (tooltip!=null && tooltip.indexOf('\n')>-1) {
			tooltip = tooltip.replaceFirst("\\\n", "</b><br><pre>");
			tooltip = "<html><b>" + tooltip;
		}
		tc.setToolTipText(tooltip); // Always set, even if null

		return er;

	}


	/**
	 * Removes an <code>ActionListener</code> from this dialog.
	 *
	 * @param l The listener to remove
	 * @see #addActionListener
	 */
	public abstract void removeActionListener(ActionListener l);


	/**
	 * Sets the text label for the "Down" radio button.
	 *
	 * @param text The new text label for the "Down" radio button.
	 * @see #getDownRadioButtonText
	 */
	public void setDownRadioButtonText(String text) {
		downButton.setText(text);
	}


	/**
	 * Sets the text on the "Find" button.
	 *
	 * @param text The text for the Find button.
	 * @see #getFindButtonText
	 */
	public final void setFindButtonText(String text) {
		findNextButton.setText(text);
	}


	/**
	 * Sets the label on the "Find what" text field.
	 *
	 * @param text The text for the "Find what" text field's label.
	 * @see #getFindWhatLabelText
	 */
	public void setFindWhatLabelText(String text) {
		findFieldLabel.setText(text);
	}


	/**
	 * Sets the text for the search direction's radio buttons' border.
	 *
	 * @param text The text for the search radio buttons' border.
	 * @see #getSearchButtonsBorderText
	 */
	public final void setSearchButtonsBorderText(String text) {
		dirPanelTitle = text;
		dirPanel.setBorder(createTitledBorder(dirPanelTitle));
	}


	/**
	 * This function should be called to update match case, whole word, etc.
	 *
	 * @param searchString The string to be at the top of the combo box of
	 *        strings to search for.
	 * @param matchCase Whether or not to match case in the search.
	 * @param wholeWord Whether or not to look for <code>searchString</code>
	 *        as a separate word when searching.
	 * @param regExp Whether or not to treat <code>searchString</code> as a
	 *        regular expression when searching.
	 * @param searchUp Whether to search up or down.
	 * @param markAll Whether to mark all occurrences.
	 */
	public void setSearchParameters(String searchString, boolean matchCase,
									boolean wholeWord,
									boolean regExp, boolean searchUp,
									boolean markAll) {
		setSearchString(searchString);
		caseCheckBox.setSelected(matchCase);
		wholeWordCheckBox.setSelected(wholeWord);
		regExpCheckBox.setSelected(regExp);
		upButton.setSelected(searchUp);
		downButton.setSelected(!searchUp);
		markAllCheckBox.setSelected(markAll);
	}


	/**
	 * This function should be called to update match case, whole word, etc.
	 *
	 * @param findComboBoxStrings The strings that the "Find" combo box
	 *        should contain.
	 * @param matchCase Whether or not to match case in the search.
	 * @param wholeWord Whether or not to look for <code>searchString</code>
	 *        as a separate word when searching.
	 * @param regExp Whether or not to treat <code>searchString</code> as a
	 *        regular expression when searching.
	 * @param searchUp Whether to search up or down.
	 * @param markAll Whether to mark all occurrences.
	 */
	public void setSearchParameters(Vector findComboBoxStrings,
							boolean matchCase, boolean wholeWord,
							boolean regExp, boolean searchUp,
							boolean markAll) {
		findTextCombo.removeAllItems();
		int size = findComboBoxStrings.size();
		for (int i=size-1; i>=0; i--) {
			findTextCombo.addItem(findComboBoxStrings.get(i));
		}
		if (size>0)
			findTextCombo.setSelectedIndex(0);
		caseCheckBox.setSelected(matchCase);
		wholeWordCheckBox.setSelected(wholeWord);
		regExpCheckBox.setSelected(regExp);
		upButton.setSelected(searchUp);
		downButton.setSelected(!searchUp);
		markAllCheckBox.setSelected(markAll);
	}


	/**
	 * Sets the text label for the "Up" radio button.
	 *
	 * @param text The new text label for the "Up" radio button.
	 * @see #getUpRadioButtonText
	 */
	public void setUpRadioButtonText(String text) {
		upButton.setText(text);
	}


}