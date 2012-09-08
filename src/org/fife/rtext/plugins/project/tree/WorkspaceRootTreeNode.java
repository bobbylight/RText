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

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.model.Workspace;


/**
 * Tree node for the root of a workspace.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class WorkspaceRootTreeNode extends AbstractWorkspaceTreeNode {

	private Workspace workspace;
	private Icon icon;


	public WorkspaceRootTreeNode(ProjectPlugin plugin, Workspace workspace) {
		super(plugin);
		this.workspace = workspace;
		icon = new ImageIcon(getClass().getResource("application_double.png"));
	}


	/**
	 * {@inheritDoc}
	 */
	public Icon getIcon() {
		return icon;
	}


	public List getPopupActions() {
		List actions = new ArrayList();
		actions.add(new RenameAction());
		actions.add(null);
		actions.add(new PropertiesAction());
		return actions;
	}


	protected void handleDelete() {
		JOptionPane.showMessageDialog(null, "Not yet supported (or used)!");
	}


	protected void handleProperties() {
		JOptionPane.showMessageDialog(null, "Properties of the item!");
	}


	protected void handleRefresh() {
		// Do nothing
	}


	protected void handleRename() {
		RText rtext = plugin.getRText();
		String type = Messages.getString("ProjectPlugin.Workspace");
		RenameDialog dialog = new RenameDialog(rtext, type, new WorkspaceNameChecker());
		dialog.setName(workspace.getName());
		dialog.setVisible(true);
		String newName = dialog.getName();
		if (newName!=null) {
			workspace.setName(newName);
			plugin.refreshTree(this);
		}
	}


	public String toString() {
		return workspace.getName();
	}


	/**
	 * Ensures that proposed project names are valid.
	 */
	private static class WorkspaceNameChecker implements NameChecker {

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