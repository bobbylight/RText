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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;

import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.app.Plugin;
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


	/**
	 * Constructor.
	 *
	 * @param rtext The parent application instance.
	 */
	public OptionsDialog(RText rtext) {

		super(rtext);

		ResourceBundle msg = ResourceBundle.getBundle(
								"org.fife.rtext.OptionsDialog");

		List<OptionsDialogPanel> panels = new ArrayList<OptionsDialogPanel>();

		OptionsDialogPanel panel = new GeneralOptionPanel(rtext, msg);
		setIcon(panel, "general.png");
		panels.add(panel);

		panel = new UIOptionPanel(rtext, msg);
		setIcon(panel, "ui.png");
		panels.add(panel);

		OptionsDialogPanel panel2 = new LanguageOptionPanel(rtext, msg);
		setIcon(panel2, "language.png");
		panel.addChildPanel(panel2);

		panel = new RTextAreaOptionPanel();
		setIcon(panel, "textarea.png");
		panels.add(panel);

		panel.addChildPanel(new RSyntaxTextAreaOptionPanel());
		panel.addChildPanel(new CaretAndSelectionOptionPanel());
		panel.addChildPanel(new GutterOptionPanel());
		panel.addChildPanel(new SpellingOptionPanel());
		panel.addChildPanel(new TemplateOptionPanel());

		panel = new SearchOptionPanel(rtext, msg);
		setIcon(panel, "search.png");
		panels.add(panel);

		panel = new RTextFileChooserOptionPanel();
		setIcon(panel, "file_chooser.png");
		panels.add(panel);

		panel.addChildPanel(new FileChooserFavoritesOptionPanel());

		panel = new PrintingOptionPanel(rtext, msg);
		setIcon(panel, "printing.png");
		panels.add(panel);

		//panel = new LanguageOptionPanel(rtext, msg);
		//setIcon(panel, "language.png");
		//optionsPanels.add(panel);

		panel = new FileFilterOptionPanel(rtext, msg);
		setIcon(panel, "file_filters.png");
		panels.add(panel);

		panel = new ShortcutOptionPanel(rtext, msg);
		setIcon(panel, "shortcuts.png");
		panels.add(panel);

		Plugin[] plugins = rtext.getPlugins();
		for (int i=0; i<plugins.length; i++) {
			Plugin plugin = plugins[i];
			panel = plugin.getOptionsDialogPanel();
			if (panel!=null) {
				String parentID = plugin.getOptionsDialogPanelParentPanelID();
				if (parentID!=null) {
					OptionsDialogPanel parent = getPanelById(panels, parentID);
					if (parent!=null) {
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
	private OptionsDialogPanel getPanelById(List<OptionsDialogPanel> panels,
			String id) {
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
	private OptionsDialogPanel getPanelByIdImpl(OptionsDialogPanel panel,
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
	 * @param iconSuffix The suffix of the icon resource.
	 */
	private void setIcon(OptionsDialogPanel panel, String iconSuffix) {
		ClassLoader cl = getClass().getClassLoader();
		String prefix = "org/fife/rtext/graphics/options_";
		panel.setIcon(new ImageIcon(cl.getResource(prefix + iconSuffix)));
	}


}