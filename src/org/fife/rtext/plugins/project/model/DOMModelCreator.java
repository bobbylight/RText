/*
 * 09/08/2012
 *
 * DOMModelCreator.java - Creates a DOM representation of a workspace.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.model;

import java.io.IOException;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fife.rtext.RTextUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Creates a DOM representation of a workspace.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DOMModelCreator implements WorkspaceVisitor {

	private Document doc;
	private Element projectsElem;
	private Stack<Element> projEntryParentStack;


	public DOMModelCreator() throws IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
			throw new IOException(pce.getMessage());
		}
		doc = db.newDocument();
	}


	private Element getCurrentProjectEntryParentElem() {
		return projEntryParentStack.peek();
	}


	/**
	 * Returns the generated DOM object.
	 *
	 * @return The generated DOM object.
	 */
	public Document getDocument() {
		return doc;
	}


	public void postVisit(Workspace workspace) {}


	public void postVisit(Project project) {
		projEntryParentStack.pop();
	}


	public void postVisit(FileProjectEntry entry) {}

	public void postVisit(FolderProjectEntry entry) {}

	public void postVisit(LogicalFolderProjectEntry entry) {
		projEntryParentStack.pop();
	}


	public void visit(Workspace workspace) {
		Element root = doc.createElement("workspace");
		doc.appendChild(root);
		projectsElem = doc.createElement("projects");
		root.appendChild(projectsElem);
		projEntryParentStack = new Stack<Element>();
	}


	public void visit(Project project) {
		Element projElem = doc.createElement("project");
		projElem.setAttribute("name", project.getName());
		projectsElem.appendChild(projElem);
		projEntryParentStack.push(projElem);
	}


	public void visit(FileProjectEntry entry) {
		Element entryElem = doc.createElement(entry.getType());
		entryElem.setAttribute("path", entry.getSaveData());
		getCurrentProjectEntryParentElem().appendChild(entryElem);
	}


	public void visit(FolderProjectEntry entry) {
		Element entryElem = doc.createElement(entry.getType());
		entryElem.setAttribute("path", entry.getSaveData());
		entryElem.setAttribute("name", entry.getDisplayName());
		FolderFilterInfo info = entry.getFilterInfo();
		entryElem.setAttribute("displayed-files",
				RTextUtilities.join(info.getAllowedFileFilters()));
		entryElem.setAttribute("hidden-files",
				RTextUtilities.join(info.getHiddenFileFilters()));
		entryElem.setAttribute("hidden-folders",
				RTextUtilities.join(info.getHiddenFolderFilters()));
		getCurrentProjectEntryParentElem().appendChild(entryElem);
	}


	public void visit(LogicalFolderProjectEntry entry) {
		Element entryElem = doc.createElement(entry.getType());
		entryElem.setAttribute("name", entry.getSaveData());
		getCurrentProjectEntryParentElem().appendChild(entryElem);
		projEntryParentStack.push(entryElem);
	}


}