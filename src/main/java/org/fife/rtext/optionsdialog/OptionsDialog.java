/*
 * 11/09/2009
 *
 * OptionsDialog.java - RText's options dialog.
 * Copyright (C) 2009 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.optionsdialog;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.app.Plugin;
import org.fife.ui.app.icons.IconGroup;
import org.fife.ui.app.options.ShortcutOptionPanel;
import org.fife.ui.rsyntaxtextarea.*;
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
		rtaPanel.addChildPanel(new FontAndTabsOptionPanel());
		RSyntaxTextAreaOptionPanel rstaPanel = new RSyntaxTextAreaOptionPanel();
		rtaPanel.addChildPanel(rstaPanel);
		rstaPanel.addChildPanel(new CaretAndSelectionOptionPanel());
		rstaPanel.addChildPanel(new HighlightsOptionPanel());
		rstaPanel.addChildPanel(new GutterOptionPanel());
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

		rtext.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, e -> updateIcons());
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


	private void updateIcons() {

		IconGroup iconGroup = rtext.getIconGroup();

		generalPanel.setIcon(iconGroup.getIcon("options_general"));
		uiPanel.setIcon(iconGroup.getIcon("options_ui"));
		languagePanel.setIcon(iconGroup.getIcon("options_language"));
		rtaPanel.setIcon(iconGroup.getIcon("options_textarea"));
		searchPanel.setIcon(iconGroup.getIcon("options_search"));
		fileChooserPanel.setIcon(iconGroup.getIcon("options_file_chooser"));
		filtersPanel.setIcon(iconGroup.getIcon("options_file_filters"));
		shortcutPanel.setIcon(iconGroup.getIcon("options_shortcuts"));
	}
}
