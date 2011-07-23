/*
 * 07/23/2011
 *
 * NewMacroAction.java - Action that creates a new macro.
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
import org.fife.ui.app.StandardAction;


/**
 * Action that creates a new macro.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class NewMacroAction extends StandardAction {

	/**
	 * The parent plugin.
	 */
	private MacroPlugin plugin;


	/**
	 * Constructor.
	 *
	 * @param plugin The parent plugin.
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 */
	public NewMacroAction(MacroPlugin plugin, RText owner, ResourceBundle msg) {
		super(owner, msg, "NewMacroAction");
		setIcon(new ImageIcon(getClass().getResource("cog_add.png")));
		this.plugin = plugin;
	}


	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent e) {

		RText owner = (RText)getApplication();
		NewMacroDialog nmd = new NewMacroDialog(plugin, owner);
		nmd.setVisible(true);

		Macro macro = nmd.getMacro();
		if (macro!=null) {
			MacroManager.get().addMacro(macro);
		}

	}


}