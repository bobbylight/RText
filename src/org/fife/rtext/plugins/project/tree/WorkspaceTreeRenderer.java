package org.fife.rtext.plugins.project.tree;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 * The renderer for workspace tree views.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class WorkspaceTreeRenderer extends DefaultTreeCellRenderer {

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean focused) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focused);

		AbstractWorkspaceTreeNode node = (AbstractWorkspaceTreeNode)value;
		setIcon(node.getIcon());

		return this;

	}


}