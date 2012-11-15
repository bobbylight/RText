/*
 * 11/14/2012
 *
 * ScalaOptionsPanel.java - Options for Scala.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for Scala.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ScalaOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 */
	public ScalaOptionsPanel() {
		super("Options.Scala.Name", "scala.png",
				SyntaxConstants.SYNTAX_STYLE_SCALA);
	}


}