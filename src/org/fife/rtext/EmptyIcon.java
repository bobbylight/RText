/*
 * 03/28/2010
 *
 * EmptyIcon.java - An empty icon.
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
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
package org.fife.rtext;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;


/**
 * An icon that paints nothing.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class EmptyIcon implements Icon {

	private int width;
	private int height;


	/**
	 * Constructor.
	 *
	 * @param width The width of the icon.
	 * @param height The height of the icon.
	 */
	public EmptyIcon(int width, int height) {
		this.width = width;
		this.height = height;
	}


	/**
	 * {@inheritDoc}
	 */
	public int getIconHeight() {
		return height;
	}


	/**
	 * {@inheritDoc}
	 */
	public int getIconWidth() {
		return width;
	}


	/**
	 * Paints nothing.
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) {
		// Do nothing
	}


}