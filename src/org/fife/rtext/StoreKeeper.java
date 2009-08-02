/*
 * 11/14/2003
 *
 * StoreKeeper.java - Keeps track of all RText instances (windows) that are
 * open, and provides an interface to update their Look and Feels together.
 * Copyright (C) 2003 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This file is a part of RText.
 *
 * RText is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.rtext;

import java.util.ArrayList;
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

	private static ArrayList rtextInstances;


	/**
	 * Notes that a new <code>RText</code> window is open.
	 *
	 * @param rtext The <code>RText</code> instance to remember.
	 */
	public static void addRTextInstance(RText rtext) {
		if (rtextInstances==null)
			rtextInstances = new ArrayList(1);
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

		final Runnable updateUIRunnable = new Runnable() {
			public void run() {
				int count = getInstanceCount();
				for (int i=0; i<count; i++)
					((RText)rtextInstances.get(i)).updateLookAndFeel(lnf);
			}
		};

		// Ensure we update Look and Feels on event dispatch thread.
		SwingUtilities.invokeLater(updateUIRunnable);

	}


}