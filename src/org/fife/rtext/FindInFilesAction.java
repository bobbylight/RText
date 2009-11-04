/*
 * 11/14/2003
 *
 * FindInFilesAction.java - Action for finding text in a group of files.
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
import java.util.ResourceBundle;
import javax.swing.Icon;

import org.fife.ui.app.StandardAction;
import org.fife.ui.search.FindInFilesDialog;


/**
 * Action used by an <code>AbstractMainView</code> to find text in files.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FindInFilesAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public FindInFilesAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "FindInFilesAction");
		setIcon(icon);
	}


	public void actionPerformed(ActionEvent e) {

		RText rtext = (RText)getApplication();
		AbstractMainView mainView = rtext.getMainView();

		// Create the "Find in Files" dialog if it hasn't already been.
		if (mainView.findInFilesDialog==null) {
			mainView.findInFilesDialog = new FindInFilesDialog(rtext);
			RTextUtilities.configureFindInFilesDialog(mainView.findInFilesDialog);
			mainView.findInFilesDialog.addPropertyChangeListener(mainView);
			mainView.findInFilesDialog.addFindInFilesListener(mainView);
		}

		// So we don't have to type so much.
		FindInFilesDialog findInFilesDialog = mainView.findInFilesDialog;

		// Set the search parameters correctly and display the dialog.
		findInFilesDialog.setSearchParameters(mainView.searchStrings,
										mainView.searchMatchCase,
										mainView.searchWholeWord,
										mainView.searchRegExpression);
		findInFilesDialog.setVisible(true);

	}


}