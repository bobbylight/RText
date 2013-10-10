/*
 * 01/06/2010
 *
 * ToolManager.java - Manages external tools.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

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
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Manages the tools available in an RText session.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ToolManager {

	/**
	 * Event fired when a tool is added or removed.
	 */
	public static final String PROPERTY_TOOLS			= "tools";

	private SortedSet<Tool> tools;
	private PropertyChangeSupport support;

	/**
	 * The extension all tool files end with.
	 */
	private static final String TOOL_FILE_EXTENSION		= ".tool";

	/**
	 * The singleton instance of this class.
	 */
	private static final ToolManager INSTANCE = new ToolManager();


	/**
	 * Private constructor to prevent instantiation.
	 */
	private ToolManager() {
		tools = new TreeSet<Tool>();
		support = new PropertyChangeSupport(this);
	}


	/**
	 * Adds a tool.  This methods fires a property change event of type
	 * {@link #PROPERTY_TOOLS}.
	 *
	 * @param tool The tool to add.
	 * @see #removeTool(Tool)
	 */
	public void addTool(Tool tool) {
		tools.add(tool);
		support.firePropertyChange(PROPERTY_TOOLS, null, null);
	}


	/**
	 * Adds a property change listener to this tool manager.
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
	 * Removes all tools from this manager.  This fires a property change
	 * event of type {@link #PROPERTY_TOOLS}.
	 */
	public void clearTools() {
		tools.clear();
		support.firePropertyChange(PROPERTY_TOOLS, null, null);
	}


	/**
	 * Returns whether a tool with a given name is already defined.
	 *
	 * @param name The name of the tool.
	 * @return Whether a tool with that name is already defined.
	 */
	public boolean containsToolWithName(String name) {
		for (Tool tool : tools) {
			if (name.equals(tool.getName())) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Returns the singleton instance of the tool manager.
	 *
	 * @return The tool manager instance.
	 */
	public static ToolManager get() {
		return INSTANCE;
	}


	/**
	 * returns the number of tools.
	 *
	 * @return The number of tools.
	 */
	public int getToolCount() {
		return tools.size();
	}


	/**
	 * Returns an iterator over the tools.
	 *
	 * @return An iterator over the tools.
	 */
	public Iterator<Tool> getToolIterator() {
		return tools.iterator();
	}


	/**
	 * Loads all tools from a directory.
	 *
	 * @param dir The directory to load tools from.
	 * @throws IOException If an IO error occurs reading the tools.
	 * @see #saveTools(File)
	 */
	public void loadTools(File dir) throws IOException {

		// Since class Tool was loaded by PluginClassLoader, XMLEncoder will
		// throw an Exception when trying to save it as the class was not loaded
		// by the same ClassLoader as XMLEncoder.  In Java 7 XMLEncoder can take
		// a ClassLoader parameter, but until then, this is a workaround.
		ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(Tool.class.getClassLoader());

		File[] files = dir.listFiles(new ToolFilenameFilter());
		for (int i=0; i<files.length; i++) {
			XMLDecoder d = new XMLDecoder(new BufferedInputStream(
					new FileInputStream(files[i])));
			try {
				addTool((Tool)d.readObject());
			} finally {
				d.close();
			}
		}

		Thread.currentThread().setContextClassLoader(threadCL);

		support.firePropertyChange(PROPERTY_TOOLS, null, null);

	}


	/**
	 * Removes a property change listener from this tool manager.
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
	 * Removes a tool.  This method fires a property change event of type
	 * {@link #PROPERTY_TOOLS}.
	 *
	 * @param tool The tool to remove.
	 * @see #addTool(Tool)
	 */
	public void removeTool(Tool tool) {
		if (tools.remove(tool)) {
			support.firePropertyChange(PROPERTY_TOOLS, null, null);
		}
	}


	/**
	 * Saves all tools known to this tool manager.
	 *
	 * @param dir The directory to save the tool definitions to.  The old
	 *        contents of this directory are deleted.
	 * @throws IOException If an IO error occurs writing the files.
	 * @see #loadTools(File)
	 */
	public void saveTools(File dir) throws IOException {

		// First, clear out old tools
		if (dir.isDirectory()) { // Should always already exist.
			File[] oldFiles = dir.listFiles(new ToolFilenameFilter());
			for (int i=0; i<oldFiles.length; i++) {
				oldFiles[i].delete();
			}
		}
		else {
			dir.mkdir();
		}

		// Since class Tool was loaded by PluginClassLoader, XMLEncoder will
		// throw an Exception when trying to save it as the class was not loaded
		// by the same ClassLoader as XMLEncoder.  In Java 7 XMLEncoder can take
		// a ClassLoader parameter, but until then, this is a workaround.
		ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(Tool.class.getClassLoader());

		// Now save the new ones.
		for (Tool tool : tools) {
			File file = new File(dir, tool.getName() + TOOL_FILE_EXTENSION);
			XMLEncoder e = new XMLEncoder(new BufferedOutputStream(
										new FileOutputStream(file)));
			try {
				e.writeObject(tool);
			} finally {
				e.close();
			}
		}

		Thread.currentThread().setContextClassLoader(threadCL);

	}


	/**
	 * Filter that locates tool definition files.
	 */
	private static class ToolFilenameFilter implements FileFilter {

		public boolean accept(File file) {
			return file.getName().endsWith(TOOL_FILE_EXTENSION);
		}

	}


}