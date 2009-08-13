/*
 * 11/14/2003
 *
 * FindNextAction.java - Action to search for text again in RText.
 * Copyright (C) 2003 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
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
package org.fife.rtext;

import java.awt.event.ActionEvent;
import java.util.regex.PatternSyntaxException;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.search.FindDialog;
import org.fife.ui.search.ReplaceDialog;


/**
 * Action used by an {@link AbstractMainView} to search for text.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FindNextAction extends FindAction {


	/**
	 * Creates a new <code>FindNextAction</code>.
	 *
	 * @param rtext The <code>RText</code> that owns the
	 *        <code>FindDialog</code>.
	 * @param text The text associated with the action.
	 * @param icon The icon associated with the action.
	 * @param desc The description of the action.
	 * @param mnemonic The mnemonic for the action.
	 * @param accelerator The accelerator key for the action.
	 */
	public FindNextAction(RText rtext, String text, Icon icon,
				String desc, int mnemonic, KeyStroke accelerator) {
		super(rtext, text, icon, desc, mnemonic, accelerator);
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action event.
	 */
	public void actionPerformed(ActionEvent e) {

		ensureSearchDialogsCreated();
		RText rtext = (RText)getApplication();
		AbstractMainView mainView = rtext.getMainView();

		// Do this just once for performance.
		FindDialog findDialog = mainView.findDialog;

		// If the current text string is nothing (ie, they haven't searched
		// yet), bring up Find dialog.
		if (mainView.searchStrings.size()==0 && !findDialog.isVisible() &&
				!mainView.replaceDialog.isVisible()) {
			findDialog.setSearchParameters(mainView.searchStrings,
									mainView.searchMatchCase,
									mainView.searchWholeWord,
									mainView.searchRegExpression,
									!mainView.searchingForward,
									mainView.searchMarkAll);
			findDialog.setVisible(true);
			return;
		}

		// Otherwise, repeat the last Find action.
		RTextEditorPane textArea = mainView.getCurrentTextArea();
		String searchString = null;

		// Get the text last searched for.
		if (findDialog.isVisible()) {
			mainView.searchStrings = findDialog.getSearchStrings();
			searchString = findDialog.getSearchString();
		}
		else if (mainView.replaceDialog.isVisible()) {
			ReplaceDialog replaceDialog = mainView.replaceDialog;
			mainView.searchStrings = replaceDialog.getSearchStrings();
			searchString = replaceDialog.getSearchString();
		}
		// else, mainView.searchStrings should already have a value (see
		// above), but we still need to give a value to searchString.
		else {
			searchString = (String)mainView.searchStrings.get(0);
		}

		try {

			// If "mark all" is selected, first mark all occurances in
			// the text area, then do the "find" (so that the next
			// occurance is indeed selected).
			textArea.clearMarkAllHighlights(); // Always remove old stuff.
			if (mainView.searchMarkAll) {
				textArea.markAll(searchString,
								mainView.searchMatchCase,
								mainView.searchWholeWord,
								mainView.searchRegExpression);
			}

			boolean found = SearchEngine.find(textArea, searchString,
								mainView.searchingForward,
								mainView.searchMatchCase,
								mainView.searchWholeWord,
								mainView.searchRegExpression);
			if (!found) {
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