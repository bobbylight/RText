/*
 * 09/08/2012
 *
 * BaseAction.java - A base class for tree node actions.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;


/**
 * A base class for tree node actions.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class BaseAction extends AbstractAction implements PopupContent{


	protected BaseAction(String keyRoot) {
		this(keyRoot, null);
	}


	protected BaseAction(String keyRoot, String image) {
		putValue(NAME, Messages.getString(keyRoot));
		int mnemonic = Messages.getMnemonic(keyRoot + ".Mnemonic");
		putValue(MNEMONIC_KEY, new Integer(mnemonic));
		if (image!=null) {
			URL url = getClass().getResource(image);
			putValue(SMALL_ICON, new ImageIcon(url));
		}
	}


}