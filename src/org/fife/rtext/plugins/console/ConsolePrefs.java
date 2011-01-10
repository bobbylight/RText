/*
 * 01/04/2011
 *
 * ConsolePrefs.java - Preferences for the console plugin.
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
package org.fife.rtext.plugins.console;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.KeyStroke;

import org.fife.ui.app.Prefs;
import org.fife.ui.dockablewindows.DockableWindow;


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
	 * The color used for stdout in consoles.
	 */
	public Color stdoutFG;

	/**
	 * The color used for stderr in consoles.
	 */
	public Color stderrFG;

	/**
	 * The color used for exceptions in consoles.
	 */
	public Color exceptionFG;

	/**
	 * The color used for prompts in consoles.
	 */
	public Color promptFG;


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
		stdoutFG = ConsoleTextArea.DEFAULT_STDOUT_FG;
		stderrFG = ConsoleTextArea.DEFAULT_STDERR_FG;
		exceptionFG = ConsoleTextArea.DEFAULT_EXCEPTION_FG;
		promptFG = ConsoleTextArea.DEFAULT_PROMPT_FG;
	}


}