/*
 * 05/29/2005
 *
 * IconGroupLoader.java - Loads icon groups from an XML file.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import org.fife.io.UnicodeReader;
import org.fife.ui.rtextarea.IconGroup;


/**
 * A class that knows how to parse an XML file defining icon groups.  RText
 * uses this class to define its available icon groups.
 *
 * @author Robert Futrell
 * @version 0.7
 */
class IconGroupLoader extends DefaultHandler {

	/**
	 * The name of the default icon group.  This icon group MUST be defined
	 * in ExtraIcons.xml, or RText will not start!
	 */
	public static final String DEFAULT_ICON_GROUP_NAME = "Eclipse Icons";

	private RText owner;
	private Map<String, IconGroup> iconGroupMap;

	private static final String GROUP				= "group";
	private static final String NAME				= "name";
	private static final String PATH				= "path";
	private static final String LARGEICONSUBDIR		= "largeIconSubDir";
	private static final String EXTENSION			= "extension";
	private static final String JAR				= "jar";


	/**
	 * Constructor.
	 *
	 * @param owner The RText instance.
	 */
	private IconGroupLoader(RText owner) {
		this.owner = owner;
		iconGroupMap = new HashMap<String, IconGroup>(3);
	}


	/**
	 * Add icon groups for the icons in OfficeLnFs.jar.  These are only
	 * shipped with RText on Windows hosts.
	 */
	private void addOfficeLnFsIconGroups() {

		String windows98Icons	= "Windows 98 Icons";
		String office2003Icons	= "Office 2003 Icons";
		IconGroup win98IconGroup = new IconGroup(windows98Icons,
												"org/fife/plaf/OfficeXP/");
		IconGroup office2003IconGroup = new IconGroup(office2003Icons,
												"org/fife/plaf/Office2003/");

		iconGroupMap.put(win98IconGroup.getName(), win98IconGroup);
		iconGroupMap.put(office2003IconGroup.getName(), office2003IconGroup);

	}


	/**
	 * Creates the XML reader to use.  Note that in 1.4 JRE's, the reader
	 * class wasn't defined by default, but in 1.5+ it is.
	 *
	 * @return The XML reader to use.
	 */
	private XMLReader createReader() {
		XMLReader reader = null;
		try {
			reader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			// Happens in JRE 1.4.x; 1.5+ define the reader class properly
			try {
				reader = XMLReaderFactory.createXMLReader(
						"org.apache.crimson.parser.XMLReaderImpl");
			} catch (SAXException se) {
				owner.displayException(se);
			}
		}
		return reader;
	}


	/**
	 * Reads all icon groups from the specified XML file.
	 *
	 * @param iconGroupFile The file from which to read.
	 * @return A map of icon groups.  The key to each icon group is that
	 *         icon group's name.  If no icon groups are available (which is
	 *         an error), an empty map is returned.
	 */
	private Map<String, IconGroup> doLoad(String iconGroupFile) {

		//long start = System.currentTimeMillis();
		try {
			XMLReader xr = createReader();
			if (xr==null) {
				return iconGroupMap; // Return the empty map
			}
			xr.setContentHandler(this);
			InputSource is = new InputSource(new UnicodeReader(
					new FileInputStream(iconGroupFile), "UTF-8"));
			is.setEncoding("UTF-8");
			xr.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
			owner.displayException(e);
			return iconGroupMap; // Is not necessarily empty
		}
		//long time = System.currentTimeMillis() - start;
		//System.err.println("DEBUG: IconGroupLoader parsing: " + time + " ms");

		// Add default icon groups.
		if (owner.getOS()==RText.OS_WINDOWS) {
			addOfficeLnFsIconGroups();
		}

		return iconGroupMap;

	}


	/**
	 * Reads all icon groups from the specified XML file and returns a map
	 * of them.
	 *
	 * @param RText The RText instance.
	 * @param iconGroupFile The file from which to read.
	 * @return A map of icon groups.  The key to each icon group is that
	 *         icon group's name.
	 */
	public static Map<String, IconGroup> loadIconGroups(RText rtext,
			String iconGroupFile) {
		IconGroupLoader loader = new IconGroupLoader(rtext);
		return loader.doLoad(iconGroupFile);
	}


	/**
	 * Callback when an XML element begins.  Our XML is simple, and we're
	 * only interested in "group" elements and their attributes.
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
							Attributes attributes) {
		if (GROUP.equals(qName)) {
			String name = attributes.getValue(NAME);
			String path = attributes.getValue(PATH);
			if (path==null) {
				path = ""; // Default to an empty path.
			}
			String largeIconSubDir = attributes.getValue(LARGEICONSUBDIR);
			String extension = attributes.getValue(EXTENSION);
			String jar = attributes.getValue(JAR);
			if (jar!=null) {
				jar = owner.getInstallLocation() + '/' + jar;
			}
			IconGroup group = new IconGroup(name, path,
					largeIconSubDir, extension, jar);
			iconGroupMap.put(name, group);
		}
	}


}