/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;


/**
 * Program entry point.
 */
public final class Main {


	/**
	 * Private constructor to prevent instantiation.
	 */
	private Main() {
		// Do nothing - comment for Sonar
	}


	/**
	 * Program entry point.
	 *
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		RTextAppContext context = new RTextAppContext();
		context.startApplication(args);
	}
}
