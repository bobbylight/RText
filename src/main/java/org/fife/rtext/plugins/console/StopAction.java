/*
 * 12/17/2010
 *
 * StopAction.java - Stops the currently running process, if any.
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
 * Stops the currently running process, if any.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class StopAction extends StandardAction {

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
	public StopAction(RText owner, ResourceBundle msg, Plugin plugin) {
		super(owner, msg, "Action.StopProcess");
		setIcon("stop.png");
		setEnabled(false);
		this.plugin = plugin;
	}


	/**
	 * Called when this action is performed.
	 *
	 * @param e The event.
	 */
	public void actionPerformed(ActionEvent e) {
		plugin.stopCurrentProcess();
	}


}