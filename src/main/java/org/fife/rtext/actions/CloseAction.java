/*
 * 11/14/2003
 *
 * CloseAction.java - Action to close the current document in RText.
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
import org.fife.ui.app.StandardAction;


/**
 * Action used by an <code>AbstractMainView</code> to close the current
 * document.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class CloseAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public CloseAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "CloseAction");
		setIcon(icon);
	}


	public void actionPerformed(ActionEvent e) {
		RText owner = (RText)getApplication();
		AbstractMainView mainView = owner.getMainView();
		mainView.closeCurrentDocument();
		owner.setStatusBarReadOnlyIndicatorEnabled(mainView.
									getCurrentTextArea().isReadOnly());
	}


}