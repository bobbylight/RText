/*
 * 05/24/2005
 *
 * OpenRemoteAction.java - Action to open a remote file via FTP.
 * Copyright (C) 2005 Robert Futrell
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
 * Action used by an <code>AbstractMainView</code> to open a document
 * from a file on disk.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class OpenRemoteAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public OpenRemoteAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "OpenRemoteAction");
		setIcon(icon);
	}


	public void actionPerformed(ActionEvent e) {
		RText owner = (RText)getApplication();
		RemoteFileChooser rfc = owner.getRemoteFileChooser();
		rfc.setMode(RemoteFileChooser.OPEN_MODE);
		rfc.setVisible(true);
	}


}