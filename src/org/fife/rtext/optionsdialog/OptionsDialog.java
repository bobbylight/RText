/*
 * 11/09/2009
 *
 * OptionsDialog.java - RText's options dialog.
 * Copyright (C) 2009 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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

		List panels = new ArrayList();

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

		panel.addChildPanel(new CaretAndSelectionOptionPanel());
		panel.addChildPanel(new RSyntaxTextAreaOptionPanel());
		panel.addChildPanel(new GutterOptionPanel());
		panel.addChildPanel(new SpellingOptionPanel());
		panel.addChildPanel(new TemplateOptionPanel());
		panel.addChildPanel(new MacroOptionPanel(rtext, msg));

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

		panel = new XmlOptionPanel(rtext, msg);
		setIcon(panel, "xml.png");
		panels.add(panel);

		Plugin[] plugins = rtext.getPlugins();
		for (int i=0; i<plugins.length; i++) {
			Plugin plugin = plugins[i];
			panel = plugin.getOptionsDialogPanel();
			if (panel!=null) {
				panels.add(panel);
			}
		}
//		panel = new ToolOptionPanel(rtext);
//		setIcon(panel, "tools.png");


		OptionsDialogPanel[] array = new OptionsDialogPanel[panels.size()];
		array = (OptionsDialogPanel[])panels.toArray(array);
		setOptionsPanels(array); // Calls pack().

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