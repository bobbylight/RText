/*
 * 08/28/2012
 *
 * Workspace.java - A collection of projects.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import org.fife.io.UnicodeReader;
import org.fife.rtext.plugins.project.Messages;


/**
 * A workspace is a collection of projects.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Workspace implements ModelEntity {

	private String name;
	private List projects;


	public Workspace(String name) {
		setName(name);
		projects = new ArrayList();
	}


	public void accept(WorkspaceVisitor visitor) {
		visitor.visit(this);
		for (Iterator i=getProjectIterator(); i.hasNext(); ) {
			((Project)i.next()).accept(visitor);
		}
		visitor.postVisit(this);
	}


	/**
	 * Adds a project to this workspace.
	 *
	 * @param project The project to add.
	 * @see #removeProject(Project)
	 */
	public void addProject(Project project) {
		projects.add(project);
	}


	/**
	 * Returns whether a project already exists with the specified name.
	 *
	 * @param name The proposed project name.
	 * @return Whether a project with that name already exists.
	 */
	public boolean containsProjectNamed(String name) {
		for (Iterator i=getProjectIterator(); i.hasNext(); ) {
			Project project = (Project)i.next();
			if (name.equals(project.getName())) {
				return true;
			}
		}
		return false;
	}


	public String getName() {
		return name;
	}


	public Iterator getProjectIterator() {
		return projects.iterator();
	}


	/**
	 * Loads a workspace from an XML file.
	 *
	 * @param file The XML file.
	 * @return The workspace.
	 * @throws IOException If an IO error occurs.
	 */
	public static Workspace load(File file) throws IOException {

		Document doc = loadXmlRepresentation(file);
		Element root = doc.getDocumentElement();
		String name = file.getName().substring(0, file.getName().length()-4);
		Workspace workspace = new Workspace(name);

		NodeList children = root.getElementsByTagName("projects");
		Element projectsElem = (Element)children.item(0);
		NodeList projElemList = projectsElem.getElementsByTagName("project");
		for (int i=0; i<projElemList.getLength(); i++) {
			Element projElem = (Element)projElemList.item(i);
			name = projElem.getAttribute("name");
			Project project = new Project(workspace, name);
			workspace.addProject(project);
			recursivelyAddChildren(projElem, project);
		}

		return workspace;

	}


	/**
	 * Loads an XML file into a DOM structure, validating against the workspace
	 * DTD.
	 *
	 * @param file the XML file.
	 * @return The document.
	 * @throws IOException If an IO error occurs reading the file.
	 */
	private static Document loadXmlRepresentation(File file)
			throws IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(true);
		DocumentBuilder db = null;
		Document doc = null;
		Handler handler = new Handler();
		try {
			db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new UnicodeReader(
								new FileInputStream(file), "UTF-8"));
			is.setEncoding("UTF-8");
			db.setEntityResolver(handler);
			db.setErrorHandler(handler);
			doc = db.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
			String desc = e.getMessage();
			if (desc==null) {
				desc = e.toString();
			}
			String text = Messages.getString("ProjectPlugin.XmlError",
					file.getName(), desc);
			throw new IOException(text);
		}
		return doc;
	}


	private static String[] parseFilters(String str) {
		if (str==null || str.trim().length()==0) {
			return null;
		}
		return str.trim().split("\\s*,\\s*");
	}


	/**
	 * Recursively adds project entry nodes to a project entry parent node.
	 *
	 * @param parentElem The parent node's XML element.
	 * @param parent The parent node.
	 */
	private static void recursivelyAddChildren(Element parentElem,
			ProjectEntryParent parent) {

		NodeList childNodes = parentElem.getChildNodes();
		for (int j=0; j<childNodes.getLength(); j++) {

			Node childNode = childNodes.item(j);
			if (childNode.getNodeType()==Node.ELEMENT_NODE) {

				Element childElem = (Element)childNode;
				String tag = childElem.getNodeName();
				ProjectEntry entry = null;

				if (ProjectEntry.DIR_PROJECT_ENTRY.equals(tag)) {
					File folder = new File(childElem.getAttribute("path"));
					entry = new FolderProjectEntry(parent, folder);
					String displayName = childElem.getAttribute("name");
					if (displayName!=null && displayName.length()>0) {
						((FolderProjectEntry)entry).setDisplayName(displayName);
					}
					String temp = childElem.getAttribute("displayed-files");
					String[] displayedFiles = parseFilters(temp);
					temp = childElem.getAttribute("hidden-files");
					String[] hiddenFiles = parseFilters(temp);
					temp = childElem.getAttribute("hidden-folders");
					String[] hiddenFolders = parseFilters(temp);
					FolderFilterInfo info = new FolderFilterInfo(displayedFiles,
							hiddenFiles, hiddenFolders);
					((FolderProjectEntry)entry).setFilterInfo(info);
				}
				else if (ProjectEntry.FILE_PROJECT_ENTRY.equals(tag)) {
					File file = new File(childElem.getAttribute("path"));
					entry = new FileProjectEntry(parent, file);
				}
				else if (ProjectEntry.LOGICAL_DIR_PROJECT_ENTRY.equals(tag)) {
					String dirName = childElem.getAttribute("name");
					entry = new LogicalFolderProjectEntry(parent, dirName);
					recursivelyAddChildren(childElem, (ProjectEntryParent)entry);
				}

				parent.addEntry(entry);

			}

		}

	}


	public void removeProject(Project project) {
		projects.remove(project);
	}


	public void save(File dir) throws IOException {

		DOMModelCreator domCreator = new DOMModelCreator();
		accept(domCreator);
		Document doc = domCreator.getDocument();

		File file = new File(dir, name + ".xml");
		Result result = new StreamResult(file);
		Transformer t = null;
		try {
			t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "rtext-workspace.dtd");
			t.transform(new DOMSource(doc), result);
		} catch (TransformerConfigurationException tce) {
			throw new IOException(tce.getMessage());
		} catch (TransformerException te) {
			te.printStackTrace();
		}

	}


	public void setName(String name) {
		this.name = name;
	}


	/**
	 * Resolves entities and handles errors while parsing workspace XML.
	 */
	private static class Handler extends DefaultHandler {

		public void error(SAXParseException e) throws SAXException {
			throw e;
		}

		public InputSource resolveEntity(String publicID,  String systemID)
				throws SAXException {
			return new InputSource(getClass().
					getResourceAsStream("rtext-workspace.dtd"));
		}

	}


}