/*
 * 08/28/2012
 *
 * ProjectEntryTreeNode.java - Tree node for project entries.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.awt.event.ActionEvent;
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
 * The tree node used for project entries.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ProjectEntryTreeNode extends AbstractWorkspaceTreeNode {

	private ProjectEntry entry;
	private Icon icon;


	public ProjectEntryTreeNode(ProjectPlugin plugin, ProjectEntry entry) {
		super(plugin);
		this.entry = entry;
		icon = FileSystemView.getFileSystemView().getSystemIcon(entry.getFile());
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


	public Icon getIcon() {
		return icon;
	}


	protected void handleProperties() {
		JOptionPane.showMessageDialog(null, "Properties of the item!");
	}


	protected void handleRename() {
		RText rtext = plugin.getRText();
		boolean directory = entry.getFile().isDirectory();
		String key = "ProjectPlugin." + (directory ? "Directory" : "File");
		String type = Messages.getString(key);
		RenameDialog dialog = new RenameDialog(rtext, type, new ProjectEntryNameChecker());
		dialog.setName(entry.getFile().getName());
		dialog.setVisible(true);
	}


	public String toString() {
		return entry.getFile().getName();
	}
	

	/**
	 * Ensures that proposed project entry names are valid.
	 */
	private static class ProjectEntryNameChecker implements NameChecker {

		public boolean isValid(String text) {
			int length = text.length();
			if (length==0) {
				return false;
			}
			for (int i=0; i<length; i++) {
				char ch = text.charAt(i);
				if (!(Character.isLetterOrDigit(ch) || ch=='_' || ch=='-' ||
						ch==' ')) {
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
				parent.remove(ProjectEntryTreeNode.this);
				Project project = parent.getProject();
				project.removeEntry(entry);
				plugin.refreshTree(parent);
			}

		}

	}


}