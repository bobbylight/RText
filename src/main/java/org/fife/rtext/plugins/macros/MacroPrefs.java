/*
 * 07/23/2011
 *
 * MacroPrefs.java - Preferences for the macros plugin.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.macros;

import javax.swing.KeyStroke;

import org.fife.ui.app.Prefs;


/**
 * Preferences for the macros plugin.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class MacroPrefs extends Prefs {

	/**
	 * Accelerator for the "New Macro..." action.
	 */
	public KeyStroke newMacroAccelerator;

	/**
	 * Accelerator for the "Edit Macros..." action.
	 */
	public KeyStroke editMacrosAccelerator;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaults() {
		newMacroAccelerator = null;
		editMacrosAccelerator = null;
	}


}