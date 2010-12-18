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
import javax.swing.JScrollPane;

import org.fife.ui.dockablewindows.DockableWindow;


/**
 * A dockable window that acts as a console.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ConsoleWindow extends DockableWindow {

	private Plugin plugin;
	private ConsoleTextArea textArea;


	public ConsoleWindow(Plugin plugin) {

		this.plugin = plugin;
		setDockableWindowName(plugin.getString("DockableWindow.Title"));
		setPosition(DockableWindow.BOTTOM);
		setLayout(new BorderLayout());

		textArea = new ConsoleTextArea(plugin);
		JScrollPane sp = new JScrollPane(textArea);
		add(sp);

	}


}