/*
 * 08/27/2011
 *
 * StopAction.java - Stops the currently running tool.
 * Copyright (C) 2011 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.fife.rtext.RText;
import org.fife.ui.app.AppAction;


/**
 * Stops the currently running tool.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class StopAction extends AppAction<RText> {

	/**
	 * The parent plugin.
	 */
	private final ToolPlugin plugin;


	/**
	 * Constructor.
	 *
	 * @param plugin The parent plugin.
	 * @param msg The resource bundle to use for localization.
	 */
	StopAction(ToolPlugin plugin, ResourceBundle msg) {

		super(plugin.getApplication(), msg, "Action.StopTool");
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
		Tool tool = plugin.getActiveTool();
		if (tool!=null) { // Should always be true
			tool.kill();
		}
	}


	private void updateIcon() {
		setIcon(plugin.getApplication().getIconGroup().getIcon("stop"));
	}
}
