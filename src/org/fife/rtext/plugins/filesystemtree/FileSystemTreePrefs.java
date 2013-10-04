/*
 * 06/13/2005
 *
 * FileSystemTreePreferences.java - Preferences for the FileSystemTree plugin.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.filesystemtree;

import javax.swing.KeyStroke;

import org.fife.ui.app.GUIPlugin;
import org.fife.ui.app.Prefs;


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


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaults() {
		active = true;
		position = GUIPlugin.LEFT;
		windowVisibilityAccelerator = null;
	}


}