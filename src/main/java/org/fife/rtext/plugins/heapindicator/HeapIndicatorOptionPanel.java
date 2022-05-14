/*
 * 09/19/2005
 *
 * HeapIndicatorOptionPanel.java - Option panel for the heap indicator.
 * Copyright (C) 2005 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.heapindicator;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.UIUtil;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.PluginOptionsDialogPanel;


/**
 * Option panel for the {@link HeapIndicatorPlugin} plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class HeapIndicatorOptionPanel extends PluginOptionsDialogPanel<HeapIndicatorPlugin>
					implements ActionListener, PropertyChangeListener,
							ChangeListener {

	private final JCheckBox visibilityCheckBox;
	private final JSpinner refreshRateSpinner;


	/**
	 * Constructor.
	 */
	HeapIndicatorOptionPanel(AbstractPluggableGUIApplication<?> app,
							 HeapIndicatorPlugin plugin) {

		super(plugin);
		ResourceBundle msg = plugin.getBundle();
		setName(plugin.getPluginName());

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		Border empty5Border = UIUtil.getEmpty5Border();
		setLayout(new BorderLayout());
		setBorder(empty5Border);

		// A panel to contain everything that will go into our "top" area.
		Box topPanel = Box.createVerticalBox();
		Box topPanel2 = Box.createVerticalBox();
		topPanel2.setBorder(BorderFactory.createCompoundBorder(
				new OptionPanelBorder(msg.getString(
								"Plugin.OptionPanel.Title.General")),
				empty5Border));

		// Panel to toggle the indicator's visibility.
		JPanel temp = new JPanel(new BorderLayout());
		visibilityCheckBox = new JCheckBox(
			msg.getString("Plugin.OptionPanel.Visibility.text"));
		visibilityCheckBox.setMnemonic((int)msg.getString(
			"Plugin.OptionPanel.Visibility.mnemonic").charAt(0));
		visibilityCheckBox.addActionListener(this);
		temp.add(visibilityCheckBox, BorderLayout.LINE_START);
		topPanel2.add(temp);
		topPanel2.add(Box.createVerticalStrut(5));

		// Panel for the indicator's refresh rate.
		temp = new JPanel(new BorderLayout());
		Box temp2 = new Box(BoxLayout.LINE_AXIS);
		JLabel label=UIUtil.newLabel(msg, "Plugin.OptionPanel.RefreshRate");
		refreshRateSpinner = new JSpinner(new SpinnerNumberModel(1,1,600,1));
		label.setLabelFor(refreshRateSpinner);
		refreshRateSpinner.addChangeListener(this);
		temp2.add(label);
		temp2.add(Box.createHorizontalStrut(5));
		temp2.add(refreshRateSpinner);
		temp2.add(Box.createHorizontalGlue());
		temp.add(temp2, BorderLayout.LINE_START);
		topPanel2.add(temp);
		topPanel.add(topPanel2);

		// Put it all together!
		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(orientation);

	}


	/**
	 * Called when an action is performed in this option panel.
	 *
	 * @param e The action event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();

		if (source==visibilityCheckBox) {
			setDirty(true);
		}

	}


	/**
	 * Updates the heap indicator's parameters to reflect those in
	 * this options panel.
	 */
	@Override
	protected void doApplyImpl(Frame owner) {
		HeapIndicatorPlugin p = getPlugin();
		p.setVisible(visibilityCheckBox.isSelected());
		int refresh = (Integer)refreshRateSpinner.getValue();
		p.setRefreshInterval(refresh*1000);
	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		OptionsPanelCheckResult res = null;
		// I don't think JSpinner allows invalid input, but just in case...
		Number number = (Number)refreshRateSpinner.getValue();
		if (!(number instanceof Integer)) {
			ResourceBundle msg = getPlugin().getBundle();
			String error = msg.getString(
							"Plugin.OptionPanel.Error.RefreshRate.text");
			error = MessageFormat.format(error, refreshRateSpinner.getValue());
			res = new OptionsPanelCheckResult(this,refreshRateSpinner,error);
		}
		return res;
	}


	/**
	 * Returns the <code>JComponent</code> at the "top" of this Options
	 * panel.  This is the component that will receive focus if the user
	 * switches to this Options panel in the Options dialog.  As an added
	 * bonus, if this component is a <code>JTextComponent</code>, its
	 * text is selected for easy changing.
	 */
	@Override
	public JComponent getTopJComponent() {
		return visibilityCheckBox;
	}


	/**
	 * Listens for property changes of the color buttons in this panel.
	 *
	 * @param e The property change event.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String name = e.getPropertyName();
		if (name.equals(RColorSwatchesButton.COLOR_CHANGED_PROPERTY)) {
			setDirty(true);
		}
	}


	/**
	 * Updates this panel's displayed parameter values to reflect those of
	 * the heap indicator.
	 */
	@Override
	protected void setValuesImpl(Frame frame) {
		HeapIndicatorPlugin p = getPlugin();
		visibilityCheckBox.setSelected(p.isVisible());
		refreshRateSpinner.setValue(p.getRefreshInterval() / 1000);
	}


	/**
	 * Called when the refresh rate spinner changes.
	 *
	 * @param e The change event.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		setDirty(true);
	}


}
