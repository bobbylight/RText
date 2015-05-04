/*
 * 09/08/2012
 *
 * ModelEntity.java - A component in the model of a workspace.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.model;


/**
 * A component in the model of a workspace.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface ModelEntity {

	/**
	 * Called when a visitor visits this entity.
	 *
	 * @param visitor The visitor.
	 */
	void accept(WorkspaceVisitor visitor);

}