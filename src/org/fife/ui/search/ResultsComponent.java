/*
 * 09/19/2006
 *
 * ResultsComponent.java - A component displaying search/replace results.
 * Copyright (C) 2006 Robert Futrell
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