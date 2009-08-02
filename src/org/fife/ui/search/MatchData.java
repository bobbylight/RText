/*
 * 10/03/2005
 *
 * MatchData.java - Information about a match found in a file by a
 * FindInFilesDialog.
 * Copyright (C) 2005 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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