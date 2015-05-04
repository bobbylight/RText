/*
 * 10/09/2012
 *
 * MoveFocusDownAction - Focuses the dockable window group below the currently
 * focused component.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;
import org.fife.ui.dockablewindows.DockableWindowConstants;


/**
 * Focuses the dockable window group below the currently focused component.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class MoveFocusDownAction extends StandardAction {


	/**
	 * Constructor.
	 */
	public MoveFocusDownAction(RText app, ResourceBundle msg) {
		super(app, msg, "MoveFocusBelowAction");
	}


	public void actionPerformed(ActionEvent e) {

		Component focused = KeyboardFocusManager.
				getCurrentKeyboardFocusManager().getFocusOwner();
		if (focused==null) {
			return;
		}

		RText rtext = (RText)getApplication();
		int focusedGroup = rtext.getFocusedDockableWindowGroup();
		int toFocus = -1;

		switch (focusedGroup) {
			case DockableWindowConstants.LEFT:
			case DockableWindowConstants.RIGHT:
			default: // In editor
				if (rtext.hasDockableWindowGroup(DockableWindowConstants.BOTTOM)) {
					toFocus = DockableWindowConstants.BOTTOM;
				}
				else if (rtext.hasDockableWindowGroup(DockableWindowConstants.TOP)) {
					toFocus = DockableWindowConstants.TOP;
				}
				else {
					return;
				}
				break;
			case DockableWindowConstants.TOP:
				toFocus = -1; // Focus currentTextArea
				break;
			case DockableWindowConstants.BOTTOM:
				if (rtext.hasDockableWindowGroup(DockableWindowConstants.TOP)) {
					toFocus = DockableWindowConstants.TOP;
				}
				else {
					toFocus = -1; // Focus currentTextArea
				}
				break;
		}

		if (toFocus==-1) {
			rtext.getMainView().getCurrentTextArea().requestFocusInWindow();
		}
		else {
			rtext.focusDockableWindowGroup(toFocus);
		}

	}


}