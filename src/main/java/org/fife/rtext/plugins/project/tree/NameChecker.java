/*
 * 08/28/2012
 *
 * NameChecker.java - Returns whether the new name for a tree node is valid.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;


/**
 * Determines whether text is valid for a particular field in a form.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface NameChecker {

	/**
	 * Determines whether the text is valid.
	 *
	 * @param text The new value.
	 * @return <code>null</code> if the new name is valid, or an error message
	 *         otherwise.
	 */
	String isValid(String text);


}