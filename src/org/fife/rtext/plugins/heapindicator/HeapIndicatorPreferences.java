/*
 * 09/20/2005
 *
 * HeapIndicatorPreferences.java - Preferences for the Heap Indicator.
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
package org.fife.rtext.plugins.heapindicator;

import java.awt.Color;
import java.util.prefs.Preferences;

import org.fife.ui.app.Plugin;
import org.fife.ui.app.PluginPreferences;


/**
 * Preferences for the heap indicator.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class HeapIndicatorPreferences extends PluginPreferences {

	public boolean visible;
	public int     refreshInterval;
	public boolean useSystemColors;
	public Color   iconForeground;
	public Color   iconBorderColor;

	private static final boolean	DEFAULT_VISIBLE			= false;
	private static final int		DEFAULT_REFRESH_INTERVAL		= 5000;
	private static final boolean  DEFAULT_USE_SYSTEM_COLORS	= true;
	private static final Color	DEFAULT_ICON_FOREGROUND		= Color.BLUE;
	private static final Color	DEFAULT_ICON_BORDER_COLOR	= Color.BLACK;

	private static final String	VISIBLE			= "visible";
	private static final String	REFRESH			= "refresh";
	private static final String	SYSTEM_COLORS		= "useSystemColors";
	private static final String	ICON_FOREGROUND	= "iconForeground";
	private static final String	ICON_BORDER_COLOR	= "iconBorderColor";


	/**
	 * Constructor.
	 */
	private HeapIndicatorPreferences() {
		setDefaults();
	}


	/**
	 * Creates a properties object with all fields initialized to the values
	 * that the specified plugin is currently running with.
	 *
	 * @param plugin The plugin from which to generate preferences.
	 * @return An <code>Preferences</code> object initialized to contain
	 *         the properties the specified plugin is running with.
	 */
	public static PluginPreferences generatePreferences(Plugin plugin) {
		HeapIndicatorPreferences prefs = new HeapIndicatorPreferences();
		HeapIndicatorPlugin hiPlugin = (HeapIndicatorPlugin)plugin;
		prefs.visible         = hiPlugin.isVisible();
		prefs.refreshInterval = hiPlugin.getRefreshInterval();
		prefs.useSystemColors = hiPlugin.getUseSystemColors();
		prefs.iconForeground  = hiPlugin.getIconForeground();
		prefs.iconBorderColor = hiPlugin.getIconBorderColor();
		return prefs;
	}


	/**
	 * Initializes this preferences instance with data saved previously via
	 * the Java Preferences API.  If the load fails, the returned preferences
	 * instance will be populated with default values.
	 *
	 * @return If the load went okay, the preferences for the given plugin
	 *         are returned.  If something went wrong, or the user has never
	 *         used this plugin before (and thus there are no saved
	 *         preferences), default values are returned.
	 */
	public static PluginPreferences load() {
		HeapIndicatorPreferences hip = new HeapIndicatorPreferences();
		try {
			Preferences prefs = Preferences.userNodeForPackage(
										HeapIndicatorPlugin.class);
			hip.visible         = prefs.getBoolean(VISIBLE, hip.visible);
			hip.refreshInterval = prefs.getInt(REFRESH, hip.refreshInterval);
			hip.useSystemColors = prefs.getBoolean(SYSTEM_COLORS,
								hip.useSystemColors);
			hip.iconForeground  = new Color(
								prefs.getInt(ICON_FOREGROUND,
								hip.iconForeground.getRGB()));
			hip.iconBorderColor	= new Color(
								prefs.getInt(ICON_BORDER_COLOR,
								hip.iconBorderColor.getRGB()));
		} catch (Exception e) {
			e.printStackTrace();
			hip.setDefaults();
		}
		return hip;
	}



	/**
	 * Saves this preferences instance via the Java Preferences API.
	 */
	public void save() {
		Preferences prefs = Preferences.userNodeForPackage(
										HeapIndicatorPlugin.class);
		prefs.putBoolean(VISIBLE,       visible);
		prefs.putInt(REFRESH,           refreshInterval);
		prefs.putBoolean(SYSTEM_COLORS, useSystemColors);
		prefs.putInt(ICON_FOREGROUND,   iconForeground.getRGB());
		prefs.putInt(ICON_BORDER_COLOR, iconBorderColor.getRGB());
	}


	/**
	 * Sets this preferences instance to contain all default values.
	 */
	protected void setDefaults() {
		visible         = DEFAULT_VISIBLE;
		refreshInterval = DEFAULT_REFRESH_INTERVAL;
		useSystemColors = DEFAULT_USE_SYSTEM_COLORS;
		iconForeground  = DEFAULT_ICON_FOREGROUND;
		iconBorderColor = DEFAULT_ICON_BORDER_COLOR;
	}


}