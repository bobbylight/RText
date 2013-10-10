/*
 * 11/10/2009
 *
 * OpenRemoteDialog.java - Dialog for opening or saving a remote file via FTP.
 * Copyright (C) 2008 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.fife.rsta.ui.RComboBoxModel;
import org.fife.ui.EscapableDialog;
import org.fife.ui.UIUtil;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.fife.ui.rtextfilechooser.RTextFileChooser;


/**
 * A dialog that allows the user to open or save a remote file via FTP.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RemoteFileChooser extends EscapableDialog
			implements ActionListener, DocumentListener, FocusListener {

	/**
	 * Mode designating a file should be opened.
	 */
	public static final int OPEN_MODE				= 0;

	/**
	 * Mode designating a file should be saved.
	 */
	public static final int SAVE_MODE				= 1;

	private RText owner;
	private JPanel formPanel;
	private JLabel protoLabel;
	private JComboBox protocolCombo;
	private JLabel hostLabel;
	private JComboBox hostCombo;
	private JLabel fileNameLabel;
	private JComboBox fileCombo;
	private JLabel userLabel;
	private JComboBox userCombo;
	private JLabel passLabel;
	private JPasswordField passField;
	private JLabel encodingLabel;
	private JComboBox encodingCombo;
	private JLabel portLabel;
	private JComboBox portCombo;
	private JButton okButton;
	private JToggleButton advancedButton;

	private String savedUser;
	private char[] savedPass;

	private String openTitle;
	private String saveTitle;
	private int mode;

	private static final String MSG = "org.fife.rtext.RemoteFileChooser";

	private static final String DEFAULT_FTP_PORT		= "21";
	private static final String DEFAULT_HTTP_PORT	= "80";


	/**
	 * Creates a new <code>RemoteFileChooser</code>.
	 *
	 * @param owner The rtext window that owns this dialog.
	 */
	public RemoteFileChooser(RText owner) {

		super(owner);
		this.owner = owner;

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
		ResourceBundle msg = ResourceBundle.getBundle(MSG);
		openTitle = msg.getString("OpenTitle");
		saveTitle = msg.getString("SaveTitle");

		JPanel cp = new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		setContentPane(cp);

		// A panel containing the file information.
		formPanel = new JPanel(new SpringLayout());
		protocolCombo = new JComboBox(new String[] { "FTP", "HTTP" });
		UIUtil.fixComboOrientation(protocolCombo);
		protocolCombo.addActionListener(this);
		protoLabel = createLabel(protocolCombo, msg, "Protocol");
		hostCombo = createCombo();
		hostLabel = createLabel(hostCombo, msg, "HostName");
		fileCombo = createCombo();
		fileNameLabel = createLabel(fileCombo, msg, "FileName");
		userCombo = createCombo();
		userLabel = createLabel(userCombo, msg, "UserID");
		passField = new JPasswordField(25);
		passField.addFocusListener(this);
		passLabel = createLabel(passField, msg, "Password");
		encodingCombo = new JComboBox();
		UIUtil.fixComboOrientation(encodingCombo);
		Map<String, Charset> availcs = Charset.availableCharsets();
		for (String key : availcs.keySet()) {
			encodingCombo.addItem(key);
		}
		// Get the canonical name of the encoding, as that's what
		// encodingCombo is populated with.
		String defaultEncoding = Charset.forName(
						RTextFileChooser.getDefaultEncoding()).name();
		encodingCombo.setSelectedItem(defaultEncoding);
		encodingLabel = createLabel(encodingCombo, msg, "Encoding");
		portCombo = createCombo();
		portLabel = createLabel(portCombo, msg, "Port");
		cp.add(formPanel, BorderLayout.NORTH);

		// Make a panel containing the OK and Cancel buttons.
		JPanel buttonPanel = new JPanel(new GridLayout(1,3, 5,5));
		okButton = UIUtil.newButton(msg, "OK", "OKMnemonic");
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		JButton cancelButton = UIUtil.newButton(msg, "Cancel",
											"CancelMnemonic");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		advancedButton = new JToggleButton(msg.getString("Advanced"));
		advancedButton.setMnemonic((int)msg.getString("AdvancedMnemonic").
								charAt(0));
		advancedButton.setActionCommand("Advanced");
		advancedButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(advancedButton);

		// Put everything into a neat little package.
		populateFormPanel();
		portCombo.setSelectedItem(DEFAULT_FTP_PORT);
		Container temp = UIUtil.createButtonFooter(buttonPanel);
		cp.add(temp, BorderLayout.SOUTH);
		JRootPane rootPane = getRootPane();
		rootPane.setDefaultButton(okButton);
		setModal(true);
		applyComponentOrientation(orientation);
		mode = -1; // Force setMode() to work the first time through
		setMode(OPEN_MODE);
		pack();
		setLocationRelativeTo(owner);


	}


	/**
	 * Called when an action occurs in this dialog.
	 *
	 * @param e The action event.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (protocolCombo==e.getSource()) {
			boolean enabled = isFTPSelected();
			userLabel.setEnabled(enabled);
			userCombo.setEnabled(enabled);
			passLabel.setEnabled(enabled);
			passField.setEnabled(enabled);
			if (!enabled) {
				savedUser = (String)userCombo.getSelectedItem();
				userCombo.setSelectedItem(null);
				savedPass = passField.getPassword();
				passField.setText(null);
				portCombo.setSelectedItem(DEFAULT_HTTP_PORT);
			}
			else {
				userCombo.setSelectedItem(savedUser);
				passField.setText(new String(savedPass));
				Arrays.fill(savedPass, (char)0);
				portCombo.setSelectedItem(DEFAULT_FTP_PORT);
			}
			updateOKButton();
		}
		else if ("OK".equals(command)) {
			okPressed();
		}
		else if ("Cancel".equals(command)) {
			cancelPressed();
		}
		else if ("Advanced".equals(command)) {
			populateFormPanel();
		}

	}


	/**
	 * Handles the Cancel button being pressed.
	 *
	 * @see #okPressed()
	 */
	protected void cancelPressed() {
		setVisible(false);
	}


	/**
	 * Called when a text field being listened to is modified.
	 *
	 * @param e The document event.
	 */
	public void changedUpdate(DocumentEvent e) {
		handleDocumentUpdate(e);
	}


	/**
	 * Creates a combo box.
	 *
	 * @return The combo box.
	 */
	private JComboBox createCombo() {
		JComboBox combo = new JComboBox(new RComboBoxModel());
		UIUtil.fixComboOrientation(combo);
		combo.setEditable(true);
		getDocument(combo).addDocumentListener(this);
		getTextComponent(combo).addFocusListener(this);
		return combo;
	}


	/**
	 * Creates a label.
	 *
	 * @param labelFor The component this label is labelling.
	 * @param msg Used for localizing.
	 * @param keyRoot The root of the text and mnemonic keys in
	 *        <code>msg</code>.
	 * @return The label.
	 */
	private static final JLabel createLabel(JComponent labelFor,
			ResourceBundle msg, String keyRoot) {
		JLabel label = new JLabel(msg.getString(keyRoot));
		label.setDisplayedMnemonic(msg.getString(keyRoot + "Mnemonic").
								charAt(0));
		label.setLabelFor(labelFor);
		return label;
	}


	/**
	 * Creates a URL for the given parameters.
	 *
	 * @param port The port to use, or <code>null</code> if the default for
	 *        the protocol should be used.
	 * @return The URL
	 * @throws MalformedURLException If something goes horribly wrong.
	 */
	private static final URL createURL(String protocol, String user,
			char[] pass, String host, String port, String file)
					throws MalformedURLException {
		String urlStr = protocol + "://";
		if ("ftp".equalsIgnoreCase(protocol)) {
			if (file.charAt(0)=='/') { // Absolute path
				file = "%2F" + file;
			}
			urlStr += user + ":" + new String(pass) + "@";
		}
		urlStr += host;
		if (port!=null) {
			urlStr += ":" + port;
		}
		urlStr += "/" + file;
		return new URL(urlStr);
	}


	/**
	 * Called when the Escape key is pressed in this dialog.  Subclasses
	 * can override to handle any custom "Cancel" logic.  The default
	 * implementation hides the dialog (via <code>setVisible(false);</code>).
	 */
	@Override
	protected void escapePressed() {
		cancelPressed();
	}


	/**
	 * Called when a combo box in this dialog gains focus.
	 *
	 * @param e The focus event.
	 * @see #focusLost(FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
		Object source = e.getSource();
		if (source instanceof JTextComponent) {
			((JTextComponent)source).selectAll();
		}
	}


	/**
	 * Called when a combo box in this dialog loses focus.
	 *
	 * @param e The focus event.
	 * @see #focusGained(FocusEvent)
	 */
	public void focusLost(FocusEvent e) {
	}


	/**
	 * Returns the document of the text field of the host combo box.
	 *
	 * @param combo The combo box.
	 * @return The <code>Document</code>.
	 * @see #getTextComponent(JComboBox)
	 */
	private static final Document getDocument(JComboBox combo) {
		return getTextComponent(combo).getDocument();
	}


	/**
	 * Returns the port to use when opening the remote file.
	 *
	 * @return The port to use, or <code>-1</code> if the default port
	 *         should be used.
	 */
	private String getSelectedPort() {
		String port = null;
		if (portCombo.isVisible()) {
			String text = ((String)portCombo.getSelectedItem()).trim();
			if (text.length()>0) { // Don't allow blank ports.
				port = text;
			}
		}
		return port;
	}


	/**
	 * Returns the text component for a combo box.
	 *
	 * @param combo The combo box.
	 * @return The text component.
	 * @see #getDocument(JComboBox)
	 */
	private static final JTextComponent getTextComponent(JComboBox combo) {
		return (JTextComponent)combo.getEditor().getEditorComponent();
	}


	/**
	 * Called when a text field being listened to is modified.
	 *
	 * @param e The document event.
	 */
	private void handleDocumentUpdate(DocumentEvent e) {
		updateOKButton();
	}


	/**
	 * Returns whether the given text field has text in it.
	 *
	 * @param field The text field.
	 * @return Whether there is text in the text field.
	 */
	private static final boolean hasContent(JTextComponent field) {
		return field.getDocument().getLength()>0;
	}


	/**
	 * Called when a text field being listened to is modified.
	 *
	 * @param e The document event.
	 */
	public void insertUpdate(DocumentEvent e) {
		handleDocumentUpdate(e);
	}


	/**
	 * Returns whether "FTP" is selected.
	 *
	 * @return Whether "FTP" is selected.
	 */
	private boolean isFTPSelected() {
		return protocolCombo.getSelectedIndex()==0;
	}


	/** 
	 * Called when the OK button is pressed.
	 *
	 * @see #cancelPressed()
	 */
	public void okPressed() {

		String protocol = (String)protocolCombo.getSelectedItem();
		String host = getTextComponent(hostCombo).getText();
		hostCombo.addItem(host); // Puts it at top of drop-down list.
		String file = getTextComponent(fileCombo).getText();
		fileCombo.addItem(file); // Puts it at the top of drop-down list.
		String user = getTextComponent(userCombo).getText();
		userCombo.addItem(user); // Puts it at top of drop-down list.
		char[] pass = passField.getPassword();
		String port = getSelectedPort();
		if (port!=null) {
			portCombo.addItem(port); // Puts it at top of drop-down list.
		}

		URL url = null;
		try {
			url = createURL(protocol, user, pass, host, port, file);
		} catch (MalformedURLException mue) {
			owner.displayException(mue);
			return;
		}

		AbstractMainView mainView = owner.getMainView();
		String encoding = encodingCombo.isVisible() ?
						(String)encodingCombo.getSelectedItem() : null;

		if (mode==OPEN_MODE) {
			if (mainView.openFile(FileLocation.create(url), encoding, false)) {
				setVisible(false); // Otherwise, keep dialog up
			}
		}
		else { // SAVE_MODE
			if (mainView.saveCurrentFileAs(FileLocation.create(url))) {
				setVisible(false); // Otherwise, keep dialog up
			}
		}

	}


	/**
	 * Resets the "form panel." This should be called after the user toggles
	 * whether "advanced" options are shown.
	 */
	private void populateFormPanel() {
		formPanel.removeAll();
		boolean advanced = advancedButton.isSelected();
		ComponentOrientation o = ComponentOrientation.
									getOrientation(getLocale());
		if (o.isLeftToRight()) {
			formPanel.add(protoLabel);    formPanel.add(protocolCombo);
			formPanel.add(hostLabel);     formPanel.add(hostCombo);
			formPanel.add(fileNameLabel); formPanel.add(fileCombo);
			formPanel.add(userLabel);     formPanel.add(userCombo);
			formPanel.add(passLabel);     formPanel.add(passField);
			if (advanced) {
				formPanel.add(encodingLabel); formPanel.add(encodingCombo);
				formPanel.add(portLabel);     formPanel.add(portCombo);
			}
		}
		else {
			formPanel.add(protocolCombo); formPanel.add(protoLabel);
			formPanel.add(hostCombo);     formPanel.add(hostLabel);
			formPanel.add(fileCombo);     formPanel.add(fileNameLabel);
			formPanel.add(userCombo);     formPanel.add(userLabel);
			formPanel.add(passField);     formPanel.add(passLabel);
			if (advanced) {
				formPanel.add(encodingCombo); formPanel.add(encodingLabel);
				formPanel.add(portCombo);     formPanel.add(portLabel);
			}
		}
		UIUtil.makeSpringCompactGrid(formPanel, advanced?7:5,2, 0,0, 5,5);
		formPanel.revalidate();
		applyComponentOrientation(o);
		pack();
	}


	/**
	 * Called when a text field being listened to is modified.
	 *
	 * @param e The document event.
	 */
	public void removeUpdate(DocumentEvent e) {
		handleDocumentUpdate(e);
	}


	/**
	 * Sets the mode to open this file chooser in.
	 *
	 * @param mode The new mode.
	 */
	public void setMode(int mode) {
		if (this.mode==mode) {
			return;
		}
		this.mode = mode;
		if (mode==SAVE_MODE) {
			setTitle(saveTitle);
		}
		else if (mode==OPEN_MODE) {
			setTitle(openTitle);
		}
		else {
			throw new IllegalArgumentException("Invalid mode value");
		}
	}


	/**
	 * Sets the visibility of this dialog.
	 *
	 * @param visible Whether the dialog is to be visible.
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			userCombo.setSelectedItem(null);
			passField.setText(null);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					hostCombo.requestFocusInWindow();
				}
			});
		}
		super.setVisible(visible);
	}


	/**
	 * Updates whether the OK button is enabled.
	 */
	private void updateOKButton() {
		boolean filledIn = hasContent(getTextComponent(hostCombo)) &&
						hasContent(getTextComponent(fileCombo));
		if (isFTPSelected()) {
			filledIn &= hasContent(getTextComponent(userCombo));
		}
		okButton.setEnabled(filledIn);
	}


	/**
	 * Note: This does not override a superclass method; there is no
	 * <code>JDialog#updateUI()</code>.  Applications should call this method
	 * when changing the UI to ensure that non-visible components get the UI
	 * change as well.
	 */
	public void updateUI() {
		boolean advanced = advancedButton.isSelected();
		if (!advanced) { // If advanced stuff isn't visible, update it too
			encodingLabel.updateUI();
			encodingCombo.updateUI();
			portLabel.updateUI();
			portCombo.updateUI();
		}
	}


}