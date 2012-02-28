/*
 * 12/18/2011
 *
 * FoldingOnlyOptionsPanel.java - Folding options panel.
 * Copyright (C) 2011 Robert Futrell
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
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.border.Border;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RButton;
import org.fife.ui.UIUtil;


/**
 * Options panel containing only code folding options.  Useful for languages
 * that only provide folding support but not code completion.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FoldingOnlyOptionsPanel extends OptionsDialogPanel {

	private String language;
	private JCheckBox enabledCB;
	private RButton rdButton;
	private Listener listener;

	private static final String PROPERTY		= "Property";


	/**
	 * Constructor.
	 */
	public FoldingOnlyOptionsPanel(String icon, String language) {
		this("Options.General.Folding.Name", icon, language);
	}


	/**
	 * Constructor.
	 */
	public FoldingOnlyOptionsPanel(String nameKey, String icon,
			String language) {

		this.language = language;
		ResourceBundle msg = Plugin.msg;
		setName(msg.getString(nameKey));
		if (icon!=null) {
			setIcon(new ImageIcon(getClass().getResource(icon)));
		}
		listener = new Listener();

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
				getString("Options.General.Section.Folding")));
		cp.add(box);
		cp.add(Box.createVerticalStrut(5));

		enabledCB = createCB("Options.General.EnableCodeFolding");
		addLeftAligned(box, enabledCB, 5);

		cp.add(Box.createVerticalStrut(5));
		rdButton = new RButton(msg.getString("Options.General.RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(cp, rdButton, 5);

		cp.add(Box.createVerticalGlue());

		applyComponentOrientation(o);

	}


	protected void doApplyCodeFoldingPreference(RText rtext) {
		AbstractMainView view = rtext.getMainView();
		view.setCodeFoldingEnabledFor(language, enabledCB.isSelected());
	}


	protected JCheckBox createCB(String key) {
		JCheckBox cb = new JCheckBox(Plugin.msg.getString(key));
		cb.addActionListener(listener);
		return cb;
	}


	/**
	 * Applies the selected code folding preference.  Subclasses can override
	 * if they display more options.
	 *
	 * @param owner The parent {@link RText} application.
	 */
	protected final void doApplyImpl(Frame owner) {
		doApplyCodeFoldingPreference((RText)owner);
	}


	/**
	 * The default implementation always returns <code>null</code>.  Subclasses
	 * can override if they add more than just check boxes to the panel.
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


	/**
	 * Checks or unchecks the "enable code folding" check box.  Subclasses can
	 * override this if they add more options to this panel.
	 */
	protected final void setValuesImpl(Frame owner) {
		setCodeFoldingValueImpl((RText)owner); 
	}


	protected void setCodeFoldingValueImpl(RText rtext) {
		AbstractMainView view = rtext.getMainView();
		enabledCB.setSelected(view.isCodeFoldingEnabledFor(language));
	}


	/**
	 * Listens for events in this options panel.
	 */
	private class Listener implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (enabledCB==source) {
				setEnabledCBSelected(enabledCB.isSelected());
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (rdButton==source &&
					!enabledCB.isSelected()) {
				setEnabledCBSelected(true);
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

		}

		private void setEnabledCBSelected(boolean selected) {
			// TODO: Toggle enabled state of other check boxes.
		}

	}


}