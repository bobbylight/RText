/*
 * 11/14/2003
 *
 * ReplaceAction.java - Action to display the Replace dialog in RText.
 * Copyright (C) 2003 Robert Futrell
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

import org.fife.ui.search.ReplaceDialog;


/**
 * Action used by an <code>AbstractMainView</code> to replace text with new text.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ReplaceAction extends FindAction {


	/**
	 * Creates a new <code>ReplaceAction</code>.
	 *
	 * @param rtext The <code>RText</code> instance.
	 * @param text The text associated with the action.
	 * @param icon The icon associated with the action.
	 * @param desc The description of the action.
	 * @param mnemonic The mnemonic for the action.
	 * @param accelerator The accelerator key for the action.
	 */
	public ReplaceAction(RText rtext, String text, Icon icon, String desc,
					int mnemonic, KeyStroke accelerator) {
		super(rtext, text, icon, desc, mnemonic, accelerator);
	}


	public void actionPerformed(ActionEvent e) {

		ensureSearchDialogsCreated();
		RText rtext = (RText)getApplication();
		AbstractMainView mainView = rtext.getMainView();

		ReplaceDialog replaceDialog = mainView.replaceDialog;
		boolean replaceDialogVisible = replaceDialog.isVisible();

		// If the dialog isn't showing, bring it up.
		if (!replaceDialogVisible && !mainView.findDialog.isVisible()) {

			replaceDialog.setSearchParameters(mainView.searchStrings,
									mainView.searchMatchCase,
									mainView.searchWholeWord,
									mainView.searchRegExpression,
									!mainView.searchingForward,
									mainView.searchMarkAll);

			// If the current document has selected text, use the selection
			// as the value to search for.
			String selectedText = mainView.currentTextArea.getSelectedText();
			if (selectedText!=null)
				replaceDialog.setSearchString(selectedText);

			replaceDialog.setVisible(true);

		}

		// If the replace dialog is already visible but not active, have
		// it request focus.
		else if (replaceDialogVisible) {
			replaceDialog.toFront();
		}

	}


}