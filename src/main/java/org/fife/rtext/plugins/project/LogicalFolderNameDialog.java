/*
 * 09/08/2012
 *
 * LogicalFolderNameDialog.java - A dialog that prompts the user for the
 * name of a logical folder.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.rsta.ui.DecorativeIconPanel;
import org.fife.rsta.ui.EscapableDialog;
import org.fife.rsta.ui.ResizableFrameContentPane;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.plugins.project.tree.LogicalFolderProjectEntryTreeNode;
import org.fife.rtext.plugins.project.tree.NameChecker;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;


/**
 * A dialog that prompts the user for the name of a logical folder.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class LogicalFolderNameDialog extends EscapableDialog {

	private Listener l;
	private JTextField nameField;
	private DecorativeIconPanel nameDIP;
	private NameChecker nameChecker;
	private String logicalFolderName;
	private JButton okButton;
	private JButton cancelButton;


	public LogicalFolderNameDialog(Frame parent, String prevName,
			NameChecker nameChecker) {

		super(parent);
		JPanel cp = new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(UIUtil.getEmpty5Border());
		ComponentOrientation o = parent.getComponentOrientation();
		l = new Listener();
		boolean isNew = prevName==null;
		this.nameChecker = nameChecker;

		Box topPanel = Box.createVerticalBox();
		cp.add(topPanel, BorderLayout.NORTH);
		topPanel.add(createDescPanel(o));
		topPanel.add(Box.createVerticalStrut(15));
		topPanel.add(createFormPanel(o, prevName));
		topPanel.add(Box.createVerticalGlue());

		cp.add(createButtonPanel(o), BorderLayout.SOUTH);

		setContentPane(cp);
		getRootPane().setDefaultButton(okButton);
		setModal(true);
		String key = "LogicalFolderNameDialog.Title" + (isNew ? ".Add" : ".Edit");
		setTitle(Messages.getString(key));
		applyComponentOrientation(o);
		packSpecial();
		setLocationRelativeTo(parent);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				nameField.requestFocusInWindow();
				nameField.selectAll();
			}
		});

	}


	/**
	 * Creates a button.
	 *
	 * @param key The key for the button text and mnemonic.
	 * @return The button.
	 */
	private static final JButton createButton(String key) {
		key = "Button." + key;
		JButton button = new JButton(Messages.getString(key));
		button.setMnemonic(Messages.getMnemonic(key + ".Mnemonic"));
		return button;
	}


	private Container createButtonPanel(ComponentOrientation o) {
		okButton = createButton("OK");
		okButton.setEnabled(false);
		okButton.addActionListener(l);
		cancelButton = createButton("Cancel");
		cancelButton.addActionListener(l);
		return UIUtil.createButtonFooter(okButton, cancelButton);
	}


	private static final Container createDescPanel(ComponentOrientation o) {
		String descText = Messages.getString("LogicalFolderNameDialog.Header");
		SelectableLabel desc = new SelectableLabel(descText);
		Icon icon = LogicalFolderProjectEntryTreeNode.getLogicalFolderIcon();
		JLabel label = new JLabel(icon);
		if (o.isLeftToRight()) {
			label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 15));
		}
		else {
			label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 10));
		}
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(label, BorderLayout.LINE_START);
		temp.add(desc);
		return temp;
	}


	private Container createFormPanel(ComponentOrientation o, String prevName) {

		JLabel nameLabel = new JLabel(Messages.
				getString("RenameDialog.Field.Label"));
		nameLabel.setDisplayedMnemonic(Messages.
				getMnemonic("RenameDialog.Field.Label.Mnemonic"));
		nameField = new JTextField(prevName, 40);
		nameField.getDocument().addDocumentListener(l);
		nameLabel.setLabelFor(nameField);

		nameDIP = new DecorativeIconPanel();
		Box box = new Box(BoxLayout.LINE_AXIS);
		box.add(nameLabel);
		box.add(Box.createHorizontalStrut(5));
		box.add(RTextUtilities.createAssistancePanel(nameField, nameDIP));
		box.add(Box.createHorizontalGlue());

		return box;

	}


	/**
	 * Returns a localized error message to use in this dialog.
	 *
	 * @param key The (part of the) key to display.
	 * @return The localized error message.
	 */
	public static String getLocalizedReason(String key) {
		return Messages.getString("LogicalFolderNameDialog.InvalidName." + key);
	}


	/**
	 * Returns the name selected.
	 *
	 * @return The name selected, or <code>null</code> if the dialog was
	 *         cancelled.
	 */
	public String getLogicalFolderName() {
		return logicalFolderName;
	}


	/**
	 * Called when the user clicks the OK button.
	 */
	private void okPressed() {
		// TODO
		escapePressed();
	}


	/**
	 * Packs this dialog, taking special care to not be too wide due to our
	 * <code>SelectableLabel</code>.
	 */
	private void packSpecial() {
		pack();
		setSize(520, getHeight()+80); // Enough for line wrapping
	}


	private void setBadNameValue(String reason) {
		nameDIP.setShowIcon(true);
		nameDIP.setIcon(RenameDialog.getErrorIcon());
		nameDIP.setToolTipText(getLocalizedReason(reason));
		okButton.setEnabled(false);
	}


	private void setGoodNameValue() {
		nameDIP.setShowIcon(false);
		nameDIP.setToolTipText(null);
		okButton.setEnabled(true);
	}


	/**
	 * Listens for events in this dialog.
	 */
	private class Listener implements ActionListener, DocumentListener {

		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source==okButton) {
				logicalFolderName = nameField.getText();
				okPressed();
			}
			else if (source==cancelButton) {
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