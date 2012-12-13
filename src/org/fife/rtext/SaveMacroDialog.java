/*
 * 09/16/2004
 *
 * SaveMacroDialog.java - A dialog prompting the user to save a macro.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.*;

import org.fife.ui.RButton;
import org.fife.ui.TitledPanel;
import org.fife.ui.UIUtil;


/**
 * A dialog prompting the user to save a macro.
 *
 * @author Robert Futrell
 * @version 0.5
 */
class SaveMacroDialog extends JDialog implements ActionListener {

	private RText rtext;

	private RButton okButton;
	private RButton cancelButton;

	private JTextField macroNameField;


	/**
	 * Creates a new <code>SaveMacroDialog</code>.
	 *
	 * @param rtext The owner of this dialog.
	 */
	public SaveMacroDialog(RText rtext) {

		super(rtext);
		this.rtext = rtext;
		ResourceBundle bundle = rtext.getResourceBundle();

		JPanel temp = new JPanel(new BorderLayout());
		temp.setBorder(BorderFactory.createEmptyBorder(10,5,5,5));

		// Panel to input the action's name.
		JPanel namePanel = new JPanel(new BorderLayout());
		JLabel label = UIUtil.createLabel(bundle, "MacroNameLabel");
		namePanel.add(label, BorderLayout.LINE_START);
		macroNameField = new JTextField(30);
		label.setLabelFor(macroNameField);
		namePanel.add(macroNameField);
		temp.add(namePanel, BorderLayout.NORTH);

		// Panel for the buttons.
		okButton = UIUtil.createRButton(bundle, "OKButtonLabel",
											"OKButtonMnemonic");
		cancelButton = UIUtil.createRButton(bundle, "Cancel",
											"CancelMnemonic");
		okButton.setActionCommand("OKButtonPressed");
		okButton.addActionListener(this);
		cancelButton.setActionCommand("CancelButtonPressed");
		cancelButton.addActionListener(this);
		JPanel buttonPanel = new JPanel(new GridLayout(1,2, 5,0));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(buttonPanel);
		
		temp.add(bottomPanel, BorderLayout.SOUTH);

		// Create the content pane and put everything in it!
		String saveMacro = bundle.getString("Dialog.SaveMacro.SaveMacro");
		JPanel contentPane = new TitledPanel(saveMacro, temp,
									TitledPanel.BEVEL_BORDER);
		setContentPane(contentPane);
		getRootPane().setDefaultButton(okButton);
		setTitle(saveMacro);
		setModal(true);
		//setResizable(false);
		setLocationRelativeTo(rtext);
		pack();

	}


	/**
	 * Listens for actions in this dialog.
	 *
	 * @param e The event that occurred.
	 */
	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		// If they press "OK", save the action.
		if (actionCommand.equals("OKButtonPressed")) {
			String name = macroNameField.getText();
			if (name!=null && name.length()>0) {

				// Add macro extension to the end of it if it isn't already
				// there.
				if (!name.endsWith(RTextUtilities.MACRO_EXTENSION))
					name += RTextUtilities.MACRO_EXTENSION;

				File macroDir = RTextUtilities.getMacroDirectory();
				if (macroDir.isDirectory()) {
					File macroFile = new File(macroDir, name);
					try {
						RTextEditorPane.getCurrentMacro().
								saveToFile(macroFile.getAbsolutePath());
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(this,
							"Error saving macro -\n" + ex,
							rtext.getResourceBundle()
									.getString("ErrorDialogTitle"),
							JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					ResourceBundle msg = rtext.getResourceBundle();
					JOptionPane.showMessageDialog(this,
						msg.getString("MacroInvalidDir"),
						msg.getString("ErrorDialogTitle"),
						JOptionPane.ERROR_MESSAGE);
				}

				this.setVisible(false);

			}
		}

		// If they press "Cancel", just hide the dialog without saving the
		// macro.
		else if (actionCommand.equals("CancelButtonPressed")) {
			this.setVisible(false);
		}

	}


}