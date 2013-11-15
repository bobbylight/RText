/*
 * 01/06/2010
 *
 * ToolPlugin.java - A plugin that adds external tool support to RText.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.fife.rtext.RText;
import org.fife.rtext.RTextMenuBar;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.GUIPlugin;
import org.fife.ui.app.MenuBar;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.StandardAction;


/**
 * A plugin that adds tool support to RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ToolPlugin extends GUIPlugin implements PropertyChangeListener {

	private static final String VERSION				= "2.5.2";

	private RText app;
	private Icon icon;
	private JMenu toolsMenu;

	private static final String MSG = "org.fife.rtext.plugins.tools.ToolPlugin";
	protected static final ResourceBundle msg = ResourceBundle.getBundle(MSG);

	private static final String EDIT_TOOLS_ACTION		= "editToolsAction";
	private static final String NEW_TOOL_ACTION			= "newToolAction";
	private static final String VIEW_TOOL_OUTPUT_ACTION	= "viewToolOutputAction";
	private static final String DOCKABLE_WINDOW_TOOLS	= "toolsDockableWindow";

	/**
	 * Constructor.
	 *
	 * @param app The parent RText application.
	 */
	public ToolPlugin(AbstractPluggableGUIApplication app) {

		URL url = getClass().getResource("tools.png");
		if (url!=null) { // Should always be true
			icon = new ImageIcon(url);
		}

		ToolsPrefs prefs = loadPrefs();

		RText rtext = (RText)app;
		this.app = rtext;
		StandardAction a = new NewToolAction(rtext, msg, null);
		a.setAccelerator(prefs.newToolAccelerator);
		rtext.addAction(NEW_TOOL_ACTION, a);

		a = new EditToolsAction(rtext, msg, null);
		a.setAccelerator(prefs.editToolsAccelerator);
		rtext.addAction(EDIT_TOOLS_ACTION, a);

		a = new ViewToolOutputAction(rtext, msg, this);
		a.setAccelerator(prefs.windowVisibilityAccelerator);
		rtext.addAction(VIEW_TOOL_OUTPUT_ACTION, a);

		// Current design forces the dockable window to always be created,
		// even if it isn't initially visible
		ToolDockableWindow window = new ToolDockableWindow(this);
		window.setPosition(prefs.windowPosition);
		window.setActive(prefs.windowVisible);
		putDockableWindow(DOCKABLE_WINDOW_TOOLS, window);

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
	 * Returns the currently running tool, if any.  This method should only be
	 * called on the EDT.
	 *
	 * @return The currently running tool, or <code>null</code> if a tool
	 *         isn't running.
	 */
	public Tool getActiveTool() {
		return getDockableWindow().getActiveTool();
	}


	/**
	 * Returns the dockable window for the tool plugin.
	 *
	 * @return The dockable window.
	 */
	public ToolDockableWindow getDockableWindow() {
		return (ToolDockableWindow)getDockableWindow(DOCKABLE_WINDOW_TOOLS);
	}


	/**
	 * {@inheritDoc}
	 */
	public PluginOptionsDialogPanel getOptionsDialogPanel() {
		return new ToolOptionPanel(this);
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
		return msg.getString("Plugin.Name");
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
						"tools.properties");
	}


	/**
	 * Returns the parent application.
	 *
	 * @return the parent application.
	 */
	RText getRText() {
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
	 * Returns the directory that tool definitions are saved to.
	 *
	 * @return The directory.
	 */
	public File getToolDir() {
		return new File(RTextUtilities.getPreferencesDirectory(), "tools");
	}


	/**
	 * {@inheritDoc}
	 */
	public void install(AbstractPluggableGUIApplication app) {

		ToolManager.get().addPropertyChangeListener(ToolManager.PROPERTY_TOOLS,
													this);

		// Add a new menu for selecting tools
		RText rtext = (RText)app;
		MenuBar mb = (org.fife.ui.app.MenuBar)rtext.getJMenuBar();
		toolsMenu = new JMenu(msg.getString("Plugin.Name"));
		Action a = rtext.getAction(ToolPlugin.NEW_TOOL_ACTION);
		toolsMenu.add(createMenuItem(a));
		a = rtext.getAction(ToolPlugin.EDIT_TOOLS_ACTION);
		toolsMenu.add(createMenuItem(a));
		toolsMenu.addSeparator();
		mb.addExtraMenu(toolsMenu);
		mb.revalidate();

		// Add an item to the "View" menu to toggle tool output visibility
		final JMenu menu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		a = rtext.getAction(VIEW_TOOL_OUTPUT_ACTION);
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
				item.setSelected(isToolOutputWindowVisible());
			}
		});

		loadTools(); // Do after menu has been added

	}


	/**
	 * Returns whether the tool output window is visible.
	 *
	 * @return Whether the tool output window is visible.
	 * @see #setToolOutputWindowVisible(boolean)
	 */
	boolean isToolOutputWindowVisible() {
		ToolDockableWindow window = getDockableWindow();
		return window!=null && window.isActive();
	}


	/**
	 * Loads saved preferences for this plugin.  If this
	 * is the first time through, default values will be returned.
	 *
	 * @return The preferences.
	 */
	private ToolsPrefs loadPrefs() {
		ToolsPrefs prefs = new ToolsPrefs();
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
	 * Loads the previously saved tools.
	 *
	 * @see #saveTools()
	 */
	private void loadTools() {

		// First time through, this directory won't exist.
		File toolDir = getToolDir();
		if (!toolDir.isDirectory()) {
			toolDir.mkdirs();
		}

		try {
			ToolManager.get().loadTools(toolDir);
		} catch (IOException ioe) {
			String text = ioe.getMessage();
			if (text==null) {
				text = ioe.toString();
			}
			String desc = msg.getString("Error.LoadingTools");
			desc = MessageFormat.format(desc, new Object[] { text });
			app.displayException(ioe, desc);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String prop = e.getPropertyName();

		if (ToolManager.PROPERTY_TOOLS.equals(prop)) {
			refreshToolMenu();
		}

	}


	/**
	 * Refreshes the elements in the Tools menu to be in sync with the tools
	 * the user has defined.
	 */
	private void refreshToolMenu() {

		while (toolsMenu.getMenuComponentCount()>3) {
			toolsMenu.remove(3);
		}

		if (ToolManager.get().getToolCount()>0) {
			for (Iterator<Tool> i=ToolManager.get().getToolIterator();
					i.hasNext(); ){
				Tool tool = i.next();
				RunToolAction a = new RunToolAction(app, tool, getDockableWindow());
				toolsMenu.add(createMenuItem(a));
			}
		}
		else {
			String text = ToolPlugin.msg.getString("NoToolsDefined");
			JMenuItem item = new JMenuItem(text);
			item.setEnabled(false);
			toolsMenu.add(item);
		}

	}


	/**
	 * {@inheritDoc}
	 */
	public void savePreferences() {

		saveTools();
		ToolDockableWindow window = getDockableWindow();

		ToolsPrefs prefs = new ToolsPrefs();
		prefs.windowPosition = window.getPosition();
		StandardAction a = (StandardAction)app.
										getAction(VIEW_TOOL_OUTPUT_ACTION);
		prefs.windowVisibilityAccelerator = a.getAccelerator();
		prefs.windowVisible = window.isActive();

		a = (StandardAction)app.getAction(NEW_TOOL_ACTION);
		prefs.newToolAccelerator = a.getAccelerator();
		a = (StandardAction)app.getAction(EDIT_TOOLS_ACTION);
		prefs.editToolsAccelerator = a.getAccelerator();

		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			app.displayException(ioe);
		}

	}


	/**
	 * Saves our current set of tools.
	 *
	 * @see #loadTools()
	 */
	private void saveTools() {
		try {
			ToolManager.get().saveTools(getToolDir());
		} catch (IOException ioe) {
			String text = ioe.getMessage();
			if (text==null) {
				text = ioe.toString();
			}
			String desc = msg.getString("Error.SavingTools");
			desc = MessageFormat.format(desc, new Object[] { text });
			app.displayException(ioe, desc);
		}
	}


	/**
	 * Sets the visibility of the tool output window.
	 *
	 * @param visible Whether the window should be visible.
	 * @see #isToolOutputWindowVisible()
	 */
	void setToolOutputWindowVisible(boolean visible) {
		if (visible!=isToolOutputWindowVisible()) {
			ToolDockableWindow window = getDockableWindow();
			if (visible && window==null) {
				window = new ToolDockableWindow(this);
				app.addDockableWindow(window);
			}
			window.setActive(visible);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean uninstall() {
		// TODO: Remove dockable window from application.
		return true;
	}


}