/*
 * 06/29/2011
 *
 * NewMacroDialog.java - Dialog for defining a new macro.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.macros;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.rsta.ui.DecorativeIconPanel;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.EscapableDialog;
import org.fife.ui.KeyStrokeField;
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
	private MacroPlugin plugin;
	private Listener l;
	private DecorativeIconPanel nameDIP;
	private JTextField nameField;
	private JTextField descField;
	private KeyStrokeField shortcutField;
	private JComboBox typeCombo;
	private SelectableLabel engineNotesLabel;
	private JButton okButton;
	private JButton editButton;
	private JButton cancelButton;
	private Macro macro;
	private boolean isNew;
	private static Icon ERROR_ICON;
	private static Icon WARN_ICON;

	private static final String[] NOTE_TYPES = { "Rhino", "Groovy", };
	private static final String[] EXTENSIONS = { ".js", ".groovy", };

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

		this.plugin = plugin;
		rtext = plugin.getRText();
		ResourceBundle parentMsg = rtext.getResourceBundle();
		JPanel cp = new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(UIUtil.getEmpty5Border());
		l = new Listener();

		Box topPanel = Box.createVerticalBox();
		cp.add(topPanel, BorderLayout.NORTH);
		String descText = msg.getString("Header.Text");
		SelectableLabel desc = new SelectableLabel(descText);
		topPanel.add(desc);
		topPanel.add(Box.createVerticalStrut(5));

		// Panel for defining the macro
		SpringLayout sl = new SpringLayout();
		JPanel formPanel = new JPanel(sl);
		JLabel nameLabel = UIUtil.newLabel(msg, "Label.Name");
		nameField = new JTextField(40);
		nameField.getDocument().addDocumentListener(l);
		nameLabel.setLabelFor(nameField);
		nameDIP = new DecorativeIconPanel();
		JPanel namePanel = RTextUtilities.createAssistancePanel(nameField, nameDIP);
		JLabel descLabel = UIUtil.newLabel(msg, "Label.Desc");
		descField = new JTextField(40);
		descLabel.setLabelFor(descField);
		JPanel descPanel = RTextUtilities.createAssistancePanel(descField, null);
		JLabel shortcutLabel = UIUtil.newLabel(msg, "Label.Shortcut");
		shortcutField = new KeyStrokeField();
		shortcutLabel.setLabelFor(shortcutField);
		JPanel shortcutPanel = RTextUtilities.createAssistancePanel(shortcutField, null);
		JLabel typeLabel = UIUtil.newLabel(msg, "Label.Type");
		String[] items = { "Rhino (JavaScript)", "Groovy" };
		typeCombo = new JComboBox(items);
		typeCombo.addActionListener(l);
		typeCombo.setEditable(false);
		typeLabel.setLabelFor(typeCombo);
		JPanel typePanel = RTextUtilities.createAssistancePanel(typeCombo, null);
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
		engineNotesLabel = new SelectableLabel(getEngineNote(0));
		topPanel.add(engineNotesLabel);
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
		setTitle(msg.getString("Title.New"));
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

		String text = null;
		int messageType = JOptionPane.INFORMATION_MESSAGE;

		// The "Edit" button is only visible when editing an existing macro, so
		// our "macro" variable should be defined.
		File file = new File(macro.getFile());

		if (file.isFile()) { // Should always be true
			rtext.openFile(macro.getFile());
			text = msg.getString("Message.MacroOpened");
			text = MessageFormat.format(text, macro.getName());
		}

		else { // Macro script was deleted outside of RText.
			text = msg.getString("Error.ScriptDoesntExist");
			text = MessageFormat.format(text, file.getAbsolutePath());
			messageType = JOptionPane.ERROR_MESSAGE;
		}

		String title = rtext.getString("InfoDialogHeader");
		JOptionPane.showMessageDialog(this, text, title, messageType);

	}


	/**
	 * Returns any notes about the specified scripting engine.
	 *
	 * @param index The index of the script engine in the combo box.
	 * @return The notes.
	 */
	private String getEngineNote(int index) {
		String note = "<html><table><tr><td valign='baseline'>" +
				msg.getString("EngineNotes.Notes") +
				"</td><td>" +//&nbsp;&nbsp;</td><td>" +
				msg.getString("EngineNotes." + NOTE_TYPES[index]) +
				"</td></table>";
		note = MessageFormat.format(note,
			new Object[] { rtext.getInstallLocation() + File.separator + "plugins" });
		return note;
	}


	/**
	 * Returns the icon to use for fields with errors.
	 *
	 * @return The icon.
	 */
	private Icon getErrorIcon() {
		if (ERROR_ICON==null) {
			URL res = getClass().getResource("error_co.gif");
			ERROR_ICON = new ImageIcon(res);
		}
		return ERROR_ICON;
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
		if (WARN_ICON==null) {
			URL res = getClass().getResource("warning_co.gif");
			WARN_ICON = new ImageIcon(res);
		}
		return WARN_ICON;
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
				String text = msg.getString("Prompt.MacroExists");
				text = MessageFormat.format(text, name);
				String title = rtext.getString("ConfDialogTitle");
				rc = JOptionPane.showConfirmDialog(NewMacroDialog.this,
						text, title, JOptionPane.YES_NO_CANCEL_OPTION);
			}

			// Next, the file could already exist, though it is associated with
			// no macro (highly unlikely, but possible, say if they manually
			// mucked with their macros).
			else if (file.isFile()) {
				String text = msg.getString("Prompt.OverwriteFile");
				text = MessageFormat.format(text, file.getName());
				String title = rtext.getString("ConfDialogTitle");
				rc = JOptionPane.showConfirmDialog(NewMacroDialog.this,
						text, title, JOptionPane.YES_NO_CANCEL_OPTION);
			}

		}

		switch (rc) {
			case JOptionPane.YES_OPTION:
				if (isNew) { // Don't delete macro file if we're editing it
					file.delete();
				}
				macro = new Macro();
				macro.setName(nameField.getText());
				macro.setDesc(descField.getText());
				macro.setFile(file.getAbsolutePath());
				KeyStroke ks = shortcutField.getKeyStroke();
				if (ks!=null) {
					macro.setAccelerator(ks.toString());
				}
				escapePressed();
				break;
			case JOptionPane.NO_OPTION:
				// Do nothing; let the user change the macro name
				nameField.selectAll();
				nameField.requestFocusInWindow();
				break;
			default://case JOptionPane.CANCEL_OPTION:
				// Kill the whole dialog
				escapePressed();
				break;
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
		String reason = msg.getString("InvalidMacroName." + reasonKey);
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

		editButton = UIUtil.newButton(msg, "Button.Edit", "Button.Edit.Mnemonic");
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
		//bpParent.add(bp2);

		UIUtil.ensureDefaultButtonWidth(okButton);
		UIUtil.ensureDefaultButtonWidth(cancelButton);
		UIUtil.ensureDefaultButtonWidth(editButton);
		okButton.setPreferredSize(editButton.getPreferredSize());
		cancelButton.setPreferredSize(editButton.getPreferredSize());

//		buttonPanel.setLayout(new GridLayout(1,3, 5,0));
//		buttonPanel.add(editButton, 1);

		setTitle(msg.getString("Title.Edit"));

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
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JTextField field = isNew ? nameField : descField;
					field.requestFocusInWindow();
					field.selectAll();
				}
			});
		}
		super.setVisible(visible);
	}


	private void setWarnMacroName() {
		nameDIP.setShowIcon(true);
		String reason = msg.getString("Warning.MacroNameTaken");
		nameDIP.setIcon(getWarningIcon());
		nameDIP.setToolTipText(reason);
	}


	/**
	 * Listens for events in this dialog.
	 */
	private class Listener implements ActionListener, DocumentListener {

		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source==typeCombo) {
				engineNotesLabel.setText(getEngineNote(typeCombo.getSelectedIndex()));
			}
			else if (source==okButton) {
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

		public void changedUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		public void handleDocumentEvent(DocumentEvent e) {

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

		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

	}


}