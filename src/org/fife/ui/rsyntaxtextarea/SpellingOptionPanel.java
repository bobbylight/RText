/*
 * 10/10/2009
 *
 * SpellingOptionPanel.java - An option panel for the spelling checker.
 * Copyright (C) 2009 Robert Futrell
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
package org.fife.ui.rsyntaxtextarea;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.NumberDocumentFilter;
import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RColorButton;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.SpecialValueComboBox;
import org.fife.ui.UIUtil;


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
	private JLabel colorLabel;
	private RColorSwatchesButton spellingColorButton;
	private JLabel errorsPerFileLabel;
	private JTextField maxErrorsField;

	private Listener listener;
	private ResourceBundle msg;

	private static final String[][] DICTIONARIES = {
		{ "English (United Kingdom)", AbstractMainView.DICTIONARIES[0] },
		{ "English (United States)", AbstractMainView.DICTIONARIES[1] },
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
		addLeftAlignedComponent(temp, enabledCB, orientation);
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
		if (orientation.isLeftToRight()) {
			temp2.add(dictLabel);			temp2.add(dictComboPanel);
			temp2.add(colorLabel);			temp2.add(colorButtonPanel);
			temp2.add(errorsPerFileLabel);	temp2.add(maxErrorsPanel);
		}
		else {
			temp2.add(dictComboPanel);			temp2.add(dictLabel);
			temp2.add(colorButtonPanel);		temp2.add(colorLabel);
			temp2.add(maxErrorsPanel);			temp2.add(errorsPerFileLabel);
		}
		UIUtil.makeSpringCompactGrid(temp2, 3, 2, 5, 5, 5, 5);
		temp.add(temp2);
		temp.add(Box.createVerticalStrut(5));

		temp.add(Box.createVerticalGlue());

		applyComponentOrientation(orientation);

	}


	private void addLeftAlignedComponent(Container addToMe, JComponent toAdd,
										ComponentOrientation orientation) {
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(toAdd, BorderLayout.LINE_START);
		addToMe.add(temp);
	}


	/**
	 * {@inheritDoc}
	 */
	protected void doApplyImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		mainView.setSpellCheckingEnabled(enabledCB.isSelected());
		mainView.setSpellingDictionary(dictCombo.getSelectedSpecialItem());
		mainView.setSpellCheckingColor(spellingColorButton.getColor());
		mainView.setMaxSpellingErrors(getMaxSpellingErrors());
	}


	/**
	 * {@inheritDoc}
	 */
	public OptionsPanelCheckResult ensureValidInputs() {

		OptionsPanelCheckResult res = null;

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

		return res;

	}


	private int getMaxSpellingErrors() {
		try {
			return Integer.parseInt(maxErrorsField.getText().trim());
		} catch (NumberFormatException nfe) { // Shouldn't happen
			return 30; // Default value
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public JComponent getTopJComponent() {
		return enabledCB;
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
		colorLabel.setEnabled(enabled);
		spellingColorButton.setEnabled(enabled);
		errorsPerFileLabel.setEnabled(enabled);
		maxErrorsField.setEnabled(enabled);
	}


	/**
	 * {@inheritDoc}
	 */
	protected void setValuesImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		boolean enabled = mainView.isSpellCheckingEnabled();
		setSpellCheckingEnabled(enabled);
		dictCombo.setSelectedSpecialItem(mainView.getSpellingDictionary());
		spellingColorButton.setColor(mainView.getSpellCheckingColor());
		maxErrorsField.setText(Integer.toString(
									mainView.getMaxSpellingErrors()));
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