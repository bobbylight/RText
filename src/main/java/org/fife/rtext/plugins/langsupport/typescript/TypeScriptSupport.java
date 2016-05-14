/*
 * 12/14/2015
 *
 * Copyright (C) 2015 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport.typescript;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.fife.rtext.RText;
import org.fife.rtext.RTextMenuBar;
import org.fife.rtext.plugins.langsupport.LangSupportPreferences;
import org.fife.rtext.plugins.langsupport.Plugin;
import org.fife.ui.app.MenuBar;
import org.fife.ui.StandardAction;
import org.fife.ui.UIUtil;


/**
 * Installs TypeScript-specific actions, etc.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class TypeScriptSupport {

	private TypeScriptNoticeWindow tsWindow;
	private BuildAction buildAction;

	private static final String DOCKABLE_WINDOW_TS_ERRORS = "TypeScriptWarnings";

	private static final String VIEW_TS_BUILD_RESULTS_ACTION = "viewTsBuildResultsAction";
	private static final String TS_BUILD_ACTION = "tsBuildAction";


	/**
	 * Adds an item to the "View" menu to toggle console visibility.
	 */
	private void addActionToDockableWindowsMenu(RText rtext) {

		MenuBar mb = (MenuBar)rtext.getJMenuBar();

		final JMenu menu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		Action a = rtext.getAction(VIEW_TS_BUILD_RESULTS_ACTION);
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(a);
		item.setToolTipText(null);
		item.applyComponentOrientation(rtext.getComponentOrientation());
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
				item.setSelected(isBuildResultsWindowVisible());
			}
		});

	}


	/**
	 * Installs TypeScript-specific language support features.
	 *
	 * @param rtext The application.
	 * @param plugin The language support plugin.
	 * @param prefs Language support preferences.
	 * @see #save(RText, LangSupportPreferences)
	 */
	public void install(RText rtext, Plugin plugin,
			LangSupportPreferences prefs) {

		ResourceBundle msg = plugin.getBundle();
		JMenu menu = UIUtil.newMenu(msg, "TypeScript");

		buildAction = new BuildAction(plugin, prefs.ts_build_accelerator);
		rtext.addAction(TS_BUILD_ACTION, buildAction);
		JMenuItem buildMenuItem = UIUtil.newMenuItem(msg, "TypeScript.Build",
				buildAction);
		menu.add(buildMenuItem);

		MenuBar mb = (MenuBar)rtext.getJMenuBar();
		JMenu editMenu = mb.getMenuByName(RTextMenuBar.MENU_EDIT);
		int itemCount = editMenu.getMenuComponentCount();
		editMenu.insert(menu, itemCount - 1);
		editMenu.insertSeparator(itemCount);

		tsWindow = new TypeScriptNoticeWindow(rtext, plugin);
		tsWindow.setPosition(prefs.ts_build_output_window_position);
		tsWindow.setActive(prefs.ts_build_output_window_visible);
		rtext.addDockableWindow(tsWindow);
		plugin.putDockableWindow(DOCKABLE_WINDOW_TS_ERRORS, tsWindow);

		ViewTypeScriptBuildResultsAction a =
				new ViewTypeScriptBuildResultsAction(rtext, msg, plugin);
		a.setAccelerator(prefs.ts_build_window_visible_accelerator);
		rtext.addAction(VIEW_TS_BUILD_RESULTS_ACTION, a);

		addActionToDockableWindowsMenu(rtext);

	}


	/**
	 * Returns whether the TypeScript build results window is visible.
	 *
	 * @return Whether the dockable window is visible.
	 * @see #toggleBuildResultsWindowVisible()
	 */
	public boolean isBuildResultsWindowVisible() {
		return tsWindow!=null && tsWindow.isActive();
	}


	/**
	 * Stores TypeScript-specific parameters into a preferences structure.
	 * This should be called when the application is shutting down.
	 * 
	 * @param rtext The parent application.
	 * @param prefs The preferences structure.
	 * @see #install(RText, Plugin, LangSupportPreferences)
	 */
	public void save(RText rtext, LangSupportPreferences prefs) {
		prefs.ts_build_accelerator = buildAction.getAccelerator();
		prefs.ts_build_window_visible_accelerator =
				(KeyStroke)rtext.getAction(VIEW_TS_BUILD_RESULTS_ACTION).
				getValue(Action.ACCELERATOR_KEY);
		prefs.ts_build_output_window_position = tsWindow.getPosition();
		prefs.ts_build_output_window_visible = tsWindow.isActive();
		// Folding is handled by plugin itself
		//prefs.ts_folding_enabled = ...
	}


	/**
	 * Toggles whether the TypeScript build results window is visible.
	 *
	 * @see #isBuildResultsWindowVisible()
	 */
	public void toggleBuildResultsWindowVisible() {
		tsWindow.setActive(!tsWindow.isActive());
	}


	/**
	 * Runs the TypeScript compiler and collects any errors.
	 */
	private static class BuildAction extends StandardAction {

		private Plugin plugin;

		private BuildAction(Plugin plugin, KeyStroke accelerator) {
			setName(plugin.getBundle().getString("TypeScript.Build"));
			this.plugin = plugin;
			setAccelerator(accelerator);
		}

		public void actionPerformed(ActionEvent e) {
			TypeScriptNoticeWindow window = (TypeScriptNoticeWindow)
					plugin.getDockableWindow(DOCKABLE_WINDOW_TS_ERRORS);
			window.doBuild();
		}

	}

}