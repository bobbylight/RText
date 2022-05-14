/*
 * 09/08/2012
 *
 * ProjectEntryParent.java - A parent model entity of project entries.
 * Copyright (C) 2012 Robert Futrell
 * https://fifesoft.com/rtext
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


	/**
	 * Moves a tree node down in its parent's list of children, if
	 * possible.
	 *
	 * @param entry The node to move down.
	 * @param toBottom Whether to move it to the bottom, as opposed to
	 *        just down one position.
	 * @return Whether the operation was successful.
	 * @see #moveProjectEntryUp(ProjectEntry, boolean)
	 */
	boolean moveProjectEntryDown(ProjectEntry entry, boolean toBottom);


	/**
	 * Moves a tree node up in its parent's list of children, if
	 * possible.
	 *
	 * @param entry The node to move up.
	 * @param toTop Whether to move it to the top, as opposed to
	 *        just up one position.
	 * @return Whether the operation was successful.
	 * @see #moveProjectEntryDown(ProjectEntry, boolean)
	 */
	boolean moveProjectEntryUp(ProjectEntry entry, boolean toTop);


	/**
	 * Removes a project entry.
	 *
	 * @param entry The entry to remove.
	 * @see #addEntry(ProjectEntry)
	 */
	void removeEntry(ProjectEntry entry);


}
