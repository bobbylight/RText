/*
 * 12/17/2010
 *
 * Plugin.java - Embeds a console-line window in RText.
 * Copyright (C) 2010 Robert Futrell
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
package org.fife.rtext.plugins.console;

import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.fife.rtext.RText;
import org.fife.rtext.RTextMenuBar;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.PluginOptionsDialogPanel;


/**
 * A plugin that allows the user to have a command prompt embedded in RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Plugin implements org.fife.ui.app.Plugin {

	private static final String VERSION				= "1.3.0";

	private RText app;
	private ConsoleWindow window;
	private Icon icon;

	private static final String MSG = "org.fife.rtext.plugins.console.Plugin";
	protected static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	public Plugin(AbstractPluggableGUIApplication app) {
	}


	public PluginOptionsDialogPanel getOptionsDialogPanel() {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	/**
	 * {@inheritDoc}
	 */
	public Icon getPluginIcon() {
		return icon;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getPluginName() {
		return msg.getString("Plugin.Name");
	}


	/**
	 * {@inheritDoc}
	 */
	public String getPluginVersion() {
		return VERSION;
	}


	/**
	 * Returns a localized message.
	 *
	 * @param key The key.
	 * @return The localized message.
	 */
	public String getString(String key) {
		return msg.getString(key);
	}


	public void install(AbstractPluggableGUIApplication app) {

		RText rtext = (RText)app;
		this.app = rtext;
		RTextMenuBar mb = (RTextMenuBar)app.getJMenuBar();
/*
		// Add an item to the "View" menu to toggle tool output visibility
		final JMenu menu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		a = rtext.getAction(VIEW_TOOL_OUTPUT_ACTION);
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(a);
		item.setToolTipText(null);
		item.applyComponentOrientation(app.getComponentOrientation());
		menu.add(item);
		JPopupMenu popup = menu.getPopupMenu();
		popup.pack();
		// Only needed for pre-1.6 support
		popup.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				item.setSelected(isToolOutputWindowVisible());
			}
		});
*/

		window = new ConsoleWindow(this);
		window.setActive(true);
		rtext.addDockableWindow(window);

	}


	public void savePreferences() {
		// TODO Auto-generated method stub

	}


	/**
	 * {@inheritDoc}
	 */
	public boolean uninstall() {
		return true;
	}


}