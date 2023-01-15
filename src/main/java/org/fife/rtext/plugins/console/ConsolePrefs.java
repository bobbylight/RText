/*
 * 01/04/2011
 *
 * ConsolePrefs.java - Preferences for the console plugin.
 * Copyright (C) 2011 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.console;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.KeyStroke;

import org.fife.ui.app.prefs.Prefs;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowConstants;


/**
 * Preferences for the Console plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ConsolePrefs extends Prefs {

	/**
	 * Whether the GUI plugin window is active (visible).
	 */
	public boolean windowVisible;

	/**
	 * The location of the dockable console output window.
	 */
	public int windowPosition;

	/**
	 * Key stroke that toggles the console window's visibility.
	 */
	public KeyStroke windowVisibilityAccelerator;


	/**
	 * Overridden to validate the dockable window position value.
	 */
	@Override
	public void load(InputStream in) throws IOException {
		super.load(in);
		// Ensure window position is valid.
		if (!DockableWindow.isValidPosition(windowPosition)) {
			windowPosition = DockableWindowConstants.BOTTOM;
		}
	}


	@Override
	public void setDefaults() {
		windowVisible = false;
		windowPosition = DockableWindowConstants.BOTTOM;
		windowVisibilityAccelerator = null;
	}


}
