/*
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.rtext.RText;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for Rust.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class RustOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	RustOptionsPanel(RText app) {
		super(app, "Options.Rust.Name", SyntaxConstants.SYNTAX_STYLE_RUST);
	}


}
