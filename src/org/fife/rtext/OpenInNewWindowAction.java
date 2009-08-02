/*
 * 11/14/2003
 *
 * OpenInNewWindowAction.java - Action to open a document in a new RText
 * window.
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
import javax.swing.SwingUtilities;

import org.fife.ui.app.StandardAction;
import org.fife.ui.rtextfilechooser.RTextFileChooser;


/**
 * Action used by an <code>AbstractMainView</code> to open a document
 * in a new window.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class OpenInNewWindowAction extends StandardAction {

	private RText newWindow; // Only run on EDT so it's okay to cache this.


	/**
	 * Creates a new <code>OpenInNewWindowAction</code>.
	 *
	 * @param rtext The RText window you'd like the new RText window to be
	 *        modeled after (usually the one that owns this action).
	 * @param text The text associated with the action.
	 * @param icon The icon associated with the action.
	 * @param desc The description of the action.
	 * @param mnemonic The mnemonic for the action.
	 * @param accelerator The accelerator key for the action.
	 */
	public OpenInNewWindowAction(RText rtext, String text, Icon icon,
				String desc, int mnemonic, KeyStroke accelerator) {
		super(rtext, text, icon, desc, mnemonic, accelerator);
	}


	/**
	 * Callback for when this action is performed.
	 *
	 * @param e The event that occurred.
	 */
	public synchronized void actionPerformed(ActionEvent e) {

		// Create a new RText window.
		newWindow = new RText(null, (RTextPreferences)RTextPreferences.
					generatePreferences((RText)getApplication()));
		StoreKeeper.addRTextInstance(newWindow);

		// Open the new RText's file chooser.  Do this in an invokeLater()
		// call as RText's constructor leaves some stuff to do via
		// invokeLater() as well, and we must wait for this stuff to complete
		// before we can continue (e.g. RText's "working directory" must be
		// set).
		SwingUtilities.invokeLater(new OpenFileChooserRunnable());

	}


	/**
	 * Opens the new RText's file chooser for the user to select a file(s)
	 * to open.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class OpenFileChooserRunnable implements Runnable {

		public void run() {

			RTextFileChooser chooser = newWindow.getFileChooser();
			chooser.setMultiSelectionEnabled(true);
			//chooser.setUnderlinedFiles(null);	// Unnecessary since no files are yet open!
			int returnVal = chooser.showOpenDialog(newWindow);

			// If they selected a file and clicked "OK", open the flie!
			if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {

				AbstractMainView newMainView = newWindow.getMainView();
				RTextEditorPane newCurrentTextArea = newMainView.currentTextArea;
				String encoding = chooser.getEncoding();

				// First, remove old listeners.
				if (newCurrentTextArea != null) {
					newCurrentTextArea.removeCaretListener(newWindow);
					newCurrentTextArea.removeKeyListener(newWindow);
				}

				// If the only document open is untitled and empty, remove
				// (and thus replace) replace it.
				RText owner = (RText)getApplication();
				if (newMainView.getNumDocuments()==1 &&
					newCurrentTextArea.getFileName().equals(owner.getNewFileName()) &&
					newCurrentTextArea.getDocument().getLength()==0)
				{
						newMainView.closeCurrentDocument();
				}

				// Add each file, one at a time, to our tabbed pane.
				File [] selectedFiles = chooser.getSelectedFiles();
				for (int i=0; i<selectedFiles.length; i++) {
					String file = selectedFiles[i].getAbsolutePath();
					newMainView.openFile(file, encoding);
				}

			} // End of if (returnVal == JFileChooser.APPROVE_OPTION).

		}

	}


}