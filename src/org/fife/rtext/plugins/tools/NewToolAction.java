/*
 * 11/3/2009
 *
 * NewToolAction.java - Action that creates a new user tool
 * Copyright (C) 2009 Robert Futrell
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
package org.fife.rtext.plugins.tools;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;


/**
 * Action that creates a new user tool.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class NewToolAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public NewToolAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "NewToolAction");
		setIcon(icon);
	}


	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent e) {

		RText owner = (RText)getApplication();
		NewToolDialog ntd = new NewToolDialog(owner);
		ntd.setVisible(true);

		Tool tool = ntd.getTool();
		if (tool!=null) {
			ToolManager.get().addTool(tool);
		}

	}


}