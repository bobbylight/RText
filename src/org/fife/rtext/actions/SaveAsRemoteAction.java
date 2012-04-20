/*
 * 11/17/2008
 *
 * SaveRemoteAction.java - Saves a remote file, e.g. via FTP.
 * Copyright (C) 2008 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import org.fife.rtext.RText;
import org.fife.rtext.RemoteFileChooser;
import org.fife.ui.app.StandardAction;


/**
 * Action used by an <code>AbstractMainView</code> to save a file on a
 * remote machine with a new name.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SaveAsRemoteAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public SaveAsRemoteAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "SaveAsRemoteAction");
		setIcon(icon);
	}


	/**
	 * Opens the remote file chooser, allowing the user to save a file.
	 *
	 * @param e The event.
	 */
	public void actionPerformed(ActionEvent e) {
		RText owner = (RText)getApplication();
		RemoteFileChooser rfc = owner.getRemoteFileChooser();
		rfc.setMode(RemoteFileChooser.SAVE_MODE);
		rfc.setVisible(true);
	}


}