/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.rtext.RText;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for Kotlin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class KotlinOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	KotlinOptionsPanel(RText app) {
		super(app, "Options.Kotlin.Name", SyntaxConstants.SYNTAX_STYLE_KOTLIN);
	}


}
