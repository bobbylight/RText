/*
 * 10/13/2012
 *
 * NsisOptionsPanel - Options for NSIS scripts.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

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
	 */
	NsisOptionsPanel() {
		super("Options.Nsis.Name", "nsis.png",
				SyntaxConstants.SYNTAX_STYLE_NSIS);
	}


}