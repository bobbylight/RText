/*
 * 11/14/2003
 *
 * OptionsAction - Action to display the Options dialog in RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;


/**
 * Action used by an {@link AbstractMainView} to display the options dialog.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class OptionsAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public OptionsAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "OptionsAction");
		setIcon(icon);
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action event.
	 */
	public void actionPerformed(ActionEvent e) {

		RText rtext = (RText)getApplication();
		rtext.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		org.fife.ui.OptionsDialog od = null;

		try {
			od = rtext.getOptionsDialog();
			od.initialize();
		} finally {
			// Make sure cursor returns to normal.
			rtext.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		od.setVisible(true); // Takes care of updating values itself.

	}


}