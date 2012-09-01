package org.fife.rtext.plugins.project;

import java.io.File;


/**
 * A project entry reflecting a file on the local file system.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FileProjectEntry implements ProjectEntry {

	private File file;


	public FileProjectEntry(String file) {
		this(new File(file));
	}


	public FileProjectEntry(File file) {
		this.file = file;
	}


	public File getFile() {
		return file;
	}


}