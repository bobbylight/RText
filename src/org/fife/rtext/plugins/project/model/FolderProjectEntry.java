/*
 * 08/28/2012
 *
 * FolderProjectEntry.java - A folder in a project.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.model;

import java.io.File;


/**
 * A project entry representing a folder on the local file system
 * and its contents, possibly filtered.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FolderProjectEntry extends AbstractProjectEntry {

	private File dir;


	public FolderProjectEntry(ProjectEntryParent parent, File folder) {
		super(parent);
		this.dir = folder;
	}


	public void accept(WorkspaceVisitor visitor) {
		visitor.visit(this);
		visitor.postVisit(this);
	}


	public int compareTo(Object o) {
		if (o instanceof FolderProjectEntry) {
			return dir.compareTo(((FolderProjectEntry)o).getFile());
		}
		return -1;
	}


	public File getFile() {
		return dir;
	}


	public String getSaveData() {
		return getFile().getAbsolutePath();
	}


	public String getType() {
		return DIR_PROJECT_ENTRY;
	}


	public int hashCode() {
		return dir.hashCode();
	}


}