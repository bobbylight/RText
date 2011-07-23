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
import java.util.Iterator;
import java.util.NoSuchElementException;
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

	private SortedSet macros;
	private PropertyChangeSupport support;

	/**
	 * The extension all macro files end with.
	 */
	private static final String MACRO_FILE_EXTENSION		= ".macro";

	/**
	 * The singleton instance of this class.
	 */
	private static final MacroManager INSTANCE = new MacroManager();


	/**
	 * Private constructor to prevent instantiation.
	 */
	private MacroManager() {
		macros = new TreeSet();
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
	 */
	public void clearMacros() {
		macros.clear();
		support.firePropertyChange(PROPERTY_MACROS, null, null);
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
	public Iterator getMacroIterator() {
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

		File[] files = dir.listFiles(new MacroFilenameFilter());
		boolean delete = false;
		for (int i=0; i<files.length; i++) {

			delete = false;
			XMLDecoder d = new XMLDecoder(new BufferedInputStream(
					new FileInputStream(files[i])));

			try {
				addMacro((Macro)d.readObject());
			} catch (NoSuchElementException nsee) {
				// Thrown when reading the old macro files.
				delete = true;
			} finally {
				d.close();
			}

			// Just the old macro format; remove these (very rude, but I
			// don't think anybody was using them).
			if (delete) {
				files[i].delete();
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
	 * {@link #PROPERTY_TOOLS}.
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
			File[] oldFiles = dir.listFiles(new MacroFilenameFilter());
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

		// Now save the new ones.
		for (Iterator i=getMacroIterator(); i.hasNext(); ) {
			Macro macro = (Macro)i.next();
			File file = new File(dir, macro.getName() + MACRO_FILE_EXTENSION);
			XMLEncoder e = new XMLEncoder(new BufferedOutputStream(
										new FileOutputStream(file)));
			try {
				e.writeObject(macro);
			} finally {
				e.close();
			}
		}

		Thread.currentThread().setContextClassLoader(threadCL);

	}


	/**
	 * Filter that locates macro definition files.
	 */
	private static class MacroFilenameFilter implements FileFilter {

		public boolean accept(File file) {
			return file.getName().endsWith(MACRO_FILE_EXTENSION);
		}

	}


}