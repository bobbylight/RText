/*
 * IncreaseFontSizeAction - Increases font sizes used in RText.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.Font;
import java.util.ResourceBundle;

import org.fife.rtext.RText;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit;


/**
 * Increases the font sizes used in this RText instance.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class IncreaseFontSizeAction extends AbstractFontSizeAction {


	public IncreaseFontSizeAction(RText app, ResourceBundle msg) {
		super(app, new RSyntaxTextAreaEditorKit.IncreaseFontSizeAction(),
				msg, "IncreaseFontSizesAction");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Font updateFontSize(Font font) {
		float size = font.getSize2D();
		size = Math.min(size+1, MAXIMUM_SIZE);
		return font.deriveFont(size);
	}


}