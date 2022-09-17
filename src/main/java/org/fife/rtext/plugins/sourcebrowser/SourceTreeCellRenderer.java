/*
 * 01/13/2013
 *
 * SourceTreeCellRenderer - Cell renderer for default source tree nodes.
 * Copyright (C) 2013 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.sourcebrowser;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;


/**
 * Cell renderer for <code>DefaultSourceTree</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
final class SourceTreeCellRenderer extends DefaultTreeCellRenderer {

	private final DefaultSourceTree tree;
	private final Icon blueBullet;
	private final Icon greenBullet;


	/**
	 * Constructor.
	 *
	 * @param tree The source tree we're rendering in.
	 * @param blueBullet The blue bullet icon.
	 * @param greenBullet The green bullet icon.
	 */
	SourceTreeCellRenderer(DefaultSourceTree tree, Icon blueBullet, Icon greenBullet) {
		this.tree = tree;
		this.blueBullet = blueBullet;
		this.greenBullet = greenBullet;
	}


	@Override
	public Component getTreeCellRendererComponent(JTree tree,
						Object value, boolean sel, boolean expanded,
						boolean leaf, int row, boolean focused) {

		super.getTreeCellRendererComponent(tree, value, sel,
								expanded, leaf, row, focused);

		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)value;
		Object obj = dmtn.getUserObject();

		if (obj instanceof String str) { // As opposed to TagEntry.
			int index = str.indexOf('(');
			if (index>-1) { // Not true if ctags not found.
				setText("<html>" + str.substring(0,index) + "<b>" +
					str.substring(index) + "</b></html>");
			}
		}

		// Determine what icon to use.
		Icon icon = null;
		if (dmtn instanceof GroupTreeNode gtn) {
			icon = gtn.getIcon();
		}
		else {
			TreeNode parent = dmtn.getParent();
			if (parent instanceof GroupTreeNode gtn) {
				icon = gtn.getIcon();
			}
		}
		if (icon==null) { // Languages without custom icons.
			if (leaf && value!=null) {
				String strVal = value.toString();
				if (strVal!=null && !strVal.contains("(0)")) {
					setIcon(icon = greenBullet);
				}
			}
			if (/*getIcon()*/icon==null) {
				setIcon(row==0 ? this.tree.getRootIcon() : blueBullet);
			}
		}
		else {
			setIcon(icon);
		}

		setOpaque(true);
		return this;

	}


}
