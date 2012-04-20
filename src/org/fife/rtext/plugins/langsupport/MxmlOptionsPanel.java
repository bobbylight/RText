/*
 * 12/18/2011
 *
 * MxmlOptionsPanel.java - Options for MXML.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for MXML.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class MxmlOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 */
	public MxmlOptionsPanel() {
		super("Options.Mxml.Name", "mxml.png",
				SyntaxConstants.SYNTAX_STYLE_MXML);
	}


}