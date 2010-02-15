/*
 * 06/13/2005
 *
 * FileSystemTreePreferences.java - Preferences for the FileSystemTree plugin.
 * Copyright (C) 2005 Robert Futrell
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
package org.fife.rtext.plugins.filesystemtree;

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
	 * {@inheritDoc}
	 */
	public void setDefaults() {
		active = true;
		position = GUIPlugin.LEFT;
	}


}