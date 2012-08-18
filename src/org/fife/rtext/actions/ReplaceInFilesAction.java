/*
 * 09/07/2006
 *
 * ReokaceInFilesAction.java - Action for replacing text in a group of files.
 * Copyright (C) 2006 Robert Futrell
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
			mainView.replaceInFilesDialog.setSearchContext(mainView.searchContext);
			RTextUtilities.configureFindInFilesDialog(mainView.replaceInFilesDialog);
			mainView.replaceInFilesDialog.addPropertyChangeListener(mainView);
			mainView.replaceInFilesDialog.addFindInFilesListener(mainView);
		}

		mainView.replaceInFilesDialog.setVisible(true);

	}


}