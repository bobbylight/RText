/*
 * 05/04/2012
 *
 * LatexOptionsPanel.java - Options for LaTeX.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for LaTeX.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class LatexOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 */
	public LatexOptionsPanel() {
		super("Options.Latex.Name", "page_white_latex.png",
				SyntaxConstants.SYNTAX_STYLE_LATEX);
	}


}