/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;


import org.fife.util.MacOSUtil;

import javax.swing.*;

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

		// Properties that must be set before amy AWT classes are loaded.
		// Note that some of these are also configured for our installable
		// package via jpackage, but are also set here for testing before
		// releases
		MacOSUtil.setApplicationName("RText");
		MacOSUtil.setApplicationAppearance(MacOSUtil.AppAppearance.SYSTEM);

		RTextAppContext context = new RTextAppContext();
		SwingUtilities.invokeLater(() -> context.createApplication(args).setVisible(true));
	}
}
