/*
 * 08/28/2012
 *
 * ProjectEntry.java - A file or directory in a project.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.model;

import java.io.File;



/**
 * A project entry is either a file or a folder on the file system.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface ProjectEntry extends ModelEntity, Comparable<ProjectEntry> {

	/**
	 * A project entry that's a single file somewhere on the file system.
	 */
	public static final String FILE_PROJECT_ENTRY = "file";

	/**
	 * A project entry that's a directory and all of its contents.
	 */
	public static final String DIR_PROJECT_ENTRY  = "folder";

	/**
	 * A project entry that's a logical folder (not a physical folder on the
	 * local file system).
	 */
	public static final String LOGICAL_DIR_PROJECT_ENTRY = "logical-folder";


	/**
	 * Returns the file system resource this project entry represents.
	 *
	 * @return The file system resource.
	 */
	File getFile();


	/**
	 * Returns the parent of this entry.
	 *
	 * @return The parent model object.
	 * @see #removeFromParent()
	 */
	ProjectEntryParent getParent();


	/**
	 * Returns the data to save about this entry in workspace XML.
	 *
	 * @return The save data.
	 */
	String getSaveData();


	/**
	 * Returns the type of project entry this is.
	 *
	 * @return The type of project entry.
	 */
	String getType();


	/**
	 * Removes this entry from its parent.
	 *
	 * @see #getParent()
	 */
	void removeFromParent();


}