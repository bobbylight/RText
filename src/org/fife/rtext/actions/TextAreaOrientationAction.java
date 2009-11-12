/*
 * 12/05/2006
 *
 * TextAreaOrientationAction.java - Aligns text in all open editors either
 * right-to-left or left-to-right.
 * Copyright (C) 2006 Robert Futrell
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

import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;


/**
 * Action used by an <code>AbstractMainView</code> to set text alignment in
 * editors to either LTR or RTL.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class TextAreaOrientationAction extends StandardAction {

	private ComponentOrientation orientation;


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param nameKey The localization key for the name (RTL or LTR).
	 * @param icon The icon associated with the action.
	 * @param o The orientation to give text areas.
	 */
	public TextAreaOrientationAction(RText owner, ResourceBundle msg,
						String nameKey, Icon icon, ComponentOrientation o) {
		super(owner, msg, nameKey);
		setIcon(icon);
		this.orientation = o;
	}


	public void actionPerformed(ActionEvent e) {
		RText rtext = (RText)getApplication();
		AbstractMainView mainView = rtext.getMainView();
		mainView.setTextAreaOrientation(orientation);
	}


}