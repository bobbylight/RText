/*
 * 05/27/2010
 *
 * ShellOptionsPanel.java - Options for Unix sh language support.
 * Copyright (C) 2010 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.sh.ShellLanguageSupport;
import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options for shell language support.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ShellOptionsPanel extends OptionsDialogPanel {

	private final Listener listener;
	private JCheckBox enabledCB;
	private JCheckBox showDescWindowCB;
	private JCheckBox useSystemManCB;
	private final JButton rdButton;


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	ShellOptionsPanel(RText app) {

		ResourceBundle msg = Plugin.MSG;
		setName(msg.getString("Options.Sh.Name"));
		listener = new Listener();
		setIcon(app.getIconGroup().getIcon("fileTypes/unix"));
		app.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, e -> {
			setIcon(app.getIconGroup().getIcon("fileTypes/unix"));
		});

		ComponentOrientation o = ComponentOrientation.
											getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());

		Box topPanel = Box.createVerticalBox();

		topPanel.add(createGeneralPanel(msg));
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		rdButton = new JButton(app.getString("RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(topPanel, rdButton);

		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(o);

	}


	private JCheckBox createCB(String key) {
		if (key.indexOf('.')==-1) {
			key = "Options.Sh." + key;
		}
		JCheckBox cb = new JCheckBox(Plugin.MSG.getString(key));
		cb.addActionListener(listener);
		return cb;
	}


	private Box createGeneralPanel(ResourceBundle msg) {

		Box generalPanel = Box.createVerticalBox();
		generalPanel.setBorder(new OptionPanelBorder(msg.
			getString("Options.General.Section.General")));

		enabledCB = createCB("Options.General.EnableCodeCompletion");
		addLeftAligned(generalPanel, enabledCB, COMPONENT_VERTICAL_SPACING);

		showDescWindowCB = createCB("Options.General.ShowDescWindow");
		addLeftAligned(generalPanel, showDescWindowCB, COMPONENT_VERTICAL_SPACING, 20);

		useSystemManCB = createCB("UseSystemManPages");
		addLeftAligned(generalPanel, useSystemManCB, 0, 20);

		return generalPanel;
	}


	@Override
	protected void doApplyImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport ls=lsf.getSupportFor(
									SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
		ShellLanguageSupport sls = (ShellLanguageSupport)ls;

		// Options dealing with code completion.
		ls.setAutoCompleteEnabled(enabledCB.isSelected());
		ls.setShowDescWindow(showDescWindowCB.isSelected());
		sls.setUseLocalManPages(useSystemManCB.isSelected());

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
		showDescWindowCB.setEnabled(selected);
		useSystemManCB.setEnabled(selected  && File.separatorChar=='/');
	}


	@Override
	protected void setValuesImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport ls=lsf.getSupportFor(
								SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
		ShellLanguageSupport sls = (ShellLanguageSupport)ls;

		// Options dealing with code completion
		setEnabledCBSelected(ls.isAutoCompleteEnabled());
		showDescWindowCB.setSelected(ls.getShowDescWindow());
		useSystemManCB.setSelected(sls.getUseLocalManPages());

	}


	/**
	 * Listens for events in this options panel.
	 */
	private class Listener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			boolean defaultShowDescWindow = File.separatorChar=='/';
			boolean defaultSystemManSelected = File.separatorChar=='/';

			Object source = e.getSource();

			if (enabledCB==source) {
				// Trick related components to toggle enabled states
				setEnabledCBSelected(enabledCB.isSelected());
				setDirty(true);
			}

			else if (showDescWindowCB==source) {
				setDirty(true);
			}

			else if (rdButton==source) {
				if (!enabledCB.isSelected() ||
						showDescWindowCB.isSelected()!=defaultShowDescWindow ||
						useSystemManCB.isSelected()!=defaultSystemManSelected) {
					setEnabledCBSelected(true);
					showDescWindowCB.setSelected(true);
					useSystemManCB.setSelected(defaultSystemManSelected);
					setDirty(true);
				}
			}

		}

	}


}
