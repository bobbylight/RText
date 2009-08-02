/*
 * 11/14/2003
 *
 * OpenAction.java - Action to open an old text file in RText.
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
import java.io.File;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.fife.ui.app.StandardAction;
import org.fife.ui.rtextfilechooser.RTextFileChooser;


/**
 * Action used by an <code>AbstractMainView</code> to open a document
 * from a file on disk.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class OpenAction extends StandardAction {


	/**
	 * Creates a new <code>OpenAction</code>.
	 *
	 * @param owner the main window of this rtext instance.
	 * @param text The text associated with the action.
	 * @param icon The icon associated with the action.
	 * @param desc The description of the action.
	 * @param mnemonic The mnemonic for the action.
	 * @param accelerator The accelerator key for the action.
	 */
	 public OpenAction(RText owner, String text, Icon icon, String desc,
	 				int mnemonic, KeyStroke accelerator) {
		super(owner, text, icon, desc, mnemonic, accelerator);
	}


	public void actionPerformed(ActionEvent e) {

		RText owner = (RText)getApplication();
		RTextFileChooser chooser = owner.getFileChooser();

		// Without this, the user can press "Ctrl+O" twice really fast
		// and get two file choosers up.  The first one behaves normally
		// but the second one has no components painted on it and is not
		// responsive, effectively hanging the program.
		if (!chooser.isShowing()) {

			// Initialize the file chooser to be an Open dialog.
			chooser.setMultiSelectionEnabled(true);
			chooser.setOpenedFiles(owner.getMainView().getOpenFiles());
			chooser.setEncoding(RTextFileChooser.getDefaultEncoding());

			int returnVal = chooser.showOpenDialog(owner);

			// If they selected a file and clicked "OK", open the flie!
			if (returnVal == RTextFileChooser.APPROVE_OPTION) {

				AbstractMainView mainView = owner.getMainView();
				String encoding = chooser.getEncoding();

				// Add each file, one at a time.
				File [] selectedFiles = chooser.getSelectedFiles();
				for (int i=0; i<selectedFiles.length; i++) {
					String fileFullPath = selectedFiles[i].getAbsolutePath();
					mainView.openFile(fileFullPath, encoding);
				}

			} // End of if (returnVal == RFileChooser.APPROVE_OPTION).

		} // End of if (!chooser.isShowing()).

	}


}