/*
 * 10/29/2009
 *
 * XmlOptionPanel.java - Options for XML language assistance.
 * Copyright (C) 2009 Robert Futrell
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
package org.fife.rtext.optionsdialog;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.modes.XMLTokenMaker;


/**
 * Options panel for XML language assistance.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class XmlOptionPanel extends OptionsDialogPanel implements ActionListener {

	private JCheckBox autoCompleteClosingTagsCB;

	private static final String PROPERTY		= "property";


	/**
	 * Constructor.
	 *
	 * @param rtext The owning RText instance.
	 * @param msg The resource bundle to use.
	 */
	public XmlOptionPanel(RText rtext, ResourceBundle msg) {

		super(msg.getString("Options.Xml.Name"));

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