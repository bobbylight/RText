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

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.fife.ui.ImageTranscodingUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.fife.io.UnicodeReader;
import org.fife.ui.OS;
import org.fife.ui.rtextarea.IconGroup;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * A class that knows how to parse an XML file defining icon groups.  RText
 * uses this class to define its available icon groups.
 *
 * @author Robert Futrell
 * @version 0.7
 */
final class IconGroupLoader extends DefaultHandler {

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
		iconGroupMap = new HashMap<>(3);
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

			SAXParser parser = SAXParserFactory.newDefaultInstance().newSAXParser();
			InputSource is = new InputSource(new UnicodeReader(
					new FileInputStream(iconGroupFile), "UTF-8"));
			is.setEncoding("UTF-8");
			parser.parse(is, this);
		} catch (Exception e) {
			e.printStackTrace();
			owner.displayException(e);
			return iconGroupMap; // Is not necessarily empty
		}
		//long time = System.currentTimeMillis() - start;
		//System.err.println("DEBUG: IconGroupLoader parsing: " + time + " ms");

		// Add default icon groups.
		if (owner.getOS()==OS.WINDOWS) {
			addOfficeLnFsIconGroups();
		}

		IconGroup flatIconGroup = new SvgIconGroup("IntelliJ Icons (Dark)",
			"icongroups/intellij-icons.jar");
		iconGroupMap.put(flatIconGroup.getName(), flatIconGroup);

		return iconGroupMap;

	}


	/**
	 * Reads all icon groups from the specified XML file and returns a map
	 * of them.
	 *
	 * @param rtext The RText instance.
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


	private class SvgIconGroup extends IconGroup {

		private String jarFile;

		SvgIconGroup(String name, String jarFile) {
			super(name, "", null, "svg", jarFile);
			this.jarFile = owner.getInstallLocation() + '/' + jarFile;
		}

		@Override
		protected Icon getIconImpl(String iconFullPath) {
			try (InputStream svg = new URL("jar:file:///" +
					jarFile + "!/" + iconFullPath).openStream()) {
				//System.err.println("***** " + url.toString());
				BufferedImage image = ImageTranscodingUtil.rasterize(
					iconFullPath, svg, 16, 16);
				return new ImageIcon(image);
			} catch (IOException ioe) {
				// If any one icon's not there, we just don't display it in the UI
				return null;
			}
		}
	}
}
