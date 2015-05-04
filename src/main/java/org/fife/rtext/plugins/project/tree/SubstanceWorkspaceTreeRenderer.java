/*
 * 01/14/2012
 *
 * SubstanceWorkspaceTreeRenderer - Renderer for workspace tree nodes when
 * Substance is installed.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.awt.Component;
import javax.swing.JTree;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTreeCellRenderer;


/**
 * The renderer for workspace tree views when Substance is installed.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SubstanceWorkspaceTreeRenderer extends SubstanceDefaultTreeCellRenderer {


	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean focused) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
				row, focused);
		// Not true the first time through!
		if (value instanceof AbstractWorkspaceTreeNode) {
			AbstractWorkspaceTreeNode node = (AbstractWorkspaceTreeNode)value;
			setIcon(node.getIcon());
		}
		return this;
	}


}