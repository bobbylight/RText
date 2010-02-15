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

import org.fife.ui.app.GUIPlugin;
import org.fife.ui.app.Prefs;


/**
 * Preferences for the source browser.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class SourceBrowserPrefs extends Prefs {

	/**
	 * Whether the GUI plugin window is active (visible).
	 */
	public boolean active;

	/**
	 * The GUI plugin window 's position.
	 */
	public int position;

	/**
	 * The filename of the Exuberant CTags executable.
	 */
	public String ctagsExecutable;

	/**
	 * The type of ctags.
	 */
	public String ctagsType;

	/**
	 * Whether HTML should be used to make tool tips look nicer.
	 */
	public boolean useHTMLToolTips;


	/**
	 * {@inheritDoc}
	 */
	public void setDefaults() {
		active = true;
		position = GUIPlugin.LEFT;
		ctagsExecutable = "/usr/contrib/bin/ctags";
		ctagsType = SourceBrowserPlugin.CTAGS_TYPE_EXUBERANT;
		useHTMLToolTips = true;
	}


}