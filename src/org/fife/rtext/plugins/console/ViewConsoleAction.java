/*
 * 12/18/2010
 *
 * ViewConsoleAction.java - Toggles visibility of the console dockable window.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.console;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;


/**
 * Toggles visibility of the console dockable window.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ViewConsoleAction extends StandardAction {

	/**
	 * The parent plugin.
	 */
	private Plugin plugin;


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param plugin The parent plugin.
	 */
	public ViewConsoleAction(RText owner, ResourceBundle msg, Plugin plugin) {
		super(owner, msg, "Action.ViewConsole");
		this.plugin = plugin;
	}


	/**
	 * Called when this action is performed.
	 *
	 * @param e The event.
	 */
	public void actionPerformed(ActionEvent e) {
		plugin.setConsoleWindowVisible(!plugin.isConsoleWindowVisible());
	}


}