/*
 * 02/14/2010
 *
 * TasksPrefs.java - Preferences for the tasks plugin.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import javax.swing.KeyStroke;

import org.fife.ui.app.Prefs;
import org.fife.ui.dockablewindows.DockableWindow;


/**
 * Preferences for the tasks plugin.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class TasksPrefs extends Prefs {

	/**
	 * Whether the GUI plugin window is active (visible).
	 */
	public boolean windowVisible;

	/**
	 * The location of the dockable tasks window.
	 */
	public int windowPosition;

	/**
	 * Key stroke that toggles the task window's visibility.
	 */
	public KeyStroke windowVisibilityAccelerator;

	/**
	 * The task identifiers.
	 */
	public String taskIdentifiers;

	private static final Pattern TASK_IDENTIFIERS_PATTERN =
		Pattern.compile("^$|^[\\p{Alpha}\\?]+(?:\\|[\\p{Alpha}\\?]+)*$");

	static final String DEFAULT_TASK_IDS	= "FIXME|TODO|HACK";


	/**
	 * Overridden to validate the task identifiers value.
	 */
	@Override
	public void load(InputStream in) throws IOException {

		super.load(in);

		// Ensure task ID's is proper format - "letter+(|letter+)*"
		if (!TASK_IDENTIFIERS_PATTERN.matcher(taskIdentifiers).matches()) {
			System.out.println("Invalid task identifiers: " + taskIdentifiers);
			taskIdentifiers = DEFAULT_TASK_IDS;
		}

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
		windowPosition = DockableWindow.BOTTOM;
		windowVisibilityAccelerator = null;
		taskIdentifiers = DEFAULT_TASK_IDS;
	}


}