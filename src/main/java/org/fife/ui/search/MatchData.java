/*
 * 10/03/2005
 *
 * MatchData.java - Information about a match found in a file by a
 * FindInFilesDialog.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.search;


/**
 * Information on a match found when searching a document.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class MatchData {

	private String fileName;
	private String lineNumber;
	private String lineText;
	private int type;

	public static final int TYPE_MATCH		= 0;
	public static final int TYPE_VERBOSE	= 1;
	public static final int TYPE_ERROR		= 2;


	public MatchData(String fileName, String lineNumber, String lineText) {
		this(fileName, lineNumber, lineText, TYPE_MATCH);
	}


	public MatchData(String fileName, String lineNumber, String lineText,
					int type) {
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.lineText = lineText;
		this.type = type;
	}


	public String getFileName() {
		return fileName;
	}


	public String getLineNumber() {
		return lineNumber;
	}


	public String getLineText() {
		return lineText;
	}


	public boolean isError() {
		return type==TYPE_ERROR;
	}


	public boolean isMatchData() {
		return type==TYPE_MATCH;
	}


	public boolean isVerboseSearchInfo() {
		return type==TYPE_VERBOSE;
	}


}