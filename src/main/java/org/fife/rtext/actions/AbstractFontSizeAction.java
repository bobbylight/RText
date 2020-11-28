/*
 * AbstractFontSizeAction - Base class for increase/decrease font size actions.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.UIManager;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.app.AppAction;


/**
 * A base class for actions that manipulate the current font size.
 *
 * @author Robert Futrell
 * @version 1.1
 */
abstract class AbstractFontSizeAction extends AppAction<RText> {

	protected static final float MINIMUM_SIZE = 2f;
	protected static final float MAXIMUM_SIZE = 40f;


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	AbstractFontSizeAction(RText app, ResourceBundle msg, String keyRoot) {
		super(app, msg, keyRoot);
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		RText rtext = getApplication();
		AbstractMainView view = rtext.getMainView();

		Font oldFont = view.getTextAreaFont();
		Font font = updateFontSize(oldFont);
		if (font.getSize2D()==oldFont.getSize2D()) {
			UIManager.getLookAndFeel().provideErrorFeedback(rtext);
			return;
		}

		// This updates the "base font" and the (shared) syntax scheme for
		// all text areas.
		view.setTextAreaFont(font, view.getTextAreaUnderline());

	}


	/**
	 * Subclasses either increase or decrease the font size in this method.
	 *
	 * @param font The font to look at.
	 * @return A font identical to <code>font</code>, but with a different
	 *         font size.
	 */
	protected abstract Font updateFontSize(Font font);


}
