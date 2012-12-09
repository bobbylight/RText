/*
 * 08/27/2011
 *
 * StopAction.java - Stops the currently running tool.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.fife.ui.app.StandardAction;


/**
 * Stops the currently running tool.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class StopAction extends StandardAction {

	/**
	 * The parent plugin.
	 */
	private ToolPlugin plugin;


	/**
	 * Constructor.
	 *
	 * @param plugin The parent plugin.
	 * @param msg The resource bundle to use for localization.
	 */
	public StopAction(ToolPlugin plugin, ResourceBundle msg) {
		super(plugin.getRText(), msg, "Action.StopTool");
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
		Tool tool = plugin.getActiveTool();
		if (tool!=null) { // Should always be true
			tool.kill();
		}
	}


}