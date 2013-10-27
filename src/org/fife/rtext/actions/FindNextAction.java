/*
 * 11/14/2003
 *
 * FindNextAction.java - Action to search for text again in RText.
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
 * Action used by an {@link AbstractMainView} to search for text.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FindNextAction extends FindAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public FindNextAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, icon, "FindNextAction");
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		RText rtext = (RText)getApplication();
		AbstractMainView mainView = rtext.getMainView();
		mainView.getSearchManager().findNext();
	}


}