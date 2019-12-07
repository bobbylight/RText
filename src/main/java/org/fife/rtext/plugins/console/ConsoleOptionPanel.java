/*
 * 12/22/2010
 *
 * ConsoleOptionPanel.java - Option panel for managing the Console plugin.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;

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

	private JCheckBox highlightInputCB;


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

		// Add the "colors" option panel.
		Container colorsPanel = createColorsPanel();
		topPanel.add(colorsPanel);
		topPanel.add(Box.createVerticalStrut(5));

		addRestoreDefaultsButton(topPanel);

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
	@Override
	public void actionPerformed(ActionEvent e) {

		// Parent class listens to color buttons and "Restore Defaults"
		super.actionPerformed(e);

		Object source = e.getSource();

		if (highlightInputCB==source) {
			boolean selected = highlightInputCB.isSelected();
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

	}


	/**
	 * Overridden to add our "syntax highlight user input" checkbox.
	 *
	 * @param parent The container to add the content in.
	 */
	@Override
	protected void addExtraColorRelatedContent(Box parent) {
		highlightInputCB = createColorActivateCB(
			getPlugin().getString("Highlight.Input"));
		addLeftAligned(parent, highlightInputCB);
	}


	@Override
	protected void doApplyImpl(Frame owner) {

		Plugin plugin = getPlugin();
		ConsoleWindow window = plugin.getDockableWindow();
		window.setActive(visibleCB.isSelected());
		window.setPosition(locationCombo.getSelectedIndex());

		plugin.setSyntaxHighlightInput(highlightInputCB.isSelected());

		Color c = exceptionsCB.isSelected() ? exceptionsButton.getColor() : null;
		window.setForeground(ConsoleTextArea.STYLE_EXCEPTION, c);
		c = promptCB.isSelected() ? promptButton.getColor() : null;
		window.setForeground(ConsoleTextArea.STYLE_PROMPT, c);
		c = stdoutCB.isSelected() ? stdoutButton.getColor() : null;
		window.setForeground(ConsoleTextArea.STYLE_STDOUT, c);
		c = stderrCB.isSelected() ? stderrButton.getColor() : null;
		window.setForeground(ConsoleTextArea.STYLE_STDERR, c);

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
	protected boolean notDefaults() {
		return super.notDefaults() || !highlightInputCB.isSelected();
	}


	/**
	 * Overridden to set all colors to values appropriate for the current Look
	 * and Feel.
	 *
	 * @param event The broadcasted event.
	 */
	@Override
	public void optionsEvent(String event) {
		restoreDefaultColors();
		super.optionsEvent(event);
	}


	/**
	 * Changes all consoles to use the default colors for the current
	 * application theme.
	 */
	private void restoreDefaultColors() {
		Plugin plugin = getPlugin();
		plugin.restoreDefaultColors();
		setValues(plugin.getRText());
	}


	@Override
	protected void restoreDefaults() {
		super.restoreDefaults();
		highlightInputCB.setSelected(true);
	}


	@Override
	protected void setValuesImpl(Frame owner) {

		Plugin plugin = getPlugin();
		ConsoleWindow window = plugin.getDockableWindow();
		visibleCB.setSelected(window.isActive());
		locationCombo.setSelectedIndex(window.getPosition());

		highlightInputCB.setSelected(plugin.getSyntaxHighlightInput());
		stdoutCB.setSelected(window.isStyleUsed(ConsoleTextArea.STYLE_STDOUT));
		stdoutButton.setEnabled(window.isStyleUsed(ConsoleTextArea.STYLE_STDOUT));
		stderrCB.setSelected(window.isStyleUsed(ConsoleTextArea.STYLE_STDERR));
		stderrButton.setEnabled(window.isStyleUsed(ConsoleTextArea.STYLE_STDERR));
		promptCB.setSelected(window.isStyleUsed(ConsoleTextArea.STYLE_PROMPT));
		promptButton.setEnabled(window.isStyleUsed(ConsoleTextArea.STYLE_PROMPT));
		exceptionsCB.setSelected(window.isStyleUsed(ConsoleTextArea.STYLE_EXCEPTION));
		exceptionsButton.setEnabled(window.isStyleUsed(ConsoleTextArea.STYLE_EXCEPTION));

		stdoutButton.setColor(window.getForeground(ConsoleTextArea.STYLE_STDOUT));
		stderrButton.setColor(window.getForeground(ConsoleTextArea.STYLE_STDERR));
		promptButton.setColor(window.getForeground(ConsoleTextArea.STYLE_PROMPT));
		exceptionsButton.setColor(window.getForeground(ConsoleTextArea.STYLE_EXCEPTION));

	}


}
