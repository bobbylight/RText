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

import java.util.ResourceBundle;
import javax.swing.ImageIcon;

import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
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

		OptionsDialogPanel[] optionsPanels = new OptionsDialogPanel[9];
		optionsPanels[0] = new GeneralOptionPanel(rtext, msg);
		setIcon(optionsPanels[0], "general.png");
		optionsPanels[1] = new UIOptionPanel(rtext, msg);
		setIcon(optionsPanels[1], "ui.png");
		optionsPanels[1].addChildPanel(new LanguageOptionPanel(rtext, msg));
		setIcon(optionsPanels[1].getChildPanel(0), "language.png");
		optionsPanels[2] = new RTextAreaOptionPanel();
		setIcon(optionsPanels[2], "textarea.png");
		optionsPanels[2].addChildPanel(new CaretAndSelectionOptionPanel());
		optionsPanels[2].addChildPanel(new RSyntaxTextAreaOptionPanel());
		optionsPanels[2].addChildPanel(new GutterOptionPanel());
		optionsPanels[2].addChildPanel(new SpellingOptionPanel());
		optionsPanels[2].addChildPanel(new TemplateOptionPanel());
		optionsPanels[2].addChildPanel(new MacroOptionPanel(rtext, msg));
		optionsPanels[3] = new RTextFileChooserOptionPanel();
		setIcon(optionsPanels[3], "file_chooser.png");
		optionsPanels[3].addChildPanel(new FileChooserFavoritesOptionPanel());
		optionsPanels[4] = new PrintingOptionPanel(rtext, msg);
		setIcon(optionsPanels[4], "printing.png");
		//optionsPanels[5] = new LanguageOptionPanel(rtext, msg);
		//setIcon(optionsPanels[5], "language.png");
		optionsPanels[5] = new FileFilterOptionPanel(rtext, msg);
		setIcon(optionsPanels[5], "file_filters.png");
		optionsPanels[6] = new ShortcutOptionPanel(rtext, msg);
		setIcon(optionsPanels[6], "shortcuts.png");
		optionsPanels[7] = new XmlOptionPanel(rtext, msg);
		setIcon(optionsPanels[7], "xml.png");
		optionsPanels[8] = new ToolOptionPanel(rtext);
		setIcon(optionsPanels[8], "tools.png");
		msg = null;

		setOptionsPanels(optionsPanels); // Calls pack().

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