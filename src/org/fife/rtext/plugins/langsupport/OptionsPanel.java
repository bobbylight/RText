/*
 * 05/20/2010
 *
 * OptionsPanel.java - The main options panel for Java language support.
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
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import org.fife.rsta.ac.java.JavaCellRenderer;
import org.fife.ui.RButton;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.autocomplete.CompletionCellRenderer;


/**
 * The main options dialog panel for language options.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class OptionsPanel extends PluginOptionsDialogPanel {

	private JCheckBox altColorCB;
	private RColorSwatchesButton altColorButton;
	private RButton rdButton;

	private static final Color DEFAULT_ALT_ROW_COLOR	= new Color(0xf4f4f4);
	private static final String PROPERTY				= "Property";


	public OptionsPanel(Plugin plugin) {

		super(plugin);
		URL url = getClass().getResource("comment.png");
		setIcon(new ImageIcon(url));

		ResourceBundle msg = Plugin.msg;
		setName(msg.getString("Name"));
		Listener listener = new Listener();

		ComponentOrientation o = ComponentOrientation.
											getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		Box cp = Box.createVerticalBox();
		add(cp, BorderLayout.NORTH);

		SelectableLabel label = new SelectableLabel();
		label.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
		label.setText(msg.getString("Options.Main.Label"));
		label.addHyperlinkListener(listener);
		cp.add(label);
		cp.add(Box.createVerticalStrut(5));

		Box temp = Box.createVerticalBox();
		temp.setBorder(new OptionPanelBorder(msg.getString("Options.Main.Section")));

		altColorCB = new JCheckBox(msg.getString("Options.Main.AlternateColor"));
		altColorCB.addActionListener(listener);
		altColorButton = new RColorSwatchesButton();
		altColorButton.addActionListener(listener);
		Box temp2 = Box.createHorizontalBox();
		temp2.add(altColorCB);
		temp2.add(Box.createHorizontalStrut(5));
		temp2.add(altColorButton);
		temp2.add(Box.createHorizontalGlue());
		temp.add(temp2);
		cp.add(temp);
		cp.add(Box.createVerticalStrut(10));

		rdButton = new RButton(msg.getString("Options.General.RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(cp, rdButton);

		cp.add(Box.createVerticalGlue());

		applyComponentOrientation(o);

		// Language-specific child panels
		addChildPanel(new COptionsPanel());
		addChildPanel(new HtmlOptionsPanel());
		addChildPanel(new JavaOptionsPanel());
		addChildPanel(new PerlOptionsPanel());
		addChildPanel(new PhpOptionsPanel());
		addChildPanel(new ShellOptionsPanel());

	}


	private void addLeftAligned(Box to, Component c) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(c, BorderLayout.LINE_START);
		to.add(panel);
		to.add(Box.createVerticalStrut(5));
	}


	/**
	 * {@inheritDoc}
	 */
	protected void doApplyImpl(Frame owner) {
		Color c = altColorCB.isSelected() ? altColorButton.getColor() : null;
		// All cell renderers except Java's are CompletionCellRenderers.
		CompletionCellRenderer.setAlternateBackground(c);
		JavaCellRenderer.setAlternateBackground(c);
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
		return altColorCB;
	}


	/**
	 * {@inheritDoc}
	 */
	protected void setValuesImpl(Frame owner) {

		Color altColor = CompletionCellRenderer.getAlternateBackground();
		if (altColor==null) {
			altColorCB.setSelected(false);
			altColorButton.setColor(DEFAULT_ALT_ROW_COLOR);
			altColorButton.setEnabled(false);
		}
		else {
			altColorCB.setSelected(true);
			altColorButton.setColor(altColor);
		}

	}


	/**
	 * Listens for events in this panel.
	 */
	private class Listener implements ActionListener, HyperlinkListener {

		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (altColorCB==source) {
				altColorButton.setEnabled(altColorCB.isSelected());
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (altColorButton==source) {
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (rdButton==source) {

				if (altColorCB.isSelected() || !DEFAULT_ALT_ROW_COLOR.
						equals(altColorButton.getColor())) {
					altColorCB.setSelected(false);
					altColorButton.setEnabled(false);
					altColorButton.setColor(DEFAULT_ALT_ROW_COLOR);
					hasUnsavedChanges = true;
					firePropertyChange(PROPERTY, null, null);
				}

			}

		}

		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType()==EventType.ACTIVATED) {
				if (!UIUtil.browse(e.getURL().toExternalForm())) {
					UIManager.getLookAndFeel().provideErrorFeedback(
													OptionsPanel.this);
				}
			}
		}

	}


}