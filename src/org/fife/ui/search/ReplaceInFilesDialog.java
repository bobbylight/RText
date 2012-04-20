/*
 * 9/19/2006
 *
 * ReplaceInFilesDialog.java - A dialog that replaces instances of text
 * across multiple files.
 * Copyright (C) 2006 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.search;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import org.fife.rtext.AssistanceIconPanel;
import org.fife.ui.*;


/**
 * Dialog that does string replacement across multiple files.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class ReplaceInFilesDialog extends FindInFilesDialog {

	protected MaxWidthComboBox replaceCombo;


	/**
	 * Creates a new <code>ReplaceInFilesDialog</code>.
	 *
	 * @param owner The main window that owns this dialog.
	 */
	public ReplaceInFilesDialog(Frame owner) {
		this(owner, ResourceBundle.getBundle("org.fife.ui.search.Search"));
	}


	/**
	 * Creates a new <code>ReplaceInFilesDialog</code>.
	 *
	 * @param owner The owner of this dialog.
	 * @param msg The resource bundle.
	 */
	public ReplaceInFilesDialog(Frame owner, ResourceBundle msg) {
		super(owner, msg);
		this.setTitle(msg.getString("ReplaceInFilesDialogTitle"));
		findButton.setText(msg.getString("Replace"));
		findButton.setMnemonic((int)msg.getString("ReplaceMnemonic").charAt(0));
	}


	/**
	 * Creates the panel containing "Report Detail" options.
	 *
	 * @param msg The resource bundle.
	 * @return The panel.
	 */
	protected JPanel createDetailsPanel(ResourceBundle msg) {

		// Make a panel containing the "Report detail" panel and some checkboxes.
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(Box.createVerticalStrut(5));
		subfoldersCheckBox = new JCheckBox(msg.getString("SearchSubfolders"));
		subfoldersCheckBox.setMnemonic((int)msg.getString("SearchSubfoldersMnemonic").charAt(0));
		panel.add(subfoldersCheckBox);
		verboseCheckBox = new JCheckBox(msg.getString("Verbose"));
		verboseCheckBox.setMnemonic((int)msg.getString("VerboseMnemonic").charAt(0));
		panel.add(verboseCheckBox);
		panel.add(Box.createVerticalGlue());

		return panel;

	}


	/**
	 * Returns a panel containing any extra options, such as a "verbose"
	 * output option.
	 *
	 * @param msg The resource bundle.
	 * @return The panel, or <code>null</code> if there are no extra
	 *         options.
	 */
	protected JPanel createExtraOptionsPanel(ResourceBundle msg) {
		return null;
	}


	/**
	 * Creates and returns the panel containing input fields and their
	 * labels.
	 *
	 * @param msg The resource bundle.
	 * @return The panel.
	 */
	protected JPanel createInputPanel(ResourceBundle msg) {

		JPanel inputPanel = super.createInputPanel(msg);

		JLabel replaceLabel = new JLabel(msg.getString("ReplaceWith"));
		replaceLabel.setDisplayedMnemonic((int)msg.getString("ReplaceWithMnemonic").charAt(0));
		replaceCombo = createSearchComboBox(true);
		replaceLabel.setLabelFor(replaceCombo);

		JPanel temp = new JPanel(new BorderLayout());
		temp.add(replaceCombo);
		AssistanceIconPanel aip = new AssistanceIconPanel(replaceCombo);
		temp.add(aip, BorderLayout.LINE_START);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
		if (orientation.isLeftToRight()) {
			inputPanel.add(replaceLabel, 2);
			inputPanel.add(temp, 3);
		}
		else {
			inputPanel.add(temp, 2);
			inputPanel.add(replaceLabel, 3);
		}

		UIUtil.makeSpringCompactGrid(inputPanel,
									4,2,		// rows,cols,
									0,0,		// initial-x, initial-y,
									5,5);	// x-spacing, y-spacing.

		return inputPanel;

	}


	/**
	 * Creates and returns the component used to display search
	 * results.
	 *
	 * @return The component.
	 */
	protected ResultsComponent createResultsComponent() {
		ReplaceInFilesTable table = new ReplaceInFilesTable();
		table.addMouseListener(new FindInFilesDialogMouseListener(table));
		return table;
	}


	/**
	 * Returns the thread that will do the searching.
	 *
	 * @param directory The directory to search in.
	 * @return The thread.
	 */
	protected FindInFilesThread createWorkerThread(File directory) {
		return new ReplaceInFilesThread(this, directory);
	}


	protected void escapePressed() {
		if (replaceCombo instanceof RegexAwareComboBox) {
			RegexAwareComboBox racb = (RegexAwareComboBox)replaceCombo;
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
	 * Returns the text to replace with.
	 *
	 * @return The text the user wants to replace with.
	 */
	public String getReplaceString() {
		return (String)replaceCombo.getSelectedItem();
	}


	/**
	 * Called when the regex checkbox is clicked.
	 */
	protected void handleRegExCheckBoxClicked() {

		super.handleRegExCheckBoxClicked();

		// "Content assist" support
		boolean b = regExpCheckBox.isSelected();
		// Always true except when debugging.  findTextCombo done in parent
		if (replaceCombo instanceof RegexAwareComboBox) {
			RegexAwareComboBox racb = (RegexAwareComboBox)replaceCombo;
			racb.setAutoCompleteEnabled(b);
		}

	}


	/**
	 * Enables or disables widgets in the dialog as appropriate.
	 *
	 * @param searching Whether searching is starting.
	 */
	protected void setSearching(boolean searching) {
		super.setSearching(searching);
		boolean enabled = !searching;
		replaceCombo.setEnabled(enabled);
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
			if (replaceCombo instanceof RegexAwareComboBox) {
				RegexAwareComboBox racb = (RegexAwareComboBox)replaceCombo;
				racb.setAutoCompleteEnabled(regexEnabled);
			}
		}

		super.setVisible(visible);

	}


}