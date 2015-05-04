/*
 * 10/03/2005
 *
 * FindInFilesTable.java - A table listing search results in a Find in Files
 * dialog.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.fife.ui.FileExplorerTableModel;
import org.fife.ui.RListSelectionModel;
import org.fife.ui.FileExplorerTableModel.SortableHeaderRenderer;
import org.fife.ui.autocomplete.Util;


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
	private List<MatchData> matchDatas;

	private StandardCellRenderer defaultRenderer;
	private VerboseCellRenderer verboseRenderer;

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

		matchDatas = new ArrayList<MatchData>();
		defaultRenderer = new StandardCellRenderer();

		// By default, tables are registered to give tool tips.  This causes
		// Disable this so the renderer isn't asked for each time the mouse
		// moves, as if styled results are enabled, this can cause some decent
		// slowdown.
		ToolTipManager.sharedInstance().unregisterComponent(this);
		ToolTipManager.sharedInstance().unregisterComponent(getTableHeader());

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
		Vector<String> v = createMatchDataVector(fileName, matchData);
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
	@Override
	public void applyComponentOrientation(ComponentOrientation o) {

		super.applyComponentOrientation(o);

		// Default renderers installed into all tables.
		TableCellRenderer r = getDefaultRenderer(Object.class);
		if (r instanceof Component) { // Never null for JTable
			Component c = (Component)r;
			c.applyComponentOrientation(o);
		}
		r = getDefaultRenderer(Number.class);
		if (r instanceof Component) { // Never null for JTable
			Component c = (Component)r;
			c.applyComponentOrientation(o);
		}
		r = getDefaultRenderer(Boolean.class);
		if (r instanceof Component) { // Never null for JTable
			Component c = (Component)r;
			c.applyComponentOrientation(o);
		}

		// Must get the header too.
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

		// Get custom renderers to fix them up as well.
		defaultRenderer.applyComponentOrientation(o);
		if (verboseRenderer!=null) {
			verboseRenderer.applyComponentOrientation(o);
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
	protected Vector<String> createMatchDataVector(String fileName,
			MatchData data) {
		Vector<String> v = new Vector<String>(3);
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
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		MatchData data = getMatchDataForRow(row);
		if (data.isVerboseSearchInfo() || data.isError()) {
			if (verboseRenderer==null)
				verboseRenderer = new VerboseCellRenderer();
			return verboseRenderer;
		}
		return defaultRenderer;
	}


	/**
	 * Returns the match data displayed in the specified row.
	 *
	 * @param row The row.
	 * @return The match data.
	 */
	public MatchData getMatchDataForRow(int row) {
		row = sorter.modelIndex(row);
		return matchDatas.get(row);
	}


	/**
	 * Returns the preferred size of this table.
	 *
	 * @return The preferred size of this table.
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return new Dimension(100, getRowHeight()*8);
	}


	/**
	 * Overridden to ensure the table completely fills the JViewport it
	 * is sitting in.  Note in Java 6 this could be taken care of by the
	 * method JTable#setFillsViewportHeight(boolean).
	 */
	@Override
	public boolean getScrollableTracksViewportHeight() {
		Component parent = getParent();
		return parent instanceof JViewport ?
			parent.getHeight()>getPreferredSize().height : false;
	}


	/**
	 * Overridden in a "hack" to ensure that the table's contents are
	 * always at least as large as the enclosing <code>JScrollPane</code>'s
	 * viewport.
	 */
	@Override
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
	@Override
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
	 * Resizes the columns of the table to accommodate their data.
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

				// If we're in the HTML column and there are a lot of results,
				// we'll get the value of the non-HTML text to speed things up
				// a little.
				String value = (String)getValueAt(i, j);
				if (rowCount>3000 && value.startsWith("<html>")) {
					value = Util.stripHtml(value);
				}

				Component comp = renderer.getTableCellRendererComponent(
								this, value, false, false, i, j);
				width = Math.max(width, comp.getPreferredSize().width);

			}

			// Set the size of the column.
			// NOTE: Why do we need to add a small amount to prevent "..."?
			column.setPreferredWidth(width + 20);

		}

	}


	/**
	 * Overridden to keep the left-hand side of the row visible on selection.
	 * This is because otherwise the viewport jumps to show the entire cell
	 * selected.
	 */
	@Override
	public void scrollRectToVisible(Rectangle r) {
		r.x = 0; r.width = 0;
		super.scrollRectToVisible(r);
	}


	/**
	 * Overridden to also update the UI of custom renderers.
	 */
	@Override
	public void updateUI() {

		/*
		 * NOTE: This is silly, but it's what it took to get a LaF change to
		 * occur without throwing an NPE because of the JRE bug.  No doubt there
		 * is a better way to handle this.  What we do is:
		 *
		 * 1. Before updating the UI, reset the JTableHeader's default renderer
		 *    to what it was originally.  This prevents the NPE from the JRE
		 *    bug, since we're no longer using the renderer with a cached
		 *    Windows-specific TableCellRenderer (when Windows LaF is enabled).
		 * 2. Update the UI, like normal.
		 * 3. After the update, we must explicitly re-set the JTableHeader as
		 *    the column view in the enclosing JScrollPane.  This is done by
		 *    default the first time you add a JTable to a JScrollPane, but
		 *    since we gave the JTable a new header as a workaround for the JRE
		 *    bug, we must explicitly tell the JScrollPane about it as well.
		 */

		// Temporarily set the table header's renderer to the default.
		Container parent = getParent();
		if (parent!=null) { // First time through, it'll be null
			TableCellRenderer r = getTableHeader().getDefaultRenderer();
			if (r instanceof SortableHeaderRenderer) { // Always true
				SortableHeaderRenderer shr = (SortableHeaderRenderer)r;
				getTableHeader().setDefaultRenderer(shr.getDelegateRenderer());
			}
		}

		super.updateUI();

		// Now set the renderer back to our custom one.
		if (parent!=null) {
			JScrollPane sp = (JScrollPane)parent.getParent();
			sp.setColumnHeaderView(getTableHeader());
			sp.revalidate();
			sp.repaint();
		}

		// Update our custom renderers too.
		if (defaultRenderer!=null) { // First time through, it's null
			defaultRenderer.updateUI();
		}
		if (verboseRenderer!=null) {
			verboseRenderer.updateUI();
		}

	}


	/**
	 * The default renderer for the table.
	 */
	private class StandardCellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table,
								Object value, boolean selected,
								boolean focused, int row, int column) {

			// If it's HTML and selected, don't colorize the HTML, let the
			// text all be the table's "selected text" color.
			if (value instanceof String) {
				String str = (String)value;
				if (str.startsWith("<html>")) {
					if (selected) {
						value = str.replaceAll("color=\"[^\"]+\"", "");
					}
				}
			}

			super.getTableCellRendererComponent(table, value, selected,
												focused, row, column);
			return this;

		}

	}


	/**
	 * Renderer for "verbose information" and "error" cells.
	 */
	private class VerboseCellRenderer extends DefaultTableCellRenderer {

		@Override
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