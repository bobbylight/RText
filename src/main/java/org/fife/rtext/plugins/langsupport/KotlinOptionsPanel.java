/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

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
	 */
	KotlinOptionsPanel() {
		super("Options.Kotlin.Name", "/org/fife/rtext/graphics/file_icons/kotlin.svg",
				SyntaxConstants.SYNTAX_STYLE_KOTLIN);
	}


}
