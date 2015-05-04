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

	void postVisit(Workspace workspace);

	void postVisit(Project project);

	void postVisit(FileProjectEntry entry);

	void postVisit(FolderProjectEntry entry);

	void postVisit(LogicalFolderProjectEntry entry);

	void visit(Workspace workspace);

	void visit(Project project);

	void visit(FileProjectEntry entry);

	void visit(FolderProjectEntry entry);

	void visit(LogicalFolderProjectEntry entry);

}