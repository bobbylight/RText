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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.fife.rtext.RText;
import org.fife.rtext.RTextMenuBar;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.StandardAction;


/**
 * A plugin that allows the user to have a command prompt embedded in RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Plugin implements org.fife.ui.app.Plugin {

	private static final String VERSION				= "1.5.0";

	private RText app;
	private ConsoleWindow window;
	private Icon icon;

	private static final String MSG = "org.fife.rtext.plugins.console.Plugin";
	protected static final ResourceBundle msg = ResourceBundle.getBundle(MSG);

	private static final String VIEW_CONSOLE_ACTION	= "viewConsoleAction";


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	public Plugin(AbstractPluggableGUIApplication app) {

		this.app = (RText)app;

		// Load the plugin icon.
		URL url = getClass().getResource("monitor.png");
		if (url!=null) { // Should always be true
			try {
				icon = new ImageIcon(ImageIO.read(url));
			} catch (IOException ioe) {
				app.displayException(ioe);
			}
		}

		ConsolePrefs prefs = loadPrefs();

		StandardAction a = new ViewConsoleAction(this.app, msg, this);
		a.setAccelerator(prefs.windowVisibilityAccelerator);
		this.app.addAction(VIEW_CONSOLE_ACTION, a);

		window = new ConsoleWindow(this.app, this);
		window.setPosition(prefs.windowPosition);
		window.setActive(prefs.windowVisible);

		window.setForeground(ConsoleTextArea.STYLE_EXCEPTION, prefs.exceptionFG);
		window.setForeground(ConsoleTextArea.STYLE_PROMPT, prefs.promptFG);
		window.setForeground(ConsoleTextArea.STYLE_STDERR, prefs.stderrFG);
		window.setForeground(ConsoleTextArea.STYLE_STDOUT, prefs.stdoutFG);

	}


	/**
	 * Returns the dockable window containing the consoles.
	 *
	 * @return The dockable window.
	 */
	public ConsoleWindow getDockableWindow() {
		return window;
	}


	/**
	 * {@inheritDoc}
	 */
	public PluginOptionsDialogPanel getOptionsDialogPanel() {
		return new ConsoleOptionPanel(this);
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
	 * Returns the file preferences for this plugin are saved in.
	 *
	 * @return The file.
	 */
	private File getPrefsFile() {
		return new File(RTextUtilities.getPreferencesDirectory(),
						"console.properties");
	}


	/**
	 * Returns the parent application.
	 *
	 * @return The parent application.
	 */
	public RText getRText() {
		return app;
	}


	/**
	 * Returns a localized message.
	 *
	 * @param key The key.
	 * @return The localized message.
	 * @see #getString(String, String)
	 * @see #getString(String, String, String)
	 */
	public String getString(String key) {
		return msg.getString(key);
	}


	/**
	 * Returns a localized message.
	 *
	 * @param key The key.
	 * @param param A parameter for the localized message.
	 * @return The localized message.
	 * @see #getString(String)
	 * @see #getString(String, String, String)
	 */
	public String getString(String key, String param) {
		String temp = msg.getString(key);
		return MessageFormat.format(temp, new String[] { param });
	}


	/**
	 * Returns a localized message.
	 *
	 * @param key The key.
	 * @param param1 A parameter for the localized message.
	 * @param param2 A parameter for the localized message.
	 * @return The localized message.
	 * @see #getString(String)
	 * @see #getString(String, String)
	 */
	public String getString(String key, String param1, String param2) {
		String temp = msg.getString(key);
		return MessageFormat.format(temp, new String[] { param1, param2 });
	}


	public void install(AbstractPluggableGUIApplication app) {

		RText rtext = (RText)app;
		RTextMenuBar mb = (RTextMenuBar)app.getJMenuBar();

		// Add an item to the "View" menu to toggle console visibility
		final JMenu menu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		Action a = rtext.getAction(VIEW_CONSOLE_ACTION);
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
				item.setSelected(isConsoleWindowVisible());
			}
		});

		window.clearConsoles(); // Needed to pick up styles
		rtext.addDockableWindow(window);

	}


	/**
	 * Returns whether the console window is visible.
	 *
	 * @return Whether the console window is visible.
	 * @see #setConsoleWindowVisible(boolean)
	 */
	boolean isConsoleWindowVisible() {
		return window!=null && window.isActive();
	}


	/**
	 * Loads saved preferences.  If this is the first time through, default
	 * values will be returned.
	 *
	 * @return The preferences.
	 */
	private ConsolePrefs loadPrefs() {
		ConsolePrefs prefs = new ConsolePrefs();
		File prefsFile = getPrefsFile();
		if (prefsFile.isFile()) {
			try {
				prefs.load(prefsFile);
			} catch (IOException ioe) {
				app.displayException(ioe);
				// (Some) defaults will be used
			}
		}
		return prefs;
	}


	/**
	 * {@inheritDoc}
	 */
	public void savePreferences() {

		ConsolePrefs prefs = new ConsolePrefs();
		prefs.windowPosition = window.getPosition();
		StandardAction a = (StandardAction)app.getAction(VIEW_CONSOLE_ACTION);
		prefs.windowVisibilityAccelerator = a.getAccelerator();
		prefs.windowVisible = window.isActive();

		prefs.exceptionFG = window.getForeground(ConsoleTextArea.STYLE_EXCEPTION);
		prefs.promptFG = window.getForeground(ConsoleTextArea.STYLE_PROMPT);
		prefs.stderrFG = window.getForeground(ConsoleTextArea.STYLE_STDERR);
		prefs.stdoutFG = window.getForeground(ConsoleTextArea.STYLE_STDOUT);

		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			app.displayException(ioe);
		}

	}


	/**
	 * Sets the visibility of the console window.
	 *
	 * @param visible Whether the window should be visible.
	 * @see #isConsoleWindowVisible()
	 */
	void setConsoleWindowVisible(boolean visible) {
		if (visible!=isConsoleWindowVisible()) {
			if (visible && window==null) {
				window = new ConsoleWindow(app, this);
				app.addDockableWindow(window);
			}
			window.setActive(visible);
		}
	}


	/**
	 * Stops the currently running process, if any.
	 */
	public void stopCurrentProcess() {
		window.stopCurrentProcess();
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean uninstall() {
		return true;
	}


}