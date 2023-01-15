/*
 * 09/08/2012
 *
 * AbstractProjectEntry.java - Base class for ProjectEntry implementations.
 * Copyright (C) 2012 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.model;


/**
 * Base class for <code>ProjectEntry</code> implementations.
 *
 * @author Robert Futrell
 * @version 1.0
 */
abstract class AbstractProjectEntry implements ProjectEntry {

	private final ProjectEntryParent parent;


	AbstractProjectEntry(ProjectEntryParent parent) {
		this.parent = parent;
	}


	@Override
	public final ProjectEntryParent getParent() {
		return parent;
	}


	@Override
	public final void removeFromParent() {
		parent.removeEntry(this);
	}


}
