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
package org.fife.rtext;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.KeyStroke;

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
	 * @param rtext The <code>RText</code> that is displaying help.
	 * @param text The text associated with the action.
	 * @param icon The icon associated with the action.
	 * @param desc The description of the action.
	 * @param mnemonic The mnemonic for the action.
	 * @param accelerator The accelerator key for the action.
	 */
	public FilePropertiesAction(RText rtext, String text, Icon icon,
					String desc, int mnemonic, KeyStroke accelerator) {
		super(rtext, text, icon, desc, mnemonic, accelerator);
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action event.
	 */
	public void actionPerformed(ActionEvent e) {
		RText rtext = (RText)getApplication();
		TextFilePropertiesDialog dialog = new TextFilePropertiesDialog(
							rtext, rtext.getMainView().currentTextArea);
		dialog.setVisible(true);
		dialog = null;
	}


}