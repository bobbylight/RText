/*
 * 09/08/2012
 *
 * LogicalFolderProjectTreeNode.java - A tree node for logical folders.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import org.fife.rsta.ac.java.DecoratableIcon;
import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.model.LogicalFolderProjectEntry;


/**
 * A tree node that's a "logical" folder; that is, one that's not reflecting
 * a physical folder structure on the local file system.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class LogicalFolderProjectEntryTreeNode extends ProjectEntryTreeNode {

	private static DecoratableIcon icon;


	public LogicalFolderProjectEntryTreeNode(ProjectPlugin plugin,
			LogicalFolderProjectEntry entry) {
		super(plugin, entry);
	}


	public Icon getIcon() {
		return icon;
	}


	public List getPopupActions() {
		List actions = new ArrayList();
		actions.add(new RenameAction());
		actions.add(null);
		actions.add(new RemoveAction());
		actions.add(new DeleteAction());
		actions.add(null);
		actions.add(new PropertiesAction());
		return actions;
	}


	protected void handleDelete() {
		// TODO Auto-generated method stub

	}


	protected void handleProperties() {
		// TODO Auto-generated method stub

	}


	protected void handleRefresh() {
		// Do nothing
	}


	protected void handleRename() {
		RText rtext = plugin.getRText();
		String key = "ProjectPlugin.LogicalFolder";
		String type = Messages.getString(key);
		RenameDialog dialog = new RenameDialog(rtext, type,
				new FolderProjectEntryTreeNode.FolderProjectEntryNameChecker());
		dialog.setName(((LogicalFolderProjectEntry)entry).getName());
		dialog.setVisible(true);
	}


	public String toString() {
		return ((LogicalFolderProjectEntry)entry).getName();
	}
	

	/**
	 * Get a slightly modified version of the standard "folder" icon for this
	 * OS.
	 */
	static {
		File testFile =  new File(System.getProperty("java.io.tmpdir"));
		Icon temp = FileSystemView.getFileSystemView().getSystemIcon(testFile);
		icon = new DecoratableIcon(temp);
		URL decorationRes = LogicalFolderProjectEntryTreeNode.class.
				getResource("/org/fife/rtext/graphics/modified_overlay.png");
		Icon decoration = new ImageIcon(decorationRes);
		icon.addDecorationIcon(decoration);
	}


}