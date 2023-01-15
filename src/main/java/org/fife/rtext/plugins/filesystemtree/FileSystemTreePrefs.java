/*
 * 06/13/2005
 *
 * FileSystemTreePreferences.java - Preferences for the FileSystemTree plugin.
 * Copyright (C) 2005 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.filesystemtree;

import javax.swing.KeyStroke;

import org.fife.ui.app.prefs.Prefs;
import org.fife.ui.dockablewindows.DockableWindowConstants;


/**
 * Preferences for the file system tree plugin.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class FileSystemTreePrefs extends Prefs {


	/**
	 * Whether the GUI plugin window is active (visible).
	 */
	public boolean active;

	/**
	 * The GUI plugin window 's position.
	 */
	public int position;

	/**
	 * Key stroke that toggles the task window's visibility.
	 */
	public KeyStroke windowVisibilityAccelerator;


	@Override
	public void setDefaults() {
		active = true;
		position = DockableWindowConstants.LEFT;
		windowVisibilityAccelerator = null;
	}


}
