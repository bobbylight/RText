/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.UIUtil;
import org.fife.ui.app.Plugin;
import org.fife.ui.app.PluginOptionsDialogPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;


/**
 * A base class for option panels for plugins that contain a "console" that
 * renders stdout, stderr, etc.  This base class provides the infrastructure
 * to allow the user to customize the styles used in the console.
 *
 * @author Robert Futrell
 * @version 1.0
 * @param <P> The type of plugin this option panel is for.
 */
public abstract class AbstractConsoleTextAreaOptionPanel<P extends Plugin<?>>
		extends PluginOptionsDialogPanel<P>
	implements ActionListener, ItemListener, PropertyChangeListener {

	protected JCheckBox visibleCB;
	protected JLabel locationLabel;
	protected JComboBox<String> locationCombo;

	protected JButton defaultsButton;

	private static final ResourceBundle MSG = ResourceBundle.getBundle(
		"org.fife.rtext.RText");


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin.
	 */
	public AbstractConsoleTextAreaOptionPanel(P plugin) {
		super(plugin);
	}


	/**
	 * Called when the user toggles various properties in this panel.
	 *
	 * @param e The event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();

		if (visibleCB==source) {
			setVisibleCBSelected(visibleCB.isSelected());
			setDirty(true);
		}

		else if (defaultsButton==source) {
			if (notDefaults()) {
				restoreDefaults();
				setDirty(true);
			}
		}
	}


	/**
	 * Adds the "Restore Defaults" button to the UI.
	 *
	 * @param parent The container to add it to.
	 */
	protected void addRestoreDefaultsButton(Container parent) {
		defaultsButton = new JButton(getString("RestoreDefaults"));
		defaultsButton.setActionCommand("RestoreDefaults");
		defaultsButton.addActionListener(this);
		addLeftAligned(parent, defaultsButton);
	}


	/**
	 * Returns a check box used to toggle whether a color in a console uses
	 * a special color.
	 *
	 * @param label The label for the check box.
	 * @return The check box.
	 */
	protected JCheckBox createColorActivateCB(String label) {
		JCheckBox cb = new JCheckBox(label);
		cb.addActionListener(this);
		return cb;
	}


	/**
	 * Creates a color picker button we're listening for changes on.
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
	protected Container createGeneralPanel() {

		ResourceBundle gpb = ResourceBundle.getBundle(
			"org.fife.ui.app.GUIPlugin");

		Box generalPanel = Box.createVerticalBox();
		generalPanel.setBorder(new OptionPanelBorder(
			gpb.getString("Options.General")));

		// A checkbox toggling the plugin's visibility.
		visibleCB = new JCheckBox(gpb.getString("Visible"));
		visibleCB.addActionListener(this);
		addLeftAligned(generalPanel, visibleCB, COMPONENT_VERTICAL_SPACING);

		// A combo in which to select the dockable window's placement.
		Box locationPanel = createHorizontalBox();
		locationCombo = new JComboBox<>();
		UIUtil.fixComboOrientation(locationCombo);
		locationCombo.addItem(gpb.getString("Location.top"));
		locationCombo.addItem(gpb.getString("Location.left"));
		locationCombo.addItem(gpb.getString("Location.bottom"));
		locationCombo.addItem(gpb.getString("Location.right"));
		locationCombo.addItem(gpb.getString("Location.floating"));
		locationCombo.addItemListener(this);
		locationLabel = new JLabel(gpb.getString("Location.title"));
		locationLabel.setLabelFor(locationCombo);
		locationPanel.add(locationLabel);
		locationPanel.add(Box.createHorizontalStrut(5));
		locationPanel.add(locationCombo);
		locationPanel.add(Box.createHorizontalGlue());
		addLeftAligned(generalPanel, locationPanel);

		return generalPanel;

	}


	private String getString(String key) {
		return MSG.getString(key);
	}


	@Override
	public JComponent getTopJComponent() {
		return visibleCB;
	}


	/**
	 * Called when the user changes the desired location of the dockable
	 * window.
	 *
	 * @param e The event.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource()==locationCombo &&
			e.getStateChange()==ItemEvent.SELECTED) {
			setDirty(true);
		}
	}


	/**
	 * Returns whether something on this panel is NOT set to its default value.
	 *
	 * @return Whether some property in this panel is NOT set to its default
	 *         value.
	 */
	protected boolean notDefaults() {
		return !visibleCB.isSelected() ||
			locationCombo.getSelectedIndex()!=2;
	}


	/**
	 * Called when one of our color picker buttons is modified.
	 *
	 * @param e The event.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		setDirty(true);
	}


	/**
	 * Restores all properties on this panel to their default values.
	 */
	protected void restoreDefaults() {
		setVisibleCBSelected(true);
		locationCombo.setSelectedIndex(2);
	}


	protected void setVisibleCBSelected(boolean selected) {
		visibleCB.setSelected(selected);
		locationLabel.setEnabled(selected);
		locationCombo.setEnabled(selected);
	}


}
