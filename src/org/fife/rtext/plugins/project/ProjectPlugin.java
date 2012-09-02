/*
 * 08/28/2012
 *
 * ProjectPlugin.java - Plugin adding "project" support to RText.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.TreeNode;

import org.fife.rtext.RText;
import org.fife.rtext.RTextMenuBar;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.AbstractPlugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.StandardAction;


/**
 * A plugin that adds a very simple "project" system into RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ProjectPlugin extends AbstractPlugin {

	private RText rtext;
	private ProjectWindow window;
	private Icon icon;
	private Workspace workspace;

	private static final String VIEW_CONSOLE_ACTION	= "viewProjectWindowAction";
	private static final String VERSION_STRING = "2.0.4";


	public ProjectPlugin(AbstractPluggableGUIApplication app) {

		rtext = (RText)app; // Needed now in case of XML errors below.

		URL res = getClass().getResource("application_side_list.png");
		icon = new ImageIcon(res);
		ProjectPluginPrefs prefs = loadPrefs();

		StandardAction a = new ViewProjectsAction((RText)app, this);
		a.setAccelerator(prefs.windowVisibilityAccelerator);
		app.addAction(VIEW_CONSOLE_ACTION, a);

		loadInitialWorkspace(prefs.openWorkspaceName);

		// Window MUST always be created for preference saving on shutdown
		window = new ProjectWindow(rtext, this);
		window.setPosition(prefs.windowPosition);
		window.setActive(prefs.windowVisible);

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
		return Messages.getString("ProjectPlugin.Name");
	}


	/**
	 * {@inheritDoc}
	 */
	public String getPluginVersion() {
		return VERSION_STRING;
	}


	/**
	 * Returns the file containing the preferences for this plugin.
	 *
	 * @return The preferences file for this plugin.
	 */
	private File getPrefsFile() {
		return new File(RTextUtilities.getPreferencesDirectory(),
				"projects.properties");
	}


	/**
	 * Returns the parent RText instance.
	 *
	 * @return The parent RText instance.
	 */
	public RText getRText() {
		return rtext;
	}


	/**
	 * Returns the directory that workspaces are saved to.
	 *
	 * @return The directory.
	 */
	public File getWorkspacesDir() {
		return new File(RTextUtilities.getPreferencesDirectory(), "workspaces");
	}


	/**
	 * Returns the active workspace.
	 *
	 * @return The active workspace, or <code>null</code> if none.
	 */
	public Workspace getWorkspace() {
		return workspace;
	}


	public void install(AbstractPluggableGUIApplication app) {

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
				item.setSelected(isProjectWindowVisible());
			}
		});

		rtext.addDockableWindow(window);

	}


	/**
	 * Returns whether the console window is visible.
	 *
	 * @return Whether the console window is visible.
	 * @see #setProjectWindowVisible(boolean)
	 */
	boolean isProjectWindowVisible() {
		return window.isActive();
	}


	/**
	 * Loads the initial workspace as previously saved in the preferences.
	 *
	 * @param workspaceName The name of the workspace to load.
	 */
	private void loadInitialWorkspace(String workspaceName) {
		if (workspaceName!=null) {
			File workspaceFile = new File(getWorkspacesDir(),
					workspaceName + ".xml");
			if (workspaceFile.isFile()) {
				// Always true unless user manually removed it
				try {
					workspace = Workspace.load(workspaceFile);
				} catch (IOException ioe) {
					rtext.displayException(ioe);
				}
			}
		}
		if (workspace==null) {
			workspace = new Workspace("Workspace");
		}
	}


	/**
	 * Loads saved preferences for this plugin.  If this is the first
	 * time through, default values will be returned.
	 *
	 * @return The preferences.
	 */
	private ProjectPluginPrefs loadPrefs() {
		ProjectPluginPrefs prefs = new ProjectPluginPrefs();
		File prefsFile = getPrefsFile();
		if (prefsFile.isFile()) {
			try {
				prefs.load(prefsFile);
			} catch (IOException ioe) {
				rtext.displayException(ioe);
				// (Some) defaults will be used
			}
		}
		return prefs;
	}


	/**
	 * Refreshes the workspace tree from the specified node down.
	 *
	 * @param fromNode The node to start the refreshing from.
	 */
	public void refreshTree(TreeNode fromNode) {
		window.refreshTree(fromNode);
	}


	public void savePreferences() {

		ProjectPluginPrefs prefs = new ProjectPluginPrefs();
		prefs.windowPosition = window.getPosition();
		StandardAction a = (StandardAction)rtext.getAction(VIEW_CONSOLE_ACTION);
		prefs.windowVisibilityAccelerator = a.getAccelerator();
		prefs.windowVisible = window.isActive();
		prefs.openWorkspaceName = workspace==null ? null : workspace.getName();
		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			rtext.displayException(ioe);
		}

		if (workspace!=null) {
			try {
				workspace.save(getWorkspacesDir());
			} catch (IOException ioe) {
				rtext.displayException(ioe);
			}
		}

	}


	/**
	 * Sets the visibility of the console window.
	 *
	 * @param visible Whether the window should be visible.
	 * @see #isProjectWindowVisible()
	 */
	void setProjectWindowVisible(boolean visible) {
		if (visible!=isProjectWindowVisible()) {
			window.setActive(visible);
		}
	}


	public boolean uninstall() {
		// TODO Auto-generated method stub
		return false;
	}


}