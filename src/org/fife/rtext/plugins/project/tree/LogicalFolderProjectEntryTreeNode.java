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
import org.fife.rtext.plugins.project.LogicalFolderNameDialog;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.PopupContent;
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


	@Override
	public String getDisplayName() {
		return ((LogicalFolderProjectEntry)entry).getName();
	}
	

	@Override
	public Icon getIcon() {
		return getLogicalFolderIcon();
	}


	/**
	 * Returns the icon shared amongst all logical folders.
	 *
	 * @return The shared icon.
	 */
	public static Icon getLogicalFolderIcon() {
		return icon;
	}


	@Override
	public List<PopupContent> getPopupActions() {
		List<PopupContent> actions = new ArrayList<PopupContent>();
		LogicalFolderProjectEntry entry = (LogicalFolderProjectEntry)this.entry;
		actions.add(new AddFileAction(entry, this));
		actions.add(new AddFolderAction(entry, this));
		actions.add(new AddLogicalFolderAction(entry, this));
		actions.add(null);
		actions.add(new MoveUpAction());
		actions.add(new MoveDownAction());
		actions.add(null);
		actions.add(new RemoveAction());
		actions.add(new DeleteAction(false));
		actions.add(null);
		actions.add(new RenameAction());
		actions.add(null);
		actions.add(new PropertiesAction(false));
		return actions;
	}


	@Override
	public String getToolTipText() {
		return Messages.getString("ProjectPlugin.ToolTip.LogicalFolderProjectEntry",
				entry.getSaveData());
	}


	@Override
	protected void handleDelete() {
		// TODO Auto-generated method stub

	}


	@Override
	protected void handleProperties() {
		// TODO Auto-generated method stub

	}


	@Override
	protected void handleRename() {
		RText parent = plugin.getRText();
		LogicalFolderNameDialog dialog = new LogicalFolderNameDialog(parent,
				entry.getSaveData(), new LogicalFolderNameChecker());
		dialog.setVisible(true);
		String name = dialog.getLogicalFolderName();
		if (name!=null) {
			LogicalFolderProjectEntry lfpe = (LogicalFolderProjectEntry)entry;
			lfpe.setName(name);
			plugin.refreshTree(this);
		}
	}


	/**
	 * Get a slightly modified version of the standard "folder" icon for this
	 * OS.
	 */
	static {
		File testFile =  new File(System.getProperty("java.io.tmpdir"));
		Icon temp = FileSystemView.getFileSystemView().getSystemIcon(testFile);
		icon = new DecoratableIcon(16, temp);
		URL decorationRes = RText.class.
				getResource("/org/fife/rsta/ui/search/lightbulb.png");
		Icon decoration = new ImageIcon(decorationRes);
		icon.addDecorationIcon(decoration);
	}


}