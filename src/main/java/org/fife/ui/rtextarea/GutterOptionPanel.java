/*
 * 02/22/2009
 *
 * GutterOptionPanel.java - Options for configuring the Gutter.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextarea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.FontSelector;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.UIUtil;


/**
 * Option panel for the text area gutter.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class GutterOptionPanel extends OptionsDialogPanel
		implements PropertyChangeListener, ActionListener, ItemListener {

	private JCheckBox lnEnabledCB;
	private FontSelector fontSelector;
	private RColorSwatchesButton lnColorButton;
	private JCheckBox enableBookmarkingCB;
	private RColorSwatchesButton borderColorButton;

	private static final String PROPERTY = "property";


	/**
	 * Constructor.
	 */
	public GutterOptionPanel() {

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		ResourceBundle msg = ResourceBundle.getBundle(
					"org.fife.ui.rtextarea.GutterOptionPanel");

		setName(msg.getString("Title"));

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		// We'll add everything to this panel, then add this panel so that
		// stuff stays at the "top."
		Box topPanel = new Box(BoxLayout.Y_AXIS);

		// Line number options.
		Box lineNumbersPanel = new Box(BoxLayout.Y_AXIS);
		lineNumbersPanel.setBorder(new OptionPanelBorder(
									msg.getString("LineNumbers")));
		lnEnabledCB = new JCheckBox(msg.getString("Enabled"));
		lnEnabledCB.addItemListener(this);
		JComponent temp = new JPanel(new BorderLayout());
		temp.add(lnEnabledCB, BorderLayout.LINE_START);
		lineNumbersPanel.add(temp);
		JPanel fontPanel = new JPanel(new BorderLayout());
		fontSelector = new FontSelector();
		fontSelector.setColorSelectable(true);
		fontSelector.addPropertyChangeListener(FontSelector.FONT_PROPERTY, this);
		fontSelector.addPropertyChangeListener(FontSelector.FONT_COLOR_PROPERTY, this);
		fontPanel.add(fontSelector);
		lineNumbersPanel.add(fontPanel);
		lineNumbersPanel.add(Box.createVerticalStrut(5));
		JLabel lnColorLabel = new JLabel(msg.getString("Color"));
		lnColorButton = new RColorSwatchesButton();
		lnColorButton.addPropertyChangeListener(
					RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		lnColorLabel.setLabelFor(lnColorButton);
		temp = new Box(BoxLayout.LINE_AXIS);
		temp.add(lnColorLabel);
		temp.add(Box.createHorizontalStrut(5));
		temp.add(lnColorButton);
		temp.add(Box.createVerticalGlue());
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(temp, BorderLayout.LINE_START);
		lineNumbersPanel.add(temp2);
		topPanel.add(lineNumbersPanel);
		topPanel.add(Box.createVerticalStrut(5));

		// Miscellaneous options
		Box otherPanel = new Box(BoxLayout.Y_AXIS);
		otherPanel.setBorder(new OptionPanelBorder(msg.getString("Other")));
		JLabel borderColorLabel = new JLabel(msg.getString("BorderColor"));
		borderColorButton = new RColorSwatchesButton();
		borderColorButton.addPropertyChangeListener(
					RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		borderColorLabel.setLabelFor(borderColorButton);
		temp = new Box(BoxLayout.LINE_AXIS);
		temp.add(borderColorLabel);
		temp.add(Box.createHorizontalStrut(5));
		temp.add(borderColorButton);
		temp.add(Box.createVerticalGlue());
		temp2 = new JPanel(new BorderLayout());
		temp2.add(temp, BorderLayout.LINE_START);
		otherPanel.add(temp2);
		enableBookmarkingCB = new JCheckBox(msg.getString("EnableBookmarks"));
		enableBookmarkingCB.addItemListener(this);
		temp = new JPanel(new BorderLayout());
		temp.add(enableBookmarkingCB, BorderLayout.LINE_START);
		otherPanel.add(temp);
		topPanel.add(otherPanel);
		topPanel.add(Box.createVerticalStrut(5));

		temp = new Box(BoxLayout.LINE_AXIS);
		JButton restoreDefaultsButton = new JButton(
									msg.getString("RestoreDefaults"));
		restoreDefaultsButton.setActionCommand("RestoreDefaults");
		restoreDefaultsButton.addActionListener(this);
		temp.add(restoreDefaultsButton);
		temp.add(Box.createHorizontalGlue());
		topPanel.add(temp);

		topPanel.add(Box.createVerticalGlue());
		add(topPanel, BorderLayout.NORTH);

		applyComponentOrientation(orientation);

	}


	/**
	 * Called when an action is performed in this panel.
	 *
	 * @param e The event.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if ("RestoreDefaults".equals(command)) {
			Font defaultFont = RTextArea.getDefaultFont();
			Color defBorderColor = new Color(221, 221, 221);
			if (!lnEnabledCB.isSelected() ||
					!defaultFont.equals(fontSelector.getDisplayedFont()) ||
					!Color.GRAY.equals(lnColorButton.getColor()) ||
					!enableBookmarkingCB.isSelected() ||
					!defBorderColor.equals(borderColorButton.getColor())) {
				lnEnabledCB.setSelected(true);
				fontSelector.setDisplayedFont(defaultFont, false);
				lnColorButton.setColor(Color.GRAY);
				enableBookmarkingCB.setSelected(true);
				borderColorButton.setColor(new Color(221, 221, 221));
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, false, true);
			}
		}

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
		mainView.setLineNumbersEnabled(lnEnabledCB.isSelected());
		mainView.setLineNumberFont(fontSelector.getDisplayedFont());
		mainView.setLineNumberColor(lnColorButton.getColor());
		mainView.setGutterBorderColor(borderColorButton.getColor());
		mainView.setBookmarksEnabled(enableBookmarkingCB.isSelected());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
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
		return lnEnabledCB;
	}


	/**
	 * Called when a check box is selected or deselected.
	 *
	 * @param e The event.
	 */
	public void itemStateChanged(ItemEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(PROPERTY, false, true);
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
		lnEnabledCB.setSelected(mainView.getLineNumbersEnabled());
		fontSelector.setDisplayedFont(mainView.getLineNumberFont(), false);
		lnColorButton.setColor(mainView.getLineNumberColor());
		borderColorButton.setColor(mainView.getGutterBorderColor());
		enableBookmarkingCB.setSelected(mainView.getBookmarksEnabled());
	}


}