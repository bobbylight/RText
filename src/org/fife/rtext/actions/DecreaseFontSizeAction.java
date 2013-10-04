/*
 * DecreaseFontSizeAction - Decreases font sizes used in RText.
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
 * Decreases the font sizes used in this RText instance.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DecreaseFontSizeAction extends AbstractFontSizeAction {


	public DecreaseFontSizeAction(RText app, ResourceBundle msg) {
		super(app, new RSyntaxTextAreaEditorKit.DecreaseFontSizeAction(),
				msg, "DecreaseFontSizesAction");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Font updateFontSize(Font font) {
		float size = font.getSize2D();
		size = Math.max(MINIMUM_SIZE, size-1);
		return font.deriveFont(size);
	}


}