/*
 * 12/15/2008
 *
 * GroupTreeNode.java - A tree node that contains a group of children in the
 * Source Browser.
 * Copyright (C) 2008 Robert Futrell
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
package org.fife.rtext.plugins.sourcebrowser;

import javax.swing.Icon;


/**
 * The tree node for the "group" nodes in the Source Browser tree.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class GroupTreeNode extends SourceTreeNode {

	private Icon icon;


	/**
	 * Constructor.
	 *
	 * @param icon The icon for this node's children.  This may be
	 *        <code>null</code>.
	 */
	public GroupTreeNode(Icon icon) {
		super(null, false);
		setIcon(icon);
	}


	/**
	 * Returns the icon for this tree node's children.
	 *
	 * @return The icon, or <code>null</code> if no icon is specified.
	 * @see #setIcon(Icon)
	 */
	public Icon getIcon() {
		return icon;
	}


	/**
	 * Sets the icon for this tree node's children.
	 *
	 * @param icon The icon.
	 * @see #getIcon()
	 */
	public void setIcon(Icon icon) {
		this.icon = icon;
	}


}