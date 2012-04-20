/*
 * 01/16/2005
 *
 * SearchToolBarAction.java - Toggles the visibility of the QuickSearch toolbar.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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