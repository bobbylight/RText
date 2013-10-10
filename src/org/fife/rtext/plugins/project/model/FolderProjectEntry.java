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

import org.fife.ui.rtextfilechooser.FileDisplayNames;


/**
 * A project entry representing a folder on the local file system
 * and its contents, possibly filtered.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FolderProjectEntry extends AbstractProjectEntry {

	private File dir;
	private String displayName;
	private FolderFilterInfo filterInfo;


	public FolderProjectEntry(ProjectEntryParent parent, File folder) {
		super(parent);
		this.dir = folder;
		this.displayName = FileDisplayNames.get().getName(this.dir);
		setFilterInfo(new FolderFilterInfo());
	}


	public void accept(WorkspaceVisitor visitor) {
		visitor.visit(this);
		visitor.postVisit(this);
	}


	public int compareTo(ProjectEntry o) {
		if (o instanceof FolderProjectEntry) {
			return dir.compareTo(o.getFile());
		}
		return -1;
	}


	@Override
	public final boolean equals(Object o) {
		if (o==this) {
			return true;
		}
		return o instanceof FolderProjectEntry &&
				compareTo((FolderProjectEntry)o)==0;
	}


	public String getDisplayName() {
		return displayName;
	}


	public File getFile() {
		return dir;
	}


	public FolderFilterInfo getFilterInfo() {
		return filterInfo;
	}


	public String getSaveData() {
		return getFile().getAbsolutePath();
	}


	public String getType() {
		return DIR_PROJECT_ENTRY;
	}


	@Override
	public int hashCode() {
		return dir.hashCode();
	}


	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	public void setFilterInfo(FolderFilterInfo filterInfo) {
		this.filterInfo = filterInfo;
	}


}