/*
 * 07/31/2011
 *
 * Macro.java - A script to run in the RText JVM.
 * Copyright (C) 2011 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This file is a part of RText.
 *
 * RText is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.rtext.plugins.macros;


/**
 * A macro; that is, a script to run in the RText JVM.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Macro implements Comparable, Cloneable {

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
	public int compareTo(Object o) {
		int val = -1;
		if (o==this) {
			return 0;
		}
		else if (o instanceof Macro) {
			val = String.CASE_INSENSITIVE_ORDER.compare(
					getName(), ((Macro)o).getName());
		}
		return val;
	}


	/**
	 * Returns whether this macro and another have the same name.
	 *
	 * @return Whether this macro and another have the same name.
	 */
	public boolean equals(Object o) {
		return compareTo(o)==0;
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
	public String toString() {
		return getName();
	}


}