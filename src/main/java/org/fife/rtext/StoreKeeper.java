/*
 * 11/14/2003
 *
 * StoreKeeper.java - Keeps track of all RText instances (windows) that are
 * open, and provides an interface to update their Look and Feels together.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import org.fife.ui.app.AppTheme;

import java.util.ArrayList;
import java.util.List;


/**
 * Keeps track of all <code>org.fife.rtext.RText</code> instances (windows) that
 * are open, and provides an interface to update their Look and Feels together.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public final class StoreKeeper {

	private static List<RText> rtextInstances;


	/**
	 * Private constructor to prevent instantiation.
	 */
	private StoreKeeper() {
		// Do nothing (comment for Sonar)
	}


	/**
	 * Notes that a new <code>RText</code> window is open.
	 *
	 * @param rtext The <code>RText</code> instance to remember.
	 */
	public static void addRTextInstance(RText rtext) {
		if (rtextInstances==null)
			rtextInstances = new ArrayList<>(1);
		rtextInstances.add(rtext);
	}


	/**
	 * Gets the number of <code>RText</code> instances (windows) that are
	 * currently open.
	 *
	 * @return The number of <code>RText</code> instances open.
	 */
	public static int getInstanceCount() {
		return rtextInstances.size();
	}


	/**
	 * Removes an <code>RText</code> instance.
	 *
	 * @param rtext The <code>RText</code> to remove.
	 * @throws NullPointerException If no <code>RText</code> instances have
	 *         been remembered yet.
	 */
	public static void removeRTextInstance(RText rtext) {
		int index = rtextInstances.indexOf(rtext);
		if (index==-1)
			return;
		rtextInstances.remove(index);
	}


	/**
	 * Updates the application theme  <code>RText</code> instances.
	 * This should only be called on the EDT.
	 *
	 * @param theme The theme to change to.
	 */
	public static void updateAppThemes(AppTheme theme) {
		int count = getInstanceCount();
		for (int i=0; i<count; i++) {
			rtextInstances.get(i).setTheme(theme);
		}
	}


}
