/*
 * 02/22/2009
 *
 * GutterOptionPanel.java - Options for configuring the Gutter.
 * Copyright (C) 2009 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

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
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.*;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextAppThemes;
import org.fife.ui.FontSelector;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.UIUtil;
import org.fife.ui.rtextarea.RTextArea;


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
	private RColorSwatchesButton foldBackgroundButton;
	private RColorSwatchesButton armedFoldBackgroundButton;


	/**
	 * Constructor.
	 */
	public GutterOptionPanel() {

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		ResourceBundle msg = ResourceBundle.getBundle(
					"org.fife.ui.rsyntaxtextarea.GutterOptionPanel");

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
		temp.add(Box.createHorizontalGlue());
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(temp, BorderLayout.LINE_START);
		lineNumbersPanel.add(temp2);
		topPanel.add(lineNumbersPanel);
		topPanel.add(Box.createVerticalStrut(5));

		// Fold area options
		JPanel foldPanel = new JPanel(new SpringLayout());
		foldPanel.setBorder(new OptionPanelBorder(msg.getString("FoldArea")));
		JLabel foldBackgroundLabel = new JLabel(msg.getString("FoldBackground"));
		foldBackgroundButton = new RColorSwatchesButton();
		foldBackgroundButton.addPropertyChangeListener(
					RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		JPanel foldBgButtonPanel = new JPanel(new BorderLayout());
		addLeftAligned(foldBgButtonPanel, foldBackgroundButton);
		foldBackgroundLabel.setLabelFor(foldBackgroundButton);
		JLabel armedFoldBackgroundLabel = new JLabel(
				msg.getString("ArmedFoldBackground"));
		armedFoldBackgroundButton = new RColorSwatchesButton();
		armedFoldBackgroundButton.addPropertyChangeListener(
					RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		JPanel armedFoldBgButtonPanel = new JPanel(new BorderLayout());
		addLeftAligned(armedFoldBgButtonPanel, armedFoldBackgroundButton);
		armedFoldBackgroundLabel.setLabelFor(armedFoldBackgroundButton);
		UIUtil.addLabelValuePairs(foldPanel, orientation,
			foldBackgroundLabel, foldBgButtonPanel,
			armedFoldBackgroundLabel, armedFoldBgButtonPanel);
		UIUtil.makeSpringCompactGrid(foldPanel, 2, 2, 0, 0, 5, 5);
		temp = Box.createVerticalBox();
		temp.add(foldPanel);
		topPanel.add(temp);
		topPanel.add(Box.createVerticalStrut(5));

		JButton rdButton = new JButton(msg.getString("RestoreDefaults"));
		rdButton.setActionCommand("RestoreDefaults");
		rdButton.addActionListener(this);
		addLeftAligned(topPanel, rdButton);

		topPanel.add(Box.createVerticalGlue());
		add(topPanel, BorderLayout.NORTH);

		applyComponentOrientation(orientation);

	}


	/**
	 * Called when an action is performed in this panel.
	 *
	 * @param e The event.
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

			// Note we're a little cheap here and go with RSTA's default font rather
			// than look for fonts in themes.  This is OK since we don't actually
			// set fonts in any of the default themes.
			Font defaultFont = RTextArea.getDefaultFont();
			Color defLineNumberColor = rstaTheme.lineNumberColor;
			Color defFoldIconBackground = rstaTheme.foldBG;
			Color defArmedFoldIconBackground = rstaTheme.armedFoldBG;

			if (!lnEnabledCB.isSelected() ||
					!defaultFont.equals(fontSelector.getDisplayedFont()) ||
					!defLineNumberColor.equals(lnColorButton.getColor()) ||
					!defFoldIconBackground.equals(foldBackgroundButton.getColor()) ||
					!defArmedFoldIconBackground.equals(armedFoldBackgroundButton.getColor())) {

				lnEnabledCB.setSelected(true);
				fontSelector.setDisplayedFont(defaultFont, false);
				lnColorButton.setColor(defLineNumberColor);
				foldBackgroundButton.setColor(defFoldIconBackground);
				armedFoldBackgroundButton.setColor(defArmedFoldIconBackground);

				setDirty(true);

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
		mainView.setFoldBackground(foldBackgroundButton.getColor());
		mainView.setArmedFoldBackground(armedFoldBackgroundButton.getColor());
	}


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
	@Override
	public void itemStateChanged(ItemEvent e) {
		setDirty(true);
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
		foldBackgroundButton.setColor(mainView.getFoldBackground());
		armedFoldBackgroundButton.setColor(mainView.getArmedFoldBackground());
	}


}
