/*
 * 10/13/2012
 *
 * NsisOptionsPanel - Options for NSIS scripts.
 * Copyright (C) 2012 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.rtext.RText;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for NSIS scripts.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class NsisOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	NsisOptionsPanel(RText app) {
		super(app, "Options.Nsis.Name", SyntaxConstants.SYNTAX_STYLE_NSIS);
	}


}
