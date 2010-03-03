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
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.fife.rtext.*;
import org.fife.ui.RScrollPane;
import org.fife.ui.app.*;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowScrollPane;


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
	private static final String VERSION_STRING	= "1.1.0";

	private static final String VIEW_FST_ACTION	= "ViewFileSystemTreeAction";


	/**
	 * Creates a new <code>FileSystemTreePlugin</code>.
	 *
	 * @param app The RText instance.
	 */
	public FileSystemTreePlugin(AbstractPluggableGUIApplication app) {

		this.owner = (RText)app;

		URL url = this.getClass().getResource("filesystemtree.gif");
		if (url!=null)
			pluginIcon = new ImageIcon(url);

		ResourceBundle msg = ResourceBundle.getBundle(BUNDLE_NAME);
		this.name = msg.getString("Name");

		FileSystemTreePrefs prefs = loadPrefs();
		viewAction = new ViewAction(owner, msg);
		viewAction.setAccelerator(prefs.windowVisibilityAccelerator);

		DockableWindow wind = createDockableWindow(prefs);
		putDockableWindow(name, wind);

	}


	/**
	 * Creates the single dockable window used by this plugin.
	 *
	 * @param prefs Preferences for this plugin.
	 * @return The dockable window.
	 */
	private DockableWindow createDockableWindow(FileSystemTreePrefs prefs) {

		DockableWindow wind = new DockableWindow(name, new BorderLayout());

		tree = new Tree(this);
		RScrollPane scrollPane = new DockableWindowScrollPane(tree);
		wind.add(scrollPane);

		wind.setActive(prefs.active);
		wind.setPosition(prefs.position);
		wind.setIcon(getPluginIcon());

		ComponentOrientation o = ComponentOrientation.
									getOrientation(Locale.getDefault());
		wind.applyComponentOrientation(o);

		return wind;

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
	 * Returns the file preferences for this plugin are saved in.
	 *
	 * @return The file.
	 */
	private File getPrefsFile() {
		return new File(RTextUtilities.getPreferencesDirectory(),
						"fileSystemTree.properties");
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
	 * {@inheritDoc}
	 */
	public void install(AbstractPluggableGUIApplication app) {

		// Add a menu item to toggle the visibility of the dockable window
		owner.addAction(VIEW_FST_ACTION, viewAction);
		RTextMenuBar mb = (RTextMenuBar)owner.getJMenuBar();
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(viewAction);
		item.setSelected(getDockableWindow(getPluginName()).isActive());
		item.applyComponentOrientation(app.getComponentOrientation());
		JMenu viewMenu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		viewMenu.add(item);
		JPopupMenu popup = viewMenu.getPopupMenu();
		popup.pack();
		// Only needed for pre-1.6 support
		popup.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				item.setSelected(getDockableWindow(getPluginName()).isActive());
			}
		});

	}


	/**
	 * Loads saved preferences into the <code>prefs</code> member.  If this
	 * is the first time through, default values will be returned.
	 *
	 * @return The preferences.
	 */
	private FileSystemTreePrefs loadPrefs() {
		FileSystemTreePrefs prefs = new FileSystemTreePrefs();
		File prefsFile = getPrefsFile();
		if (prefsFile.isFile()) {
			try {
				prefs.load(prefsFile);
			} catch (IOException ioe) {
				getRText().displayException(ioe);
				// (Some) defaults will be used
			}
		}
		return prefs;
	}


	/**
	 * {@inheritDoc}
	 */
	public void savePreferences() {
		FileSystemTreePrefs prefs = new FileSystemTreePrefs();
		prefs.active = getDockableWindow(name).isActive();
		prefs.position = getDockableWindow(name).getPosition();
		prefs.windowVisibilityAccelerator = viewAction.getAccelerator();
		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			getRText().displayException(ioe);
		}
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
	private class ViewAction extends StandardAction {

		public ViewAction(RText app, ResourceBundle msg) {
			super(app, msg, "MenuItem.View");
		}

		public void actionPerformed(ActionEvent e) {
			DockableWindow wind = getDockableWindow(getPluginName());
			wind.setActive(!wind.isActive());
		}

	}


}