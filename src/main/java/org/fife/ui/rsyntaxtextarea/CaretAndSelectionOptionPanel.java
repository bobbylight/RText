/*
 * 03/20/2006
 *
 * CaretAndSelectionOptionPanel.java - Contains options relating to the caret
 * and selection.
 * Copyright (C) 2006 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.RColorButton;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.UIUtil;
import org.fife.ui.rtextarea.CaretStyle;
import org.fife.ui.rtextarea.ConfigurableCaret;
import org.fife.ui.rtextarea.RTextArea;


/**
 * Option panel for caret and selection options.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class CaretAndSelectionOptionPanel extends AbstractTextAreaOptionPanel
		implements ChangeListener, PropertyChangeListener {

	private JComboBox<String> insCaretCombo;
	private JComboBox<String> overCaretCombo;
	private JSpinner blinkRateSpinner;
	private RColorSwatchesButton caretColorButton;
	private RColorSwatchesButton selColorButton;
	private JCheckBox selectedTextColorCB;
	private RColorSwatchesButton selectedTextColorButton;
	private JButton systemSelectionButton;


	/**
	 * Constructor.
	 */
	public CaretAndSelectionOptionPanel() {

		ComponentOrientation o = ComponentOrientation.
									getOrientation(getLocale());

		setName(MSG.getString("Title.CaretAndSelection"));

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		// We'll add everything to this panel, then add this panel so that
		// stuff stays at the "top."
		Box topPanel = Box.createVerticalBox();

		topPanel.add(createOverridePanel());
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		topPanel.add(createCaretPanel(o));
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		topPanel.add(createSelectionPanel(o));
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		// Create a panel containing the preview and "Restore Defaults"
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(new PreviewPanel(MSG, 9, 40));
		bottomPanel.add(createRestoreDefaultsPanel(), BorderLayout.SOUTH);
		topPanel.add(bottomPanel);

		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(o);

	}


	/**
	 * Listens for actions in this panel.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		Object source = e.getSource();

		if ("InsertCaretCombo".equals(command)) {
			setDirty(true);
		}

		else if ("OverwriteCaretCombo".equals(command)) {
			setDirty(true);
		}

		else if (selectedTextColorCB==source) {
			boolean selected = ((JCheckBox)source).isSelected();
			setSelectedTextColorEnabled(selected);
			setDirty(true);
		}

		else if (systemSelectionButton==source) {
			// The only foolproof, cross-LAF way to get these colors is to
			// grab them from a component, unfortunately.
			JTextArea textArea = new JTextArea();
			Color systemSelectionColor = textArea.getSelectionColor();
			Color selectedTextColor = textArea.getSelectedTextColor();
			if (!systemSelectionColor.equals(selColorButton.getColor()) ||
					!selectedTextColorCB.isSelected() ||
					!selectedTextColorButton.getColor().equals(selectedTextColor)) {
				selColorButton.setColor(systemSelectionColor);
				setSelectedTextColorEnabled(true);
				selectedTextColorButton.setColor(selectedTextColor);
				setDirty(true);
			}
		}

		else {
			super.actionPerformed(e);
		}
	}


	/**
	 * Creates a combo box with caret choices.
	 *
	 * @return The combo box.
	 */
	private static JComboBox<String> createCaretComboBox() {
		JComboBox<String> combo = new JComboBox<>();
		UIUtil.fixComboOrientation(combo);
		combo.addItem(MSG.getString("CaretVerticalLine"));
		combo.addItem(MSG.getString("CaretUnderline"));
		combo.addItem(MSG.getString("CaretBlock"));
		combo.addItem(MSG.getString("CaretRectangle"));
		combo.addItem(MSG.getString("CaretThickVerticalLine"));
		return combo;
	}


	/**
	 * Creates a panel containing caret-related options.
	 *
	 * @param o The component orientation to apply.
	 * @return The panel.
	 */
	private Box createCaretPanel(ComponentOrientation o) {

		Box caretPanel = Box.createVerticalBox();
		caretPanel.setBorder(new OptionPanelBorder(MSG.getString("Carets")));

		JPanel temp = new JPanel(new SpringLayout());

		JLabel insLabel = new JLabel(MSG.getString("InsertCaret"));
		insCaretCombo = createCaretComboBox();
		insCaretCombo.setActionCommand("InsertCaretCombo");
		insCaretCombo.addActionListener(this);
		insLabel.setLabelFor(insCaretCombo);

		JLabel overLabel = new JLabel(MSG.getString("OverwriteCaret"));
		overCaretCombo = createCaretComboBox();
		overCaretCombo.setActionCommand("OverwriteCaretCombo");
		overCaretCombo.addActionListener(this);
		overLabel.setLabelFor(overCaretCombo);

		JLabel caretDelayLabel = new JLabel(MSG.getString("BlinkRate"));
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(500, 0,10000, 50);
		blinkRateSpinner = new JSpinner(spinnerModel);
		blinkRateSpinner.addChangeListener(this);
		caretDelayLabel.setLabelFor(blinkRateSpinner);

		JLabel caretColorLabel = new JLabel(MSG.getString("Color"));
		caretColorButton = new RColorSwatchesButton();
		caretColorButton.addPropertyChangeListener(
			RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		caretColorLabel.setLabelFor(caretColorButton);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(caretColorButton, BorderLayout.LINE_START);

		UIUtil.addLabelValuePairs(temp, o,
			insLabel, insCaretCombo,
			caretDelayLabel, blinkRateSpinner,
			overLabel, overCaretCombo,
			caretColorLabel, buttonPanel);
		UIUtil.makeSpringCompactGrid(temp, 2,4, 0,0, 20,8);
		caretPanel.add(temp);

		return caretPanel;
	}


	/**
	 * Creates a panel containing the selection-related options.
	 *
	 * @param o The component orientation.
	 * @return The panel.
	 */
	private Box createSelectionPanel(ComponentOrientation o) {

		Box p = Box.createVerticalBox();
		p.setBorder(new OptionPanelBorder(MSG.getString("Selection")));

		selColorButton = new RColorSwatchesButton();
		selColorButton.addPropertyChangeListener(
					RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		JLabel selLabel = new JLabel(MSG.getString("SelColor"));
		selLabel.setLabelFor(selColorButton);

		selectedTextColorCB = new JCheckBox(MSG.getString("SelectedTextColor"));
		selectedTextColorCB.addActionListener(this);
		selectedTextColorButton = new RColorSwatchesButton();
		selectedTextColorButton.addPropertyChangeListener(
				RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);

		JPanel selectionColorsPanel = new JPanel(new SpringLayout());
		UIUtil.addLabelValuePairs(selectionColorsPanel, o,
			selLabel, selColorButton,
			selectedTextColorCB, selectedTextColorButton);
		UIUtil.makeSpringCompactGrid(selectionColorsPanel, 2, 2, 0, 0, 5, 5);
		addLeftAligned(p, selectionColorsPanel, 0, 20);

		systemSelectionButton = new JButton(MSG.getString("SystemSelection"));
		systemSelectionButton.addActionListener(this);
		JPanel systemSelectionPanel = new JPanel(new BorderLayout());
		systemSelectionPanel.add(systemSelectionButton, BorderLayout.NORTH);

		JPanel mainSelectionPanel = new JPanel(new BorderLayout());
		mainSelectionPanel.add(selectionColorsPanel, BorderLayout.LINE_START);
		mainSelectionPanel.add(systemSelectionPanel, BorderLayout.LINE_END);
		p.add(mainSelectionPanel);

		return p;

	}


	@Override
	protected void doApplyImpl(Frame owner) {

		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		mainView.setOverrideEditorStyles(overrideCheckBox.isSelected());

		if (overrideCheckBox.isSelected()) {
			mainView.setCaretColor(caretColorButton.getColor());
			mainView.setSelectionColor(getColor(selColorButton));
			mainView.setCaretStyle(RTextArea.INSERT_MODE, getCaretStyle(RTextArea.INSERT_MODE));
			mainView.setCaretStyle(RTextArea.OVERWRITE_MODE, getCaretStyle(RTextArea.OVERWRITE_MODE));
			mainView.setCaretBlinkRate((Integer)blinkRateSpinner.getValue());
			mainView.setSelectedTextColor(getColor(selectedTextColorButton));
			mainView.setUseSelectedTextColor(selectedTextColorCB.isSelected());
		}
		else {
			Theme editorTheme = EditorOptionsPreviewContext.get().getEditorTheme(rtext);
			mainView.setCaretColor(editorTheme.caretColor);
			mainView.setSelectionColor(editorTheme.selectionBG);
			mainView.setCaretStyle(RTextArea.INSERT_MODE, CaretStyle.THICK_VERTICAL_LINE_STYLE);
			mainView.setCaretStyle(RTextArea.OVERWRITE_MODE, CaretStyle.BLOCK_STYLE);
			mainView.setCaretBlinkRate(500);
			mainView.setSelectedTextColor(editorTheme.selectionFG);
			mainView.setUseSelectedTextColor(editorTheme.useSelectionFG);
		}
	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * Returns the caret style for either the insert or overwrite caret
	 * that the user chose.
	 *
	 * @param mode Either <code>RTextArea.INSERT_MODE</code> or
	 *        <code>RTextArea.OVERWRITE_MODE</code>.
	 * @return The style of that caret, such as
	 *        <code>CaretStyle.VERTICAL_LINE_STYLE</code>.
	 * @see ConfigurableCaret
	 */
	public CaretStyle getCaretStyle(int mode) {
		int ordinal;
		if (mode==RTextArea.INSERT_MODE) {
			ordinal = insCaretCombo.getSelectedIndex();
		}
		else {
			ordinal = overCaretCombo.getSelectedIndex(); // OVERWRITE_MODE
		}
		return CaretStyle.values()[ordinal];
	}


	/**
	 * Returns the color displayed, as a <code>Color</code> and not a
	 * <code>ColorUIResource</code>, to prevent LookAndFeel changes from
	 * overriding them when they are installed.
	 *
	 * @param button The button.
	 * @return The color displayed by the button.
	 */
	private static Color getColor(RColorSwatchesButton button) {
		return new Color(button.getColor().getRGB());
	}


	@Override
	protected void handleRestoreDefaults() {

		// This panel's defaults are based on the current theme.
		RText app = (RText)getOptionsDialog().getParent();
		EditorOptionsPreviewContext editorContext = EditorOptionsPreviewContext.get();
		Theme rstaTheme = editorContext.getEditorTheme(app);

		Color defaultCaretColor = rstaTheme.caretColor;
		Color defaultSelectionColor = rstaTheme.selectionBG;
		boolean defaultSelectedTextColorCBChecked = rstaTheme.useSelectionFG;
		Color defaultSelectedTextColor = rstaTheme.selectionFG;
		CaretStyle defaultInsertCaret = CaretStyle.THICK_VERTICAL_LINE_STYLE;
		CaretStyle defaultOverwriteCaret = CaretStyle.BLOCK_STYLE;
		Integer defaultCaretBlinkRate = 500;

		if (overrideCheckBox.isSelected() ||
			!caretColorButton.getColor().equals(defaultCaretColor) ||
			!getColor(selColorButton).equals(defaultSelectionColor) ||
			getCaretStyle(RTextArea.INSERT_MODE)!=defaultInsertCaret ||
			getCaretStyle(RTextArea.OVERWRITE_MODE)!=defaultOverwriteCaret ||
			!blinkRateSpinner.getValue().equals(defaultCaretBlinkRate) ||
			selectedTextColorCB.isSelected() != defaultSelectedTextColorCBChecked ||
			!selectedTextColorButton.getColor().equals(defaultSelectedTextColor)) {

			overrideCheckBox.setSelected(false);
			setCaretColor(defaultCaretColor);
			setSelectionColor(defaultSelectionColor);
			setCaretStyle(RTextArea.INSERT_MODE, defaultInsertCaret);
			setCaretStyle(RTextArea.OVERWRITE_MODE, defaultOverwriteCaret);
			blinkRateSpinner.setValue(defaultCaretBlinkRate);
			setSelectedTextColorEnabled(defaultSelectedTextColorCBChecked);
			selectedTextColorButton.setColor(Color.white);
			setDirty(true);
		}
	}


	/**
	 * Called when a property changes in an object we're listening to.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		// We need to forward this on to the options dialog, whatever
		// it is, so that the "Apply" button gets updated.
		if (RColorButton.COLOR_CHANGED_PROPERTY.equals(e.getPropertyName())) {
			setDirty(true);
		}
	}


	/**
	 * Sets the blink rate displayed.
	 *
	 * @param blinkRate The blink rate to display.
	 */
	private void setBlinkRate(int blinkRate) {
		blinkRateSpinner.setValue(blinkRate);
	}


	/**
	 * Sets the caret color displayed in this panel.
	 *
	 * @param color The caret color to display.  If <code>null</code> is
	 *        passed in, <code>Color.BLACK</code> is used.
	 */
	private void setCaretColor(Color color) {
		if (color==null) {
			color = Color.BLACK;
		}
		caretColorButton.setColor(color);
	}


	/**
	 * Sets the caret style for either the insert or overwrite caret, as
	 * displayed in this option panel.
	 *
	 * @param mode Either <code>RTextArea.INSERT_MODE</code> or
	 *        <code>RTextArea.OVERWRITE_MODE</code>.
	 * @param style The style for the specified caret, such as
	 *        <code>CaretStyle.VERTICAL_LINE_STYLE</code>.
	 * @see #getCaretStyle(int)
	 */
	private void setCaretStyle(int mode, CaretStyle style) {
		switch (mode) {
			case RTextArea.INSERT_MODE -> insCaretCombo.setSelectedIndex(style.ordinal());
			case RTextArea.OVERWRITE_MODE -> overCaretCombo.setSelectedIndex(style.ordinal());
			default -> throw new IllegalArgumentException("mode must be " + RTextArea.INSERT_MODE + " or " +
				RTextArea.OVERWRITE_MODE);
		}
	}


	@Override
	protected void setComponentsEnabled(boolean enabled, Component... ignore) {

		super.setComponentsEnabled(enabled, ignore);

		// These components ignore the global change above since they have additional
		// conditions to check to determine whether they are enabled
		selectedTextColorButton.setEnabled(enabled && selectedTextColorCB.isSelected());
	}


	/**
	 * Sets the selection color displayed in this panel.
	 *
	 * @param color The selection color to display.
	 *        <code>null</code> is passed in, nothing happens.
	 */
	private void setSelectionColor(Color color) {
		if (color!=null)
			selColorButton.setColor(color);
	}


	private void setSelectedTextColorEnabled(boolean enabled) {
		selectedTextColorCB.setSelected(enabled);
		selectedTextColorButton.setEnabled(enabled);
	}


	@Override
	protected void setValuesImpl(Frame owner) {

		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		setCaretColor(mainView.getCaretColor());
		setSelectionColor(mainView.getSelectionColor());
		setCaretStyle(RTextArea.INSERT_MODE, mainView.getCaretStyle(RTextArea.INSERT_MODE));
		setCaretStyle(RTextArea.OVERWRITE_MODE, mainView.getCaretStyle(RTextArea.OVERWRITE_MODE));
		setBlinkRate(mainView.getCaretBlinkRate());
		setSelectedTextColorEnabled(mainView.getUseSelectedTextColor());
		selectedTextColorButton.setColor(mainView.getSelectedTextColor());

		// Do this after initializing all values above
		overrideCheckBox.setSelected(mainView.getOverrideEditorStyles());
		setComponentsEnabled(overrideCheckBox.isSelected());

		syncEditorOptionsPreviewContext();
	}


	/**
	 * Called when the user changes the caret blink rate spinner value.
	 *
	 * @param e The change event.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		setDirty(true);
	}


	@Override
	protected void syncEditorOptionsPreviewContext() {

		EditorOptionsPreviewContext context = EditorOptionsPreviewContext.get();
		context.setOverrideEditorTheme(overrideCheckBox.isSelected());

		// "Carets" section
		context.setInsertCaret(getCaretStyle(RTextArea.INSERT_MODE));
		context.setOverwriteCaret(getCaretStyle(RTextArea.OVERWRITE_MODE));
		context.setCaretBlinkRate((Integer)blinkRateSpinner.getValue());
		context.setCaretColor(caretColorButton.getColor());

		// "Selection" section
		context.setSelectionColor(getColor(selColorButton));
		context.setSelectedTextColor(getColor(selectedTextColorButton));
		context.setUseSelectedTextColor(selectedTextColorCB.isSelected());
	}
}
