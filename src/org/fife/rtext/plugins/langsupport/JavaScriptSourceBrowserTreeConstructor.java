/*
 * 02/19/2012
 *
 * JavaScriptSourceBrowserTreeConstructor.java - "Plug-in" for the SourceBrowser
 * plug-in that creates the tree view to use for JavaScript source code.
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

import org.fife.rsta.ac.js.tree.JavaScriptOutlineTree;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;


/**
 * Constructs the source browser tree for JS source files.  This is a more
 * sophisticated one than the default one created from ctags output.  Features
 * include:
 * 
 * <ul>
 *    <li>Informative icons for each node that describe the access modifiers of
 *        an element (public/protected/private, static, final, etc.) and
 *        type (method, field, etc.), just like Eclipse.</li>
 *    <li>Members are grouped under their parent classes to show logical
 *        structure.</li>
 *    <li>Local variables are added.</li>
 *    <li>The tree automatically updates after a small delay whenever the user
 *        types - no need to wait for them to save the file.</li>
 * </ul>
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public class JavaScriptSourceBrowserTreeConstructor {


	public JTree constructSourceBrowserTree(RText rtext) {
		RTextEditorPane textArea = rtext.getMainView().getCurrentTextArea();
		JavaScriptOutlineTree tree = new JavaScriptOutlineTree(false);
		tree.listenTo(textArea);
		return tree;
	}


}