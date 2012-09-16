/*
 * 08/28/2012
 *
 * WorkspaceRootTreeNode.java - Tree node for the workspace root.
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
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.BaseAction;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.RenameDialog;
import org.fife.rtext.plugins.project.model.Project;
import org.fife.rtext.plugins.project.model.Workspace;


/**
 * Tree node for the root of a workspace.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class WorkspaceRootTreeNode extends AbstractWorkspaceTreeNode {

	private Workspace workspace;
	private static final Icon icon;


	public WorkspaceRootTreeNode(ProjectPlugin plugin, Workspace workspace) {
		super(plugin);
		this.workspace = workspace;
	}


	public String getDisplayName() {
		return workspace.getName();
	}


	/**
	 * {@inheritDoc}
	 */
	public Icon getIcon() {
		return getWorkspaceIcon();
	}


	public List getPopupActions() {
		List actions = new ArrayList();
		actions.add(new NewProjectAction());
		actions.add(null);
		actions.add(new RenameAction());
		actions.add(null);
		actions.add(new PropertiesAction());
		return actions;
	}


	public String getToolTipText() {
		return null;
	}


	/**
	 * Returns the icon shared amongst all workspaces.
	 *
	 * @return The shared icon instance.
	 */
	public static final Icon getWorkspaceIcon() {
		return icon;
	}


	protected void handleDelete() {
		JOptionPane.showMessageDialog(null, "Not yet supported (or used)!");
	}


	protected void handleProperties() {
		JOptionPane.showMessageDialog(null, "Properties of the item!");
	}


	protected void handleRename() {
		RText rtext = plugin.getRText();
		String type = Messages.getString("ProjectPlugin.Workspace");
		RenameDialog dialog = new RenameDialog(rtext, type, new WorkspaceNameChecker());
		dialog.setName(workspace.getName());
		dialog.setVisible(true);
		String newName = dialog.getName();
		if (newName!=null) {
			if (workspace.setName(newName)) {
				plugin.refreshTree(this);
			}
			else {
				String msg = Messages.getString("ProjectPlugin.ErrorRenamingWorkspace");
				String title = rtext.getString("ErrorDialogTitle");
				JOptionPane.showMessageDialog(rtext, msg, title,
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}


	static {
		icon = new ImageIcon(WorkspaceRootTreeNode.class.
								getResource("application_double.png"));
	}


	/**
	 * Creates a new project in this workspace.
	 */
	private class NewProjectAction extends BaseAction {

		public NewProjectAction() {
			super("Action.NewProject");
		}

		public void actionPerformed(ActionEvent e) {
			RText rtext = plugin.getRText();
			RenameDialog dialog = new RenameDialog(rtext, "Project",
					new ProjectTreeNode.ProjectNameChecker(workspace));
			Icon icon = ProjectTreeNode.getProjectIcon();
			dialog.setDescription(icon, Messages.getString("NewProjectDialog.Desc"));
			dialog.setTitle(Messages.getString("NewProjectDialog.Title"));
			dialog.setName(null); // Move focus from desc SelectableLabel to field.
			dialog.setVisible(true);
			String newName = dialog.getName();
			if (newName!=null) {
				Project project = new Project(workspace, newName);
				workspace.addProject(project);
				ProjectTreeNode childNode =
						new ProjectTreeNode(plugin, project);
				plugin.insertTreeNodeInto(childNode, WorkspaceRootTreeNode.this);
			}
		}

	}


	/**
	 * Ensures that proposed project names are valid.
	 */
	public static class WorkspaceNameChecker implements NameChecker {

		public String isValid(String text) {
			int length = text.length();
			if (length==0) {
				return "empty";
			}
			for (int i=0; i<length; i++) {
				char ch = text.charAt(i);
				if (!(Character.isLetterOrDigit(ch) || ch=='_' || ch=='-' ||
						ch==' ' || ch=='.')) {
					return "invalidWorkspaceName";
				}
			}
			if (text.endsWith(".")) {
				return "workspaceCannotEndWithDot";
			}
			return null;
		}

	}


}