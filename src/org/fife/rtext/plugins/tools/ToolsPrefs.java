/*
 * 02/26/2010
 *
 * ToolsPrefs.java - Preferences for the tools plugin.
 * Copyright (C) 2010 Robert Futrell
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
package org.fife.rtext.plugins.tools;

import java.io.IOException;
import java.io.InputStream;
import javax.swing.KeyStroke;

import org.fife.ui.app.Prefs;
import org.fife.ui.dockablewindows.DockableWindow;


/**
 * Preferences for the tools plugin.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class ToolsPrefs extends Prefs {

	/**
	 * Whether the GUI plugin window is active (visible).
	 */
	public boolean windowVisible;

	/**
	 * The location of the dockable tool output window.
	 */
	public int windowPosition;

	/**
	 * Key stroke that toggles the task window's visibility.
	 */
	public KeyStroke windowVisibilityAccelerator;

	/**
	 * Accelerator for the "New Tool..." action.
	 */
	public KeyStroke newToolAccelerator;

	/**
	 * Accelerator for the "Edit Tools..." action.
	 */
	public KeyStroke editToolsAccelerator;


	/**
	 * Overridden to validate the task identifiers value.
	 */
	public void load(InputStream in) throws IOException {
		super.load(in);
		// Ensure window position is valid.
		if (!DockableWindow.isValidPosition(windowPosition)) {
			windowPosition = DockableWindow.BOTTOM;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void setDefaults() {
		windowVisible = false;
		windowPosition = DockableWindow.BOTTOM;
		windowVisibilityAccelerator = null;
		newToolAccelerator = null;
		editToolsAccelerator = null;
	}


}