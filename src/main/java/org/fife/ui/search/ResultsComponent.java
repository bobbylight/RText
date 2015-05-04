/*
 * 09/19/2006
 *
 * ResultsComponent.java - A component displaying search/replace results.
 * Copyright (C) 2006 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.search;


/**
 * Interface that identifies a component that displays search or
 * replace information in a <code>FindInFilesDialog</code> or
 * <code>ReplaceInFilesDialog</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
interface ResultsComponent {

	/**
	 * Adds data on a match to the component.
	 *
	 * @param matchData The data.
	 * @param dirName The "root directory" searching was done in.  This is
	 *        used so all file paths displayed in are abbreviated to be
	 *        relative to this directory.
	 * @see #clear()
	 */
	public void addMatchData(MatchData matchData, String dirName);


	/**
	 * Clears all match results from the table.
	 *
	 * @see #addMatchData(MatchData, String)
	 */
	public void clear();


	/**
	 * Returns the match data displayed in the specified row.
	 *
	 * @param row The row.
	 * @return The match data.
	 */
	public MatchData getMatchDataForRow(int row);


	/**
	 * Returns the number of rows displayed in this component.
	 *
	 * @return The number of rows displayed.
	 */
	public int getRowCount();


	/**
	 * Returns the row selected, or <code>-1</code> if none.
	 *
	 * @return The row selected.
	 */
	public int getSelectedRow();


	/**
	 * Allows the results component to update its appearance after
	 * having lots of data added to it.
	 */
	public void prettyUp();


}