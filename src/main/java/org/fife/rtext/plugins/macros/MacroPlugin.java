/*
 * 07/31/2011
 *
 * MacroPlugin.java - Plugin adding macro support to RText.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.macros;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.AbstractPlugin;
import org.fife.ui.app.MenuBar;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.AppAction;
import org.fife.ui.rtextarea.IconGroup;


/**
 * A plugin providing scripted macro support to RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class MacroPlugin extends AbstractPlugin
		implements PropertyChangeListener {

	private static final String VERSION				= "3.1.0";

	private final RText app;
	private JMenu macrosMenu;
	private final NewMacroAction newMacroAction;
	private final EditMacrosAction editMacrosAction;

	private static final String MSG_BUNDLE = "org.fife.rtext.plugins.macros.MacrosPlugin";
	static final ResourceBundle MSG = ResourceBundle.getBundle(MSG_BUNDLE);

	private static final String EDIT_MACROS_ACTION		= "editMacrosAction";
	private static final String NEW_MACRO_ACTION		= "newMacroAction";


	/**
	 * Constructor.
	 *
	 * @param app The parent RText application.
	 */
	public MacroPlugin(AbstractPluggableGUIApplication<?> app) {

		MacroPrefs prefs = loadPrefs();

		RText rtext = (RText)app;
		this.app = rtext;
		newMacroAction = new NewMacroAction(this, rtext, MSG);
		newMacroAction.setAccelerator(prefs.newMacroAccelerator);
		rtext.addAction(NEW_MACRO_ACTION, newMacroAction);

		editMacrosAction = new EditMacrosAction(rtext, MSG);
		editMacrosAction.setAccelerator(prefs.editMacrosAccelerator);
		rtext.addAction(EDIT_MACROS_ACTION, editMacrosAction);

		updateActionIcons(rtext.getIconGroup());
		rtext.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, this);
	}


	/**
	 * Creates a menu item from an action, with no tool tip.
	 *
	 * @param a The action.
	 * @return The menu item.
	 */
	private static JMenuItem createMenuItem(Action a) {
		JMenuItem item = new JMenuItem(a);
		item.setToolTipText(null);
		return item;
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
		return new MacroOptionPanel(this);
	}


	@Override
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	@Override
	public Icon getPluginIcon(boolean darkLookAndFeel) {
		// This allows us to get a theme-specific icon if there is one
		return newMacroAction != null ? newMacroAction.getIcon() : null;
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


	public RText getRText() {
		return app;
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
	public void install(AbstractPluggableGUIApplication<?> app) {

		MacroManager.get().addPropertyChangeListener(
				MacroManager.PROPERTY_MACROS, this);

		// Add a new menu for selecting macros
		RText rtext = (RText)app;
		MenuBar mb = (org.fife.ui.app.MenuBar)rtext.getJMenuBar();
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
			app.displayException(ioe, desc);
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
				app.displayException(ioe);
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

		else if (RText.ICON_STYLE_PROPERTY.equals(prop)) {
			updateActionIcons((IconGroup)e.getNewValue());
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
				RunMacroAction a = new RunMacroAction(app, this, macro);
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
			app.displayException(ioe, desc);
		}
	}


	@Override
	public void savePreferences() {

		saveMacros();

		MacroPrefs prefs = new MacroPrefs();

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
		}
		else {
			newMacroAction.restoreDefaultIcon();
		}

		icon = iconGroup.getIcon("editmacros");
		if (icon != null) {
			editMacrosAction.setIcon(icon);
		}
		else {
			editMacrosAction.setIcon((Icon)null);
		}
	}
}
