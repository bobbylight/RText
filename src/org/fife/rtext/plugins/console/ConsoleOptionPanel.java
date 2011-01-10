/*
 * 12/22/2010
 *
 * ConsoleOptionPanel.java - Option panel for managing the Console plugin.
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
package org.fife.rtext.plugins.console;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.fife.ui.RButton;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.UIUtil;
import org.fife.ui.app.PluginOptionsDialogPanel;


/**
 * Options panel for managing the Console plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ConsoleOptionPanel extends PluginOptionsDialogPanel
			implements ActionListener, ItemListener, PropertyChangeListener {

	private JCheckBox visibleCB;
	private JComboBox locationCombo;
	private RColorSwatchesButton stdoutButton;
	private RColorSwatchesButton stderrButton;
	private RColorSwatchesButton promptButton;
	private RColorSwatchesButton exceptionsButton;
	private RButton defaultsButton;

	private static final String PROPERTY = "Property";


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin.
	 */
	public ConsoleOptionPanel(Plugin plugin) {

		super(plugin);
		setName(plugin.getString("Options.Title"));
		ComponentOrientation o = ComponentOrientation.
										getOrientation(getLocale());

		// Set up our border and layout.
		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());
		Box topPanel = Box.createVerticalBox();
		add(topPanel, BorderLayout.NORTH);

		// Add the "general" options panel.
		Container generalPanel = createGeneralPanel();
		topPanel.add(generalPanel);
		topPanel.add(Box.createVerticalStrut(5));

		// Add the "colors" option panel.
		Container colorsPanel = createColorsPanel();
		topPanel.add(colorsPanel);
		topPanel.add(Box.createVerticalStrut(5));

		// Add a "Restore Defaults" button
		defaultsButton = new RButton(plugin.getString("RestoreDefaults"));
		defaultsButton.setActionCommand("RestoreDefaults");
		defaultsButton.addActionListener(this);
		JPanel temp = new JPanel(new BorderLayout());
		temp.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
		temp.add(defaultsButton, BorderLayout.LINE_START);
		topPanel.add(temp);

		// Put it all together!
		topPanel.add(Box.createVerticalGlue());
		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(o);

	}


	/**
	 * Called when the user toggles various properties in this panel.
	 *
	 * @param e The event.
	 */
	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();

		if (visibleCB==source) {
			hasUnsavedChanges = true;
			boolean visible = visibleCB.isSelected();
			firePropertyChange(PROPERTY, !visible, visible);
		}

		else if (defaultsButton==source) {
			if (notDefaults()) {
				restoreDefaults();
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, false, true);
			}
		}

	}


	/**
	 * Creates the "Colors" section of options for this plugin.
	 *
	 * @return A panel with the "color" options.
	 */
	private Container createColorsPanel() {

		Box temp = Box.createVerticalBox();

		Plugin plugin = (Plugin)getPlugin();
		temp.setBorder(new OptionPanelBorder(
									plugin.getString("Options.Colors")));

		JLabel stdoutLabel = new JLabel(plugin.getString("Color.Stdout"));
		stdoutButton = createColorSwatchesButton();
		JLabel stderrLabel = new JLabel(plugin.getString("Color.Stderr"));
		stderrButton = createColorSwatchesButton();
		JLabel promptLabel = new JLabel(plugin.getString("Color.Prompts"));
		promptButton = createColorSwatchesButton();
		JLabel excLabel = new JLabel(plugin.getString("Color.Exceptions"));
		exceptionsButton = createColorSwatchesButton();

		JPanel sp = new JPanel(new SpringLayout());
		if (getComponentOrientation().isLeftToRight()) {
			sp.add(stdoutLabel); sp.add(stdoutButton);
			sp.add(stderrLabel); sp.add(stderrButton);
			sp.add(promptLabel); sp.add(promptButton);
			sp.add(excLabel);    sp.add(exceptionsButton);
		}
		else {
			sp.add(stdoutButton); sp.add(stdoutLabel);
			sp.add(stderrButton); sp.add(stderrLabel);
			sp.add(promptButton); sp.add(promptLabel);
			sp.add(exceptionsButton); sp.add(excLabel);
		}
		UIUtil.makeSpringCompactGrid(sp, 4,2, 5,5, 5,5);

		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(sp, BorderLayout.LINE_START);
		temp.add(temp2);
		temp.add(Box.createVerticalGlue());

		return temp;

	}


	/**
	 * Creates a color swatch button we're listening for changes on.
	 *
	 * @return The button.
	 */
	private RColorSwatchesButton createColorSwatchesButton() {
		RColorSwatchesButton button = new RColorSwatchesButton();
		button.addPropertyChangeListener(
				RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		return button;
	}


	/**
	 * Creates the "General" section of options for this plugin.
	 *
	 * @return A panel with the "general" options.
	 */
	private Container createGeneralPanel() {

		Plugin plugin = (Plugin)getPlugin();
		ResourceBundle gpb = ResourceBundle.getBundle(
										"org.fife.ui.app.GUIPlugin");

		Box temp = Box.createVerticalBox();
		temp.setBorder(new OptionPanelBorder(
									plugin.getString("Options.General")));

		// A check box toggling the plugin's visibility.
		visibleCB = new JCheckBox(gpb.getString("Visible"));
		visibleCB.addActionListener(this);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(visibleCB, BorderLayout.LINE_START);
		temp.add(temp2);
		temp.add(Box.createVerticalStrut(5));

		// A combo in which to select the dockable window's placement.
		Box locationPanel = Box.createHorizontalBox();
		locationCombo = new JComboBox();
		UIUtil.fixComboOrientation(locationCombo);
		locationCombo.addItem(gpb.getString("Location.top"));
		locationCombo.addItem(gpb.getString("Location.left"));
		locationCombo.addItem(gpb.getString("Location.bottom"));
		locationCombo.addItem(gpb.getString("Location.right"));
		locationCombo.addItem(gpb.getString("Location.floating"));
		locationCombo.addItemListener(this);
		JLabel locLabel = new JLabel(gpb.getString("Location.title"));
		locLabel.setLabelFor(locationCombo);
		locationPanel.add(locLabel);
		locationPanel.add(Box.createHorizontalStrut(5));
		locationPanel.add(locationCombo);
		locationPanel.add(Box.createHorizontalGlue());
		temp.add(locationPanel);

		temp.add(Box.createVerticalGlue());
		return temp;

	}


	/**
	 * {@inheritDoc}
	 */
	protected void doApplyImpl(Frame owner) {

		Plugin plugin = (Plugin)getPlugin();
		ConsoleWindow window = plugin.getDockableWindow();
		window.setActive(visibleCB.isSelected());
		window.setPosition(locationCombo.getSelectedIndex());

		window.setForeground(ConsoleTextArea.STYLE_EXCEPTION, exceptionsButton.getColor());
		window.setForeground(ConsoleTextArea.STYLE_PROMPT, promptButton.getColor());
		window.setForeground(ConsoleTextArea.STYLE_STDOUT, stdoutButton.getColor());
		window.setForeground(ConsoleTextArea.STYLE_STDERR, stderrButton.getColor());

	}


	/**
	 * Always returns <code>null</code>, as the user cannot enter invalid
	 * input on this panel.
	 *
	 * @return <code>null</code> always.
	 */
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	public JComponent getTopJComponent() {
		return visibleCB;
	}


	/**
	 * Called when the user changes the desired location of the dockable
	 * window.
	 *
	 * @param e The event.
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource()==locationCombo &&
				e.getStateChange()==ItemEvent.SELECTED) {
			hasUnsavedChanges = true;
			int placement = locationCombo.getSelectedIndex();
			firePropertyChange(PROPERTY, -1, placement);
		}
	}


	/**
	 * Returns whether something on this panel is NOT set to its default value.
	 *
	 * @return Whether some property in this panel is NOT set to its default
	 * value.
	 */
	private boolean notDefaults() {
		return !visibleCB.isSelected() ||
			locationCombo.getSelectedIndex()!=2 ||
			!ConsoleTextArea.DEFAULT_STDOUT_FG.equals(stdoutButton.getColor()) ||
			!ConsoleTextArea.DEFAULT_STDERR_FG.equals(stderrButton.getColor()) ||
			!ConsoleTextArea.DEFAULT_PROMPT_FG.equals(promptButton.getColor()) ||
			!ConsoleTextArea.DEFAULT_EXCEPTION_FG.equals(exceptionsButton.getColor());
	}


	/**
	 * Called when one of our color swatch buttons is modified.
	 *
	 * @param e The event.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(PROPERTY, false, true);
	}


	/**
	 * Restores all properties on this panel to their default values.
	 */
	private void restoreDefaults() {

		visibleCB.setSelected(true);
		locationCombo.setSelectedIndex(2);

		stdoutButton.setColor(ConsoleTextArea.DEFAULT_STDOUT_FG);
		stderrButton.setColor(ConsoleTextArea.DEFAULT_STDERR_FG);
		promptButton.setColor(ConsoleTextArea.DEFAULT_PROMPT_FG);
		exceptionsButton.setColor(ConsoleTextArea.DEFAULT_EXCEPTION_FG);

	}


	/**
	 * {@inheritDoc}
	 */
	protected void setValuesImpl(Frame owner) {

		Plugin plugin = (Plugin)getPlugin();
		ConsoleWindow window = plugin.getDockableWindow();
		visibleCB.setSelected(window.isActive());
		locationCombo.setSelectedIndex(window.getPosition());

		stdoutButton.setColor(window.getForeground(ConsoleTextArea.STYLE_STDOUT));
		stderrButton.setColor(window.getForeground(ConsoleTextArea.STYLE_STDERR));
		promptButton.setColor(window.getForeground(ConsoleTextArea.STYLE_PROMPT));
		exceptionsButton.setColor(window.getForeground(ConsoleTextArea.STYLE_EXCEPTION));

	}


}