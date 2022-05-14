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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
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
		Border empty5Border = UIUtil.getEmpty5Border();

		setLayout(new BorderLayout());
		setBorder(empty5Border);
		Box cp = Box.createVerticalBox();
		add(cp, BorderLayout.NORTH);

		cp.add(createHtmlOptionsSection());
		cp.add(Box.createVerticalStrut(5));
		cp.add(createXmlOptionsSection());
		cp.add(Box.createVerticalStrut(5));
		cp.add(createJsonOptionsSection());
		cp.add(Box.createVerticalStrut(5));

		JButton defaultsButton = new JButton(
			plugin.getApplication().getString("RestoreDefaults"));
		defaultsButton.setActionCommand("RestoreDefaults");
		defaultsButton.addActionListener(this);
		Box temp = createHorizontalBox();
		temp.add(defaultsButton);
		temp.add(Box.createHorizontalGlue());
		cp.add(temp);
		cp.add(Box.createVerticalGlue());

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
					getIntValue(jsonSpaceSpinner)!=3 ||
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
				jsonSpaceSpinner.setValue(3);
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
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(cb, BorderLayout.LINE_START);
		addTo.add(temp);
		addTo.add(Box.createVerticalStrut(5));
		cb.addActionListener(this);
		return cb;
	}


	private Container createHtmlOptionsSection() {

		ResourceBundle msg = Plugin.MSG;

		Box temp = Box.createVerticalBox();
		temp.setBorder(BorderFactory.createCompoundBorder(
				new OptionPanelBorder(msg.getString("Options.Section.HTML")),
				UIUtil.getEmpty5Border()));

		JLabel label = new JLabel(msg.getString("Options.SpaceCount"));
		htmlSpaceSpinner = newSpinner(new SpinnerNumberModel(4, -1,25, 1));
		label.setLabelFor(htmlSpaceSpinner);
		Container temp2 = createHorizontalBox();
		temp2.add(label);
		temp2.add(Box.createHorizontalStrut(5));
		temp2.add(htmlSpaceSpinner);
		temp2.add(Box.createHorizontalGlue());
		temp.add(temp2);
		temp.add(Box.createVerticalStrut(5));

		dropEmptyParasCB = addCheckBox("Options.DropEmptyParas", temp);
		hideEndTagsCB = addCheckBox("Options.HideOptionalEndTags", temp);
		logicalEmpCB = addCheckBox("Options.UseLogicalEmphasisTags", temp);
		makeCleanCB = addCheckBox("Options.ReplacePresentationalTags", temp);
		upperCaseTagNamesCB = addCheckBox("Options.UpperCaseTagNames", temp);
		upperCaseAttrNamesCB = addCheckBox("Options.UpperCaseAttrNames", temp);

		label = new JLabel(msg.getString("Options.WrapLength"));
		htmlWrapLenSpinner = newSpinner(new SpinnerNumberModel(4, 0,999999, 1));
		label.setLabelFor(htmlWrapLenSpinner);
		temp2 = createHorizontalBox();
		temp2.add(label);
		temp2.add(Box.createHorizontalStrut(5));
		temp2.add(htmlWrapLenSpinner);
		temp2.add(Box.createHorizontalGlue());
		temp.add(temp2);
		temp.add(Box.createVerticalStrut(5));

		return temp;

	}


	private Container createJsonOptionsSection() {

		ResourceBundle msg = Plugin.MSG;

		Box temp = Box.createVerticalBox();
		temp.setBorder(BorderFactory.createCompoundBorder(
				new OptionPanelBorder(msg.getString("Options.Section.JSON")),
				UIUtil.getEmpty5Border()));

		JLabel label = new JLabel(msg.getString("Options.SpaceCount"));
		jsonSpaceSpinner = newSpinner(new SpinnerNumberModel(4, -1,25, 1));
		label.setLabelFor(jsonSpaceSpinner);
		Container temp2 = createHorizontalBox();
		temp2.add(label);
		temp2.add(Box.createHorizontalStrut(5));
		temp2.add(jsonSpaceSpinner);
		temp2.add(Box.createHorizontalGlue());
		temp.add(temp2);
		temp.add(Box.createVerticalStrut(5));

		jsonStyleCombo = new LabelValueComboBox<>();
		jsonStyleCombo.addLabelValuePair("JSON", "json");
		jsonStyleCombo.addLabelValuePair("JavaScript", "javascript");
		jsonStyleCombo.addLabelValuePair(
				msg.getString("Options.JSON.Style.Minimal"), "minimal");
		jsonStyleCombo.addActionListener(this);
		label = UIUtil.newLabel(msg, "Options.JSON.Style", jsonStyleCombo);
		temp2 = createHorizontalBox();
		temp2.add(label);
		temp2.add(Box.createHorizontalStrut(5));
		temp2.add(jsonStyleCombo);
		temp2.add(Box.createHorizontalStrut(80));
		jsonIndentFirstLevelCB = UIUtil.newCheckBox(msg,
				"Options.JSON.IndentFirstLevel");
		jsonIndentFirstLevelCB.addActionListener(this);
		temp2.add(jsonIndentFirstLevelCB);
		temp2.add(Box.createHorizontalGlue());
		JPanel temp3 = new JPanel(new BorderLayout());
		temp3.add(temp2, BorderLayout.LINE_START);
		temp.add(temp3);
		temp.add(Box.createVerticalStrut(5));

		return temp;

	}


	private Container createXmlOptionsSection() {

		ResourceBundle msg = Plugin.MSG;

		Box temp = Box.createVerticalBox();
		temp.setBorder(BorderFactory.createCompoundBorder(
				new OptionPanelBorder(msg.getString("Options.Section.XML")),
				UIUtil.getEmpty5Border()));

		JLabel label = new JLabel(msg.getString("Options.SpaceCount"));
		xmlSpaceSpinner = newSpinner(new SpinnerNumberModel(4, -1,25, 1));
		label.setLabelFor(xmlSpaceSpinner);
		Box temp2 = createHorizontalBox();
		temp2.add(label);
		temp2.add(Box.createHorizontalStrut(5));
		temp2.add(xmlSpaceSpinner);
		temp2.add(Box.createHorizontalGlue());
		temp.add(temp2);
		temp.add(Box.createVerticalStrut(5));

		addXmlPiCB = addCheckBox("Options.AddXmlDeclaration", temp);
		temp.add(Box.createVerticalStrut(5));

		label = new JLabel(msg.getString("Options.WrapLength"));
		xmlWrapLenSpinner = newSpinner(new SpinnerNumberModel(4, 0,999999, 1));
		label.setLabelFor(xmlWrapLenSpinner);
		temp2 = createHorizontalBox();
		temp2.add(label);
		temp2.add(Box.createHorizontalStrut(5));
		temp2.add(xmlWrapLenSpinner);
		temp2.add(Box.createHorizontalGlue());
		temp.add(temp2);
		temp.add(Box.createVerticalStrut(5));

		//temp.add(Box.createVerticalGlue());
		return temp;

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
