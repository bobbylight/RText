/*
 * 10/29/2004
 *
 * RTextAreaOptionPanel.java - An options panel that can be added to
 * org.fife.ui.OptionsDialog for an RTextArea.
 * Copyright (C) 2004 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextarea;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextAppThemes;
import org.fife.ui.*;
import org.fife.ui.rsyntaxtextarea.Theme;


/**
 * Options panel for basic <code>RTextArea</code> options.
 *
 * @author Robert Futrell
 * @version 0.7
 */
public class RTextAreaOptionPanel extends OptionsDialogPanel
		implements ActionListener, DocumentListener, PropertyChangeListener {

	/**
	 * ID used to identify this option panel.
	 */
	public static final String OPTION_PANEL_ID = "RTextAreaOptionPanel";

	private JLabel tabSizeLabel;
	private JTextField tabSizeField;
	private int tabSize;
	private JCheckBox emulateTabsCheckBox;

	private JCheckBox wordWrapCheckBox;
	private JCheckBox highlightCurrentLineCheckBox;
	private JCheckBox marginLineCheckBox;
	private JTextField marginLinePositionField;
	private JLabel marginLineColorLabel;
	private RColorSwatchesButton marginLineColorButton;
	private int marginLinePosition;

	private JCheckBox visibleWhitespaceCheckBox;
	private JCheckBox visibleEOLCheckBox;
	//private JCheckBox autoIndentCheckBox;
	private JCheckBox remWhitespaceLinesCheckBox;
	private JCheckBox autoInsertClosingCurlyCheckBox;
	private JCheckBox aaCheckBox;
	private JCheckBox fractionalMetricsCheckBox;

	private Box bracketMatchingPanel;
	private JCheckBox bracketMatchCheckBox;
	private JLabel bmBGColorLabel;
	private RColorSwatchesButton bmBGColorButton;
	private JLabel bmBorderColorLabel;
	private RColorSwatchesButton bmBorderColorButton;
	private JCheckBox bothBracketsCB;

	private JCheckBox showTabLinesCheckBox;
	private RColorSwatchesButton tabLineColorButton;

	private JButton restoreDefaultsButton;


	/**
	 * Constructor.
	 */
	public RTextAreaOptionPanel() {

		setId(OPTION_PANEL_ID);

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

		Box tabPanel = Box.createVerticalBox();
		tabPanel.setBorder(new OptionPanelBorder(msg.getString("Tabs")));
		Box inputPanel = createHorizontalBox();
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
		emulateTabsCheckBox = new JCheckBox(msg.getString("EmulateTabs"));
		emulateTabsCheckBox.setActionCommand("EmulateTabsCheckBox");
		emulateTabsCheckBox.addActionListener(this);
		addLeftAligned(tabPanel, emulateTabsCheckBox);
		tabPanel.add(Box.createVerticalGlue());
		topPanel.add(tabPanel);

		topPanel.add(Box.createVerticalStrut(5));

		Box bigOtherPanel = Box.createVerticalBox();
		bigOtherPanel.setBorder(new OptionPanelBorder(msg.getString("Other")));

		wordWrapCheckBox = new JCheckBox(msg.getString("WordWrap"));
		wordWrapCheckBox.setActionCommand("WordWrapCheckBox");
		wordWrapCheckBox.addActionListener(this);
		addLeftAligned(bigOtherPanel, wordWrapCheckBox);

		Box otherPanel = new Box(BoxLayout.LINE_AXIS);
		highlightCurrentLineCheckBox = new JCheckBox(msg.getString("HighlightCL"));
		highlightCurrentLineCheckBox.setActionCommand("HighlightCurrentLineCheckBox");
		highlightCurrentLineCheckBox.addActionListener(this);
		otherPanel.add(highlightCurrentLineCheckBox);
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
		marginLineColorButton = new RColorSwatchesButton();
		marginLineColorButton.addPropertyChangeListener(RColorButton.COLOR_CHANGED_PROPERTY, this);
		marginLineColorLabel.setLabelFor(marginLineColorButton);
		otherPanel.add(marginLineCheckBox);
		otherPanel.add(marginLinePositionField);
		otherPanel.add(Box.createHorizontalStrut(5));
		otherPanel.add(marginLineColorLabel);
		otherPanel.add(marginLineColorButton);

		otherPanel.add(Box.createHorizontalGlue());
		bigOtherPanel.add(otherPanel);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		visibleWhitespaceCheckBox = createCheckBox(msg, "VisibleWhitespace");
		addLeftAligned(bigOtherPanel, visibleWhitespaceCheckBox);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		visibleEOLCheckBox = createCheckBox(msg, "VisibleEOL");
		addLeftAligned(bigOtherPanel, visibleEOLCheckBox);

		autoInsertClosingCurlyCheckBox = createCheckBox(msg, "AutoCloseCurlys");
		addLeftAligned(bigOtherPanel, autoInsertClosingCurlyCheckBox);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		/*
		autoIndentCheckBox = createCheckBox("AutoIndent");
		addLeftAligned(bigOtherPanel, autoIndentCheckBox);
		bigOtherPanel.add(Box.createVerticalStrut(3));
		*/
		remWhitespaceLinesCheckBox = createCheckBox(msg, "RemWhitespaceLines");
		addLeftAligned(bigOtherPanel, remWhitespaceLinesCheckBox);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		aaCheckBox = new JCheckBox(msg.getString("SmoothText"));
		aaCheckBox.setActionCommand("aaCB");
		aaCheckBox.addActionListener(this);
		addLeftAligned(bigOtherPanel, aaCheckBox);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		fractionalMetricsCheckBox = createCheckBox(msg, "FracFM");
		addLeftAligned(bigOtherPanel, fractionalMetricsCheckBox);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		bracketMatchingPanel = createHorizontalBox();
		bracketMatchCheckBox = new JCheckBox(msg.getString("HighlightMB"));
		bracketMatchCheckBox.setActionCommand("BracketMatchCheckBox");
		bracketMatchCheckBox.addActionListener(this);
		bmBGColorLabel = new JLabel(msg.getString("BackgroundFill"));
		bmBGColorButton = new RColorSwatchesButton();
		bmBGColorButton.addPropertyChangeListener(RColorButton.COLOR_CHANGED_PROPERTY, this);
		bmBorderColorLabel = new JLabel(msg.getString("Border"));
		bmBorderColorButton = new RColorSwatchesButton();
		bmBorderColorButton.addPropertyChangeListener(RColorButton.COLOR_CHANGED_PROPERTY, this);
		bracketMatchingPanel.add(bracketMatchCheckBox);
		bracketMatchingPanel.add(bmBGColorLabel);
		bracketMatchingPanel.add(bmBGColorButton);
		bracketMatchingPanel.add(Box.createHorizontalStrut(5));
		bracketMatchingPanel.add(bmBorderColorLabel);
		bracketMatchingPanel.add(bmBorderColorButton);
		bracketMatchingPanel.add(Box.createHorizontalGlue());
		addLeftAligned(bigOtherPanel, bracketMatchingPanel);
		bothBracketsCB = new JCheckBox(msg.getString("HighlightBothBrackets"));
		bothBracketsCB.setActionCommand("BothBracketsCB");
		bothBracketsCB.addActionListener(this);
		addLeftAligned(bigOtherPanel, bothBracketsCB, 3, 20);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		showTabLinesCheckBox = new JCheckBox(msg.getString("ShowIndentGuide"));
		showTabLinesCheckBox.setActionCommand("ShowIndentGuide");
		showTabLinesCheckBox.addActionListener(this);
		Box box = createHorizontalBox();
		box.add(showTabLinesCheckBox);
		box.add(Box.createHorizontalStrut(5));
		tabLineColorButton = new RColorSwatchesButton();
		tabLineColorButton.addPropertyChangeListener(RColorButton.COLOR_CHANGED_PROPERTY, this);
		box.add(tabLineColorButton);
		box.add(Box.createHorizontalGlue());
		addLeftAligned(bigOtherPanel, box);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		topPanel.add(bigOtherPanel);

		restoreDefaultsButton = new JButton(msg.getString("RestoreDefaults"));
		restoreDefaultsButton.setActionCommand("RestoreDefaults");
		restoreDefaultsButton.addActionListener(this);
		addLeftAligned(topPanel, restoreDefaultsButton);

		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(orientation);

	}


	/**
	 * Listens for actions in this panel.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if ("RestoreDefaults".equals(command)) {

			// This panel's defaults are based on the current theme.
			RText app = (RText)getOptionsDialog().getParent();
			Theme rstaTheme;
			try {
				rstaTheme = RTextAppThemes.getRstaTheme(app.getTheme());
			} catch (IOException ioe) {
				app.displayException(ioe);
				return;
			}

			Color defaultCurrentLineHighlightColor = rstaTheme.currentLineHighlight;
			int defaultTabSize = RTextArea.getDefaultTabSize();
			int defaultMarginLinePosition = RTextArea.getDefaultMarginLinePosition();
			Color defaultMarginLineColor = rstaTheme.marginLineColor;
			boolean defaultAA = File.separatorChar=='\\';
			Color defaultBMBGColor = rstaTheme.matchedBracketBG;
			Color defaultBMBorderColor = rstaTheme.matchedBracketFG;
			Color defaultTabLineColor = rstaTheme.tabLineColor;
			if (defaultTabLineColor == null) { // This is optional in the theme, with no default
				defaultTabLineColor = Color.GRAY;
			}

			if (wordWrapCheckBox.isSelected() ||
				!highlightCurrentLineCheckBox.isSelected() ||
				getTabSize()!=defaultTabSize ||
				getEmulateTabs() ||
				!marginLineCheckBox.isSelected() ||
				getMarginLinePosition()!=defaultMarginLinePosition ||
				!getMarginLineColor().equals(defaultMarginLineColor) ||
				isWhitespaceVisible() ||
				visibleEOLCheckBox.isSelected() ||
				autoInsertClosingCurlyCheckBox.isSelected() ||
				remWhitespaceLinesCheckBox.isSelected() ||
				aaCheckBox.isSelected()!=defaultAA ||
				fractionalMetricsCheckBox.isSelected() ||
				!bracketMatchCheckBox.isSelected() ||
				!bmBGColorButton.getColor().equals(defaultBMBGColor) ||
				!bmBorderColorButton.getColor().equals(defaultBMBorderColor) ||
				bothBracketsCB.isSelected() ||
				showTabLinesCheckBox.isSelected() ||
				!defaultTabLineColor.equals(tabLineColorButton.getColor())) {
				wordWrapCheckBox.setSelected(false);
				highlightCurrentLineCheckBox.setSelected(true);
				setTabSize(defaultTabSize);
				setEmulateTabs(false);
				setMarginLineEnabled(true);
				setMarginLinePosition(defaultMarginLinePosition);
				setMarginLineColor(defaultMarginLineColor);
				setWhitespaceVisible(false);
				visibleEOLCheckBox.setSelected(false);
				autoInsertClosingCurlyCheckBox.setSelected(false);
				remWhitespaceLinesCheckBox.setSelected(false);
				aaCheckBox.setSelected(defaultAA);
				fractionalMetricsCheckBox.setSelected(false);
				setBracketMatchCheckboxSelected(true);
				setBracketMatchBGColor(defaultBMBGColor);
				bmBorderColorButton.setColor(defaultBMBorderColor);
				bothBracketsCB.setSelected(false);
				setTabLinesEnabled(false);
				tabLineColorButton.setColor(defaultTabLineColor);
				setDirty(true);
			}

		}

		else if ("WordWrapCheckBox".equals(command)) {
			setDirty(true);
		}

		else if ("HighlightCurrentLineCheckBox".equals(command)) {
			setDirty(true);
		}

		else if ("EmulateTabsCheckBox".equals(command)) {
			setDirty(true);
		}

		else if ("MarginLineCheckBox".equals(command)) {
			boolean selected = marginLineCheckBox.isSelected();
			marginLinePositionField.setEnabled(selected);
			marginLineColorButton.setEnabled(selected);
			setDirty(true);
		}

		else if ("VisibleWhitespace".equals(command)) {
			setDirty(true);
		}

		else if ("VisibleEOL".equals(command)) {
			setDirty(true);
		}

		else if ("AutoIndent".equals(command)) {
		}

		else if ("RemWhitespaceLines".equals(command)) {
			setDirty(true);
		}

		else if ("AutoCloseCurlys".equals(command)) {
			setDirty(true);
		}

		else if ("aaCB".equals(command)) {
			setDirty(true);
		}

		else if ("FracFM".equals(command)) {
			setDirty(true);
		}

		else if ("BracketMatchCheckBox".equals(command)) {
			boolean selected = bracketMatchCheckBox.isSelected();
			bmBGColorButton.setEnabled(selected);
			bmBorderColorButton.setEnabled(selected);
			bothBracketsCB.setEnabled(selected);
			setDirty(true);
		}

		else if ("BothBracketsCB".equals(command)) {
			setDirty(true);
		}

		else if ("ShowIndentGuide".equals(command)) {
			boolean show = showTabLinesCheckBox.isSelected();
			tabLineColorButton.setEnabled(show);
			setDirty(true);
		}

	}


	/**
	 * This doesn't get called but is here because this class implements
	 * <code>DocumentListener</code>.
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {
	}


	private JCheckBox createCheckBox(ResourceBundle msg, String key) {
		JCheckBox cb = new JCheckBox(msg.getString(key));
		cb.setActionCommand(key);
		cb.addActionListener(this);
		return cb;
	}


	/**
	 * Applies the settings entered into this dialog on the specified
	 * application.
	 *
	 * @param owner The application.
	 */
	@Override
	protected void doApplyImpl(Frame owner) {

		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();

		mainView.setLineWrap(getWordWrap());
		rtext.setRowColumnIndicatorVisible(!mainView.getLineWrap());
		mainView.setCurrentLineHighlightEnabled(isCurrentLineHighlightCheckboxSelected());
		mainView.setTabSize(getTabSize());				// Doesn't update if unnecessary.
		mainView.setTabsEmulated(getEmulateTabs());		// Doesn't update if unnecessary.
		mainView.setMarginLineEnabled(isMarginLineEnabled());	// Doesn't update if unnecessary.
		mainView.setMarginLinePosition(getMarginLinePosition()); // Doesn't update if unnecessary.
		mainView.setMarginLineColor(getMarginLineColor());	// Doesn't update if unnecessary.
		mainView.setRememberWhitespaceLines(!remWhitespaceLinesCheckBox.isSelected()); // Doesn't update if it doesn't have to.
		mainView.setAutoInsertClosingCurlys(autoInsertClosingCurlyCheckBox.isSelected()); // Doesn't update if it doesn't have to.
		mainView.setWhitespaceVisible(isWhitespaceVisible()); // (RSyntaxTextArea) doesn't update if not necessary.
		mainView.setShowEOLMarkers(visibleEOLCheckBox.isSelected());
		mainView.setAntiAliasEnabled(aaCheckBox.isSelected());
		mainView.setFractionalFontMetricsEnabled(fractionalMetricsCheckBox.isSelected()); // Doesn't update if not necessary.
		boolean bmEnabled = isBracketMatchCheckboxSelected();
		mainView.setBracketMatchingEnabled(bmEnabled);	// Doesn't update if it doesn't have to.
		mainView.setMatchedBracketBGColor(getBracketMatchBGColor()); // Doesn't update if it doesn't have to.
		mainView.setMatchedBracketBorderColor(bmBorderColorButton.getColor()); // Doesn't update if it doesn't have to.
		mainView.setMatchBothBrackets(bothBracketsCB.isSelected());
		mainView.setShowTabLines(showTabLinesCheckBox.isSelected());
		mainView.setTabLinesColor(tabLineColorButton.getColor());

	}


	/**
	 * Called when a text field in this panel gets updated.
	 */
	private void doDocumentUpdated(DocumentEvent e) {
		setDirty(true);
	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {

		// Ensure the tab size specified is valid.
		int temp;
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
	 * Returns the color the user chose for the background of a matched
	 * bracket.
	 *
	 * @return The color the user chose.
	 * @see #setBracketMatchBGColor
	 */
	public Color getBracketMatchBGColor() {
		return bmBGColorButton.getColor();
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
	 * Returns the <code>JComponent</code> at the "top" of this Options
	 * panel.  This is the component that will receive focus if the user
	 * switches to this Options panel in the Options dialog.  As an added
	 * bonus, if this component is a <code>JTextComponent</code>, its
	 * text is selected for easy changing.
	 */
	@Override
	public JComponent getTopJComponent() {
		return tabSizeField;
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
	@Override
	public void insertUpdate(DocumentEvent e) {
		doDocumentUpdated(e);
	}


	/**
	 * Returns whether or not the bracketMatch checkbox is selected.
	 *
	 * @return Whether or not the checkbox is selected.
	 * @see #setBracketMatchCheckboxSelected
	 */
	public boolean isBracketMatchCheckboxSelected() {
		return bracketMatchCheckBox.isSelected();
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
	 * Returns whether the user decided whitespace should be visible.
	 *
	 * @return Whether or not the user wants whitespace to be visible in the
	 *         text area(s).
	 * @see #setWhitespaceVisible
	 */
	public boolean isWhitespaceVisible() {
		return visibleWhitespaceCheckBox.isSelected();
	}


	/**
	 * Called when a property changes in an object we're listening to.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		// We need to forward this on to the options dialog, whatever
		// it is, so that the "Apply" button gets updated.
		setDirty(true);
	}


	/**
	 * Called when a text field in this panel gets updated.
	 */
	@Override
	public void removeUpdate(DocumentEvent e) {
		doDocumentUpdated(e);
	}


	/**
	 * Sets the color to use for the background of a matched bracket.
	 *
	 * @param color The color to use.
	 * @see #getBracketMatchBGColor
	 */
	public void setBracketMatchBGColor(Color color) {
		bmBGColorButton.setColor(color);
	}


	/**
	 * Sets whether or not the bracket match color checkbox is selected.
	 *
	 * @param selected Whether or not the checkbox is selected.
	 * @see #isBracketMatchCheckboxSelected
	 */
	public void setBracketMatchCheckboxSelected(boolean selected) {
		bracketMatchCheckBox.setSelected(selected);
		bmBGColorButton.setEnabled(selected);
		bmBorderColorButton.setEnabled(selected);
		bothBracketsCB.setEnabled(selected);
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
	 * Toggles whether tab lines are enabled.
	 *
	 * @param enabled Whether tab lines are enabled.
	 */
	public void setTabLinesEnabled(boolean enabled) {
		showTabLinesCheckBox.setSelected(enabled);
		tabLineColorButton.setEnabled(enabled);
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
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are not handled.
	 *
	 * @param owner The parent application.
	 * @see #setValues(Frame)
	 */
	@Override
	protected void setValuesImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		setWordWrap(mainView.getLineWrap());
		setCurrentLineHighlightCheckboxSelected(mainView.isCurrentLineHighlightEnabled());
		setTabSize(mainView.getTabSize());
		setEmulateTabs(mainView.areTabsEmulated());
		setMarginLineEnabled(mainView.isMarginLineEnabled());
		setMarginLinePosition(mainView.getMarginLinePosition());
		setMarginLineColor(mainView.getMarginLineColor());
		remWhitespaceLinesCheckBox.setSelected(!mainView.getRememberWhitespaceLines());
		autoInsertClosingCurlyCheckBox.setSelected(mainView.getAutoInsertClosingCurlys());
		setWhitespaceVisible(mainView.isWhitespaceVisible());
		visibleEOLCheckBox.setSelected(mainView.getShowEOLMarkers());
		aaCheckBox.setSelected(mainView.isAntiAliasEnabled());
		fractionalMetricsCheckBox.setSelected(mainView.isFractionalFontMetricsEnabled());
		boolean bmEnabled = mainView.isBracketMatchingEnabled();
		setBracketMatchCheckboxSelected(bmEnabled);
		setBracketMatchBGColor(mainView.getMatchedBracketBGColor());
		bmBorderColorButton.setColor(mainView.getMatchedBracketBorderColor());
		bothBracketsCB.setSelected(mainView.getMatchBothBrackets());
		setTabLinesEnabled(mainView.getShowTabLines());
		tabLineColorButton.setColor(mainView.getTabLinesColor());
	}


	/**
	 * Sets whether the "Visible whitespace" checkbox is selected.
	 *
	 * @param visible Whether the "visible whitespace" checkbox should be
	 *        selected.
	 * @see #isWhitespaceVisible
	 */
	public void setWhitespaceVisible(boolean visible) {
		visibleWhitespaceCheckBox.setSelected(visible);
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


}
