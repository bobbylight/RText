/*
 * 04/08/2004
 *
 * AbstractSearchDialog.java - Base class for all search dialogs
 * (find, replace, etc.).
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

import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import org.fife.ui.EscapableDialog;
import org.fife.ui.MaxWidthComboBox;
import org.fife.ui.RButton;
import org.fife.ui.UIUtil;


/**
 * Base class for all search dialogs (find, replace, find in files, etc.).
 * This class is not useful on its own; you should use either FindDialog
 * or ReplaceDialog, or extend this class to create your own search
 * dialog.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class AbstractSearchDialog extends EscapableDialog
							implements ActionListener {

	private static final long serialVersionUID = 1L;

	public static final String MATCH_CASE_PROPERTY		= "SearchDialog.MatchCase";
	public static final String MATCH_WHOLE_WORD_PROPERTY	= "SearchDialog.MatchWholeWord";
	public static final String USE_REG_EX_PROPERTY		= "SearchDialog.UseRegularExpressions";


	// Conditions check boxes and the panel they go in.
	// This should be added in the actual layout of the search dialog.
	protected JCheckBox caseCheckBox;
	protected JCheckBox wholeWordCheckBox;
	protected JCheckBox regExpCheckBox;
	protected JPanel searchConditionsPanel;

	/**
	 * The image to use beside a text component when content assist is
	 * available.
	 */
	private static Image contentAssistImage;

	/**
	 * The combo box where the user enters the text for which to search.
	 */
	protected JComboBox findTextCombo;

	// Miscellaneous other stuff.
	protected JButton cancelButton;


	/**
	 * Constructor.  Does initializing for parts common to all search
	 * dialogs.
	 *
	 * @param owner The window that owns this search dialog.
	 * @param msg The resource bundle from which to get strings, etc.
	 * @param useRButtons If <code>true</code>, then
	 *        <code>org.fife.ui.RButton</code>s will be used for all buttons
	 *        defined here (currently just the Cancel button).  Otherwise,
	 *        regular <code>JButton</code>s are used.
	 */
	public AbstractSearchDialog(Frame owner, ResourceBundle msg,
										boolean useRButtons) {

		super(owner);

		// Make a panel containing the option checkboxes.
		searchConditionsPanel = new JPanel();
		searchConditionsPanel.setLayout(new BoxLayout(
						searchConditionsPanel, BoxLayout.Y_AXIS));
		caseCheckBox = createCheckBox(msg, "MatchCase");
		searchConditionsPanel.add(caseCheckBox);
		wholeWordCheckBox = createCheckBox(msg, "WholeWord");
		searchConditionsPanel.add(wholeWordCheckBox);
		regExpCheckBox = createCheckBox(msg, "RegEx");
		searchConditionsPanel.add(regExpCheckBox);

		// Initialize any text fields.
		findTextCombo = createSearchComboBox(false);

		// Initialize other stuff.
		if (useRButtons) {
			cancelButton = new RButton(msg.getString("Cancel"));
		}
		else {
			cancelButton = new JButton(msg.getString("Cancel"));
		}
		cancelButton.setMnemonic((int)msg.getString("CancelMnemonic").charAt(0));
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		
	}


	/**
	 * Listens for actions in this search dialog.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		// They check/uncheck the "Match Case" checkbox on the Find dialog.
		if (command.equals("FlipMatchCase")) {
			boolean matchCase = caseCheckBox.isSelected();
			firePropertyChange(MATCH_CASE_PROPERTY, !matchCase, matchCase);
		}

		// They check/uncheck the "Whole word" checkbox on the Find dialog.
		else if (command.equals("FlipWholeWord")) {
			boolean wholeWord = wholeWordCheckBox.isSelected();
			firePropertyChange(MATCH_WHOLE_WORD_PROPERTY, !wholeWord, wholeWord);
		}

		// They check/uncheck the "Regular expression" checkbox.
		else if (command.equals("FlipRegEx")) {
			boolean useRegEx = regExpCheckBox.isSelected();
			firePropertyChange(USE_REG_EX_PROPERTY, !useRegEx, useRegEx);
		}

		// If they press the "Cancel" button.
		else if (command.equals("Cancel")) {
			setVisible(false);
		}

		// Also possibly toggle button status.
		if (command.equals("FlipRegEx")) {
			handleRegExCheckBoxClicked();
		}

	}


	private JCheckBox createCheckBox(ResourceBundle msg, String keyRoot) {
		JCheckBox cb = new JCheckBox(msg.getString(keyRoot));
		cb.setMnemonic((int)msg.getString(keyRoot + "Mnemonic").charAt(0));
		cb.setActionCommand("Flip" + keyRoot);
		cb.addActionListener(this);
		return cb;
	}


	/**
	 * Returns a combo box suitable for a "search in" or "replace with"
	 * field. Subclasses can override to provide combo boxes with enhanced
	 * functionality.
	 *
	 * @param replace Whether this is a "replace" combo box (as opposed to a
	 *        "find" combo box).  This affects what content assistance they
	 *        receive.
	 * @return The combo box.
	 */
	protected MaxWidthComboBox createSearchComboBox(boolean replace) {
		MaxWidthComboBox combo = new RegexAwareComboBox(replace);
		UIUtil.fixComboOrientation(combo);
		return combo;
	}


	/**
	 * Returns a titled border for panels on search dialogs.
	 *
	 * @param title The title for the border.
	 * @return The border.
	 */
	protected Border createTitledBorder(String title) {
		if (title!=null && title.charAt(title.length()-1)!=':')
			title += ":";
		return BorderFactory.createTitledBorder(title);
	}


	protected void escapePressed() {
		if (findTextCombo instanceof RegexAwareComboBox) {
			RegexAwareComboBox racb = (RegexAwareComboBox)findTextCombo;
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
	 * Returns the text on the Cancel button.
	 *
	 * @return The text on the Cancel button.
	 * @see #setCancelButtonText
	 */
	public final String getCancelButtonText() {
		return cancelButton.getText();
	}


	/**
	 * Returns the image to display beside text components when content assist
	 * is available.
	 *
	 * @return The image to use.
	 */
	static Image getContentAssistImage() {
		if (contentAssistImage==null) {
			ClassLoader cl = AbstractSearchDialog.class.getClassLoader();
			URL url = cl.getResource("org/fife/rtext/graphics/common_icons/lightbulb.png");
			try {
				contentAssistImage = ImageIO.read(url);
			} catch (IOException ioe) { // Never happens
				ioe.printStackTrace();
			}
		}
		return contentAssistImage;
	}


	/**
	 * Returns the text for the "Match Case" check box.
	 *
	 * @return The text for the "Match Case" check box.
	 * @see #setMatchCaseCheckboxText
	 */
	public final String getMatchCaseCheckboxText() {
		return caseCheckBox.getText();
	}


	/**
	 * Returns the text for the "Regular Expression" check box.
	 *
	 * @return The text for the "Regular Expression" check box.
	 * @see #setRegularExpressionCheckboxText
	 */
	public final String getRegularExpressionCheckboxText() {
		return regExpCheckBox.getText();
	}


	/**
	 * Returns the text to search for.
	 *
	 * @return The text the user wants to search for.
	 */
	public String getSearchString() {
		return (String)findTextCombo.getSelectedItem();
	}


	/**
	 * Returns the <code>Strings</code> contained in the "Find what" combo
	 * box.
	 *
	 * @return A <code>java.util.Vector</code> of strings found in the "Find
	 *         what" combo box.  If that combo box is empty, than a
	 *         zero-length <code>Vector</code> is returned.
	 */
	public Vector getSearchStrings() {

		// First, ensure that the item in the combo box editor is indeed in the combo box.
		int selectedIndex = findTextCombo.getSelectedIndex();
		if (selectedIndex==-1) {
			findTextCombo.addItem(getSearchString());
		}

		// If they just searched for an item that's already in the list other than
		// the first, move it to the first position.
		else if (selectedIndex>0) {
			Object item = findTextCombo.getSelectedItem();
			findTextCombo.removeItem(item);
			findTextCombo.insertItemAt(item, 0);
			findTextCombo.setSelectedIndex(0);
		}


		int itemCount = findTextCombo.getItemCount();
		Vector vector = new Vector(itemCount);
		for (int i=0; i<itemCount; i++)
			vector.add(findTextCombo.getItemAt(i));
		return vector;

	}


	/**
	 * Returns the text editor component for the specified combo box.
	 *
	 * @param combo The combo box.
	 * @return The text component.
	 */
	protected static JTextComponent getTextComponent(JComboBox combo) {
		return (JTextComponent)combo.getEditor().getEditorComponent();
	}


	/**
	 * Returns the text for the "Whole Word" check box.
	 *
	 * @return The text for the "Whole Word" check box.
	 * @see #setWholeWordCheckboxText
	 */
	public final String getWholeWordCheckboxText() {
		return wholeWordCheckBox.getText();
	}


	/**
	 * Called when the regex checkbox is clicked.  Subclasses can override
	 * to add custom behavior, but should call the super implementation.
	 */
	protected void handleRegExCheckBoxClicked() {

		handleToggleButtons();

		// "Content assist" support
		boolean b = regExpCheckBox.isSelected();
		// Always true except when debugging
		if (findTextCombo instanceof RegexAwareComboBox) {
			RegexAwareComboBox racb = (RegexAwareComboBox)findTextCombo;
			racb.setAutoCompleteEnabled(b);
		}

	}


	/**
	 * Returns whether any action-related buttons (Find Next, Replace, etc.)
	 * should be enabled.  Subclasses can call this method when the "Find What"
	 * or "Replace With" text fields are modified.  They can then
	 * enable/disable any components as appropriate.
	 *
	 * @return Whether the buttons should be enabled.
	 */
	protected EnableResult handleToggleButtons() {

		//String text = getSearchString();
		JTextComponent tc = getTextComponent(findTextCombo);
		String text = tc.getText();
		if (text.length()==0) {
			return new EnableResult(false, null);
		}
		if (regExpCheckBox.isSelected()) {
			try {
				Pattern.compile(text);
			} catch (PatternSyntaxException pse) {
				return new EnableResult(false, pse.getMessage());
			}
		}
		return new EnableResult(true, null);
	}


	/**
	 * This method allows us to check if the current JRE is 1.4 or 1.5.
	 * This is used to workaround some Java bugs, for example, pre 1.6,
	 * JComboBoxes would "swallow" enter keypresses in them when their
	 * content changed.  This causes the user to have to press Enter twice
	 * when entering text to search for in a "Find" dialog, so instead we
	 * detect if a JRE is old enough to have this behavior and, if so,
	 * programmitcally press the Find button.
	 *
	 * @return Whether this is a 1.4 or 1.5 JRE.
	 */
	protected static boolean isPreJava6JRE() {
		// We only support 1.4+, so no need to check 1.3, etc.
		String version = System.getProperty("java.specification.version");
		return version.startsWith("1.5") || version.startsWith("1.4");
	}


	/**
	 * Returns whether the characters on either side of
	 * <code>substr(searchIn,startPos,startPos+searchStringLength)</code>
	 * are whitespace.  While this isn't the best definition of "whole word",
	 * it's the one we're going to use for now.
	 */
	protected static final boolean isWholeWord(CharSequence searchIn,
											int offset, int len) {

		boolean wsBefore, wsAfter;

		try {
			wsBefore = Character.isWhitespace(searchIn.charAt(offset - 1));
		} catch (IndexOutOfBoundsException e) { wsBefore = true; }
		try {
			wsAfter  = Character.isWhitespace(searchIn.charAt(offset + len));
		} catch (IndexOutOfBoundsException e) { wsAfter = true; }

		return wsBefore && wsAfter;

	}


	/**
	 * Sets the text on the Cancel button.
	 *
	 * @param text The text for the Cancel button.
	 * @see #getCancelButtonText
	 */
	public final void setCancelButtonText(String text) {
		cancelButton.setText(text);
	}


	/**
	 * Sets the text for the "Match Case" check box.
	 *
	 * @param text The text for the "Match Case" check box.
	 * @see #getMatchCaseCheckboxText
	 */
	public final void setMatchCaseCheckboxText(String text) {
		caseCheckBox.setText(text);
	}


	/**
	 * Sets the text for the "Regular Expression" check box.
	 *
	 * @param text The text for the "Regular Expression" check box.
	 * @see #getRegularExpressionCheckboxText
	 */
	public final void setRegularExpressionCheckboxText(String text) {
		regExpCheckBox.setText(text);
	}


	/**
	 * Sets the <code>java.lang.String</code> to search for.
	 *
	 * @param newSearchString The <code>java.lang.String</code> to put into
	 *        the search field.
	 */
	public void setSearchString(String newSearchString) {
		findTextCombo.addItem(newSearchString);
		findTextCombo.setSelectedIndex(0);
	}


	/**
	 * {@inheritDoc}
	 */
	public void setVisible(boolean visible) {

		// Make sure content assist is enabled (regex check box might have
		// been checked in a different search dialog).
		if (visible) {
			boolean regexEnabled = regExpCheckBox.isSelected();
			// Always true except when debugging.  findTextCombo done in parent
			if (findTextCombo instanceof RegexAwareComboBox) {
				RegexAwareComboBox racb = (RegexAwareComboBox)findTextCombo;
				racb.setAutoCompleteEnabled(regexEnabled);
			}
		}

		super.setVisible(visible);

	}


	/**
	 * Sets the text for the "Whole Word" check box.
	 *
	 * @param text The text for the "Whole Word" check box.
	 * @see #getWholeWordCheckboxText
	 */
	public final void setWholeWordCheckboxText(String text) {
		wholeWordCheckBox.setText(text);
	}


	/**
	 * Returns the result of whether the "action" buttons such as "Find"
	 * and "Replace" should be enabled.
	 *
	 * @author Robert Futrell
	 */
	protected static class EnableResult {

		private boolean enable;
		private String tooltip;

		public EnableResult(boolean enable, String tooltip) {
			this.enable = enable;
			this.tooltip = tooltip;
		}

		public boolean getEnable() {
			return enable;
		}

		public String getToolTip() {
			return tooltip;
		}

		public void setEnable(boolean enable) {
			this.enable = enable;
		}

	}


}