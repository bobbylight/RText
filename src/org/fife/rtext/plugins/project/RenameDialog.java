/*
 * 08/28/2012
 *
 * RenameDialog.java - Dialog for renaming workspace tree nodes.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.rsta.ui.DecorativeIconPanel;
import org.fife.rsta.ui.EscapableDialog;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.plugins.project.tree.NameChecker;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;


/**
 * A dialog for renaming nodes in the workspace outline tree.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RenameDialog extends EscapableDialog{

	private JPanel topPanel;
	private JLabel nameLabel;
	private JButton okButton;
	private JButton cancelButton;
	private JTextField nameField;
	private DecorativeIconPanel renameDIP;
	private NameChecker nameChecker;

	private static Icon ERROR_ICON;


	/**
	 * Constructor.
	 *
	 * @param owner The rtext window that owns this dialog.
	 * @param type The type of node being renamed.
	 */
	public RenameDialog(RText owner, String type, NameChecker checker) {

		super(owner);
		Listener listener = new Listener();
		this.nameChecker = checker;

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
		ResourceBundle bundle = owner.getResourceBundle();

		JPanel cp =new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		setContentPane(cp);

		// A panel containing the main content.
		String key = "RenameDialog.Field.Label";
		nameLabel = new JLabel(Messages.getString(key));
		nameLabel.setDisplayedMnemonic(Messages.getString(key + ".Mnemonic").charAt(0));
		nameField = new JTextField(40);
		nameField.getDocument().addDocumentListener(listener);
		nameLabel.setLabelFor(nameField);
		renameDIP = new DecorativeIconPanel();
		Box box = new Box(BoxLayout.LINE_AXIS);
		box.add(nameLabel);
		box.add(Box.createHorizontalStrut(5));
		box.add(RTextUtilities.createAssistancePanel(nameField, renameDIP));
		box.add(Box.createHorizontalGlue());
		topPanel = new JPanel(new BorderLayout());
		topPanel.add(box, BorderLayout.SOUTH);

		// Make a panel containing the OK and Cancel buttons.
		okButton = UIUtil.newButton(bundle, "OKButtonLabel", "OKButtonMnemonic");
		okButton.setActionCommand("OK");
		okButton.addActionListener(listener);
		cancelButton = UIUtil.newButton(bundle, "Cancel", "CancelMnemonic");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(listener);

		// Put everything into a neat little package.
		cp.add(topPanel, BorderLayout.NORTH);
		Container buttons = UIUtil.createButtonFooter(okButton, cancelButton);
		cp.add(buttons, BorderLayout.SOUTH);
		JRootPane rootPane = getRootPane();
		rootPane.setDefaultButton(okButton);
		setTitle(Messages.getString("RenameDialog.Title", type));
		setModal(true);
		applyComponentOrientation(orientation);
		packSpecial();
		setLocationRelativeTo(owner);

	}


	/**
	 * Overridden to set the name "the user entered" to <code>null</code>.
	 */
	@Override
	public void escapePressed() {
		nameField.setText(null); // So user gets back nothing
		super.escapePressed();
	}


	/**
	 * Returns the icon to use for fields with errors.
	 *
	 * @return The icon.
	 */
	public static Icon getErrorIcon() {
		if (ERROR_ICON==null) {
			URL res = RenameDialog.class.getResource("error_co.gif");
			ERROR_ICON = new ImageIcon(res);
		}
		return ERROR_ICON;
	}


	/**
	 * Returns a localized error message to use in this dialog.
	 *
	 * @param key The (part of the) key to display.
	 * @return The localized error message.
	 */
	public static String getLocalizedReason(String key) {
		return Messages.getString("RenameDialog.InvalidName." + key);
	}


	/**
	 * Returns the name selected.
	 *
	 * @return The name selected, or <code>null</code> if the dialog was
	 *         cancelled.
	 * @see #setName(String)
	 */
	@Override
	public String getName() {
		String name = nameField.getText();
		return name.length()>0 ? name : null;
	}


	/**
	 * Packs this dialog, taking special care to not be too wide due to our
	 * <code>SelectableLabel</code>.
	 */
	private void packSpecial() {
		pack();
		setSize(520, getHeight()+60); // Enough for line wrapping
	}


	private void setBadNameValue(String reason) {
		renameDIP.setShowIcon(true);
		renameDIP.setIcon(getErrorIcon());
		renameDIP.setToolTipText(getLocalizedReason(reason));
		okButton.setEnabled(false);
	}


	public void setDescription(Icon icon, String desc) {
		SelectableLabel descText = new SelectableLabel(desc);
		JLabel label = new JLabel(icon);
		if (getComponentOrientation().isLeftToRight()) {
			label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 15));
		}
		else {
			label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 10));
		}
		JPanel temp = new JPanel(new BorderLayout());
		temp.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
		temp.add(label, BorderLayout.LINE_START);
		temp.add(descText);
		topPanel.add(temp, BorderLayout.NORTH);
		packSpecial();
	}


	private void setGoodNameValue() {
		renameDIP.setShowIcon(false);
		renameDIP.setToolTipText(null);
		okButton.setEnabled(true);
	}


	/**
	 * Sets the name displayed in this dialog.
	 *
	 * @param name The name to display.
	 * @see #getName()
	 */
	@Override
	public void setName(String name) {
		nameField.setText(name);
		nameField.requestFocusInWindow();
		nameField.selectAll();
		if (name==null) {
			okButton.setEnabled(false);
		}
	}


	/**
	 * Sets the label text for the name field.
	 *
	 * @param label The new label text.
	 */
	public void setNameLabel(String label) {
		nameLabel.setText(label);
	}


	/**
	 * Listens for events in this dialog.
	 */
	private class Listener implements ActionListener, DocumentListener {

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("OK")) {
				setVisible(false);
			}
			else if (command.equals("Cancel")) {
				escapePressed();
			}
		}

		public void changedUpdate(DocumentEvent e) {
		}

		private void handleDocumentEvent(DocumentEvent e) {

			if (nameField.getDocument().getLength()==0) {
				setBadNameValue("empty");
				return;
			}

			String text = nameField.getText();
			String error = nameChecker.isValid(text);
			if (error!=null) {
				setBadNameValue(error);
				return;
			}

//			if (isNew && MacroManager.get().containsMacroNamed(name)) {
//				setWarnMacroName();
//			}
//			else {
//				setGoodMacroName();
//			}
setGoodNameValue();

		}

		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

	}


}