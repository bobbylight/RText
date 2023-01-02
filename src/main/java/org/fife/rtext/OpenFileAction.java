/*
 * 11/14/2003
 *
 * OpenFileFromHistoryAction.java - Action to open a file from the "history"
 * in the File menu in RText.
 * Copyright (C) 2003 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.event.ActionEvent;

import org.fife.ui.UIUtil;
import org.fife.ui.app.AppAction;


/**
 * Action used by an <code>RText</code> to open a specific file, such as one
 * selected from the "file history" in the <code>RText</code>'s File menu.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class OpenFileAction extends AppAction<RText> {

	private final String fileFullPath;


	/**
	 * Constructor.
	 *
	 * @param owner the main window of this rtext instance.
	 * @param fileFullPath The full path of the file to open.
	 */
	OpenFileAction(RText owner, String fileFullPath) {
		super(owner);
		this.fileFullPath = fileFullPath;
		setName(UIUtil.getDisplayPathForFile(owner, fileFullPath));
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		RText owner = getApplication();
		AbstractMainView mainView = owner.getMainView();
		RTextEditorPane textArea = mainView.getCurrentTextArea();

		// If the only document open is untitled and empty, remove
		// (and thus replace) it.
		if (mainView.getNumDocuments()==1 &&
			textArea.getFileName().equals(owner.getNewFileName()) &&
			textArea.getDocument().getLength()==0)
				mainView.closeCurrentDocument();

		// Attempt to open the file.
		// "null" encoding means check for Unicode before using default.
		mainView.openFile(fileFullPath, null);

	}

}
