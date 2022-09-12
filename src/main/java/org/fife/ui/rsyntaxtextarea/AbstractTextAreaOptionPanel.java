/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;


/**
 * A base class for option panels containing editor-related options. It provides
 * a top-level checkbox to toggle whether this panel's options should override
 * default theme values.
 *
 * @author Robert Futrell
 * @version 1.0
 */
abstract class AbstractTextAreaOptionPanel extends OptionsDialogPanel
		implements ActionListener, ItemListener, EditorOptionsPreviewContextListener {

	protected JCheckBox overrideCheckBox;
	protected boolean processingEditorOptionsPreviewContextChanges;

	protected static final int COMPONENT_VERTICAL_SPACING = 3;
	protected static final int SECTION_VERTICAL_SPACING = 5;

	protected static final ResourceBundle MSG = ResourceBundle.getBundle(
		"org.fife.ui.rsyntaxtextarea.TextAreaOptionPanel");
	private static final ResourceBundle DIALOG_MSG = ResourceBundle.getBundle(
		"org.fife.rtext.OptionsDialog");


	AbstractTextAreaOptionPanel() {
		EditorOptionsPreviewContext.get().addListener(this);
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		// If the user clicked the "restore defaults" button.
		if ("RestoreDefaults".equals(command)) {
			handleRestoreDefaults();
		}
	}


	protected JComponent createOverridePanel() {
		overrideCheckBox = UIUtil.newCheckBox(MSG, "OverrideTheme");
		overrideCheckBox.addItemListener(this);
		overrideCheckBox.putClientProperty(UIUtil.PROPERTY_ALWAYS_IGNORE, Boolean.TRUE);
		overrideCheckBox.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		Box temp = Box.createHorizontalBox();
		temp.add(overrideCheckBox);
		temp.add(Box.createHorizontalGlue());
		return temp;
	}


	protected JComponent createRestoreDefaultsPanel() {
		Box rdPanel = createHorizontalBox();
		JButton rdButton = new JButton(MSG.getString("RestoreDefaults"));
		rdButton.setActionCommand("RestoreDefaults");
		rdButton.addActionListener(this);
		rdButton.putClientProperty(UIUtil.PROPERTY_ALWAYS_IGNORE, Boolean.TRUE);
		rdPanel.add(rdButton);
		rdPanel.add(Box.createHorizontalGlue());
		return rdPanel;
	}


	@Override
	public final void editorOptionsPreviewContextChanged(EditorOptionsPreviewContext context) {

		processingEditorOptionsPreviewContextChanges = true;
		try {
			editorOptionsPreviewContextChangedImpl(context);
		} finally {
			processingEditorOptionsPreviewContextChanges = false;
		}
	}


	protected void editorOptionsPreviewContextChangedImpl(EditorOptionsPreviewContext context) {
		// If the "override theme styles" checkbox was toggled, update things here
		boolean overrideEditorTheme = context.getOverrideEditorTheme();
		if (overrideCheckBox != null && overrideCheckBox.isSelected() != overrideEditorTheme) {
			overrideCheckBox.setSelected(overrideEditorTheme);
		}
	}


	/**
	 * Looks up a localized string. First queries the resource bundle
	 * for text area-related options panels, then for the parent Options
	 * dialog itself (for general/high-level strings).
	 *
	 * @param key The key to look up.
	 * @return The localized value.
	 */
	protected String getString(String key) {
		if (MSG.containsKey(key)) {
			return MSG.getString(key);
		}
		return DIALOG_MSG.getString(key);
	}


	@Override
	public JComponent getTopJComponent() {
		return overrideCheckBox;
	}


	protected abstract void handleRestoreDefaults();


	@Override
	public void itemStateChanged(ItemEvent e) {

		Object source = e.getItemSelectable();

		if (overrideCheckBox == source) {
			boolean overrideTheme = e.getStateChange() == ItemEvent.SELECTED;
			setOverrideTheme(overrideTheme);
		}
	}


	protected void setComponentsEnabled(boolean enabled, Component... ignore) {
		UIUtil.setComponentsEnabled(this, enabled, ignore);
	}


	@Override
	public void setDirty(boolean dirty) {
		// We do this even if dirty isn't changing to ensure the
		// preview panel is kept in sync
		if (dirty && !getOptionsDialog().isInitializing() &&
				!processingEditorOptionsPreviewContextChanges) {
			syncEditorOptionsPreviewContext();
		}
		super.setDirty(dirty);
	}


	/**
	 * Updates the sample area to use either the current application theme or the
	 * selected values in this dialog.
	 *
	 * @param overrideTheme Whether to override (vs. use) the application theme settings.
	 */
	private void setOverrideTheme(boolean overrideTheme) {
		setComponentsEnabled(overrideTheme);
		setDirty(true);
	}


	/**
	 * Ensures the values in the shared editor option contexdt are sync'd with
	 * the values in this options panel.
	 */
	protected abstract void syncEditorOptionsPreviewContext();
}
