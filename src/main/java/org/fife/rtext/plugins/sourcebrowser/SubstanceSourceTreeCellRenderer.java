/*
 * 01/13/2013
 *
 * SubstanceSourceTreeCellRenderer - Cell renderer for default source tree
 * nodes when Substance is installed.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.sourcebrowser;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTreeCellRenderer;


/**
 * Cell renderer for <code>DefaultSourceTree</code> when Substance is
 * installed.
 * 
 * @author Robert Futrell
 * @version 1.0
 */
class SubstanceSourceTreeCellRenderer extends SubstanceDefaultTreeCellRenderer {

	private DefaultSourceTree tree;
	private Icon blueBullet;
	private Icon greenBullet;

	private static final String BLUE_BULLET	= "bullet_blue.gif";
	private static final String GREEN_BULLET	= "bullet_green.gif";


	/**
	 * Don't use this constructor; use
	 * {@link SourceTreeCellRenderer#createTreeCellRenderer(DefaultSourceTree)}
	 * to ensure the proper renderer is always used.
	 *
	 * @param tree The source tree we're rendering in.
	 */
	public SubstanceSourceTreeCellRenderer(DefaultSourceTree tree) {
		this.tree = tree;
		Class<?> clazz = getClass();
		blueBullet = new ImageIcon(clazz.getResource(BLUE_BULLET));
		greenBullet = new ImageIcon(clazz.getResource(GREEN_BULLET));
	}


	// TODO: Share code between this class and SourceTreeCellRenderer.
	@Override
	public Component getTreeCellRendererComponent(JTree tree,
						Object value, boolean sel, boolean expanded,
						boolean leaf, int row, boolean focused) {

		super.getTreeCellRendererComponent(tree, value, sel,
								expanded, leaf, row, focused);

		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)value;
		Object obj = dmtn.getUserObject();

		if (obj instanceof String) { // As opposed to TagEntry.
			String str = (String)obj;
			int index = str.indexOf('(');
			if (index>-1) { // Not true if ctags not found.
				setText("<html>" + str.substring(0,index) + "<b>" +
					str.substring(index) + "</b></html>");
			}
		}

		// Determine what icon to use.
		Icon icon = null;
		if (dmtn instanceof GroupTreeNode) {
			GroupTreeNode gtn = (GroupTreeNode)dmtn;
			icon = gtn.getIcon();
		}
		else {
			TreeNode parent = dmtn.getParent();
			if (parent instanceof GroupTreeNode) {
				GroupTreeNode gtn = (GroupTreeNode)parent;
				icon = gtn.getIcon();
			}
		}
		if (icon==null) { // Languages without custom icons.
			if (leaf && value!=null) {
				String strVal = value.toString();
				if (strVal!=null && strVal.indexOf("(0)")==-1) {
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

		return this;

	}


}