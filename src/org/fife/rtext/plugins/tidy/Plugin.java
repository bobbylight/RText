/*
 * 03/22/2010
 *
 * Plugin.java - Plugin that "tidies" source code.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tidy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.rtext.RTextMenuBar;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.AbstractPlugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * A plugin that "tidies" source code. This code calls into other libraries
 * for its functionality.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Plugin extends AbstractPlugin
		implements PropertyChangeListener {

	private RText rtext;
	private TidyAction action;
	private Icon icon;

	private HtmlOptions htmlOptions;
	private XmlOptions xmlOptions;

	private static final String PLUGIN_VERSION			= "2.0.2";
	private static final String PREFS_DIR_NAME			= "tidy";

	private static final String MSG = "org.fife.rtext.plugins.tidy.Plugin";

	/**
	 * The resource bundle used across this plugin.
	 */
	static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	public Plugin(AbstractPluggableGUIApplication app) {
		this.rtext = (RText)app; // Needed in loadPreferences if error
		loadPreferences();
	}


	/**
	 * Returns the options related to tidying HTML.
	 *
	 * @return The HTML options.
	 */
	public HtmlOptions getHtmlOptions() {
		return htmlOptions;
	}


	/**
	 * {@inheritDoc}
	 */
	public PluginOptionsDialogPanel getOptionsDialogPanel() {
		return new OptionsPanel(this);
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
		if (icon==null) {
			URL url = getClass().getResource("lightning.png");
			if (url!=null) { // Should always be true
				try {
					icon = new ImageIcon(ImageIO.read(url));
				} catch (IOException ioe) {
					rtext.displayException(ioe);
				}
			}
		}
		return icon;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getPluginName() {
		return msg.getString("Name");
	}


	/**
	 * {@inheritDoc}
	 */
	public String getPluginVersion() {
		return PLUGIN_VERSION;
	}


	/**
	 * Returns the options related to tidying XML.
	 *
	 * @return The XML tidying-options.
	 */
	public XmlOptions getXmlOptions() {
		return xmlOptions;
	}


	/**
	 * {@inheritDoc}
	 */
	public void install(AbstractPluggableGUIApplication app) {

		RTextMenuBar mb = (RTextMenuBar)rtext.getJMenuBar();
		JMenu menu = mb.getMenuByName(RTextMenuBar.MENU_EDIT);

		action = new TidyAction(app, this);
		action.setEnabled(false); // Gets enabled for appropriate files.
		JMenuItem item = new JMenuItem(action);
		item.setToolTipText(null);

		// On OS X, the "Options" menu item is in the application menu, not
		// in the Edit menu.
		if (rtext.getOS()==RText.OS_MAC_OSX) {
			menu.addSeparator();
			menu.add(item);
		}
		else {
			int index = menu.getMenuComponentCount() - 2;
			menu.insert(item, index);
			menu.insertSeparator(index);
		}

		rtext.getMainView().addPropertyChangeListener(
							AbstractMainView.CURRENT_DOCUMENT_PROPERTY, this);
	}


	/**
	 * Loads our tidying preferences.
	 */
	private void loadPreferences() {

		File prefsDir = new File(RTextUtilities.getPreferencesDirectory(),
									PREFS_DIR_NAME);

		if (!prefsDir.isDirectory()) {
			prefsDir.mkdirs();
		}

		if (prefsDir.isDirectory()) { // Always true, just being paranoid
			htmlOptions = new HtmlOptions();
			xmlOptions = new XmlOptions();
			try {
				File file = new File(prefsDir, "html.propeties");
				if (file.isFile()) {
					htmlOptions.load(file);
				}
				file = new File(prefsDir, "xml.propeties");
				if (file.isFile()) {
					xmlOptions.load(file);
				}
			} catch (IOException ioe) {
				rtext.displayException(ioe);
			}
		}

	}


	/**
	 * Called whenever a change we're interested in occurs in RText.
	 *
	 * @param e The event.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String name = e.getPropertyName();

		// If the current file changed.
		if (AbstractMainView.CURRENT_DOCUMENT_PROPERTY.equals(name)) {
			RTextEditorPane textArea = rtext.getMainView().getCurrentTextArea();
			String style = textArea.getSyntaxEditingStyle();
			boolean supported =
				SyntaxConstants.SYNTAX_STYLE_HTML.equals(style) ||
				SyntaxConstants.SYNTAX_STYLE_XML.equals(style);
			action.setEnabled(supported);
		}

	}


	/**
	 * {@inheritDoc}
	 */
	public void savePreferences() {

		File prefsDir = new File(RTextUtilities.getPreferencesDirectory(),
									PREFS_DIR_NAME);
		if (!prefsDir.isDirectory()) {
			prefsDir.mkdirs();
		}

		if (prefsDir.isDirectory()) { // Should always be true
			try {
				File file = new File(prefsDir, "html.propeties");
				getHtmlOptions().save(file);
				file = new File(prefsDir, "xml.propeties");
				getXmlOptions().save(file);
			} catch (IOException ioe) {
				rtext.displayException(ioe);
			}
		}

	}


	/**
	 * {@inheritDoc}
	 */
	public boolean uninstall() {
		return true;
	}


}