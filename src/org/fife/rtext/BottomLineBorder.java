/*
 * 06/13/2005
 *
 * BottomLineBorder.java - A minimal tool bar border.
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

	public Insets getBorderInsets(Component c) {
		return getBorderInsets(c, new Insets(0, 0, 0, 0));
	}

	public Insets getBorderInsets(Component c, Insets insets) {
		insets.top = 0;
		insets.right = insets.left = horizInsets;
		insets.bottom = 1;
		return insets;
	}

	public void paintBorder(Component c, Graphics g, int x, int y,
							int width, int height) {
		g.setColor(UIManager.getColor("controlDkShadow"));
		y = y + height - 1;
		g.drawLine(x,y, x+width-1,y);
	}

}