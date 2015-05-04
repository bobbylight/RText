/*
 * 01/24/2010
 *
 * EditToolsAction.java - Action that opens the Options dialog to the "tools"
 * panel.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import org.fife.rtext.RText;
import org.fife.ui.OptionsDialog;
import org.fife.ui.app.StandardAction;


/**
 * Action that opens the Options dialog to the "Tools" panel.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class EditToolsAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public EditToolsAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "EditToolsAction");
		setIcon(icon);
	}


	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent e) {
		RText owner = (RText)getApplication();
		OptionsDialog od = owner.getOptionsDialog();
		ResourceBundle msg = ResourceBundle.getBundle(ToolOptionPanel.MSG);
		od.setSelectedOptionsPanel(msg.getString(ToolOptionPanel.TITLE_KEY));
		od.initialize();
		od.setVisible(true);
	}


}