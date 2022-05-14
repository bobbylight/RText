/*
 * 12/17/2010
 *
 * StopAction.java - Stops the currently running process, if any.
 * Copyright (C) 2010 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.console;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.fife.rtext.RText;
import org.fife.ui.app.AppAction;


/**
 * Stops the currently running process, if any.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class StopAction extends AppAction<RText> {

	/**
	 * The parent plugin.
	 */
	private final Plugin plugin;


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param plugin The parent plugin.
	 */
	StopAction(RText owner, ResourceBundle msg, Plugin plugin) {

		super(owner, msg, "Action.StopProcess");
		this.plugin = plugin;
		updateIcon();
		setEnabled(false);

		plugin.getApplication().addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, e -> updateIcon());
	}


	/**
	 * Called when this action is performed.
	 *
	 * @param e The event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		plugin.stopCurrentProcess();
	}


	private void updateIcon() {
		setIcon(plugin.getApplication().getIconGroup().getIcon("stop"));
	}


}
