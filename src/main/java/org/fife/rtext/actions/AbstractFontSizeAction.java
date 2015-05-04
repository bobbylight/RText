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
import org.fife.rtext.RTextEditorPane;
import org.fife.ui.app.StandardAction;
import org.fife.ui.rtextarea.RecordableTextAction;


/**
 * A base class for actions that manipulate the current font size.
 *
 * @author Robert Futrell
 * @version 1.0
 */
abstract class AbstractFontSizeAction extends StandardAction {

	protected static final float MINIMUM_SIZE = 2f;
	protected static final float MAXIMUM_SIZE = 40f;

	/**
	 * The RSTA version of this action.
	 */
	private RecordableTextAction delegate;


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 * @param delegate The RSTA version of this increase/decrease font size
	 *        action.
	 */
	protected AbstractFontSizeAction(RText app, RecordableTextAction delegate,
			ResourceBundle msg, String keyRoot) {
		super(app, msg, keyRoot);
		this.delegate = delegate;
	}


	public void actionPerformed(ActionEvent e) {

		RText rtext = (RText)getApplication();
		AbstractMainView view = rtext.getMainView();
		RTextEditorPane textArea = view.getCurrentTextArea();

		Font oldFont = view.getTextAreaFont();
		Font font = updateFontSize(oldFont);
		if (font.getSize2D()==oldFont.getSize2D()) {
			UIManager.getLookAndFeel().provideErrorFeedback(rtext);
			return;
		}

		// First, update the "base font" of the text areas.
		view.setTextAreaFont(font, view.getTextAreaUnderline());

		// As the SyntaxScheme is shared, this will update it on all text areas
		// and the AbstractMainView.
		delegate.actionPerformedImpl(e, textArea);

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