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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.FontSelector;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.UIUtil;
import org.fife.ui.rtextarea.RTextArea;


/**
 * Option panel for the text area gutter.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class GutterOptionPanel extends AbstractTextAreaOptionPanel
		implements PropertyChangeListener, ItemListener {

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

		setName(MSG.getString("Title.Gutter"));

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		// We'll add everything to this panel, then add this panel so that
		// stuff stays at the "top."
		Box topPanel = new Box(BoxLayout.Y_AXIS);

		topPanel.add(createOverridePanel());
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		topPanel.add(createLineNumbersPanel());
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		// Fold area options
		topPanel.add(createFoldAreaPanel(orientation));
		topPanel.add(Box.createVerticalStrut(5));

		// Create a panel containing the preview and "Restore Defaults"
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(new PreviewPanel(MSG, 9, 40));
		bottomPanel.add(createRestoreDefaultsPanel(), BorderLayout.SOUTH);
		topPanel.add(bottomPanel);

		topPanel.add(Box.createVerticalGlue());
		add(topPanel, BorderLayout.NORTH);

		applyComponentOrientation(orientation);

	}


	private JPanel createFoldAreaPanel(ComponentOrientation orientation) {

		JPanel foldPanel = new JPanel(new SpringLayout());
		foldPanel.setBorder(new OptionPanelBorder(MSG.getString("FoldArea")));

		JLabel foldBackgroundLabel = new JLabel(MSG.getString("FoldBackground"));
		foldBackgroundButton = new RColorSwatchesButton();
		foldBackgroundButton.addPropertyChangeListener(
			RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		JPanel foldBgButtonPanel = new JPanel(new BorderLayout());
		addLeftAligned(foldBgButtonPanel, foldBackgroundButton);
		foldBackgroundLabel.setLabelFor(foldBackgroundButton);

		JLabel armedFoldBackgroundLabel = new JLabel(
			MSG.getString("ArmedFoldBackground"));
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
		return foldPanel;
	}


	private Box createLineNumbersPanel() {

		Box lineNumbersPanel = new Box(BoxLayout.Y_AXIS);
		lineNumbersPanel.setBorder(new OptionPanelBorder(
			MSG.getString("LineNumbers")));

		lnEnabledCB = new JCheckBox(MSG.getString("Enabled"));
		lnEnabledCB.addItemListener(this);
		addLeftAligned(lineNumbersPanel, lnEnabledCB, COMPONENT_VERTICAL_SPACING);

		JPanel fontPanel = new JPanel(new BorderLayout());
		fontSelector = new FontSelector();
		fontSelector.setColorSelectable(true);
		fontSelector.addPropertyChangeListener(FontSelector.FONT_PROPERTY, this);
		fontSelector.addPropertyChangeListener(FontSelector.FONT_COLOR_PROPERTY, this);
		fontPanel.add(fontSelector);
		lineNumbersPanel.add(fontPanel);
		lineNumbersPanel.add(Box.createVerticalStrut(COMPONENT_VERTICAL_SPACING));

		JLabel lnColorLabel = new JLabel(MSG.getString("Color"));
		lnColorButton = new RColorSwatchesButton();
		lnColorButton.addPropertyChangeListener(
			RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		lnColorLabel.setLabelFor(lnColorButton);
		Box temp = new Box(BoxLayout.LINE_AXIS);
		temp.add(lnColorLabel);
		temp.add(Box.createHorizontalStrut(5));
		temp.add(lnColorButton);
		temp.add(Box.createHorizontalGlue());
		addLeftAligned(lineNumbersPanel, temp);

		return lineNumbersPanel;
	}


	@Override
	protected void doApplyImpl(Frame owner) {

		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		mainView.setOverrideEditorStyles(overrideCheckBox.isSelected());

		if (overrideCheckBox.isSelected()) {
			mainView.setLineNumbersEnabled(lnEnabledCB.isSelected());
			mainView.setLineNumberFont(fontSelector.getDisplayedFont());
			mainView.setLineNumberColor(lnColorButton.getColor());
			mainView.setFoldBackground(foldBackgroundButton.getColor());
			mainView.setArmedFoldBackground(armedFoldBackgroundButton.getColor());
		}
		else {
			EditorOptionsPreviewContext editorContext = EditorOptionsPreviewContext.get();
			Theme editorTheme = editorContext.getEditorTheme(rtext);
			mainView.setLineNumbersEnabled(true);
			mainView.setLineNumberFont(editorContext.getFont());
			mainView.setLineNumberColor(editorTheme.lineNumberColor);
			mainView.setFoldBackground(editorTheme.foldBG);
			mainView.setArmedFoldBackground(editorTheme.armedFoldBG);
		}
	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	@Override
	public void handleRestoreDefaults() {

		// This panel's defaults are based on the current theme.
		RText app = (RText)getOptionsDialog().getParent();
		EditorOptionsPreviewContext editorContext = EditorOptionsPreviewContext.get();
		Theme rstaTheme = editorContext.getEditorTheme(app);

		// Note we're a little cheap here and go with RSTA's default font rather
		// than look for fonts in themes.  This is OK since we don't actually
		// set fonts in any of the default themes.
		Font defaultFont = RTextArea.getDefaultFont();
		Color defLineNumberColor = rstaTheme.lineNumberColor;
		Color defFoldIconBackground = rstaTheme.foldBG;
		Color defArmedFoldIconBackground = rstaTheme.armedFoldBG;

		if (overrideCheckBox.isSelected() ||
			!lnEnabledCB.isSelected() ||
			!defaultFont.equals(fontSelector.getDisplayedFont()) ||
			!defLineNumberColor.equals(lnColorButton.getColor()) ||
			!defFoldIconBackground.equals(foldBackgroundButton.getColor()) ||
			!defArmedFoldIconBackground.equals(armedFoldBackgroundButton.getColor())) {

			overrideCheckBox.setSelected(false);
			lnEnabledCB.setSelected(true);
			fontSelector.setDisplayedFont(defaultFont, false);
			lnColorButton.setColor(defLineNumberColor);
			foldBackgroundButton.setColor(defFoldIconBackground);
			armedFoldBackgroundButton.setColor(defArmedFoldIconBackground);

			setDirty(true);

		}
	}


	@Override
	public void itemStateChanged(ItemEvent e) {
		setDirty(true);
		super.itemStateChanged(e);
	}


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

		// Do this after initializing all values above
		overrideCheckBox.setSelected(mainView.getOverrideEditorStyles());
		setComponentsEnabled(overrideCheckBox.isSelected());

		syncEditorOptionsPreviewContext();
	}


	@Override
	protected void syncEditorOptionsPreviewContext() {
		EditorOptionsPreviewContext context = EditorOptionsPreviewContext.get();
		context.setOverrideEditorTheme(overrideCheckBox.isSelected());
		context.setLineNumbersEnabled(lnEnabledCB.isSelected());
		context.setLineNumberFont(fontSelector.getDisplayedFont());
		context.setLineNumberColor(lnColorButton.getColor());
		context.setFoldBackground(foldBackgroundButton.getColor());
		context.setArmedFoldBackground(armedFoldBackgroundButton.getColor());

		context.possiblyFireChangeEventAndReset();
	}
}
