/*
 * 11/14/2003
 *
 * OptionsAction - Action to display the Options dialog in RText.
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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.optionsdialog.OptionsDialog;
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
			od = getOptionsDialog();
			od.initialize();
		} finally {
			// Make sure cursor returns to normal.
			rtext.setCursor(Cursor.getPredefinedCursor(
											Cursor.DEFAULT_CURSOR));
		}

		od.setVisible(true); // Takes care of updating values itself.

	}


	private org.fife.ui.OptionsDialog getOptionsDialog() {
		RText rtext = (RText)getApplication();
		if (rtext.optionsDialog==null) {
			rtext.optionsDialog = new OptionsDialog(rtext);
			rtext.optionsDialog.setLocationRelativeTo(rtext);
		}
		return rtext.optionsDialog;
	}


}