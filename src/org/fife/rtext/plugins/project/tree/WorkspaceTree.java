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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.plugins.project.FileProjectEntry;
import org.fife.rtext.plugins.project.FolderProjectEntry;
import org.fife.rtext.plugins.project.Project;
import org.fife.rtext.plugins.project.ProjectEntry;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.Workspace;
import org.fife.ui.rtextfilechooser.FileSelector;


/**
 * A tree view of a workspace and its projects.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class WorkspaceTree extends JTree implements FileSelector {

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
		if (awtn instanceof PhysicalLocationTreeNode) {
			PhysicalLocationTreeNode pltn = (PhysicalLocationTreeNode)awtn;
			if (pltn.isNotPopulated()) {
				Cursor orig = getCursor();
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					refreshChildren(pltn);
				} finally {
					setCursor(orig);
				}
			}
		}

	}


	/**
	 * Returns the file currently selected by the user.
	 *
	 * @return The file currently selected, or <code>null</code>
	 *         if no file is selected.
	 * @see #getSelectedFiles()
	 */
	public File getSelectedFile() {
		TreePath path = getSelectionPath();
		if (path!=null) {
			Object comp = path.getLastPathComponent();
			if (comp instanceof FileTreeNode) {
				FileTreeNode node = (FileTreeNode)comp;
				return node.getFile();
			}
			else if (comp instanceof FileProjectEntryTreeNode) {
				FileProjectEntryTreeNode node = (FileProjectEntryTreeNode)comp;
				File file = node.getFile();
				if (!file.isDirectory()) {
					return file;
				}
			}
		}
		return null;
	}


	/**
	 * Returns any selected files.
	 *
	 * @return The selected files, or a zero-length array if no files are
	 *         selected.
	 */
	public File[] getSelectedFiles() {
		File file = getSelectedFile();
		if (file!=null) {
			return new File[] { file };
		}
		return new File[0];
	}


	/**
	 * Opens the selected file in RText, if any.
	 */
	private void handleOpenFile() {
		File file = getSelectedFile();
		if (file!=null) {
			// We'll make sure the file exists and is a regular file
			// (as opposed to a directory) before attempting to open it.
			if (file.isFile()) {
				AbstractMainView mainView = plugin.getRText().getMainView();
				mainView.openFile(file.getAbsolutePath(), null);
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
		else if (e.getID()==MouseEvent.MOUSE_CLICKED && e.getClickCount()==2) {
			handleOpenFile();
		}
	}


	/**
	 * Refreshes the children of the specified node (representing a directory)
	 * to accurately reflect the files inside of it.
	 *
	 * @param node The node whose children should be refreshed.
	 */
	private void refreshChildren(PhysicalLocationTreeNode node) {

		if (node instanceof PhysicalLocationTreeNode) {
			node.refreshChildren();
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