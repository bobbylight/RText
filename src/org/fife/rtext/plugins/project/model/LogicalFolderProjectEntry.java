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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * A project entry representing a logical folder (i.e., not an actual
 * folder on the file system).
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class LogicalFolderProjectEntry extends AbstractProjectEntry
		implements ProjectEntryParent {

	private String name;
	private List entries;


	public LogicalFolderProjectEntry(ProjectEntryParent parent, String name) {
		super(parent);
		this.name = name;
		entries = new ArrayList();
	}


	public void accept(WorkspaceVisitor visitor) {
		visitor.visit(this);
		for (Iterator i=getEntryIterator(); i.hasNext(); ) {
			((ProjectEntry)i.next()).accept(visitor);
		}
		visitor.postVisit(this);
	}


	public void addEntry(ProjectEntry entry) {
		entries.add(entry);
	}


	public int compareTo(Object o) {
		if (o instanceof LogicalFolderProjectEntry) {
			return name.compareTo(((LogicalFolderProjectEntry)o).getName());
		}
		return -1;
	}


	public Iterator getEntryIterator() {
		return entries.iterator();
	}


	public File getFile() {
		return null;
	}


	public String getName() {
		return name;
	}


	public String getSaveData() {
		return getName();
	}


	public String getType() {
		return LOGICAL_DIR_PROJECT_ENTRY;
	}


	public int hashCode() {
		return name.hashCode();
	}


	public void removeEntry(ProjectEntry entry) {
		entries.remove(entry);
	}


	public void setName(String name) {
		this.name = name;
	}


}