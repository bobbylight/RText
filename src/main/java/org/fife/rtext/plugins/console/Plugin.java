/*
 * 12/17/2010
 *
 * Plugin.java - Embeds a console-line window in RText.
 * Copyright (C) 2010 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.console;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import org.fife.rtext.RText;
import org.fife.rtext.RTextMenuBar;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.ImageTranscodingUtil;
import org.fife.ui.app.*;
import org.fife.ui.app.console.AbstractConsoleTextArea;
import org.fife.ui.app.icons.IconGroup;
import org.fife.ui.app.themes.*;


/**
 * A plugin that allows the user to have a command prompt embedded in RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Plugin extends GUIPlugin<RText> {

	private static final String VERSION					= "6.0.3";
	private static final String DOCKABLE_WINDOW_CONSOLE	= "consoleDockableWindow";

	private ConsoleWindow window;
	private Map<String, Icon> icons;
	private ConsoleOptionPanel optionPanel;

	private static final String MSG_BUNDLE = "org.fife.rtext.plugins.console.Plugin";
	static final ResourceBundle MSG = ResourceBundle.getBundle(MSG_BUNDLE);

	private static final String VIEW_CONSOLE_ACTION	= "viewConsoleAction";


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	public Plugin(RText app) {

		super(app);
		loadIcons();

		ConsolePrefs prefs = loadPrefs();

		AppAction<RText> a = new ViewConsoleAction(app, MSG, this);
		a.setAccelerator(prefs.windowVisibilityAccelerator);
		app.addAction(VIEW_CONSOLE_ACTION, a);

		// Window MUST always be created for preference saving on shutdown
		window = new ConsoleWindow(app, this);
		window.setPosition(prefs.windowPosition);
		window.setActive(prefs.windowVisible);
		putDockableWindow(DOCKABLE_WINDOW_CONSOLE, window);

		app.addPropertyChangeListener(AbstractGUIApplication.THEME_PROPERTY, e -> {
			restoreDefaultColors((AppTheme)e.getNewValue());
		});

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
		if (optionPanel == null) {
			optionPanel = new ConsoleOptionPanel(this);
		}
		return optionPanel;
	}


	@Override
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	@Override
	public Icon getPluginIcon() {
		return icons.get(getApplication().getTheme().getId());
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
	 * Returns a localized message.
	 *
	 * @param key The key.
	 * @param params Any parameters for the message.
	 * @return The localized message.
	 */
	public String getString(String key, String... params) {
		String temp = MSG.getString(key);
		return MessageFormat.format(temp, (Object[])params);
	}


	@Override
	public void install() {

		RText app = getApplication();
		RTextMenuBar mb = (RTextMenuBar)app.getJMenuBar();

		// Add an item to the "View" menu to toggle console visibility
		final JMenu menu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		Action a = app.getAction(VIEW_CONSOLE_ACTION);
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(a);
		item.setSelected(isConsoleWindowVisible());
		item.setToolTipText(null);
		item.applyComponentOrientation(app.getComponentOrientation());
		menu.add(item);

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


	/**
	 * Creates a map from application theme ID to icon.
	 */
	private void loadIcons() {

		icons = new HashMap<>();

		try {

			icons.put(NativeTheme.ID, new ImageIcon(getClass().getResource("eclipse/console.png")));

			Image darkThemeImage = ImageTranscodingUtil.rasterize("console dark",
				getClass().getResourceAsStream("flat-dark/console.svg"), 16, 16);
			icons.put(FlatDarkTheme.ID, new ImageIcon(darkThemeImage));
			icons.put(FlatMacDarkTheme.ID, new ImageIcon(darkThemeImage));

			Image lightThemeImage = ImageTranscodingUtil.rasterize("console light",
				getClass().getResourceAsStream("flat-light/console.svg"), 16, 16);
			icons.put(FlatLightTheme.ID, new ImageIcon(lightThemeImage));
			icons.put(FlatMacLightTheme.ID, new ImageIcon(lightThemeImage));
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
				getApplication().displayException(ioe);
				// (Some) defaults will be used
			}
		}
		return prefs;
	}


	/**
	 * Changes all consoles to use the default colors for the current
	 * application theme.
	 */
	void restoreDefaultColors(AppTheme theme) {
		window.setForeground(AbstractConsoleTextArea.STYLE_RESULT,
			(Color)theme.getExtraUiDefaults().get("rtext.console.result"));
		window.setForeground(AbstractConsoleTextArea.STYLE_PROMPT,
			(Color)theme.getExtraUiDefaults().get("rtext.console.prompt"));
		window.setForeground(AbstractConsoleTextArea.STYLE_STDOUT,
			(Color)theme.getExtraUiDefaults().get("rtext.console.stdout"));
		window.setForeground(AbstractConsoleTextArea.STYLE_STDERR,
			(Color)theme.getExtraUiDefaults().get("rtext.console.stderr"));
	}


	@Override
	public void savePreferences() {

		ConsolePrefs prefs = new ConsolePrefs();
		prefs.windowPosition = window.getPosition();
		AppAction<?> a = (AppAction<?>)getApplication().getAction(VIEW_CONSOLE_ACTION);
		prefs.windowVisibilityAccelerator = a.getAccelerator();
		prefs.windowVisible = window.isActive();

		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			getApplication().displayException(ioe);
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
				window = new ConsoleWindow(getApplication(), this);
				getApplication().addDockableWindow(window);
			}
			window.setActive(visible);
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


	@Override
	public void iconGroupChanged(IconGroup iconGroup) {
		optionPanel.setIcon(getPluginIcon());
		window.setIcon(getPluginIcon());
	}


}
