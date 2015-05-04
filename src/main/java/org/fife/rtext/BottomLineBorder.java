/*
 * 06/13/2005
 *
 * BottomLineBorder.java - A minimal tool bar border.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;


/**
 * A border that draws a single 1-pixel line across the bottom of the
 * component.  Useful for minimal "toolbars" in dockable windows.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class BottomLineBorder extends AbstractBorder {

	private int horizInsets;

	public BottomLineBorder(int horizInsets) {
		this.horizInsets = horizInsets;
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return getBorderInsets(c, new Insets(0, 0, 0, 0));
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		insets.top = 0;
		insets.right = insets.left = horizInsets;
		insets.bottom = 1;
		return insets;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y,
							int width, int height) {
		g.setColor(UIManager.getColor("controlDkShadow"));
		y = y + height - 1;
		g.drawLine(x,y, x+width-1,y);
	}

}