/*
 * 09/03/2012
 *
 * FileTreeNode.java - A tree node for regular files and directories.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.io.File;
import java.util.List;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import org.fife.rtext.plugins.project.ProjectPlugin;


/**
 * A tree node for regular files and directories.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FileTreeNode extends AbstractWorkspaceTreeNode {

	private File file;
	private Icon icon;


	public FileTreeNode(ProjectPlugin plugin, File file) {
		super(plugin);
		this.file = file;
		icon = FileSystemView.getFileSystemView().getSystemIcon(file);
	}


	public List getPopupActions() {
		// TODO Auto-generated method stub
		return null;
	}


	public Icon getIcon() {
		return icon;
	}


	protected void handleProperties() {
		// TODO Auto-generated method stub
	}


	protected void handleRename() {
		// TODO Auto-generated method stub
	}


	public String toString() {
		return file.getName();
	}
	

}