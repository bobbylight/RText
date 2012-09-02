/*
 * 08/28/2012
 *
 * WorkspaceTree.java - A tree representation of a workspace.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.fife.rtext.plugins.project.Project;
import org.fife.rtext.plugins.project.ProjectEntry;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.Workspace;


/**
 * A tree view of a workspace and its projects.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class WorkspaceTree extends JTree {

	private ProjectPlugin plugin;
	private DefaultTreeModel model;
	private DefaultMutableTreeNode root;
	private JPopupMenu popup;


	public WorkspaceTree(ProjectPlugin plugin, Workspace workspace) {

		this.plugin = plugin;
		root = new WorkspaceRootTreeNode(plugin, workspace);
		model = new DefaultTreeModel(root);
		setModel(model);
		setWorkspace(workspace);
		setCellRenderer(new WorkspaceTreeRenderer());

		// Add a needed extra bit of space at the top.
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(3, 3, 0, 3),
				getBorder()));

	}


	private void configurePopupMenu() {

		if (popup==null) {
			popup = new JPopupMenu();
		}
		else {
			popup.removeAll();
		}

		Object node = getSelectionPath().getLastPathComponent();
		if (node!=null) {
			AbstractWorkspaceTreeNode treeNode =
					(AbstractWorkspaceTreeNode)node;
			List actions = treeNode.getPopupActions();
			for (Iterator i=actions.iterator(); i.hasNext(); ) {
				Action action = (Action)i.next();
				if (action==null) {
					popup.addSeparator();
				}
				else {
					popup.add(new JMenuItem(action));
				}
			}
		}

	}


	private ProjectEntryTreeNode createProjectEntryNode(ProjectEntry entry) {
		ProjectEntryTreeNode node = new ProjectEntryTreeNode(plugin, entry);
		return node;
	}


	private ProjectTreeNode createProjectNode(Project project) {
		ProjectTreeNode node = new ProjectTreeNode(plugin, project);
		for (Iterator i=project.getEntryIterator(); i.hasNext(); ) {
			ProjectEntry entry = (ProjectEntry)i.next();
			node.add(createProjectEntryNode(entry));
		}
		return node;
	}


	/**
	 * Displays the popup menu at the specified location.
	 *
	 * @param p The location at which to display the popup.
	 */
	private void displayPopupMenu(Point p) {

		// Select the tree node at the mouse position.
		TreePath path = getPathForLocation(p.x, p.y);
		if (path!=null) {
			setSelectionPath(path);
			scrollPathToVisible(path);
		}
		else {
			clearSelection();
			return;
		}

		// Configure and display it!
		configurePopupMenu();
		if (popup.getComponentCount()!=0) {
			popup.show(this, p.x, p.y);
		}

	}


	/**
	 * Overridden to display our popup menu if necessary.
	 *
	 * @param e The mouse event.
	 */
	protected void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
		if (e.isPopupTrigger()) {
			displayPopupMenu(e.getPoint());
		}
	}


	public void setWorkspace(Workspace workspace) {

		root = new WorkspaceRootTreeNode(plugin, workspace);

		for (Iterator i=workspace.getProjectIterator(); i.hasNext(); ) {
			Project project = (Project)i.next();
			root.add(createProjectNode(project));
		}

		model.setRoot(root);

	}


}