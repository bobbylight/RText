/*
 * 03/22/2010
 *
 * OptionsPanel.java - Option panel for the Tidy plugin.
 * Copyright (C) 2010 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tidy;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.fife.ui.LabelValueComboBox;
import org.fife.ui.UIUtil;
import org.fife.ui.app.PluginOptionsDialogPanel;


/**
 * Options panel for the tidy plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class OptionsPanel extends PluginOptionsDialogPanel<Plugin>
		implements ActionListener, ChangeListener {

	// HTML properties
	private JSpinner htmlSpaceSpinner;
	private JCheckBox dropEmptyParasCB;
	private JCheckBox hideEndTagsCB;
	private JCheckBox logicalEmpCB;
	private JCheckBox makeCleanCB;
	private JCheckBox upperCaseTagNamesCB;
	private JCheckBox upperCaseAttrNamesCB;
	private JSpinner htmlWrapLenSpinner;

	// XML properties
	private JSpinner xmlSpaceSpinner;
	private JCheckBox addXmlPiCB;
	private JSpinner xmlWrapLenSpinner;

	// JSON properties
	private JSpinner jsonSpaceSpinner;
	private LabelValueComboBox<String, String> jsonStyleCombo;
	private JCheckBox jsonIndentFirstLevelCB;


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin.
	 */
	OptionsPanel(Plugin plugin) {

		super(plugin);

		ResourceBundle msg = Plugin.MSG;
		setName(msg.getString("Options.Panel.Name"));

		ComponentOrientation o = ComponentOrientation.
											getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		Box topPanel = Box.createVerticalBox();

		topPanel.add(createHtmlOptionsSection(o));
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));
		topPanel.add(createXmlOptionsSection(o));
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));
		topPanel.add(createJsonOptionsSection(o));
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		JButton defaultsButton = new JButton(
			plugin.getApplication().getString("RestoreDefaults"));
		defaultsButton.setActionCommand("RestoreDefaults");
		defaultsButton.addActionListener(this);
		addLeftAligned(topPanel, defaultsButton);

		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(o);

	}


	/**
	 * Called when an event occurs in this panel.
	 *
	 * @param e The event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();

		if (source instanceof JCheckBox || source instanceof JComboBox) {
			setDirty(true);
		}

		else if ("RestoreDefaults".equals(e.getActionCommand())) {
			final String defaultJsonStyle = "json";
			if (addXmlPiCB.isSelected() ||
					dropEmptyParasCB.isSelected() ||
					hideEndTagsCB.isSelected() ||
					getIntValue(htmlSpaceSpinner)!=4 ||
					getIntValue(htmlWrapLenSpinner)!=0 ||
					logicalEmpCB.isSelected() ||
					makeCleanCB.isSelected() ||
					upperCaseAttrNamesCB.isSelected() ||
					upperCaseTagNamesCB.isSelected() ||
					getIntValue(xmlSpaceSpinner)!=4 ||
					getIntValue(xmlWrapLenSpinner)!=0 ||
					jsonIndentFirstLevelCB.isSelected() ||
					getIntValue(jsonSpaceSpinner)!=2 ||
					!defaultJsonStyle.equals(
							jsonStyleCombo.getSelectedValue())) {
				addXmlPiCB.setSelected(false);
				dropEmptyParasCB.setSelected(false);
				hideEndTagsCB.setSelected(false);
				htmlSpaceSpinner.setValue(4);
				logicalEmpCB.setSelected(false);
				makeCleanCB.setSelected(false);
				upperCaseAttrNamesCB.setSelected(false);
				upperCaseTagNamesCB.setSelected(false);
				xmlSpaceSpinner.setValue(4);
				xmlWrapLenSpinner.setValue(0);
				jsonIndentFirstLevelCB.setSelected(false);
				jsonSpaceSpinner.setValue(2);
				jsonStyleCombo.setSelectedValue(defaultJsonStyle);
				setDirty(true);
			}
		}

	}


	/**
	 * Adds a check box to this panel, left-aligned (right-aligned for RTL).
	 *
	 * @param key The key for the check box's text.
	 * @param addTo The panel to add the check box to.
	 * @return The check box added.
	 */
	private JCheckBox addCheckBox(String key, Box addTo) {
		JCheckBox cb = new JCheckBox(Plugin.MSG.getString(key));
		cb.addActionListener(this);
		addLeftAligned(addTo, cb, COMPONENT_VERTICAL_SPACING);
		return cb;
	}


	private Container createHtmlOptionsSection(ComponentOrientation o) {

		ResourceBundle msg = Plugin.MSG;

		Box htmlPanel = Box.createVerticalBox();
		htmlPanel.setBorder(new OptionPanelBorder(msg.getString("Options.Section.HTML")));

		JLabel spaceLabel = new JLabel(msg.getString("Options.SpaceCount"));
		htmlSpaceSpinner = newSpinner(new SpinnerNumberModel(4, -1,25, 1));
		spaceLabel.setLabelFor(htmlSpaceSpinner);

		JLabel wrapLenLabel = new JLabel(msg.getString("Options.WrapLength"));
		htmlWrapLenSpinner = newSpinner(new SpinnerNumberModel(4, 0,999999, 1));
		wrapLenLabel.setLabelFor(htmlWrapLenSpinner);

		JPanel labelledSpinnersPanel = new JPanel(new SpringLayout());
		UIUtil.addLabelValuePairs(labelledSpinnersPanel, o,
			spaceLabel, htmlSpaceSpinner,
			wrapLenLabel, htmlWrapLenSpinner);
		UIUtil.makeSpringCompactGrid(labelledSpinnersPanel, 2, 2, 0, 0, 5, 5);
		addLeftAligned(htmlPanel, labelledSpinnersPanel, COMPONENT_VERTICAL_SPACING);

		dropEmptyParasCB = addCheckBox("Options.DropEmptyParas", htmlPanel);
		hideEndTagsCB = addCheckBox("Options.HideOptionalEndTags", htmlPanel);
		logicalEmpCB = addCheckBox("Options.UseLogicalEmphasisTags", htmlPanel);
		makeCleanCB = addCheckBox("Options.ReplacePresentationalTags", htmlPanel);
		upperCaseTagNamesCB = addCheckBox("Options.UpperCaseTagNames", htmlPanel);
		upperCaseAttrNamesCB = addCheckBox("Options.UpperCaseAttrNames", htmlPanel);

		return htmlPanel;

	}


	private Container createJsonOptionsSection(ComponentOrientation o) {

		ResourceBundle msg = Plugin.MSG;

		Box jsonPanel = Box.createVerticalBox();
		jsonPanel.setBorder(new OptionPanelBorder(msg.getString("Options.Section.JSON")));

		JLabel spaceLabel = new JLabel(msg.getString("Options.SpaceCount"));
		jsonSpaceSpinner = newSpinner(new SpinnerNumberModel(4, -1,25, 1));
		spaceLabel.setLabelFor(jsonSpaceSpinner);

		jsonStyleCombo = new LabelValueComboBox<>();
		jsonStyleCombo.addLabelValuePair("JSON", "json");
		jsonStyleCombo.addLabelValuePair("JavaScript", "javascript");
		jsonStyleCombo.addLabelValuePair(
				msg.getString("Options.JSON.Style.Minimal"), "minimal");
		jsonStyleCombo.addActionListener(this);
		JLabel styleLabel = UIUtil.newLabel(msg, "Options.JSON.Style", jsonStyleCombo);

		JPanel labelledFieldsPanel = new JPanel(new SpringLayout());
		UIUtil.addLabelValuePairs(labelledFieldsPanel, o,
			spaceLabel, jsonSpaceSpinner,
			styleLabel, jsonStyleCombo);
		UIUtil.makeSpringCompactGrid(labelledFieldsPanel, 2, 2, 0, 0, 5, 5);
		addLeftAligned(jsonPanel, labelledFieldsPanel, COMPONENT_VERTICAL_SPACING);

		jsonIndentFirstLevelCB = UIUtil.newCheckBox(msg,
				"Options.JSON.IndentFirstLevel");
		jsonIndentFirstLevelCB.addActionListener(this);
		addLeftAligned(jsonPanel, jsonIndentFirstLevelCB);

		return jsonPanel;

	}


	private Container createXmlOptionsSection(ComponentOrientation o) {

		ResourceBundle msg = Plugin.MSG;

		Box xmlPanel = Box.createVerticalBox();
		xmlPanel.setBorder(new OptionPanelBorder(msg.getString("Options.Section.XML")));

		JLabel spaceLabel = new JLabel(msg.getString("Options.SpaceCount"));
		xmlSpaceSpinner = newSpinner(new SpinnerNumberModel(4, -1,25, 1));
		spaceLabel.setLabelFor(xmlSpaceSpinner);

		JLabel wrapLenLabel = new JLabel(msg.getString("Options.WrapLength"));
		xmlWrapLenSpinner = newSpinner(new SpinnerNumberModel(4, 0,999999, 1));
		wrapLenLabel.setLabelFor(xmlWrapLenSpinner);

		JPanel labelledSpinnersPanel = new JPanel(new SpringLayout());
		UIUtil.addLabelValuePairs(labelledSpinnersPanel, o,
			spaceLabel, xmlSpaceSpinner,
			wrapLenLabel, xmlWrapLenSpinner);
		UIUtil.makeSpringCompactGrid(labelledSpinnersPanel, 2, 2, 0, 0, 5, 5);
		addLeftAligned(xmlPanel, labelledSpinnersPanel, COMPONENT_VERTICAL_SPACING);

		addXmlPiCB = addCheckBox("Options.AddXmlDeclaration", xmlPanel);

		return xmlPanel;

	}


	@Override
	protected void doApplyImpl(Frame owner) {

		Plugin plugin = getPlugin();

		HtmlOptions opts = plugin.getHtmlOptions();
		opts.setSpaceCount(getIntValue(htmlSpaceSpinner));
		opts.setDropEmptyParas(dropEmptyParasCB.isSelected());
		opts.setHideOptionalEndTags(hideEndTagsCB.isSelected());
		opts.setLogicalEmphasis(logicalEmpCB.isSelected());
		opts.setMakeClean(makeCleanCB.isSelected());
		opts.setUpperCaseTagNames(upperCaseTagNamesCB.isSelected());
		opts.setUpperCaseAttrNames(upperCaseAttrNamesCB.isSelected());
		opts.setWrapLength(getIntValue(htmlWrapLenSpinner));

		XmlOptions xmlOpts = plugin.getXmlOptions();
		xmlOpts.setSpaceCount(getIntValue(xmlSpaceSpinner));
		xmlOpts.setAddXmlDeclaration(addXmlPiCB.isSelected());
		xmlOpts.setWrapLength(getIntValue(xmlWrapLenSpinner));

		JsonOptions jsonOpts = plugin.getJsonOptions();
		jsonOpts.setSpaceCount(getIntValue(jsonSpaceSpinner));
		jsonOpts.setOutputStyle(jsonStyleCombo.getSelectedValue());
		jsonOpts.setIndentFirstLevel(jsonIndentFirstLevelCB.isSelected());

	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * Short-hand for getting the value from a spinner displaying integers.
	 *
	 * @param spinner The spinner.
	 * @return The value displayed by the spinner.
	 */
	private static int getIntValue(JSpinner spinner) {
		return ((Integer)spinner.getValue());
	}


	@Override
	public JComponent getTopJComponent() {
		return htmlSpaceSpinner;
	}


	/**
	 * Creates a spinner for use in this option panel.
	 *
	 * @param model The model for the spinner.
	 * @return The spinner.
	 */
	private JSpinner newSpinner(SpinnerModel model) {
		JSpinner spinner = new JSpinner(model);
		spinner.addChangeListener(this);
		return spinner;
	}


	@Override
	protected void setValuesImpl(Frame owner) {

		Plugin plugin = getPlugin();

		HtmlOptions opts = plugin.getHtmlOptions();
		htmlSpaceSpinner.setValue(opts.getSpaceCount());
		dropEmptyParasCB.setSelected(opts.getDropEmptyParas());
		hideEndTagsCB.setSelected(opts.getHideOptionalEndTags());
		logicalEmpCB.setSelected(opts.getLogicalEmphasis());
		makeCleanCB.setSelected(opts.getMakeClean());
		upperCaseTagNamesCB.setSelected(opts.getUpperCaseTagNames());
		upperCaseAttrNamesCB.setSelected(opts.getUpperCaseAttrNames());
		htmlWrapLenSpinner.setValue(opts.getWrapLength());

		XmlOptions xmlOpts = plugin.getXmlOptions();
		xmlSpaceSpinner.setValue(xmlOpts.getSpaceCount());
		addXmlPiCB.setSelected(xmlOpts.getAddXmlDeclaration());
		xmlWrapLenSpinner.setValue(xmlOpts.getWrapLength());

		JsonOptions jsonOpts = plugin.getJsonOptions();
		jsonSpaceSpinner.setValue(jsonOpts.getSpaceCount());
		jsonStyleCombo.setSelectedValue(jsonOpts.getOutputStyle());
		jsonIndentFirstLevelCB.setSelected(jsonOpts.getIndentFirstLevel());

	}


	@Override
	public void stateChanged(ChangeEvent e) {
		setDirty(true);
	}


}
