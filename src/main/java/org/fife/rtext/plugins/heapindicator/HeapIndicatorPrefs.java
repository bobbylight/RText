/*
 * 09/20/2005
 *
 * HeapIndicatorPreferences.java - Preferences for the Heap Indicator.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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
	@Override
	public void setDefaults() {
		visible         = DEFAULT_VISIBLE;
		refreshInterval = DEFAULT_REFRESH_INTERVAL;
		useSystemColors = DEFAULT_USE_SYSTEM_COLORS;
		iconForeground  = DEFAULT_ICON_FOREGROUND;
		iconBorderColor = DEFAULT_ICON_BORDER_COLOR;
	}


}