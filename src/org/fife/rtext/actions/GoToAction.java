/*
 * 11/14/2003
 *
 * GoToAction.java - Action to "goto" a specific line number in RText.
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
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.GoToDialog;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.ui.app.StandardAction;


/**
 * Action used by an <code>AbstractMainView</code> to "goto" a specific line
 * number in the current document.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class GoToAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public GoToAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "GoToAction");
		setIcon(icon);
	}


	public void actionPerformed(ActionEvent e) {

		RText rtext = (RText)getApplication();
		AbstractMainView mainView = rtext.getMainView();

		// Initializing the dialog now saves on load time when we first bring rtext up.
		if (mainView.goToDialog==null)
			mainView.goToDialog = new GoToDialog(rtext, mainView);

		// Prepare and show the GoTo Line dialog.
		RTextEditorPane editor = mainView.getCurrentTextArea();
		mainView.goToDialog.setMaxLineNumberAllowed(editor.getLineCount());
		mainView.goToDialog.setVisible(true);

		// If a real line number is returned, go to that line number.
		int line = mainView.goToDialog.getLineNumber();
		if (line>0) {

			try {
				editor.setCaretPosition(editor.getLineStartOffset(line-1));
			} catch (BadLocationException ble) {
				String temp = rtext.getString("InternalErrorILN",
									Integer.toString(line));
				JOptionPane.showMessageDialog(rtext, temp,
									rtext.getString("ErrorDialogTitle"),
									JOptionPane.ERROR_MESSAGE);
			}

		}

	}


}