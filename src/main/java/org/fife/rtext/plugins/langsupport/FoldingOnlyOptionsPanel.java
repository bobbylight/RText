/*
 * 12/18/2011
 *
 * FoldingOnlyOptionsPanel.java - Folding options panel.
 * Copyright (C) 2011 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.Border;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;


/**
 * Options panel containing only code folding options.  Useful for languages
 * that only provide folding support but not code completion.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FoldingOnlyOptionsPanel extends OptionsDialogPanel {

	private final String language;
	private final JCheckBox enabledCB;
	private final JButton rdButton;
	private final Listener listener;

	private static final String DEFAULT_PANEL_NAME = "Options.General.Folding.Name";

	/**
	 * Constructor.
	 */
	FoldingOnlyOptionsPanel(RText app, String language) {
		this(app, language, true);
	}


	/**
	 * Constructor.
	 */
	FoldingOnlyOptionsPanel(RText app, String language, boolean showIcon) {
		this(app, DEFAULT_PANEL_NAME, language, showIcon);
	}


	/**
	 * Constructor.
	 */
	FoldingOnlyOptionsPanel(RText app, String nameKey, String language) {
		this(app, nameKey, language, true);
	}


	/**
	 * Constructor.
	 */
	FoldingOnlyOptionsPanel(RText app, String nameKey, String language, boolean showIcon) {

		this.language = language;
		ResourceBundle msg = Plugin.MSG;
		setName(msg.getString(nameKey));
		listener = new Listener();

		// Load the proper icon, and listen for theme changes to update the icon
		// accordingly.
		if (showIcon) {
			String langName = language.substring(language.lastIndexOf('/') + 1);
			String image = "fileTypes/" + langName;
			setIcon(app.getIconGroup().getIcon(image));
			app.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, e -> {
				setIcon(app.getIconGroup().getIcon(image));
			});
		}

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
		rdButton = new JButton(app.getString("RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(cp, rdButton, 5);

		cp.add(Box.createVerticalGlue());

		applyComponentOrientation(o);

	}


	private JCheckBox createCB(String key) {
		JCheckBox cb = new JCheckBox(Plugin.MSG.getString(key));
		cb.addActionListener(listener);
		return cb;
	}


	private void doApplyCodeFoldingPreference(RText rtext) {
		AbstractMainView view = rtext.getMainView();
		view.setCodeFoldingEnabledFor(language, enabledCB.isSelected());
	}


	/**
	 * Applies the selected code folding preference.  Subclasses can override
	 * if they display more options.
	 *
	 * @param owner The parent {@link RText} application.
	 */
	@Override
	protected final void doApplyImpl(Frame owner) {
		doApplyCodeFoldingPreference((RText)owner);
	}


	/**
	 * The default implementation always returns <code>null</code>.  Subclasses
	 * can override if they add more than just check boxes to the panel.
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	@Override
	public JComponent getTopJComponent() {
		return enabledCB;
	}


	private void setCodeFoldingValueImpl(RText rtext) {
		AbstractMainView view = rtext.getMainView();
		enabledCB.setSelected(view.isCodeFoldingEnabledFor(language));
	}


	/**
	 * Checks or unchecks the "enable code folding" check box.  Subclasses can
	 * override this if they add more options to this panel.
	 */
	@Override
	protected final void setValuesImpl(Frame owner) {
		setCodeFoldingValueImpl((RText)owner);
	}


	/**
	 * Listens for events in this options panel.
	 */
	private class Listener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (enabledCB==source) {
				setEnabledCBSelected(enabledCB.isSelected());
				setDirty(true);
			}

			else if (rdButton==source &&
					!enabledCB.isSelected()) {
				setEnabledCBSelected(true);
				setDirty(true);
			}

		}

		private void setEnabledCBSelected(boolean selected) {
			enabledCB.setSelected(selected); // Should be a no-op
			// Toggle enabled state of any other check boxes.
		}

	}


}
