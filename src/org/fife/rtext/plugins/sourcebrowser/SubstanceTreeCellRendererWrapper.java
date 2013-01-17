/*
 * 01/15/2013
 *
 * SubstanceTreeCellRendererWrapper - Wraps non-Substance tree cell renderers
 * so our trees look nice in Substance.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.sourcebrowser;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTreeCellRenderer;
import org.pushingpixels.substance.internal.utils.SubstanceStripingUtils;


/**
 * A wrapper tree cell renderer to use for trees whose libraries create and use
 * custom renderers, but we want to look pretty in the Substance Look and Feel.
 * I love how Substance forces inheritance on renderers!<p>
 * 
 * Note that this class only works for renderers that return instances of
 * <code>JLabel</code>, but that's the case for
 * <code>DefaultTreeCellRenderer</code> and is the case for all trees we wrap.
 * 
 * @author Robert Futrell
 * @version 1.0
 */
class SubstanceTreeCellRendererWrapper extends SubstanceDefaultTreeCellRenderer{

	private TreeCellRenderer delegate;


	public SubstanceTreeCellRendererWrapper(TreeCellRenderer delegate) {
		if (!(delegate instanceof JLabel)) {
			throw new IllegalArgumentException(
					"Delegate renderer must extend JLabel");
		}
		this.delegate = delegate;
	}


	/**
	 * Overridden to prevent quirkiness when a node's text is HTML.  See the
	 * implementation of <code>DefaultTreeCellRenderer#getFont()</code> for
	 * more information.
	 */
	public Font getFont() {
		return delegate!=null ? ((JLabel)delegate).getFont() : super.getFont();
	}


	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean focused) {
		SubstanceStripingUtils.applyStripedBackground(tree, row, this);
		Component c = delegate.getTreeCellRendererComponent(tree, value,
				selected, expanded, leaf, row, focused);
		if (c instanceof JLabel) {
			JLabel label = (JLabel)c;
			setFont(label.getFont());
			setText(label.getText());
			setForeground(label.getForeground());
			setEnabled(label.isEnabled());
			setDisabledIcon(label.getDisabledIcon());
			setIcon(label.getIcon());
			setComponentOrientation(label.getComponentOrientation());
		}
		return this;
	}


	/**
	 * Overridden to update our delegate's UI.
	 */
	public void updateUI() {
		super.updateUI();
		if (delegate instanceof JComponent) {
			((JComponent)delegate).updateUI();
		}
	}


}