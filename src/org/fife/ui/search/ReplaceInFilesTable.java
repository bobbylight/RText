/*
 * 9/19/2006
 *
 * ReplaceInFilesTable.java - A table listing replace results in a Replace
 * in Files dialog.
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

import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;


/**
 * The table used to display search results in a
 * <code>ReplaceInFilesDialog</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ReplaceInFilesTable extends FindInFilesTable {


	/**
	 * Return the vector of data to display in the table for a
	 * match data instance.
	 *
	 * @param fileName The (relative) filename.
	 * @param data The match data.
	 * @return The vector.
	 */
	protected Vector createMatchDataVector(String fileName, MatchData data) {
		Vector v = new Vector(2);
		v.add(fileName);
		v.add(data.getLineText());
		return v;
	}


	/**
	 * Returns the table model to use.
	 *
	 * @param msg The resource bundle.
	 * @return The table model.
	 */
	protected DefaultTableModel createTableModel(ResourceBundle msg) {
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn(msg.getString("FindInFiles.Column.File"));
		model.addColumn(msg.getString("ReplaceInFiles.Column.Replacement"));
		return model;
	}


	/**
	 * Initializes the column widths.
	 */
	protected void initColumnWidths() {
		TableColumnModel columnModel = getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(100);
		columnModel.getColumn(1).setPreferredWidth(60);
	}


}