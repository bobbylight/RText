/*
 * 09/05/2012
 *
 * ClojureOptionsPanel.java - Options for Clojure.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for Clojure.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ClojureOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 */
	public ClojureOptionsPanel() {
		super("Options.Clojure.Name", "clojure.png",
				SyntaxConstants.SYNTAX_STYLE_CLOJURE);
	}


}