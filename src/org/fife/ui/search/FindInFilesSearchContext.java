/*
 * 08/18/2012
 *
 * FindInFilesSearchContext.java - Search context for Find in Files dialogs.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.search;

import org.fife.ui.rtextarea.SearchContext;


/**
 * A search context that also contains options relevant to a Find in Files
 * or Replace in Files dialog.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FindInFilesSearchContext extends SearchContext {

	private boolean searchSubfolders;
	private boolean verbose;


	/**
	 * Returns whether subfolders should be searched.
	 *
	 * @return Whether subfolders should be searched.
	 * @see #setSearchSubfolders(boolean)
	 */
	public boolean getSearchSubfolders() {
		return searchSubfolders;
	}


	/**
	 * Returns whether verbose output should be enabled.
	 *
	 * @return Whether verbose output should be enabled.
	 * @see #setVerbose(boolean)
	 */
	public boolean getVerbose() {
		return verbose;
	}


	/**
	 * Sets whether subfolders should be searched.
	 *
	 * @param search Whether to search subfolders.
	 * @see #getSearchSubfolders()
	 */
	public void setSearchSubfolders(boolean search) {
		searchSubfolders = search;
	}


	/**
	 * Sets whether verbose output should be enabled.
	 *
	 * @param verbose Whether verbose output should be enabled.
	 * @see #getVerbose()
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}


}