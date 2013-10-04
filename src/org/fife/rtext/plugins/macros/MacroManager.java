/*
 * 07/30/2011
 *
 * MacroManager.java - Manages all macros.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.macros;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Manages all of the macros in an RText session.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class MacroManager {

	/**
	 * Event fired when a macro is added or removed.
	 */
	public static final String PROPERTY_MACROS			= "macros";

	private SortedSet<Macro> macros;
	private PropertyChangeSupport support;

	/**
	 * The extension all macro files in older versions of RText ended with.
	 */
	private static final String OLD_MACRO_FILE_EXTENSION	= ".macro";

	/**
	 * The singleton instance of this class.
	 */
	private static final MacroManager INSTANCE = new MacroManager();

	/**
	 * The name of the file that lists our macro definitions.
	 */
	private static final String MACRO_DEFINITION_FILE_NAME = "macroDefinitions.xml";


	/**
	 * Private constructor to prevent instantiation.
	 */
	private MacroManager() {
		macros = new TreeSet<Macro>();
		support = new PropertyChangeSupport(this);
	}


	/**
	 * Adds a macro.  This methods fires a property change event of type
	 * {@link #PROPERTY_MACROS}.
	 *
	 * @param macro The macro to add.
	 * @see #removeMacro(Macro)
	 */
	public void addMacro(Macro macro) {
		// Macros are considered equal if they have the same name.  Since the
		// user is allowed to "overwrite" an existing macro, we must remove any
		// previous macro with that name before adding the new one, since Sets
		// don't add an element if it "already exists."
		macros.remove(macro);
		macros.add(macro);
		support.firePropertyChange(PROPERTY_MACROS, null, null);
	}


	/**
	 * Adds a property change listener to this macro manager.
	 *
	 * @param property The property to listen for.
	 * @param l The listener.
	 * @see #removePropertyChangeListener(String, PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String property,
											PropertyChangeListener l) {
		support.addPropertyChangeListener(property, l);
	}


	/**
	 * Removes all macros from this manager.  This fires a property change
	 * event of type {@link #PROPERTY_MACROS}.
	 *
	 * @return The macros that existed before the clear operation.  This may be
	 *         empty, but will never be <code>null</code>.
	 */
	public SortedSet<Macro> clearMacros() {
		TreeSet<Macro> copy = new TreeSet<Macro>(macros);
		macros.clear();
		support.firePropertyChange(PROPERTY_MACROS, null, null);
		return copy;
	}


	/**
	 * Returns whether a macro is defined with the specified name.
	 *
	 * @param name The name to check for.
	 * @return Whether a macro is already defined with that name.
	 */
	public boolean containsMacroNamed(String name) {
		boolean found = false;
		for (Macro macro : macros) {
			if (macro.getName().equalsIgnoreCase(name)) {
				found = true;
				break;
			}
		}
		return found;
	}


	/**
	 * Returns the singleton instance of the macro manager.
	 *
	 * @return The macro manager instance.
	 */
	public static MacroManager get() {
		return INSTANCE;
	}


	/**
	 * returns the number of macros.
	 *
	 * @return The number of macros.
	 */
	public int getMacroCount() {
		return macros.size();
	}


	/**
	 * Returns an iterator over the macros.
	 *
	 * @return An iterator over the macros.
	 */
	public Iterator<Macro> getMacroIterator() {
		return macros.iterator();
	}


	/**
	 * Loads all macros from a directory.
	 *
	 * @param dir The directory to load macros from.
	 * @throws IOException If an IO error occurs reading the macros.
	 * @see #saveMacros(File)
	 */
	public void loadMacros(File dir) throws IOException {

		// Since class Macro was loaded by PluginClassLoader, XMLEncoder will
		// throw an Exception when trying to save it as the class was not loaded
		// by the same ClassLoader as XMLEncoder.  In Java 7 XMLEncoder can take
		// a ClassLoader parameter, but until then, this is a workaround.
		ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(Macro.class.getClassLoader());

		File file = new File(dir, MACRO_DEFINITION_FILE_NAME);
		if (file.isFile()) {
			XMLDecoder d = new XMLDecoder(new BufferedInputStream(
					new FileInputStream(file)));
			@SuppressWarnings("unchecked")
			List<Macro> macroList = (List<Macro>)d.readObject();
			for (Macro macro : macroList) {
				addMacro(macro);
			}
		}

		Thread.currentThread().setContextClassLoader(threadCL);

		support.firePropertyChange(PROPERTY_MACROS, null, null);

	}


	/**
	 * Removes a property change listener from this macro manager.
	 *
	 * @param property The property listened for.
	 * @param l The listener to remove.
	 * @see #addPropertyChangeListener(String, PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String property,
											PropertyChangeListener l) {
		support.removePropertyChangeListener(property, l);
	}


	/**
	 * Removes a macro.  This method fires a property change event of type
	 * {@link #PROPERTY_MACROS}.
	 *
	 * @param macro The macro to remove.
	 * @see #addMacro(Macro)
	 */
	public void removeMacro(Macro macro) {
		if (macros.remove(macro)) {
			support.firePropertyChange(PROPERTY_MACROS, null, null);
		}
	}


	/**
	 * Saves all macros known to this macro manager.
	 *
	 * @param dir The directory to save the macro definitions to.  The old
	 *        contents of this directory are deleted.
	 * @throws IOException If an IO error occurs writing the files.
	 * @see #loadMacros(File)
	 */
	public void saveMacros(File dir) throws IOException {

		// First, clear out old macros
		if (dir.isDirectory()) { // Should always already exist.
			File[] oldFiles = dir.listFiles(new OldMacroFilenameFilter());
			for (int i=0; i<oldFiles.length; i++) {
				oldFiles[i].delete();
			}
		}
		else {
			dir.mkdir();
		}

		// Since class Macro was loaded by PluginClassLoader, XMLEncoder will
		// throw an Exception when trying to save it as the class was not loaded
		// by the same ClassLoader as XMLEncoder.  In Java 7 XMLEncoder can take
		// a ClassLoader parameter, but until then, this is a workaround.
		ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(Macro.class.getClassLoader());
		try {

			// Put our macros into a list.
			List<Macro> macroList = new ArrayList<Macro>(macros);

			// Save our list of macros as XML.
			File file = new File(dir, MACRO_DEFINITION_FILE_NAME);
			XMLEncoder e = new XMLEncoder(new BufferedOutputStream(
										new FileOutputStream(file)));
			try {
				e.writeObject(macroList);
			} finally {
				e.close();
			}

		} finally {
			Thread.currentThread().setContextClassLoader(threadCL);
		}

	}


	/**
	 * Filter that locates old macro definition files.
	 */
	private static class OldMacroFilenameFilter implements FileFilter {

		public boolean accept(File file) {
			return file.getName().endsWith(OLD_MACRO_FILE_EXTENSION);
		}

	}


}