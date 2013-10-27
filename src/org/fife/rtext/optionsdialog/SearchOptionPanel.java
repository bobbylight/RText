/*
 * 10/26/2013
 *
 * SearchOptionPanel.java - Options related to searching.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.optionsdialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.SearchManager.SearchingMode;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.SelectableLabel;
import org.fife.ui.SpecialValueComboBox;
import org.fife.ui.UIUtil;
import org.fife.ui.app.GUIApplication;
import org.fife.util.TranslucencyUtil;


/**
 * An options panel that display all search-related options.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SearchOptionPanel extends OptionsDialogPanel
		implements ActionListener, ChangeListener {

	private JRadioButton dialogRB;
	private JRadioButton toolbarRB;
	private JCheckBox translucentSearchDialogsCB;
	private JLabel ruleLabel;
	private SpecialValueComboBox ruleCombo;
	private JLabel opacityLabel;
	private JSlider slider;
	private JLabel opacityDisplay;

	private DecimalFormat format;

	private static final String PROPERTY	= "property";


	/**
	 * Constructor.
	 *
	 * @param app The owner of the options dialog in which this panel appears.
	 * @param msg The resource bundle to use.
	 */
	public SearchOptionPanel(GUIApplication app, ResourceBundle msg) {

		super(msg.getString("OptSearchOptionsName"));
		format = new DecimalFormat("0%");

		// Set up our border and layout.
		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		// Create a panel for stuff aligned at the top.
		Box topPanel = Box.createVerticalBox();

		ComponentOrientation o = ComponentOrientation.
									getOrientation(getLocale());

		// A panel for toggling the search UI
		Box otherPanel = Box.createVerticalBox();
		otherPanel.setBorder(new OptionPanelBorder(
									msg.getString("OptGenTitle")));
		ButtonGroup bg = new ButtonGroup();
		dialogRB = UIUtil.newRadio(msg, "Search.UIType.Dialog",
				bg, this);
		addLeftAligned(otherPanel, dialogRB);
		toolbarRB = UIUtil.newRadio(msg, "Search.UIType.Toolbar",
				bg, this);
		addLeftAligned(otherPanel, toolbarRB);
		topPanel.add(otherPanel);
		topPanel.add(Box.createVerticalStrut(5));

		// A panel for "experimental" options.
		Box expPanel = Box.createVerticalBox();
		expPanel.setBorder(new OptionPanelBorder(msg.
										getString("OptExperimentalTitle")));
		SelectableLabel label = new SelectableLabel(
								msg.getString("ExperimentalDisclaimer"));
		expPanel.add(label);
		expPanel.add(Box.createVerticalStrut(10));
		translucentSearchDialogsCB = new JCheckBox(
								msg.getString("TranslucentSearchBoxes"));
		translucentSearchDialogsCB.setActionCommand("TranslucentSearchDialogsCB");
		translucentSearchDialogsCB.addActionListener(this);
		addLeftAligned(expPanel, translucentSearchDialogsCB);
		ruleLabel = new JLabel(msg.getString("TranslucencyRule"));
		ruleCombo = new SpecialValueComboBox();
		ruleCombo.addSpecialItem(msg.getString("Translucency.Never"), "0");
		ruleCombo.addSpecialItem(msg.getString("Translucency.WhenNotFocused"), "1");
		ruleCombo.addSpecialItem(msg.getString("Translucency.WhenOverlappingApp"), "2");
		ruleCombo.addSpecialItem(msg.getString("Translucency.Always"), "3");
		ruleCombo.setActionCommand("TranslucencyRuleChanged");
		ruleCombo.addActionListener(this);
		opacityLabel = new JLabel(msg.getString("Opacity"));
		slider = new JSlider(0, 100);
		slider.setMajorTickSpacing(20);
		slider.setPaintTicks(true);
		slider.setPaintLabels(false);
		slider.addChangeListener(this);
		opacityDisplay = new JLabel("100%") { // will be replaced with real value
			// hack to keep SpringLayout from shifting when # of digits changes in %
			@Override
			public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				size.width = Math.max(50, size.width);
				return size;
			}
		};
		// Small border for spacing in both LTR and RTL locales
		opacityDisplay.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		Component filler = Box.createRigidArea(new Dimension(1, 1)); // Must have real size!
		JPanel temp = new JPanel(new SpringLayout());
		if (o.isLeftToRight()) {
			temp.add(ruleLabel);    temp.add(ruleCombo); temp.add(filler);
			temp.add(opacityLabel); temp.add(slider);    temp.add(opacityDisplay);
		}
		else {
			temp.add(filler);         temp.add(ruleCombo); temp.add(ruleLabel);
			temp.add(opacityDisplay); temp.add(slider);    temp.add(opacityLabel);
		}
		UIUtil.makeSpringCompactGrid(temp, 2,3, 5,5, 5,5);
		addLeftAligned(expPanel, temp, 5, 20);
		topPanel.add(expPanel);

		JButton defaultsButton = new JButton(msg.getString("RestoreDefaults"));
		defaultsButton.setActionCommand("RestoreDefaults");
		defaultsButton.addActionListener(this);
		temp = new JPanel(new BorderLayout());
		temp.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
		temp.add(defaultsButton, BorderLayout.LINE_START);
		topPanel.add(temp);

		// Do this after everything else is created.
		if (RTextUtilities.isPreJava6() ||
				TranslucencyUtil.get().isTranslucencySupported(false)) {
			setTranslucentSearchDialogsSelected(false);
		}

		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(o);

	}


	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();
		String command = e.getActionCommand();

		if (dialogRB==source) {
			hasUnsavedChanges = true;
			setUseSearchToolbars(false);
			firePropertyChange(PROPERTY, true, false);
		}

		else if (toolbarRB==source) {
			hasUnsavedChanges = true;
			setUseSearchToolbars(true);
			firePropertyChange(PROPERTY, false, true);
		}

		else if (translucentSearchDialogsCB==source) {
			hasUnsavedChanges = true;
			boolean selected = translucentSearchDialogsCB.isSelected();
			setTranslucentSearchDialogsSelected(selected);
			firePropertyChange(PROPERTY, !selected, selected);
		}

		else if (ruleCombo==source) {
			hasUnsavedChanges = true;
			int value = ruleCombo.getSelectedIndex();
			firePropertyChange(PROPERTY, -1, value);
		}

		else if ("RestoreDefaults".equals(command)) {

			final int defaultOpacity = 60;

			if (dialogRB.isSelected() ||
					translucentSearchDialogsCB.isSelected() ||
					ruleCombo.getSelectedIndex()!=2 ||
					slider.getValue()!=defaultOpacity) {
				setUseSearchToolbars(true);
				setTranslucentSearchDialogsSelected(false);
				ruleCombo.setSelectedIndex(2);
				slider.setValue(defaultOpacity);
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, false, true);
			}
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doApplyImpl(Frame owner) {

		RText rtext = (RText)owner;

		SearchingMode mode = dialogRB.isSelected() ?
				SearchingMode.DIALOGS : SearchingMode.TOOLBARS;
		rtext.getMainView().getSearchManager().setSearchingMode(mode);

		// Experimental options
		rtext.setSearchWindowOpacityEnabled(translucentSearchDialogsCB.
															isSelected());
		int rule = Integer.parseInt(ruleCombo.getSelectedSpecialItem());
		rtext.setSearchWindowOpacityRule(rule);
		float opacity = slider.getValue() / 100f;
		rtext.setSearchWindowOpacity(opacity);

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		// They can't input invalid stuff on this options panel.
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public JComponent getTopJComponent() {
		return dialogRB;
	}


	private void setTranslucentSearchDialogsSelected(boolean selected) {

		translucentSearchDialogsCB.setSelected(selected); // Probably already done

		// The sub-options always stay disabled if we're not using Java 6u10+.
		if (RTextUtilities.isPreJava6() ||
				!TranslucencyUtil.get().isTranslucencySupported(false)) {
			translucentSearchDialogsCB.setEnabled(false);
			selected = false;
		}

		ruleLabel.setEnabled(selected);
		ruleCombo.setEnabled(selected);
		opacityLabel.setEnabled(selected);
		opacityDisplay.setEnabled(selected);
		slider.setEnabled(selected);

	}


	private void setUseSearchToolbars(boolean use) {
		if (use) {
			toolbarRB.setSelected(true);
			translucentSearchDialogsCB.setEnabled(false);
			ruleLabel.setEnabled(false);
			ruleCombo.setEnabled(false);
			opacityLabel.setEnabled(false);
			opacityDisplay.setEnabled(false);
			slider.setEnabled(false);
		}
		else {
			dialogRB.setSelected(true);
			translucentSearchDialogsCB.setEnabled(true);
			// Set related components to correct enabled state
			setTranslucentSearchDialogsSelected(
					translucentSearchDialogsCB.isSelected());
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setValuesImpl(Frame owner) {

		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();

		// Experimental options
		setTranslucentSearchDialogsSelected(rtext.isSearchWindowOpacityEnabled());
		ruleCombo.setSelectedIndex(rtext.getSearchWindowOpacityRule());
		int percent = (int)(rtext.getSearchWindowOpacity()*100);
		slider.setValue(percent);
		opacityDisplay.setText(format.format(rtext.getSearchWindowOpacity()));

		// Do this after experimental section, since its toggling of enabled
		// states of components could override certain values.
		SearchingMode sm = mainView.getSearchManager().getSearchingMode();
		setUseSearchToolbars(sm==SearchingMode.TOOLBARS);

	}


	/**
	 * Called when the user plays with the opacity slider.
	 *
	 * @param e The change event.
	 */
	public void stateChanged(ChangeEvent e) {
		float value = slider.getValue() / 100f;
		opacityDisplay.setText(format.format(value));
		hasUnsavedChanges = true;
		firePropertyChange(PROPERTY, -1, slider.getValue());
	}


}