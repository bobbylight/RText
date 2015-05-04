/*
 * 08/28/2012
 *
 * Messages.java - Localized messages for the project plugin.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 * Localized messages for the Project plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Messages {

	private static final String MSG = "org.fife.rtext.plugins.project.ProjectPlugin";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	/**
	 * Private constructor to prevent instantiation.
	 */
	private Messages() {}


	/**
	 * Returns the message bundle for this plugin.
	 *
	 * @return The message bundle.
	 */
	static ResourceBundle getBundle() {
		return msg;
	}


	/**
	 * Returns a localized message representing a mnemonic (a single character).
	 *
	 * @param key The key for the message.
	 * @return The localized mnemonic.
	 */
	public static int getMnemonic(String key) {
		return msg.getString(key).charAt(0);
	}


	/**
	 * Returns a localized message.
	 *
	 * @param key The key for the message.
	 * @param params Any arguments for the localized message string.
	 * @return The localized message.
	 */
	public static String getString(String key, String... params) {
		String temp = msg.getString(key);
		return MessageFormat.format(temp, (Object[])params);
	}


}