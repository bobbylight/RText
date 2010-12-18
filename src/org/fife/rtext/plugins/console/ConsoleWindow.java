/*
 * 12/17/2010
 *
 * ConsoleWindow.java - Text component for the console.
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.sfifesoft.com
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
package org.fife.rtext.plugins.console;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.fife.rtext.RText;
import org.fife.ui.dockablewindows.DockableWindow;


/**
 * A dockable window that acts as a console.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ConsoleWindow extends DockableWindow
							implements PropertyChangeListener {

//	private Plugin plugin;
	private ConsoleTextArea textArea;

	private JToolBar toolbar;
	private StopAction stopAction;


	public ConsoleWindow(RText app, Plugin plugin) {

//		this.plugin = plugin;
		setDockableWindowName(plugin.getString("DockableWindow.Title"));
		setIcon(plugin.getPluginIcon());
		setPosition(DockableWindow.BOTTOM);
		setLayout(new BorderLayout());

		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		stopAction = new StopAction(app, Plugin.msg, plugin);
		JButton b = new JButton(stopAction);
		b.setText(null);
		toolbar.add(b);
		add(toolbar, BorderLayout.NORTH);

		textArea = new ConsoleTextArea(plugin);
		textArea.addPropertyChangeListener(
							ConsoleTextArea.PROPERTY_PROCESS_RUNNING, this);
		JScrollPane sp = new JScrollPane(textArea);
		add(sp);

	}


	/**
	 * Called whenever a process starts or completes.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String prop = e.getPropertyName();

		if (ConsoleTextArea.PROPERTY_PROCESS_RUNNING.equals(prop)) {
			boolean running = ((Boolean)e.getNewValue()).booleanValue();
			stopAction.setEnabled(running);
		}

	}


	/**
	 * Stops the currently running process, if any.
	 */
	public void stopCurrentProcess() {
		textArea.stopCurrentProcess();
	}


}