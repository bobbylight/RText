/*
 * 03/28/2010
 *
 * EmptyIcon.java - An empty icon.
 * Copyright (C) 2010 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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


	@Override
	public int getIconHeight() {
		return height;
	}


	@Override
	public int getIconWidth() {
		return width;
	}


	/**
	 * Paints nothing.
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		// Do nothing
	}


}
