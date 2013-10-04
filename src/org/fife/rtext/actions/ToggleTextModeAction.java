/*
 * 01/02/2010
 *
 * ToggleTextModeAction.java - Toggles the text mode (insert/overwrite) for
 * all open text editors.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.rtext.StatusBar;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextAreaEditorKit;
import org.fife.ui.rtextarea.RecordableTextAction;


/**
 * Action that toggles the text mode (insert vs. overwrite) for all open text
 * editors.  This class extends <code>RecordableTextAction</code> so that it
 * it is recorded in macros.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ToggleTextModeAction extends RecordableTextAction {

	private RText rtext;


	/**
	 * Constructor.
	 *
	 * @param rtext The parent application.
	 */
	public ToggleTextModeAction(RText rtext) {
		super(RTextAreaEditorKit.rtaToggleTextModeAction);
		this.rtext = rtext;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformedImpl(ActionEvent e, RTextArea textArea) {
		StatusBar statusBar = (StatusBar)rtext.getStatusBar();
		AbstractMainView view = rtext.getMainView();
		boolean isInsertMode = view.getTextMode()==RTextEditorPane.INSERT_MODE;
		statusBar.setOverwriteModeIndicatorEnabled(isInsertMode);
		view.setTextMode(isInsertMode ? RTextEditorPane.OVERWRITE_MODE :
										RTextEditorPane.INSERT_MODE);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMacroID() {
		return getName();
	}


}