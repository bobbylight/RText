/*
 * 11/10/2010
 *
 * XmlSourceBrowserTreeConstructor.java - "Plug-in" for the SourceBrowser
 * plug-in that creates the tree view to use for XML.
 * Copyright (C) 2008 Robert Futrell
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