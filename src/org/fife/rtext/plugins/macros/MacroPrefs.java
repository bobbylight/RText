/*
 * 07/23/2011
 *
 * MacroPrefs.java - Preferences for the macros plugin.
 * Copyright (C) 2011 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This program is a part of RText.
 *
 * RText program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
	public void setDefaults() {
		newMacroAccelerator = null;
		editMacrosAccelerator = null;
	}


}