/*
 * 11/05/2009
 *
 * KeyStrokeCellRenderer.java - Renderers Keystrokes in a JTable.
 * Copyright (C) 2009 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This file is a part of RText.
 *
 * RText is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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