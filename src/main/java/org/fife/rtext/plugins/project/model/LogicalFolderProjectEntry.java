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
	private List<ProjectEntry> entries;


	public LogicalFolderProjectEntry(ProjectEntryParent parent, String name) {
		super(parent);
		this.name = name;
		entries = new ArrayList<ProjectEntry>();
	}


	public void accept(WorkspaceVisitor visitor) {
		visitor.visit(this);
		for (ProjectEntry entry : entries) {
			entry.accept(visitor);
		}
		visitor.postVisit(this);
	}


	public void addEntry(ProjectEntry entry) {
		entries.add(entry);
	}


	public int compareTo(ProjectEntry o) {
		if (o instanceof LogicalFolderProjectEntry) {
			return name.compareTo(((LogicalFolderProjectEntry)o).getName());
		}
		return -1;
	}


	@Override
	public final boolean equals(Object o) {
		if (o==this) {
			return true;
		}
		return o instanceof LogicalFolderProjectEntry &&
				compareTo((LogicalFolderProjectEntry)o)==0;
	}


	/**
	 * Returns the index of the specified project entry.
	 *
	 * @param entry The entry to look for.
	 * @return The index of the entry, or <code>-1</code> if it is not
	 *         contained in this logical folder.
	 */
	private int getEntryIndex(ProjectEntry entry) {
		for (int i=0; i<entries.size(); i++) {
			if (entry==entries.get(i)) {
				return i;
			}
		}
		return -1;
	}


	public Iterator<ProjectEntry> getEntryIterator() {
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


	@Override
	public int hashCode() {
		return name.hashCode();
	}


	public boolean moveProjectEntryDown(ProjectEntry entry) {
		int index = getEntryIndex(entry);
		if (index>-1 && index<entries.size()-1) {
			entries.remove(index);
			entries.add(index+1, entry);
			return true;
		}
		return false;
	}


	public boolean moveProjectEntryUp(ProjectEntry entry) {
		int index = getEntryIndex(entry);
		if (index>0) {
			entries.remove(index);
			entries.add(index-1, entry);
			return true;
		}
		return false;
	}


	public void removeEntry(ProjectEntry entry) {
		entries.remove(entry);
	}


	public void setName(String name) {
		this.name = name;
	}


}