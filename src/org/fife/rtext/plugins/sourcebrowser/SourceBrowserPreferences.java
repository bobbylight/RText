/*
 * 05/03/2005
 *
 * SourceBrowserPreference.sjava- Preferences for the source browser.
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
package org.fife.rtext.plugins.sourcebrowser;

import java.util.prefs.Preferences;

import org.fife.ui.app.GUIPluginPreferences;
import org.fife.ui.app.Plugin;
import org.fife.ui.app.PluginPreferences;


/**
 * Preferences for the source browser.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class SourceBrowserPreferences extends GUIPluginPreferences {

	/**
	 * The filename of the Exuberant CTags executable.
	 */
	public String ctagsExecutable;
	public String ctagsType;
	public boolean useHTMLToolTips;

	private static final String CTAGS_LOCATION	= "CTagsLocation";
	private static final String CTAGS_TYPE		= "CTagsType";
	private static final String HTML_TOOLTIPS	= "HTMLToolTips";


	/**
	 * Constructor.
	 */
	private SourceBrowserPreferences() {
		setDefaults();
	}


	/**
	 * Creates a properties object with all fields initialized to the values
	 * that the specified plugin is currently running with.
	 *
	 * @param plugin The plugin from which to generate preferences.
	 * @return An <code>RPreferences</code> object initialized to contain
	 *         the properties the specified plugin is running with.
	 */
	public static PluginPreferences generatePreferences(Plugin plugin) {
		SourceBrowserPreferences prefs = new SourceBrowserPreferences();
		SourceBrowserPlugin sbPlugin = (SourceBrowserPlugin)plugin;
		prefs.active = sbPlugin.isActive();
		prefs.position = sbPlugin.getPosition();
		prefs.ctagsExecutable = sbPlugin.getCTagsExecutableLocation();
		prefs.ctagsType = sbPlugin.getCTagsType();
		prefs.useHTMLToolTips = sbPlugin.getUseHTMLToolTips();
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
		SourceBrowserPreferences sbp = new SourceBrowserPreferences();
		try {
			Preferences prefs = Preferences.userNodeForPackage(
										SourceBrowserPlugin.class);
			GUIPluginPreferences.loadCommonPreferences(sbp, prefs);
			sbp.ctagsExecutable = prefs.get(CTAGS_LOCATION, sbp.ctagsExecutable);
			sbp.ctagsType = prefs.get(CTAGS_TYPE, sbp.ctagsType);
			sbp.useHTMLToolTips = prefs.getBoolean(HTML_TOOLTIPS, sbp.useHTMLToolTips);
		} catch (Exception e) {
			e.printStackTrace();
			sbp.setDefaults();
		}
		return sbp;
	}



	/**
	 * Saves this preferences instance via the Java Preferences API.
	 */
	public void save() {
		Preferences prefs = Preferences.userNodeForPackage(
										SourceBrowserPlugin.class);
		saveCommonPreferences(prefs);
		prefs.put(CTAGS_LOCATION, ctagsExecutable);
		prefs.put(CTAGS_TYPE, ctagsType);
		prefs.putBoolean(HTML_TOOLTIPS, useHTMLToolTips);
	}


	/**
	 * Sets this preferences instance to contain all default values.
	 */
	protected void setDefaults() {
		super.setDefaults(); // active and position.
		ctagsExecutable = "/usr/contrib/bin/ctags";
		ctagsType = SourceBrowserPlugin.CTAGS_TYPE_EXUBERANT;
		useHTMLToolTips = true;
	}


}