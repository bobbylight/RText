/*
 * 02/06/2010
 *
 * TasksPlugin.java - A plugin that adds task support to RText.
 * Copyright (C) 2010 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tasks;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.rtext.RTextMenuBar;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.ImageTranscodingUtil;
import org.fife.ui.app.GUIPlugin;
import org.fife.ui.app.MenuBar;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.icons.IconGroup;
import org.fife.ui.app.themes.*;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.rsyntaxtextarea.parser.Parser;


/**
 * Plugin that adds "Tasks" support to RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class TasksPlugin extends GUIPlugin<RText> {

	/**
	 * The tasks dockable window.
	 */
	private TaskWindow window;

	private TasksOptionPanel optionPanel;
	private Map<String, Icon> icons;

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

	private static final String MSG_BUNDLE = "org.fife.rtext.plugins.tasks.TasksPlugin";
	private static final ResourceBundle MSG = ResourceBundle.getBundle(MSG_BUNDLE);

	private static final String VERSION					= "6.0.0";
	private static final String VIEW_TASKS_ACTION		= "viewTasksAction";
	private static final String DOCKABLE_WINDOW_TASKS	= "tasksDockableWindow";


	/**
	 * Constructor.
	 *
	 * @param app The parent RText application.
	 */
	public TasksPlugin(RText app) {

		super(app);
		TasksPrefs prefs = loadPrefs();
		taskIdentifiers = prefs.taskIdentifiers;
		windowPosition = prefs.windowPosition;
		loadIcons();

		ViewTasksAction a = new ViewTasksAction(app, MSG, this);
		a.setAccelerator(prefs.windowVisibilityAccelerator);
		app.addAction(VIEW_TASKS_ACTION, a);

		if (prefs.windowVisible) {
			toggleTaskWindowVisible(); // Will create and add task window.
		}

	}


	@Override
	public PluginOptionsDialogPanel<TasksPlugin> getOptionsDialogPanel() {
		if (optionPanel == null) {
			optionPanel = new TasksOptionPanel(getApplication(), this);
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
		return MSG.getString("PluginName");
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
						"tasks.properties");
	}


	/**
	 * Returns localized text.
	 *
	 * @param key The key for the localized text.
	 * @return The localized text.
	 */
	String getString(String key) {
		return MSG.getString(key);
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


	@Override
	public void iconGroupChanged(IconGroup iconGroup) {
		window.setIcon(getPluginIcon());
		if (optionPanel != null) {
			optionPanel.setIcon(getPluginIcon());
		}
	}


	@Override
	public void install() {

		RText rtext = getApplication();

		ViewTasksAction vta = (ViewTasksAction)rtext.getAction(VIEW_TASKS_ACTION);
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(vta);
		item.setToolTipText(null);
		item.setSelected(isTaskWindowVisible());
		item.applyComponentOrientation(rtext.getComponentOrientation());

		@SuppressWarnings("unchecked")
		MenuBar<RText> mb = (MenuBar<RText>)rtext.getJMenuBar();
		final JMenu menu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		menu.add(item);

	}


	/**
	 * Returns whether the task window is currently visible.
	 *
	 * @return Whether the task window is currently visible.
	 */
	public boolean isTaskWindowVisible() {
		return window!=null && window.isActive();
	}


	private void loadIcons() {

		icons = new HashMap<>();

		try {

			icons.put(NativeTheme.ID, new ImageIcon(getClass().getResource("eclipse/tasks.png")));

			Image darkThemeImage = ImageTranscodingUtil.rasterize("tasks dark",
				getClass().getResourceAsStream("flat-dark/tasks.svg"), 16, 16);
			icons.put(FlatDarkTheme.ID, new ImageIcon(darkThemeImage));
			icons.put(FlatMacDarkTheme.ID, new ImageIcon(darkThemeImage));

			Image lightThemeImage = ImageTranscodingUtil.rasterize("tasks light",
				getClass().getResourceAsStream("flat-light/tasks.svg"), 16, 16);
			icons.put(FlatLightTheme.ID, new ImageIcon(lightThemeImage));
			icons.put(FlatMacLightTheme.ID, new ImageIcon(lightThemeImage));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
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
				getApplication().displayException(ioe);
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
		AbstractMainView view = getApplication().getMainView();
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


	@Override
	public void savePreferences() {
		TasksPrefs prefs = new TasksPrefs();
		prefs.taskIdentifiers = taskIdentifiers;
		prefs.windowVisible = isTaskWindowVisible();
		prefs.windowPosition = windowPosition;
		ViewTasksAction vta = (ViewTasksAction)getApplication().getAction(VIEW_TASKS_ACTION);
		prefs.windowVisibilityAccelerator = vta.getAccelerator();
		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			getApplication().displayException(ioe);
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
			window = new TaskWindow(this, getApplication(), taskIdentifiers);
			window.setPosition(windowPosition);
			window.setActive(true);
			getApplication().addDockableWindow(window);
			putDockableWindow(DOCKABLE_WINDOW_TASKS, window);
		}
		else {
			window.setActive(!window.isActive());
		}
	}


	@Override
	public boolean uninstall() {
		return true;
	}


}
