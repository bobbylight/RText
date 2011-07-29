/*
 * 06/29/2011
 *
 * NewMacroDialog.java - Dialog for defining a new macro.
 * Copyright (C) 2011 Robert Futrell
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
package org.fife.rtext.plugins.macros;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.fife.rtext.KeyStrokeField;
import org.fife.rtext.RText;
import org.fife.ui.EscapableDialog;
import org.fife.ui.RButton;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;


/**
 * A dialog allowing the user to create a new macro.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class NewMacroDialog extends EscapableDialog {

	private RText rtext;
	private JComboBox typeCombo;
	private SelectableLabel engineNotesLabel;
	private RButton okButton;
	private RButton cancelButton;
	private Macro macro;

	private static final String[] NOTE_TYPES = { "Rhino", "Groovy", };

	private static final String MSG = "org.fife.rtext.plugins.macros.NewMacroDialog";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	public NewMacroDialog(MacroPlugin plugin, JDialog parent) {
		super(parent);
		createGUI(plugin);
	}


	public NewMacroDialog(MacroPlugin plugin, JFrame parent) {
		super(parent);
		createGUI(plugin);
	}


	public void createGUI(MacroPlugin plugin) {

		rtext = plugin.getRText();
		ResourceBundle parentMsg = rtext.getResourceBundle();
		JPanel cp = new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(UIUtil.getEmpty5Border());
		Listener l = new Listener();

		Box topPanel = Box.createVerticalBox();
		cp.add(topPanel, BorderLayout.NORTH);
		String descText = msg.getString("Header.Text");
		SelectableLabel desc = new SelectableLabel(descText);
		topPanel.add(desc);
		topPanel.add(Box.createVerticalStrut(5));

		// Panel for defining the macro
		SpringLayout sl = new SpringLayout();
		JPanel formPanel = new JPanel(sl);
		JLabel nameLabel = UIUtil.createLabel(msg, "Label.Name", "Label.Name.Mnemonic");
		JTextField nameField = new JTextField(40);
		JLabel descLabel = UIUtil.createLabel(msg, "Label.Desc", "Label.Desc.Mnemonic");
		JTextField descField = new JTextField(40);
		JLabel shortcutLabel = UIUtil.createLabel(msg, "Label.Shortcut", "Label.Shortcut.Mnemonic");
		KeyStrokeField shortcutField = new KeyStrokeField();
		JLabel typeLabel = UIUtil.createLabel(msg, "Label.Type", "Label.Type.Mnemonic");
		String[] items = { "Rhino (JavaScript)", "Groovy" };
		typeCombo = new JComboBox(items);
		typeCombo.addActionListener(l);
		typeCombo.setEditable(false);
		if (rtext.getComponentOrientation().isLeftToRight()) {
			formPanel.add(nameLabel);     formPanel.add(nameField);
			formPanel.add(descLabel);     formPanel.add(descField);
			formPanel.add(shortcutLabel); formPanel.add(shortcutField);
			formPanel.add(typeLabel);     formPanel.add(typeCombo);
		}
		else {
			formPanel.add(nameField);     formPanel.add(nameLabel);
			formPanel.add(descField);     formPanel.add(descLabel);
			formPanel.add(shortcutField); formPanel.add(shortcutLabel);
			formPanel.add(typeCombo);     formPanel.add(typeLabel);
		}
		UIUtil.makeSpringCompactGrid(formPanel, 4, 2,
										5, 5, 5, 5);
		topPanel.add(formPanel);
		topPanel.add(Box.createVerticalStrut(10));
		engineNotesLabel = new SelectableLabel(getEngineNote(0));
		topPanel.add(engineNotesLabel);
		topPanel.add(Box.createVerticalGlue());

		// Panel for the buttons.
		okButton = UIUtil.createRButton(parentMsg, "OKButtonLabel",
											"OKButtonMnemonic");
		cancelButton = UIUtil.createRButton(parentMsg, "Cancel",
											"CancelMnemonic");
		okButton.addActionListener(l);
		cancelButton.addActionListener(l);
		JPanel buttonPanel = new JPanel(new GridLayout(1,2, 5,0));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(buttonPanel);
		cp.add(bottomPanel, BorderLayout.SOUTH);
		
		setContentPane(cp);
		setTitle(msg.getString("Title"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModal(true);
		pack();
		setSize(500, getHeight()+60); // Enough for line wrapping
		setLocationRelativeTo(rtext);

	}


	/**
	 * Returns any notes about the specified scripting engine.
	 *
	 * @param index The index of the script engine in the combo box.
	 * @return The notes.
	 */
	private String getEngineNote(int index) {
		String note = msg.getString("EngineNotes." + NOTE_TYPES[index]);
		note = MessageFormat.format(note,
				new Object[] { rtext.getInstallLocation() });
		return note;
	}


	/**
	 * Returns the macro created by the user, or <code>null</code> if this
	 * dialog was canceled.
	 *
	 * @return The macro created.
	 */
	public Macro getMacro() {
		return macro;
	}


	/**
	 * Initializes the fields in this dialog to show the values for a
	 * specific macro.
	 *
	 * @param macro The macro.
	 */
	public void setMacro(Macro macro) {
		// TODO
	}


	/**
	 * Listens for events in this dialog.
	 */
	private class Listener implements ActionListener {


		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();
			//String command = e.getActionCommand();

			if (source==typeCombo) {
				engineNotesLabel.setText(getEngineNote(typeCombo.getSelectedIndex()));
			}

			else if (source==okButton) {
				macro = new Macro();
				macro.setName("test");
				macro.setDesc("A test macro.");
				macro.setFile(new java.io.File("c:/temp/test.js"));
				macro.setAccelerator(null);
				escapePressed();
			}

			else if (source==cancelButton) {
				escapePressed();
			}

		}


	}


}