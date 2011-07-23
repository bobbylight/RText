/*
 * 07/23/2011
 *
 * EditMacrosAction.java - Action that opens the Options dialog to the "macros"
 * panel.
 * Copyright (C) 2011 Robert Futrell
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
package org.fife.rtext.plugins.macros;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;

import org.fife.rtext.RText;
import org.fife.ui.OptionsDialog;
import org.fife.ui.app.StandardAction;


/**
 * Action that opens the Options dialog to the "Macros" panel.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class EditMacrosAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 */
	public EditMacrosAction(RText owner, ResourceBundle msg) {
		super(owner, msg, "EditMacrosAction");
		setIcon(new ImageIcon(getClass().getResource("cog_edit.png")));
	}


	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent e) {
		RText owner = (RText)getApplication();
		OptionsDialog od = owner.getOptionsDialog();
		ResourceBundle msg = MacroPlugin.msg;
		od.setSelectedOptionsPanel(msg.getString(MacroOptionPanel.TITLE_KEY));
		od.initialize();
		od.setVisible(true);
	}


}