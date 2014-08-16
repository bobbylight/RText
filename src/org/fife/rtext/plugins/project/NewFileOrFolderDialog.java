/*
 * 09/08/2012
 *
 * LogicalFolderNameDialog.java - A dialog that prompts the user for the
 * name of a logical folder.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.tree.NameChecker;


/**
 * A dialog that prompts the user for the name of a new file or folder.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class NewFileOrFolderDialog extends AbstractEnterFileNameDialog {

	private boolean isFile;


	public NewFileOrFolderDialog(RText parent, boolean isFile,
			NameChecker nameChecker) {
		super(parent, nameChecker);
		String key = "NewFileOrFolderDialog.Title." +
					(isFile ? "File" : "Folder");
		setTitle(Messages.getString(key));
		this.isFile = isFile;
	}


	@Override
	protected void addDescPanel() {
		String header = "NewFileOrFolderDialog.Header." +
				(isFile ? "File" : "Folder");
		String descText = Messages.getString(header);
		String imgName = isFile ? "page_white_add.png" : "folder_add.png";
		URL url = getClass().getResource("tree/" + imgName);
		Icon icon = new ImageIcon(url);
		setDescription(icon, descText);
	}


}