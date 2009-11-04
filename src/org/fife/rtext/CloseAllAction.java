/*
 * 11/14/2003
 *
 * CloseAllAction.java - Action to close all open documents in RText.
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


/**
 * Action used by an <code>RTextTabbedPane</code> to close all open documents.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class CloseAllAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public CloseAllAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "CloseAllAction");
		setIcon(icon);
	}


	public void actionPerformed(ActionEvent e) {
		((RText)getApplication()).getMainView().closeAllDocuments();
	}


}