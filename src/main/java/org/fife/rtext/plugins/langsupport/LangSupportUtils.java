/*
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import java.io.File;

import org.fife.rsta.ac.java.buildpath.DirLibraryInfo;
import org.fife.rsta.ac.java.buildpath.JarLibraryInfo;
import org.fife.rsta.ac.java.buildpath.Jdk9LibraryInfo;
import org.fife.rsta.ac.java.buildpath.LibraryInfo;

/**
 * Utility methods for this plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
final class LangSupportUtils {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private LangSupportUtils() {
		// Nothing to do
	}

	/**
	 * Returns a {@code File} representing either the JDK/JRE root, the
	 * class file, or the directory containing the classes to autocomplete,
	 * based on the concrete type of the library info provided.
	 *
	 * @param li The library info.
	 * @return The file.
	 */
	public static File getClassFileLocation(LibraryInfo li) {
		if (li instanceof JarLibraryInfo) {
			return ((JarLibraryInfo)li).getJarFile();
		}
		else if (li instanceof DirLibraryInfo) {
			return new File(((DirLibraryInfo)li).getLocationAsString());
		}
		else if (li instanceof Jdk9LibraryInfo) {
			return ((Jdk9LibraryInfo)li).getJreHome();
		}
		throw new IllegalArgumentException("Unknown LibraryInfo type: " + li.getClass().getName());
	}

}
