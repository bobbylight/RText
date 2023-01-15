/*
 * 03/25/2004
 *
 * UIOptionPanel.java - Option panel for overall user interface
 * (non-textarea) related options.
 * Copyright (C) 2004 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.optionsdialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.*;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.*;
import org.fife.ui.app.AppTheme;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaOptionPanel;
import org.fife.ui.rsyntaxtextarea.RTextAreaOptionPanel;


/**
 * Option panel for user-interface specific options (that don't have to
 * do with the text area).
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class UIOptionPanel extends OptionsDialogPanel implements ActionListener,
										PropertyChangeListener {

	/**
	 * ID used to identify this option panel, so others can attach to it.
	 */
	public static final String OPTION_PANEL_ID = "UIOptionPanel";

	private final RText rtext;
	private LabelValueComboBox<String, AppTheme> themeCombo;
	private JButton applyButton;

	private JPanel springPanel;
	private JComboBox<String> viewCombo;
	private JComboBox<String> docSelCombo;

	private JCheckBox showHostNameCheckBox;

	/**
	 * Constructor.
	 */
	UIOptionPanel(RText rtext, ResourceBundle msg) {

		super(msg.getString("OptUIName"));
		setId(OPTION_PANEL_ID);
		this.rtext = rtext;
		Listener listener = new Listener();

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		// Set up our border and layout.
		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		Box cp = Box.createVerticalBox();

		cp.add(createThemePanel(msg, listener));
		cp.add(Box.createVerticalStrut(10));
		cp.add(createLayoutPanel(msg));
		cp.add(createOtherPanel(msg));

		// Add everything "to the north" so the spacing between stuff doesn't
		// change then the user stretches the dialog.
		add(cp, BorderLayout.NORTH);
		applyComponentOrientation(orientation);
	}


	/**
	 * Listens for actions being performed in this panel.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		switch (actionCommand) {
			case "ViewComboBox", "ShowHostNameCB", "AppThemeComboBox" -> setDirty(true);
		}

	}


	private void applySelectedTheme() {

		AppTheme theme = themeCombo.getSelectedValue();

		RText rtext = (RText)getOptionsDialog().getOwner();
		RTextUtilities.setThemeForAllOpenAppInstances(rtext, theme); // Doesn't update if...
		rtext.getMainView().setOverrideEditorStyles(false); // For now any overrides are just lost

		// Refresh other option panels whose properties were affected
		org.fife.ui.OptionsDialog dialog = getOptionsDialog();
		dialog.getPanelById(RTextAreaOptionPanel.OPTION_PANEL_ID).setValues(rtext);
		dialog.getPanelById(RSyntaxTextAreaOptionPanel.OPTION_PANEL_ID).setValues(rtext);
		// Options panels installed by plugins weren't loaded by the same
		// ClassLoader, so we can't reference them directly, but we can
		// broadcast events to them.
		getOptionsDialog().broadcast("appTheme:" + theme);
	}


	/**
	 * Creates and returns a special value combo box containing all available
	 * application themes.
	 *
	 * @return The combo box.
	 */
	private LabelValueComboBox<String, AppTheme> createAppThemeComboBox() {

		LabelValueComboBox<String, AppTheme> combo = new LabelValueComboBox<>();
		UIUtil.fixComboOrientation(combo);

		for (AppTheme theme : rtext.getAppThemes()) {
			combo.addLabelValuePair(theme.getName(), theme);
		}

		return combo;

	}


	private JPanel createLayoutPanel(ResourceBundle msg) {

		ComponentOrientation orientation = ComponentOrientation.
			getOrientation(getLocale());

		viewCombo = new JComboBox<>();
		UIUtil.fixComboOrientation(viewCombo);
		viewCombo.setActionCommand("ViewComboBox");
		viewCombo.addActionListener(this);
		viewCombo.addItem(msg.getString("OptUITV"));
		viewCombo.addItem(msg.getString("OptUISPV"));
		viewCombo.addItem(msg.getString("OptUIMDIV"));

		docSelCombo = new JComboBox<>();
		UIUtil.fixComboOrientation(docSelCombo);
		docSelCombo.setActionCommand("DocSelCombo");
		docSelCombo.addActionListener(this);
		docSelCombo.addItem(msg.getString("OptUITop"));
		docSelCombo.addItem(msg.getString("OptUILeft"));
		docSelCombo.addItem(msg.getString("OptUIBottom"));
		docSelCombo.addItem(msg.getString("OptUIRight"));

		// Add a panel for the "Layout" stuff.
		springPanel = new JPanel(new SpringLayout());
		JPanel temp = new JPanel(new BorderLayout());
		temp.setBorder(new OptionPanelBorder(msg.getString("OptUILT")));
		UIUtil.addLabelValuePairs(springPanel, orientation,
			new JLabel(msg.getString("OptUIViewT")), viewCombo,
			new JLabel(msg.getString("OptUIDSPT")), docSelCombo);
		temp.add(springPanel, BorderLayout.LINE_START);
		UIUtil.makeSpringCompactGrid(springPanel,
			2,2,		// rows,cols,
			0,0,		// initial-x, initial-y,
			5,5);	// x-spacing, y-spacing.

		return temp;
	}


	private Container createOtherPanel(ResourceBundle msg) {

		Box temp = Box.createVerticalBox();
		temp.setBorder(new OptionPanelBorder(msg.getString("OptOtherTitle")));

		JPanel showHostNamePanel = new JPanel(new BorderLayout());
		showHostNameCheckBox = new JCheckBox(msg.getString("OptUIShowHostName"));
		showHostNameCheckBox.setActionCommand("ShowHostNameCB");
		showHostNameCheckBox.addActionListener(this);
		showHostNamePanel.add(showHostNameCheckBox, BorderLayout.LINE_START);
		temp.add(showHostNamePanel);

		temp.add(Box.createVerticalGlue());
		return temp;
	}


	private Container createThemePanel(ResourceBundle msg, Listener listener) {

		Box temp = Box.createVerticalBox();
		temp.setBorder(new OptionPanelBorder(msg.getString("OptThemeLabel")));

		SelectableLabel label = new SelectableLabel();
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 2 * COMPONENT_VERTICAL_SPACING, 0));
		label.setText(msg.getString("OptThemeDesc"));
		temp.add(label, BorderLayout.NORTH);

		themeCombo = createAppThemeComboBox();
		themeCombo.setActionCommand("AppThemeComboBox");
		themeCombo.addActionListener(this);
		UIUtil.fixComboOrientation(themeCombo);
		Box temp2 = createHorizontalBox();
		temp2.add(themeCombo);
		temp2.add(Box.createHorizontalStrut(5));
		applyButton = UIUtil.newButton(msg, "OptThemeApply", listener);
		temp2.add(applyButton);
		temp2.add(Box.createHorizontalGlue());
		temp2.setBorder(BorderFactory.createEmptyBorder(COMPONENT_VERTICAL_SPACING, 0, 0, 0));
		temp.add(temp2);

		return temp;
	}

	/**
	 * Applies the settings entered into this dialog on the specified
	 * application.
	 *
	 * @param owner The application.
	 */
	@Override
	protected void doApplyImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		mainView.setDocumentSelectionPlacement(getDocumentSelectionPlacement()); // Doesn't update if it doesn't have to.
		applySelectedTheme();
		rtext.setMainViewStyle(getMainViewStyle());	// Doesn't update if it doesn't have to.
		rtext.setShowHostName(showHostNameCheckBox.isSelected());	// Doesn't update if doesn't have to.
	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		// They can't input invalid stuff on this options panel.
		return null;
	}


	/**
	 * Returns the document selection placement the user chose.
	 *
	 * @return The document selection placement.
	 * @see #setDocumentSelectionPlacement
	 */
	private int getDocumentSelectionPlacement() {
		return docSelCombo.getSelectedIndex() + JTabbedPane.TOP;
	}


	/**
	 * Returns the main view style the user chose.
	 *
	 * @return The main view style, either <code>RText.TABBED_VIEW</code> or
	 *         <code>RText.SPLIT_PANE_VIEW</code>.
	 * @see #setMainViewStyle
	 */
	private int getMainViewStyle() {
		return viewCombo.getSelectedIndex();
	}


	/**
	 * Returns the selected Theme.
	 *
	 * @return The theme
	 * @see #setTheme(AppTheme)
	 */
	private AppTheme getTheme() {
		return themeCombo.getSelectedValue();
	}


	@Override
	public JComponent getTopJComponent() {
		return themeCombo;
	}


	/**
	 * Called whenever a property change occurs in this panel.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {

		String propName = e.getPropertyName();

		if (propName.equals(RColorSwatchesButton.COLOR_CHANGED_PROPERTY)) {
			setDirty(true);
		}

	}


	/**
	 * Sets the tab placement displayed by this panel.
	 *
	 * @param documentSelectionPlacement The tab placement displayed; one of
	 *        <code>JTabbedPane.LEFT</code>, <code>TOP</code>,
	 *        <code>RIGHT</code>, or <code>BOTTOM</code>.
	 * @see #getDocumentSelectionPlacement
	 */
	private void setDocumentSelectionPlacement(int documentSelectionPlacement) {

		if (documentSelectionPlacement!=JTabbedPane.LEFT &&
				documentSelectionPlacement!=JTabbedPane.RIGHT &&
				documentSelectionPlacement!=JTabbedPane.TOP &&
				documentSelectionPlacement!=JTabbedPane.BOTTOM) {
			documentSelectionPlacement = JTabbedPane.TOP;
		}

		docSelCombo.setSelectedIndex(documentSelectionPlacement -
								JTabbedPane.TOP);

	}


	/**
	 * Sets the look and feel displayed in this panel.
	 *
	 * @param theme The currently installed theme.
	 * @see #getTheme()
	 */
	private void setTheme(AppTheme theme) {

		for (int i = 0; i < themeCombo.getItemCount(); i++) {
			AppTheme comboTheme = themeCombo.getValueAt(i);
			if (comboTheme.getName().equals(theme.getName())) {
				themeCombo.setSelectedIndex(i);
				return;
			}
		}

		themeCombo.setSelectedIndex(0); // Default value???
	}


	/**
	 * Sets the main view style displayed by this panel.
	 *
	 * @param viewStyle The style displayed; either
	 *        <code>RText.TABBED_VIEW</code> or
	 *        <code>RText.SPLIT_PANE_VIEW</code>.
	 * @see #getMainViewStyle
	 */
	private void setMainViewStyle(int viewStyle) {
		if (viewStyle!=RText.TABBED_VIEW && viewStyle!=RText.SPLIT_PANE_VIEW &&
				viewStyle!=RText.MDI_VIEW) {
			viewStyle = RText.TABBED_VIEW;
		}
		viewCombo.setSelectedIndex(viewStyle);
	}


	/**
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are not handled.
	 *
	 * @param owner The parent application.
	 * @see #setValues(Frame)
	 */
	@Override
	protected void setValuesImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		setDocumentSelectionPlacement(mainView.getDocumentSelectionPlacement());
		setTheme(rtext.getTheme());
		setMainViewStyle(rtext.getMainViewStyle());
		showHostNameCheckBox.setSelected(rtext.getShowHostName());
	}


	@Override
	public void updateUI() {
		super.updateUI();
		if (springPanel!=null) {
			UIUtil.makeSpringCompactGrid(springPanel,
								2,2,		// rows,cols,
								0,0,		// initial-x, initial-y,
								5,5);	// x-spacing, y-spacing.
		}
	}



	/**
	 * Listens for events in this panel.
	 */
	private class Listener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (applyButton == source) {
				applySelectedTheme();
			}
		}

	}

}
