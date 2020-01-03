/*
 * 12/17/2010
 *
 * Plugin.java - Embeds a console-line window in RText.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.console;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
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
import org.fife.ui.ImageTranscodingUtil;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.GUIPlugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.AppAction;


/**
 * A plugin that allows the user to have a command prompt embedded in RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Plugin extends GUIPlugin {

	private static final String VERSION					= "3.0.3";
	private static final String DOCKABLE_WINDOW_CONSOLE	= "consoleDockableWindow";

	private final RText app;
	private boolean highlightInput;
	private ConsoleWindow window;
	private Icon darkThemeIcon;
	private Icon lightThemeIcon;

	private static final String MSG_BUNDLE = "org.fife.rtext.plugins.console.Plugin";
	static final ResourceBundle MSG = ResourceBundle.getBundle(MSG_BUNDLE);

	private static final String VIEW_CONSOLE_ACTION	= "viewConsoleAction";


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	public Plugin(AbstractPluggableGUIApplication<?> app) {

		this.app = (RText)app;

		loadIcons();

		ConsolePrefs prefs = loadPrefs();
		setSyntaxHighlightInput(prefs.syntaxHighlightInput);

		AppAction<RText> a = new ViewConsoleAction(this.app, MSG, this);
		a.setAccelerator(prefs.windowVisibilityAccelerator);
		app.addAction(VIEW_CONSOLE_ACTION, a);

		// Window MUST always be created for preference saving on shutdown
		window = new ConsoleWindow(this.app, this);
		window.setPosition(prefs.windowPosition);
		window.setActive(prefs.windowVisible);
		putDockableWindow(DOCKABLE_WINDOW_CONSOLE, window);

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


	@Override
	public PluginOptionsDialogPanel<Plugin> getOptionsDialogPanel() {
		return new ConsoleOptionPanel(this);
	}


	@Override
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	@Override
	public Icon getPluginIcon(boolean darkLookAndFeel) {
		return darkLookAndFeel ? darkThemeIcon : lightThemeIcon;
	}


	@Override
	public String getPluginName() {
		return MSG.getString("Plugin.Name");
	}


	@Override
	public String getPluginVersion() {
		return VERSION;
	}


	/**
	 * Returns the file preferences for this plugin are saved in.
	 *
	 * @return The file.
	 */
	private static File getPrefsFile() {
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
	 * @param params Any parameters for the message.
	 * @return The localized message.
	 */
	public String getString(String key, String... params) {
		String temp = MSG.getString(key);
		//noinspection RedundantCast
		return MessageFormat.format(temp, (Object[])params);
	}


	/**
	 * Returns whether user input is syntax highlighted.
	 *
	 * @return Whether user input is syntax highlighted.
	 * @see #setSyntaxHighlightInput(boolean)
	 */
	boolean getSyntaxHighlightInput() {
		return highlightInput;
	}


	@Override
	public void install(AbstractPluggableGUIApplication<?> app) {

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
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				item.setSelected(isConsoleWindowVisible());
			}
		});

		window.clearConsoles(); // Needed to pick up styles

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


	private void loadIcons() {

		try {

			lightThemeIcon = new ImageIcon(getClass().getResource("monitor.png"));

			Image darkThemeImage = ImageTranscodingUtil.rasterize("console dark",
				getClass().getResourceAsStream("console_dark.svg"), 16, 16);
			darkThemeIcon = new ImageIcon(darkThemeImage);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
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
	 * Changes all consoles to use the default colors for the current
	 * application theme.
	 */
	void restoreDefaultColors() {
		window.restoreDefaultColors();
	}


	@Override
	public void savePreferences() {

		ConsolePrefs prefs = new ConsolePrefs();
		prefs.syntaxHighlightInput = getSyntaxHighlightInput();
		prefs.windowPosition = window.getPosition();
		AppAction<?> a = (AppAction<?>)app.getAction(VIEW_CONSOLE_ACTION);
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
	 * Toggles whether user input should be syntax highlighted.
	 *
	 * @param highlightInput Whether to syntax highlight user input.
	 * @see #getSyntaxHighlightInput()
	 */
	void setSyntaxHighlightInput(boolean highlightInput) {
		if (highlightInput!=this.highlightInput) {
			this.highlightInput = highlightInput;
			if (window!=null) {
				window.setSyntaxHighlightInput(highlightInput);
			}
		}
	}


	/**
	 * Stops the currently running process, if any.
	 */
	void stopCurrentProcess() {
		window.stopCurrentProcess();
	}


	@Override
	public boolean uninstall() {
		return true;
	}


}
