/*
 * 11/14/2003
 *
 * FindAction.java - Action for searching for text in RText.
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
import org.fife.ui.app.AppAction;


/**
 * Action used by an <code>AbstractMainView</code> to search for text.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FindAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	FindAction(RText owner, ResourceBundle msg, Icon icon) {
		this(owner, msg, icon, "FindAction");
	}


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 * @param nameKey The key for localizing the name of this action.
	 */
	FindAction(RText owner, ResourceBundle msg, Icon icon, String nameKey) {
		super(owner, msg, nameKey);
		setIcon(icon);
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action performed.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		RText rtext = getApplication();
		AbstractMainView mainView = rtext.getMainView();
		mainView.getSearchManager().showFindUI();
	}


}
