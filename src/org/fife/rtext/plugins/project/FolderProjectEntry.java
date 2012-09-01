package org.fife.rtext.plugins.project;

import java.io.File;


/**
 * A project entry representing a folder on the local file system
 * and its contents, possibly filtered.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FolderProjectEntry implements ProjectEntry {

	private File dir;


	public FolderProjectEntry(File folder) {
		this.dir = folder;
	}


	public File getFile() {
		return dir;
	}


}