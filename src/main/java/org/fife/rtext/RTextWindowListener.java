/*
 * 11/14/2003
 *
 * RTextWindowListener.java - Listens for RText instances to close, so it
 * knows when to terminate the JVM.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * A window listener that listens for rtext instances to close, so it
 * knows when to terminate the JVM.  All it does is keep track of how
 * many rtext windows are open, and terminates the JVM when that number
 * reaches zero.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class RTextWindowListener extends WindowAdapter {

	private RText owner;


	/**
	 * Creates a new <code>RTextWindowListener</code>.
	 *
	 * @param owner The first rtext window to register under this listener.
	 */
	public RTextWindowListener(RText owner) {
		this.owner = owner;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void windowDeactivated(WindowEvent e) {
		// Make sure the selection is always visible.
		RTextEditorPane editor = owner.getMainView().getCurrentTextArea();
		if (editor!=null) {
			editor.getCaret().setSelectionVisible(true);
		}
	}


	/**
	 * Overridden to help minimize a Swing issue on Windows.  If the main
	 * application window is minimized for a long time (e.g. overnight), the
	 * system seems to cache the entire app to disk, resulting in a very long
	 * pause when the user brings the app back up.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void windowIconified(WindowEvent e) {
		System.gc();
	}


}