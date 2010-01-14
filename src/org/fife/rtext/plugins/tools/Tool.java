/*
 * 11/05/2009
 *
 * Tool.java - An "external tool."
 * Copyright (C) 2009 Robert Futrell
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
package org.fife.rtext.plugins.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An "external tool."
 *
 * @author Robert Futrell
 * @version 1.0
 */
/*
 * NOTE: In 1.5, most of these fields could be replaced with a single
 * ProcessBuilder instance.
 */
public class Tool implements Comparable {

	private String name;
	private String desc;
	private File dir;
	private String program;
	private List args;
	private Map env;
	private boolean appendEnv;


	/**
	 * Constructor.
	 *
	 * @param name The name of this tool.
	 * @param desc A description of this tool.  This may be <code>null</code>.
	 */
	public Tool(String name, String desc) {
		this.name = name;
		setDescription(desc);
		args = new ArrayList(3);
		env = new HashMap();
	}


	/**
	 * Adds a command line argument for this tool.
	 *
	 * @param arg The argument.  This cannot be <code>null</code>.
	 * @see #clearArgs()
	 * @see #setProgram(String)
	 */
	public void addArg(String arg) {
		if (arg==null) {
			throw new IllegalArgumentException("arg cannot be null");
		}
		args.add(arg);
	}


	/**
	 * Clears the command line arguments.
	 *
	 * @see #addArg(String)
	 */
	public void clearArgs() {
		args.clear();
	}


	/**
	 * Clears the environment variables associated with this tool.
	 * Note that if this tool is appending its environment to RText's
	 * environment, this does not clear the RText environment that is
	 * appended to; it only clears the environment variables to add.
	 *
	 * @see #putEnvVar(String, String)
	 */
	public void clearEnvVars() {
		env.clear();
	}


	/**
	 * Compares this tool to another by name, lexicographically.
	 *
	 * @param o The other tool.
	 * @return The sort order of this tool, compared to another.
	 */
	public int compareTo(Object o) {
		int val = -1;
		if (o==this) {
			val = 0;
		}
		else if (o instanceof Tool) {
			val = getName().compareTo(((Tool)o).getName());
		}
		return val;
	}


	/**
	 * Returns whether this tool and another have the same name.
	 *
	 * @return Whether this tool and another have the same name.
	 */
	public boolean equals(Object o) {
		return compareTo(o)==0;
	}


	/**
	 * Returns whether this tool should append any environment variables
	 * it defines to RText's current environment.
	 *
	 * @return Whether to append the environment variables defined.  If this
	 *         value is <code>false</code>, RText's environment is not
	 *         appended.
	 * @see #setAppendEnvironmentVars(boolean)
	 */
	public boolean getAppendEnvironmentVars() {
		return appendEnv;
	}


	/**
	 * Returns a description of this tool.
	 *
	 * @return A description of this tool, or <code>null</code> if none
	 *         is defined.
	 * @see #setDescription(String)
	 */
	public String getDescription() {
		return desc;
	}


	/**
	 * Returns the name of this tool.
	 *
	 * @return The name of this tool.
	 */
	public String getName() {
		return name;
	}


	/**
	 * Returns the program to launch.
	 *
	 * @return The program to launch.
	 * @see #setProgram(String)
	 */
	public String getProgram() {
		return program;
	}


	/**
	 * Returns the directory the tool will run in.
	 *
	 * @return The directory.
	 * @see #setDirectory(File)
	 */
	public File getDirectory() {
		return dir;
	}


	/**
	 * Returns the hash code of this tool.
	 *
	 * @return This tool's hash code.
	 */
	public int hashCode() {
		return getName().hashCode();
	}


	/**
	 * Sets an environment variable for this tool.
	 *
	 * @param name The name of the environment variable.
	 * @param value The value of the variable.  If this is <code>null</code>,
	 *        then this variable will not be set with a special value.
	 * @see #clearEnvVars()
	 */
	public void putEnvVar(String name, String value) {
		// env.put(name, null) will store a null value into a HashMap
		if (value!=null) {
			env.put(name, value);
		}
		else {
			env.remove(name);
		}
	}


	/**
	 * Sets whether this tool should append any environment variables
	 * it defines to RText's current environment.
	 *
	 * @param append Whether to append the environment variables defined.  If
	 *        this value is <code>false</code>, RText's environment is not
	 *        appended.
	 * @see #getAppendEnvironmentVars()
	 */
	public void setAppendEnvironmentVars(boolean append) {
		this.appendEnv = append;
	}


	/**
	 * Sets a description of this tool.
	 *
	 * @param desc A description of this tool.  This may be <code>null</code>.
	 * @see #getDescription()
	 */
	public void setDescription(String desc) {
		this.desc = desc;
	}


	/**
	 * Sets the directory for this tool to run in.
	 *
	 * @param dir The directory.  This cannot be <code>null</code>.
	 * @see #getDirectory()
	 */
	public void setDirectory(File dir) {
		if (dir==null) {
			throw new IllegalArgumentException("dir cannot be null");
		}
		this.dir = dir;
	}


	/**
	 * Sets the program to launch.
	 *
	 * @param program The program.  This cannot be <code>null</code>.
	 * @see #getProgram()
	 * @see #addArg(String)
	 */
	public void setProgram(String program) {
		if (program==null) {
			throw new IllegalArgumentException("program cannot be null");
		}
		this.program = program;
	}


}