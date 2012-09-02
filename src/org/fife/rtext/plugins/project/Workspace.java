/*
 * 08/28/2012
 *
 * Workspace.java - A collection of projects.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.fife.io.UnicodeReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


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

//		// Test data
//		for (int i=0; i<3; i++) {
//			Project project = new Project("Project" + (i+1));
//			project.addEntry(new FileProjectEntry("C:/temp/test.txt"));
//			project.addEntry(new FileProjectEntry("C:/temp/test.java"));
//			projects.add(project);
//		}

	}


	/**
	 * Adds a project to this workspace.
	 *
	 * @param project The project to add.
	 */
	public void addProject(Project project) {
		projects.add(project);
	}


	/**
	 * Creates a DOM representation of this workspace.
	 *
	 * @return The DOM representation of this workspace.
	 * @throws IOException If an error occurs.
	 */
	private Document createXmlRepresentation() throws IOException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
			throw new IOException(pce.getMessage());
		}
		Document doc = db.newDocument();

		Element root = doc.createElement("workspace");
		doc.appendChild(root);

		Element projectsElem = doc.createElement("projects");
		root.appendChild(projectsElem);

		for (Iterator i=getProjectIterator(); i.hasNext(); ) {

			Project project = (Project)i.next();
			Element projElem = doc.createElement("project");
			projElem.setAttribute("name", project.getName());
			projectsElem.appendChild(projElem);

			for (Iterator j=project.getEntryIterator(); j.hasNext(); ) {
				ProjectEntry entry = (ProjectEntry)j.next();
				Element entryElem = doc.createElement("project-entry");
				entryElem.setAttribute("type", entry.getType());
				entryElem.setAttribute("name", entry.getFile().getAbsolutePath());
				projElem.appendChild(entryElem);
			}

		}

		return doc;

	}


	public String getName() {
		return name;
	}


	public Iterator getProjectIterator() {
		return projects.iterator();
	}


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
			Project project = new Project(name);
			workspace.addProject(project);

			NodeList entryList = projElem.getElementsByTagName("project-entry");
			for (int j=0; j<entryList.getLength(); j++) {
				Element entryElem = (Element)entryList.item(j);
				name = entryElem.getAttribute("name");
				String type = entryElem.getAttribute("type");
				ProjectEntry entry = null;
				if (ProjectEntry.DIR_PROJECT_ENTRY.equals(type)) {
					entry = new FolderProjectEntry(new File(name));
				}
				else if (ProjectEntry.FILE_PROJECT_ENTRY.equals(type)) {
					entry = new FileProjectEntry(new File(name));
				}
				project.addEntry(entry);
			}

		}

		return workspace;

	}


	/**
	 * Loads a workspace from XML.
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


	public void save(File dir) throws IOException {

		Document doc = createXmlRepresentation();

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