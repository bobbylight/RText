/*
 * 07/03/2011
 *
 * NextDocumentAction.java - Changes focus to the next, or previous, document.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.app.AppAction;


/**
 * Moves focus to the next (or previous) document.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class NextDocumentAction extends AppAction<RText> {

	private boolean forward;


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 * @param msg The resource bundle to localize from.
	 * @param forward Whether we're going to the next or previous document.
	 */
	public NextDocumentAction(RText app, ResourceBundle msg, boolean forward) {
		super(app, msg, forward ? "NextDocumentAction" : "PrevDocumentAction");
		this.forward = forward;
	}


	public void actionPerformed(ActionEvent e) {

		AbstractMainView view = getApplication().getMainView();
		int currentTab = view.getSelectedIndex();

		int tab = 0;
		if (forward) {
			tab = (currentTab+1) % view.getNumDocuments();
		}
		else {
			tab = currentTab - 1;
			if (tab<0) {
				tab = view.getNumDocuments() - 1;
			}
		}

		if (tab!=currentTab) {
			view.setSelectedIndex(tab);
		}

	}


}