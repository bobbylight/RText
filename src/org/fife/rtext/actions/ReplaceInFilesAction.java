/*
 * 09/07/2006
 *
 * ReokaceInFilesAction.java - Action for replacing text in a group of files.
 * Copyright (C) 2006 Robert Futrell
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

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.app.StandardAction;
import org.fife.ui.search.ReplaceInFilesDialog;


/**
 * Action used by an <code>AbstractMainView</code> to find text in files.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ReplaceInFilesAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public ReplaceInFilesAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "ReplaceInFilesAction");
		setIcon(icon);
	}


	/**
	 * Called when the user initiates this action.
	 *
	 * @param e The event.
	 */
	public void actionPerformed(ActionEvent e) {

		RText rtext = (RText)getApplication();
		AbstractMainView mainView = rtext.getMainView();

		// Create the "Replace in Files" dialog if it hasn't already been.
		if (mainView.replaceInFilesDialog==null) {
			mainView.replaceInFilesDialog = new ReplaceInFilesDialog(rtext);
			RTextUtilities.configureFindInFilesDialog(mainView.replaceInFilesDialog);
			mainView.replaceInFilesDialog.addPropertyChangeListener(mainView);
			mainView.replaceInFilesDialog.addFindInFilesListener(mainView);
		}

		// So we don't have to type so much.
		ReplaceInFilesDialog dialog = mainView.replaceInFilesDialog;

		// Set the search parameters correctly and display the dialog.
		dialog.setSearchParameters(mainView.searchStrings,
							mainView.searchMatchCase,
							mainView.searchWholeWord,
							mainView.searchRegExpression);
		dialog.setVisible(true);

	}


}