/*
 * 12/29/2012
 *
 * JsonOptionsPanel.java - Options for JSON.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for JSON.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class JsonOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 */
	public JsonOptionsPanel() {
		super("Options.JSON.Name", "json.png",
				SyntaxConstants.SYNTAX_STYLE_JSON);
	}


}