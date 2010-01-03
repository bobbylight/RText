/*
 * 01/02/2010
 *
 * CapsLockAction.java - Toggles the state of the caps lock indicator in the
 * status bar.
 * Copyright (C) 2010 Robert Futrell
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
import java.awt.event.KeyEvent;

import org.fife.rtext.RText;
import org.fife.rtext.StatusBar;
import org.fife.ui.app.StandardAction;


/**
 * Action called when the caps lock key is pressed in a text area.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class CapsLockAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param rtext The parent application.
	 */
	public CapsLockAction(RText rtext) {
		super(rtext, "NotUsed");
	}


	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent e) {
		RText rtext = (RText)getApplication();
		if (rtext.getOS()!=RText.OS_MAC_OSX) {
			try {
				boolean state = rtext.getToolkit().getLockingKeyState(
										KeyEvent.VK_CAPS_LOCK);
				StatusBar statusBar = (StatusBar)rtext.getStatusBar();
				statusBar.setCapsLockIndicatorEnabled(state);
			} catch (UnsupportedOperationException uoe) {
				// Swallow; some OS's (OS X, some Linux) just
				// don't support this.
			}
		}
	}


}