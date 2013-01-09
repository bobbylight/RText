/*
 * 09/17/2010
 *
 * AWTExceptionHandler.java - Catches uncaught throwables in the EDT.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
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

	private static Logger logger;
	private FileHandler fileHandler;


	public AWTExceptionHandler() {
		// This code is rubbish, but we don't instantiate directly.  We should
		// never actually have this constructor called twice, but just to make
		// sure...
		if (logger!=null) {
			logger = Logger.getLogger("org.fife.rtext");
			try {
				fileHandler = new FileHandler(
						"%h/uncaughtRTextAwtExceptions.log", true);
				logger.addHandler(fileHandler);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
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
			if (logger!=null) {
				logger.log(Level.SEVERE, "Uncaught exception in EDT", t);
			}
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


	/**
	 * Cleans up this exception handler.  This should be called when the
	 * application shuts down.
	 */
	public static void shutdown() {
		if (logger!=null) {
			// Manually close FileHandlers to remove .lck files.
			Handler[] handlers = logger.getHandlers();
			for (int i=0; i<handlers.length; i++) {
				handlers[i].close();
			}
			logger = null;
		}
	}


}