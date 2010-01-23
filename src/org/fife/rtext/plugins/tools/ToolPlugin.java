/*
 * 01/06/2010
 *
 * ToolPlugin.java - A plugin that adds external tool support to RText.
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
package org.fife.rtext.plugins.tools;

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
import org.fife.ui.app.MenuBar;
import org.fife.ui.app.Plugin;
import org.fife.ui.app.PluginOptionsDialogPanel;


/**
 * A plugin that adds tool support to RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ToolPlugin implements Plugin, PropertyChangeListener {

	private static final String VERSION				= "1.1.0";

	private RText app;
	private Icon icon;
	private ToolDockableWindow window;
	private JMenu toolsMenu;

	private static final String MSG = "org.fife.rtext.plugins.tools.ToolPlugin";
	protected static final ResourceBundle msg = ResourceBundle.getBundle(MSG);

	static final String NEW_TOOL_ACTION		= "newToolAction";


	/**
	 * Constructor.
	 *
	 * @param app The parent RText application.
	 */
	public ToolPlugin(AbstractPluggableGUIApplication app) {

		URL url = getClass().getResource("tools.png");
		if (url!=null) { // Should always be true
			try {
				icon = new ImageIcon(ImageIO.read(url));
			} catch (IOException ioe) {
				app.displayException(ioe);
			}
		}

		RText rtext = (RText)app;
		this.app = rtext;
		NewToolAction a = new NewToolAction(rtext, msg, null);
//		a.setAccelerator(prefs.getAccelerator(NEW_TOOL_ACTION));
		rtext.addAction(NEW_TOOL_ACTION, a);

	}


	/**
	 * This plugin doesn't add anything to the plugin menu.
	 *
	 * @return <code>false</code> always.
	 */
	public boolean getAddToPluginMenu() {
		return false;
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
	public JMenu getPluginMenu() {
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getPluginName() {
		return "Tools";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPluginVersion() {
		return VERSION;
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
	 * Returns the menu displaying the user tools.
	 *
	 * @return The tools menu.
	 */
	public JMenu getToolsMenu() {
		return toolsMenu;
	}


	/**
	 * {@inheritDoc}
	 */
	public void install(AbstractPluggableGUIApplication app) {

		ToolManager.get().addPropertyChangeListener(ToolManager.PROPERTY_TOOLS,
													this);

		RText rtext = (RText)app;
		MenuBar mb = (org.fife.ui.app.MenuBar)rtext.getJMenuBar();
		toolsMenu = new JMenu(msg.getString("Menu.Name"));
		toolsMenu.addSeparator();
		Action a = rtext.getAction(ToolPlugin.NEW_TOOL_ACTION);
		toolsMenu.add(new JMenuItem(a));//createMenuItem(a));
		mb.addExtraMenu(toolsMenu);
		mb.revalidate();

		window = new ToolDockableWindow(this);
		rtext.addDockableWindow(window);

		loadTools(); // Do after menu has been added

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


	private void refreshToolMenu() {

		while (toolsMenu.getMenuComponentCount()>2) {
			toolsMenu.remove(0);
		}

		if (ToolManager.get().getToolCount()>0) {
			for (Iterator i=ToolManager.get().getToolIterator(); i.hasNext(); ){
				Tool tool = (Tool)i.next();
				RunToolAction a = new RunToolAction(app, tool, window);
				JMenuItem item = new JMenuItem(a);
				toolsMenu.add(item, toolsMenu.getMenuComponentCount()-2);
			}
		}
		else {
			// TODO: Localize me!
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
	 * {@inheritDoc}
	 */
	public boolean uninstall() {
		// TODO: Remove dockable window from application.
		return true;
	}


}