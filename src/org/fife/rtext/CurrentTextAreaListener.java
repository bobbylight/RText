/*
 * 11/14/2003
 *
 * CurrentTextAreaListener.java - Listens for the current text area changing
 * in RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.util.EventListener;


/**
 * A listener interested in knowing about changes to the current text area.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public interface CurrentTextAreaListener extends EventListener {


	/**
	 * Called when a property of the current text area (or the current text
	 * area itself) is modified.
	 *
	 * @param e The event.
	 */
	public void currentTextAreaPropertyChanged(CurrentTextAreaEvent e);


}