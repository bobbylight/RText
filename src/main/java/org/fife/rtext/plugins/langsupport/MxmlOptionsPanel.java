/*
 * 12/18/2011
 *
 * MxmlOptionsPanel.java - Options for MXML.
 * Copyright (C) 2011 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.rtext.RText;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for MXML.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class MxmlOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	MxmlOptionsPanel(RText app) {
		super(app, "Options.Mxml.Name", SyntaxConstants.SYNTAX_STYLE_MXML);
	}


}
