/*
 * 06/13/2005
 *
 * FileSystemTreePlugin.java - A plugin that displays a tree of files on the
 * local filesystem, allowing for easy opening of files.
 * Copyright (C) 2005 Robert Futrell
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
package org.fife.rtext.plugins.filesystemtree;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.*;

import org.fife.rtext.*;
import org.fife.ui.RScrollPane;
import org.fife.ui.app.*;


/**
 * A panel displaying all files on the local filesystem, allowing for quick
 * and easy opening of files without opening the file chooser.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FileSystemTreePlugin extends GUIPlugin {

	private RText owner;
	private String name;
	private Tree tree;
	private FileSystemTreeOptionPanel optionPanel;
	private Icon pluginIcon;
	private ViewAction viewAction;

	static final String BUNDLE_NAME			=
					"org/fife/rtext/plugins/filesystemtree/FileSystemTree";
	private static final String VERSION_STRING	= "0.9.9.9";


	/**
	 * Creates a new <code>FileSystemTreePlugin</code>.
	 *
	 * @param app The RText instance.
	 */
	public FileSystemTreePlugin(AbstractPluggableGUIApplication app) {

		this.owner = (RText)app;

		ClassLoader cl = this.getClass().getClassLoader();
		URL url = cl.getResource("org/fife/rtext/plugins/filesystemtree/filesystemtree.gif");
		if (url!=null)
			pluginIcon = new ImageIcon(url);

		ResourceBundle msg = ResourceBundle.getBundle(BUNDLE_NAME,
											getLocale(), cl);
		this.name = msg.getString("Name");

		viewAction = new ViewAction(msg);

		setLayout(new BorderLayout());

		tree = new Tree(this);
		RScrollPane scrollPane = new RScrollPane(tree);
		add(scrollPane);

		// Set any preferences saved from the last time this plugin was used.
		FileSystemTreePreferences sbp = (FileSystemTreePreferences)
									FileSystemTreePreferences.load();
		setActive(sbp.active);
		setPosition(sbp.position);

		ComponentOrientation o = ComponentOrientation.
									getOrientation(getLocale());
		applyComponentOrientation(o);

	}


	/**
	 * Creates a preferences instance for this GUI plugin based on its
	 * current properties.  Your GUI plugin should create a subclass of
	 * <code>GUIPluginPreferences</code> that loads and saves properties
	 * specific to your plugin, and return it from this method.
	 *
	 * @return A preferences instance.
	 * @see org.fife.ui.app.GUIPluginPreferences
	 */
	protected GUIPluginPreferences createPreferences() {
		return (GUIPluginPreferences)FileSystemTreePreferences.
										generatePreferences(this);
	}


	/**
	 * Returns the options panel for this source browser.
	 *
	 * @return The options panel.
	 */
	public synchronized PluginOptionsDialogPanel getOptionsDialogPanel() {
		if (optionPanel==null) {
			optionPanel = new FileSystemTreeOptionPanel(owner, this);
		}
		return optionPanel;
	}


	/**
	 * Returns the author of the plugin.
	 *
	 * @return The plugin's author.
	 */
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	/**
	 * Returns the icon for this plugin.
	 *
	 * @return The icon for this plugin.
	 */
	public Icon getPluginIcon() {
		return pluginIcon;
	}


	/**
	 * Returns the menu items for this plugin.
	 *
	 * @return The menu for this plugin.
	 */
	public JMenu getPluginMenu() {

		JMenu menu = new JMenu(getPluginName());
		
		JCheckBoxMenuItem cbMenuItem =
					new JCheckBoxMenuItem(viewAction);
		cbMenuItem.setSelected(isActive());
		menu.add(cbMenuItem);

		return menu;

	}


	/**
	 * Returns the name of this <code>GUIPlugin</code>.
	 *
	 * @return This plugin's name.
	 */
	public String getPluginName() {
		return name;
	}


	/**
	 * Returns the plugin version.
	 */
	public String getPluginVersion() {
		return VERSION_STRING;
	}


	/**
	 * Returns the parent RText instance.
	 *
	 * @return The parent RText instance.
	 */
	RText getRText() {
		return owner;
	}


	/**
	 * Called just after a plugin is added to a GUI application.
	 *
	 * @param app The application to which this plugin was just added.
	 * @see #uninstall
	 */
	public void install(AbstractPluggableGUIApplication app) {
	}


	/**
	 * Called just before this <code>Plugin</code> is removed from an
	 * RText instance.  Here we uninstall any listeners we registered.
	 *
	 * @return Whether the uninstall went cleanly.
	 */
	public boolean uninstall() {
		return true;
	}


	/**
	 * Toggles the visibility of this file system tree.
	 */
	private class ViewAction extends AbstractAction {

		public ViewAction(ResourceBundle msg) {
			putValue(NAME, msg.getString("MenuItem.View"));
			putValue(MNEMONIC_KEY, new Integer(
					msg.getString("MenuItem.View.Mnemonic").charAt(0)));
			putValue(LONG_DESCRIPTION, msg.getString("MenuItem.View.Desc"));
		}

		public void actionPerformed(ActionEvent e) {
			setActive(!isActive());
		}

	}


}