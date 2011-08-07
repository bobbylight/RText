/*
 * 07/30/2011
 *
 * DecorativeIconPanel.java - Displays a small decorative icon beside some
 * other component.
 * Copyright (C) 2011 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
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

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;


/**
 * A panel that displays an 8x8 decorative icon for a component, such as a
 * text field or combo box.  This can be used to display error icons, warning
 * icons, etc.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see AssistanceIconPanel
 */
public class DecorativeIconPanel extends JPanel {

	/**
	 * The width of this icon panel, to help align the components we're
	 * listening to with other combo boxes or text fields without a
	 * DecorativeIconPanel.
	 */
	public static final int WIDTH		= 8;

	private JLabel iconLabel;
	private boolean showIcon;
	private String tip;

	protected static final EmptyIcon EMPTY_ICON = new EmptyIcon(WIDTH, WIDTH);


	/**
	 * Constructor.
	 */
	public DecorativeIconPanel() {
		setLayout(new BorderLayout());
		iconLabel = new JLabel(EMPTY_ICON) {
			public String getToolTipText(MouseEvent e) {
				return showIcon ? tip : null;
			}
		};
		iconLabel.setVerticalAlignment(SwingConstants.TOP);
		ToolTipManager.sharedInstance().registerComponent(iconLabel);
		add(iconLabel, BorderLayout.NORTH);
	}


	/**
	 * Returns the icon being displayed.
	 *
	 * @return The icon.
	 * @see #setIcon(Icon)
	 */
	public Icon getIcon() {
		return iconLabel.getIcon();
	}


	/**
	 * Returns whether the icon (if any) is being rendered.
	 *
	 * @return Whether the icon is being rendered.
	 * @see #setShowIcon(boolean)
	 */
	public boolean getShowIcon() {
		return showIcon;
	}


	/**
	 * Returns the tool tip displayed when the mouse hovers over the icon.
	 * If the icon is not being displayed, this parameter is ignored.
	 *
	 * @return The tool tip text.
	 * @see #setToolTipText(String)
	 */
	public String getToolTipText() {
		return tip;
	}


	/**
	 * Paints any child components.  Overridden so the user can explicitly
	 * hide the icon.
	 *
	 * @param g The graphics context.
	 * @see #setShowIcon(boolean)
	 */
	protected void paintChildren(Graphics g) {
		if (showIcon) {
			super.paintChildren(g);
		}
	}


	/**
	 * Sets the icon to display.
	 *
	 * @param icon The new icon to display.
	 * @see #getIcon()
	 */
	public void setIcon(Icon icon) {
		if (icon==null) {
			icon = EMPTY_ICON;
		}
		iconLabel.setIcon(icon);
	}


	/**
	 * Toggles whether the icon should be shown.
	 *
	 * @param show Whether to show the icon.
	 * @see #getShowIcon()
	 */
	public void setShowIcon(boolean show) {
		if (show!=showIcon) {
			showIcon = show;
			repaint();
		}
	}


	/**
	 * Sets the tool tip text to display when the mouse is over the icon.
	 * This parameter is ignored if the icon is not being displayed.
	 *
	 * @param tip The tool tip text to display.
	 * @see #getToolTipText()
	 */
	public void setToolTipText(String tip) {
		this.tip = tip;
	}


}