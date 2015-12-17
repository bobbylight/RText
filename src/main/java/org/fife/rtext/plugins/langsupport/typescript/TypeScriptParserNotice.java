/*
 * 12/14/2015
 *
 * Copyright (C) 2015 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport.typescript;

import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.Parser;


/**
 * The parser notice type used in this package.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class TypeScriptParserNotice extends DefaultParserNotice {

	private String fileFullPath;


	/**
	 * Constructor.
	 *
	 * @param parser The parser that created this notice.
	 * @param msg The text of the message.
	 * @param line The line number for the message.
	 */
	public TypeScriptParserNotice(Parser parser, String msg, int line) {
		super(parser, msg, line);
	}


	/**
	 * Returns the file that contained this error or warning.
	 *
	 * @return The file.
	 * @see #setFileFullPath(String)
	 */
	public String getFileFullPath() {
		return fileFullPath;
	}


	/**
	 * Sets the file that contained this error or warning.
	 *
	 * @param fileFullPath The file.
	 * @see #getFileFullPath()
	 */
	public void setFileFullPath(String fileFullPath) {
		this.fileFullPath = fileFullPath;
	}


}
