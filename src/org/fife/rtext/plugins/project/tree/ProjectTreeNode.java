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

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.RenameDialog;
import org.fife.rtext.plugins.project.model.Project;


/**
 * Tree node for projects.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ProjectTreeNode extends AbstractWorkspaceTreeNode {

	private Project project;
	private static Icon icon;


	public ProjectTreeNode(ProjectPlugin plugin, Project project) {
		super(plugin);
		this.project = project;
	}


	/**
	 * {@inheritDoc}
	 */
	public Icon getIcon() {
		return icon;
	}


	public List getPopupActions() {
		List actions = new ArrayList();
		actions.add(new AddFileAction(project, this));
		actions.add(new AddFolderAction(project, this));
		actions.add(new AddLogicalFolderAction(project, this));
		actions.add(null);
		actions.add(new DeleteAction());
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


	public String getToolTipText() {
		return null;
	}

	protected void handleDelete() {

		String text = Messages.getString("Action.DeleteProject.Confirm",
				getProject().getName());
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