/*
 * 10/13/2013
 *
 * SearchManager.java - Manages search-related UI components for RText.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.util.regex.PatternSyntaxException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.fife.rsta.ui.CollapsibleSectionPanel;
import org.fife.rsta.ui.search.AbstractFindReplaceDialog;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.ReplaceToolBar;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;


/**
 * Handles the Find and Replace dialogs and search bars.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SearchManager {

	private RText rtext;
	private SearchingMode searchingMode;
	private FindDialog findDialog;
	private ReplaceDialog replaceDialog;
	public FindToolBar findToolBar;
	public ReplaceToolBar replaceToolBar;


	/**
	 * Constructor.
	 *
	 * @param rtext The parent application.
	 */
	public SearchManager(RText rtext) {
		this.rtext = rtext;
		setSearchingMode(SearchingMode.TOOLBARS);
	}


	/**
	 * Changes all listeners registered on search dialogs/tool bars from an
	 * old main view to the new one.
	 *
	 * @param fromView The old main view.
	 */
	public void changeSearchListener(AbstractMainView fromView) {
		AbstractMainView mainView = rtext.getMainView();
		if (findDialog!=null) {
			findDialog.removeSearchListener(fromView);
			findDialog.addSearchListener(mainView);
			replaceDialog.removeSearchListener(fromView);
			replaceDialog.addSearchListener(mainView);
		}
		if (findToolBar!=null) {
			findToolBar.removeSearchListener(fromView);
			replaceToolBar.addSearchListener(mainView);
		}
	}


	/**
	 * Configures the Find or Replace dialog.
	 *
	 * @param dialog Either the Find or Replace dialog.
	 */
	private void configureSearchDialog(AbstractFindReplaceDialog dialog) {
		AbstractMainView mainView = rtext.getMainView();
		dialog.setSearchContext(mainView.searchContext);
		rtext.registerChildWindowListeners(dialog);
	}


	/**
	 * Ensures the find and replace dialogs are created.
	 */
	private void ensureSearchDialogsCreated() {
		if (replaceDialog==null) {
			AbstractMainView mainView = rtext.getMainView();
			findDialog = new FindDialog(rtext, mainView);
			configureSearchDialog(findDialog);
			replaceDialog = new ReplaceDialog(rtext, mainView);
			configureSearchDialog(replaceDialog);
		}
	}


	/**
	 * Ensures the find and replace tool bars are created.
	 */
	private void ensureToolbarsCreated() {
		if (findToolBar==null) {
			AbstractMainView mainView = rtext.getMainView();
			CollapsibleSectionPanel csp = rtext.getCollapsibleSectionPanel();
			findToolBar = new FindToolBar(mainView);
			findToolBar.setSearchContext(mainView.searchContext);
			csp.addBottomComponent(findToolBar);
			replaceToolBar = new ReplaceToolBar(mainView);
			replaceToolBar.setSearchContext(mainView.searchContext);
			csp.addBottomComponent(replaceToolBar);
		}
	}


	/**
	 * Executes a "find" operation in the active editor.
	 *
	 * @see #replaceNext()
	 */
	public void findNext() {

		AbstractMainView mainView = rtext.getMainView();

		// If the current text string is nothing (ie, they haven't searched
		// yet), bring up Find dialog.
		SearchContext context = mainView.searchContext;
		String searchString = mainView.searchContext.getSearchFor();
		if (searchString==null || searchString.length()==0) {
			switch (searchingMode) {
				case DIALOGS:
					ensureSearchDialogsCreated();
					findDialog.setVisible(true);
					break;
				case TOOLBARS:
					ensureToolbarsCreated();
					rtext.getCollapsibleSectionPanel().
							showBottomComponent(findToolBar);
					break;
			}
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

			// If they used the "find next" tool bar button, make sure the
			// editor gets focused.
			if (isNoSearchUIVisible()) {
				textArea.requestFocusInWindow();
			}

		} catch (PatternSyntaxException pse) {
			// There was a problem with the user's regex search string.
			// Won't usually happen; should be caught earlier.
			JOptionPane.showMessageDialog(rtext,
			"Invalid regular expression:\n" + pse.toString() +
			"\nPlease check your regular expression search string.",
			"Error", JOptionPane.ERROR_MESSAGE);
		}

	}


	/**
	 * Returns whether dialogs or tool bars are used for searching.
	 *
	 * @return The searching mode.
	 * @see #setSearchingMode(SearchingMode)
	 */
	public SearchingMode getSearchingMode() {
		return searchingMode;
	}


	private void hideFindDialogIfVisible() {
		if (findDialog!=null && findDialog.isVisible()) {
			findDialog.setVisible(false);
		}
	}


	private void hideReplaceDialogIfVisible() {
		if (replaceDialog!=null && replaceDialog.isVisible()) {
			replaceDialog.setVisible(false);
		}
	}


	/**
	 * Returns whether any search UI (find or replace dialog or tool bar) is
	 * visible.
	 *
	 * @return Whether any search UI is visible.
	 */
	private boolean isNoSearchUIVisible() {
		return (findDialog==null || !findDialog.isVisible()) &&
				(replaceDialog==null || !replaceDialog.isVisible()) &&
				(rtext.getCollapsibleSectionPanel().
					getDisplayedBottomComponent()==null);
	}


	/**
	 * Updates the find/replace dialog translucencies, if necessary.  This
	 * method is really hackish, and we should really find a better way to do
	 * this.
	 *
	 * @param listener The child window listener.
	 */
	public void refreshDialogTranslucencies(ChildWindowListener listener) {
		// A window must be showing for its bounds to be queried.
		if (findDialog!=null && findDialog.isShowing()) {
			listener.refreshTranslucency(findDialog);
		}
		if (replaceDialog!=null && replaceDialog.isShowing()) {
			listener.refreshTranslucency(replaceDialog);
		}
	}


	/**
	 * Executes a "replace" operation in the active editor.
	 *
	 * @see #findNext()
	 */
	public void replaceNext() {

		AbstractMainView mainView = rtext.getMainView();

		// If it's nothing (ie, they haven't searched yet), bring up the
		// Replace dialog.
		SearchContext context = mainView.searchContext;
		String searchString = context.getSearchFor();
		if (searchString==null || searchString.length()==0) {
			switch (searchingMode) {
				case DIALOGS:
					ensureSearchDialogsCreated();
					replaceDialog.setVisible(true);
					break;
				case TOOLBARS:
					ensureToolbarsCreated();
					rtext.getCollapsibleSectionPanel().
							showBottomComponent(replaceToolBar);
					break;
			}
			return;
		}

		// Otherwise, repeat the last Replace action.
		RTextEditorPane textArea = mainView.getCurrentTextArea();

		try {

			SearchResult result = SearchEngine.replace(textArea, context);
			if (!result.wasFound()) {
				searchString = RTextUtilities.escapeForHTML(searchString, null);
				String temp = rtext.getString("CannotFindString", searchString);
				// "null" parent returns focus to previously focused window,
				// whether it be RText, the Find dialog or the Replace dialog.
				JOptionPane.showMessageDialog(null, temp,
							rtext.getString("InfoDialogHeader"),
							JOptionPane.INFORMATION_MESSAGE);
			}

			// If they used the "replace next" tool bar button, make sure the
			// editor gets focused.
			if (isNoSearchUIVisible()) {
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


	/**
	 * Toggles whether to use dialogs or tool bars for searching.
	 *
	 * @param mode The new search mode.
	 * @see #getSearchingMode()
	 */
	public void setSearchingMode(SearchingMode mode) {

		if (mode==null) {
			throw new NullPointerException("mode cannot be null");
		}

		CollapsibleSectionPanel csp = rtext.getCollapsibleSectionPanel();
		if (csp!=null) { // UI is fully realized
			switch (mode) {
				case DIALOGS:
					csp.hideBottomComponent();
					break;
				case TOOLBARS:
					hideFindDialogIfVisible();
					hideReplaceDialogIfVisible();
					break;
			}
		}

		searchingMode = mode;

	}


	/**
	 * Displays the "find" UI (either a dialog or a tool bar).
	 *
	 * @see #showReplaceUI()
	 */
	public void showFindUI() {

		if (searchingMode==SearchingMode.DIALOGS) {

			rtext.getCollapsibleSectionPanel().hideBottomComponent();
			ensureSearchDialogsCreated();

			if (replaceDialog.isVisible()) {
				replaceDialog.setVisible(false);
			}

			if (!findDialog.isVisible()) {
				// If the current document has selected text, use the selection
				// as the value to search for.
				RTextEditorPane editor = rtext.getMainView().getCurrentTextArea();
				String selectedText = editor.getSelectedText();
				if (selectedText!=null) {
					findDialog.setSearchString(selectedText);
				}
				findDialog.setVisible(true);
			}
			else {
				findDialog.requestFocus();
			}

		}

		else { // SearchingMode.TOOLBARS
			hideFindDialogIfVisible();
			hideReplaceDialogIfVisible();
			ensureToolbarsCreated();
			rtext.getCollapsibleSectionPanel().showBottomComponent(findToolBar);
		}

	}


	/**
	 * Displays the "replace" UI (either a dialog or a tool bar).
	 *
	 * @see #showFindUI()
	 */
	public void showReplaceUI() {

		if (searchingMode==SearchingMode.DIALOGS) {

			rtext.getCollapsibleSectionPanel().hideBottomComponent();
			ensureSearchDialogsCreated();

			if (findDialog.isVisible()) {
				findDialog.setVisible(false);
			}

			if (!replaceDialog.isVisible()) {
				// If the current document has selected text, use the selection
				// as the value to search for.
				RTextEditorPane editor = rtext.getMainView().getCurrentTextArea();
				String selectedText = editor.getSelectedText();
				if (selectedText!=null)
					replaceDialog.setSearchString(selectedText);
				replaceDialog.setVisible(true);
			}
			else {
				replaceDialog.requestFocus();
			}

		}

		else { // SearchingMode.TOOLBARS
			hideFindDialogIfVisible();
			hideReplaceDialogIfVisible();
			ensureToolbarsCreated();
			rtext.getCollapsibleSectionPanel().
					showBottomComponent(replaceToolBar);
		}

	}


	/**
	 * This method should be called whenever the application Look and Feel
	 * changes.  Updates any realized search-related components.
	 */
	public void updateUI() {

		// The find and replace dialogs are created together.
		if (findDialog != null) {
			SwingUtilities.updateComponentTreeUI(findDialog);
			// Unique to findDialog, NOT all JDialogs.
			findDialog.updateUI();
			findDialog.pack();
			SwingUtilities.updateComponentTreeUI(replaceDialog);
			// Also unique to replaceDialog, NOT all JDialogs.
			replaceDialog.updateUI();
			replaceDialog.pack();
			// tool bars are handled by the CollapsibleSectionPanel
		}

	}


	/**
	 * Specifies what searching UI the user wants to use.
	 */
	public static enum SearchingMode {
		DIALOGS,
		TOOLBARS;
	}


}