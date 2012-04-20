/*
 * 11/10/2010
 *
 * XmlSourceBrowserTreeConstructor.java - "Plug-in" for the SourceBrowser
 * plug-in that creates the tree view to use for XML.
 * Copyright (C) 2008 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import javax.swing.JTree;

import org.fife.rsta.ac.xml.tree.XmlOutlineTree;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;


/**
 * Constructs the source browser tree for XML files.  ctags doesn't support
 * generating tags for XML, so we do it ourselves.  Elements are displayed in a
 * tree view, along with their "primary" attribute (an "id" attribute is
 * preferred; if one isn't defined, "name" is preferred.  If neither attribute
 * is defined, the first attribute on the element is used).
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public class XmlSourceBrowserTreeConstructor {


	public JTree constructSourceBrowserTree(RText rtext) {
		RTextEditorPane textArea = rtext.getMainView().getCurrentTextArea();
		XmlOutlineTree tree = new XmlOutlineTree(false);
		tree.listenTo(textArea);
		return tree;
	}


}