/*
 * 11/14/2003
 *
 * OptionsAction - Action to display the Options dialog in RText.
 * Copyright (C) 2003 Robert Futrell
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
package org.fife.rtext;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.fife.ui.OptionsDialog;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.app.StandardAction;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaOptionPanel;
import org.fife.ui.rsyntaxtextarea.TemplateOptionPanel;
import org.fife.ui.rtextarea.CaretAndSelectionOptionPanel;
import org.fife.ui.rtextarea.GutterOptionPanel;
import org.fife.ui.rtextarea.RTextAreaOptionPanel;
import org.fife.ui.rtextfilechooser.FileChooserFavoritesOptionPanel;
import org.fife.ui.rtextfilechooser.RTextFileChooserOptionPanel;


/**
 * Action used by an <code>RTextDocumentTabbedPane</code> to display the
 * options dialog.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class OptionsAction extends StandardAction {


	/**
	 * Creates a new <code>OptionsAction</code>.
	 *
	 * @param rtext The <code>RText</code> instance for which you're changing
	 *        options.
	 * @param text The text associated with the action.
	 * @param icon The icon associated with the action.
	 * @param desc The description of the action.
	 * @param mnemonic The mnemonic for the action.
	 * @param accelerator The accelerator key for the action.
	 */
	public OptionsAction(RText rtext, String text, Icon icon, String desc,
						int mnemonic, KeyStroke accelerator) {
		super(rtext, text, icon, desc, mnemonic, accelerator);
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action event.
	 */
	public void actionPerformed(ActionEvent e) {

		RText rtext = (RText)getApplication();
		rtext.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		OptionsDialog od = null;

		try {
			od = getOptionsDialog();
			od.initialize();
		} finally {
			// Make sure cursor returns to normal.
			rtext.setCursor(Cursor.getPredefinedCursor(
											Cursor.DEFAULT_CURSOR));
		}

		od.setVisible(true); // Takes care of updating values itself.

	}


	protected OptionsDialog getOptionsDialog() {

		RText rtext = (RText)getApplication();
		if (rtext.optionsDialog==null) {

			rtext.optionsDialog = new OptionsDialog(rtext);
			ResourceBundle msg = ResourceBundle.getBundle(
									"org.fife.rtext.OptionsDialog");

			OptionsDialogPanel[] optionsPanels = new OptionsDialogPanel[8];
			optionsPanels[0] = new GeneralOptionPanel(rtext, msg);
			optionsPanels[1] = new UIOptionPanel(rtext, msg);
			optionsPanels[2] = new RTextAreaOptionPanel();
			optionsPanels[2].addChildPanel(new CaretAndSelectionOptionPanel());
			optionsPanels[2].addChildPanel(new RSyntaxTextAreaOptionPanel());
			optionsPanels[2].addChildPanel(new GutterOptionPanel());
			optionsPanels[2].addChildPanel(new TemplateOptionPanel());
			optionsPanels[2].addChildPanel(new MacroOptionPanel(rtext, msg));
			optionsPanels[3] = new RTextFileChooserOptionPanel();
			optionsPanels[3].addChildPanel(new FileChooserFavoritesOptionPanel());
			optionsPanels[4] = new PrintingOptionPanel(rtext, msg);
			optionsPanels[5] = new LanguageOptionPanel(rtext, msg);
			optionsPanels[6] = new FileFilterOptionPanel(rtext, msg);
			optionsPanels[7] = new ShortcutOptionPanel(rtext, msg);

			msg = null;

			OptionsDialog od = rtext.optionsDialog;
			od.setOptionsPanels(optionsPanels); // Calls pack().
			od.setLocationRelativeTo(rtext);

		}

		return rtext.optionsDialog;

	}


}