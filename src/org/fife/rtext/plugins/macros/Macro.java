package org.fife.rtext.plugins.macros;

import java.io.File;


/**
 * A macro; that is, a script to run in the RText JVM.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Macro implements Comparable {

	private String name;
	private String desc;
	private File file;
	private String accelerator;


	/**
	 * Constructor used to support serialization.
	 */
	public Macro() {
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
			val = ((Macro)o).getName().compareTo(getName());
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


	public File getFile() {
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


	public void setFile(File file) {
		this.file = file;
	}


	public void setName(String name) {
		this.name = name;
	}


}