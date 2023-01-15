/*
 * 11/14/2012
 *
 * ScalaOptionsPanel.java - Options for Scala.
 * Copyright (C) 2012 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.rtext.RText;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for Scala.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ScalaOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	ScalaOptionsPanel(RText app) {
		super(app, "Options.Scala.Name", SyntaxConstants.SYNTAX_STYLE_SCALA);
	}


}
