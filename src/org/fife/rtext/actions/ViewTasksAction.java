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
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.fife.rtext.RText;
import org.fife.rtext.TaskWindow;
import org.fife.ui.app.StandardAction;


/**
 * Toggles the display of the "Tasks" dockable window.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ViewTasksAction extends StandardAction {

	/**
	 * Dockable window that displays "tasks" ("TODO", "FIXME", etc.) in opened
	 * files.
	 */
	private TaskWindow taskWindow;


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 * @param visible Whether the task window should be initially visible.
	 */
	public ViewTasksAction(final RText owner, ResourceBundle msg, Icon icon,
							boolean visible) {
		super(owner, msg, "ViewTasksAction");
		setIcon(icon);
		if (visible) {
			// Defer task window creation until after entire RText GUI has been
			// instantiated to prevent NPE's.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					taskWindow = new TaskWindow(owner);
					owner.addDockableWindow(taskWindow);
				}
			});
		}
	}


	/**
	 * Called when this action is performed.
	 *
	 * @param e The event.
	 */
	public void actionPerformed(ActionEvent e) {
		if (taskWindow==null) { // First time through
			RText rtext = (RText)getApplication();
			taskWindow = new TaskWindow(rtext);
			rtext.addDockableWindow(taskWindow);
		}
		else {
			taskWindow.setActive(!taskWindow.isActive());
		}
	}


	/**
	 * Returns whether the task window is currently visible.
	 *
	 * @return Whether the task window is currently visible.
	 */
	public boolean isTaskWindowVisible() {
		return taskWindow!=null && taskWindow.isActive();
	}


}