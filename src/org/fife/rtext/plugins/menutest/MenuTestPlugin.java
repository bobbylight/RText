package org.fife.rtext.plugins.menutest;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

//import org.fife.rtext.*;
import org.fife.ui.app.*;


/**
 * 
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class MenuTestPlugin extends MenuPlugin {

	//private AbstractPluggableGUIApplication app;


	public MenuTestPlugin(AbstractPluggableGUIApplication app) {
		//this.app = app;
	}


	/**
	 * Creates the menu for this plugin.
	 *
	 * @return The menu for this plugin.
	 * @see #getPluginMenu()
	 */
	protected JMenu createMenu() {
		JMenu menu = new JMenu("Test!");
		JMenuItem item = new JMenuItem("Item 1");
		menu.add(item);
		return menu;
	}


	/**
	 * Returns an options panel for use in an Options dialog.  This panel
	 * should contain all options pertaining to this plugin.
	 *
	 * @return The options panel.
	 */
	public PluginOptionsDialogPanel getOptionsDialogPanel() {
		return null;
	}


	/**
	 * Returns the author of the plugin.
	 *
	 * @return The author.
	 */
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	/**
	 * Returns the icon to display beside the name of this plugin in the
	 * application's interface.
	 *
	 * @return The icon for this plugin.  This value may be <code>null</code>
	 *         to represent no icon.
	 */
	public Icon getPluginIcon() {
		return null;
	}


	/**
	 * Returns the name of the plugin.
	 *
	 * @return The plugin name.
	 */
	public String getPluginName() {
		return "Test MenuPlugin";
	}


	/**
	 * Returns the version of the plugin.
	 *
	 * @return The version number of this plugin.
	 */
	public String getPluginVersion() {
		return "0.9.9.9";
	}


	/**
	 * Called when the GUI application is shutting down.  When this method is
	 * called, the <code>Plugin</code> should save any properties via the
	 * Java Preferences API.
	 *
	 * @see PluginPreferences
	 */
	public void savePreferences() {
	}


}