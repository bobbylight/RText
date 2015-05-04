/*
 * 07/31/2011
 *
 * Macro.java - A script to run in the RText JVM.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.macros;


/**
 * A macro; that is, a script to run in the RText JVM.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Macro implements Comparable<Macro>, Cloneable {

	/**
	 * Name of the macro.  Usually the same as the name of the file, without
	 * extension.
	 */
	private String name;

	/**
	 * A short (1-line) description of the macro.
	 */
	private String desc;

	/**
	 * Full path to the file.  Not a <code>java.io.File</code> to support
	 * encoding via XMLEncoder/XMLDecoder.
	 */
	private String file;

	/**
	 * Keyboard shortcut to execute the macro.
	 */
	private String accelerator;


	/**
	 * Constructor used to support serialization.
	 */
	public Macro() {
	}


	/**
	 * Returns a clone of this macro.
	 *
	 * @return A clone of this macro.
	 */
	@Override
	public Object clone() {
		Macro m = null;
		try {
			m = (Macro)super.clone();
		} catch (CloneNotSupportedException cnse) { // Never happens
			cnse.printStackTrace();
		}
		return m;
	}


	/**
	 * Compares this macro to another by name, lexicographically.
	 *
	 * @param o The other macro.
	 * @return The sort order of this macro, compared to another.
	 */
	public int compareTo(Macro o) {
		int val = -1;
		if (o==this) {
			return 0;
		}
		else if (o!=null) {
			val = String.CASE_INSENSITIVE_ORDER.compare(
					getName(), o.getName());
		}
		return val;
	}


	/**
	 * Returns whether this macro and another have the same name.
	 *
	 * @return Whether this macro and another have the same name.
	 */
	@Override
	public boolean equals(Object o) {
		return o instanceof Macro && compareTo((Macro)o)==0;
	}


	public String getAccelerator() {
		return accelerator;
	}


	public String getDesc() {
		return desc;
	}


	/**
	 * Returns the full path to the file for this macro.
	 *
	 * @return The full path to the file.
	 * @see #setFile(String)
	 */
	public String getFile() {
		return file;
	}


	public String getName() {
		return name;
	}


	@Override
	public int hashCode() {
		return getName().hashCode();
	}


	public void setAccelerator(String accelerator) {
		this.accelerator = accelerator;
	}


	public void setDesc(String desc) {
		this.desc = desc;
	}


	/**
	 * Sets the full path to the file for this macro.
	 *
	 * @param file The full path to the file.
	 * @see #getFile()
	 */
	public void setFile(String file) {
		this.file = file;
	}


	public void setName(String name) {
		this.name = name;
	}


	/**
	 * Overridden to return the name of this macro.  Used by the Macro options
	 * panel.
	 *
	 * @return The name of this macro.
	 */
	@Override
	public String toString() {
		return getName();
	}


}