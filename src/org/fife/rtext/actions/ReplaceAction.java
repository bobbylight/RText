/*
 * 11/14/2003
 *
 * ReplaceAction.java - Action to display the Replace dialog in RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;


/**
 * Action used by an <code>AbstractMainView</code> to replace text with new text.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ReplaceAction extends FindAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public ReplaceAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, icon, "ReplaceAction");
	}


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 * @param nameKey The key for localizing the name of this action.
	 */
	protected ReplaceAction(RText owner, ResourceBundle msg, Icon icon,
							String nameKey) {
		super(owner, msg, icon, nameKey);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		RText rtext = (RText)getApplication();
		AbstractMainView mainView = rtext.getMainView();
		mainView.getSearchManager().showReplaceUI();
	}


}