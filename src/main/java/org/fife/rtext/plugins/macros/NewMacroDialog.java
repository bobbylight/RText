/*
 * 06/29/2011
 *
 * NewMacroDialog.java - Dialog for defining a new macro.
 * Copyright (C) 2011 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.macros;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.rsta.ui.DecorativeIconPanel;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.*;


/**
 * A dialog allowing the user to create a new macro.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class NewMacroDialog extends EscapableDialog {

	private RText rtext;
	private MacroPlugin plugin;
	private Listener l;
	private DecorativeIconPanel nameDIP;
	private JTextField nameField;
	private JTextField descField;
	private KeyStrokeField shortcutField;
	private JComboBox<String> typeCombo;
	private JButton okButton;
	private JButton editButton;
	private JButton cancelButton;
	private Macro macro;
	private boolean isNew;
	private static Icon errorIcon;
	private static Icon warnIcon;

	private static final int DECORATIVE_ICON_WIDTH = 12;

	private static final String[] EXTENSIONS = { ".js", ".groovy", };

	private static final String MSG_BUNDLE = "org.fife.rtext.plugins.macros.NewMacroDialog";
	private static final ResourceBundle MSG = ResourceBundle.getBundle(MSG_BUNDLE);


	NewMacroDialog(MacroPlugin plugin, JDialog parent) {
		super(parent);
		createGUI(plugin);
	}


	NewMacroDialog(MacroPlugin plugin, JFrame parent) {
		super(parent);
		createGUI(plugin);
	}


	private void createGUI(MacroPlugin plugin) {

		this.plugin = plugin;
		rtext = plugin.getApplication();
		ResourceBundle parentMsg = rtext.getResourceBundle();
		JPanel cp = new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(UIUtil.getEmpty5Border());
		l = new Listener();

		Box topPanel = Box.createVerticalBox();
		cp.add(topPanel, BorderLayout.NORTH);
		String descText = MSG.getString("Header.Text");
		SelectableLabel desc = new SelectableLabel(descText);
		topPanel.add(desc);
		topPanel.add(Box.createVerticalStrut(5));

		// Panel for defining the macro
		SpringLayout sl = new SpringLayout();
		JPanel formPanel = new JPanel(sl);
		JLabel nameLabel = UIUtil.newLabel(MSG, "Label.Name");
		nameField = new JTextField(40);
		nameField.getDocument().addDocumentListener(l);
		nameLabel.setLabelFor(nameField);
		nameDIP = new DecorativeIconPanel(DECORATIVE_ICON_WIDTH);
		JPanel namePanel = RTextUtilities.createAssistancePanel(nameField, nameDIP);
		JLabel descLabel = UIUtil.newLabel(MSG, "Label.Desc");
		descField = new JTextField(40);
		descLabel.setLabelFor(descField);
		JPanel descPanel = RTextUtilities.createAssistancePanel(descField, DECORATIVE_ICON_WIDTH);
		JLabel shortcutLabel = UIUtil.newLabel(MSG, "Label.Shortcut");
		shortcutField = new KeyStrokeField();
		shortcutLabel.setLabelFor(shortcutField);
		JPanel shortcutPanel = RTextUtilities.createAssistancePanel(shortcutField, DECORATIVE_ICON_WIDTH);
		JLabel typeLabel = UIUtil.newLabel(MSG, "Label.Type");
		String[] items = { "JavaScript", "Groovy" };
		typeCombo = new JComboBox<>(items);
		typeCombo.addActionListener(l);
		typeCombo.setEditable(false);
		typeLabel.setLabelFor(typeCombo);
		JPanel typePanel = RTextUtilities.createAssistancePanel(typeCombo, DECORATIVE_ICON_WIDTH);
		if (rtext.getComponentOrientation().isLeftToRight()) {
			formPanel.add(nameLabel);     formPanel.add(namePanel);
			formPanel.add(typeLabel);     formPanel.add(typePanel);
			formPanel.add(descLabel);     formPanel.add(descPanel);
			formPanel.add(shortcutLabel); formPanel.add(shortcutPanel);
		}
		else {
			formPanel.add(namePanel);     formPanel.add(nameLabel);
			formPanel.add(typePanel);     formPanel.add(typeLabel);
			formPanel.add(descPanel);     formPanel.add(descLabel);
			formPanel.add(shortcutPanel); formPanel.add(shortcutLabel);
		}
		UIUtil.makeSpringCompactGrid(formPanel, 4, 2,
										5, 5, 5, 5);
		topPanel.add(formPanel);
		topPanel.add(Box.createVerticalStrut(10));
		topPanel.add(Box.createVerticalGlue());

		// Panel for the buttons.
		okButton = UIUtil.newButton(parentMsg, "OKButtonLabel",
											"OKButtonMnemonic");
		okButton.setEnabled(false);
		cancelButton = UIUtil.newButton(parentMsg, "Cancel",
											"CancelMnemonic");
		okButton.addActionListener(l);
		cancelButton.addActionListener(l);
		Container buttonPanel= UIUtil.createButtonFooter(okButton,cancelButton);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		setContentPane(cp);
		setTitle(MSG.getString("Title.New"));
		getRootPane().setDefaultButton(okButton);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModal(true);
		packSpecial();
		setLocationRelativeTo(rtext);

		isNew = true;

	}


	/**
	 * Returns the file to store a macro in.
	 *
	 * @return The file.
	 */
	private File createMacroFile() {
		if (!isNew) { // Preserve old file
			return new File(macro.getFile());
		}
		return new File(plugin.getMacroDir(),
				nameField.getText() + EXTENSIONS[typeCombo.getSelectedIndex()]);
	}


	/**
	 * Called when the user clicks the "Edit Script" button.
	 */
	private void editPressed() {

		String text;
		int messageType = JOptionPane.INFORMATION_MESSAGE;

		// The "Edit" button is only visible when editing an existing macro, so
		// our "macro" variable should be defined.
		File file = new File(macro.getFile());

		if (file.isFile()) { // Should always be true
			rtext.openFile(file);
			text = MSG.getString("Message.MacroOpened");
			text = MessageFormat.format(text, macro.getName());
		}

		else { // Macro script was deleted outside of RText.
			text = MSG.getString("Error.ScriptDoesntExist");
			text = MessageFormat.format(text, file.getAbsolutePath());
			messageType = JOptionPane.ERROR_MESSAGE;
		}

		String title = rtext.getString("InfoDialogHeader");
		JOptionPane.showMessageDialog(this, text, title, messageType);

	}


	/**
	 * Returns the icon to use for fields with errors.
	 *
	 * @return The icon.
	 */
	private Icon getErrorIcon() {
		// The IconGroup caches this value, so we just always fetch it so we can pick up
		// changes in themes/icon groups
		return rtext.getIconGroup().getIcon("error_annotation", 12, 12);
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
	 * Returns the icon to use for fields with warnings.
	 *
	 * @return The icon.
	 */
	private Icon getWarningIcon() {
		// The IconGroup caches this value, so we just always fetch it so we can pick up
		// changes in themes/icon groups
		return rtext.getIconGroup().getIcon("warning_annotation", 12, 12);
	}


	/**
	 * Called when the user clicks the OK button.
	 */
	private void okPressed() {

		File file = createMacroFile();
		int rc = JOptionPane.YES_OPTION;

		if (isNew) {

			// Warn the user if they will be overwriting an existing macro.
			// Note that we check by macro name, so a Rhino macro named "test"
			// would overwrite a Groovy macro named "test".
			String name = nameField.getText();
			if (MacroManager.get().containsMacroNamed(name)) {
				String text = MSG.getString("Prompt.MacroExists");
				text = MessageFormat.format(text, name);
				String title = rtext.getString("ConfDialogTitle");
				rc = JOptionPane.showConfirmDialog(NewMacroDialog.this,
						text, title, JOptionPane.YES_NO_CANCEL_OPTION);
			}

			// Next, the file could already exist, though it is associated with
			// no macro (highly unlikely, but possible, say if they manually
			// mucked with their macros).
			else if (file.isFile()) {
				String text = MSG.getString("Prompt.OverwriteFile");
				text = MessageFormat.format(text, file.getName());
				String title = rtext.getString("ConfDialogTitle");
				rc = JOptionPane.showConfirmDialog(NewMacroDialog.this,
						text, title, JOptionPane.YES_NO_CANCEL_OPTION);
			}

		}

		switch (rc) {
			case JOptionPane.YES_OPTION -> {
				if (isNew) { // Don't delete macro file if we're editing it
					file.delete();
				}
				macro = new Macro();
				macro.setName(nameField.getText());
				macro.setDesc(descField.getText());
				macro.setFile(file.getAbsolutePath());
				KeyStroke ks = shortcutField.getKeyStroke();
				if (ks != null) {
					macro.setAccelerator(ks.toString());
				}
				escapePressed();
			}
			case JOptionPane.NO_OPTION -> {
				// Do nothing; let the user change the macro name
				nameField.selectAll();
				nameField.requestFocusInWindow();
			}
			default ->//case JOptionPane.CANCEL_OPTION:
				// Kill the whole dialog
				escapePressed();
		}

	}


	/**
	 * Packs this dialog, taking special care to not be too wide due to our
	 * <code>SelectableLabel</code>.
	 */
	private void packSpecial() {
		pack();
		setSize(520, getHeight()+80); // Enough for line wrapping
	}


	private void setBadMacroName(String reasonKey) {
		nameDIP.setShowIcon(true);
		String reason = MSG.getString("InvalidMacroName." + reasonKey);
		nameDIP.setIcon(getErrorIcon());
		nameDIP.setToolTipText(reason);
		okButton.setEnabled(false);
	}


	private void setGoodMacroName() {
		nameDIP.setShowIcon(false);
		nameDIP.setToolTipText(null);
		okButton.setEnabled(true);
	}


	/**
	 * Initializes the fields in this dialog to show the values for a
	 * specific macro.
	 *
	 * @param macro The macro.
	 */
	public void setMacro(Macro macro) {

		this.macro = macro;
		isNew = false;
		descField.setText(macro.getDesc());
		nameField.setText(macro.getName());
		nameField.setEditable(false); // Can't change name
		String accelerator = macro.getAccelerator();
		if (accelerator!=null && accelerator.length()>0) {
			shortcutField.setKeyStroke(KeyStroke.getKeyStroke(accelerator));
		}

		int index = 0;
		int dot = macro.getFile().lastIndexOf('.');
		if (dot>-1) {
			String ext = macro.getFile().substring(dot);
			//index = Arrays.binarySearch(EXTENSIONS, ext);
			for (int i=0; i<EXTENSIONS.length; i++) {
				if (EXTENSIONS[i].equals(ext)) {
					index = i;
					break;
				}
			}
		}
		typeCombo.setSelectedIndex(index);
		typeCombo.setEnabled(false); // Can't change language

		editButton = UIUtil.newButton(MSG, "Button.Edit", "Button.Edit.Mnemonic");
		editButton.addActionListener(l);
		Container buttonPanel = okButton.getParent();
		Container bpParent = buttonPanel.getParent();
		buttonPanel = new JPanel(new GridLayout(1,2, 5,0));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		JPanel bp2 = new JPanel(new FlowLayout());
		bp2.add(buttonPanel);
		bp2.add(Box.createHorizontalStrut(20));
		bp2.add(editButton);
		bpParent.add(UIUtil.createButtonFooter(bp2));

		UIUtil.ensureDefaultButtonWidth(okButton);
		UIUtil.ensureDefaultButtonWidth(cancelButton);
		UIUtil.ensureDefaultButtonWidth(editButton);
		okButton.setPreferredSize(editButton.getPreferredSize());
		cancelButton.setPreferredSize(editButton.getPreferredSize());

		setTitle(MSG.getString("Title.Edit"));

		packSpecial();

	}


	/**
	 * Overridden to give focus to the appropriate text component when this
	 * dialog is made visible.
	 *
	 * @param visible Whether this dialog is to be made visible.
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			SwingUtilities.invokeLater(() -> {
				JTextField field = isNew ? nameField : descField;
				field.requestFocusInWindow();
				field.selectAll();
			});
		}
		super.setVisible(visible);
	}


	private void setWarnMacroName() {
		nameDIP.setShowIcon(true);
		String reason = MSG.getString("Warning.MacroNameTaken");
		nameDIP.setIcon(getWarningIcon());
		nameDIP.setToolTipText(reason);
	}


	/**
	 * Listens for events in this dialog.
	 */
	private class Listener implements ActionListener, DocumentListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source==okButton) {
				okPressed();
			}
			else if (source==cancelButton) {
				macro = null; // In case we're editing one via setMacro()
				escapePressed();
			}
			else if (source==editButton) {
				editPressed();
			}

		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			handleDocumentEvent();
		}

		void handleDocumentEvent() {

			if (nameField.getDocument().getLength()==0) {
				setBadMacroName("empty");
				return;
			}

			String name = nameField.getText();
			for (int i=0; i<name.length(); i++) {
				char ch = name.charAt(i);
				if (!(Character.isLetterOrDigit(ch) || ch=='_' || ch=='-' ||
						ch==' ')) {
					setBadMacroName("invalidChars");
					return;
				}
			}

			if (isNew && MacroManager.get().containsMacroNamed(name)) {
				setWarnMacroName();
			}
			else {
				setGoodMacroName();
			}

		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent();
		}

	}


}
