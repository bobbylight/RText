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

import java.util.ArrayList;
import java.util.List;

import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;


/**
 * Keeps track of all <code>org.fife.rtext.RText</code> instances (windows) that
 * are open, and provides an interface to update their Look and Feels together.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class StoreKeeper {

	private static List<RText> rtextInstances;


	/**
	 * Notes that a new <code>RText</code> window is open.
	 *
	 * @param rtext The <code>RText</code> instance to remember.
	 */
	public static void addRTextInstance(RText rtext) {
		if (rtextInstances==null)
			rtextInstances = new ArrayList<RText>(1);
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
			//throw new RTextNotFoundException();
			return;
		rtextInstances.remove(index);
	}


	/**
	 * Updates the Look and Feel of all <code>RText</code> instances.
	 *
	 * @param lnf The Look and Feel to change to.
	 */
	public static void updateLookAndFeels(final LookAndFeel lnf) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int count = getInstanceCount();
				for (int i=0; i<count; i++) {
					rtextInstances.get(i).updateLookAndFeel(lnf);
				}
			}
		});
	}


}