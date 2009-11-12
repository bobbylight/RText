/*
 * 11/14/2003
 *
 * FindAction.java - Action for searching for text in RText.
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
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.JButton;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.ui.app.StandardAction;
import org.fife.ui.search.FindDialog;
import org.fife.ui.search.ReplaceDialog;


/**
 * Action used by an <code>AbstractMainView</code> to search for text.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FindAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public FindAction(RText owner, ResourceBundle msg, Icon icon) {
		this(owner, msg, icon, "FindAction");
	}


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 * @param nameKey The key for localizing the name of this action.
	 */
	protected FindAction(RText owner, ResourceBundle msg, Icon icon,
							String nameKey) {
		super(owner, msg, nameKey);
		setIcon(icon);
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action performed.
	 */
	public void actionPerformed(ActionEvent e) {

		ensureSearchDialogsCreated();
		RText rtext = (RText)getApplication();

		// If the QuickSearch bar is visible, shift focus to that instead
		// of displaying the Find dialog if they hit Ctrl+F or chose the
		// menu item (i.e., the toolbar "Find" button still brings up the
		// Find dialog).
		if (!(e.getSource() instanceof JButton) &&
				rtext.isSearchToolBarVisible()) {
			rtext.getSearchToolBar().focusFindField();
			return;
		}

		AbstractMainView mainView = rtext.getMainView();

		// Lazily create find and replace dialogs if necessary.
		if (mainView.findDialog==null) {
			mainView.findDialog = new FindDialog(rtext, mainView);
			mainView.replaceDialog = new ReplaceDialog(rtext, mainView);
			mainView.findDialog.addActionListener(mainView);
			mainView.replaceDialog.addActionListener(mainView);
			mainView.findDialog.addPropertyChangeListener(mainView);
			mainView.replaceDialog.addPropertyChangeListener(mainView);
		}

		FindDialog findDialog = mainView.findDialog;
		boolean findDialogVisible = findDialog.isVisible();

		// Display the Find dialog if necessary.
		if (!findDialogVisible && !mainView.replaceDialog.isVisible()) {

			findDialog.setSearchParameters(mainView.searchStrings,
									mainView.searchMatchCase,
									mainView.searchWholeWord,
									mainView.searchRegExpression,
									!mainView.searchingForward,
									mainView.searchMarkAll);

			// If the current document has selected text, use the selection
			// as the value to search for.
			RTextEditorPane editor = mainView.getCurrentTextArea();
			String selectedText = editor.getSelectedText();
			if (selectedText!=null) {
				findDialog.setSearchString(selectedText);
			}

			findDialog.setVisible(true);

		}

		// If the find dialog is visible but not enabled, have it request
		// focus.
		else if (findDialogVisible) {
			findDialog.toFront();
		}

	}


	/**
	 * Ensures the find and replace dialogs are created.
	 */
	protected void ensureSearchDialogsCreated() {
		RText rtext = (RText)getApplication();
		AbstractMainView mainView = rtext.getMainView();
		if (mainView.replaceDialog==null) {
			mainView.findDialog = new FindDialog(rtext, mainView);
			mainView.replaceDialog = new ReplaceDialog(rtext, mainView);
			mainView.findDialog.addActionListener(mainView);
			mainView.replaceDialog.addActionListener(mainView);
			mainView.findDialog.addPropertyChangeListener(mainView);
			mainView.replaceDialog.addPropertyChangeListener(mainView);
			rtext.registerChildWindowListeners(mainView.findDialog);
			rtext.registerChildWindowListeners(mainView.replaceDialog);
		}
	}


}