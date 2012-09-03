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

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.Project;
import org.fife.rtext.plugins.project.ProjectEntry;
import org.fife.rtext.plugins.project.ProjectPlugin;


/**
 * The tree node used for file project entries.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FileProjectEntryTreeNode extends AbstractWorkspaceTreeNode {

	private ProjectEntry entry;
	private Icon icon;


	public FileProjectEntryTreeNode(ProjectPlugin plugin, ProjectEntry entry) {
		super(plugin);
		this.entry = entry;
		icon = FileSystemView.getFileSystemView().getSystemIcon(entry.getFile());
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
		actions.add(new RemoveAction());
		actions.add(null);
		actions.add(new RenameAction());
		actions.add(null);
		actions.add(new PropertiesAction());
		return actions;
	}


	protected void handleProperties() {
		JOptionPane.showMessageDialog(null, "Properties of the item!");
	}


	protected void handleRename() {
		RText rtext = plugin.getRText();
		boolean directory = entry.getFile().isDirectory();
		String key = "ProjectPlugin." + (directory ? "Directory" : "File");
		String type = Messages.getString(key);
		RenameDialog dialog = new RenameDialog(rtext, type, new FileProjectEntryNameChecker());
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

		public boolean isValid(String text) {
			int length = text.length();
			if (length==0) {
				return false;
			}
			for (int i=0; i<length; i++) {
				char ch = text.charAt(i);
				if (!(Character.isLetterOrDigit(ch) || ch=='_' || ch==' ')) {
					return false;
				}
			}
			return !text.endsWith(".");
		}

	}


	/**
	 * Removes this entry from its parent.
	 */
	private class RemoveAction extends BaseAction {

		public RemoveAction() {
			super("Action.RemoveProjectEntry");
		}

		public void actionPerformed(ActionEvent e) {

			RText rtext = plugin.getRText();
			String title = rtext.getString("ConfDialogTitle");
			String selectedEntry = entry.getFile().getName();
			String text = Messages.getString(
					"Action.RemoveProjectEntry.Confirm", selectedEntry);

			int rc = JOptionPane.showConfirmDialog(rtext, text, title,
					JOptionPane.YES_NO_OPTION);
			if (rc==JOptionPane.YES_OPTION) {
				ProjectTreeNode parent = (ProjectTreeNode)getParent();
				parent.remove(FileProjectEntryTreeNode.this);
				Project project = parent.getProject();
				project.removeEntry(entry);
				plugin.refreshTree(parent);
			}

		}

	}


}