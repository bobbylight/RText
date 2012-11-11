/*
 * 11/14/2003
 *
 * GoToAction.java - Action to "goto" a specific line number in RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;

import org.fife.rsta.ui.GoToDialog;
import org.fife.rtext.AbstractMainView;
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

		if (mainView.goToDialog==null) {
			mainView.goToDialog = new GoToDialog(rtext);
			mainView.goToDialog.setErrorDialogTitle(
					rtext.getString("ErrorDialogTitle"));
		}

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