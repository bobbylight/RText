/*
 * 08/27/2011
 *
 * StopAction.java - Stops the currently running tool.
 * Copyright (C) 2011 Robert Futrell
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
package org.fife.rtext.plugins.tools;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;

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
		setIcon(new ImageIcon(getClass().getResource("stop.png")));
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