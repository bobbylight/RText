/*
 * 09/08/2012
 *
 * ProjectEntryParent.java - A parent model entity of project entries.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.model;

import java.util.Iterator;


/**
 * A <code>ModelEntity</code> that can be the parent of a
 * <code>ProjectEntry</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface ProjectEntryParent extends ModelEntity {


	/**
	 * Adds a new project entry.
	 *
	 * @param entry The entry to add.
	 * @see #addEntry(ProjectEntry)
	 */
	void addEntry(ProjectEntry entry);


	/**
	 * Returns an iterator over the child project entries.
	 *
	 * @return The child project entry iterator.
	 */
	Iterator<ProjectEntry> getEntryIterator();


	boolean moveProjectEntryDown(ProjectEntry entry);


	boolean moveProjectEntryUp(ProjectEntry entry);


	/**
	 * Removes a project entry.
	 *
	 * @param entry The entry to remove.
	 * @see #addEntry(ProjectEntry)
	 */
	void removeEntry(ProjectEntry entry);


}