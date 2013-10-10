/*
 * 11/14/2003
 *
 * FindNextAction.java - Action to search for text again in RText.
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

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;
import org.fife.rsta.ui.search.FindDialog;


/**
 * Action used by an {@link AbstractMainView} to search for text.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FindNextAction extends FindAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public FindNextAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, icon, "FindNextAction");
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
		FindDialog findDialog = mainView.findDialog;

		// If the current text string is nothing (ie, they haven't searched
		// yet), bring up Find dialog.
		SearchContext context = mainView.searchContext;
		String searchString = mainView.searchContext.getSearchFor();
		if (searchString==null || searchString.length()==0) {
			findDialog.setVisible(true);
			return;
		}

		// Otherwise, repeat the last Find action.
		RTextEditorPane textArea = mainView.getCurrentTextArea();

		try {

			SearchResult result = SearchEngine.find(textArea, context);
			if (!result.wasFound()) {
				searchString = RTextUtilities.escapeForHTML(searchString, null);
				String temp = rtext.getString("CannotFindString", searchString);
				// "null" parent returns focus to previously focused window,
				// whether it be RText, the Find dialog or the Replace dialog.
				JOptionPane.showMessageDialog(null, temp,
							rtext.getString("InfoDialogHeader"),
							JOptionPane.INFORMATION_MESSAGE);
			}

			// If find/replace dialogs aren't up, give text area focus.
			if (!findDialog.isVisible() &&
					!mainView.replaceDialog.isVisible())
				textArea.requestFocusInWindow();

		} catch (PatternSyntaxException pse) {
			// There was a problem with the user's regex search string.
			// Won't usually happen; should be caught earlier.
			JOptionPane.showMessageDialog(rtext,
			"Invalid regular expression:\n" + pse.toString() +
			"\nPlease check your regular expression search string.",
			"Error", JOptionPane.ERROR_MESSAGE);
		}

	}


}