/*
 * 03/27/2010
 *
 * XmlOptions.java - Options for tidying XML.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tidy;

import org.fife.ui.app.Prefs;


/**
 * Options for tidying XML.<p>
 *
 * While all fields in this class are public, please use the getters and
 * setters for accessing them for bounds checking, etc.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class XmlOptions extends Prefs {

	public boolean addXmlDeclaration;
	public int xmlSpaceCount;
	public int xmlWrapLength;


	/**
	 * Returns whether the XML declaration should be added.
	 *
	 * @return Whether the XML declaration should be added.
	 * @see #setAddXmlDeclaration(boolean)
	 */
	public boolean getAddXmlDeclaration() {
		return addXmlDeclaration;
	}


	/**
	 * Returns the number of spaces to use for indentation when formatting
	 * XML.
	 *
	 * @return The number of spaces to use.
	 * @see #setSpaceCount(int)
	 */
	public int getSpaceCount() {
		return xmlSpaceCount;
	}


	/**
	 * Returns how long formatted lines can be before they are wrapped.
	 *
	 * @return The maximum length of a formatted line, or "0" for no limit.
	 * @see #setWrapLength(int)
	 */
	public int getWrapLength() {
		return xmlWrapLength;
	}


	/**
	 * Sets whether the XML declaration should be added.
	 *
	 * @param add Whether the XML declaration should be added.
	 * @see #getAddXmlDeclaration()
	 */
	public void setAddXmlDeclaration(boolean add) {
		addXmlDeclaration = add;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaults() {
		addXmlDeclaration = true;
		xmlSpaceCount = 4;
	}


	/**
	 * Sets the number of spaces to use for indentation when formatting XML.
	 *
	 * @param count The number of spaces.
	 * @see #getSpaceCount()
	 */
	public void setSpaceCount(int count) {
		xmlSpaceCount = count;
	}


	/**
	 * Sets how long formatted lines can be before they are wrapped.
	 *
	 * @param length The maximum length of a formatted line, or "0" for no
	 *        limit.
	 * @see #getWrapLength()
	 */
	public void setWrapLength(int length) {
		xmlWrapLength = Math.max(0, length);
	}


}