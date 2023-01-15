/*
 * 11/14/2003
 *
 * SaveAllAction.java - Action to save all open documents in RText.
 * Copyright (C) 2003 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import org.fife.rtext.RText;
import org.fife.ui.app.AppAction;


/**
 * Action used by an <code>RTextTabbedPane</code> to save all currently
 * open documents.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SaveAllAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	SaveAllAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "SaveAllAction");
		setIcon(icon);
	}


	/**
	 * Called when this action is performed.
	 *
	 * @param e The event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		getApplication().getMainView().saveAllFiles();
	}


}
