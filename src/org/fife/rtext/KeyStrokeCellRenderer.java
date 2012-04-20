/*
 * 11/05/2009
 *
 * KeyStrokeCellRenderer.java - Renderers Keystrokes in a JTable.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * Renderer for KeyStrokes in a JTable.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class KeyStrokeCellRenderer extends DefaultTableCellRenderer {


	public Component getTableCellRendererComponent(JTable table,
							Object value, boolean selected, boolean focused,
							int row, int column) {
		super.getTableCellRendererComponent(table, value, selected,
											focused, row, column);
		KeyStroke ks = (KeyStroke)value;
		setText(RTextUtilities.getPrettyStringFor(ks));
		setComponentOrientation(table.getComponentOrientation());
		return this;
	}


}