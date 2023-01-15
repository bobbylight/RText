/*
 * IncreaseFontSizeAction - Increases font sizes used in RText.
 * Copyright (C) 2013 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.Font;
import java.util.ResourceBundle;

import org.fife.rtext.RText;


/**
 * Increases the font sizes used in this RText instance.
 *
 * @author Robert Futrell
 * @version 1.1
 */
class IncreaseFontSizeAction extends AbstractFontSizeAction {


	IncreaseFontSizeAction(RText app, ResourceBundle msg) {
		super(app, msg, "IncreaseFontSizesAction");
	}


	@Override
	protected Font updateFontSize(Font font) {
		float size = font.getSize2D();
		size = Math.min(size+1, MAXIMUM_SIZE);
		return font.deriveFont(size);
	}


}
