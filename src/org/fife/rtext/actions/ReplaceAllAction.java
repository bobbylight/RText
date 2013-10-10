/*
 * 11/14/2003
 *
 * ReplaceAllAction.java - Action to replace all occurences of a given string
 * with a new string in RText.
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
import org.fife.ui.app.StandardAction;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;


/**
 * Action used by an <code>AbstractMainView</code> to replace all occurrences
 * of a given text string with new text.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ReplaceAllAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public ReplaceAllAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "ReplaceAllAction");
		setIcon(icon);
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action event.
	 */
	public void actionPerformed(ActionEvent e) {

		RText rtext = (RText)getApplication();
		AbstractMainView mainView = rtext.getMainView();
		RTextEditorPane textArea = mainView.getCurrentTextArea();

		// Next, initialize some variables for this action.
		SearchContext context = mainView.searchContext;
		String searchString = context.getSearchFor();

		// Do the replacement.
		try {

			SearchResult result = SearchEngine.replaceAll(textArea, context);
			int count = result.getCount();

			if (count==-1) {
				// TODO: Display an error about bad regex.
			}
			else if (count>0) {
				String temp = rtext.getString("ReplacedNOccString",
							Integer.toString(count), searchString);
				JOptionPane.showMessageDialog(rtext, temp,
								rtext.getString("InfoDialogHeader"),
								JOptionPane.INFORMATION_MESSAGE);
			}
			else { // count==0.
				searchString = RTextUtilities.escapeForHTML(searchString, null);
				String temp = rtext.getString("CannotFindString", searchString);
				// "null" parent returns focus to previously focused window,
				// whether it be RText, the Find dialog or the Replace dialog.
				JOptionPane.showMessageDialog(null, temp,
								rtext.getString("InfoDialogHeader"),
								JOptionPane.INFORMATION_MESSAGE);
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