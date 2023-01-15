/*
 * 05/26/2010
 *
 * COptionsPanel.java - Options for C language support.
 * Copyright (C) 2010 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.border.Border;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel for C language support.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class COptionsPanel extends OptionsDialogPanel {

	private final Listener listener;
	private JCheckBox enabledCB;
	private JCheckBox paramAssistanceCB;
	private JCheckBox showDescWindowCB;
	private JCheckBox foldingEnabledCB;
	private JButton rdButton;


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	COptionsPanel(RText app) {

		ResourceBundle msg = Plugin.MSG;
		setName(msg.getString("Options.C.Name"));
		listener = new Listener();
		setIcon(app.getIconGroup().getIcon("fileTypes/c"));
		app.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, e -> {
			setIcon(app.getIconGroup().getIcon("fileTypes/c"));
		});

		ComponentOrientation o = ComponentOrientation.
											getOrientation(getLocale());

		setLayout(new BorderLayout());
		Border empty5Border = UIUtil.getEmpty5Border();
		setBorder(empty5Border);

		Box topPanel = Box.createVerticalBox();
		topPanel.setBorder(null);
		add(topPanel, BorderLayout.NORTH);

		topPanel.add(createGeneralPanel(msg, o));
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		topPanel.add(createFoldingPanel(msg));
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		rdButton = new JButton(app.getString("RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(topPanel, rdButton);

		topPanel.add(Box.createVerticalGlue());
		applyComponentOrientation(o);

	}


	private JCheckBox createCB(String key) {
		if (key.indexOf('.')==-1) {
			key = "Options.C." + key;
		}
		JCheckBox cb = new JCheckBox(Plugin.MSG.getString(key));
		cb.addActionListener(listener);
		return cb;
	}


	private Container createFoldingPanel(ResourceBundle msg) {

		Box foldingPanel = Box.createVerticalBox();
		foldingPanel.setBorder(new OptionPanelBorder(msg.
			getString("Options.General.Section.Folding")));

		foldingEnabledCB = createCB("Options.General.EnableCodeFolding");
		addLeftAligned(foldingPanel, foldingEnabledCB);

		return foldingPanel;
	}


	private Container createGeneralPanel(ResourceBundle msg,
										 ComponentOrientation o) {

		Box generalPanel = Box.createVerticalBox();
		generalPanel.setBorder(new OptionPanelBorder(msg.
			getString("Options.General.Section.General")));

		enabledCB = createCB("Options.General.EnableCodeCompletion");
		addLeftAligned(generalPanel, enabledCB, COMPONENT_VERTICAL_SPACING);

		Box subOptionsPanel = Box.createVerticalBox();
		if (o.isLeftToRight()) {
			subOptionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		}
		else {
			subOptionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
		}
		generalPanel.add(subOptionsPanel);

		showDescWindowCB = createCB("Options.General.ShowDescWindow");
		addLeftAligned(subOptionsPanel, showDescWindowCB, COMPONENT_VERTICAL_SPACING);

		paramAssistanceCB = createCB("Options.General.ParameterAssistance");
		addLeftAligned(subOptionsPanel, paramAssistanceCB);

		subOptionsPanel.add(Box.createVerticalGlue());

		return generalPanel;
	}


	@Override
	protected void doApplyImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport ls=lsf.getSupportFor(SyntaxConstants.SYNTAX_STYLE_C);

		// Options dealing with code completion.
		ls.setAutoCompleteEnabled(enabledCB.isSelected());
		ls.setParameterAssistanceEnabled(paramAssistanceCB.isSelected());
		ls.setShowDescWindow(showDescWindowCB.isSelected());

	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	@Override
	public JComponent getTopJComponent() {
		return enabledCB;
	}


	private void setEnabledCBSelected(boolean selected) {
		enabledCB.setSelected(selected);
		paramAssistanceCB.setEnabled(selected);
		showDescWindowCB.setEnabled(selected);
	}


	@Override
	protected void setValuesImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport ls=lsf.getSupportFor(SyntaxConstants.SYNTAX_STYLE_C);

		// Options dealing with code completion
		setEnabledCBSelected(ls.isAutoCompleteEnabled());
		paramAssistanceCB.setSelected(ls.isParameterAssistanceEnabled());
		showDescWindowCB.setSelected(ls.getShowDescWindow());

		// Code folding options
		//foldingEnabledCB.setSelected()

	}


	/**
	 * Listens for events in this options panel.
	 */
	private class Listener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (enabledCB==source) {
				// Trick related components to toggle enabled states
				setEnabledCBSelected(enabledCB.isSelected());
				setDirty(true);
			}

			else if (showDescWindowCB==source) {
				setDirty(true);
			}

			else if (foldingEnabledCB==source) {
				setDirty(true);
			}

			else if (rdButton==source) {
				if (!enabledCB.isSelected() ||
						!foldingEnabledCB.isSelected() ||
						!paramAssistanceCB.isSelected() ||
						!showDescWindowCB.isSelected()) {
					setEnabledCBSelected(true);
					foldingEnabledCB.setSelected(true);
					paramAssistanceCB.setSelected(true);
					showDescWindowCB.setSelected(true);
					setDirty(true);
				}
			}

		}

	}


}
