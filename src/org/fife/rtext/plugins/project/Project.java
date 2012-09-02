/*
 * 08/28/2012
 *
 * Project.java - A logical representation of a programming project.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A project is a logical collection of {@link ProjectEntry}s.  For maximum
 * flexibility, this plugin does not require all entries in a project to live in
 * a common root folder.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Project {

	private String name;
	private List entries;


	public Project(String name) {
		setName(name);
		entries = new ArrayList();
	}


	public void addEntry(ProjectEntry entry) {
		entries.add(entry);
	}


	public Iterator getEntryIterator() {
		return entries.iterator();
	}


	public String getName() {
		return name;
	}


	public void removeEntry(ProjectEntry entry) {
		entries.remove(entry);
	}


	public void setName(String name) {
		this.name = name;
	}


}