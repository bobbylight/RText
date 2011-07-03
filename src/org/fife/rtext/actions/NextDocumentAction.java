/*
 * 07/03/2011
 *
 * NextDocumentAction.java - Changes focus to the next, or previous, document.
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
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;


/**
 * Moves focus to the next (or previous) document.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class NextDocumentAction extends StandardAction {

	private boolean forward;


	/**
	 * Constructor.
	 *
	 * @param app
	 * @param msg
	 * @param forward
	 */
	public NextDocumentAction(RText app, ResourceBundle msg, boolean forward) {
		super(app, msg, forward ? "NextDocumentAction" : "PrevDocumentAction");
		this.forward = forward;
	}


	public void actionPerformed(ActionEvent e) {

		AbstractMainView view = ((RText)getApplication()).getMainView();
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