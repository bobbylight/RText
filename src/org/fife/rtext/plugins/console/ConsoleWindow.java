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
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
	private CardLayout cards;
	private JPanel mainPanel;
	private SystemShellTextArea shellTextArea;
	private JavaScriptShellTextArea jsTextArea;

	private JToolBar toolbar;
	private JComboBox shellCombo;
	private StopAction stopAction;


	public ConsoleWindow(RText app, Plugin plugin) {

//		this.plugin = plugin;
		setDockableWindowName(plugin.getString("DockableWindow.Title"));
		setIcon(plugin.getPluginIcon());
		setPosition(DockableWindow.BOTTOM);
		setLayout(new BorderLayout());

		Listener listener = new Listener();

		// Create the main panel, containing the shells.
		cards = new CardLayout();
		mainPanel = new JPanel(cards);
		add(mainPanel);

		shellTextArea = new SystemShellTextArea(plugin);
		shellTextArea.addPropertyChangeListener(
							ConsoleTextArea.PROPERTY_PROCESS_RUNNING, this);
		JScrollPane sp = new JScrollPane(shellTextArea);
		mainPanel.add(sp, "System");

		jsTextArea = new JavaScriptShellTextArea(plugin);
		sp = new JScrollPane(jsTextArea);
		mainPanel.add(sp, "JavaScript");

		// Create a "toolbar" for the shells.
		toolbar = new JToolBar();
		toolbar.setFloatable(false);

		JLabel label = new JLabel(plugin.getString("Shell"));
		Box temp = Box.createHorizontalBox();
		temp.add(label);
		temp.add(Box.createHorizontalStrut(5));
		shellCombo = new JComboBox();
		shellCombo.addItemListener(listener);
		shellCombo.addItem(plugin.getString("System"));
		shellCombo.addItem(plugin.getString("JavaScript"));
		temp.add(shellCombo);
		temp.add(Box.createHorizontalGlue());
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(temp, BorderLayout.LINE_START);
		toolbar.add(temp2);
		toolbar.add(Box.createHorizontalGlue());

		stopAction = new StopAction(app, Plugin.msg, plugin);
		JButton b = new JButton(stopAction);
		b.setText(null);
		toolbar.add(b);
		add(toolbar, BorderLayout.NORTH);

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
		shellTextArea.stopCurrentProcess();
	}


	/**
	 * Listens for events in this dockable window.
	 */
	private class Listener implements ItemListener {

		public void itemStateChanged(ItemEvent e) {

			JComboBox source = (JComboBox)e.getSource();
			if (source==shellCombo) {
				int index = shellCombo.getSelectedIndex();
				if (index==0) {
					cards.show(mainPanel, "System");
				}
				else {
					cards.show(mainPanel, "JavaScript");
				}
			}

		}

	}


}