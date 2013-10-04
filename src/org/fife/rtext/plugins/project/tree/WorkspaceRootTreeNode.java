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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.BaseAction;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.PopupContent;
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


	@Override
	public String getDisplayName() {
		return workspace.getName();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() {
		return getWorkspaceIcon();
	}


	@Override
	public List<PopupContent> getPopupActions() {
		List<PopupContent> actions = new ArrayList<PopupContent>();
		actions.add(new NewProjectAction());
		actions.add(null);
		actions.add(new RenameAction(true));
		actions.add(null);
		actions.add(new PropertiesAction(true, true));
		return actions;
	}


	@Override
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


	@Override
	protected void handleDelete() {
		JOptionPane.showMessageDialog(null, "Not yet supported (or used)!");
	}


	@Override
	protected void handleProperties() {
		File file = new File(workspace.getFileFullPath());
		FileTreeNode.handleProperties(plugin.getRText(), file);
	}


	@Override
	protected void handleRename() {
		RText rtext = plugin.getRText();
		String type = Messages.getString("ProjectPlugin.Workspace");
		RenameDialog dialog = new RenameDialog(rtext, type, new WorkspaceNameChecker());
		dialog.setName(workspace.getName());
		dialog.setVisible(true);
		String newName = dialog.getName();
		if (newName!=null) {
			if (workspace.setName(newName)) {
				plugin.getTree().nodeChanged(this);
				plugin.refreshWorkspaceName();
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
				// Ensure Workspace root node is expanded when first plugin is
				// added.
				plugin.getTree().expandPath(new TreePath(getPath()));
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