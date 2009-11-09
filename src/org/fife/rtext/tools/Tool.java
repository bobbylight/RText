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
package org.fife.rtext.tools;

import java.io.File;
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
public class Tool {

	private String name;
	private File dir;
	private String program;
	private List args;
	private Map env;
	private boolean appendEnv;


	/**
	 * Constructor.
	 *
	 * @param name The name of this tool.
	 */
	public Tool(String name) {
		this.name = name;
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
	 */
	public void setProgram(String program) {
		if (program==null) {
			throw new IllegalArgumentException("program cannot be null");
		}
		this.program = program;
	}


}