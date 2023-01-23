/*
 * 11/3/2009
 *
 * NewToolAction.java - Action that creates a new user tool
 * Copyright (C) 2009 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.*;

import org.fife.rtext.RText;
import org.fife.ui.app.AppAction;
import org.fife.ui.app.themes.FlatLightTheme;
import org.fife.util.MacOSUtil;


/**
 * Action that creates a new user tool.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class NewToolAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	NewToolAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "NewToolAction");
		setIcon(icon);
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		RText owner = getApplication();
		NewToolDialog ntd = new NewToolDialog(owner);
		MacOSUtil.setTransparentTitleBar(ntd, true);
		ntd.setVisible(true);

		Tool tool = ntd.getTool();
		if (tool!=null) {
			ToolManager.get().addTool(tool);
		}

	}


	void restoreDefaultIcon(ToolPlugin plugin) {

		if (MacOSUtil.isMacOs()) {
			setIcon(plugin.getIcon(FlatLightTheme.ID));
			setRolloverIcon((Icon)null);
			return;
		}

		String themeId = getApplication().getTheme().getId();
		setIcon(plugin.getIcon(themeId));
		Icon rolloverIcon = FlatLightTheme.ID.equals(themeId) ?
			plugin.getIcon("white") : null;
		setRolloverIcon(rolloverIcon);
	}
}
