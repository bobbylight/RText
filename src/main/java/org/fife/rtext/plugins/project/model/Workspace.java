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
import javax.swing.JOptionPane;
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
import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectPlugin;


/**
 * A workspace is a collection of projects.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Workspace implements ModelEntity {

	private ProjectPlugin plugin;
	private File file;
	private String name;
	private List<Project> projects;


	public Workspace(ProjectPlugin plugin, File file) {
		this.plugin = plugin;
		this.file = file;
		this.name = getNameFromFile(file);//setName(getNameFromFile(file));
		projects = new ArrayList<Project>();
	}


	public void accept(WorkspaceVisitor visitor) {
		visitor.visit(this);
		for (Project project : projects) {
			project.accept(visitor);
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
		for (Project project : projects) {
			if (name.equals(project.getName())) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Returns the absolute path to the file containing this workspace.
	 *
	 * @return This workspace's file.
	 */
	public String getFileFullPath() {
		return file.getAbsolutePath();
	}


	public String getName() {
		return name;
	}


	private static final String getNameFromFile(File file) {
		String name = file.getName();
		int lastDot = name.lastIndexOf('.');
		if (lastDot>-1) {
			name = name.substring(0, lastDot);
		}
		return name;
	}


	/**
	 * Returns the index of the specified project in the project list.
	 *
	 * @param project The project to search for.
	 * @return The index of the project, or <code>-1</code> if the project is
	 *         not contained in this workspace.
	 */
	private int getProjectIndex(Project project) {
		for (int i=0; i<projects.size(); i++) {
			if (project==projects.get(i)) {
				return i;
			}
		}
		return -1;
	}


	public Iterator<Project> getProjectIterator() {
		return projects.iterator();
	}


	/**
	 * Loads a workspace from an XML file.
	 *
	 * @param plugin The project plugin.
	 * @param file The XML file.
	 * @return The workspace.
	 * @throws IOException If an IO error occurs.
	 */
	public static Workspace load(ProjectPlugin plugin, File file) throws IOException {

		Document doc = loadXmlRepresentation(file);
		Element root = doc.getDocumentElement();
		Workspace workspace = new Workspace(plugin, file);

		NodeList children = root.getElementsByTagName("projects");
		Element projectsElem = (Element)children.item(0);
		NodeList projElemList = projectsElem.getElementsByTagName("project");
		for (int i=0; i<projElemList.getLength(); i++) {
			Element projElem = (Element)projElemList.item(i);
			String name = projElem.getAttribute("name");
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


	public boolean moveProjectDown(Project project) {
		int index = getProjectIndex(project);
		if (index>-1 && index<projects.size()-1) {
			projects.remove(index);
			projects.add(index+1, project);
			return true;
		}
		return false;
	}


	public boolean moveProjectUp(Project project) {
		int index = getProjectIndex(project);
		if (index>0) {
			projects.remove(index);
			projects.add(index-1, project);
			return true;
		}
		return false;
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


	public void save() throws IOException {
		saveImpl(file);
	}


	private void saveImpl(File loc) throws IOException {

		// Sanity check, folders may be deleted out from under us
		File parentDir = loc.getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}

		DOMModelCreator domCreator = new DOMModelCreator();
		accept(domCreator);
		Document doc = domCreator.getDocument();

		Result result = new StreamResult(loc);
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
			throw new IOException(te.getMessage());
		}

	}


	/**
	 * Gives this workspace a new name.  Immediately creates a new XML file
	 * in the same directory containing the previous workspace file.
	 * 
	 * @param name The new workspace name.
	 * @return Whether the operation was successful.
	 */
	public boolean setName(String name) {

		RText rtext = plugin.getRText();

		File tempFile = new File(file.getParentFile(), name + ".xml");
		if (tempFile.isFile()) {
			String msg = rtext.getString("FileAlreadyExists",
					tempFile.getAbsolutePath());
			String title = rtext.getString("ConfDialogTitle");
			int rc = JOptionPane.showConfirmDialog(rtext, msg, title,
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (rc!=JOptionPane.YES_OPTION) {
				// Bail early, but don't give an error message by returning
				// false
				return true;
			}
		}

		// If a save in the new location is successful, remember our new file
		// name and location.
		try {
			saveImpl(tempFile);
			file.delete(); // Don't leave outdated workspace files laying around 
			file = tempFile;
			this.name = name;
		} catch (IOException ioe) {
			rtext.displayException(ioe);
			return false;
		}

		return true;

	}


	/**
	 * Resolves entities and handles errors while parsing workspace XML.
	 */
	private static class Handler extends DefaultHandler {

		@Override
		public void error(SAXParseException e) throws SAXException {
			throw e;
		}

		@Override
		public InputSource resolveEntity(String publicID,  String systemID)
				throws SAXException {
			return new InputSource(getClass().
					getResourceAsStream("rtext-workspace-1.0.dtd"));
		}

	}


}