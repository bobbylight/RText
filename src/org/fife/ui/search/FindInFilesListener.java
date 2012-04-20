/*
 * 08/11/2004
 *
 * FindInFilesListener.java - Listens for events from a FindInFilesDialog.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.search;

import java.util.EventListener;


/**
 * An interface for objects that wish to be notified of files being selected
 * in a <code>FindInFilesDialog.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public interface FindInFilesListener extends EventListener {


	/**
	 * Called when the user selects a file in a listened-to find-in-files dialog.
	 */
	public void findInFilesFileSelected(FindInFilesEvent e);


}