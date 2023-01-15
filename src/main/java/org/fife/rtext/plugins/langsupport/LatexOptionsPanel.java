/*
 * 05/04/2012
 *
 * LatexOptionsPanel.java - Options for LaTeX.
 * Copyright (C) 2012 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.rtext.RText;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for LaTeX.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class LatexOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	LatexOptionsPanel(RText app) {
		super(app, "Options.Latex.Name", SyntaxConstants.SYNTAX_STYLE_LATEX);
	}


}
