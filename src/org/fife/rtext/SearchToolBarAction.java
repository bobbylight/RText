/*
 * 01/16/2005
 *
 * SearchToolBarAction.java - Toggles the visibility of the QuickSearch toolbar.
 * Copyright (C) 2005 Robert Futrell
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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

import org.fife.ui.app.StandardAction;


/**
 * Action used by an <code>RText</code> to toggle the visibility of
 * the QuickSearch toolbar.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SearchToolBarAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param rtext The <code>RText</code>.
	 * @param text The text associated with the action.
	 * @param icon The icon associated with the action.
	 * @param desc The description of the action.
	 * @param mnemonic The mnemonic for the action.
	 * @param accelerator The accelerator key for the action.
	 */
	public SearchToolBarAction(RText rtext, String text, Icon icon,
				String desc, int mnemonic, KeyStroke accelerator) {
		super(rtext, text, icon, desc, mnemonic, accelerator);
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action event.
	 */
	public void actionPerformed(ActionEvent e) {
		// The source must be the "QuickSearch bar" menu item.
		JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
		RText rtext = (RText)getApplication();
		rtext.getSearchToolBar().setVisible(item.isSelected());
	}


}