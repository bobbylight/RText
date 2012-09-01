package org.fife.rtext.plugins.project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A workspace is a collection of projects.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Workspace {

	private String name;
	private List projects;


	public Workspace(String name) {

		setName(name);
		projects = new ArrayList();

		// Test data
		for (int i=0; i<3; i++) {
			Project project = new Project("Project" + (i+1));
			project.addEntry(new FileProjectEntry("C:/temp/test.txt"));
			project.addEntry(new FileProjectEntry("C:/temp/test.java"));
			projects.add(project);
		}

	}


	public String getName() {
		return name;
	}


	public Iterator getProjectIterator() {
		return projects.iterator();
	}


	public void setName(String name) {
		this.name = name;
	}


}