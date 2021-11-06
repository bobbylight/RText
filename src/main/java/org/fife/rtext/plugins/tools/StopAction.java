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
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

import org.fife.rtext.RText;
import org.fife.ui.ImageTranscodingUtil;
import org.fife.ui.UIUtil;
import org.fife.ui.app.AppAction;

import javax.swing.*;


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
		initIcon();
		setEnabled(false);
		this.plugin = plugin;
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


	private void initIcon() {

		if (UIUtil.isLightForeground(new JLabel().getForeground())) {
			try {
				InputStream in = getClass().getResourceAsStream("suspend.svg");
				setIcon(new ImageIcon(ImageTranscodingUtil.rasterize("suspend.svg", in, 16, 16)));
			} catch (IOException ioe) {
				plugin.getApplication().displayException(ioe);
			}
		}

		else {
			setIcon("stop.png");
		}
	}
}
