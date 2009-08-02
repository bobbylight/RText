/*
 * 10/03/2005
 *
 * FindInFilesTable.java - A table listing search results in a Find in Files
 * dialog.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.fife.ui.FileExplorerTableModel;
import org.fife.ui.RListSelectionModel;


/**
 * The table used to display search results in a
 * <code>FindInFilesDialog</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FindInFilesTable extends JTable implements ResultsComponent {

	private FileExplorerTableModel sorter;
	private DefaultTableModel tableModel;
	private ArrayList matchDatas;

	private TableCellRenderer verboseCellRenderer;

	private static final String MSG = "org.fife.ui.search.FindInFilesTable";


	/**
	 * Constructor.
	 */
	public FindInFilesTable() {

		ResourceBundle msg = ResourceBundle.getBundle(MSG);

		// Create the table model, and make it sortable.
		// Keep a pointer to the "real" table model since it's a
		// DefaultTableModel and is easy to modify.
		tableModel = createTableModel(msg);
		sorter = new FileExplorerTableModel(tableModel);
		setModel(sorter);
		sorter.setTable(this);

		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setSelectionModel(new RListSelectionModel());
		setRowSelectionAllowed(true);
		setShowGrid(false);

		initColumnWidths();

		matchDatas = new ArrayList();

	}


	/**
	 * Adds data on a match to the table.
	 *
	 * @param matchData The data.
	 * @param dirName The "root directory" searching was done in.  This is
	 *        used so all file paths displayed in the table are abbreviated
	 *        to be relative to this directory.
	 * @see #clear()
	 */
	public void addMatchData(MatchData matchData, String dirName) {

		// Make the displayed filename be in a path relative to the
		// directory typed into the Find in Files dialog.
		int pos = 0;
		String fileName = matchData.getFileName().toLowerCase();
		dirName = dirName.toLowerCase();
		int dirNameLength = dirName.length();
		while (pos<dirNameLength &&
					(fileName.charAt(pos)==dirName.charAt(pos) ||
					isFileSeparatorChar(fileName.charAt(pos)))) {
			pos++;
		}
		if (isFileSeparatorChar(fileName.charAt(pos)))
			pos++;
		fileName = matchData.getFileName().substring(pos);

		// We create and pass a Vector since that's what DefaultTableModel
		// uses internally anyway.  This saves, say, creating an Object[]
		// array to pass in.
		Vector v = createMatchDataVector(fileName, matchData);
		tableModel.addRow(v);
		matchDatas.add(matchData);

	}


	/**
	 * Overridden to ensure the table's header and cells are also rendered
	 * correctly (RTL, LTR).  For some reason, Swing doesn't take care of
	 * this by default.
	 *
	 * @param o The new component orientation.
	 */
	public void applyComponentOrientation(ComponentOrientation o) {
		super.applyComponentOrientation(o);
		TableCellRenderer r = getDefaultRenderer(Object.class);
		if (r instanceof Component) { // Never null for JTable
			Component c = (Component)r;
			c.setComponentOrientation(o);
		}
		r = getDefaultRenderer(Number.class);
		if (r instanceof Component) { // Never null for JTable
			Component c = (Component)r;
			c.setComponentOrientation(o);
		}
		r = getDefaultRenderer(Boolean.class);
		if (r instanceof Component) { // Never null for JTable
			Component c = (Component)r;
			c.setComponentOrientation(o);
		}

		if (getTableHeader()!=null) {
			r = getTableHeader().getDefaultRenderer();
			// Should always be the first one.
			if (r instanceof FileExplorerTableModel.SortableHeaderRenderer) {
				((FileExplorerTableModel.SortableHeaderRenderer)r).
								applyComponentOrientation(o);
			}
			else if (r instanceof Component) {
				Component c = (Component)r;
				c.applyComponentOrientation(o);
			}
		}
	}


	/**
	 * Clears all match results from the table.
	 *
	 * @see #addMatchData(MatchData, String)
	 */
	public void clear() {
		tableModel.setRowCount(0);
		matchDatas.clear();
	}


	/**
	 * Return the vector of data to display in the table for a
	 * match data instance.
	 *
	 * @param fileName The (relative) filename.
	 * @param data The match data.
	 * @return The vector.
	 */
	protected Vector createMatchDataVector(String fileName, MatchData data) {
		Vector v = new Vector(3);
		v.add(fileName);
		v.add(data.getLineNumber());
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
		DefaultTableModel tableModel = new DefaultTableModel();
		tableModel.addColumn(msg.getString("FindInFiles.Column.File"));
		tableModel.addColumn(msg.getString("FindInFiles.Column.Line"));
		tableModel.addColumn(msg.getString("FindInFiles.Column.Text"));
		return tableModel;
	}


	/**
	 * Returns the renderer to use for the given cell.
	 *
	 * @param row The row of the cell.
	 * @param column The column of the cell.
	 * @return The renderer.
	 */
	public TableCellRenderer getCellRenderer(int row, int column) {
		MatchData data = getMatchDataForRow(row);
		if (data.isVerboseSearchInfo() || data.isError()) {
			if (verboseCellRenderer==null)
				verboseCellRenderer = new VerboseCellRenderer();
			return verboseCellRenderer;
		}
		return super.getCellRenderer(row, column);
	}


	/**
	 * Returns the match data displayed in the specified row.
	 *
	 * @param row The row.
	 * @return The match data.
	 */
	public MatchData getMatchDataForRow(int row) {
		row = sorter.modelIndex(row);
		return (MatchData)matchDatas.get(row);
	}


	/**
	 * Returns the preferred size of this table.
	 *
	 * @return The preferred size of this table.
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return new Dimension(100, getRowHeight()*8);
	}


	/**
	 * Overridden in a "hack" to ensure that the table's contents are
	 * always at least as large as the enclosing <code>JScrollPane</code>'s
	 * viewport.
	 */
	public boolean getScrollableTracksViewportWidth() {
 		Container parent = getParent();
		if (parent instanceof JViewport) {
			return parent.getSize().getWidth()>getPreferredSize().getWidth();
		}
		return super.getScrollableTracksViewportWidth();
	}


	/**
	 * Initializes the column widths.
	 */
	protected void initColumnWidths() {
		TableColumnModel columnModel = getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(80);
		columnModel.getColumn(1).setPreferredWidth(40);
		columnModel.getColumn(2).setPreferredWidth(180);
	}


	/**
	 * This method always returns false, as match data is immutable.
	 *
	 * @param row The row of the cell.
	 * @param column The column of the cell.
	 * @return <code>false</code> always.
	 */
	public boolean isCellEditable(int row, int column) {
		return false;
	}


	private static final boolean isFileSeparatorChar(char ch) {
		return ch=='\\' || ch=='/';
	}


	/**
	 * Allows the results component to update its appearance after
	 * having lots of data added to it.
	 */
	public void prettyUp() {
		refreshColumnWidths();
		revalidate();
	}


	/**
	 * Resizes the columns of the table to accomodate their data.
	 */
	private void refreshColumnWidths() {

		TableColumnModel columnModel = getColumnModel();
		int columnCount = getColumnCount();
		int width;
		int rowCount = getRowCount();

		for (int j=0; j<columnCount; j++) {

			// Initialize width of column to best width for its header.
			TableColumn column = columnModel.getColumn(j);
			//column.sizeWidthToFit();
			//width = column.getWidth();
			Component c = getTableHeader().getDefaultRenderer().
						getTableCellRendererComponent(this,
							column.getHeaderValue(), false, false, 0, 0); 
			width = c.getPreferredSize().width;

			// Loop through all cells in the column to find the longest.
			for (int i=0; i<rowCount; i++) {
				TableCellRenderer renderer = getCellRenderer(i, j);
				Component comp = renderer.getTableCellRendererComponent(
								this, getValueAt(i, j), false, false,
								i, j);
				int w = comp.getPreferredSize().width;
				if (w>width)
					width = w;
			}

			// Set the size of the column.
			// NOTE: Why do we need to add a small amount to prevent "..."?
			column.setPreferredWidth(width + 20);

		}

	}


	/**
	 * Renderer for "verbose information" and "error" cells.
	 */
	private class VerboseCellRenderer extends DefaultTableCellRenderer {

		public Component getTableCellRendererComponent(JTable table,
								Object value, boolean isSelected,
								boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
										hasFocus, row, column);
			if (!isSelected) {
				MatchData data = getMatchDataForRow(row);
				if (data.isVerboseSearchInfo()) {
					setForeground(Color.GRAY);
				}
				else if (data.isError()) {
					setForeground(Color.RED);
				}
			}
			return this;
		}

	}


}