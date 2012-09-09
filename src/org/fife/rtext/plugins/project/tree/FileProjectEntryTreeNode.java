/*
 * 08/28/2012
 *
 * FileProjectEntryTreeNode.java - Tree node for file project entries.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.RenameDialog;
import org.fife.rtext.plugins.project.model.ProjectEntry;
import org.fife.ui.rtextfilechooser.extras.FileIOExtras;


/**
 * The tree node used for file project entries.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FileProjectEntryTreeNode extends ProjectEntryTreeNode {

	private Icon icon;


	public FileProjectEntryTreeNode(ProjectPlugin plugin, ProjectEntry entry) {
		super(plugin, entry);
		icon = FileSystemView.getFileSystemView().getSystemIcon(entry.getFile());
	}


	protected NameChecker createNameChecker() {
		return new FileProjectEntryNameChecker();
	}


	/**
	 * Returns the file or directory represented by this tree node's project
	 * entry.
	 *
	 * @return The file or directory.
	 */
	public File getFile() {
		return entry.getFile();
	}


	public Icon getIcon() {
		return icon;
	}


	public List getPopupActions() {
		List actions = new ArrayList();
		if (!getFile().isDirectory()) {
			actions.add(new OpenAction());
		}
		possiblyAddOpenInActions(actions);
		actions.add(new RenameAction());
		actions.add(null);
		actions.add(new RemoveAction());
		actions.add(new DeleteAction());
		actions.add(null);
		if (getFile().isDirectory()) {
			actions.add(new RefreshAction());
			actions.add(null);
		}
		actions.add(new PropertiesAction());
		return actions;
	}


	protected void handleDelete() {

		final boolean hard = false;
		File[] files = new File[1];
		files[0] = entry.getFile();

		FileIOExtras extras = FileIOExtras.getInstance();
		if (!hard && extras!=null) {
			if (FileTreeNode.handleDeleteNative(files, plugin)) {
				entry.removeFromParent();
				removeFromParent();
				plugin.refreshTree(getParent());
			}
			return;
		}

		String text = Messages.getString("Action.Delete.Confirm",
				"ProjectPlugin.File", getFile().getName());
		RText rtext = plugin.getRText();
		String title = rtext.getString("ConfDialogTitle");

		int rc = JOptionPane.showConfirmDialog(rtext, text, title,
				JOptionPane.YES_NO_OPTION);
		if (rc==JOptionPane.YES_OPTION) {
			if (!entry.getFile().delete()) {
				text = Messages.getString("ProjectPlugin.Error.DeletingFile",
						getFile().getName());
				title = rtext.getString("ErrorDialogTitle");
				JOptionPane.showMessageDialog(rtext, text, title,
						JOptionPane.ERROR_MESSAGE);
			}
			entry.removeFromParent();
			removeFromParent();
			plugin.refreshTree(getParent());
		}

	}


	protected void handleProperties() {
		JOptionPane.showMessageDialog(null, "Properties of the item!");
	}


	protected void handleRename() {
		RText rtext = plugin.getRText();
		boolean directory = entry.getFile().isDirectory();
		String key = "ProjectPlugin." + (directory ? "Folder" : "File");
		String type = Messages.getString(key);
		RenameDialog dialog = new RenameDialog(rtext, type, createNameChecker());
		dialog.setName(entry.getFile().getName());
		dialog.setVisible(true);
	}


	public String toString() {
		return entry.getFile().getName();
	}
	

	/**
	 * Ensures that proposed file project entry names are valid.
	 */
	private static class FileProjectEntryNameChecker implements NameChecker {

		public String isValid(String text) {
			int length = text.length();
			if (length==0) {
				return "empty";
			}
			for (int i=0; i<length; i++) {
				char ch = text.charAt(i);
				if (!(Character.isLetterOrDigit(ch) || ch=='_' || ch=='-' ||
						ch==' ' || ch=='.')) {
					return "invalidFileName";
				}
			}
			if (text.endsWith(".")) {
				return "fileNameCannotEndWithDot";
			}
			return null;
		}

	}


}