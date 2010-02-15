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
import org.fife.ui.app.Prefs;


/**
 * Preferences for the heap indicator.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class HeapIndicatorPrefs extends Prefs {

	public boolean visible;
	public int     refreshInterval;
	public boolean useSystemColors;
	public Color   iconForeground;
	public Color   iconBorderColor;

	private static final boolean	DEFAULT_VISIBLE			= true;
	private static final int		DEFAULT_REFRESH_INTERVAL		= 5000;
	private static final boolean  DEFAULT_USE_SYSTEM_COLORS	= true;
	private static final Color	DEFAULT_ICON_FOREGROUND		= Color.BLUE;
	private static final Color	DEFAULT_ICON_BORDER_COLOR	= Color.BLACK;

	/**
	 * {@inheritDoc}
	 */
	public void setDefaults() {
		visible         = DEFAULT_VISIBLE;
		refreshInterval = DEFAULT_REFRESH_INTERVAL;
		useSystemColors = DEFAULT_USE_SYSTEM_COLORS;
		iconForeground  = DEFAULT_ICON_FOREGROUND;
		iconBorderColor = DEFAULT_ICON_BORDER_COLOR;
	}


}