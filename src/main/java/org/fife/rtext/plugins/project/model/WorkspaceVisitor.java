/*
 * 09/08/2012
 *
 * WorkspaceVisitor.java - Visitor for a workspace and its project entries.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.model;


/**
 * A visitor of a workspace and its project entries.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface WorkspaceVisitor {

	/**
	 * Called after visiting a workspace node.
	 *
	 * @param workspace The visited node.
	 */
	void postVisit(Workspace workspace);

	/**
	 * Called after visiting a project node.
	 *
	 * @param project The visited node.
	 */
	void postVisit(Project project);

	/**
	 * Called after visiting a file project entry node.
	 *
	 * @param entry The visited node.
	 */
	void postVisit(FileProjectEntry entry);

	/**
	 * Called after visiting a folder project entry node.
	 *
	 * @param entry The visited node.
	 */
	void postVisit(FolderProjectEntry entry);

	/**
	 * Called after visiting a logical folder project entry node.
	 *
	 * @param entry The visited node.
	 */
	void postVisit(LogicalFolderProjectEntry entry);

	/**
	 * Called before visiting a workspace node.
	 *
	 * @param workspace The visited node.
	 */
	void visit(Workspace workspace);

	/**
	 * Called before visiting a project node.
	 *
	 * @param project The visited node.
	 */
	void visit(Project project);

	/**
	 * Called before visiting a file project entry node.
	 *
	 * @param entry The visited node.
	 */
	void visit(FileProjectEntry entry);

	/**
	 * Called before visiting a folder projecgt entry node.
	 *
	 * @param entry The visited node.
	 */
	void visit(FolderProjectEntry entry);

	/**
	 * Called before visiting a logical folder project entry node.
	 *
	 * @param entry The visited node.
	 */
	void visit(LogicalFolderProjectEntry entry);

}
