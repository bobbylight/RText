/*
 * 12/19/2014
 *
 * DartOptionsPanel.java - Options for Dart.
 * Copyright (C) 2014 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for Dart.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DartOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 */
	public DartOptionsPanel() {
		super("Options.Dart.Name",
				"/org/fife/rtext/graphics/file_icons/dart.png",
				SyntaxConstants.SYNTAX_STYLE_DART);
	}


}