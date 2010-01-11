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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;

import org.fife.rtext.RText;
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
public class ToolPlugin implements Plugin {

	private static final String VERSION				= "1.1.0";

	private Icon icon;

	private static final String MSG = "org.fife.rtext.plugins.tools.ToolPlugin";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);

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
	 * {@inheritDoc}
	 */
	public void install(AbstractPluggableGUIApplication app) {
		RText rtext = (RText)app;
		MenuBar mb = (org.fife.ui.app.MenuBar)rtext.getJMenuBar();
		ToolManager tm = ToolManager.get();
		tm.init(rtext);
		mb.addExtraMenu(tm.getToolsMenu());
		mb.revalidate();
	}


	/**
	 * {@inheritDoc}
	 */
	public void savePreferences() {
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean uninstall() {
		return true;
	}


}