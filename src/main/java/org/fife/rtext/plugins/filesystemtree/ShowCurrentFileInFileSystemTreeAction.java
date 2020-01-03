/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.filesystemtree;

import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.ui.app.AppAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;

/**
 * Selects the current file in the file system tree.
 */
class ShowCurrentFileInFileSystemTreeAction extends AppAction<RText> {

	private final FileSystemTreePlugin plugin;

	ShowCurrentFileInFileSystemTreeAction(RText app,
				 	FileSystemTreePlugin plugin, ResourceBundle msg) {
		super(app, msg, "Action.SelectCurrentFile");
		this.plugin = plugin;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		RText rtext = getApplication();

		RTextEditorPane textArea = rtext.getMainView().getCurrentTextArea();
		File file = new File(textArea.getFileFullPath());

		if (file.isFile()) {
			plugin.selectInFileChooser(file);
		}
		else {
			UIManager.getLookAndFeel().provideErrorFeedback(rtext);
		}
	}
}
