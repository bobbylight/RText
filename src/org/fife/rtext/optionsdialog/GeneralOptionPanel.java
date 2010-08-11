/*
 * 12/07/2004
 *
 * GeneralOptionPanel.java - Option panel for general RText options.
 * Copyright (C) 2004 Robert Futrell
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
package org.fife.rtext.optionsdialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.OptionsDialog;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RButton;
import org.fife.ui.SelectableLabel;
import org.fife.ui.SpecialValueComboBox;
import org.fife.ui.UIUtil;
import org.fife.ui.rtextfilechooser.RDirectoryChooser;
import org.fife.ui.rtextfilechooser.RTextFileChooser;


/**
 * Option panel for general RText options.
 *
 * @author Robert Futrell
 * @version 0.1
 */
class GeneralOptionPanel extends OptionsDialogPanel
						implements ActionListener, DocumentListener {

	private JTextField dirField;
	private RButton dirBrowseButton;
	private SpecialValueComboBox terminatorCombo;
	private JComboBox encCombo;
	private JCheckBox utf8BomCB;
	private JCheckBox sizeCheckCB;
	private JFormattedTextField sizeField;
	private JCheckBox translucentSearchDialogsCB;
	private SpecialValueComboBox translucencyCombo;
	private JSlider slider;

	private String fileSizeError;

	public static final String TERM_CR		= "\r";
	public static final String TERM_LF		= "\n";
	public static final String TERM_CRLF	= "\r\n";
	public static final String TERM_SYSTEM	= System.getProperty("line.separator");

	private static final String PROPERTY	= "property";


	/**
	 * Constructor.
	 *
	 * @param rtext The owning RText instance.
	 * @param msg The resource bundle to use.
	 */
	public GeneralOptionPanel(RText rtext, ResourceBundle msg) {

		super(msg.getString("OptGenName"));
		fileSizeError = msg.getString("OptGenFileSizeError");

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		// Set up our border and layout.
		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		// Create a panel for stuff aligned at the top.
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		JPanel newFilePanel = new JPanel(new SpringLayout());
		newFilePanel.setBorder(new OptionPanelBorder(
							msg.getString("OptNewFileTitle")));

		// Create a panel for selecting properties of new files.
		JLabel dirLabel = new JLabel(msg.getString("OptNewFileWD"));
		dirField = new JTextField();
		dirField.getDocument().addDocumentListener(this);
		dirLabel.setLabelFor(dirField);
		dirBrowseButton = new RButton(msg.getString("Browse"));
		dirBrowseButton.setActionCommand("Browse");
		dirBrowseButton.addActionListener(this);
		if (orientation.isLeftToRight()) {
			newFilePanel.add(dirLabel);
			newFilePanel.add(dirField);
			newFilePanel.add(dirBrowseButton);
		}
		else {
			newFilePanel.add(dirBrowseButton);
			newFilePanel.add(dirField);
			newFilePanel.add(dirLabel);
		}
		JLabel newlineLabel = new JLabel(msg.getString("LineTerminator"));
		terminatorCombo = new SpecialValueComboBox();
		UIUtil.fixComboOrientation(terminatorCombo);
		terminatorCombo.addSpecialItem(msg.getString("SysDef"), TERM_SYSTEM);
		terminatorCombo.addSpecialItem(msg.getString("CR"),     TERM_CR);
		terminatorCombo.addSpecialItem(msg.getString("LF"),     TERM_LF);
		terminatorCombo.addSpecialItem(msg.getString("CRLF"),   TERM_CRLF);
		terminatorCombo.setSelectedSpecialItem(TERM_SYSTEM);
		terminatorCombo.setActionCommand("LineTerminator");
		terminatorCombo.addActionListener(this);
		if (orientation.isLeftToRight()) {
			newFilePanel.add(newlineLabel);
			newFilePanel.add(terminatorCombo);
			// The filler rigid area below MUST have finite preferred width.
			newFilePanel.add(Box.createRigidArea(new Dimension(1,1)));
		}
		else {
			// The filler rigid area below MUST have finite preferred width.
			newFilePanel.add(Box.createRigidArea(new Dimension(1,1)));
			newFilePanel.add(terminatorCombo);
			newFilePanel.add(newlineLabel);
		}
		JLabel encLabel = new JLabel(msg.getString("OptNewFileEncoding"));
		encCombo = new JComboBox();
		UIUtil.fixComboOrientation(encCombo);
		// Populate the combo box with all available encodings.
		Map availcs = Charset.availableCharsets();
		Set keys = availcs.keySet();
		for (Iterator i=keys.iterator(); i.hasNext(); )
			encCombo.addItem(i.next());
		encCombo.setActionCommand("Encoding");
		encCombo.addActionListener(this);

		if (orientation.isLeftToRight()) {
			newFilePanel.add(encLabel);
			newFilePanel.add(encCombo);
			// The filler rigid area below MUST have finite preferred width.
			newFilePanel.add(Box.createRigidArea(new Dimension(1,1)));
		}
		else {
			// The filler rigid area below MUST have finite preferred width.
			newFilePanel.add(Box.createRigidArea(new Dimension(1,1)));
			newFilePanel.add(encCombo);
			newFilePanel.add(encLabel);
		}
		UIUtil.makeSpringCompactGrid(newFilePanel, 3,3, 5,5, 5,5);
		topPanel.add(newFilePanel);
		topPanel.add(Box.createVerticalStrut(5));

		// A panel for other general stuff.
		JPanel otherPanel = new JPanel();
		otherPanel.setLayout(new BoxLayout(otherPanel, BoxLayout.Y_AXIS));
		otherPanel.setBorder(new OptionPanelBorder(
									msg.getString("OptOtherTitle")));
		JPanel temp = new JPanel();
		temp.setLayout(new BoxLayout(temp, BoxLayout.LINE_AXIS));
		utf8BomCB = new JCheckBox(msg.getString("OptBOMInUtf8Files"));
		utf8BomCB.setActionCommand("Utf8BomCB");
		utf8BomCB.addActionListener(this);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(utf8BomCB, BorderLayout.LINE_START);
		otherPanel.add(temp2);
		sizeCheckCB = new JCheckBox(msg.getString("OptWarnIfFileLargerThan"));
		sizeCheckCB.setActionCommand("SizeCheckCB");
		sizeCheckCB.addActionListener(this);
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(1);
		sizeField = new JFormattedTextField(format);
		sizeField.getDocument().addDocumentListener(this);
		sizeField.setColumns(8);
		sizeField.setEditable(false);
		temp2 = new JPanel(new BorderLayout());
		temp.add(sizeCheckCB);
		temp.add(Box.createHorizontalStrut(5));
		temp.add(sizeField);
		temp.add(Box.createHorizontalGlue());
		temp2.add(temp, BorderLayout.LINE_START);
		otherPanel.add(temp2);
		topPanel.add(otherPanel);

		// A panel for "experimental" options.
		Box expPanel = Box.createVerticalBox();
		expPanel.setBorder(new OptionPanelBorder(msg.
										getString("OptExperimentalTitle")));
		SelectableLabel label = new SelectableLabel(
								msg.getString("ExperimentalDisclaimer"));
		expPanel.add(label);
		expPanel.add(Box.createVerticalStrut(5));
		translucentSearchDialogsCB = new JCheckBox(
								msg.getString("TranslucentSearchBoxes"));
		addLeftAligned(expPanel, translucentSearchDialogsCB);
		JLabel ruleLabel = new JLabel(msg.getString("TranslucencyRule"));
		translucencyCombo = new SpecialValueComboBox();
		translucencyCombo.addSpecialItem(msg.getString("Translucency.Never"), "0");
		translucencyCombo.addSpecialItem(msg.getString("Translucency.WhenNotFocused"), "1");
		translucencyCombo.addSpecialItem(msg.getString("Translucency.WhenOverlappingApp"), "2");
		translucencyCombo.addSpecialItem(msg.getString("Translucency.Always"), "3");
		JLabel opacityLabel = new JLabel(msg.getString("Opacity"));
		slider = new JSlider(0, 100);
		slider.setMajorTickSpacing(20);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		temp2 = new JPanel(new SpringLayout());
		if (orientation.isLeftToRight()) {
			temp2.add(ruleLabel);          temp2.add(translucencyCombo);
			temp2.add(opacityLabel);       temp2.add(slider);
		}
		else {
			temp2.add(translucencyCombo);  temp2.add(ruleLabel);
			temp2.add(slider);             temp2.add(opacityLabel);
		}
		UIUtil.makeSpringCompactGrid(temp2, 2,2, 5,5, 5,5);
		addLeftAligned(expPanel, temp2, 5, 20);
		topPanel.add(expPanel);

		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(orientation);

	}


	/**
	 * Called whenever an action occurs in this dialog.
	 *
	 * @param e The action event that occurred.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if ("Browse".equals(command)) {
			OptionsDialog od = getOptionsDialog();
			RDirectoryChooser dc = new RDirectoryChooser(od);
			String dirName = dirField.getText().trim();
			if (dirName.length()>0) {
				File dir = new File(dirName);
				dc.setChosenDirectory(dir);
			}
			dc.setVisible(true);
			String chosenDir = dc.getChosenDirectory();
			if (chosenDir!=null) {
				File dir = new File(chosenDir);
				if (dir.isDirectory() &&
						!chosenDir.equals(dirField.getText())) {
					dirField.setText(chosenDir);
					hasUnsavedChanges = true;
					firePropertyChange(PROPERTY, null, chosenDir);
				}
			}
		}

		else if ("LineTerminator".equals(command)) {
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, null,
							terminatorCombo.getSelectedItem());
		}

		else if ("Encoding".equals(command)) {
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, null, encCombo.getSelectedItem());
		}

		else if ("Utf8BomCB".equals(command)) {
			hasUnsavedChanges = true;
			boolean bom = utf8BomCB.isSelected();
			firePropertyChange(PROPERTY, !bom, bom);
		}

		else if ("SizeCheckCB".equals(command)) {
			hasUnsavedChanges = true;
			boolean sizeCheck = sizeCheckCB.isSelected();
			sizeField.setEditable(sizeCheck);
			firePropertyChange(PROPERTY, !sizeCheck, sizeCheck);
		}

	}


	/**
	 * Never called since we don't have JEditorPanes.
	 */
	public void changedUpdate(DocumentEvent e) {
	}


	/**
	 * {@inheritDoc}
	 */
	protected void doApplyImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		rtext.setWorkingDirectory(getWorkingDirectory()); // Doesn't update if not necessary.
		mainView.setLineTerminator(getLineTerminator()); // Ditto.
		mainView.setDefaultEncoding(getDefaultEncoding()); // Ditto.
		mainView.setWriteBOMInUtf8Files(getWriteUtf8BOM()); // Ditto.
		mainView.setDoFileSizeCheck(getDoFileSizeCheck()); // Ditto.
		mainView.setMaxFileSize(getMaxFileSize());		// Ditto.
	}


	/**
	 * {@inheritDoc}
	 */
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		float maxFileSize = getMaxFileSize();
		if (maxFileSize<0) {
			return new OptionsPanelCheckResult(this,sizeField,fileSizeError);
		}
		return null;
	}


	/**
	 * Returns the encoding selected by the user to use for new text files.
	 *
	 * @return The default encoding to use for new text files.
	 * @see #setDefaultEncoding(String)
	 */
	public String getDefaultEncoding() {
		return (String)encCombo.getSelectedItem();
	}


	/**
	 * Returns whether a file's size should be checked before it is opened.
	 *
	 * @return Whether a file's size is checked before it is opened.
	 * @see #setDoFileSizeCheck(boolean)
	 */
	public boolean getDoFileSizeCheck() {
		return sizeCheckCB.isSelected();
	}


	/**
	 * Returns the line terminator selected by the user.
	 *
	 * @return The line terminator.
	 * @see #setLineTerminator(String)
	 */
	public String getLineTerminator() {
		return terminatorCombo.getSelectedSpecialItem();
	}


	/**
	 * If file-size checking is enabled, this is the maximum size a
	 * file can be before the user is prompted before opening it.
	 *
	 * @return The maximum file size.
	 * @see #setMaxFileSize(float)
	 * @see #getDoFileSizeCheck()
	 */
	public float getMaxFileSize() {
		// JFormattedTextField.getValue() gets last GOOD value.
		Number num = (Number)sizeField.getValue();
		return num==null ? -1 : num.floatValue();
	}


	/**
	 * {@inheritDoc}
	 */
	public JComponent getTopJComponent() {
		return dirBrowseButton;
	}


	/**
	 * Returns whether "Write a BOM for UTF-8 files" is selected.
	 *
	 * @return Whether the value is selected.
	 */
	public boolean getWriteUtf8BOM() {
		return utf8BomCB.isSelected();
	}


	/**
	 * Returns the working directory chosen by the user.
	 *
	 * @return The working directory.
	 * @see #setWorkingDirectory
	 */
	public String getWorkingDirectory() {
		return dirField.getText();
	}


	/**
	 * Called when a text field is edited.
	 *
	 * @param e The document event.
	 */
	public void insertUpdate(DocumentEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(PROPERTY, null, "fake");
	}


	/**
	 * Called when a text field is edited.
	 *
	 * @param e The document event.
	 */
	public void removeUpdate(DocumentEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(PROPERTY, null, "fake");
	}


	/**
	 * Sets the encoding displayed to use for new text files.
	 *
	 * @param encoding The encoding.  If this value is invalid or not
	 *        supported by this OS, the system default is used.
	 * @see #getDefaultEncoding()
	 */
	private void setDefaultEncoding(String encoding) {

		if (encoding==null) {
			encoding = RTextFileChooser.getDefaultEncoding();
		}

		Charset cs1 = Charset.forName(encoding);

		int count = encCombo.getItemCount();
		for (int i=0; i<count; i++) {
			String item = (String)encCombo.getItemAt(i);
			Charset cs2 = Charset.forName(item);
			if (cs1.equals(cs2)) {
				encCombo.setSelectedIndex(i);
				return;
			}
		}

		// Encoding not found: select default.
		cs1 = Charset.forName(RTextFileChooser.getDefaultEncoding());
		for (int i=0; i<count; i++) {
			String item = (String)encCombo.getItemAt(i);
			Charset cs2 = Charset.forName(item);
			if (cs1.equals(cs2)) {
				encCombo.setSelectedIndex(i);
				return;
			}
		}

	}


	/**
	 * Sets whether a file's size is checked before it is opened.
	 *
	 * @param doCheck Whether to check a file's size.
	 * @see #getDoFileSizeCheck()
	 */
	private void setDoFileSizeCheck(boolean doCheck) {
		sizeCheckCB.setSelected(doCheck);
		sizeField.setEditable(doCheck);
	}


	/**
	 * Selects the specified line terminator.
	 *
	 * @param terminator The line terminator to select.
	 * @see #getLineTerminator()
	 */
	private void setLineTerminator(String terminator) {
		terminatorCombo.setSelectedSpecialItem(terminator);
	}


	/**
	 * If file size checking is enabled, this is the maximum size a file
	 * can be before the user is prompted before opening it.
	 *
	 * @param size The new maximum size for a file before the user is
	 *        prompted before opening it.
	 * @see #getMaxFileSize()
	 * @see #setDoFileSizeCheck(boolean)
	 */
	private void setMaxFileSize(float size) {
		sizeField.setValue(new Float(size));
	}


	/**
	 * {@inheritDoc}
	 */
	protected void setValuesImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		setWorkingDirectory(getWorkingDirectory());
		setLineTerminator(mainView.getLineTerminator());
		setDefaultEncoding(mainView.getDefaultEncoding());
		setWriteUtf8BOM(mainView.getWriteBOMInUtf8Files());
		setDoFileSizeCheck(mainView.getDoFileSizeCheck());
		setMaxFileSize(mainView.getMaxFileSize());
	}


	/**
	 * Toggles whether "Write a BOM for UTF-8 files" is selected.
	 *
	 * @param write Whether the value should be selected.
	 */
	private void setWriteUtf8BOM(boolean write) {
		utf8BomCB.setSelected(write);
	}


	/**
	 * Sets the working directory displayed.
	 *
	 * @param directory The working directory to display.
	 * @see #getWorkingDirectory
	 */
	private void setWorkingDirectory(String directory) {
		dirField.setText(directory);
	}


}