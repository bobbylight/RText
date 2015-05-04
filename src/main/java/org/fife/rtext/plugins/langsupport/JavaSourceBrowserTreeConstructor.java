/*
 * 11/10/2010
 *
 * JavaSourceBrowserTreeConstructor.java - "Plug-in" for the SourceBrowser
 * plug-in that creates the tree view to use for Java source code.
 * Copyright (C) 2008 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import javax.swing.JTree;

import org.fife.rsta.ac.java.tree.JavaOutlineTree;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;


/**
 * Constructs the source browser tree for Java source files.  This is a more
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
public class JavaSourceBrowserTreeConstructor {


	public JTree constructSourceBrowserTree(RText rtext) {
		RTextEditorPane textArea = rtext.getMainView().getCurrentTextArea();
		JavaOutlineTree tree = new JavaOutlineTree(false);
		tree.listenTo(textArea);
		return tree;
	}


}