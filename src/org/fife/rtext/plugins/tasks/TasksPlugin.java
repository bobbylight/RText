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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.rtext.RTextMenuBar;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.MenuBar;
import org.fife.ui.app.Plugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
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
	 * List of task identifiers, such as <code>TODO</code> and
	 * <code>FIXME</code>, separated by the '<code>|</code>' character.  This
	 * is kept in a field separate from the TaskParser, as it is only created
	 * when necessary, but this field is set at initialization time.
	 */
	private String taskIdentifiers;

	private static final String MSG = "org.fife.rtext.plugins.tasks.TasksPlugin";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);

	private static final Pattern TASK_IDENTIFIERS_PATTERN =
			Pattern.compile("^$|^[\\p{Alpha}\\?]+(?:\\|[\\p{Alpha}\\?]+)*$");

	static final String DEFAULT_TASK_IDS				= "FIXME|TODO|HACK";
	private static final String PREF_TASK_IDS			= "taskIdentifiers";
	private static final String PREF_TASK_LIST_VISIBLE	= "taskListVisible";
	private static final String VERSION					= "1.1.0";
	private static final String VIEW_TASKS_ACTION		= "viewTasksAction";


	/**
	 * Constructor.
	 *
	 * @param app The parent RText application.
	 */
	public TasksPlugin(AbstractPluggableGUIApplication app) {

		Properties prefs = loadPreferences();
		taskIdentifiers = prefs.getProperty(PREF_TASK_IDS);

		URL url = getClass().getResource("page_white_edit.png");
		if (url!=null) { // Should always be true
			icon = new ImageIcon(url);
		}

		RText rtext = (RText)app;
		this.app = rtext;

		ViewTasksAction a = new ViewTasksAction(rtext, msg, this);
//		a.setAccelerator(prefs.getAccelerator(VIEW_TASKS_ACTION));
		rtext.addAction(VIEW_TASKS_ACTION, a);

		boolean visible = Boolean.valueOf(
				prefs.getProperty(PREF_TASK_LIST_VISIBLE)).booleanValue();
		if (visible) {
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
	 * Returns the window that displays tasks.
	 *
	 * @return The window.  This may be <code>null</code> if tasks have not
	 *         yet been enabled at least once in the application.
	 */
	public TaskWindow getTasksWindow() {
		return window;
	}


	/**
	 * {@inheritDoc}
	 */
	public void install(AbstractPluggableGUIApplication app) {

		RText rtext = (RText)app;

		final ViewTasksAction vta = (ViewTasksAction)rtext.getAction(VIEW_TASKS_ACTION);
		final JCheckBoxMenuItem tasksItem = new JCheckBoxMenuItem(vta);
		tasksItem.setToolTipText(null);
		tasksItem.setState(isTaskWindowVisible());

		MenuBar mb = (org.fife.ui.app.MenuBar)rtext.getJMenuBar();
		JMenu menu = mb.getMenuByName(RTextMenuBar.MENU_VIEW);
		menu.insert(tasksItem, menu.getItemCount()-2);


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
	 * Loads preferences for this plugin.
	 *
	 * @return This plugin's preferences.
	 */
	private Properties loadPreferences() {

		// Load default preferences
		Properties prefs = new Properties();
		prefs.setProperty(PREF_TASK_LIST_VISIBLE, Boolean.TRUE.toString());
		prefs.setProperty(PREF_TASK_IDS, DEFAULT_TASK_IDS);

		// Load saved preferences, if any.
		File file = new File(RTextUtilities.getPreferencesDirectory(),
									"tasks.properties");
		if (file.isFile()) {
			try {
				BufferedInputStream bin = new BufferedInputStream(
												new FileInputStream(file));
				prefs.load(bin);
				bin.close();
			} catch (IOException ioe) {
				app.displayException(ioe);
			}
		}

		// Ensure task ID's is proper format - "letter+(|letter+)*"
		String ids = prefs.getProperty(PREF_TASK_IDS);
		if (!TASK_IDENTIFIERS_PATTERN.matcher(ids).matches()) {
			System.out.println("Invalid identifiers: " + ids);
			prefs.setProperty(PREF_TASK_IDS, DEFAULT_TASK_IDS);
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

		// Create preferences for this plugin.
		Properties prefs = new Properties();
		prefs.setProperty(PREF_TASK_LIST_VISIBLE, Boolean.valueOf(
									isTaskWindowVisible()).toString());
		prefs.setProperty(PREF_TASK_IDS, taskIdentifiers);

		File file = new File(RTextUtilities.getPreferencesDirectory(),
									"tasks.properties");
		try {
			BufferedOutputStream bout = new BufferedOutputStream(
					new FileOutputStream(file));
			prefs.store(bout, "Preferences for the Tasks plugin");
			bout.close();
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
		if (window.setTaskIdentifiers(identifiers)) { // If it was modified
			taskIdentifiers = window.getTaskIdentifiers(); // In case invalid
			reparseForTasks();
		}
	}


	/**
	 * Toggles visibility of the task window.
	 */
	void toggleTaskWindowVisible() {
		if (window==null) { // First time through
			window = new TaskWindow(app, taskIdentifiers);
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