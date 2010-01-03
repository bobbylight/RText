/*
 * 01/02/2010
 *
 * ToggleTextModeAction.java - Toggles the text mode (insert/overwrite) for
 * all open text editors.
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This file is a part of RText.
 *
 * RText is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
	public String getMacroID() {
		return getName();
	}


}