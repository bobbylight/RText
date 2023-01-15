/*
 * 12/14/2015
 *
 * Copyright (C) 2015 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.rtext.RText;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for TypeScript.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class TypeScriptOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	TypeScriptOptionsPanel(RText app) {
		super(app, "Options.TypeScript.Name",
			SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT);
	}


}
