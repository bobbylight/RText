/*
 * 11/14/2003
 *
 * ReplaceNextAction.java - Action in RText to replace text with new text.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.regex.PatternSyntaxException;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;


/**
 * Action used by an <code>AbstractMainView</code> to replace text.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ReplaceNextAction extends ReplaceAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public ReplaceNextAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, icon, "ReplaceNextAction");
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		ensureSearchDialogsCreated();
		RText rtext = (RText)getApplication();
		AbstractMainView mainView = rtext.getMainView();
		ReplaceDialog replaceDialog = mainView.replaceDialog;

		// If it's nothing (ie, they haven't searched yet), bring up the
		// Replace dialog.
		if (mainView.searchStrings.size()==0 && !replaceDialog.isVisible()
				&& !mainView.findDialog.isVisible()) {
			replaceDialog.setVisible(true);
			return;
		}

		// Otherwise, repeat the last Replace action.
		RTextEditorPane textArea = mainView.getCurrentTextArea();
		String searchString = "";

		// Get the text to search for.
		if (replaceDialog.isVisible()) {
			mainView.searchStrings = replaceDialog.getSearchStrings();
			searchString = replaceDialog.getSearchString();
		}
		// Otherwise, mainView.searchStrings already has a value, but we
		// still need to give a value to searchString.
		else {
			searchString = (String)mainView.searchStrings.get(0);
		}

		try {

			SearchContext context = mainView.searchContext;
			boolean found = SearchEngine.replace(textArea, context);
			if (!found) {
				searchString = RTextUtilities.escapeForHTML(searchString, null);
				String temp = rtext.getString("CannotFindString", searchString);
				// "null" parent returns focus to previously focused window,
				// whether it be RText, the Find dialog or the Replace dialog.
				JOptionPane.showMessageDialog(null, temp,
							rtext.getString("InfoDialogHeader"),
							JOptionPane.INFORMATION_MESSAGE);
			}

			// If find and replace dialogs aren't up, give text area focus.
			if (!mainView.findDialog.isVisible() &&
					!replaceDialog.isVisible()) {
				textArea.requestFocusInWindow();
			}

		} catch (PatternSyntaxException pse) {
			// There was a problem with the user's regex search string.
			// Won't usually happen; should be caught earlier.
			JOptionPane.showMessageDialog(rtext,
			"Invalid regular expression:\n" + pse.toString() +
			"\nPlease check your regular expression search string.",
			"Error", JOptionPane.ERROR_MESSAGE);
		} catch (IndexOutOfBoundsException ioobe) {
			// The user's regex replacement string referenced an
			// invalid group.
			JOptionPane.showMessageDialog(rtext,
			"Invalid group reference in replacement string:\n" +
			ioobe.getMessage(),
			"Error", JOptionPane.ERROR_MESSAGE);
		}

	}


}