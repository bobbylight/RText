/*
 * 12/08/2004
 *
 * FilePropertiesAction.java - Action to display the information on the
 * current text area.
 * Copyright (C) 2004 Robert Futrell
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

import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;
import org.fife.ui.rsyntaxtextarea.TextFilePropertiesDialog;


/**
 * Action to display information on the current text area.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FilePropertiesAction extends StandardAction {


	/**
	 * Creates a new <code>FilePropertiesAction</code>.
	 *
	 * @param rtext The parent application.
	 * @param msg The resource bundle to use for localization.
	 */
	public FilePropertiesAction(RText rtext, ResourceBundle msg) {
		super(rtext, msg, "FilePropertiesAction");
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action event.
	 */
	public void actionPerformed(ActionEvent e) {
		RText rtext = (RText)getApplication();
		TextFilePropertiesDialog dialog = new TextFilePropertiesDialog(
							rtext, rtext.getMainView().getCurrentTextArea());
		dialog.setVisible(true);
	}


}