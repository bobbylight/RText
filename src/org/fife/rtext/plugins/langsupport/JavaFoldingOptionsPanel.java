/*
 * 09/29/2011
 *
 * JavaFoldingOptionsPanel.java - Folding options for Java language support.
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
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.border.Border;

import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RButton;
import org.fife.ui.UIUtil;


/**
 * Options panel containing code folding options for Java files.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class JavaFoldingOptionsPanel extends OptionsDialogPanel {

	private JCheckBox enabledCB;
	private RButton rdButton;
	private Listener listener;

	private static final String PROPERTY		= "Property";


	/**
	 * Constructor.
	 */
	public JavaFoldingOptionsPanel() {

		ResourceBundle msg = Plugin.msg;
		setName(msg.getString("Options.Java.Folding.Name"));
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


	private JCheckBox createCB(String key) {
		if (key.indexOf('.')==-1) {
			key = "Options.Java." + key;
		}
		JCheckBox cb = new JCheckBox(Plugin.msg.getString(key));
		cb.addActionListener(listener);
		return cb;
	}


	protected void doApplyImpl(Frame owner) {
		// TODO Auto-generated method stub

	}


	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	public JComponent getTopJComponent() {
		return enabledCB;
	}


	protected void setValuesImpl(Frame owner) {
		// TODO Auto-generated method stub

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

		}

		private void setEnabledCBSelected(boolean selected) {
			// TODO: Toggle enabled state of other check boxes.
		}
	}


}