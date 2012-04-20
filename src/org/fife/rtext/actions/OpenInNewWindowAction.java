/*
 * 11/14/2003
 *
 * OpenInNewWindowAction.java - Action to open a document in a new RText
 * window.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.rtext.RTextPreferences;
import org.fife.rtext.StoreKeeper;
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
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public OpenInNewWindowAction(RText owner, ResourceBundle msg, Icon icon) {
		//super(owner, text, icon, desc, mnemonic, accelerator);
		super(owner, msg, "OpenInNewWindowAction");
		setIcon(icon);
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

			// If they selected a file and clicked "OK", open the file!
			if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {

				AbstractMainView newMainView = newWindow.getMainView();
				RTextEditorPane newCurrentTextArea = newMainView.getCurrentTextArea();
				String encoding = chooser.getEncoding();

				// First, remove old listeners.
				if (newCurrentTextArea != null) {
					newCurrentTextArea.removeCaretListener(newWindow);
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