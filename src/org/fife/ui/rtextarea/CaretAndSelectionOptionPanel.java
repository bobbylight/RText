/*
 * 03/20/2006
 *
 * CaretAndSelectionOptionPanel.java - Contains options relating to the caret
 * and selection.
 * Copyright (C) 2006 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextarea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RColorButton;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.ConfigurableCaret;
import org.fife.ui.rtextarea.RTextArea;


/**
 * Option panel for caret and selection options.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class CaretAndSelectionOptionPanel extends OptionsDialogPanel
		implements ActionListener, ChangeListener, PropertyChangeListener {

	private JComboBox insCaretCombo;
	private JComboBox overCaretCombo;
	private JSpinner blinkRateSpinner;
	private RColorSwatchesButton caretColorButton;
	private RColorSwatchesButton selColorButton;
	private RColorSwatchesButton markAllColorButton;
	private JCheckBox selectedTextColorCB;
	private RColorSwatchesButton selectedTextColorButton;
	private JCheckBox roundedSelCheckBox;
	private JButton systemSelectionButton;
	private JCheckBox enableMOCheckBox;
	private RColorSwatchesButton moColorButton;
	private JCheckBox secLangCB;
	private JLabel[] secLangLabels;
	private RColorSwatchesButton[] secLangButtons;

	private static final int SEC_LANG_COUNT		= 3;
	private static final String PROPERTY		= "property";


	/**
	 * Constructor.
	 */
	public CaretAndSelectionOptionPanel() {

		ComponentOrientation o = ComponentOrientation.
									getOrientation(getLocale());

		ResourceBundle msg = ResourceBundle.getBundle(
					"org.fife.ui.rtextarea.CaretAndSelectionOptionPanel");

		setName(msg.getString("Title"));

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		// We'll add everything to this panel, then add this panel so that
		// stuff stays at the "top."
		Box topPanel = Box.createVerticalBox();

		Box caretPanel = Box.createVerticalBox();
		caretPanel.setBorder(new OptionPanelBorder(msg.getString("Carets")));
		JPanel temp = new JPanel(new SpringLayout());
		JLabel insLabel = new JLabel(msg.getString("InsertCaret"));
		insCaretCombo = createCaretComboBox(msg);
		insCaretCombo.setActionCommand("InsertCaretCombo");
		insCaretCombo.addActionListener(this);
		insLabel.setLabelFor(insCaretCombo);
		JLabel overLabel = new JLabel(msg.getString("OverwriteCaret"));
		overCaretCombo = createCaretComboBox(msg);
		overCaretCombo.setActionCommand("OverwriteCaretCombo");
		overCaretCombo.addActionListener(this);
		overLabel.setLabelFor(overCaretCombo);
		JLabel caretDelayLabel = new JLabel(msg.getString("BlinkRate"));
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(500, 0,10000, 50);
		blinkRateSpinner = new JSpinner(spinnerModel);
		blinkRateSpinner.addChangeListener(this);
		caretDelayLabel.setLabelFor(blinkRateSpinner);
		JLabel caretColorLabel = new JLabel(msg.getString("Color"));
		caretColorButton = new RColorSwatchesButton();
		caretColorButton.addPropertyChangeListener(
					RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		caretColorLabel.setLabelFor(caretColorButton);
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(caretColorButton, BorderLayout.LINE_START);
		if (o.isLeftToRight()) {
			temp.add(insLabel);          temp.add(insCaretCombo);
			temp.add(overLabel);         temp.add(overCaretCombo);
			temp.add(caretDelayLabel);   temp.add(blinkRateSpinner);
			temp.add(caretColorLabel);   temp.add(buttonPanel);
		}
		else {
			temp.add(insCaretCombo);     temp.add(insLabel);
			temp.add(overCaretCombo);    temp.add(overLabel);
			temp.add(blinkRateSpinner);  temp.add(caretDelayLabel);
			temp.add(buttonPanel);       temp.add(caretColorLabel);
		}
		UIUtil.makeSpringCompactGrid(temp, 4,2, 0,0, 5,5);
		caretPanel.add(temp);
		caretPanel.add(Box.createVerticalStrut(5));
		topPanel.add(caretPanel);
		topPanel.add(Box.createVerticalStrut(5));

		topPanel.add(createSelectionPanel(msg, o));
		topPanel.add(Box.createVerticalStrut(5));

		topPanel.add(createMarkOccurrencesPanel(msg, o));
		topPanel.add(Box.createVerticalStrut(5));

		topPanel.add(createSecondaryLanguagesPanel(msg, o));
		topPanel.add(Box.createVerticalStrut(10));

		JButton rdButton = new JButton(msg.getString("RestoreDefaults"));
		rdButton.setActionCommand("RestoreDefaults");
		rdButton.addActionListener(this);
		addLeftAligned(topPanel, rdButton);

		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(o);

		msg = null;

	}


	/**
	 * Listens for actions in this panel.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		Object source = e.getSource();

		if ("RestoreDefaults".equals(command)) {

			Color defaultCaretColor = RTextArea.getDefaultCaretColor();
			Color defaultSelectionColor = RSyntaxTextArea.getDefaultSelectionColor();
			Color defaultMarkAllColor = RTextArea.getDefaultMarkAllHighlightColor();
			Color defaultMarkOccurrencesColor = new Color(224, 224, 224);
			Color defaultSelectedTextColor = Color.white;
			int defaultInsertCaret = ConfigurableCaret.THICK_VERTICAL_LINE_STYLE;
			int defaultOverwriteCaret = ConfigurableCaret.BLOCK_STYLE;
			Integer defaultCaretBlinkRate = new Integer(500);
			Color[] defaultSecLangColor = new Color[SEC_LANG_COUNT];
			defaultSecLangColor[0] = new Color(0xfff0cc);
			defaultSecLangColor[1] = new Color(0xdafeda);
			defaultSecLangColor[2] = new Color(0xffe0f0);

			if ( !getCaretColor().equals(defaultCaretColor) ||
				!getSelectionColor().equals(defaultSelectionColor) ||
				!getMarkAllHighlightColor().equals(defaultMarkAllColor) ||
				getCaretStyle(RTextArea.INSERT_MODE)!=defaultInsertCaret ||
				getCaretStyle(RTextArea.OVERWRITE_MODE)!=defaultOverwriteCaret ||
				!blinkRateSpinner.getValue().equals(defaultCaretBlinkRate) ||
				getRoundedSelection()==true ||
				!enableMOCheckBox.isSelected() ||
				!moColorButton.getColor().equals(defaultMarkOccurrencesColor) ||
				selectedTextColorCB.isSelected() ||
				!selectedTextColorButton.getColor().equals(defaultSelectedTextColor) ||
				secLangCB.isSelected() ||
				!defaultSecLangColor[0].equals(secLangButtons[0]) ||
				!defaultSecLangColor[1].equals(secLangButtons[1]) ||
				!defaultSecLangColor[2].equals(secLangButtons[2]))
			{
				setCaretColor(defaultCaretColor);
				setSelectionColor(defaultSelectionColor);
				setMarkAllHighlightColor(defaultMarkAllColor);
				setCaretStyle(RTextArea.INSERT_MODE, defaultInsertCaret);
				setCaretStyle(RTextArea.OVERWRITE_MODE, defaultOverwriteCaret);
				blinkRateSpinner.setValue(defaultCaretBlinkRate);
				setRoundedSelection(false);
				enableMOCheckBox.setSelected(true);
				moColorButton.setEnabled(true);
				moColorButton.setColor(defaultMarkOccurrencesColor);
				setSelectedTextColorEnabled(false);
				selectedTextColorButton.setColor(Color.white);
				setHighlightSecondaryLanguages(false);
				for (int i=0; i<SEC_LANG_COUNT; i++) {
					secLangButtons[i].setColor(defaultSecLangColor[i]);
				}
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, defaultCaretColor);
			}

		}

		else if ("InsertCaretCombo".equals(command)) {
			int style = insCaretCombo.getSelectedIndex();
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, -1, style);
		}

		else if ("OverwriteCaretCombo".equals(command)) {
			int style = overCaretCombo.getSelectedIndex();
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, -1, style);
		}

		else if ("RoundedSelectionCheckBox".equals(command)) {
			boolean selected = roundedSelCheckBox.isSelected();
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

		else if (selectedTextColorCB==source) {
			boolean selected = ((JCheckBox)source).isSelected();
			setSelectedTextColorEnabled(selected);
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

		else if (systemSelectionButton==source) {
			// The only foolproof, cross-LAF way to get these colors is to
			// grab them from a component, unfortunately.
			JTextArea textArea = new JTextArea();
			Color systemSelectionColor = textArea.getSelectionColor();
			Color selectedTextColor = textArea.getSelectedTextColor();
			if (!systemSelectionColor.equals(selColorButton.getColor()) ||
					!selectedTextColorCB.isSelected() ||
					!selectedTextColorButton.equals(selectedTextColor)) {
				selColorButton.setColor(systemSelectionColor);
				setSelectedTextColorEnabled(true);
				selectedTextColorButton.setColor(selectedTextColor);
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, false, true);
			}
		}

		else if ("MarkOccurrences".equals(command)) {
			boolean selected = enableMOCheckBox.isSelected();
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
			moColorButton.setEnabled(selected);
		}

		else if (secLangCB==source) {
			boolean selected = ((JCheckBox)source).isSelected();
			setHighlightSecondaryLanguages(selected);
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

	}


	/**
	 * Creates a combo box with caret choices.
	 *
	 * @param msg The resource bundle with localized caret string values.
	 */
	private static final JComboBox createCaretComboBox(ResourceBundle msg) {
		JComboBox combo = new JComboBox();
		UIUtil.fixComboOrientation(combo);
		combo.addItem(msg.getString("CaretVerticalLine"));
		combo.addItem(msg.getString("CaretUnderline"));
		combo.addItem(msg.getString("CaretBlock"));
		combo.addItem(msg.getString("CaretRectangle"));
		combo.addItem(msg.getString("CaretThickVerticalLine"));
		return combo;
	}


	/**
	 * Creates a panel containing the secondary language-related options.<p>
	 * These should really be elsewhere, but we're getting short on space in
	 * other text editor-related panels.
	 *
	 * @param msg The resource bundle to use for localization.
	 * @param o The component orientation.
	 * @return The panel.
	 */
	private Box createSecondaryLanguagesPanel(ResourceBundle msg,
											ComponentOrientation o) {

		Box p = Box.createVerticalBox();
		p.setBorder(new OptionPanelBorder(msg.getString("SecondaryLanguages")));

		secLangCB =  new JCheckBox(msg.getString("HighlightSecondaryLanguages"));
		secLangCB.addActionListener(this);
		addLeftAligned(p, secLangCB);

		secLangLabels = new JLabel[SEC_LANG_COUNT];
		secLangButtons = new RColorSwatchesButton[SEC_LANG_COUNT];
		for (int i=0; i<SEC_LANG_COUNT; i++) {
			secLangButtons[i] = new RColorSwatchesButton();
			secLangButtons[i].addPropertyChangeListener(
					RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
			secLangLabels[i] = new JLabel(msg.getString(
					"HighlightSecondaryLanguages.Color" + (i+1)));
			secLangLabels[i].setLabelFor(secLangButtons[i]);
		}

		JPanel temp = new JPanel(new SpringLayout());
		if (o.isLeftToRight()) {
			temp.add(secLangLabels[0]);  temp.add(secLangButtons[0]);
			temp.add(Box.createVerticalStrut(20));
			temp.add(secLangLabels[1]);  temp.add(secLangButtons[1]);
			temp.add(Box.createVerticalStrut(20));
			temp.add(secLangLabels[2]);  temp.add(secLangButtons[2]);
		}
		else {
			temp.add(secLangButtons[0]); temp.add(secLangLabels[0]);
			temp.add(Box.createVerticalStrut(20));
			temp.add(secLangButtons[1]);  temp.add(secLangLabels[1]);
			temp.add(Box.createVerticalStrut(20));
			temp.add(secLangButtons[2]);  temp.add(secLangLabels[2]);
		}
		UIUtil.makeSpringCompactGrid(temp, 1,8, 0,0, 5,5);
		addLeftAligned(p, temp, 0, 20);

		return p;

	}


	/**
	 * Creates a panel containing the selection-related options.
	 *
	 * @param msg The resource bundle to use for localization.
	 * @param o The component orientation.
	 * @return The panel.
	 */
	private Box createSelectionPanel(ResourceBundle msg,
									ComponentOrientation o) {

		Box p = Box.createVerticalBox();
		p.setBorder(new OptionPanelBorder(msg.getString("Selection")));

		selColorButton = new RColorSwatchesButton();
		selColorButton.addPropertyChangeListener(
					RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		JLabel selLabel = new JLabel(msg.getString("SelColor"));
		selLabel.setLabelFor(selColorButton);

		markAllColorButton = new RColorSwatchesButton();
		markAllColorButton.addPropertyChangeListener(
					RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		JLabel markAllLabel = new JLabel(msg.getString("MarkAllColor"));
		markAllLabel.setLabelFor(markAllColorButton);

		selectedTextColorCB = new JCheckBox(msg.getString("SelectedTextColor"));
		selectedTextColorCB.addActionListener(this);
		selectedTextColorButton = new RColorSwatchesButton();
		selectedTextColorButton.addPropertyChangeListener(
				RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);

		JPanel contents = new JPanel(new SpringLayout());
		Dimension d = new Dimension(5, 5);
		if (o.isLeftToRight()) {
			contents.add(selLabel); contents.add(selColorButton);
			contents.add(Box.createHorizontalStrut(40));
			contents.add(markAllLabel); contents.add(markAllColorButton);
			contents.add(selectedTextColorCB);
			contents.add(selectedTextColorButton);
			contents.add(Box.createHorizontalStrut(40));
			contents.add(Box.createRigidArea(d));
			contents.add(Box.createRigidArea(d));
		}
		else {
			contents.add(markAllColorButton); contents.add(markAllLabel);
			contents.add(Box.createHorizontalStrut(40));
			contents.add(selColorButton); contents.add(selLabel);
			contents.add(selectedTextColorButton);
			contents.add(selectedTextColorCB);
			contents.add(Box.createHorizontalStrut(40));
			contents.add(Box.createRigidArea(d));
			contents.add(Box.createRigidArea(d));
		}
		UIUtil.makeSpringCompactGrid(contents, 2, 5, 0, 0, 5, 5);
		//addLeftAligned(p, contents);

		systemSelectionButton = new JButton(msg.getString("SystemSelection"));
		systemSelectionButton.addActionListener(this);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(systemSelectionButton, BorderLayout.NORTH);

		JPanel temp = new JPanel(new BorderLayout());
		temp.add(contents, BorderLayout.LINE_START);
		temp.add(temp2, BorderLayout.LINE_END);
		p.add(temp);

		roundedSelCheckBox = new JCheckBox(msg.getString("RoundSel"));
		roundedSelCheckBox.setActionCommand("RoundedSelectionCheckBox");
		roundedSelCheckBox.addActionListener(this);
		addLeftAligned(p, roundedSelCheckBox);

		return p;

	}


	/**
	 * Creates a panel containing the "mark occurrences"-related options.
	 *
	 * @param msg The resource bundle to use for localization.
	 * @param o The component orientation.
	 * @return The panel.
	 */
	private Box createMarkOccurrencesPanel(ResourceBundle msg,
								ComponentOrientation o) {

		Box p = Box.createVerticalBox();
		p.setBorder(new OptionPanelBorder(msg.getString("MarkOccurrences")));

		enableMOCheckBox = new JCheckBox(
							msg.getString("EnableMarkOccurrences"));
		enableMOCheckBox.setActionCommand("MarkOccurrences");
		enableMOCheckBox.addActionListener(this);

		moColorButton = new RColorSwatchesButton();
		moColorButton.addPropertyChangeListener(
					RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);

		Box box = createHorizontalBox();
		box.add(enableMOCheckBox);
		box.add(Box.createHorizontalStrut(5));
		box.add(moColorButton);
		box.add(Box.createHorizontalGlue());
		p.add(box);

		return p;

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doApplyImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		mainView.setCaretColor(getCaretColor());
		mainView.setSelectionColor(getSelectionColor());
		mainView.setMarkAllHighlightColor(getMarkAllHighlightColor());
		mainView.setRoundedSelectionEdges(getRoundedSelection());
		mainView.setCaretStyle(RTextArea.INSERT_MODE, getCaretStyle(RTextArea.INSERT_MODE));
		mainView.setCaretStyle(RTextArea.OVERWRITE_MODE, getCaretStyle(RTextArea.OVERWRITE_MODE));
		mainView.setCaretBlinkRate(getBlinkRate());
		mainView.setMarkOccurrences(enableMOCheckBox.isSelected());
		mainView.setMarkOccurrencesColor(moColorButton.getColor());
		mainView.setSelectedTextColor(getColor(selectedTextColorButton));
		mainView.setUseSelectedTextColor(selectedTextColorCB.isSelected());

		mainView.setHighlightSecondaryLanguages(secLangCB.isSelected());
		for (int i=0; i<SEC_LANG_COUNT; i++) {
			mainView.setSecondaryLanguageColor(i, secLangButtons[i].getColor());
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * Returns the blink rate selected by the user.
	 *
	 * @return The blink rate.
	 */
	public int getBlinkRate() {
		return ((Integer)blinkRateSpinner.getValue()).intValue();
	}


	/**
	 * Returns the color the user chose for the caret.
	 *
	 * @return The caret color the user chose.
	 */
	public Color getCaretColor() {
		return caretColorButton.getColor();
	}


	/**
	 * Returns the caret style for either the insert or overwrite caret
	 * that the user chose.
	 *
	 * @param mode Either <code>RTextArea.INSERT_MODE</code> or
	 *        <code>RTextArea.OVERWRITE_MODE</code>.
	 * @return The style of that caret, such as
	 *        <code>ConfigurableCaret.VERTICAL_LINE_STYLE</code>.
	 * @see org.fife.ui.rtextarea.ConfigurableCaret
	 */
	public int getCaretStyle(int mode) {
		if (mode==RTextArea.INSERT_MODE)
			return insCaretCombo.getSelectedIndex();
		return overCaretCombo.getSelectedIndex(); // OVERWRITE_MODE
	}


	/**
	 * Returns the color displayed, as a <code>Color</code> and not a
	 * <code>ColorUIResource</code>, to prevent LookAndFeel changes from
	 * overriding them when they are installed.
	 *
	 * @param button The button.
	 * @return The color displayed by the button.
	 */
	private static final Color getColor(RColorSwatchesButton button) {
		return new Color(button.getColor().getRGB());
	}


	/**
	 * Returns the color selected by the user for "mark all."
	 *
	 * @return The color.
	 */
	public Color getMarkAllHighlightColor() {
		return markAllColorButton.getColor();
	}


	/**
	 * Returns whether the user selected to use rounded edges on selections.
	 *
	 * @return Whether rounded selection edges were selected.
	 */
	public boolean getRoundedSelection() {
		return roundedSelCheckBox.isSelected();
	}


	/**
	 * Returns the color the user chose for selections.
	 *
	 * @return The selection color the user chose.
	 */
	public Color getSelectionColor() {
		return getColor(selColorButton);
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
		return caretColorButton;
	}


	/**
	 * Called when a property changes in an object we're listening to.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		// We need to forward this on to the options dialog, whatever
		// it is, so that the "Apply" button gets updated.
		if (RColorButton.COLOR_CHANGED_PROPERTY.equals(e.getPropertyName())) {
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, e.getOldValue(), e.getNewValue());
		}
	}


	/**
	 * Sets the blink rate displayed.
	 *
	 * @param blinkRate The blink rate to display.
	 * @see #getBlinkRate()
	 */
	private void setBlinkRate(int blinkRate) {
		blinkRateSpinner.setValue(new Integer(blinkRate));
	}


	/**
	 * Sets the caret color displayed in this panel.
	 *
	 * @param color The caret color to display.  If <code>null</code> is
	 *        passed in, <code>Color.BLACK</code> is used.
	 * @see #getCaretColor()
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
	 *        <code>ConfigurableCaret.VERTICAL_LINE_STYLE</code>.
	 * @see #getCaretStyle(int)
	 */
	private void setCaretStyle(int mode, int style) {
		switch (mode) {
			case RTextArea.INSERT_MODE:
				insCaretCombo.setSelectedIndex(style);
				break;
			case RTextArea.OVERWRITE_MODE:
				overCaretCombo.setSelectedIndex(style);
				break;
			default:
				throw new IllegalArgumentException("mode must be " +
					RTextArea.INSERT_MODE + " or " +
					RTextArea.OVERWRITE_MODE);
		}
	}


	private void setHighlightSecondaryLanguages(boolean highlight) {
		secLangCB.setSelected(highlight);
		for (int i=0; i<SEC_LANG_COUNT; i++) {
			secLangLabels[i].setEnabled(highlight);
			secLangButtons[i].setEnabled(highlight);
		}
	}


	/**
	 * Sets the color selected for "mark all."
	 *
	 * @param color The color to have selected.
	 * @see #getMarkAllHighlightColor()
	 */
	private void setMarkAllHighlightColor(Color color) {
		if (color!=null)
			markAllColorButton.setColor(color);
	}


	/**
	 * Sets whether the rounded selection check box is selected.
	 *
	 * @param selected Whether the check box is selected.
	 * @see #getRoundedSelection
	 */
	private void setRoundedSelection(boolean selected) {
		roundedSelCheckBox.setSelected(selected);
	}


	/**
	 * Sets the selection color displayed in this panel.
	 *
	 * @param color The selection color to display.
	 *        <code>null</code> is passed in, nothing happens.
	 * @see #getSelectionColor()
	 */
	private void setSelectionColor(Color color) {
		if (color!=null)
			selColorButton.setColor(color);
	}


	private void setSelectedTextColorEnabled(boolean enabled) {
		selectedTextColorCB.setSelected(enabled);
		selectedTextColorButton.setEnabled(enabled);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setValuesImpl(Frame owner) {

		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		setCaretColor(mainView.getCaretColor());
		setSelectionColor(mainView.getSelectionColor());
		setMarkAllHighlightColor(mainView.getMarkAllHighlightColor());
		setRoundedSelection(mainView.getRoundedSelectionEdges());
		setCaretStyle(RTextArea.INSERT_MODE, mainView.getCaretStyle(RTextArea.INSERT_MODE));
		setCaretStyle(RTextArea.OVERWRITE_MODE, mainView.getCaretStyle(RTextArea.OVERWRITE_MODE));
		setBlinkRate(mainView.getCaretBlinkRate());
		enableMOCheckBox.setSelected(mainView.getMarkOccurrences());
		moColorButton.setEnabled(enableMOCheckBox.isSelected());
		moColorButton.setColor(mainView.getMarkOccurrencesColor());
		setSelectedTextColorEnabled(mainView.getUseSelectedTextColor());
		selectedTextColorButton.setColor(mainView.getSelectedTextColor());

		setHighlightSecondaryLanguages(mainView.getHighlightSecondaryLanguages());
		for (int i=0; i<SEC_LANG_COUNT; i++) {
			secLangButtons[i].setColor(mainView.getSecondaryLanguageColor(i));
		}

	}


	/**
	 * Called when the user changes the caret blink rate spinner value.
	 *
	 * @param e The change event.
	 */
	public void stateChanged(ChangeEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(PROPERTY, null, blinkRateSpinner.getValue());
	}


}