/*
 * 10/29/2004
 *
 * RTextAreaOptionPanel.java - An options panel that can be added to
 * org.fife.ui.OptionsDialog for an RTextArea.
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
package org.fife.ui.rtextarea;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.*;
import org.fife.ui.rtextarea.RTextArea;


/**
 * Options panel for basic <code>RTextArea</code> options.
 *
 * @author Robert Futrell
 * @version 0.7
 */
public class RTextAreaOptionPanel extends OptionsDialogPanel
		implements ActionListener, DocumentListener, PropertyChangeListener {

	private FontSelector fontSelector;

	private JLabel tabSizeLabel;
	private JTextField tabSizeField;
	private int tabSize;
	private JCheckBox emulateTabsCheckBox;

	private JCheckBox linkCB;
	private JLabel modKeyLabel;
	private JComboBox modKeyCombo;
	private JLabel linkColorLabel;
	private RColorSwatchesButton linkColorButton;

	private JTextField backgroundField;
	private RButton backgroundButton;
	private BackgroundDialog backgroundDialog;
	private Object background;
	private String bgImageFileName; // null if background is a color.
	private JCheckBox wordWrapCheckBox;
	private JCheckBox highlightCurrentLineCheckBox;
	private RColorSwatchesButton hclColorButton;
	private JCheckBox marginLineCheckBox;
	private JTextField marginLinePositionField;
	private JLabel marginLineColorLabel;
	private RColorSwatchesButton marginLineColorButton;
	private int marginLinePosition;

	private RButton restoreDefaultsButton;

	private static final String PROPERTY		= "property";


	/**
	 * Constructor.
	 */
	public RTextAreaOptionPanel() {

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		ResourceBundle msg = ResourceBundle.getBundle(
								"org.fife.ui.rtextarea.OptionPanel");

		setName(msg.getString("Title"));

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		// We'll add everything to this panel, then add this panel so that
		// stuff stays at the "top."
		Box topPanel = Box.createVerticalBox();
		JPanel fontPanel = new JPanel(new BorderLayout());
		fontPanel.setBorder(new OptionPanelBorder(msg.getString("Font")));
		fontSelector = new FontSelector();
		fontSelector.setColorSelectable(true);
		fontSelector.addPropertyChangeListener(FontSelector.FONT_PROPERTY, this);
		fontSelector.addPropertyChangeListener(FontSelector.FONT_COLOR_PROPERTY, this);
		fontPanel.add(fontSelector);
		topPanel.add(fontPanel);

		topPanel.add(Box.createVerticalStrut(5));

		Box tabPanel = Box.createVerticalBox();
		tabPanel.setBorder(new OptionPanelBorder(msg.getString("Tabs")));
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));
		tabSizeLabel = new JLabel(msg.getString("TabSize"));
		tabSizeField = new JTextField();
		tabSizeField.getDocument().addDocumentListener(this);
		Dimension size = new Dimension(40,tabSizeField.getPreferredSize().height);
		tabSizeField.setMaximumSize(size);
		tabSizeField.setPreferredSize(size);
		inputPanel.add(tabSizeLabel);
		inputPanel.add(tabSizeField);
		inputPanel.add(Box.createHorizontalGlue());
		tabPanel.add(inputPanel);
		JPanel etPanel = new JPanel();
		etPanel.setLayout(new BoxLayout(etPanel, BoxLayout.LINE_AXIS));
		emulateTabsCheckBox = new JCheckBox(msg.getString("EmulateTabs"));
		emulateTabsCheckBox.setActionCommand("EmulateTabsCheckBox");
		emulateTabsCheckBox.addActionListener(this);
		etPanel.add(emulateTabsCheckBox);
		etPanel.add(Box.createHorizontalGlue());
		tabPanel.add(etPanel);
		tabPanel.add(Box.createVerticalGlue());
		topPanel.add(tabPanel);

		topPanel.add(Box.createVerticalStrut(5));

		Box linkPanel = Box.createVerticalBox();
		linkPanel.setBorder(new OptionPanelBorder(msg.getString("Hyperlinks")));

		JPanel temp = new JPanel();
		temp.setLayout(new BoxLayout(temp, BoxLayout.LINE_AXIS));
		linkCB = new JCheckBox(msg.getString("MakeLinksClickable"));
		linkCB.setActionCommand("MakeLinksClickable");
		linkCB.addActionListener(this);
		temp.add(linkCB);
		temp.add(Box.createHorizontalGlue());
		linkPanel.add(temp);
		linkPanel.add(Box.createVerticalStrut(5));

		modKeyCombo = createModKeyCombo();
		modKeyLabel = new JLabel(msg.getString("ModifierKey"));
		modKeyLabel.setLabelFor(modKeyCombo);
		linkColorButton = new RColorSwatchesButton();
		linkColorButton.addPropertyChangeListener(this);
		linkColorLabel = new JLabel(msg.getString("HyperlinkColor"));
		linkColorLabel.setLabelFor(linkColorButton);
		JPanel modKeyPanel = new JPanel(new BorderLayout());
		modKeyPanel.add(modKeyCombo, BorderLayout.LINE_START);
		JPanel linkColorPanel = new JPanel(new BorderLayout());
		linkColorPanel.add(linkColorButton, BorderLayout.LINE_START);
		temp = new JPanel(new SpringLayout());
		if (orientation.isLeftToRight()) {
			temp.add(modKeyLabel);    temp.add(modKeyPanel);
			temp.add(linkColorLabel); temp.add(linkColorPanel);
		}
		else {
			temp.add(modKeyPanel);    temp.add(modKeyLabel);
			temp.add(linkColorPanel); temp.add(linkColorLabel);
		}
		UIUtil.makeSpringCompactGrid(temp, 2,2, 20,0, 5,5);
		linkPanel.add(temp);

		topPanel.add(linkPanel);
		topPanel.add(Box.createVerticalStrut(5));

		Box bigOtherPanel = Box.createVerticalBox();
		bigOtherPanel.setBorder(new OptionPanelBorder(msg.getString("Other")));

		Box otherPanel = new Box(BoxLayout.LINE_AXIS);
		JLabel bgLabel = new JLabel(msg.getString("Background"));
		backgroundField = new JTextField(20);
		backgroundField.setEditable(false);
		backgroundButton = new RButton(msg.getString("Change"));
		backgroundButton.setActionCommand("BackgroundButton");
		backgroundButton.addActionListener(this);
		bgLabel.setLabelFor(backgroundButton);
		otherPanel.add(bgLabel);
		otherPanel.add(Box.createHorizontalStrut(5));
		otherPanel.add(backgroundField);
		otherPanel.add(Box.createHorizontalStrut(5));
		otherPanel.add(backgroundButton);
		otherPanel.add(Box.createHorizontalGlue());
		bigOtherPanel.add(otherPanel);

		otherPanel = new Box(BoxLayout.LINE_AXIS);
		wordWrapCheckBox = new JCheckBox(msg.getString("WordWrap"));
		wordWrapCheckBox.setActionCommand("WordWrapCheckBox");
		wordWrapCheckBox.addActionListener(this);
		otherPanel.add(wordWrapCheckBox);
		otherPanel.add(Box.createHorizontalGlue());
		bigOtherPanel.add(otherPanel);

		otherPanel = new Box(BoxLayout.LINE_AXIS);
		highlightCurrentLineCheckBox = new JCheckBox(msg.getString("HighlightCL"));
		highlightCurrentLineCheckBox.setActionCommand("HighlightCurrentLineCheckBox");
		highlightCurrentLineCheckBox.addActionListener(this);
		hclColorButton = new RColorSwatchesButton(Color.BLACK, 50,15);
		hclColorButton.addPropertyChangeListener(this);
		otherPanel.add(highlightCurrentLineCheckBox);
		otherPanel.add(hclColorButton);
		otherPanel.add(Box.createHorizontalGlue());
		bigOtherPanel.add(otherPanel);

		otherPanel = new Box(BoxLayout.LINE_AXIS);
		marginLineCheckBox = new JCheckBox(msg.getString("DrawML"));
		marginLineCheckBox.setActionCommand("MarginLineCheckBox");
		marginLineCheckBox.addActionListener(this);
		marginLinePositionField = new JTextField();
		marginLinePositionField.getDocument().addDocumentListener(this);
		size = new Dimension(40,marginLinePositionField.getPreferredSize().height);
		marginLinePositionField.setMaximumSize(size);
		marginLinePositionField.setPreferredSize(size);
		marginLineColorLabel = new JLabel(msg.getString("WithThisColor"));
		marginLineColorButton = new RColorSwatchesButton(Color.BLACK, 50,15);
		marginLineColorButton.addPropertyChangeListener(this);
		marginLineColorLabel.setLabelFor(marginLineColorButton);
		otherPanel.add(marginLineCheckBox);
		otherPanel.add(marginLinePositionField);
		otherPanel.add(Box.createHorizontalStrut(5));
		otherPanel.add(marginLineColorLabel);
		otherPanel.add(marginLineColorButton);
		otherPanel.add(Box.createHorizontalGlue());
		bigOtherPanel.add(otherPanel);

		bigOtherPanel.add(Box.createVerticalStrut(5));

		topPanel.add(bigOtherPanel);

		JPanel rdPanel = new JPanel();
		rdPanel.setLayout(new BoxLayout(rdPanel, BoxLayout.LINE_AXIS));
		restoreDefaultsButton = new RButton(msg.getString("RestoreDefaults"));
		restoreDefaultsButton.setActionCommand("RestoreDefaults");
		restoreDefaultsButton.addActionListener(this);
		rdPanel.add(restoreDefaultsButton);
		rdPanel.add(Box.createHorizontalGlue());
		topPanel.add(rdPanel);

		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(orientation);

		msg = null;

	}


	/**
	 * Listens for actions in this panel.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals("RestoreDefaults")) {

			Color defaultCurrentLineHighlightColor = RTextArea.getDefaultCurrentLineHighlightColor();
			int defaultTabSize = RTextArea.getDefaultTabSize();
			int defaultMarginLinePosition = RTextArea.getDefaultMarginLinePosition();
			Color defaultMarginLineColor = RTextArea.getDefaultMarginLineColor();
			Font defaultFont = RTextArea.getDefaultFont();
			Color defaultForeground = RTextArea.getDefaultForeground();

			if ( !Color.WHITE.equals(background) ||
				wordWrapCheckBox.isSelected() ||
				!highlightCurrentLineCheckBox.isSelected() ||
				!getCurrentLineHighlightColor().equals(defaultCurrentLineHighlightColor) ||
				getTabSize()!=defaultTabSize ||
				getEmulateTabs()==true ||
				!linkCB.isSelected() ||
				modKeyCombo.getSelectedIndex()!=0 ||
				!linkColorButton.getColor().equals(Color.BLUE) ||
				!marginLineCheckBox.isSelected() ||
				getMarginLinePosition()!=defaultMarginLinePosition ||
				!getMarginLineColor().equals(defaultMarginLineColor) ||
				!getTextAreaFont().equals(defaultFont) ||
				getUnderline()==true ||
				!getTextAreaForeground().equals(defaultForeground))
			{
				setBackgroundObject(Color.WHITE);
				wordWrapCheckBox.setSelected(false);
				highlightCurrentLineCheckBox.setSelected(true);
				hclColorButton.setEnabled(true);
				setCurrentLineHighlightColor(defaultCurrentLineHighlightColor);
				setTabSize(defaultTabSize);
				setEmulateTabs(false);
				linkCB.setSelected(true);
				modKeyCombo.setSelectedIndex(0);
				linkColorButton.setColor(Color.BLUE);
				setMarginLineEnabled(true);
				setMarginLinePosition(defaultMarginLinePosition);
				setMarginLineColor(defaultMarginLineColor);
				setTextAreaFont(defaultFont, false);
				setTextAreaForeground(defaultForeground);
				setHyperlinksEnabled(true);
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

		}

		else if (command.equals("BackgroundButton")) {
			if (backgroundDialog==null) {
				backgroundDialog = new BackgroundDialog(getOptionsDialog());
			}
			backgroundDialog.initializeData(background, bgImageFileName);
			backgroundDialog.setVisible(true);
			Object newBG = backgroundDialog.getChosenBackground();
			// Non-null newBG means user hit OK, not Cancel.
			if (newBG!=null && !newBG.equals(background)) {
				Object oldBG = background;
				setBackgroundObject(newBG);
				setBackgroundImageFileName(backgroundDialog.
										getCurrentImageFileName());
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, oldBG, newBG);
			}
		}

		else if (command.equals("WordWrapCheckBox")) {
			boolean ww = wordWrapCheckBox.isSelected();
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !ww, ww);
		}
	
		else if (command.equals("HighlightCurrentLineCheckBox")) {
			boolean selected = highlightCurrentLineCheckBox.isSelected();
			hclColorButton.setEnabled(selected);
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

		else if (command.equals("EmulateTabsCheckBox")) {
			boolean selected = emulateTabsCheckBox.isSelected();
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

		else if (command.equals("MarginLineCheckBox")) {
			boolean selected = marginLineCheckBox.isSelected();
			marginLinePositionField.setEnabled(selected);
			marginLineColorButton.setEnabled(selected);
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

		else if (command.equals("MakeLinksClickable")) {
			boolean selected = linkCB.isSelected();
			modKeyLabel.setEnabled(selected);
			modKeyCombo.setEnabled(selected);
			linkColorLabel.setEnabled(selected);
			linkColorButton.setEnabled(selected);
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

		else if (command.equals("ModKeyCombo")) {
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, -1, modKeyCombo.getSelectedIndex());
		}

	}


	/**
	 * This doesn't get called but is here because this class implements
	 * <code>DocumentListener</code>.
	 */
	public void changedUpdate(DocumentEvent e) {
	}


	private JComboBox createModKeyCombo() {
		Integer[] items = new Integer[] {
			new Integer(InputEvent.CTRL_DOWN_MASK),
			new Integer(InputEvent.META_DOWN_MASK),
			new Integer(InputEvent.SHIFT_DOWN_MASK),
			new Integer(InputEvent.ALT_DOWN_MASK),
		};
		JComboBox combo = new JComboBox(items);
		combo.setRenderer(new ModKeyCellRenderer());
		combo.setActionCommand("ModKeyCombo");
		combo.addActionListener(this);
		return combo;
	}


	/**
	 * Applies the settings entered into this dialog on the specified
	 * application.
	 *
	 * @param owner The application.
	 */
	protected void doApplyImpl(Frame owner) {

		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();

		mainView.setTextAreaForeground(getTextAreaForeground());
		mainView.setTextAreaFont(getTextAreaFont(), getUnderline());
		mainView.setBackgroundObject(getBackgroundObject());
		mainView.setBackgroundImageFileName(getBackgroundImageFileName());
		mainView.setLineWrap(getWordWrap());
		rtext.setRowColumnIndicatorVisible(!mainView.getLineWrap());
		if (isCurrentLineHighlightCheckboxSelected()==true) {
			mainView.setCurrentLineHighlightEnabled(true);
			mainView.setCurrentLineHighlightColor(getCurrentLineHighlightColor());
		}
		else {
			mainView.setCurrentLineHighlightEnabled(false);
		}
		mainView.setTabSize(getTabSize());				// Doesn't update if unnecessary.
		mainView.setTabsEmulated(getEmulateTabs());		// Doesn't update if unnecessary.
		mainView.setMarginLineEnabled(isMarginLineEnabled());	// Doesn't update if unnecessary.
		mainView.setMarginLinePosition(getMarginLinePosition()); // Doesn't update if unnecessary.
		mainView.setMarginLineColor(getMarginLineColor());	// Doesn't update if unnecessary.
		mainView.setHyperlinksEnabled(getHyperlinksEnabled()); // Doesn't update if unnecessary.
		mainView.setHyperlinkColor(getHyperlinkColor()); // Doesn't update if unnecessary.
		mainView.setHyperlinkModifierKey(getHyperlinkModifierKey()); // Doesn't update if unnecessary.

	}


	/**
	 * Called when a text field in this panel gets updated.
	 */
	private void doDocumentUpdated(DocumentEvent e) {

		hasUnsavedChanges = true;

		Document modifiedDocument = e.getDocument();

		if (modifiedDocument==tabSizeField.getDocument()) {
			firePropertyChange(PROPERTY, null, tabSizeField.getText());
		}
		else if (modifiedDocument==marginLinePositionField.getDocument()) {
			firePropertyChange(PROPERTY,
							null, marginLinePositionField.getText());
		}
			
	}


	/**
	 * Checks whether or not all input the user specified on this panel is
	 * valid.  This should be overridden to check, for example, whether
	 * text fields have valid values, etc.  This method will be called
	 * whenever the user clicks "OK" or "Apply" on the options dialog to
	 * ensure all input is valid.  If it isn't, the component with invalid
	 * data will be given focus and the user will be prompted to fix it.<br>
	 * 
	 *
	 * @return <code>null</code> if the panel has all valid inputs, or an
	 *         <code>OptionsPanelCheckResult</code> if an input was invalid.
	 *         This component is the one that had the error and will be
	 *         given focus, and the string is an error message that will be
	 *         displayed.
	 */
	public OptionsPanelCheckResult ensureValidInputs() {

		// Ensure the tab size specified is valid.
		int temp = 0;
		try {
			temp = Integer.parseInt(tabSizeField.getText());
			if (temp<0) throw new NumberFormatException();
		} catch (NumberFormatException nfe) {
			OptionsPanelCheckResult res = new OptionsPanelCheckResult(this);
			res.errorMessage = "Invalid number format for tab size;\nPlease input a tab size greater than zero.";
			res.component = tabSizeField;
			// Hack; without this, tabSize is still valid, so if they hit Cancel
			// then brought the Options dialog back up, the invalid text would
			// still be there.
			tabSize = -1;
			return res;
		}
		tabSize = temp;	// Store the value the user will get.

		// Ensure the margin line position specified is valid.
		try {
			temp = Integer.parseInt(marginLinePositionField.getText());
			if (temp<0) throw new NumberFormatException();
		} catch (NumberFormatException nfe) {
			OptionsPanelCheckResult res = new OptionsPanelCheckResult(this);
			res.errorMessage = "Invalid margin line position;\nPlease input a position greater than zero.";
			res.component = marginLinePositionField;
			// Hack; without this, marginLinePosition is still valid, so if
			// they hig Cancel then brought the Options dialog back up, the
			// invalid text would still be there.
			marginLinePosition = -1;
			return res;
		}
		marginLinePosition = temp;	// Store the value the user will get.

		// If that went okay then the entire panel is okay.
		return null;

	}


	/**
	 * Returns the name of the file containing the chosen background image.
	 * If the user selected a color for the background, this method returns
	 * <code>null</code>.
	 *
	 * @return The name of the file containing the chosen background image.
	 * @see #getBackgroundObject()
	 */
	public String getBackgroundImageFileName() {
		return bgImageFileName;
	}


	/**
	 * Returns the background object (a color or an image) selected by the
	 * user.
	 *
	 * @return The background object.
	 * @see #getBackgroundImageFileName()
	 */
	public Object getBackgroundObject() {
		return background;
	}


	/**
	 * Returns the current line highlight color chosen by the user.
	 *
	 * @return The color.
	 */
	public Color getCurrentLineHighlightColor() {
		return hclColorButton.getColor();
	}


	/**
	 * Returns whether or not the user decided to emulate tabs.
	 *
	 * @return <code>true</code> iff the "emulate tabs with whitespace"
	 *         checkbox was checked.
	 */
	public boolean getEmulateTabs() {
		return emulateTabsCheckBox.isSelected();
	}


	/**
	 * Returns the color this dialog shows to use for hyperlinks.
	 *
	 * @return The color displayed to use.
	 */
	private Color getHyperlinkColor() {
		return linkColorButton.getColor();
	}


	/**
	 * Returns the modifier key this dialog shows for hyperlinks.
	 *
	 * @return The modifier key(s).
	 * @see java.awt.event.InputEvent
	 */
	private int getHyperlinkModifierKey() {
		switch (modKeyCombo.getSelectedIndex()) {
			default:
			case 0:
				return InputEvent.CTRL_DOWN_MASK;
			case 1:
				return InputEvent.META_DOWN_MASK;
			case 2:
				return InputEvent.SHIFT_DOWN_MASK;
			case 3:
				return InputEvent.ALT_DOWN_MASK;
		}
	}


	/**
	 * Returns whether the user decided to enable hyperlinks in editors.
	 *
	 * @return Whether the user wants hyperlinks enabled.
	 */
	public boolean getHyperlinksEnabled() {
		return linkCB.isSelected();
	}


	/**
	 * Returns the color the user chose for the margin line.
	 *
	 * @return The color the user chose.
	 */
	public Color getMarginLineColor() {
		return marginLineColorButton.getColor();
	}


	/**
	 * Returns the margin line position text on this panel.
	 *
	 * @return The text in the "margin line position" text field.
	 */
	public int getMarginLinePosition() {
		return marginLinePosition;
	}


	/**
	 * Returns the tab size selected by the user.
	 *
	 * @return The tab size selected by the user.
	 */
	public int getTabSize() {
		return tabSize;
	}


	/**
	 * Returns the font to use in text areas.
	 *
	 * @return The font to use.
	 */
	public Font getTextAreaFont() {
		return fontSelector.getDisplayedFont();
	}


	/**
	 * Returns the text area's foreground color.
	 *
	 * @return The foreground color of the text area.
	 */
	public Color getTextAreaForeground() {
		return fontSelector.getFontColor();
	}


	/**
	 * Returns the <code>JComponent</code> at the "top" of this Options
	 * panel.  This is the component that will receive focus if the user
	 * switches to this Options panel in the Options dialog.  As an added
	 * bonus, if this component is a <code>JTextComponent</code>, its
	 * text is selected for easy changing.
	 */
	public JComponent getTopJComponent() {
		return tabSizeField;
	}


	/**
	 * Returns whether the text area's font should be underlined.
	 *
	 * @return Whether the text areas should underline their font.
	 * @see #getFont()
	 */
	public boolean getUnderline() {
		return fontSelector.getUnderline();
	}


	/**
	 * Returns whether the user selected word wrap.
	 *
	 * @return Whether or not the word wrap checkbox is checked.
	 */
	public boolean getWordWrap() {
		return wordWrapCheckBox.isSelected();
	}


	/**
	 * Called when a text field in this panel gets updated.
	 */
	public void insertUpdate(DocumentEvent e) {
		doDocumentUpdated(e);
	}


	/**
	 * Returns whether or not the current line highlight color checkbox is
	 * selected.
	 *
	 * @return Whether or not the checkbox is selected.
	 */
	public boolean isCurrentLineHighlightCheckboxSelected() {
		return highlightCurrentLineCheckBox.isSelected();
	}


	/**
	 * Returns whether or not the margin line stuff is enabled (i.e.,
	 * whether or not the "Margin line" checkbox is checked).
	 *
	 * @return Whether or not the margin line options are enabled.
	 */
	public boolean isMarginLineEnabled() {
		return marginLineCheckBox.isSelected();
	}


	/**
	 * Called when a property changes in an object we're listening to.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		// We need to forward this on to the options dialog, whatever
		// it is, so that the "Apply" button gets updated.
		hasUnsavedChanges = true;
		firePropertyChange(PROPERTY, e.getOldValue(), e.getNewValue());
	}


	/**
	 * Called when a text field in this panel gets updated.
	 */
	public void removeUpdate(DocumentEvent e) {
		doDocumentUpdated(e);
	}


	/**
	 * Sets the name of the file containing the background image.  If the
	 * initial background object is a color, you should pass <code>null</code>
	 * to this method.
	 *
	 * @param name The name of the file containing the background image.
	 * @see #getBackgroundImageFileName
	 * @see #setBackgroundObject
	 */
	private void setBackgroundImageFileName(String name) {
		bgImageFileName = name;
		if (bgImageFileName!=null)
			backgroundField.setText(bgImageFileName);
	}


	/**
	 * Sets the background object displayed in this options panel.
	 *
	 * @param background The background object.
	 * @see #getBackgroundObject
	 */
	private void setBackgroundObject(Object background) {
		if (background instanceof Color) {
			String s = background.toString();
			backgroundField.setText(s.substring(s.indexOf('[')));
		}
		else if (background instanceof Image) {
			// backgroundField taken care of by setBackgroundImageFileName.
		}
		else {
			throw new IllegalArgumentException("Background must be either " +
				"a Color or an Image");
		}
		this.background = background;
	}


	/**
	 * Sets the current line highlight color displayed by this dialog.
	 *
	 * @param color The color to display for the current line highlight color.
	 *        If this parameter is <code>null</code>, <code>Color.BLACK</code>
	 *        is used (??).
	 * @see #getCurrentLineHighlightColor
	 */
	private void setCurrentLineHighlightColor(Color color) {
		hclColorButton.setColor(color);
	}


	/**
	 * Sets whether or not the current line highlight color checkbox is
	 * selected.
	 *
	 * @param selected Whether or not the checkbox is selected.
	 * @see #isCurrentLineHighlightCheckboxSelected
	 */
	private void setCurrentLineHighlightCheckboxSelected(boolean selected) {
		highlightCurrentLineCheckBox.setSelected(selected);
		hclColorButton.setEnabled(selected);
	}


	/**
	 * Sets the status of the "emulate tabs with whitespace" check box.
	 *
	 * @param areEmulated Whether or not the check box is checked.
	 */
	private void setEmulateTabs(boolean areEmulated) {
		emulateTabsCheckBox.setSelected(areEmulated);
	}


	/**
	 * Sets the color this dialog displays for hyperlinks.
	 *
	 * @param c The color to display.  This cannot be <code>null</code>.
	 */
	private void setHyperlinkColor(Color c) {
		linkColorButton.setColor(c);
	}


	/**
	 * Sets the modifier key this dialog shows for hyperlinks.
	 *
	 * @param key The modifier key(s).
	 * @see java.awt.event.InputEvent
	 */
	private void setHyperlinkModifierKey(int key) {
		switch (key) {
			default:
			case InputEvent.CTRL_DOWN_MASK:
				modKeyCombo.setSelectedIndex(0);
				break;
			case InputEvent.META_DOWN_MASK:
				modKeyCombo.setSelectedIndex(1);
				break;
			case InputEvent.SHIFT_DOWN_MASK:
				modKeyCombo.setSelectedIndex(2);
				break;
			case InputEvent.ALT_DOWN_MASK:
				modKeyCombo.setSelectedIndex(3);
				break;
		}
	}


	/**
	 * Sets the status of the "Enable hyperlinks" checkbox.
	 *
	 * @param clickable Whether the checkbox is selected.
	 */
	private void setHyperlinksEnabled(boolean enabled) {
		linkCB.setSelected(enabled);
		modKeyLabel.setEnabled(enabled);
		modKeyCombo.setEnabled(enabled);
		linkColorLabel.setEnabled(enabled);
		linkColorButton.setEnabled(enabled);
	}


	/**
	 * Sets the margin line color displayed by this dialog.
	 *
	 * @param color The color to display for the margin line color.
	 * @see #getMarginLineColor
	 */
	private void setMarginLineColor(Color color) {
		marginLineColorButton.setColor(color);
	}


	/**
	 * Sets whether or not the margin line stuff is enabled (i.e.,
	 * whether or not the "Margin line" checkbox is checked).
	 *
	 * @param enabled Whether or not the margin line options should be
	 *        enabled.
	 * @see #isMarginLineEnabled
	 */
	private void setMarginLineEnabled(boolean enabled) {
		marginLineCheckBox.setSelected(enabled);
		marginLinePositionField.setEnabled(enabled);
		marginLineColorButton.setEnabled(enabled);
	}


	/**
	 * Returns the margin line position currently being displayed.
	 *
	 * @param position The margin line position to display.
	 * @see #getMarginLinePosition
	 */
	private void setMarginLinePosition(int position) {
		if (marginLinePosition!=position && position>0) {
			marginLinePosition = position;
		}
		// We do this not in the if-condition above because the user could
		// have typed in a bad value, then hit "Cancel" previously, and we
		// need to clear this out.
		marginLinePositionField.setText(Integer.toString(marginLinePosition));
	}


	/**
	 * Sets the tab size currently being displayed.
	 *
	 * @param tabSize The tab size to display.
	 */
	private void setTabSize(int tabSize) {
		if (this.tabSize!=tabSize && tabSize>0) {
			this.tabSize = tabSize;
		}
		// We do this not in the if-condition above because the user could
		// have typed in a bad value, then hit "Cancel" previously, and we
		// need to clear this out.
		tabSizeField.setText(Integer.toString(tabSize));
	}


	/**
	 * Sets the font/underline status to display for text areas.
	 *
	 * @param font The font.
	 * @param underline Whether the font is underlined.
	 * @see #getTextAreaFont()
	 * @see #getUnderline()
	 */
	private void setTextAreaFont(Font font, boolean underline) {
		fontSelector.setDisplayedFont(font, underline);
	}


	/**
	 * Sets the foreground color for text areas.
	 *
	 * @param fg The new foreground color.
	 * @see #getTextAreaForeground()
	 */
	private void setTextAreaForeground(Color fg) {
		fontSelector.setFontColor(fg);
	}


	/**
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are not handled.
	 *
	 * @param owner The parent application.
	 * @see #setValues(Frame)
	 */
	protected void setValuesImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		setTextAreaForeground(mainView.getTextAreaForeground());
		setTextAreaFont(mainView.getTextAreaFont(), mainView.getTextAreaUnderline());
		setBackgroundObject(mainView.getBackgroundObject());
		setBackgroundImageFileName(mainView.getBackgroundImageFileName());
		setWordWrap(mainView.getLineWrap());
		setCurrentLineHighlightCheckboxSelected(mainView.isCurrentLineHighlightEnabled());
		setCurrentLineHighlightColor(mainView.getCurrentLineHighlightColor());
		setTabSize(mainView.getTabSize());
		setEmulateTabs(mainView.areTabsEmulated());
		setMarginLineEnabled(mainView.isMarginLineEnabled());
		setMarginLinePosition(mainView.getMarginLinePosition());
		setMarginLineColor(mainView.getMarginLineColor());
		setHyperlinksEnabled(mainView.getHyperlinksEnabled());
		setHyperlinkColor(mainView.getHyperlinkColor());
		setHyperlinkModifierKey(mainView.getHyperlinkModifierKey());
	}


	/**
	 * Sets whether the word wrap checkbox is checked.
	 *
	 * @param enabled Whether or not to check the word wrap checkbox.
	 * @see #getWordWrap
	 */
	private void setWordWrap(boolean enabled) {
		wordWrapCheckBox.setSelected(enabled);
	}


	/**
	 * Overridden to ensure the background dialog is updated as well.
	 */
	public void updateUI() {
		super.updateUI();
		if (backgroundDialog!=null) {
			SwingUtilities.updateComponentTreeUI(backgroundDialog); // Updates dialog.
			backgroundDialog.updateUI(); // Updates image file chooser.
		}
	}


	/**
	 * Renderer for a "modifier key" combo box.
	 */
	private static class ModKeyCellRenderer extends DefaultListCellRenderer {

		public Component getListCellRendererComponent(JList list,
			Object value, int index, boolean selected, boolean hasFocus) {
			super.getListCellRendererComponent(list, value, index,
										selected, hasFocus);
			int i = ((Integer)value).intValue();
			setText(InputEvent.getModifiersExText(i));
			return this;
		}

	}


}