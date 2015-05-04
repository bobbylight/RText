/*
 * 11/14/2003
 *
 * OpenFileFromHistoryAction.java - Action to open a file from the "history"
 * in the File menu in RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.event.ActionEvent;

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
	 * @see #setFileFullPath(String)
	 */
	 public OpenFileFromHistoryAction(RText owner) {
		super(owner);
	}


	public void actionPerformed(ActionEvent e) {

		RText owner = (RText)getApplication();
		AbstractMainView mainView = owner.getMainView();
		RTextEditorPane textArea = mainView.getCurrentTextArea();

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


	/**
	 * Sets the file to open.
	 *
	 * @param fileFullPath The full path of the file to open.
	 */
	public void setFileFullPath(String fileFullPath) {
		this.fileFullPath = fileFullPath;
	}


}