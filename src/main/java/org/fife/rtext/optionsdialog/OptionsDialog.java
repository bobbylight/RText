/*
 * 11/09/2009
 *
 * OptionsDialog.java - RText's options dialog.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.optionsdialog;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.*;

import org.fife.rtext.RText;
import org.fife.ui.ImageTranscodingUtil;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.app.Plugin;
import org.fife.ui.app.options.ShortcutOptionPanel;
import org.fife.ui.app.themes.FlatDarkTheme;
import org.fife.ui.app.themes.FlatLightTheme;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaOptionPanel;
import org.fife.ui.rsyntaxtextarea.SpellingOptionPanel;
import org.fife.ui.rsyntaxtextarea.TemplateOptionPanel;
import org.fife.ui.rtextarea.CaretAndSelectionOptionPanel;
import org.fife.ui.rtextarea.GutterOptionPanel;
import org.fife.ui.rtextarea.RTextAreaOptionPanel;
import org.fife.ui.rtextfilechooser.FileChooserFavoritesOptionPanel;
import org.fife.ui.rtextfilechooser.RTextFileChooserOptionPanel;


/**
 * RText's options dialog.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class OptionsDialog extends org.fife.ui.OptionsDialog {

	private RText rtext;
	private OptionsDialogPanel generalPanel;
	private OptionsDialogPanel uiPanel;
	private OptionsDialogPanel languagePanel;
	private OptionsDialogPanel rtaPanel;
	private OptionsDialogPanel searchPanel;
	private OptionsDialogPanel fileChooserPanel;
	private OptionsDialogPanel filtersPanel;
	private OptionsDialogPanel shortcutPanel;


	/**
	 * Constructor.
	 *
	 * @param rtext The parent application instance.
	 */
	public OptionsDialog(RText rtext) {

		super(rtext);
		this.rtext = rtext;

		ResourceBundle msg = ResourceBundle.getBundle(
								"org.fife.rtext.OptionsDialog");

		List<OptionsDialogPanel> panels = new ArrayList<>();

		generalPanel = new GeneralOptionPanel(rtext, msg);
		panels.add(generalPanel);

		uiPanel = new UIOptionPanel(rtext, msg);
		panels.add(uiPanel);

		languagePanel = new LanguageOptionPanel(rtext, msg);
		uiPanel.addChildPanel(languagePanel);

		rtaPanel = new RTextAreaOptionPanel();
		panels.add(rtaPanel);

		rtaPanel.addChildPanel(new RSyntaxTextAreaOptionPanel());
		rtaPanel.addChildPanel(new CaretAndSelectionOptionPanel());
		rtaPanel.addChildPanel(new GutterOptionPanel());
		rtaPanel.addChildPanel(new SpellingOptionPanel());
		rtaPanel.addChildPanel(new TemplateOptionPanel());

		searchPanel = new SearchOptionPanel(rtext, msg);
		panels.add(searchPanel);

		fileChooserPanel = new RTextFileChooserOptionPanel();
		panels.add(fileChooserPanel);

		fileChooserPanel.addChildPanel(new FileChooserFavoritesOptionPanel());

		OptionsDialogPanel printPanel = new PrintingOptionPanel(rtext, msg);
		panels.add(printPanel);

		filtersPanel = new FileFilterOptionPanel(rtext, msg);
		panels.add(filtersPanel);

		shortcutPanel = new ShortcutOptionPanel(rtext);
		panels.add(shortcutPanel);

		Plugin<?>[] plugins = rtext.getPlugins();
		for (Plugin<?> plugin : plugins) {
			OptionsDialogPanel panel = plugin.getOptionsDialogPanel();
			if (panel != null) {
				panel.setIcon(plugin.getPluginIcon());
				String parentID = plugin.getOptionsDialogPanelParentPanelID();
				if (parentID != null) {
					OptionsDialogPanel parent = getPanelById(panels, parentID);
					if (parent != null) {
						parent.addChildPanel(panel);
					}
					else { // Unknown parent specified...
						panels.add(panel);
					}
				}
				else {
					panels.add(panel);
				}
			}
		}

		rtext.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, (e) -> updateIcons());
		updateIcons();

		OptionsDialogPanel[] array = new OptionsDialogPanel[panels.size()];
		array = panels.toArray(array);
		setOptionsPanels(array); // Calls pack().

	}


	/**
	 * Returns the options dialog panel with the specified ID.
	 *
	 * @param panels The already added panels.
	 * @param id The panel ID to search for.  Should not be <code>null</code>.
	 * @return The panel, or <code>null</code> if it wasn't found.
	 */
	private static OptionsDialogPanel getPanelById(
			List<OptionsDialogPanel> panels, String id) {
		for (OptionsDialogPanel panel : panels) {
			OptionsDialogPanel result = getPanelByIdImpl(panel, id);
			if (result!=null) {
				return result;
			}
		}
		return null;
	}


	/**
	 * Scans a panel and its children recursively, checking for the panel
	 * with the specified ID.
	 *
	 * @param id The ID of the panel to search for.
	 * @return The panel, or <code>null</code> if it wasn't found.
	 */
	private static OptionsDialogPanel getPanelByIdImpl(OptionsDialogPanel panel,
			String id) {
		if (id.equals(panel.getId())) {
			return panel;
		}
		for (int i=0; i<panel.getChildPanelCount(); i++) {
			OptionsDialogPanel child = panel.getChildPanel(i);
			OptionsDialogPanel result = getPanelByIdImpl(child, id);
			if (result!=null) {
				return result;
			}
		}
		return null;
	}


	/**
	 * Sets the icon for an options panel.
	 *
	 * @param panel The options panel.
	 * @param packageName The package from which to load icons.
	 * @param iconSuffix The suffix of the icon resource.
	 */
	private void setIcon(OptionsDialogPanel panel, String packageName, String iconSuffix) {

		ClassLoader cl = getClass().getClassLoader();
		String prefix = "org/fife/rtext/graphics/" + packageName + "/options_";
		Icon icon = null;

		if (iconSuffix.endsWith(".svg")) {
			InputStream in = cl.getResourceAsStream(prefix + iconSuffix);
			if (in != null) {
				try {
					Image image = ImageTranscodingUtil.rasterize(iconSuffix, in, 16, 16);
					icon = new ImageIcon(image);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		else {
			URL resource = cl.getResource(prefix + iconSuffix);
			if (resource != null) {
				icon = new ImageIcon(resource);
			}
		}

		panel.setIcon(icon);
	}


	private void updateIcons() {

		String packageName;
		String extension;

		switch (rtext.getTheme().getId()) {
			case FlatDarkTheme.ID -> {
				packageName = "flat-dark";
				extension = "svg";
			}
			case FlatLightTheme.ID -> {
				packageName = "flat-light";
				extension = "svg";
			}
			default -> {
				packageName = "eclipse";
				extension = "png";
			}
		}

		setIcon(generalPanel, packageName, "general." + extension);
		setIcon(uiPanel, packageName, "ui." + extension);
		setIcon(languagePanel, packageName, "language." + extension);
		setIcon(rtaPanel, packageName, "textarea." + extension);
		setIcon(searchPanel, packageName, "search." + extension);
		setIcon(fileChooserPanel, packageName, "file_chooser." + extension);
		setIcon(filtersPanel, packageName, "file_filters." + extension);
		setIcon(shortcutPanel, packageName, "shortcuts." + extension);
	}
}
