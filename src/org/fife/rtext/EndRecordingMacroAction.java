/*
 * 09/15/2004
 *
 * EndRecordingMacroAction.java - Action to end recording a macro in RText.
 * Copyright (C) 2004 Robert Futrell
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
package org.fife.rtext;

import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextAreaEditorKit;


/**
 * An extension of the default "end recording a macro" action that disables
 * the "Recording" indicator in the status bar, as well as any other necessary
 * configuration that is RText (i.e., entire application) specific.
 *
 * @author Robert Futrell
 * @version 0.5
 */
class EndRecordingMacroAction
			extends RTextAreaEditorKit.EndRecordingMacroAction {

	/**
	 * The RText application that owns the RTextAreas recording this macro.
	 */
	private RText rtext;


	/**
	 * Constructor.
	 *
	 * @param text The text associated with the action.
	 * @param icon The icon associated with the action.
	 * @param desc The description of the action.
	 * @param mnemonic The mnemonic for the action.
	 * @param accelerator The accelerator key for the action.
	 * @param rtext The RText application in which the text area resides.
	 */
	public EndRecordingMacroAction(String text, ImageIcon icon, String desc,
					Integer mnemonic, KeyStroke accelerator, RText rtext) {
		super(text, icon, desc, mnemonic, accelerator);
		this.rtext = rtext;
	}


	/**
	 * Called when the user ends recording a macro.
	 *
	 * @param e The action event performed.
	 * @param textArea The text area on which the action was performed.
	 */
	public void actionPerformedImpl(ActionEvent e, RTextArea textArea) {

		try {

			// Don't do anything if we're not even recording a macro.
			if (RTextArea.isRecordingMacro()) {

				super.actionPerformedImpl(e, textArea);
				((StatusBar)rtext.getStatusBar()).
								setRecIndicatorEnabled(false);

				// Get whether the macro was temporary.
				String property = System.getProperty(
								RTextUtilities.MACRO_TEMPORARY_PROPERTY);
				boolean isTemporary = (property==null ? true :
								(property.equals("true")));

				// If this macro wasn't temporary, prompt the user to save it.
				if (!isTemporary) {
					SaveMacroDialog smd = new SaveMacroDialog(rtext);
					smd.setVisible(true);
				}

			}

		// No matter what happens, be sure to set the cursor back to the
		// default on all text areas.
		} finally {
			rtext.getMainView().setRecordingMacro(false);
		}

	}


}