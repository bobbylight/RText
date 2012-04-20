/*
 * 10/29/2009
 *
 * XmlOptionPanel.java - Options for XML language assistance.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.modes.XMLTokenMaker;


/**
 * Options panel for XML language assistance.  Most of the options here
 * actually don't correspond to options made available in
 * <code>RSTALanguageSupport</code>, but rather options built into RSTA proper;
 * however, to keep the options dialog organized consistently all
 * language-specific options are under the umbrella of this plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class XmlOptionsPanel extends OptionsDialogPanel implements ActionListener {

	private JCheckBox autoCompleteClosingTagsCB;

	private static final String PROPERTY		= "property";


	/**
	 * Constructor.
	 */
	public XmlOptionsPanel() {

		ResourceBundle msg = Plugin.msg;
		setName(msg.getString("Options.Xml.Name"));
		setIcon(new ImageIcon(getClass().getResource("xml.png")));

		ComponentOrientation o = ComponentOrientation.
										getOrientation(getLocale());
		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createCompoundBorder(
						new OptionPanelBorder(
								msg.getString("Options.Xml.GeneralSection")),
								UIUtil.getEmpty5Border()));
		autoCompleteClosingTagsCB = new JCheckBox(
							msg.getString("Options.Xml.CompleteClosingTags"));
		autoCompleteClosingTagsCB.addActionListener(this);
		topPanel.add(autoCompleteClosingTagsCB, BorderLayout.LINE_START);
		add(topPanel, BorderLayout.NORTH);

		applyComponentOrientation(o);

	}


	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();

		if (autoCompleteClosingTagsCB==source) {
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, null, null);
		}

	}


	/**
	 * {@inheritDoc}
	 */
	protected void doApplyImpl(Frame owner) {
		boolean closeClosingTags = autoCompleteClosingTagsCB.isSelected();
		XMLTokenMaker.setCompleteCloseTags(closeClosingTags);
	}


	/**
	 * {@inheritDoc}
	 */
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	public JComponent getTopJComponent() {
		return autoCompleteClosingTagsCB;
	}


	/**
	 * {@inheritDoc}
	 */
	protected void setValuesImpl(Frame owner) {
		boolean close = XMLTokenMaker.getCompleteCloseMarkupTags();
		autoCompleteClosingTagsCB.setSelected(close);
	}


}