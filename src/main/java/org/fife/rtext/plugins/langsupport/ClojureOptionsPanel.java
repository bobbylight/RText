/*
 * 09/05/2012
 *
 * ClojureOptionsPanel.java - Options for Clojure.
 * Copyright (C) 2012 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.rtext.RText;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for Clojure.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ClojureOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 */
	ClojureOptionsPanel(RText app) {
		super(app, "Options.Clojure.Name", SyntaxConstants.SYNTAX_STYLE_CLOJURE);
	}


}
