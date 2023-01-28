/*
 * 07/23/2011
 *
 * EditMacrosAction.java - Action that opens the Options dialog to the "macros"
 * panel.
 * Copyright (C) 2011 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.macros;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.fife.rtext.RText;
import org.fife.ui.OptionsDialog;
import org.fife.ui.app.AppAction;
import org.fife.ui.app.themes.NativeTheme;

import javax.swing.*;


/**
 * Action that opens the Options dialog to the "Macros" panel.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class EditMacrosAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 */
	EditMacrosAction(RText owner, ResourceBundle msg) {
		super(owner, msg, "EditMacrosAction");
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		RText owner = getApplication();
		OptionsDialog od = owner.getOptionsDialog();
		ResourceBundle msg = MacroPlugin.MSG;
		od.setSelectedOptionsPanel(msg.getString(MacroOptionPanel.TITLE_KEY));
		od.initialize();
		od.setVisible(true);
	}


	void restoreDefaultIcon() {

		if (NativeTheme.ID.equals(getApplication().getTheme().getId())) {
			setIcon("eclipse/cog.png");
			setRolloverIcon((Icon)null);
		}

		// In flat themes, cog_add === just a cog, so show no icon for this action
		else {
			setIcon((Icon)null);
			setRolloverIcon((Icon)null);
		}
	}


}
