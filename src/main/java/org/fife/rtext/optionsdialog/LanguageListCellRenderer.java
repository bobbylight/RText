/*
 * 01/15/2013
 *
 * LanguageListCellRenderer - Cell renderer for the Language option panel.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.optionsdialog;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.fife.rtext.optionsdialog.LanguageOptionPanel.IconTextInfo;
import org.fife.ui.SubstanceUtils;
import org.fife.ui.WebLookAndFeelUtils;


/**
 * Cell renderer that knows how to display both an icon and text (as
 * <code>DefaultListCellRenderer</code> only knows how to display either/or).
 *
 * @author Robert Futrell
 * @version 1.0
 * @see SubstanceLanguageListCellRenderer
 */
class LanguageListCellRenderer extends DefaultListCellRenderer {

	/**
	 * For certain LAFs, we delegate to their custom renderer for simplicity.
	 */
	private JLabel possibleDelegate;

	private static final String SUBSTANCE_RENDERER_CLASS =
			"org.fife.rtext.optionsdialog.SubstanceLanguageListCellRenderer";


	/**
	 * Private constructor to prevent direct instantiation.
	 */
	private LanguageListCellRenderer(JLabel delegate) {
		this.possibleDelegate = delegate;
	}


	/**
	 * Creates the cell renderer to use for the Language option panel.  Note
	 * that this may not be an instance of this class (or a subclass), as some
	 * Look and Feels require inheritance to look good (e.g. Substance).
	 *
	 * @return The renderer to use.
	 */
	public static ListCellRenderer create() {

		JLabel delegate = null;

		if (SubstanceUtils.isSubstanceInstalled()) {
			// Can't delegate with Substance, because it unfortunately
			// has hard dependencies on renderer types.  Yuck.
			try {
				// Use reflection to avoid hard dependency on Substance.
				Class<?> clazz = Class.forName(SUBSTANCE_RENDERER_CLASS);
				return (ListCellRenderer)clazz.newInstance();
			} catch (Exception e) { // Never happens
				e.printStackTrace();
			}
		}
		else if (WebLookAndFeelUtils.isWebLookAndFeelInstalled()) {
			delegate = (JLabel)UIManager.get("List.cellRenderer");
		}

		return new LanguageListCellRenderer(delegate);

	}


	@Override
	public Component getListCellRendererComponent(JList list,
			Object value, int index, boolean selected, boolean focused) {

		JLabel renderer = null;
		if (possibleDelegate!=null) {
			renderer = possibleDelegate;
			((ListCellRenderer)possibleDelegate).getListCellRendererComponent(
					list, value, index, selected, focused);
		}
		else {
			renderer = this;
			super.getListCellRendererComponent(list, value, index, selected,
					focused);
		}

		IconTextInfo iti = (IconTextInfo)value;
		renderer.setIcon(iti.getIcon());
		renderer.setText(iti.getText());
		return renderer;

	}


}