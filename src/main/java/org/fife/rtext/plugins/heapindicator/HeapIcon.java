/*
 * 09/16/2005
 *
 * HeapIndicatorPlugin.java - Status bar panel showing the current JVM heap.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.heapindicator;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Graphics;
import java.util.Locale;

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
		// Not sure why panel's orientation doesn't change, do JPanels not
		// set orientations??
		//if (c.getComponentOrientation().isLeftToRight()) {
		if (ComponentOrientation.getOrientation(Locale.getDefault()).
														isLeftToRight()) {
			g.fillRect(x,y+1, x2-x,height-1);
		}
		else {
			g.fillRect((x+width)-x2,y+1, x2-x,height-1);
		}
	}


}