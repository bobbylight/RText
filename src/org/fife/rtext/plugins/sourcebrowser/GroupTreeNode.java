/*
 * 12/15/2008
 *
 * GroupTreeNode.java - A tree node that contains a group of children in the
 * Source Browser.
 * Copyright (C) 2008 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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