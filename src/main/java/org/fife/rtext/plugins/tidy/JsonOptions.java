/*
 * 02/18/2013
 *
 * JsonOptions.java - Options for tidying JSON.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tidy;

import org.fife.ui.app.Prefs;


/**
 * Options for tidying JSON.<p>
 *
 * While all fields in this class are public, please use the getters and
 * setters for accessing them for bounds checking, etc.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class JsonOptions extends Prefs {

	public String jsonOutputStyle;
	public int jsonSpaceCount;
	public boolean jsonIndentFirstLevel;

	private static final String DEFAULT_OUTPUT_STYLE = "json";


	/**
	 * Returns whether the "first level" of the top-level JSON object should be
	 * indented.
	 *
	 * @return Whether the "first level" of JSON objects should be indented.
	 * @see #setIndentFirstLevel(boolean)
	 */
	public boolean getIndentFirstLevel() {
		return jsonIndentFirstLevel;
	}


	/**
	 * Returns the style of the formatted JSON.
	 *
	 * @return The formatted style.
	 * @see #setOutputStyle(String)
	 */
	public String getOutputStyle() {
		return jsonOutputStyle;
	}


	/**
	 * Returns the number of spaces to use for indentation when formatting
	 * JSON.
	 *
	 * @return The number of spaces to use.
	 * @see #setSpaceCount(int)
	 */
	public int getSpaceCount() {
		return jsonSpaceCount;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaults() {
		jsonOutputStyle = DEFAULT_OUTPUT_STYLE;
		jsonSpaceCount = 3;
	}


	/**
	 * Sets whether the first level of the top-level JSON object should be
	 * indented.
	 *
	 * @param indent Whether to indent the first level of a JSON object.
	 * @see #getIndentFirstLevel()
	 */
	public void setIndentFirstLevel(boolean indent) {
		jsonIndentFirstLevel = indent;
	}


	/**
	 * Sets the output style to use when formatting JSON.
	 *
	 * @param outputStyle The output style to use.  If this is invalid, a
	 *        default style is used.
	 * @see #getOutputStyle()
	 */
	public void setOutputStyle(String outputStyle) {
		this.jsonOutputStyle = validateOutputStyle(outputStyle);
	}


	/**
	 * Sets the number of spaces to use for indentation when formatting JSON.
	 *
	 * @param count The number of spaces.
	 * @see #getSpaceCount()
	 */
	public void setSpaceCount(int count) {
		jsonSpaceCount = count;
	}


	/**
	 * Verifies that an output style string is valid.
	 *
	 * @param outputStyle The output style string to validate.
	 * @return The output style string to use.
	 */
	private static final String validateOutputStyle(String outputStyle) {
		if (!"json".equals(outputStyle) && !"javascript".equals(outputStyle) &&
				!"minimal".equals(outputStyle)) {
			outputStyle = DEFAULT_OUTPUT_STYLE;
		}
		return outputStyle;
	}


}