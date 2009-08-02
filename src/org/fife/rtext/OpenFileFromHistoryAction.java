/*
 * 11/14/2003
 *
 * OpenFileFromHistoryAction.java - Action to open a file from the "history"
 * in the File menu in RText.
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
import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.fife.ui.app.StandardAction;


/**
 * Action used by an <code>RText</code> to open a file selected from the
 * "file history" in the <code>RText</code>'s File menu.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class OpenFileFromHistoryAction extends StandardAction {

	private String fileFullPath;


	/**
	 * Creates a new <code>OpenFileFromHistoryAction</code>.
	 *
	 * @param owner the main window of this rtext instance.
	 * @param text The text associated with the action.
	 * @param icon The icon associated with the action.
	 * @param desc The description of the action.
	 * @param mnemonic The mnemonic for the action.
	 * @param accelerator The accelerator key for the action.
	 * @param fileFullPath The name and path of the file to be opened.
	 */
	 public OpenFileFromHistoryAction(RText owner, String text, Icon icon,
	 		String desc, int mnemonic, KeyStroke accelerator,
	 		String fileFullPath) {
		super(owner, text, icon, desc, mnemonic, accelerator);
		this.fileFullPath = fileFullPath;
	}


	public void actionPerformed(ActionEvent e) {

		RText owner = (RText)getApplication();
		AbstractMainView mainView = owner.getMainView();
		RTextEditorPane textArea = mainView.currentTextArea;

		// If the only document open is untitled and empty, remove
		// (and thus replace) it.
		if (mainView.getNumDocuments()==1 &&
			textArea.getFileName().equals(owner.getNewFileName()) &&
			textArea.getDocument().getLength()==0)
				mainView.closeCurrentDocument();

		// Attempt to add the old text file.
		// "null" encoding means check for Unicode before using default.
		mainView.openFile(fileFullPath, null);

	}


}