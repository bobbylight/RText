/*
 * 11/08/2009
 *
 * ViewTasksAction - Toggles visibility of the "Tasks" dockable window.
 * Copyright (C) 2009 Robert Futrell
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
package org.fife.rtext.plugins.tasks;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;


/**
 * Toggles the display of the "Tasks" dockable window.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ViewTasksAction extends StandardAction {

	/**
	 * The tasks plugin.
	 */
	private TasksPlugin plugin;


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param plugin The tasks plugin.
	 */
	public ViewTasksAction(final RText owner, ResourceBundle msg,
							TasksPlugin plugin) {
		super(owner, msg, "ViewTasksAction");
		this.plugin = plugin;
	}


	/**
	 * Called when this action is performed.
	 *
	 * @param e The event.
	 */
	public void actionPerformed(ActionEvent e) {
		plugin.toggleTaskWindowVisible();
	}


}