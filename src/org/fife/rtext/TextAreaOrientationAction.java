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
package org.fife.rtext;

import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.KeyStroke;

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
	 * Creates a new <code>TextAreaOrientationAction</code>.
	 *
	 * @param rtext The parent application.
	 * @param text The text associated with the action.
	 * @param icon The icon associated with the action.
	 * @param desc The description of the action.
	 * @param mnemonic The mnemonic for the action.
	 * @param accelerator The accelerator key for the action.
	 * @param o The component orientation.
	 */
	public TextAreaOrientationAction(RText rtext, String text, Icon icon,
				String desc, int mnemonic, KeyStroke accelerator,
				ComponentOrientation o) {
		super(rtext, text, icon, desc, mnemonic, accelerator);
		this.orientation = o;
	}


	public void actionPerformed(ActionEvent e) {
		RText rtext = (RText)getApplication();
		AbstractMainView mainView = rtext.getMainView();
		mainView.setTextAreaOrientation(orientation);
	}


}