/*
 * 12/19/2014
 *
 * RecentFileManager.java - Keeps a list of the files opened in RText.
 * Copyright (C) 2014 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.fife.ui.rsyntaxtextarea.FileLocation;


/**
 * Listens for files being opened in RText, so anyone interested can easily
 * get this list.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RecentFileManager implements PropertyChangeListener {

	private RText rtext;
	private List<FileLocation> files;

	/**
	 * The number of files we remember.
	 */
	private static final int MAX_FILE_COUNT = 75;


	/**
	 * Constructor.
	 *
	 * @param rtext The parent application.
	 */
	public RecentFileManager(RText rtext) {

		this.rtext = rtext;
		files = new ArrayList<FileLocation>();

		rtext.getMainView().addPropertyChangeListener(
				AbstractMainView.TEXT_AREA_ADDED_PROPERTY, this);

		List<String> history = ((RTextMenuBar)rtext.getJMenuBar()).
				getFileHistory();
		for (int i=history.size() - 1; i>=0; i--) {
			addFile(history.get(i));
		}

	}


	/**
	 * Called when a property in the application we are interested in
	 * changes.
	 *
	 * @param e The event.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String prop = e.getPropertyName();

		if (AbstractMainView.TEXT_AREA_ADDED_PROPERTY.equals(prop)) {

			RTextEditorPane textArea = (RTextEditorPane)e.getNewValue();
			if (textArea != null) {
				String fullPath = textArea.getFileFullPath();
				// We don't remember just-created empty text files.
				// Also, due to the Preferences API needing a non-null key for
				// all values, a "-" filename means no files were found for the
				// file history.  So, we won't add this file in either.
				if (fullPath.endsWith(File.separatorChar + rtext.getNewFileName())
						|| fullPath.equals("-")) {
					return;
				}
				addFile(fullPath);
			}
		}

	}


	/**
	 * Adds a file to the list of recent files.
	 *
	 * @param file The file to add.
	 */
	private void addFile(String file) {

		if (file == null) {
			return;
		}

		// If we already are remembering this file, move it to the "top."
		int index = indexOf(file);
		if (index > -1) {
			FileLocation loc = files.remove(index);
			files.add(0, loc);
			return;
		}

		// Add our new file to the "top" of remembered files.
		// TODO: Simplify when RSyntaxTextArea bug #94 is fixed
		FileLocation loc = null;
		try {
			loc = FileLocation.create(file);
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace(); // Malformed URL, shouldn't happen.
			return;
		}

		if (loc.isLocal() && !loc.isLocalAndExists()) {
			// When adding from saved history, some files may no longer
			// exist
			return;
		}
		files.add(0, loc);

		// Too many files?  Oust the file in history added least recently.
		if (files.size() > MAX_FILE_COUNT) {
			files.remove(files.size() - 1);
		}

	}


	/**
	 * Returns the current index of the specified file in this history.
	 *
	 * @param file The file to look for.
	 * @return The index of the file, or <code>-1</code> if it is not
	 *         currently in the list.
	 */
	private int indexOf(String file) {
		for (int i=0; i<files.size(); i++) {
			FileLocation loc = files.get(i);
			if (file.equals(loc.getFileFullPath())) {
				return i;
			}
		}
		return -1;
	}


	/**
	 * Returns the list of recent files.
	 *
	 * @return The list of recent files.
	 */
	public List<FileLocation> getRecentFiles() {
		return files;
	}


}