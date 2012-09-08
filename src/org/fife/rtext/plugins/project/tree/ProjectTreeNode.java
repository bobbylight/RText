/*
 * 08/28/2012
 *
 * ProjectTreeNode.java - A tree node for projects.
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
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.model.FileProjectEntry;
import org.fife.rtext.plugins.project.model.FolderProjectEntry;
import org.fife.rtext.plugins.project.model.LogicalFolderProjectEntry;
import org.fife.rtext.plugins.project.model.Project;
import org.fife.rtext.plugins.project.model.ProjectEntry;
import org.fife.ui.rtextfilechooser.RDirectoryChooser;
import org.fife.ui.rtextfilechooser.RTextFileChooser;


/**
 * Tree node for projects.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ProjectTreeNode extends AbstractWorkspaceTreeNode {

	private Project project;
	private static Icon icon;
	private static RTextFileChooser chooser;


	public ProjectTreeNode(ProjectPlugin plugin, Project project) {
		super(plugin);
		this.project = project;
	}


	/**
	 * Returns the file chooser to use when adding files to a project.
	 *
	 * @return The file chooser.
	 */
	private static RTextFileChooser getFileChooser() {
		if (chooser==null) {
			chooser = new RTextFileChooser();
			chooser.setShowHiddenFiles(true);
		}
		return chooser;
	}


	/**
	 * {@inheritDoc}
	 */
	public Icon getIcon() {
		return icon;
	}


	public List getPopupActions() {
		List actions = new ArrayList();
		actions.add(new NewFileAction());
		actions.add(new NewFolderAction());
		actions.add(new NewLogicalFolderAction());
		actions.add(null);
		actions.add(new RenameAction());
		actions.add(new DeleteAction());
		actions.add(null);
		actions.add(new PropertiesAction());
		return actions;
	}


	/**
	 * Returns the project being represented.
	 *
	 * @return The project represented.
	 */
	public Project getProject() {
		return project;
	}


	protected void handleDelete() {

		String text = Messages.getString("Action.Delete.Confirm",
				"ProjectPlugin.Project", getProject().getName());
		RText rtext = plugin.getRText();
		String title = rtext.getString("ConfDialogTitle");

		int rc = JOptionPane.showConfirmDialog(rtext, text, title,
				JOptionPane.YES_NO_OPTION);
		if (rc==JOptionPane.YES_OPTION) {
			project.removeFromWorkspace();
			removeFromParent();
			plugin.refreshTree(getParent());
		}

	}


	protected void handleProperties() {
		JOptionPane.showMessageDialog(null, "Properties of the item!");
	}


	protected void handleRefresh() {
		// Do nothing
	}


	protected void handleRename() {
		RText rtext = plugin.getRText();
		String type = Messages.getString("ProjectPlugin.Project");
		RenameDialog dialog = new RenameDialog(rtext, type, new ProjectNameChecker());
		dialog.setName(project.getName());
		dialog.setVisible(true);
		String newName = dialog.getName();
		if (newName!=null) {
			project.setName(newName);
			plugin.refreshTree(getParent());
		}
	}


	public String toString() {
		return project.getName();
	}


	/**
	 * Action for a menu item that adds a file to this project.
	 */
	private class NewFileAction extends BaseAction {

		public NewFileAction() {
			super("Action.NewFile", "page_white_add.png");
		}

		public void actionPerformed(ActionEvent e) {
			RTextFileChooser chooser = getFileChooser();
			int rc = chooser.showOpenDialog(plugin.getRText());
			if (rc==RTextFileChooser.APPROVE_OPTION) {
				File toAdd = chooser.getSelectedFile();
				ProjectEntry entry = new FileProjectEntry(project, toAdd);
				project.addEntry(entry);
				add(new FileProjectEntryTreeNode(plugin, entry));
				plugin.refreshTree(ProjectTreeNode.this);
			}
		}

	}


	/**
	 * Action for a menu item that adds a folder to this project.
	 */
	private class NewFolderAction extends BaseAction {

		public NewFolderAction() {
			super("Action.NewFolder", "folder_add.png");
		}

		public void actionPerformed(ActionEvent e) {
			RText rtext = plugin.getRText();
			RDirectoryChooser chooser = new RDirectoryChooser(rtext);
			chooser.setVisible(true);
			String dir = chooser.getChosenDirectory();
			if (dir!=null) {
				File dirFile = new File(dir);
				ProjectEntry entry = new FolderProjectEntry(project, dirFile);
				project.addEntry(entry);
				add(new FileProjectEntryTreeNode(plugin, entry));
				plugin.refreshTree(ProjectTreeNode.this);
			}
		}

	}


	/**
	 * Action for a menu item that adds a logical folder to this project.
	 */
	private class NewLogicalFolderAction extends BaseAction {

		public NewLogicalFolderAction() {
			super("Action.NewLogicalFolder", "folder_add.png");
		}

		public void actionPerformed(ActionEvent e) {
			String input = JOptionPane.showInputDialog("Enter logical folder name!");
			if (input!=null) {
				LogicalFolderProjectEntry entry =
						new LogicalFolderProjectEntry(project, input);
				project.addEntry(entry);
				add(new LogicalFolderProjectEntryTreeNode(plugin, entry));
				plugin.refreshTree(ProjectTreeNode.this);
			}
		}

	}


	/**
	 * Ensures that proposed project names are valid.
	 */
	private static class ProjectNameChecker implements NameChecker {

		public String isValid(String text) {
			int length = text.length();
			if (length==0) {
				return "empty";
			}
			for (int i=0; i<length; i++) {
				char ch = text.charAt(i);
				if (ch=='<' || ch=='>' || ch=='&') {
					return "invalidProjectNameChars";
				}
			}
			return null;
		}

	}


	static {
		icon = new ImageIcon(ProjectTreeNode.class.
				getResource("application.png"));
	}


}