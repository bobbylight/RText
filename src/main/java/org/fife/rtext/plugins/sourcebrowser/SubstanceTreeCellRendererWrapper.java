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

import org.fife.ui.autocomplete.Util;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTreeCellRenderer;


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
	@Override
	public Font getFont() {
		return delegate!=null ? ((JLabel)delegate).getFont() : super.getFont();
	}


	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean focused) {

		// Some features such as selected & rollover foreground are needed
		super.getTreeCellRendererComponent(tree, value,
				selected, expanded, leaf, row, focused);

		Component c = delegate.getTreeCellRendererComponent(tree, value,
				selected, expanded, leaf, row, focused);
		if (c instanceof JLabel) {
			JLabel label = (JLabel)c;
			setFont(label.getFont());
			String text = label.getText();
			// NOTE: As of 7.2.1, Insubstantial's SubstanceDefaultTreeCellRenderers
			// do not play nicely with tree nodes displaying HTML.  On rollover,
			// colors get wonky and inconsistent.  So for now, we have to simply
			// strip all HTML when using Substance.  
			// Our renderers already strip HTML when they are selected.
//			if (substanceAndRollover(tree, row)) {
				text = Util.stripHtml(text);
//			}
//			else {//if (text.startsWith("<html>")) {
////				System.out.println("--- " + UIManager.getColor("Tree.textForeground").toString().replace("javax.swing.plaf.ColorUIResource", "") + ": " + text);
//				// Convert ColorUIResource to Color
//				Color c2 = ((DefaultTreeCellRenderer)delegate).getTextNonSelectionColor();
//				c2 = new Color(c2.getRGB());
//				System.out.println("--- " + c2);
//				setForeground(c2);
//			}
//if (text.endsWith("int")) {
//	printComponentState(text, tree, row);
//}
//System.out.println(getForeground() + ": " + text);
			setText(text);
			setEnabled(label.isEnabled());
			setDisabledIcon(label.getDisabledIcon());
			setIcon(label.getIcon());
			setComponentOrientation(label.getComponentOrientation());
		}
		return this;

	}

//private void printComponentState(String text, JTree tree, int row) {
//	// Already know we're Substance if this renderer is installed...
//	SubstanceTreeUI ui = (SubstanceTreeUI)tree.getUI();
//	TreePathId pathId = new TreePathId(tree.getPathForRow(row));
//	System.out.println(ui.getPathState(pathId) + ": " + text);
//}
//	private boolean substanceAndRollover(JTree tree, int row) {
//		// Already know we're Substance if this renderer is installed...
//		SubstanceTreeUI ui = (SubstanceTreeUI)tree.getUI();
//		TreePathId pathId = new TreePathId(tree.getPathForRow(row));
//		ComponentState state = ui.getPathState(pathId);
//		return state==ComponentState.ROLLOVER_ARMED ||
//				state==ComponentState.ROLLOVER_SELECTED ||
//				state==ComponentState.ROLLOVER_UNSELECTED;
//	}


	/**
	 * Overridden to update our delegate's UI.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		if (delegate instanceof JComponent) {
			((JComponent)delegate).updateUI();
		}
	}


}