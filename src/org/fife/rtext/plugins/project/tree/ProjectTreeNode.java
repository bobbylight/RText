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
import org.fife.rtext.plugins.project.FileProjectEntry;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.Project;
import org.fife.rtext.plugins.project.ProjectEntry;
import org.fife.rtext.plugins.project.ProjectPlugin;
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
		actions.add(null);
		actions.add(new RenameAction());
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


	protected void handleProperties() {
		JOptionPane.showMessageDialog(null, "Properties of the item!");
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
			super("Action.NewFile");
		}

		public void actionPerformed(ActionEvent e) {
			RTextFileChooser chooser = getFileChooser();
			int rc = chooser.showOpenDialog(plugin.getRText());
			if (rc==RTextFileChooser.APPROVE_OPTION) {
				File toAdd = chooser.getSelectedFile();
				ProjectEntry entry = new FileProjectEntry(toAdd);
				project.addEntry(entry);
				add(new ProjectEntryTreeNode(plugin, entry));
				plugin.refreshTree(ProjectTreeNode.this);
			}
		}

	}


	/**
	 * Ensures that proposed project names are valid.
	 */
	private static class ProjectNameChecker implements NameChecker {

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


	static {
		icon = new ImageIcon(ProjectTreeNode.class.
				getResource("application.png"));
	}


}