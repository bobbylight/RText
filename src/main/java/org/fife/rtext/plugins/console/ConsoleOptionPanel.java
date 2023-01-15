/*
 * 12/22/2010
 *
 * ConsoleOptionPanel.java - Option panel for managing the Console plugin.
 * Copyright (C) 2010 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.console;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionListener;

import javax.swing.Box;

import org.fife.rtext.AbstractConsoleTextAreaOptionPanel;
import org.fife.ui.UIUtil;


/**
 * Options panel for managing the Console plugin.
 *
 * @author Robert Futrell
 * @version 1.1
 */
class ConsoleOptionPanel extends AbstractConsoleTextAreaOptionPanel<Plugin>
			implements ActionListener {

	/**
	 * ID used to identify this option panel.
	 */
	private static final String OPTION_PANEL_ID = "ConsoleOptionPanel";


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin.
	 */
	ConsoleOptionPanel(Plugin plugin) {

		super(plugin);
		setId(OPTION_PANEL_ID);
		setName(plugin.getString("Options.Title"));
		setId(OPTION_PANEL_ID);
		ComponentOrientation o = ComponentOrientation.
										getOrientation(getLocale());

		// Set up our border and layout.
		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());
		Box topPanel = Box.createVerticalBox();

		// Add the "general" options panel.
		Container generalPanel = createGeneralPanel();
		topPanel.add(generalPanel);
		topPanel.add(Box.createVerticalStrut(5));

		addRestoreDefaultsButton(topPanel);

		// Put it all together!
		topPanel.add(Box.createVerticalGlue());
		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(o);

	}


	@Override
	protected void doApplyImpl(Frame owner) {

		Plugin plugin = getPlugin();
		ConsoleWindow window = plugin.getDockableWindow();
		window.setActive(visibleCB.isSelected());
		window.setPosition(locationCombo.getSelectedIndex());

	}


	/**
	 * Always returns <code>null</code>, as the user cannot enter invalid
	 * input on this panel.
	 *
	 * @return <code>null</code> always.
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	@Override
	protected void setValuesImpl(Frame owner) {

		Plugin plugin = getPlugin();
		ConsoleWindow window = plugin.getDockableWindow();
		visibleCB.setSelected(window.isActive());
		locationCombo.setSelectedIndex(window.getPosition());
	}


}
