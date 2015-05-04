/*
 * 08/11/2004
 *
 * FindInFilesEvent.java - Event fired from FindInFileDialogs when the user
 *                                   selects a match.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.search;

import java.util.EventObject;


/**
 * Event fired by <code>FindInFileDialog</code>s when the user clicks on a
 * match.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class FindInFilesEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	/**
	 * The name of the file for the match they clicked.
	 */
	private String fileName;

	/**
	 * The line number of the match they clicked.  Note that this value may be
	 * <code>-1</code> signifying that no match was found (i.e., they clicked on
	 * a "verbose" informational line).
	 */
	private int line;


	/**
	 * Constructor.
	 *
	 * @param source The find-in-files dialog that fired this event.
	 * @param fileName The name of the file of the match they clicked.
	 * @param line The line number of the match they clicked.
	 */
	public FindInFilesEvent(Object source, String fileName, int line) {
		super(source);
		this.fileName = fileName;
		this.line = line;
	}


	/**
	 * Returns the name of the file for the match of this event.
	 *
	 * @return The file name.
	 */
	public String getFileName() {
		return fileName;
	}


	/**
	 * Returns the line number for the match of this event.
	 *
	 * @return The line number.
	 */
	public int getLine() {
		return line;
	}


}