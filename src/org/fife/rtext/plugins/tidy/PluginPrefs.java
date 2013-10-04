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


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaults() {
		tidyActionAccelerator = null;
	}


}