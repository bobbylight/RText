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
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.AbstractPlugin;
import org.fife.ui.app.MenuBar;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.StandardAction;


/**
 * A plugin providing scripted macro support to RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class MacroPlugin extends AbstractPlugin
		implements PropertyChangeListener {

	private static final String VERSION				= "2.5.2";

	private RText app;
	private Icon icon;
	private JMenu macrosMenu;

	private static final String MSG = "org.fife.rtext.plugins.macros.MacrosPlugin";
	protected static final ResourceBundle msg = ResourceBundle.getBundle(MSG);

	private static final String EDIT_MACROS_ACTION		= "editMacrosAction";
	private static final String NEW_MACRO_ACTION		= "newMacroAction";


	/**
	 * Constructor.
	 *
	 * @param app The parent RText application.
	 */
	public MacroPlugin(AbstractPluggableGUIApplication app) {

		URL url = getClass().getResource("cog.png");
		if (url!=null) { // Should always be true
			try {
				icon = new ImageIcon(ImageIO.read(url));
			} catch (IOException ioe) {
				app.displayException(ioe);
			}
		}

		MacroPrefs prefs = loadPrefs();

		RText rtext = (RText)app;
		this.app = rtext;
		StandardAction a = new NewMacroAction(this, rtext, msg);
		a.setAccelerator(prefs.newMacroAccelerator);
		rtext.addAction(NEW_MACRO_ACTION, a);

		a = new EditMacrosAction(rtext, msg);
		a.setAccelerator(prefs.editMacrosAccelerator);
		rtext.addAction(EDIT_MACROS_ACTION, a);

	}


	/**
	 * Creates a menu item from an action, with no tool tip.
	 *
	 * @param a The action.
	 * @return The menu item.
	 */
	private static final JMenuItem createMenuItem(Action a) {
		JMenuItem item = new JMenuItem(a);
		item.setToolTipText(null);
		return item;
	}


	/**
	 * Returns the directory that macro definitions are saved to.
	 *
	 * @return The directory.
	 */
	public File getMacroDir() {
		return new File(RTextUtilities.getPreferencesDirectory(), "macros");
	}


	/**
	 * {@inheritDoc}
	 */
	public PluginOptionsDialogPanel getOptionsDialogPanel() {
		return new MacroOptionPanel(this);
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
		return getString("Plugin.Name");
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
	private static final File getPrefsFile() {
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
		return msg.getString(key);
	}


	/**
	 * Returns localized text for the given key.
	 *
	 * @param key The key.
	 * @param param The single parameter for the localized text.
	 * @return The localized text.
	 */
	String getString(String key, String param) {
		String text = msg.getString(key);
		text = MessageFormat.format(text, param);
		return text;
	}


	/**
	 * {@inheritDoc}
	 */
	public void install(AbstractPluggableGUIApplication app) {

		MacroManager.get().addPropertyChangeListener(
				MacroManager.PROPERTY_MACROS, this);

		// Add a new menu for selecting macros
		RText rtext = (RText)app;
		MenuBar mb = (org.fife.ui.app.MenuBar)rtext.getJMenuBar();
		macrosMenu = new JMenu(getString("Plugin.Name"));
		Action a = rtext.getAction(MacroPlugin.NEW_MACRO_ACTION);
		macrosMenu.add(createMenuItem(a));//createMenuItem(a));
		a = rtext.getAction(MacroPlugin.EDIT_MACROS_ACTION);
		macrosMenu.add(createMenuItem(a));//createMenuItem(a));
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
			desc = MessageFormat.format(desc, new Object[] { text });
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


	/**
	 * {@inheritDoc}
	 */
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
				RunMacroAction a = new RunMacroAction(app, this, macro);
				macrosMenu.add(createMenuItem(a));
			}
		}
		else {
			String text = MacroPlugin.msg.getString("NoMacrosDefined");
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
			desc = MessageFormat.format(desc, new Object[] { text });
			app.displayException(ioe, desc);
		}
	}


	public void savePreferences() {

		saveMacros();

		MacroPrefs prefs = new MacroPrefs();

		StandardAction a = (StandardAction)app.getAction(NEW_MACRO_ACTION);
		prefs.newMacroAccelerator = a.getAccelerator();
		a = (StandardAction)app.getAction(EDIT_MACROS_ACTION);
		prefs.editMacrosAccelerator = a.getAccelerator();

		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			app.displayException(ioe);
		}

	}


	/**
	 * {@inheritDoc}
	 */
	public boolean uninstall() {
		return true;
	}


}