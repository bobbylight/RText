/*
 * 02/06/2010
 *
 * TasksPlugin.java - A plugin that adds task support to RText.
 * Copyright (C) 2010 Robert Futrell
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
package org.fife.rtext.plugins.tasks;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.rtext.RTextMenuBar;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.MenuBar;
import org.fife.ui.app.Plugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.rsyntaxtextarea.parser.Parser;


/**
 * Plugin that adds "Tasks" support to RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class TasksPlugin implements Plugin {

	/**
	 * The parent application.
	 */
	private RText app;

	/**
	 * The tasks dockable window.
	 */
	private TaskWindow window;

	/**
	 * The plugin's icon.
	 */
	private Icon icon;

	/**
	 * The location of the task window.
	 */
	private int windowPosition;

	/**
	 * List of task identifiers, such as <code>TODO</code> and
	 * <code>FIXME</code>, separated by the '<code>|</code>' character.  This
	 * is kept in a field separate from the TaskParser, as it is only created
	 * when necessary, but this field is set at initialization time.
	 */
	private String taskIdentifiers;

	private static final String MSG = "org.fife.rtext.plugins.tasks.TasksPlugin";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);

	private static final String VERSION					= "1.1.0";
	private static final String VIEW_TASKS_ACTION		= "viewTasksAction";


	/**
	 * Constructor.
	 *
	 * @param app The parent RText application.
	 */
	public TasksPlugin(AbstractPluggableGUIApplication app) {

		RText rtext = (RText)app;
		this.app = rtext;

		TasksPrefs prefs = loadPrefs();
		taskIdentifiers = prefs.taskIdentifiers;
		windowPosition = prefs.windowPosition;

		URL url = getClass().getResource("page_white_edit.png");
		if (url!=null) { // Should always be true
			icon = new ImageIcon(url);
		}

		ViewTasksAction a = new ViewTasksAction(rtext, msg, this);
		a.setAccelerator(prefs.windowVisibilityAccelerator);
		rtext.addAction(VIEW_TASKS_ACTION, a);

		if (prefs.windowVisible) {
			toggleTaskWindowVisible(); // Will create and add task window.
		}

	}


	/**
	 * {@inheritDoc}
	 */
	public PluginOptionsDialogPanel getOptionsDialogPanel() {
		return new TasksOptionPanel(app, this);
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
		return msg.getString("PluginName");
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
	private File getPrefsFile() {
		return new File(RTextUtilities.getPreferencesDirectory(),
						"tasks.properties");
	}


	/**
	 * Returns localized text.
	 *
	 * @param key The key for the localized text.
	 * @return The localized text.
	 */
	String getString(String key) {
		return msg.getString(key);
	}


	/**
	 * Returns the current task identifiers.
	 *
	 * @return The current task identifiers.
	 */
	public String getTaskIdentifiers() {
		return taskIdentifiers;
	}


	/**
	 * Returns the position of the tasks dockable window.  If the dockable
	 * window is not yet visible, this is the position it will be placed in
	 * when it is made visible.
	 *
	 * @return The position.
	 * @see #setTaskWindowPosition(int)
	 */
	int getTaskWindowPosition() {
		return windowPosition;
	}


	/**
	 * {@inheritDoc}
	 */
	public void install(AbstractPluggableGUIApplication app) {

		RText rtext = (RText)app;

		ViewTasksAction vta = (ViewTasksAction)rtext.getAction(VIEW_TASKS_ACTION);
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(vta);
		item.setToolTipText(null);
		item.setSelected(isTaskWindowVisible());

		MenuBar mb = (org.fife.ui.app.MenuBar)rtext.getJMenuBar();
		final JMenu menu = mb.getMenuByName(RTextMenuBar.MENU_VIEW);
		menu.insert(item, menu.getItemCount()-2);
		JPopupMenu popup = menu.getPopupMenu();
		popup.pack();
		// Only needed for pre-1.6 support
		popup.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				item.setSelected(isTaskWindowVisible());
			}
		});

	}


	/**
	 * Returns whether the task window is currently visible.
	 *
	 * @return Whether the task window is currently visible.
	 */
	public boolean isTaskWindowVisible() {
		return window!=null && window.isActive();
	}


	/**
	 * Loads saved preferences into the <code>prefs</code> member.  If this
	 * is the first time through, default values will be returned.
	 *
	 * @return The preferences.
	 */
	private TasksPrefs loadPrefs() {
		TasksPrefs prefs = new TasksPrefs();
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
	 * Re-parses all open files for tasks.  This is called when the task
	 * identifier list changes.
	 */
	private void reparseForTasks() {
		AbstractMainView view = app.getMainView();
		for (int i=0; i<view.getNumDocuments(); i++) {
			RTextEditorPane textArea = view.getRTextEditorPaneAt(i);
			for (int j=0; j<textArea.getParserCount(); j++) {
				Parser parser = textArea.getParser(j);
				if (window.isTaskParser(parser)) {
					textArea.forceReparsing(j);
					break;
				}
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void savePreferences() {
		TasksPrefs prefs = new TasksPrefs();
		prefs.taskIdentifiers = taskIdentifiers;
		prefs.windowVisible = isTaskWindowVisible();
		prefs.windowPosition = windowPosition;
		ViewTasksAction vta = (ViewTasksAction)app.getAction(VIEW_TASKS_ACTION);
		prefs.windowVisibilityAccelerator = vta.getAccelerator();
		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			app.displayException(ioe);
		}
	}


	/**
	 * Sets the task identifiers scanned for.
	 *
	 * @param identifiers The identifiers.
	 */
	void setTaskIdentifiers(String identifiers) {
		if (window!=null) {
			if (window.setTaskIdentifiers(identifiers)) { // If it was modified
				taskIdentifiers = window.getTaskIdentifiers(); // In case invalid
				reparseForTasks();
			}
		}
		else {
			taskIdentifiers = identifiers;
		}
	}


	/**
	 * Sets the position of the dockable window.  This method does nothing if
	 * the position specified by <code>pos</code> is invalid or is the same
	 * as the current position.
	 *
	 * @param pos The new position.
	 * @see #getTaskWindowPosition()
	 */
	void setTaskWindowPosition(int pos) {
		if (pos!=windowPosition && DockableWindow.isValidPosition(pos)) {
			if (window!=null) {
				window.setPosition(pos);
			}
			windowPosition = pos;
		}
	}


	/**
	 * Sets whether the tasks window is visible.  This method does nothing if
	 * the visibility of the tasks window already matches what is specified
	 * by <code>visible</code>.
	 *
	 * @param visible Whether the tasks window should be visible.
	 * @see #toggleTaskWindowVisible()
	 */
	void setTaskWindowVisible(boolean visible) {
		if (visible!=isTaskWindowVisible()) {
			toggleTaskWindowVisible();
		}
	}


	/**
	 * Toggles visibility of the task window.
	 *
	 * @see #setTaskWindowVisible(boolean)
	 */
	void toggleTaskWindowVisible() {
		if (window==null) { // First time through
			window = new TaskWindow(app, taskIdentifiers);
			window.setPosition(windowPosition);
			window.setActive(true);
			app.addDockableWindow(window);
		}
		else {
			window.setActive(!window.isActive());
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean uninstall() {
		return true;
	}


}