/*
 * 11/17/2008
 *
 * SaveRemoteAction.java - Saves a remote file, e.g. via FTP.
 * Copyright (C) 2008 Robert Futrell
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


/**
 * Action used by an <code>AbstractMainView</code> to save a file on a
 * remote machine with a new name.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SaveAsRemoteAction extends StandardAction {


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