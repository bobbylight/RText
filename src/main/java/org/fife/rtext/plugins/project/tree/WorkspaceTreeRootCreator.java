/*
 * 09/08/2012
 *
 * WorkspaceTreeRootCreator.java - Creates a root for a WorkspaceTree.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.util.Stack;

import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.model.FileProjectEntry;
import org.fife.rtext.plugins.project.model.FolderProjectEntry;
import org.fife.rtext.plugins.project.model.LogicalFolderProjectEntry;
import org.fife.rtext.plugins.project.model.Project;
import org.fife.rtext.plugins.project.model.Workspace;
import org.fife.rtext.plugins.project.model.WorkspaceVisitor;


/**
 * Creates a root for a <code>WorkspaceTree</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class WorkspaceTreeRootCreator implements WorkspaceVisitor {

	private final ProjectPlugin plugin;
	private WorkspaceRootTreeNode root;
	private final Stack<AbstractWorkspaceTreeNode> entryParentNodeStack;


	public WorkspaceTreeRootCreator(ProjectPlugin plugin) {
		this.plugin = plugin;
		entryParentNodeStack = new Stack<>();
	}


	private AbstractWorkspaceTreeNode getCurrentProjectEntryParentNode() {
		return entryParentNodeStack.peek();
	}


	/**
	 * Returns the constructed tree root.
	 *
	 * @return The tree root.
	 */
	public WorkspaceRootTreeNode getRoot() {
		return root;
	}


	@Override
	public void postVisit(Workspace workspace) {}


	@Override
	public void postVisit(Project project) {
		entryParentNodeStack.pop();
	}


	@Override
	public void postVisit(FileProjectEntry entry) {}


	@Override
	public void postVisit(FolderProjectEntry entry) {}


	@Override
	public void postVisit(LogicalFolderProjectEntry entry) {
		entryParentNodeStack.pop();
	}


	@Override
	public void visit(Workspace workspace) {
		root = new WorkspaceRootTreeNode(plugin, workspace);
	}


	@Override
	public void visit(Project project) {
		ProjectTreeNode node = new ProjectTreeNode(plugin, project);
		root.add(node);
		entryParentNodeStack.push(node);
	}


	@Override
	public void visit(FileProjectEntry entry) {
		FileProjectEntryTreeNode node = new FileProjectEntryTreeNode(
															plugin, entry);
		getCurrentProjectEntryParentNode().add(node);
	}


	@Override
	public void visit(FolderProjectEntry entry) {
		FolderProjectEntryTreeNode node = new FolderProjectEntryTreeNode(
															plugin, entry);
		getCurrentProjectEntryParentNode().add(node);
	}


	@Override
	public void visit(LogicalFolderProjectEntry entry) {
		LogicalFolderProjectEntryTreeNode node =
				new LogicalFolderProjectEntryTreeNode(plugin, entry);
		getCurrentProjectEntryParentNode().add(node);
		entryParentNodeStack.push(node);
	}


}
