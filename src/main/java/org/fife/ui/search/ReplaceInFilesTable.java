/*
 * 9/19/2006
 *
 * ReplaceInFilesTable.java - A table listing replace results in a Replace
 * in Files dialog.
 * Copyright (C) 2006 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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
	@Override
	protected Vector<String> createMatchDataVector(String fileName,
			MatchData data) {
		Vector<String> v = new Vector<String>(2);
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
	@Override
	protected DefaultTableModel createTableModel(ResourceBundle msg) {
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn(msg.getString("FindInFiles.Column.File"));
		model.addColumn(msg.getString("ReplaceInFiles.Column.Replacement"));
		return model;
	}


	/**
	 * Initializes the column widths.
	 */
	@Override
	protected void initColumnWidths() {
		TableColumnModel columnModel = getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(100);
		columnModel.getColumn(1).setPreferredWidth(60);
	}


}