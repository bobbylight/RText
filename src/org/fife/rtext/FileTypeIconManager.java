/*
 * 08/23/2005
 *
 * FileTypeIconManager.java - Associates an icon with a file type.
 * Copyright (C) 2005 Robert Futrell
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
package org.fife.rtext;

import java.awt.Component;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.fife.ui.rtextfilechooser.Utilities;


/**
 * Manages icons used for file types by subclasses of
 * <code>AbstractMainView</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FileTypeIconManager {

	/**
	 * The map of file extensions to icon names.
	 */
	private HashMap ext2IconNameMap;

	/**
	 * The map of icon names to icons.
	 */
	private HashMap iconName2IconMap;

	/**
	 * The icon to use when no specific icon is found.
	 */
	private Icon defaultIcon;

	/**
	 * The singleton instance of this class.
	 */
	private static final FileTypeIconManager INSTANCE =
									new FileTypeIconManager();

	private static final String PATH = "org/fife/rtext/graphics/file_icons/";
	private static final String DEFAULT_ICON_PATH	= PATH + "txt.gif";


	/**
	 * Private constructor.
	 */
	private FileTypeIconManager() {

		ClassLoader cl = this.getClass().getClassLoader();
		defaultIcon = new ImageIcon(cl.getResource(DEFAULT_ICON_PATH));

		ext2IconNameMap = new HashMap();
		ext2IconNameMap.put("bat",	PATH + "bat.gif");
		ext2IconNameMap.put("cmd",	PATH + "bat.gif");
		ext2IconNameMap.put("c",		PATH + "c.gif");
		ext2IconNameMap.put("cpp",	PATH + "cpp.gif");
		ext2IconNameMap.put("cxx",	PATH + "cpp.gif");
		ext2IconNameMap.put("cs",	PATH + "cs.gif");
		ext2IconNameMap.put("h",		PATH + "h.gif");
		ext2IconNameMap.put("html",	PATH + "html.gif");
		ext2IconNameMap.put("java",	PATH + "java.gif");
		ext2IconNameMap.put("pl",	PATH + "pl.gif");
		ext2IconNameMap.put("perl",	PATH + "pl.gif");
		ext2IconNameMap.put("pm",	PATH + "pl.gif");
		ext2IconNameMap.put("sas",	PATH + "sas.gif");
		ext2IconNameMap.put("sh",	PATH + "sh.gif");
		ext2IconNameMap.put("bsh",	PATH + "sh.gif");
		ext2IconNameMap.put("csh",	PATH + "sh.gif");
		ext2IconNameMap.put("ksh",	PATH + "sh.gif");

		iconName2IconMap = new HashMap();

	}


	/**
	 * Returns the icon for a view type to use for the specified text area.
	 *
	 * @param textArea The text area.
	 * @return The icon to use for the text area.
	 */
	public Icon getIconFor(RTextEditorPane textArea) {

		Icon icon = null;

		// If this file has no extension, use the default icon.
		String fileName = textArea.getFileName();
		String extension = Utilities.getExtension(fileName);

		if (extension==null) {
			icon = defaultIcon;
		}
		else {

			// Check whether there's a special icon for this file extension.
			String iconName = (String)ext2IconNameMap.get(extension);
			if (iconName!=null) {
				icon = (Icon)iconName2IconMap.get(iconName);
				// Load and cache the icon if it's not yet loaded.
				if (icon==null) {
					ClassLoader cl = this.getClass().getClassLoader();
					icon = new ImageIcon(cl.getResource(iconName));
					iconName2IconMap.put(iconName, icon);
				}
			}

			// No special icon?  Then use the default one.
			else { // iconName==null.
				icon = defaultIcon;
			}

		}

		return icon;//new TextAreaAwareIcon(textArea, icon);

	}


	/**
	 * Returns the singleton instance of this class.
	 *
	 * @return The singleton instance of this class.
	 */
	public static FileTypeIconManager getInstance() {
		return INSTANCE;
	}


	/**
	 * An icon capable of displaying informational "subicons" in the corners
	 * of a main icon.
	 */
	static class TextAreaAwareIcon implements Icon, PropertyChangeListener {

		private Icon icon;
		private boolean paintModifiedMarker;

		public TextAreaAwareIcon(RTextEditorPane editorPane, Icon icon) {
			editorPane.addPropertyChangeListener(this);
			this.icon = icon;
		}

		public int getIconHeight() {
			return icon.getIconHeight();
		}

		public int getIconWidth() {
			return icon.getIconWidth();
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			icon.paintIcon(c, g, x,y);
			if (paintModifiedMarker) {
				g.setColor(java.awt.Color.RED);
				g.fillRect(0,0, getIconWidth(),getIconHeight());
			}
		}

		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			if (RTextEditorPane.DIRTY_PROPERTY.equals(propertyName)) {
				paintModifiedMarker = ((Boolean)e.getNewValue()).booleanValue();
			}
		}

	}


}