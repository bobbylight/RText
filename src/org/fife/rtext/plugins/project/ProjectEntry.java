package org.fife.rtext.plugins.project;

import java.io.File;


/**
 * A project entry is either a file or a folder on the file system.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface ProjectEntry {


	/**
	 * Returns the file system resource this project entry represents.
	 *
	 * @return The file system resource.
	 */
	File getFile();


}