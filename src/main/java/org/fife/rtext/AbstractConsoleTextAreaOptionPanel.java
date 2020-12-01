/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.UIUtil;
import org.fife.ui.app.Plugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.console.AbstractConsoleTextArea;

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
 * @param <T> The type of plugin this option panel is for.
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class AbstractConsoleTextAreaOptionPanel<T extends Plugin>
		extends PluginOptionsDialogPanel<T>
	implements ActionListener, ItemListener, PropertyChangeListener {

	protected JCheckBox visibleCB;
	protected JLabel locationLabel;
	protected JComboBox<String> locationCombo;

	protected JCheckBox stdoutCB;
	protected JCheckBox stderrCB;
	protected JCheckBox promptCB;
	protected JCheckBox exceptionsCB;
	protected RColorSwatchesButton stdoutButton;
	protected RColorSwatchesButton stderrButton;
	protected RColorSwatchesButton promptButton;
	protected RColorSwatchesButton exceptionsButton;

	protected JButton defaultsButton;

	protected static final String PROPERTY = "Property";

	private static final ResourceBundle MSG = ResourceBundle.getBundle(
		"org.fife.rtext.RText");


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin.
	 */
	public AbstractConsoleTextAreaOptionPanel(T plugin) {
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
			hasUnsavedChanges = true;
			boolean visible = visibleCB.isSelected();
			firePropertyChange(PROPERTY, !visible, visible);
		}

		else if (exceptionsCB==source) {
			boolean selected = exceptionsCB.isSelected();
			exceptionsButton.setEnabled(selected);
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

		else if (promptCB==source) {
			boolean selected = promptCB.isSelected();
			promptButton.setEnabled(selected);
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

		else if (stderrCB==source) {
			boolean selected = stderrCB.isSelected();
			stderrButton.setEnabled(selected);
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

		else if (stdoutCB==source) {
			boolean selected = stdoutCB.isSelected();
			stdoutButton.setEnabled(selected);
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
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
	 * Provides a hook for subclasses to add content in the "Colors" section,
	 * before the color buttons for stdout, stderr, etc.  The default
	 * implementation does nothing; subclasses can override.
	 *
	 * @param parent The container to add the content in.
	 */
	protected void addExtraColorRelatedContent(Box parent) {
		// Do nothing (comment for Sonar)
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
	 * Creates the "Colors" section of options for this plugin.
	 *
	 * @return A panel with the "color" options.
	 */
	protected Container createColorsPanel() {

		Box temp = Box.createVerticalBox();

		temp.setBorder(new OptionPanelBorder(
			getString("Options.Colors")));

		addExtraColorRelatedContent(temp);

		stdoutCB = createColorActivateCB(getString("Color.Stdout"));
		stdoutButton = createColorSwatchesButton();
		stderrCB = createColorActivateCB(getString("Color.Stderr"));
		stderrButton = createColorSwatchesButton();
		promptCB = createColorActivateCB(getString("Color.Prompts"));
		promptButton = createColorSwatchesButton();
		exceptionsCB = createColorActivateCB(getString("Color.Exceptions"));
		exceptionsButton = createColorSwatchesButton();

		JPanel sp = new JPanel(new SpringLayout());
		if (getComponentOrientation().isLeftToRight()) {
			sp.add(stdoutCB);     sp.add(stdoutButton);
			sp.add(stderrCB);     sp.add(stderrButton);
			sp.add(promptCB);     sp.add(promptButton);
			sp.add(exceptionsCB); sp.add(exceptionsButton);
		}
		else {
			sp.add(stdoutButton);     sp.add(stdoutCB);
			sp.add(stderrButton);     sp.add(stderrCB);
			sp.add(promptButton);     sp.add(promptCB);
			sp.add(exceptionsButton); sp.add(exceptionsCB);
		}
		UIUtil.makeSpringCompactGrid(sp, 4,2, 0,0, 5,5);

		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(sp, BorderLayout.LINE_START);
		temp.add(temp2);
		temp.add(Box.createVerticalGlue());

		return temp;

	}


	/**
	 * Creates the "General" section of options for this plugin.
	 *
	 * @return A panel with the "general" options.
	 */
	protected Container createGeneralPanel() {

		ResourceBundle gpb = ResourceBundle.getBundle(
			"org.fife.ui.app.GUIPlugin");

		Box temp = Box.createVerticalBox();
		temp.setBorder(new OptionPanelBorder(
			gpb.getString("Options.General")));

		// A check box toggling the plugin's visibility.
		visibleCB = new JCheckBox(gpb.getString("Visible"));
		visibleCB.addActionListener(this);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(visibleCB, BorderLayout.LINE_START);
		temp.add(temp2);
		temp.add(Box.createVerticalStrut(5));

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
		temp.add(locationPanel);

		temp.add(Box.createVerticalGlue());
		return temp;

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
			hasUnsavedChanges = true;
			int placement = locationCombo.getSelectedIndex();
			firePropertyChange(PROPERTY, -1, placement);
		}
	}


	/**
	 * Returns whether something on this panel is NOT set to its default value.
	 *
	 * @return Whether some property in this panel is NOT set to its default
	 *         value.
	 */
	protected boolean notDefaults() {

		boolean isDark = RTextUtilities.isDarkLookAndFeel();
		Color defaultStdout = isDark ? AbstractConsoleTextArea.DEFAULT_DARK_STDOUT_FG :
			AbstractConsoleTextArea.DEFAULT_LIGHT_STDOUT_FG;
		Color defaultStderr = isDark ? AbstractConsoleTextArea.DEFAULT_DARK_STDERR_FG :
			AbstractConsoleTextArea.DEFAULT_LIGHT_STDERR_FG;
		Color defaultPrompt = isDark ? AbstractConsoleTextArea.DEFAULT_DARK_PROMPT_FG :
			AbstractConsoleTextArea.DEFAULT_LIGHT_PROMPT_FG;
		Color defaultException = isDark ? AbstractConsoleTextArea.DEFAULT_DARK_EXCEPTION_FG :
			AbstractConsoleTextArea.DEFAULT_LIGHT_EXCEPTION_FG;

		return !visibleCB.isSelected() ||
			locationCombo.getSelectedIndex()!=2 ||
			!stdoutCB.isSelected() ||
			!stderrCB.isSelected() ||
			!promptCB.isSelected() ||
			!exceptionsCB.isSelected() ||
			!defaultStdout.equals(stdoutButton.getColor()) ||
			!defaultStderr.equals(stderrButton.getColor()) ||
			!defaultPrompt.equals(promptButton.getColor()) ||
			!defaultException.equals(exceptionsButton.getColor());
	}


	/**
	 * Called when one of our color picker buttons is modified.
	 *
	 * @param e The event.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(PROPERTY, false, true);
	}


	/**
	 * Restores all properties on this panel to their default values.
	 */
	protected void restoreDefaults() {
		setVisibleCBSelected(true);
		locationCombo.setSelectedIndex(2);
		restoreDefaultsForColorsPanel();
	}


	/**
	 * Restores defaults values for all widgets in the "Colors" panel.
	 */
	protected void restoreDefaultsForColorsPanel() {

		stdoutCB.setSelected(true);
		stderrCB.setSelected(true);
		promptCB.setSelected(true);
		exceptionsCB.setSelected(true);
		stdoutButton.setEnabled(true);
		stderrButton.setEnabled(true);
		promptButton.setEnabled(true);
		exceptionsButton.setEnabled(true);

		boolean isDark = RTextUtilities.isDarkLookAndFeel();
		if (isDark) {
			stdoutButton.setColor(AbstractConsoleTextArea.DEFAULT_DARK_STDOUT_FG);
			stderrButton.setColor(AbstractConsoleTextArea.DEFAULT_DARK_STDERR_FG);
			promptButton.setColor(AbstractConsoleTextArea.DEFAULT_DARK_PROMPT_FG);
			exceptionsButton.setColor(AbstractConsoleTextArea.DEFAULT_DARK_EXCEPTION_FG);
		}
		else {
			stdoutButton.setColor(AbstractConsoleTextArea.DEFAULT_LIGHT_STDOUT_FG);
			stderrButton.setColor(AbstractConsoleTextArea.DEFAULT_LIGHT_STDERR_FG);
			promptButton.setColor(AbstractConsoleTextArea.DEFAULT_LIGHT_PROMPT_FG);
			exceptionsButton.setColor(AbstractConsoleTextArea.DEFAULT_LIGHT_EXCEPTION_FG);
		}
	}


	protected void setVisibleCBSelected(boolean selected) {
		visibleCB.setSelected(selected);
		locationLabel.setEnabled(selected);
		locationCombo.setEnabled(selected);
	}


}
