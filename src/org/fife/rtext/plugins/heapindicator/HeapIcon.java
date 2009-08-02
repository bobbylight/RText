/*
 * 09/16/2005
 *
 * HeapIndicatorPlugin.java - Status bar panel showing the current JVM heap.
 * Copyright (C) 2005 Robert Futrell
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
package org.fife.rtext.plugins.heapindicator;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;


/**
 * Icon displaying the JVM heap's state.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class HeapIcon implements Icon {

	private HeapIndicatorPlugin plugin;


	public HeapIcon(HeapIndicatorPlugin plugin) {
		this.plugin = plugin;
	}


	public int getIconHeight() {
		return plugin.getHeight() - 8;
	}


	public int getIconWidth() {
		return 40;
	}


	public void paintIcon(Component c, Graphics g, int x, int y) {
		int width = getIconWidth() - 1;
		int height = getIconHeight() - 1;
		g.setColor(plugin.getIconBorderColor());
		g.drawLine(x,y+1,          x,y+height-1);
		g.drawLine(x+width,y+1,    x+width,y+height-1);
		g.drawLine(x+1,y,          x+width-1,y);
		g.drawLine(x+1,y+height, x+width-1,y+height);
		g.setColor(plugin.getIconForeground());
		long usedMem = plugin.getUsedMemory();
		long totalMem = plugin.getTotalMemory();
		int x2 = (int)(width*((float)usedMem/(float)totalMem));
		x++;
		if (c.getComponentOrientation().isLeftToRight()) {
			g.fillRect(x,y+1, x2-x,height-1);
		}
		else {
			g.fillRect((x+width)-x2,y+1, x2-x,height-1);
		}
	}


}