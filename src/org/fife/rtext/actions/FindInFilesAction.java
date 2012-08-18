/*
 * 11/14/2003
 *
 * FindInFilesAction.java - Action for finding text in a group of files.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
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
			mainView.findInFilesDialog.setSearchContext(mainView.searchContext);
			RTextUtilities.configureFindInFilesDialog(mainView.findInFilesDialog);
			mainView.findInFilesDialog.addPropertyChangeListener(mainView);
			mainView.findInFilesDialog.addFindInFilesListener(mainView);
		}

		mainView.findInFilesDialog.setVisible(true);

	}


}