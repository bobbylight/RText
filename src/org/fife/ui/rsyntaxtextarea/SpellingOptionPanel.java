/*
 * 10/10/2009
 *
 * SpellingOptionPanel.java - An option panel for the spelling checker.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import org.fife.rtext.NumberDocumentFilter;
import org.fife.rtext.RText;
import org.fife.rtext.RTextPreferences;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.SpellingSupport;
import org.fife.ui.FSATextField;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RColorButton;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.SelectableLabel;
import org.fife.ui.SpecialValueComboBox;
import org.fife.ui.UIUtil;
import org.fife.ui.rtextfilechooser.RTextFileChooser;


/**
 * Options panel for the spelling checker.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SpellingOptionPanel extends OptionsDialogPanel {

	private JCheckBox enabledCB;
	private JLabel dictLabel;
	private SpecialValueComboBox dictCombo;
	private JLabel userDictLabel;
	private FSATextField userDictField;
	private SelectableLabel userDictDescField;
	private JButton userDictBrowse;
	private RTextFileChooser chooser;
	private JLabel colorLabel;
	private RColorSwatchesButton spellingColorButton;
	private JLabel errorsPerFileLabel;
	private JTextField maxErrorsField;
	private JCheckBox viewSpellingWindowCB;

	private Listener listener;
	private ResourceBundle msg;

	private static final String[][] DICTIONARIES = {
		{ "English (United Kingdom)", SpellingSupport.DICTIONARIES[0] },
		{ "English (United States)", SpellingSupport.DICTIONARIES[1] },
	};

	private static final String MISC_PROPERTY			= "Miscellaneous";


	public SpellingOptionPanel() {

		ComponentOrientation orientation = ComponentOrientation.
		getOrientation(getLocale());

		listener = new Listener();
		msg = ResourceBundle.getBundle(
						"org.fife.ui.rsyntaxtextarea.SpellingOptionPanel");
		setName(msg.getString("Title"));

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(new OptionPanelBorder(msg.getString("Spelling")));
		add(contentPane);

		Box temp = Box.createVerticalBox();
		contentPane.add(temp, BorderLayout.NORTH);

		enabledCB = new JCheckBox(msg.getString("Enabled"));
		enabledCB.setActionCommand("Enabled");
		enabledCB.addActionListener(listener);
		addLeftAligned(temp, enabledCB);
		temp.add(Box.createVerticalStrut(5));

		dictLabel = new JLabel(msg.getString("Dictionary"));
		dictCombo = new SpecialValueComboBox();
		for (int i=0; i<DICTIONARIES.length; i++) {
			dictCombo.addSpecialItem(DICTIONARIES[i][0], DICTIONARIES[i][1]);
		}
		dictCombo.setEditable(false);
		dictCombo.setActionCommand("Dictionary");
		dictCombo.addActionListener(listener);
		JPanel dictComboPanel = new JPanel(new BorderLayout());
		dictComboPanel.add(dictCombo, BorderLayout.LINE_START);
		dictLabel.setLabelFor(dictCombo);

		userDictLabel = new JLabel(msg.getString("UserDictionary"));
		userDictField = new FSATextField(35);
		userDictField.getDocument().addDocumentListener(listener);
		userDictBrowse = new JButton(msg.getString("Browse"));
		userDictBrowse.setActionCommand("BrowseUserDictionary");
		userDictBrowse.addActionListener(listener);
		JPanel userDictFieldPanel = new JPanel(new BorderLayout());
		userDictFieldPanel.add(userDictField);
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		buttonPanel.add(userDictBrowse);
		userDictFieldPanel.add(buttonPanel, BorderLayout.LINE_END);
		userDictDescField = new SelectableLabel(msg.getString("UserDictionaryDesc"));

		colorLabel = new JLabel(msg.getString("Color"));
		spellingColorButton = new RColorSwatchesButton();
		spellingColorButton.addPropertyChangeListener(
								RColorButton.COLOR_CHANGED_PROPERTY, listener);
		JPanel colorButtonPanel = new JPanel(new BorderLayout());
		colorButtonPanel.add(spellingColorButton, BorderLayout.LINE_START);

		errorsPerFileLabel = new JLabel(msg.getString("MaxErrorsPerFile"));
		maxErrorsField = new JTextField(8);
		((AbstractDocument)maxErrorsField.getDocument()).setDocumentFilter(
										new NumberDocumentFilter());
		maxErrorsField.getDocument().addDocumentListener(listener);
		JPanel maxErrorsPanel = new JPanel(new BorderLayout());
		maxErrorsPanel.add(maxErrorsField, BorderLayout.LINE_START);

		JPanel temp2 = new JPanel(new SpringLayout());
		temp2.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // "Indent"
		if (orientation.isLeftToRight()) {
			temp2.add(dictLabel);				temp2.add(dictComboPanel);
			temp2.add(userDictLabel);			temp2.add(userDictFieldPanel);
			temp2.add(Box.createRigidArea(new Dimension(1,1))); temp2.add(userDictDescField);
			temp2.add(colorLabel);				temp2.add(colorButtonPanel);
			temp2.add(errorsPerFileLabel);		temp2.add(maxErrorsPanel);
		}
		else {
			temp2.add(dictComboPanel);			temp2.add(dictLabel);
			temp2.add(userDictFieldPanel);		temp2.add(userDictLabel);
			temp2.add(userDictDescField);		temp2.add(Box.createRigidArea(new Dimension(1,1)));
			temp2.add(colorButtonPanel);		temp2.add(colorLabel);
			temp2.add(maxErrorsPanel);			temp2.add(errorsPerFileLabel);
		}
		UIUtil.makeSpringCompactGrid(temp2, 5, 2, 5, 5, 5, 5);
		addLeftAligned(temp, temp2);
		temp.add(Box.createVerticalStrut(5));

		viewSpellingWindowCB = new JCheckBox(msg.getString("ViewSpellingErrorWindow"));
		viewSpellingWindowCB.setActionCommand("ViewSpellingWindow");
		viewSpellingWindowCB.addActionListener(listener);
		addLeftAligned(temp, viewSpellingWindowCB);
		temp.add(Box.createVerticalStrut(5));

		JButton rdButton = new JButton(msg.getString("RestoreDefaults"));
		rdButton.setActionCommand("RestoreDefaults");
		rdButton.addActionListener(listener);
		addLeftAligned(temp, rdButton);
		temp.add(Box.createVerticalStrut(5));

		temp.add(Box.createVerticalGlue());

		applyComponentOrientation(orientation);

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doApplyImpl(Frame owner) {
		RText rtext = (RText)owner;
		SpellingSupport support = rtext.getMainView().getSpellingSupport();
		support.setSpellCheckingEnabled(enabledCB.isSelected());
		support.setSpellingDictionary(dictCombo.getSelectedSpecialItem());
		support.setUserDictionary(getUserDictionary());
		support.setSpellCheckingColor(spellingColorButton.getColor());
		support.setMaxSpellingErrors(getMaxSpellingErrors());
		rtext.setSpellingWindowVisible(viewSpellingWindowCB.isSelected());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {

		OptionsPanelCheckResult res = null;

		// Check maximum error count
		String maxErrors = maxErrorsField.getText();
		try {
			int max = Integer.parseInt(maxErrors);
			if (max<0) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException nfe) {
			String desc = msg.getString("Error.InvalidMaxErrors.txt");
			res = new OptionsPanelCheckResult(this, maxErrorsField, desc);
		}

		if (res==null) {

			// Check user dictionary file.  It's okay if it doesn't exist yet,
			// just verify that its parent directory exists, and that it itself
			// isn't a directory.
			File userDict = getUserDictionary();
			if (userDict!=null) {
				if (userDict.isDirectory()) {
					String desc = msg.getString("Error.UserDictionaryIsDirectory.txt");
					res = new OptionsPanelCheckResult(this, userDictField, desc);
				}
				else {
					File parent = userDict.getParentFile();
					if (parent==null || !parent.exists()) {
						String desc = msg.getString("Error.CannotCreateUserDictionary.txt");
						res = new OptionsPanelCheckResult(this, userDictField, desc);
					}
				}
			}

		}

		return res;

	}


	private int getMaxSpellingErrors() {
		try {
			return Integer.parseInt(maxErrorsField.getText().trim());
		} catch (NumberFormatException nfe) { // Shouldn't happen
			return RTextPreferences.DEFAULT_MAX_SPELLING_ERRORS; // Default value
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public JComponent getTopJComponent() {
		return enabledCB;
	}


	private File getUserDictionary() {
		String temp = userDictField.getText();
		if (temp.trim().length()==0) {
			return null;
		}
		return new File(temp).getAbsoluteFile();
	}


	/**
	 * Toggles whether relevant widgets are enabled based on whether spell
	 * checking is currently enabled.
	 *
	 * @param enabled Whether spell checking is currently enabled.
	 */
	private void setSpellCheckingEnabled(boolean enabled) {
		enabledCB.setSelected(enabled);
		dictLabel.setEnabled(enabled);
		dictCombo.setEnabled(enabled);
		userDictLabel.setEnabled(enabled);
		userDictField.setEnabled(enabled);
		userDictBrowse.setEnabled(enabled);
		userDictDescField.setEnabled(enabled);
		colorLabel.setEnabled(enabled);
		spellingColorButton.setEnabled(enabled);
		errorsPerFileLabel.setEnabled(enabled);
		maxErrorsField.setEnabled(enabled);
		//viewSpellingWindowCB.setEnabled(enabled);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setValuesImpl(Frame owner) {
		RText rtext = (RText)owner;
		SpellingSupport support = rtext.getMainView().getSpellingSupport();
		boolean enabled = support.isSpellCheckingEnabled();
		setSpellCheckingEnabled(enabled);
		dictCombo.setSelectedSpecialItem(support.getSpellingDictionary());
		userDictField.setFileSystemAware(false);
		File temp = support.getUserDictionary();
		userDictField.setText(temp==null ? "" : temp.getAbsolutePath());
		userDictField.setFileSystemAware(true);
		spellingColorButton.setColor(support.getSpellCheckingColor());
		maxErrorsField.setText(Integer.toString(
				support.getMaxSpellingErrors()));
		viewSpellingWindowCB.setSelected(rtext.isSpellingWindowVisible());
	}


	/**
	 * Listens for events in this panel.
	 */
	private class Listener implements DocumentListener, ActionListener,
								PropertyChangeListener {

		public void actionPerformed(ActionEvent e) {

			String command = e.getActionCommand();

			if ("Enabled".equals(command)) {
				boolean enabled = enabledCB.isSelected();
				setSpellCheckingEnabled(enabled);
				hasUnsavedChanges = true;
				firePropertyChange(MISC_PROPERTY, null, null);
			}

			else if ("Dictionary".equals(command)) {
				hasUnsavedChanges = true;
				firePropertyChange(MISC_PROPERTY, null, null);
			}

			else if ("BrowseUserDictionary".equals(command)) {
				if (chooser==null) {
					chooser = new RTextFileChooser();
				}
				int rc = chooser.showOpenDialog(null);
				if (rc==RTextFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					userDictField.setFileSystemAware(false);
					userDictField.setText(file.getAbsolutePath());
					userDictField.setFileSystemAware(true);
					hasUnsavedChanges = true;
					firePropertyChange(MISC_PROPERTY, null, null);
				}
			}

			else if ("ViewSpellingWindow".equals(command)) {
				hasUnsavedChanges = true;
				firePropertyChange(MISC_PROPERTY, null, null);
			}

			else if ("RestoreDefaults".equals(command)) {

				File userDictFile = new File(
						RTextUtilities.getPreferencesDirectory(),
						"userDictionary.txt");
				String userDictFileName = userDictFile.getAbsolutePath();
				Color defaultColor = RTextPreferences.DEFAULT_SPELLING_ERROR_COLOR;
				String defaultMaxErrors = Integer.toString(RTextPreferences.
												DEFAULT_MAX_SPELLING_ERRORS);

				if (enabledCB.isSelected() ||
						dictCombo.getSelectedIndex()!=1 ||
						!userDictField.getText().equals(userDictFileName) ||
						!spellingColorButton.getColor().equals(defaultColor) ||
						!defaultMaxErrors.equals(maxErrorsField.getText()) ||
						viewSpellingWindowCB.isSelected()) {

					setSpellCheckingEnabled(false);
					dictCombo.setSelectedIndex(1);
					userDictField.setFileSystemAware(false);
					userDictField.setText(userDictFileName);
					userDictField.setFileSystemAware(true);
					spellingColorButton.setColor(defaultColor);
					maxErrorsField.setText(defaultMaxErrors);
					viewSpellingWindowCB.setSelected(false);

					hasUnsavedChanges = true;
					firePropertyChange(MISC_PROPERTY, null, null);

				}
			}

		}

		public void changedUpdate(DocumentEvent e) {
		}

		public void insertUpdate(DocumentEvent e) {
			hasUnsavedChanges = true;
			firePropertyChange(MISC_PROPERTY, null, null);
		}

		public void propertyChange(PropertyChangeEvent e) {

			String prop = e.getPropertyName();

			if (RColorSwatchesButton.COLOR_CHANGED_PROPERTY.equals(prop)) {
				hasUnsavedChanges = true;
				firePropertyChange(MISC_PROPERTY, null, null);
			}

		}

		public void removeUpdate(DocumentEvent e) {
			hasUnsavedChanges = true;
			firePropertyChange(MISC_PROPERTY, null, null);
		}

	}


}