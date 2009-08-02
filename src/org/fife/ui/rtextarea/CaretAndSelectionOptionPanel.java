/*
 * 03/20/2006
 *
 * CaretAndSelectionOptionPanel.java - Contains options relating to the caret
 * and selection.
 * Copyright (C) 2006 Robert Futrell
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RButton;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.ConfigurableCaret;
import org.fife.ui.rtextarea.RTextArea;


/**
 * Option panel for caret and selectionn options.
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
	private JCheckBox roundedSelCheckBox;
	private JCheckBox enableMOCheckBox;
	private JLabel moColorLabel;
	private RColorSwatchesButton moColorButton;

	private static final String PROPERTY		= "property";


	/**
	 * Constructor.
	 */
	public CaretAndSelectionOptionPanel() {

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		ResourceBundle msg = ResourceBundle.getBundle(
					"org.fife.ui.rtextarea.CaretAndSelectionOptionPanel");

		setName(msg.getString("Title"));

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		// We'll add everything to this panel, then add this panel so that
		// stuff stays at the "top."
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		JPanel caretPanel = new JPanel();
		caretPanel.setLayout(new BoxLayout(caretPanel, BoxLayout.Y_AXIS));
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
		if (orientation.isLeftToRight()) {
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

		JPanel selectionPanel = createSelectionPanel(msg, orientation);
		topPanel.add(selectionPanel);
		topPanel.add(Box.createVerticalStrut(5));

		JPanel markOccPanel = createMarkOccurrencesPanel(msg, orientation);
		topPanel.add(markOccPanel);
		topPanel.add(Box.createVerticalStrut(5));

		JPanel rdPanel = new JPanel();
		rdPanel.setLayout(new BoxLayout(rdPanel, BoxLayout.LINE_AXIS));
		RButton restoreDefaultsButton = new RButton(
									msg.getString("RestoreDefaults"));
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

		// If the user clicked the "restore defaults" button.
		if (command.equals("RestoreDefaults")) {

			Color defaultCaretColor = RTextArea.getDefaultCaretColor();
			Color defaultSelectionColor = RSyntaxTextArea.getDefaultSelectionColor();
			Color defaultMarkAllColor = RTextArea.getDefaultMarkAllHighlightColor();
			Color defaultMarkOccurrencesColor = new Color(224, 224, 224);
			int defaultInsertCaret = ConfigurableCaret.THICK_VERTICAL_LINE_STYLE;
			int defaultOverwriteCaret = ConfigurableCaret.BLOCK_STYLE;
			Integer defaultCaretBlinkRate = new Integer(500);

			if ( !getCaretColor().equals(defaultCaretColor) ||
				!getSelectionColor().equals(defaultSelectionColor) ||
				!getMarkAllHighlightColor().equals(defaultMarkAllColor) ||
				getCaretStyle(RTextArea.INSERT_MODE)!=defaultInsertCaret ||
				getCaretStyle(RTextArea.OVERWRITE_MODE)!=defaultOverwriteCaret ||
				!blinkRateSpinner.getValue().equals(defaultCaretBlinkRate) ||
				getRoundedSelection()==true ||
				!enableMOCheckBox.isSelected() ||
				!moColorButton.getColor().equals(defaultMarkOccurrencesColor))
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
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, defaultCaretColor);
			}

		}

		// Change the insert caret.
		else if (command.equals("InsertCaretCombo")) {
			int style = insCaretCombo.getSelectedIndex();
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, -1, style);
		}

		// Change the overwrite caret.
		else if (command.equals("OverwriteCaretCombo")) {
			int style = overCaretCombo.getSelectedIndex();
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, -1, style);
		}

		// Toggle whether to use rounded edges for selections.
		else if (command.equals("RoundedSelectionCheckBox")) {
			boolean selected = roundedSelCheckBox.isSelected();
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

		else if (command.equals("MarkOccurrences")) {
			boolean selected = enableMOCheckBox.isSelected();
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
			moColorLabel.setEnabled(selected);
			moColorButton.setEnabled(selected);
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
	 * Creates a panel containing the selection-related options.
	 *
	 * @param msg The resource bundle to use for localization.
	 * @param o The component orientation.
	 * @return The panel.
	 */
	private JPanel createSelectionPanel(ResourceBundle msg,
								ComponentOrientation o) {

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
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

		JPanel temp = new JPanel(new SpringLayout());
		if (o.isLeftToRight()) {
			temp.add(selLabel);     temp.add(selColorButton);
			temp.add(markAllLabel); temp.add(markAllColorButton);
		}
		else {
			temp.add(selColorButton);     temp.add(selLabel);
			temp.add(markAllColorButton); temp.add(markAllLabel);
		}
		UIUtil.makeSpringCompactGrid(temp, 2,2, 0,0, 5,5);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(temp, BorderLayout.LINE_START);
		p.add(temp2);

		temp = new JPanel();
		temp.setLayout(new BoxLayout(temp, BoxLayout.LINE_AXIS));
		roundedSelCheckBox = new JCheckBox(msg.getString("RoundSel"));
		roundedSelCheckBox.setActionCommand("RoundedSelectionCheckBox");
		roundedSelCheckBox.addActionListener(this);
		temp.add(roundedSelCheckBox);
		temp.add(Box.createHorizontalGlue());
		p.add(temp);

		return p;

	}


	/**
	 * Creates a panel containing the "mark occurrences"-related options.
	 *
	 * @param msg The resource bundle to use for localization.
	 * @param o The component orientation.
	 * @return The panel.
	 */
	private JPanel createMarkOccurrencesPanel(ResourceBundle msg,
								ComponentOrientation o) {

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(new OptionPanelBorder(msg.getString("MarkOccurrences")));

		enableMOCheckBox = new JCheckBox(
							msg.getString("EnableMarkOccurrences"));
		enableMOCheckBox.setActionCommand("MarkOccurrences");
		enableMOCheckBox.addActionListener(this);
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(enableMOCheckBox, BorderLayout.LINE_START);
		p.add(temp);
		
		moColorButton = new RColorSwatchesButton();
		moColorButton.addPropertyChangeListener(
					RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		moColorLabel = new JLabel(msg.getString("Color"));
		moColorLabel.setLabelFor(moColorButton);
		temp = new JPanel();
		temp.setLayout(new BoxLayout(temp, BoxLayout.LINE_AXIS));
		temp.add(Box.createHorizontalStrut(20));
		temp.add(moColorLabel);
		temp.add(Box.createHorizontalStrut(5));
		temp.add(moColorButton);
		temp.add(Box.createHorizontalGlue());
		p.add(temp);

		return p;

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
		mainView.setCaretColor(getCaretColor());
		mainView.setSelectionColor(getSelectionColor());
		mainView.setMarkAllHighlightColor(getMarkAllHighlightColor());
		mainView.setRoundedSelectionEdges(getRoundedSelection());
		mainView.setCaretStyle(RTextArea.INSERT_MODE, getCaretStyle(RTextArea.INSERT_MODE));
		mainView.setCaretStyle(RTextArea.OVERWRITE_MODE, getCaretStyle(RTextArea.OVERWRITE_MODE));
		mainView.setCaretBlinkRate(getBlinkRate());
		mainView.setMarkOccurrences(enableMOCheckBox.isSelected());
		mainView.setMarkOccurrencesColor(moColorButton.getColor());
	}


	/**
	 * Checks whether or not all input the user specified on this panel is
	 * valid.
	 *
	 * @return <code>null</code> always, as the user cannot enter invalid
	 *         data into this panel.
	 */
	public OptionsPanelCheckResult ensureValidInputs() {
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
		return selColorButton.getColor();
	}


	/**
	 * Returns the <code>JComponent</code> at the "top" of this Options
	 * panel.  This is the component that will receive focus if the user
	 * switches to this Options panel in the Options dialog.  As an added
	 * bonus, if this component is a <code>JTextComponent</code>, its
	 * text is selected for easy changing.
	 */
	public JComponent getTopJComponent() {
		return caretColorButton;
	}


	/**
	 * Called when a property changes in an object we're listening to.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		Object source = e.getSource();

		// We need to forward this on to the options dialog, whatever
		// it is, so that the "Apply" button gets updated.

		if (source==caretColorButton || source==selColorButton ||
				source==markAllColorButton || source==moColorButton) {
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, e.getOldValue(), e.getNewValue());
		}

	}


	/**
	 * Sets the blink rate displayed.
	 *
	 * @param blinkRate The blink rate to display.
	 * @see #getBlinkRate
	 */
	private void setBlinkRate(int blinkRate) {
		blinkRateSpinner.setValue(new Integer(blinkRate));
	}


	/**
	 * Sets the caret color displayed in this panel.
	 *
	 * @param color The caret color to display.  If <code>null</code> is
	 *        passed in, <code>Color.BLACK</code> is used.
	 * @see #getCaretColor
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
	 * @see #getCaretStyle
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


	/**
	 * Sets the color selected for "mark all."
	 *
	 * @param color The color to have selected.
	 * @see #getMarkAllHighlightColor
	 */
	private void setMarkAllHighlightColor(Color color) {
		if (color!=null)
			markAllColorButton.setColor(color);
	}


	/**
	 * Sets whether the rounded selection checkbox is selected.
	 *
	 * @param selected Whether the checkbox is selected.
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
	 * @see #getSelectionColor
	 */
	private void setSelectionColor(final Color color) {
		if (color!=null)
			selColorButton.setColor(color);
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
		setCaretColor(mainView.getCaretColor());
		setSelectionColor(mainView.getSelectionColor());
		setMarkAllHighlightColor(mainView.getMarkAllHighlightColor());
		setRoundedSelection(mainView.getRoundedSelectionEdges());
		setCaretStyle(RTextArea.INSERT_MODE, mainView.getCaretStyle(RTextArea.INSERT_MODE));
		setCaretStyle(RTextArea.OVERWRITE_MODE, mainView.getCaretStyle(RTextArea.OVERWRITE_MODE));
		setBlinkRate(mainView.getCaretBlinkRate());
		enableMOCheckBox.setSelected(mainView.getMarkOccurrences());
		moColorLabel.setEnabled(enableMOCheckBox.isSelected());
		moColorButton.setEnabled(enableMOCheckBox.isSelected());
		moColorButton.setColor(mainView.getMarkOccurrencesColor());
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