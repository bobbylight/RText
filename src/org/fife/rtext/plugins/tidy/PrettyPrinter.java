/*
 * 02/15/2013
 *
 * PrettyPrinter - Pretty-prints content for some language.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tidy;


/**
 * A pretty printer for one or more languages.
 *
 * @author Robert Futrell
 * @version 1.0
 */
interface PrettyPrinter {

	/**
	 * Specifies that there were no issues formatting the source.
	 */
	public static final int RESULT_OK				= 0;

	/**
	 * Specifies that there were warnings formatting the source.
	 */
	public static final int RESULT_WARNINGS			= 1;

	/**
	 * Specifies that there were errors formatting the source.
	 */
	public static final int RESULT_ERRORS			= 2;


	/**
	 * Pretty prints the specified text.
	 *
	 * @param text The text to pretty print.
	 * @return The result of pretty printing the text.
	 * @throws Exception If something goes wrong.
	 */
	PrettyPrintResult prettyPrint(String text) throws Exception;


}