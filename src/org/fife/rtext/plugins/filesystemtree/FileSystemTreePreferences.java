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

import java.util.prefs.Preferences;

import org.fife.ui.app.GUIPluginPreferences;
import org.fife.ui.app.Plugin;
import org.fife.ui.app.PluginPreferences;


/**
 * Preferences for the file system tree plugin.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class FileSystemTreePreferences extends GUIPluginPreferences {


	/**
	 * Constructor.
	 */
	private FileSystemTreePreferences() {
		setDefaults();
	}


	/**
	 * Creates a properties object with all fields initialized to the values
	 * that the specified plugin is currently running with.
	 *
	 * @param plugin The plugin from which to generate preferences.
	 * @return An <code>RPrerences</code> object initialized to contain
	 *         the properties the specified plugin is running with.
	 */
	public static PluginPreferences generatePreferences(Plugin plugin) {
		FileSystemTreePreferences prefs = new FileSystemTreePreferences();
		FileSystemTreePlugin fstPlugin = (FileSystemTreePlugin)plugin;
		prefs.active = fstPlugin.isActive();
		prefs.position = fstPlugin.getPosition();
		return prefs;
	}


	/**
	 * Initializes this preferences instance with data saved previously via
	 * the Java Preferences API.  If the load fails, the returned preferences
	 * instance will be populated with default values.<p>
	 *
	 * @return If the load went okay, the preferences for the given plugin
	 *         are returned.  If something went wrong, or the user has never
	 *         used this plugin before (and thus there are no saved
	 *         preferences), default values are returned.
	 */
	public static PluginPreferences load() {
		FileSystemTreePreferences fstp = new FileSystemTreePreferences();
		try {
			Preferences prefs = Preferences.userNodeForPackage(
										FileSystemTreePlugin.class);
			GUIPluginPreferences.loadCommonPreferences(fstp, prefs);
		} catch (Exception e) {
			e.printStackTrace();
			fstp.setDefaults();
		}
		return fstp;
	}



	/**
	 * Saves this preferences instance via the Java Preferences API.
	 */
	public void save() {
		Preferences prefs = Preferences.userNodeForPackage(
										FileSystemTreePlugin.class);
		saveCommonPreferences(prefs);
	}


	/**
	 * Sets this preferences instance to contain all default values.
	 */
	protected void setDefaults() {
		super.setDefaults(); // active and position.
	}


}