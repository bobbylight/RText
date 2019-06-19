/*
 * 02/24/2013
 *
 * PluginPrefs - General preferences for the Tidy plugin.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tidy;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.fife.ui.app.Prefs;


/**
 * General preferences for the Tidy plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class PluginPrefs extends Prefs {

	/**
	 * Accelerator key for the "pretty print source code" action.
	 */
	public KeyStroke tidyActionAccelerator;


	@Override
	public void setDefaults() {
		int defaultMod = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		int defShift = defaultMod | InputEvent.SHIFT_DOWN_MASK;
		tidyActionAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_P, defShift);
	}


}
