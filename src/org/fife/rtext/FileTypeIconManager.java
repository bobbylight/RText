/*
 * 08/23/2005
 *
 * FileTypeIconManager.java - Associates an icon with a file type.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.Component;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


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
	private Map<String, String> type2IconNameMap;

	/**
	 * The map of icon names to icons.
	 */
	private Map<String, Icon> iconName2IconMap;

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

		type2IconNameMap = new HashMap<String, String>();
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_C,				PATH + "c.gif");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_CLOJURE,			PATH + "clojure.png");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS,		PATH + "cpp.gif");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_CSHARP,			PATH + "cs.gif");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_CSS,				PATH + "css.png");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_HTML,				PATH + "html.png");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_JAVA,				PATH + "java.png");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT,		PATH + "script_code.png");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_PERL,				PATH + "epic.gif");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_PHP,				PATH + "page_white_php.png");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_SAS,				PATH + "sas.gif");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_SCALA,			PATH + "scala.png");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL,		PATH + "page_white_tux.png");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH,	PATH + "bat.gif");
		type2IconNameMap.put(SyntaxConstants.SYNTAX_STYLE_XML,				PATH + "xml.png");

		iconName2IconMap = new HashMap<String, Icon>();

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
		String style = textArea.getSyntaxEditingStyle();

		if (style==null) { // Never happens
			icon = defaultIcon;
		}
		else {

			// Check whether there's a special icon for this file extension.
			String iconName = type2IconNameMap.get(style);
			if (iconName!=null) {
				icon = iconName2IconMap.get(iconName);
				// Load and cache the icon if it's not yet loaded.
				if (icon==null) {
					ClassLoader cl = this.getClass().getClassLoader();
					icon = new ImageIcon(cl.getResource(iconName));
					iconName2IconMap.put(iconName, icon);
				}
			}

			// No special icon?  Then use the default one.
			else {
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
	public static FileTypeIconManager get() {
		return INSTANCE;
	}


	/**
	 * Sets the icon to use for a specific syntax style.  This allows plugins
	 * adding language support for specific file types to set the icon to use
	 * for the relevant files.
	 *
	 * @param syntaxStyle The syntax style.
	 * @param icon The icon to use.
	 */
	public void setIconFor(String syntaxStyle, Icon icon) {
		type2IconNameMap.put(syntaxStyle, syntaxStyle);
		iconName2IconMap.put(syntaxStyle, icon);
	}


	/**
	 * An icon capable of displaying informational "sub-icons" in the corners
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