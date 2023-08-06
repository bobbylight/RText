/*
 * 07/31/2011
 *
 * MacroPlugin.java - Plugin adding macro support to RText.
 * Copyright (C) 2011 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.macros;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.*;

import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.ImageTranscodingUtil;
import org.fife.ui.StandardMenuItem;
import org.fife.ui.app.AbstractPlugin;
import org.fife.ui.app.MenuBar;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.AppAction;
import org.fife.ui.app.icons.IconGroup;
import org.fife.ui.app.themes.*;


/**
 * A plugin providing scripted macro support to RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class MacroPlugin extends AbstractPlugin<RText>
		implements PropertyChangeListener {

	private static final String VERSION				= "6.0.2";

	private MacroOptionPanel optionPanel;
	private JMenu macrosMenu;
	private final NewMacroAction newMacroAction;
	private final EditMacrosAction editMacrosAction;
	private Map<String, Icon> icons;

	private static final String MSG_BUNDLE = "org.fife.rtext.plugins.macros.MacrosPlugin";
	static final ResourceBundle MSG = ResourceBundle.getBundle(MSG_BUNDLE);

	private static final String EDIT_MACROS_ACTION		= "editMacrosAction";
	private static final String NEW_MACRO_ACTION		= "newMacroAction";


	/**
	 * Constructor.
	 *
	 * @param rtext The parent RText application.
	 */
	public MacroPlugin(RText rtext) {

		super(rtext);
		loadIcons();
		MacroPrefs prefs = loadPrefs();

		newMacroAction = new NewMacroAction(this, rtext, MSG);
		newMacroAction.setAccelerator(prefs.newMacroAccelerator);
		rtext.addAction(NEW_MACRO_ACTION, newMacroAction);

		editMacrosAction = new EditMacrosAction(rtext, MSG);
		editMacrosAction.setAccelerator(prefs.editMacrosAccelerator);
		rtext.addAction(EDIT_MACROS_ACTION, editMacrosAction);

		updateActionIcons(rtext.getIconGroup());
	}


	/**
	 * Creates a menu item from an action, with no tool tip.
	 *
	 * @param a The action.
	 * @return The menu item.
	 */
	private static JMenuItem createMenuItem(Action a) {
		JMenuItem item = new StandardMenuItem(a);
		item.setToolTipText(null);
		return item;
	}


	void editMacro(Macro macro, Component parent) {

		RText rtext = getApplication();
		String text;
		int messageType = JOptionPane.INFORMATION_MESSAGE;
		File file = new File(macro.getFile());

		if (file.isFile()) { // Should always be true
			rtext.openFile(file);
			text = MSG.getString("Message.MacroOpened");
			text = MessageFormat.format(text, macro.getName());
		}

		else { // Macro script was deleted outside of RText.
			text = MSG.getString("Error.ScriptDoesntExist");
			text = MessageFormat.format(text, file.getAbsolutePath());
			messageType = JOptionPane.ERROR_MESSAGE;
		}

		String title = rtext.getString("InfoDialogHeader");
		JOptionPane.showMessageDialog(parent, text, title, messageType);
	}


	Icon getIcon(String iconName) {
		return icons.get(iconName);
	}


	/**
	 * Returns the directory that macro definitions are saved to.
	 *
	 * @return The directory.
	 */
	File getMacroDir() {
		return new File(RTextUtilities.getPreferencesDirectory(), "macros");
	}


	@Override
	public PluginOptionsDialogPanel<MacroPlugin> getOptionsDialogPanel() {
		if (optionPanel == null) {
			optionPanel = new MacroOptionPanel(this);
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
		return getString("Plugin.Name");
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
						"macros.properties");
	}


	/**
	 * Returns localized text for the given key.
	 *
	 * @param key The key.
	 * @return The localized text.
	 */
	String getString(String key) {
		return MSG.getString(key);
	}


	/**
	 * Returns localized text for the given key.
	 *
	 * @param key The key.
	 * @param param The single parameter for the localized text.
	 * @return The localized text.
	 */
	String getString(String key, String param) {
		String text = MSG.getString(key);
		text = MessageFormat.format(text, param);
		return text;
	}


	@Override
	public void iconGroupChanged(IconGroup iconGroup) {
		updateActionIcons(iconGroup);
		optionPanel.setIcon(getPluginIcon());
	}


	@Override
	public void install() {

		MacroManager.get().addPropertyChangeListener(
				MacroManager.PROPERTY_MACROS, this);

		// Add a new menu for selecting macros
		RText rtext = getApplication();
		@SuppressWarnings("unchecked")
		MenuBar<RText> mb = (MenuBar<RText>)rtext.getJMenuBar();
		macrosMenu = new JMenu(getString("Plugin.Name"));
		Action a = rtext.getAction(MacroPlugin.NEW_MACRO_ACTION);
		macrosMenu.add(createMenuItem(a));
		a = rtext.getAction(MacroPlugin.EDIT_MACROS_ACTION);
		macrosMenu.add(createMenuItem(a));
		macrosMenu.addSeparator();
		mb.addExtraMenu(macrosMenu);
		mb.revalidate();

		loadMacros(); // Do after menu has been added

	}


	private void loadIcons() {

		icons = new HashMap<>();

		try {

			icons.put(NativeTheme.ID, new ImageIcon(getClass().getResource("eclipse/cog_add.png")));

			Image darkThemeImage = ImageTranscodingUtil.rasterize("macro dark",
				getClass().getResourceAsStream("flat-dark/cog_add.svg"), 16, 16);
			icons.put(FlatDarkTheme.ID, new ImageIcon(darkThemeImage));
			icons.put(FlatMacDarkTheme.ID, new ImageIcon(darkThemeImage));

			Image lightThemeImage = ImageTranscodingUtil.rasterize("macro light",
				getClass().getResourceAsStream("flat-light/cog_add.svg"), 16, 16);
			icons.put(FlatLightTheme.ID, new ImageIcon(lightThemeImage));
			icons.put(FlatMacLightTheme.ID, new ImageIcon(lightThemeImage));

			Image whiteImage = ImageTranscodingUtil.rasterize("macro white",
				getClass().getResourceAsStream("flat-white/cog_add.svg"), 16, 16);
			icons.put("white", new ImageIcon(whiteImage));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}


	/**
	 * Loads the previously saved macros.
	 *
	 * @see #saveMacros()
	 */
	private void loadMacros() {

		// First time through, this directory won't exist.
		File macroDir = getMacroDir();
		if (!macroDir.isDirectory()) {
			macroDir.mkdirs();
		}

		try {
			MacroManager.get().loadMacros(macroDir);
		} catch (IOException ioe) {
			String text = ioe.getMessage();
			if (text==null) {
				text = ioe.toString();
			}
			String desc = getString("Error.LoadingMacros");
			desc = MessageFormat.format(desc, text);
			getApplication().displayException(ioe, desc);
		}

	}


	/**
	 * Loads saved preferences for this plugin.  If this is the first
	 * time through, default values will be returned.
	 *
	 * @return The preferences.
	 */
	private MacroPrefs loadPrefs() {
		MacroPrefs prefs = new MacroPrefs();
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


	@Override
	public void propertyChange(PropertyChangeEvent e) {

		String prop = e.getPropertyName();

		if (MacroManager.PROPERTY_MACROS.equals(prop)) {
			refreshMacrosMenu();
		}
	}


	/**
	 * Refreshes the elements in the Macros menu to be in sync with the macros
	 * the user has defined.
	 */
	private void refreshMacrosMenu() {

		while (macrosMenu.getMenuComponentCount()>3) {
			macrosMenu.remove(3);
		}

		if (MacroManager.get().getMacroCount()>0) {
			Iterator<Macro> i = MacroManager.get().getMacroIterator();
			while (i.hasNext()) {
				Macro macro = i.next();
				RunMacroAction a = new RunMacroAction(getApplication(), this, macro);
				macrosMenu.add(createMenuItem(a));
			}
		}
		else {
			String text = MacroPlugin.MSG.getString("NoMacrosDefined");
			JMenuItem item = new JMenuItem(text);
			item.setEnabled(false);
			macrosMenu.add(item);
		}

	}


	/**
	 * Saves our current set of macros.
	 *
	 * @see #loadMacros()
	 */
	private void saveMacros() {
		try {
			MacroManager.get().saveMacros(getMacroDir());
		} catch (IOException ioe) {
			String text = ioe.getMessage();
			if (text==null) {
				text = ioe.toString();
			}
			String desc = getString("Error.SavingMacros");
			desc = MessageFormat.format(desc, text);
			getApplication().displayException(ioe, desc);
		}
	}


	@Override
	public void savePreferences() {

		saveMacros();

		MacroPrefs prefs = new MacroPrefs();

		RText app = getApplication();
		AppAction<?> a = (AppAction<?>)app.getAction(NEW_MACRO_ACTION);
		prefs.newMacroAccelerator = a.getAccelerator();
		a = (AppAction<?>)app.getAction(EDIT_MACROS_ACTION);
		prefs.editMacrosAccelerator = a.getAccelerator();

		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			app.displayException(ioe);
		}

	}


	@Override
	public boolean uninstall() {
		return true;
	}


	private void updateActionIcons(IconGroup iconGroup) {

		Icon icon = iconGroup.getIcon("newmacro");
		if (icon != null) {
			newMacroAction.setIcon(icon);
			newMacroAction.setRolloverIcon((Icon)null);
		}
		else {
			newMacroAction.restoreDefaultIcon();
		}

		icon = iconGroup.getIcon("editmacros");
		if (icon != null) {
			editMacrosAction.setIcon(icon);
			editMacrosAction.setRolloverIcon((Icon)null);
		}
		else {
			editMacrosAction.restoreDefaultIcon();
		}
	}
}
