/*
 * 05/27/2010
 *
 * ShellOptionsPanel.java - Options for Unix sh language support.
 * Copyright (C) 2010 Robert Futrell
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
package org.fife.rtext.plugins.langsupport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.sh.ShellLanguageSupport;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RButton;
import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options for shell language support.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ShellOptionsPanel extends OptionsDialogPanel {

	private Listener listener;
	private JCheckBox enabledCB;
	private JCheckBox showDescWindowCB;
	private JCheckBox useSystemManCB;
	private RButton rdButton;

	private static final String PROPERTY		= "Property";


	/**
	 * Constructor.
	 */
	public ShellOptionsPanel() {

		ResourceBundle msg = Plugin.msg;
		setName(msg.getString("Options.Sh.Name"));
		listener = new Listener();
		setIcon(new ImageIcon(getClass().getResource("page_white_tux.png")));

		ComponentOrientation o = ComponentOrientation.
											getOrientation(getLocale());

		setLayout(new BorderLayout());
		Border empty5Border = UIUtil.getEmpty5Border();
		setBorder(empty5Border);

		Box cp = Box.createVerticalBox();
		cp.setBorder(null);
		add(cp, BorderLayout.NORTH);

		Box box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(msg.
				getString("Options.General.Section.General")));
		cp.add(box);
		cp.add(Box.createVerticalStrut(5));

		enabledCB = createCB("Options.General.EnableCodeCompletion");
		addLeftAligned(box, enabledCB);

		Box box2 = Box.createVerticalBox();
		if (o.isLeftToRight()) {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		}
		else {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
		}
		box.add(box2);

		showDescWindowCB = createCB("Options.General.ShowDescWindow");
		addLeftAligned(box2, showDescWindowCB);

		useSystemManCB = createCB("UseSystemManPages");
		addLeftAligned(box2, useSystemManCB);

		box2.add(Box.createVerticalGlue());

		cp.add(Box.createVerticalStrut(5));
		rdButton = new RButton(msg.getString("Options.General.RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(cp, rdButton);

		cp.add(Box.createVerticalGlue());

		applyComponentOrientation(o);

	}


	private void addLeftAligned(Box to, Component c) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(c, BorderLayout.LINE_START);
		to.add(panel);
		to.add(Box.createVerticalStrut(5));
	}


	private JCheckBox createCB(String key) {
		if (key.indexOf('.')==-1) {
			key = "Options.Sh." + key;
		}
		JCheckBox cb = new JCheckBox(Plugin.msg.getString(key));
		cb.addActionListener(listener);
		return cb;
	}


	/**
	 * {@inheritDoc}
	 */
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
		return enabledCB;
	}


	private void setEnabledCBSelected(boolean selected) {
		enabledCB.setSelected(selected);
		showDescWindowCB.setEnabled(selected);
		useSystemManCB.setEnabled(selected  && File.separatorChar=='/');
	}


	/**
	 * {@inheritDoc}
	 */
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

		public void actionPerformed(ActionEvent e) {

			boolean defaultShowDescWindow = File.separatorChar=='/';
			boolean defaultSystemManSelected = File.separatorChar=='/';

			Object source = e.getSource();

			if (enabledCB==source) {
				// Trick related components to toggle enabled states
				setEnabledCBSelected(enabledCB.isSelected());
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (showDescWindowCB==source) {
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (rdButton==source) {
				if (!enabledCB.isSelected() ||
						showDescWindowCB.isSelected()!=defaultShowDescWindow ||
						useSystemManCB.isSelected()!=defaultSystemManSelected) {
					setEnabledCBSelected(true);
					showDescWindowCB.setSelected(true);
					useSystemManCB.setSelected(defaultSystemManSelected);
					hasUnsavedChanges = true;
					firePropertyChange(PROPERTY, null, null);
				}
			}

		}

	}


}