/*
 * 03/22/2010
 *
 * OptionsPanel.java - Option panel for the Tidy plugin.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
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
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

import org.fife.ui.RButton;
import org.fife.ui.UIUtil;
import org.fife.ui.app.PluginOptionsDialogPanel;


/**
 * Options panel for the tidy plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class OptionsPanel extends PluginOptionsDialogPanel implements ActionListener {

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

	private static final String PROPERTY		= "Property";


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin.
	 */
	public OptionsPanel(Plugin plugin) {

		super(plugin);

		ResourceBundle msg = Plugin.msg;
		setName(msg.getString("Options.Panel.Name"));

		ComponentOrientation o = ComponentOrientation.
											getOrientation(getLocale());
		Border empty5Border = UIUtil.getEmpty5Border();

		setLayout(new BorderLayout());
		setBorder(empty5Border);
		Box cp = Box.createVerticalBox();
		add(cp, BorderLayout.NORTH);

		// HTML tidy options
		Box temp = Box.createVerticalBox();
		temp.setBorder(BorderFactory.createCompoundBorder(
				new OptionPanelBorder(msg.getString("Options.Section.HTML")),
				empty5Border));

		JLabel label = new JLabel(msg.getString("Options.SpaceCount"));
		htmlSpaceSpinner = new JSpinner(new SpinnerNumberModel(4, -1,25, 1));
		label.setLabelFor(htmlSpaceSpinner);
		Container temp2 = Box.createHorizontalBox();
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
		htmlWrapLenSpinner = new JSpinner(new SpinnerNumberModel(4, 0,999999, 1));
		label.setLabelFor(htmlWrapLenSpinner);
		temp2 = Box.createHorizontalBox();
		temp2.add(label);
		temp2.add(Box.createHorizontalStrut(5));
		temp2.add(htmlWrapLenSpinner);
		temp2.add(Box.createHorizontalGlue());
		temp.add(temp2);
		temp.add(Box.createVerticalStrut(5));

		cp.add(temp);
		cp.add(Box.createVerticalStrut(5));

		// XML tidy options
		temp = Box.createVerticalBox();
		temp.setBorder(BorderFactory.createCompoundBorder(
				new OptionPanelBorder(msg.getString("Options.Section.XML")),
				empty5Border));

		label = new JLabel(msg.getString("Options.SpaceCount"));
		xmlSpaceSpinner = new JSpinner(new SpinnerNumberModel(4, -1,25, 1));
		label.setLabelFor(xmlSpaceSpinner);
		temp2 = Box.createHorizontalBox();
		temp2.add(label);
		temp2.add(Box.createHorizontalStrut(5));
		temp2.add(xmlSpaceSpinner);
		temp2.add(Box.createHorizontalGlue());
		temp.add(temp2);
		temp.add(Box.createVerticalStrut(5));

		addXmlPiCB = addCheckBox("Options.AddXmlDeclaration", temp);
		temp.add(Box.createVerticalStrut(5));

		label = new JLabel(msg.getString("Options.WrapLength"));
		xmlWrapLenSpinner = new JSpinner(new SpinnerNumberModel(4, 0,999999, 1));
		label.setLabelFor(xmlWrapLenSpinner);
		temp2 = Box.createHorizontalBox();
		temp2.add(label);
		temp2.add(Box.createHorizontalStrut(5));
		temp2.add(xmlWrapLenSpinner);
		temp2.add(Box.createHorizontalGlue());
		temp.add(temp2);
		temp.add(Box.createVerticalStrut(5));

		temp.add(Box.createVerticalGlue());

		cp.add(temp);
		cp.add(Box.createVerticalStrut(5));

		RButton defaultsButton = new RButton(
							msg.getString("Options.RestoreDefaults"));
		defaultsButton.setActionCommand("RestoreDefaults");
		defaultsButton.addActionListener(this);
		temp = Box.createHorizontalBox();
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
	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();

		if (source instanceof JCheckBox) {
			hasUnsavedChanges = true;
			JCheckBox cb = (JCheckBox)source;
			firePropertyChange(PROPERTY, !cb.isSelected(), cb.isSelected());
		}

		else if ("RestoreDefaults".equals(e.getActionCommand())) {
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
					getIntValue(xmlWrapLenSpinner)!=0) {
				addXmlPiCB.setSelected(false);
				dropEmptyParasCB.setSelected(false);
				hideEndTagsCB.setSelected(false);
				htmlSpaceSpinner.setValue(new Integer(4));
				logicalEmpCB.setSelected(false);
				makeCleanCB.setSelected(false);
				upperCaseAttrNamesCB.setSelected(false);
				upperCaseTagNamesCB.setSelected(false);
				xmlSpaceSpinner.setValue(new Integer(4));
				xmlWrapLenSpinner.setValue(new Integer(0));
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
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
		JCheckBox cb = new JCheckBox(Plugin.msg.getString(key));
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(cb, BorderLayout.LINE_START);
		addTo.add(temp);
		addTo.add(Box.createVerticalStrut(5));
		cb.addActionListener(this);
		return cb;
	}


	/**
	 * {@inheritDoc}
	 */
	protected void doApplyImpl(Frame owner) {

		Plugin plugin = (Plugin)getPlugin();

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

	}


	/**
	 * {@inheritDoc}
	 */
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * Short-hand for getting the value from a spinner displaying integers.
	 *
	 * @param spinner The spinner.
	 * @return The value displayed by the spinner.
	 */
	private int getIntValue(JSpinner spinner) {
		return ((Integer)spinner.getValue()).intValue();
	}


	/**
	 * {@inheritDoc}
	 */
	public JComponent getTopJComponent() {
		return htmlSpaceSpinner;
	}


	/**
	 * {@inheritDoc}
	 */
	protected void setValuesImpl(Frame owner) {

		Plugin plugin = (Plugin)getPlugin();

		HtmlOptions opts = plugin.getHtmlOptions();
		htmlSpaceSpinner.setValue(new Integer(opts.getSpaceCount()));
		dropEmptyParasCB.setSelected(opts.getDropEmptyParas());
		hideEndTagsCB.setSelected(opts.getHideOptionalEndTags());
		logicalEmpCB.setSelected(opts.getLogicalEmphasis());
		makeCleanCB.setSelected(opts.getMakeClean());
		upperCaseTagNamesCB.setSelected(opts.getUpperCaseTagNames());
		upperCaseAttrNamesCB.setSelected(opts.getUpperCaseAttrNames());
		htmlWrapLenSpinner.setValue(new Integer(opts.getWrapLength()));

		XmlOptions xmlOpts = plugin.getXmlOptions();
		xmlSpaceSpinner.setValue(new Integer(xmlOpts.getSpaceCount()));
		addXmlPiCB.setSelected(xmlOpts.getAddXmlDeclaration());
		xmlWrapLenSpinner.setValue(new Integer(xmlOpts.getWrapLength()));

	}


}