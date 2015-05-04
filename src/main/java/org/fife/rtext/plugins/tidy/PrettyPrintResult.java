/*
 * 02/15/2013
 *
 * PrettyPrintResult - Result object for a pretty printing operation.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tidy;


/**
 * The results of a pretty-print operation.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class PrettyPrintResult {

	private int result;
	private String text;
	private String summary;


	public PrettyPrintResult(int result, String text, String summary) {
		this.result = result;
		this.text = text;
		this.summary = summary;
	}


	/**
	 * Returns the result of the operation.
	 *
	 * @return The result of the operation.
	 */
	public int getResult() {
		return result;
	}


	/**
	 * Returns a short summary of how the pretty printing went, suitable for
	 * display in a popup window.
	 *
	 * @return A summary, or <code>null</code> if none is available.
	 */
	public String getSummary() {
		return summary;
	}


	/**
	 * Returns the pretty printed text.
	 *
	 * @return The pretty printed text.  This may be <code>null</code> or
	 *         unchanged if {@link #getResult()} does not return success.
	 */
	public String getText() {
		return text;
	}


}