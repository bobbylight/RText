/*
 * 08/28/2012
 *
 * FileProjectEntry.java - A file in a project.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
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


	public int compareTo(Object o) {
		if (o instanceof FileProjectEntry) {
			return file.compareTo(((FileProjectEntry)o).getFile());
		}
		return -1;
	}


	public boolean equals(Object o) {
		if (o==this) {
			return true;
		}
		return compareTo(o)==0;
	}


	public File getFile() {
		return file;
	}


	public String getType() {
		return FILE_PROJECT_ENTRY;
	}


	public int hashCode() {
		return file.hashCode();
	}


}