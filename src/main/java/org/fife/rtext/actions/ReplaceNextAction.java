/*
 * 11/14/2003
 *
 * ReplaceNextAction.java - Action in RText to replace text with new text.
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
import org.fife.ui.rtextarea.SearchContext;


/**
 * Action used by an <code>AbstractMainView</code> to replace text.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ReplaceNextAction extends ReplaceAction implements AbstractSearchAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	ReplaceNextAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, icon, "ReplaceNextAction");
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action event.
	 * @see #actionPerformed(SearchContext)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		actionPerformed((SearchContext)null);
	}


	@Override
	public void actionPerformed(SearchContext context) {
		RText rtext = getApplication();
		AbstractMainView mainView = rtext.getMainView();
		mainView.getSearchManager().replaceNext(context);
	}


}
