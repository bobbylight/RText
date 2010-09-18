/*
 * 09/17/2010
 *
 * AWTExceptionHandler.java - Catches uncaught throwables in the EDT.
 * Copyright (C) 2010 Robert Futrell
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

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Catches uncaught exceptions thrown in the EDT.  This is "magic" that works
 * in at least 1.4 - 1.6 in Sun JVMs.  We cannot simply use
 * <code>Thread.UncaughtExceptionHandler</code>, since it didn't exist in
 * 1.4 (and it doesn't work for exceptions thrown while modal dialogs are
 * visible on the EDT anyway).<p>
 * 
 * To use this class, call
 * <code>AWTExceptionHandler.register()</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
// NOTE: This class must be public and have a public, no-args default
// constructor for the EDT to use it.
public class AWTExceptionHandler {

	private Logger logger;
	private FileHandler fileHandler;


	public AWTExceptionHandler() {
		logger = Logger.getLogger("org.fife.rtext");
		try {
			fileHandler = new FileHandler(
					"%h/uncaughtRTextAwtExceptions.log", true);
			logger.addHandler(fileHandler);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}


	/**
	 * Callback for whenever an uncaught Throwable is thrown on the EDT.
	 *
	 * @param t the uncaught Throwable.
	 */
	public void handle(Throwable t) {
		try {
			t.printStackTrace();
			logger.log(Level.SEVERE, "Uncaught exception in EDT", t);
		} catch (Throwable t2) {
			// don't let the exception get thrown out, will cause infinite
			// looping!
		}
	}


	/**
	 * Call this method to register this exception handler with the EDT.
	 */
	public static void register() {
		System.setProperty("sun.awt.exception.handler",
								AWTExceptionHandler.class.getName());
	}


}