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
		entries = new ArrayList<>();
	}


	@Override
	public void accept(WorkspaceVisitor visitor) {
		visitor.visit(this);
		for (ProjectEntry entry : entries) {
			entry.accept(visitor);
		}
		visitor.postVisit(this);
	}


	@Override
	public void addEntry(ProjectEntry entry) {
		entries.add(entry);
	}


	@Override
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


	@Override
	public Iterator<ProjectEntry> getEntryIterator() {
		return entries.iterator();
	}


	@Override
	public File getFile() {
		return null;
	}


	public String getName() {
		return name;
	}


	@Override
	public String getSaveData() {
		return getName();
	}


	@Override
	public String getType() {
		return LOGICAL_DIR_PROJECT_ENTRY;
	}


	@Override
	public int hashCode() {
		return name.hashCode();
	}


	@Override
	public boolean moveProjectEntryDown(ProjectEntry entry, boolean toBottom) {
		int index = getEntryIndex(entry);
		if (index>-1 && index<entries.size()-1) {
			entries.remove(index);
			int newIndex = toBottom ? entries.size() - 1 : index + 1;
			entries.add(newIndex, entry);
			return true;
		}
		return false;
	}


	@Override
	public boolean moveProjectEntryUp(ProjectEntry entry, boolean toTop) {
		int index = getEntryIndex(entry);
		if (index>0) {
			entries.remove(index);
			int newIndex = toTop ? 0 : index - 1;
			entries.add(newIndex, entry);
			return true;
		}
		return false;
	}


	@Override
	public void removeEntry(ProjectEntry entry) {
		entries.remove(entry);
	}


	public void setName(String name) {
		this.name = name;
	}


}
