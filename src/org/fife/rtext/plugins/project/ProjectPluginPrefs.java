/*
 * 09/01/2012
 *
 * ProjectPluginPrefs.java - Preferences for the projects plugin.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.io.IOException;
import java.io.InputStream;
import javax.swing.KeyStroke;

import org.fife.ui.app.Prefs;
import org.fife.ui.dockablewindows.DockableWindow;


/**
 * Preferences for the Projects plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ProjectPluginPrefs extends Prefs {

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
	 * The name of the most recently opened workspace.
	 */
	public String openWorkspaceName;

	/**
	 * Whether the root node of the workspace tree view is visible.
	 */
	public boolean treeRootVisible;


	/**
	 * Overridden to validate the task identifiers value.
	 */
	@Override
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
	@Override
	public void setDefaults() {
		windowVisible = true;
		windowPosition = DockableWindow.LEFT;
		windowVisibilityAccelerator = null;
		openWorkspaceName = null;
		treeRootVisible = false;
	}


}