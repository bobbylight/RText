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
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.fife.rtext.plugins.project.FileProjectEntry;
import org.fife.rtext.plugins.project.FolderProjectEntry;
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


	private FileTreeNode createFileTreeNode(File file) {
		return new FileTreeNode(plugin, file);
	}


	private FileProjectEntryTreeNode createProjectEntryNode(ProjectEntry entry) {
		FileProjectEntryTreeNode node = null;
		if (entry instanceof FileProjectEntry) {
			node = new FileProjectEntryTreeNode(plugin, entry);
		}
		else {
			node = new FolderProjectEntryTreeNode(plugin, (FolderProjectEntry)entry);
		}
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
	 * Does any filtering and sorting of an array of files so that they will
	 * be displayed properly.
	 *
	 * @param files The array of files to filter and sort.
	 * @return The filtered and sorted array of files.
	 */
	// TODO: Have FolderProjectEntrys pass in filters.
	private File[] filterAndSort(File[] files) {

		int num = files.length;
		ArrayList dirList = new ArrayList();
		ArrayList fileList = new ArrayList();

		// First, separate the directories from regular files so we can
		// sort them individually.  This part could be made more compact,
		// but it isn't just for a tad more speed.
		for (int i=0; i<num; i++) {
			if (files[i].isDirectory())
				dirList.add(files[i]);
			else
				fileList.add(files[i]);
		}

		// On Windows and OS X, comparison is case-insensitive.
		Comparator c = null;
		String os = System.getProperty("os.name");
		boolean isOSX = os!=null ? os.toLowerCase().indexOf("os x")>-1 : false;
		if (File.separatorChar=='\\' || isOSX) {
			c = new Comparator() {
				public int compare(Object o1, Object o2) {
					File f1 = (File)o1;
					File f2 = (File)o2;
					return f1.getName().compareToIgnoreCase(f2.getName());
				}
			};
		}

		Collections.sort(fileList, c);
		Collections.sort(dirList, c);
		dirList.addAll(fileList);

		File[] fileArray = new File[dirList.size()];
		return (File[])dirList.toArray(fileArray);

	}


	/**
	 * Called when a node is about to be expanded.  This method is overridden
	 * so that the node that is being expanded will be populated with its
	 * subdirectories, if necessary.
	 */
	public void fireTreeWillExpand(TreePath e) throws ExpandVetoException {

		super.fireTreeWillExpand(e);

		AbstractWorkspaceTreeNode awtn =
					(AbstractWorkspaceTreeNode)e.getLastPathComponent();

		// If the only child is the dummy one, we know we haven't populated
		// this node with true children yet.
		if (awtn instanceof FolderProjectEntryTreeNode) {
			FolderProjectEntryTreeNode fpetn = (FolderProjectEntryTreeNode)awtn;
			if (fpetn.isNotPopulated()) {
				refreshChildren(fpetn);
			}
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


	/**
	 * Refreshes the children of the specified node (representing a directory)
	 * to accurately reflect the files inside of it.
	 *
	 * @param node The node whose children should be refreshed.
	 */
	private void refreshChildren(AbstractWorkspaceTreeNode node) {

		if (node instanceof FolderProjectEntryTreeNode) {
			node.removeAllChildren();
			FolderProjectEntryTreeNode fpetn = (FolderProjectEntryTreeNode)node;
			File file = fpetn.getFile(); // Should always be true
			if (file.isDirectory()) {
				FileSystemView fsv = FileSystemView.getFileSystemView();
				File[] children = fsv.getFiles(file, false);
				File[] filteredChildren = filterAndSort(children);
				for (int i=0; i<filteredChildren.length; i++) {
					node.add(createFileTreeNode(filteredChildren[i]));
				}
			}
			model.reload(node);
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