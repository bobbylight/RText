/*
 * 09/15/2004
 *
 * BeginRecordingMacroAction.java - Action to begin recording a macro in RText.
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
import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextAreaEditorKit;


/**
 * An extension of the default "begin recording a macro" action that enables
 * the "Recording" indicator in the status bar, as well as any other necessary
 * configuration that is RText (i.e., entire application) specific.
 *
 * @author Robert Futrell
 * @version 0.5
 */
class BeginRecordingMacroAction
			extends RTextAreaEditorKit.BeginRecordingMacroAction {

	/**
	 * The RText application that owns the RTextAreas recording this macro.
	 */
	private RText rtext;

	/**
	 * Whether macros recorded beginning with this action are temporary
	 * (i.e., whether RText should prompt the user to save the macro after
	 * they finish recording).
	 */
	private boolean isTemporary;


	/**
	 * Constructor.
	 *
	 * @param text The text associated with the action.
	 * @param icon The icon associated with the action.
	 * @param desc The description of the action.
	 * @param mnemonic The mnemonic for the action.
	 * @param accelerator The accelerator key for the action.
	 * @param rtext The RText application in which the text area resides.
	 * @param isTemporary Whether or not the macro is temporary.
	 */
	public BeginRecordingMacroAction(String text, Icon icon, String desc,
					Integer mnemonic, KeyStroke accelerator, RText rtext,
					boolean isTemporary) {
		super(text, icon, desc, mnemonic, accelerator);
		this.rtext = rtext;
		this.isTemporary = isTemporary;
	}


	/**
	 * Called when the user begins recording a macro.
	 *
	 * @param e The action event performed.
	 * @param textArea The text area on which the action was performed.
	 */
	public void actionPerformedImpl(ActionEvent e, RTextArea textArea) {
/*
		// We check whether or not a macro is already being recorded so that
		// we don't re-assign a value to the "temporary macro" property.
		// The super's implementation does this too, but we don't want the
		// user choosing "record temporary macro," then "record regular macro"
		// and be confused as to which type they're recording.
		if (!RTextArea.isRecordingMacro()) {
			super.actionPerformedImpl(e, textArea);
			rtext.getMainView().setRecordingMacro(true);
			((StatusBar)rtext.getStatusBar()).setRecIndicatorEnabled(true);
			System.setProperty(RTextUtilities.MACRO_TEMPORARY_PROPERTY,
							isTemporary ? "true" : "false");
		}
*/

		NewMacroDialog nmd = new NewMacroDialog(rtext);
		nmd.setVisible(true);

	}


}