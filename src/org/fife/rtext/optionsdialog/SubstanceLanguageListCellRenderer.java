/*
 * 01/15/2013
 *
 * SubstanceLanguageListCellRenderer - Cell renderer for the Language option
 * panel when Substance is installed.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.optionsdialog;

import java.awt.Component;
import javax.swing.JList;

import org.fife.rtext.optionsdialog.LanguageOptionPanel.IconTextInfo;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultListCellRenderer;


/**
 * Cell renderer for the Language option panel when Substance is installed.
 * I love how Substance makes custom renderers a pain in the ass!
 * 
 * @author Robert Futrell
 * @version 1.0
 * @see LanguageListCellRenderer
 */
class SubstanceLanguageListCellRenderer
		extends SubstanceDefaultListCellRenderer {


	@Override
	public Component getListCellRendererComponent(JList list,
			Object value, int index, boolean selected, boolean focused) {
		super.getListCellRendererComponent(list, value, index,
									selected, focused);
		IconTextInfo iti = (IconTextInfo)value;
		setIcon(iti.getIcon());
		setText(iti.getText());
		return this;
	}


}