/*
 * 08/28/2012
 *
 * FolderProjectEntry.java - A folder in a project.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.io.File;


/**
 * A project entry representing a folder on the local file system
 * and its contents, possibly filtered.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FolderProjectEntry implements ProjectEntry {

	private Project parent;
	private File dir;


	public FolderProjectEntry(Project parent, File folder) {
		this.parent = parent;
		this.dir = folder;
	}


	public int compareTo(Object o) {
		if (o instanceof FolderProjectEntry) {
			return dir.compareTo(((FolderProjectEntry)o).getFile());
		}
		return -1;
	}


	public boolean equals(Object o) {
		if (o==this) {
			return true;
		}
		return compareTo(o)==0;
	}


	public File getFile() {
		return dir;
	}


	public Project getProject() {
		return parent;
	}


	public String getType() {
		return DIR_PROJECT_ENTRY;
	}


	public int hashCode() {
		return dir.hashCode();
	}


	public void removeFromProject() {
		parent.removeEntry(this);
	}


}