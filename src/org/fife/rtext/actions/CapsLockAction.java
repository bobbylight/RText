/*
 * 01/02/2010
 *
 * CapsLockAction.java - Toggles the state of the caps lock indicator in the
 * status bar.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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
		super(rtext, rtext.getResourceBundle(), "NotUsed");
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