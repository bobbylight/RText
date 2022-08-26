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
package org.fife.ui.rsyntaxtextarea;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.*;
import org.fife.ui.rtextarea.RTextArea;


/**
 * Options panel for basic <code>RTextArea</code> options.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class RTextAreaOptionPanel extends AbstractTextAreaOptionPanel
		implements DocumentListener, PropertyChangeListener {

	/**
	 * ID used to identify this option panel.
	 */
	public static final String OPTION_PANEL_ID = "RTextAreaOptionPanel";

	private JCheckBox wordWrapCheckBox;
	private JCheckBox highlightCurrentLineCheckBox;
	private JCheckBox marginLineCheckBox;
	private JTextField marginLinePositionField;
	private int marginLinePosition;

	//private JCheckBox autoIndentCheckBox;
	private JCheckBox remWhitespaceLinesCheckBox;
	private JCheckBox autoInsertClosingCurlyCheckBox;
	private JCheckBox aaCheckBox;
	private JCheckBox fractionalMetricsCheckBox;

	private JCheckBox bracketMatchCheckBox;
	private JCheckBox bothBracketsCB;


	/**
	 * Constructor.
	 */
	public RTextAreaOptionPanel() {

		setId(OPTION_PANEL_ID);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setName(MSG.getString("Title"));

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		// We'll add everything to this panel, then add this panel so that
		// stuff stays at the "top."
		Box topPanel = Box.createVerticalBox();

		Box bigOtherPanel = Box.createVerticalBox();
		bigOtherPanel.setBorder(new OptionPanelBorder(MSG.getString("Other")));

		wordWrapCheckBox = new JCheckBox(MSG.getString("WordWrap"));
		wordWrapCheckBox.setActionCommand("WordWrapCheckBox");
		wordWrapCheckBox.addActionListener(this);
		addLeftAligned(bigOtherPanel, wordWrapCheckBox);

		Box otherPanel = new Box(BoxLayout.LINE_AXIS);
		highlightCurrentLineCheckBox = new JCheckBox(MSG.getString("HighlightCL"));
		highlightCurrentLineCheckBox.setActionCommand("HighlightCurrentLineCheckBox");
		highlightCurrentLineCheckBox.addActionListener(this);
		otherPanel.add(highlightCurrentLineCheckBox);
		otherPanel.add(Box.createHorizontalGlue());
		bigOtherPanel.add(otherPanel);

		otherPanel = new Box(BoxLayout.LINE_AXIS);
		marginLineCheckBox = new JCheckBox(MSG.getString("DrawML"));
		marginLineCheckBox.setActionCommand("MarginLineCheckBox");
		marginLineCheckBox.addActionListener(this);
		marginLinePositionField = new JTextField();
		marginLinePositionField.getDocument().addDocumentListener(this);
		Dimension size = new Dimension(40,marginLinePositionField.getPreferredSize().height);
		marginLinePositionField.setMaximumSize(size);
		marginLinePositionField.setPreferredSize(size);
		otherPanel.add(marginLineCheckBox);
		otherPanel.add(marginLinePositionField);

		otherPanel.add(Box.createHorizontalGlue());
		bigOtherPanel.add(otherPanel);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		autoInsertClosingCurlyCheckBox = createCheckBox("AutoCloseCurlys");
		addLeftAligned(bigOtherPanel, autoInsertClosingCurlyCheckBox);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		/*
		autoIndentCheckBox = createCheckBox("AutoIndent");
		addLeftAligned(bigOtherPanel, autoIndentCheckBox);
		bigOtherPanel.add(Box.createVerticalStrut(3));
		*/
		remWhitespaceLinesCheckBox = createCheckBox("RemWhitespaceLines");
		addLeftAligned(bigOtherPanel, remWhitespaceLinesCheckBox);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		aaCheckBox = new JCheckBox(MSG.getString("SmoothText"));
		aaCheckBox.setActionCommand("aaCB");
		aaCheckBox.addActionListener(this);
		addLeftAligned(bigOtherPanel, aaCheckBox);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		fractionalMetricsCheckBox = createCheckBox("FracFM");
		addLeftAligned(bigOtherPanel, fractionalMetricsCheckBox);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		Box bracketMatchingPanel = createHorizontalBox();
		bracketMatchCheckBox = new JCheckBox(MSG.getString("HighlightMB"));
		bracketMatchCheckBox.setActionCommand("BracketMatchCheckBox");
		bracketMatchCheckBox.addActionListener(this);
		bracketMatchingPanel.add(bracketMatchCheckBox);
		bracketMatchingPanel.add(Box.createHorizontalGlue());
		addLeftAligned(bigOtherPanel, bracketMatchingPanel);
		bothBracketsCB = new JCheckBox(MSG.getString("HighlightBothBrackets"));
		bothBracketsCB.setActionCommand("BothBracketsCB");
		bothBracketsCB.addActionListener(this);
		addLeftAligned(bigOtherPanel, bothBracketsCB, 3, 20);
		bigOtherPanel.add(Box.createVerticalStrut(3));

		topPanel.add(bigOtherPanel);

		// Create a panel containing the preview and "Restore Defaults"
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(new PreviewPanel(MSG, 9, 40));
		bottomPanel.add(createRestoreDefaultsPanel(), BorderLayout.SOUTH);
		topPanel.add(bottomPanel);

		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(orientation);

	}


	/**
	 * Listens for actions in this panel.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if ("WordWrapCheckBox".equals(command)) {
			setDirty(true);
		}

		else if ("HighlightCurrentLineCheckBox".equals(command)) {
			setDirty(true);
		}

		else if ("EmulateTabsCheckBox".equals(command)) {
			setDirty(true);
		}

		else if ("MarginLineCheckBox".equals(command)) {
			marginLinePositionField.setEnabled(marginLineCheckBox.isSelected());
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
			bothBracketsCB.setEnabled(bracketMatchCheckBox.isSelected());
			setDirty(true);
		}

		else if ("BothBracketsCB".equals(command)) {
			setDirty(true);
		}

		else if ("ShowIndentGuide".equals(command)) {
			setDirty(true);
		}

		else {
			super.actionPerformed(e);
		}
	}


	/**
	 * This doesn't get called but is here because this class implements
	 * <code>DocumentListener</code>.
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {
	}


	private JCheckBox createCheckBox(String key) {
		JCheckBox cb = new JCheckBox(MSG.getString(key));
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

		mainView.setLineWrap(wordWrapCheckBox.isSelected());
		rtext.setRowColumnIndicatorVisible(!mainView.getLineWrap());
		mainView.setCurrentLineHighlightEnabled(highlightCurrentLineCheckBox.isSelected());
		mainView.setMarginLineEnabled(marginLineCheckBox.isSelected());	// Doesn't update if unnecessary.
		mainView.setMarginLinePosition(getMarginLinePosition()); // Doesn't update if unnecessary.
		mainView.setRememberWhitespaceLines(!remWhitespaceLinesCheckBox.isSelected()); // Doesn't update if it doesn't have to.
		mainView.setAutoInsertClosingCurlys(autoInsertClosingCurlyCheckBox.isSelected()); // Doesn't update if it doesn't have to.
		mainView.setAntiAliasEnabled(aaCheckBox.isSelected());
		mainView.setFractionalFontMetricsEnabled(fractionalMetricsCheckBox.isSelected()); // Doesn't update if not necessary.
		mainView.setBracketMatchingEnabled(bracketMatchCheckBox.isSelected());	// Doesn't update if it doesn't have to.
		mainView.setMatchBothBrackets(bothBracketsCB.isSelected());

	}


	/**
	 * Called when a text field in this panel gets updated.
	 */
	private void doDocumentUpdated(DocumentEvent e) {
		setDirty(true);
	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {

		// Ensure the margin line position specified is valid.
		int temp;
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
	 * Returns the margin line position text on this panel.
	 *
	 * @return The text in the "margin line position" text field.
	 */
	public int getMarginLinePosition() {
		return marginLinePosition;
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
		return wordWrapCheckBox;
	}


	protected void handleRestoreDefaults() {

		// Note we're a little cheap here and go with RSTA's default font rather
		// than look for fonts in themes.  This is OK since we don't actually
		// set fonts in any of the default themes.
		int defaultMarginLinePosition = RTextArea.getDefaultMarginLinePosition();
		boolean defaultAA = File.separatorChar=='\\';

		if (wordWrapCheckBox.isSelected() ||
			!highlightCurrentLineCheckBox.isSelected() ||
			!marginLineCheckBox.isSelected() ||
			getMarginLinePosition()!=defaultMarginLinePosition ||
			autoInsertClosingCurlyCheckBox.isSelected() ||
			remWhitespaceLinesCheckBox.isSelected() ||
			aaCheckBox.isSelected()!=defaultAA ||
			fractionalMetricsCheckBox.isSelected() ||
			!bracketMatchCheckBox.isSelected() ||
			bothBracketsCB.isSelected()) {
			wordWrapCheckBox.setSelected(false);
			highlightCurrentLineCheckBox.setSelected(true);
			setMarginLineEnabled(true);
			setMarginLinePosition(defaultMarginLinePosition);
			autoInsertClosingCurlyCheckBox.setSelected(false);
			remWhitespaceLinesCheckBox.setSelected(false);
			aaCheckBox.setSelected(defaultAA);
			fractionalMetricsCheckBox.setSelected(false);
			setBracketMatchCheckboxSelected(true);
			bothBracketsCB.setSelected(false);
			setDirty(true);
		}

	}


	/**
	 * Called when a text field in this panel gets updated.
	 */
	@Override
	public void insertUpdate(DocumentEvent e) {
		doDocumentUpdated(e);
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
	 * Sets whether the bracket match color checkbox is selected.
	 *
	 * @param selected Whether the checkbox is selected.
	 */
	public void setBracketMatchCheckboxSelected(boolean selected) {
		bracketMatchCheckBox.setSelected(selected);
		bothBracketsCB.setEnabled(selected);
	}


	/**
	 * Sets whether the margin line stuff is enabled (i.e.,
	 * whether the "Margin line" checkbox is checked).
	 *
	 * @param enabled Whether the margin line options should be
	 *        enabled.
	 */
	private void setMarginLineEnabled(boolean enabled) {
		marginLineCheckBox.setSelected(enabled);
		marginLinePositionField.setEnabled(enabled);
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
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are not handled.
	 *
	 * @param owner The parent application.
	 * @see #setValues(Frame)
	 */
	@Override
	protected void setValuesImpl(Frame owner) {

		RText rtext = (RText)owner;

		// Iniitialize the shared preview context if this is the first pass.
		// Otherwise we'll get NPE's
		EditorOptionsPreviewContext previewContext = EditorOptionsPreviewContext.get();
		if (previewContext.getSyntaxScheme() == null) {
			previewContext.initialize(rtext);
		}

		AbstractMainView mainView = rtext.getMainView();
		wordWrapCheckBox.setSelected(mainView.getLineWrap());
		highlightCurrentLineCheckBox.setSelected(mainView.isCurrentLineHighlightEnabled());
		setMarginLineEnabled(mainView.isMarginLineEnabled());
		setMarginLinePosition(mainView.getMarginLinePosition());
		remWhitespaceLinesCheckBox.setSelected(!mainView.getRememberWhitespaceLines());
		autoInsertClosingCurlyCheckBox.setSelected(mainView.getAutoInsertClosingCurlys());
		aaCheckBox.setSelected(mainView.isAntiAliasEnabled());
		fractionalMetricsCheckBox.setSelected(mainView.isFractionalFontMetricsEnabled());
		setBracketMatchCheckboxSelected(mainView.isBracketMatchingEnabled());
		bothBracketsCB.setSelected(mainView.getMatchBothBrackets());

		syncEditorOptionsPreviewContext();
	}


	@Override
	protected void syncEditorOptionsPreviewContext() {
		EditorOptionsPreviewContext context = EditorOptionsPreviewContext.get();
		context.setWordWrap(wordWrapCheckBox.isSelected());
		context.setHighlightCurrentLine(highlightCurrentLineCheckBox.isSelected());
		context.setMarginLineEnabled(marginLineCheckBox.isSelected());
		context.setMarginLinePosition(getMarginLinePosition());
		context.setAutoInsertClosingCurly(autoInsertClosingCurlyCheckBox.isSelected());
		context.setClearWhitespaceLines(remWhitespaceLinesCheckBox.isSelected());
		context.setAntiAliasingEnabled(aaCheckBox.isSelected());
		context.setFractionalFontMetricsEnabled(fractionalMetricsCheckBox.isSelected());
		context.setHighlightMatchingBrackets(bracketMatchCheckBox.isSelected());
		context.setHighlightBothBrackets(bothBracketsCB.isSelected());
	}


}
