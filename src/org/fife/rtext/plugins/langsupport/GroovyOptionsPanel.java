/*
 * 12/18/2011
 *
 * GroovyOptionsPanel.java - Options for Groovy.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for Groovy.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class GroovyOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 */
	public GroovyOptionsPanel() {
		super("Options.Groovy.Name", "page_white_groovy.png",
				SyntaxConstants.SYNTAX_STYLE_GROOVY);
	}


}