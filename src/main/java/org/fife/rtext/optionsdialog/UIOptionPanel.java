/*
 * 03/25/2004
 *
 * UIOptionPanel.java - Option panel for overall user interface
 * (non-textarea) related options.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.optionsdialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.*;
import org.fife.ui.app.ExtendedLookAndFeelInfo;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaOptionPanel;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.IconGroup;
import org.fife.ui.rtextarea.RTextAreaOptionPanel;
import org.fife.util.SubstanceUtil;


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

	private LabelValueComboBox<String, String> themeCombo;
	private JButton applyButton;
	private IconGroup eclipseIconGroup;
	private IconGroup flatIconGroup;

	private int mainViewStyle;
	private int documentSelectionPlacement;
	private int statusBarStyle;

	private JPanel springPanel, springPanel2;
	private JComboBox<String> viewCombo;
	private JComboBox<String> docSelCombo;
	private LabelValueComboBox<String, String> lnfCombo;
	private JLabel substanceSkinLabel;
	private LabelValueComboBox<String, String> substanceSkinCombo;
	private LabelValueComboBox<String, String> imageLnFCombo;
	private JComboBox<String> statusBarCombo;

	private JCheckBox highlightModifiedCheckBox;
	private RColorSwatchesButton hmColorButton;

	private JCheckBox showHostNameCheckBox;

	/**
	 * The value displayed in the Look and Feel dropdown when a Substance skin is selected.
	 */
	private static final String LNF_VALUE_SUBSTANCE = "SUBSTANCE_STUB_VALUE";

	/**
	 * Constructor.
	 */
	UIOptionPanel(RText rtext, ResourceBundle msg) {

		super(msg.getString("OptUIName"));
		setId(OPTION_PANEL_ID);
		Listener listener = new Listener();

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		// Set up our border and layout.
		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		Box cp = Box.createVerticalBox();

		cp.add(createThemePanel(msg, listener));
		cp.add(Box.createVerticalStrut(10));
		cp.add(createAppearancePanel(msg, rtext));
		cp.add(createLayoutPanel(msg));
		cp.add(createOtherPanel(msg));

		// Add everything "to the north" so the spacing between stuff doesn't
		// change then the user stretches the dialog.
		add(cp, BorderLayout.NORTH);
		applyComponentOrientation(orientation);

		eclipseIconGroup = rtext.getIconGroupMap().get("Eclipse Icons");
		flatIconGroup = rtext.getIconGroupMap().get("IntelliJ Icons (Dark)");
	}


	/**
	 * Listens for actions being performed in this panel.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		switch (actionCommand) {
			case "ViewComboBox": {
				hasUnsavedChanges = true;
				int old = mainViewStyle;
				mainViewStyle = viewCombo.getSelectedIndex();
				firePropertyChange("UIOptionPanel.mainViewStyle", old, mainViewStyle);
				break;
			}
			case "DocSelCombo": {
				hasUnsavedChanges = true;
				int old = documentSelectionPlacement;
				documentSelectionPlacement = docSelCombo.getSelectedIndex() +
					JTabbedPane.TOP;
				firePropertyChange("UIOptionPanel.documentSelectionPlacement",
					old, documentSelectionPlacement);
				break;
			}
			case "LookAndFeelComboBox":
				hasUnsavedChanges = true;
				possiblyUpdateSubstanceThemeWidgets();
				String newLnF = getLookAndFeelClassName();
				firePropertyChange("UIOptionPanel.lookAndFeel", null, newLnF);
				break;
			case "SubstanceThemeComboBox":
				hasUnsavedChanges = true;
				newLnF = getLookAndFeelClassName();
				firePropertyChange("UIOptionPanel.lookAndFeel", null, newLnF);
				break;
			case "IconComboBox":
				hasUnsavedChanges = true;
				String name = imageLnFCombo.getSelectedValue();
				firePropertyChange("UIOptionPanel.iconStyle", null, name);
				break;
			case "StatusBarComboBox": {
				hasUnsavedChanges = true;
				int old = statusBarStyle;
				statusBarStyle = statusBarCombo.getSelectedIndex();
				firePropertyChange("UIOptionPanel.statusBarStyle", old, statusBarStyle);
				break;
			}
			case "HighlightModifiedCheckBox":
				boolean highlight = highlightModifiedDocumentDisplayNames();
				hmColorButton.setEnabled(highlight);
				hasUnsavedChanges = true;
				firePropertyChange("UIOptionPanel.highlightModified", !highlight, highlight);
				break;
			case "ShowHostNameCB":
				boolean show = getShowHostName();
				hasUnsavedChanges = true;
				firePropertyChange("UIOptionPanel.showHostName", !show, show);
				break;
		}

	}


	private void applySelectedTheme() {

		String theme = themeCombo.getSelectedValue();
		String laf = null;
		String editorTheme = null;
		IconGroup iconGroup = eclipseIconGroup;

		if ("default".equals(theme)) {
			laf = UIManager.getSystemLookAndFeelClassName();
			editorTheme = "/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml";
		}

		else if ("eclipse".equals(theme)) {
			laf = UIManager.getSystemLookAndFeelClassName();
			editorTheme = "/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml";
		}

		else if ("dark".equals(theme)) {
			laf = "com.bulenkov.darcula.DarculaLaf";
			editorTheme = "/org/fife/ui/rsyntaxtextarea/themes/dark.xml";
			iconGroup = flatIconGroup;
		}

		else if ("monokai".equals(theme)) {
			laf = "com.bulenkov.darcula.DarculaLaf";
			editorTheme = "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml";
			iconGroup = flatIconGroup;
		}

		RText rtext = (RText)getOptionsDialog().getOwner();
		if (laf != null) {
			RTextUtilities.setLookAndFeel(rtext, laf); // Doesn't update if...
		}
		rtext.setIconGroupByName(iconGroup.getName());

		if (editorTheme != null) {
			Theme themeObj;
			try {
				themeObj = Theme.load(getClass().getResourceAsStream(editorTheme));
				installRstaTheme(rtext, themeObj);
			} catch (Exception ioe) {
				rtext.displayException(ioe);
				return;
			}
		}

		// Any colors not specific to the LAF or the RSTA theme.
		OtherColors otherColors = getOtherColorsForTheme(theme);
		AbstractMainView mainView = rtext.getMainView();
		mainView.setModifiedDocumentDisplayNamesColor(
			otherColors.getModifiedDocumentNameColor());

		// Refresh other option panels whose properties were affected
		org.fife.ui.OptionsDialog dialog = getOptionsDialog();
		setValues(rtext);
		dialog.getPanelById(UIOptionPanel.OPTION_PANEL_ID).setValues(rtext);
		dialog.getPanelById(RTextAreaOptionPanel.OPTION_PANEL_ID).setValues(rtext);
		dialog.getPanelById(RSyntaxTextAreaOptionPanel.OPTION_PANEL_ID).setValues(rtext);
		// Options panels installed by plugins weren't loaded by the same
		// ClassLoader, so we can't reference them directly, but we can
		// broadcast events to them.
		getOptionsDialog().broadcast("appTheme:" + theme);
	}


	private JPanel createAppearancePanel(ResourceBundle msg, RText rtext) {

		ComponentOrientation orientation = ComponentOrientation.
			getOrientation(getLocale());

		JPanel temp = new JPanel(new BorderLayout());

		SelectableLabel label = new SelectableLabel();
		label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		label.setText(msg.getString("OptAppearanceDesc"));
		temp.add(label, BorderLayout.NORTH);

		lnfCombo = createLookAndFeelComboBox(rtext);
		lnfCombo.setActionCommand("LookAndFeelComboBox");
		lnfCombo.addActionListener(this);

		substanceSkinLabel = new JLabel(msg.getString("OptUISubstanceSkin"));

		substanceSkinCombo = createSubstanceThemeComboBox();
		substanceSkinCombo.setActionCommand("SubstanceThemeComboBox");
		substanceSkinCombo.addActionListener(this);

		imageLnFCombo = new LabelValueComboBox<>();
		UIUtil.fixComboOrientation(imageLnFCombo);
		imageLnFCombo.setActionCommand("IconComboBox");
		imageLnFCombo.addActionListener(this);
		Collection<IconGroup> iconGroups = rtext.getIconGroupMap().values();
		for (IconGroup group : iconGroups) {
			imageLnFCombo.addLabelValuePair(group.getName(), group.getName());
		}

		statusBarCombo = new JComboBox<>();
		UIUtil.fixComboOrientation(statusBarCombo);
		statusBarCombo.setActionCommand("StatusBarComboBox");
		statusBarCombo.addActionListener(this);
		statusBarCombo.addItem(msg.getString("OptUIW95A"));
		statusBarCombo.addItem(msg.getString("OptUIWXPA"));

		// Add a panel for the "Appearance" stuff.
		springPanel2 = new JPanel(new SpringLayout());
		JPanel temp2 = new JPanel(new BorderLayout());
		temp.setBorder(new OptionPanelBorder(msg.getString("OptUIAT")));
		if (orientation.isLeftToRight()) {
			springPanel2.add(new JLabel(msg.getString("OptUILnFT")));
			springPanel2.add(lnfCombo);
			springPanel2.add(substanceSkinLabel);
			springPanel2.add(substanceSkinCombo);
			springPanel2.add(new JLabel(msg.getString("OptUIIAT")));
			springPanel2.add(imageLnFCombo);
			springPanel2.add(new JPanel()); springPanel2.add(new JPanel());
			springPanel2.add(new JLabel(msg.getString("OptUISBT")));
			springPanel2.add(statusBarCombo);
			springPanel2.add(new JPanel()); springPanel2.add(new JPanel());
		}
		else {
			springPanel2.add(lnfCombo);
			springPanel2.add(new JLabel(msg.getString("OptUILnFT")));
			springPanel2.add(substanceSkinCombo);
			springPanel2.add(substanceSkinLabel);
			springPanel2.add(new JPanel()); springPanel2.add(new JPanel());
			springPanel2.add(imageLnFCombo);
			springPanel2.add(new JLabel(msg.getString("OptUIIAT")));
			springPanel2.add(new JPanel()); springPanel2.add(new JPanel());
			springPanel2.add(statusBarCombo);
			springPanel2.add(new JLabel(msg.getString("OptUISBT")));
		}
		temp2.add(springPanel2, BorderLayout.LINE_START);
		UIUtil.makeSpringCompactGrid(springPanel2,
			3,4,		// rows,cols,
			0,0,		// initial-x, initial-y,
			5,5);	// x-spacing, y-spacing.

		temp.add(temp2);

		return temp;
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
		if (orientation.isLeftToRight()) {
			springPanel.add(new JLabel(msg.getString("OptUIViewT")));
			springPanel.add(viewCombo);
			springPanel.add(new JLabel(msg.getString("OptUIDSPT")));
			springPanel.add(docSelCombo);
		}
		else {
			springPanel.add(viewCombo);
			springPanel.add(new JLabel(msg.getString("OptUIViewT")));
			springPanel.add(docSelCombo);
			springPanel.add(new JLabel(msg.getString("OptUIDSPT")));
		}
		temp.add(springPanel, BorderLayout.LINE_START);
		UIUtil.makeSpringCompactGrid(springPanel,
			2,2,		// rows,cols,
			0,0,		// initial-x, initial-y,
			5,5);	// x-spacing, y-spacing.

		return temp;
	}

	/**
	 * Creates and returns a special value combo box containing all available
	 * Look and Feels.
	 *
	 * @param rtext The parent RText instance.
	 * @return The combo box.
	 */
	private static LabelValueComboBox<String, String> createLookAndFeelComboBox(
			RText rtext) {

		LabelValueComboBox<String, String> combo = new LabelValueComboBox<>();
		UIUtil.fixComboOrientation(combo);

		// Get the system look and feel.
		LookAndFeelInfo systemInfo = null;
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			String clazzName = info.getClassName();
			if (clazzName.equals(UIManager.getSystemLookAndFeelClassName())) {
				systemInfo = info;
				break;
			}
		}

		if (systemInfo != null) {
			combo.addLabelValuePair(systemInfo.getName(), systemInfo.getClassName());
		}

		combo.addLabelValuePair("Substance", LNF_VALUE_SUBSTANCE);

		// Add any 3rd party Look and Feels in the lnfs subdirectory.
		ExtendedLookAndFeelInfo[] info = rtext.get3rdPartyLookAndFeelInfo();
		if (info!=null && info.length>0) {
			for (ExtendedLookAndFeelInfo extendedLookAndFeelInfo : info) {
				combo.addLabelValuePair(extendedLookAndFeelInfo.getName(), extendedLookAndFeelInfo.getClassName());
			}
		}

		return combo;

	}


	private Container createOtherPanel(ResourceBundle msg) {

		Box temp = Box.createVerticalBox();
		temp.setBorder(new OptionPanelBorder(msg.getString("OptOtherTitle")));

		Container modifiedDocsPanel = createHorizontalBox();
		highlightModifiedCheckBox = new JCheckBox(msg.getString("OptUIHMDN"));
		highlightModifiedCheckBox.setActionCommand("HighlightModifiedCheckBox");
		highlightModifiedCheckBox.addActionListener(this);
		hmColorButton = new RColorSwatchesButton(Color.RED);
		hmColorButton.addPropertyChangeListener(this);
		modifiedDocsPanel.add(highlightModifiedCheckBox);
		modifiedDocsPanel.add(hmColorButton);
		modifiedDocsPanel.add(Box.createHorizontalGlue());
		temp.add(modifiedDocsPanel);

		JPanel showHostNamePanel = new JPanel(new BorderLayout());
		showHostNameCheckBox = new JCheckBox(msg.getString("OptUIShowHostName"));
		showHostNameCheckBox.setActionCommand("ShowHostNameCB");
		showHostNameCheckBox.addActionListener(this);
		showHostNamePanel.add(showHostNameCheckBox, BorderLayout.LINE_START);
		temp.add(showHostNamePanel);

		temp.add(Box.createVerticalGlue());
		return temp;
	}

	/**
	 * Creates and returns a special value combo box containing all available
	 * Substance themes.
	 *
	 * @return The combo box.
	 */
	private static LabelValueComboBox<String, String> createSubstanceThemeComboBox() {

		LabelValueComboBox<String, String> combo = new LabelValueComboBox<>();
		UIUtil.fixComboOrientation(combo);

		String root = "org.pushingpixels.substance.api.skin.Substance";

		combo.addLabelValuePair("Business", root + "BusinessLookAndFeel");
		combo.addLabelValuePair("Business Black Steel", root + "BusinessBlackSteelLookAndFeel");
		combo.addLabelValuePair("Business Blue Steel", root + "BusinessBlueSteelLookAndFeel");
		combo.addLabelValuePair("Cerulean", root + "CeruleanLookAndFeel");
		combo.addLabelValuePair("Creme", root + "CremeLookAndFeel");
		combo.addLabelValuePair("Creme Coffee", root + "CremeCoffeeLookAndFeel");
		combo.addLabelValuePair("Dust", root + "DustLookAndFeel");
		combo.addLabelValuePair("Dust Coffee", root + "DustCoffeeLookAndFeel");
		combo.addLabelValuePair("Gemini", root + "GeminiLookAndFeel");
		combo.addLabelValuePair("Graphite", root + "GraphiteLookAndFeel");
		combo.addLabelValuePair("Graphite Aqua", root + "GraphiteAquaLookAndFeel");
		combo.addLabelValuePair("Graphite Chalk", root + "GraphiteChalkLookAndFeel");
		combo.addLabelValuePair("Graphite Glass", root + "GraphiteGlassLookAndFeel");
		combo.addLabelValuePair("Mariner", root + "MarinerLookAndFeel");
		combo.addLabelValuePair("Mist Aqua", root + "MistAquaLookAndFeel");
		combo.addLabelValuePair("Mist Silver", root + "MistSilverLookAndFeel");
		combo.addLabelValuePair("Moderate", root + "ModerateLookAndFeel");
		combo.addLabelValuePair("Nebula", root + "NebulaLookAndFeel");
		combo.addLabelValuePair("Nebula Brick Wall", root + "NebulaBrickWallLookAndFeel");
		combo.addLabelValuePair("Office Black 2007", root + "OfficeBlack2007LookAndFeel");
		combo.addLabelValuePair("Sahara", root + "SaharaLookAndFeel");
		combo.addLabelValuePair("Twilight", root + "TwilightLookAndFeel");

		return combo;
	}


	private Container createThemePanel(ResourceBundle msg, Listener listener) {

		Box temp = Box.createVerticalBox();
		temp.setBorder(new OptionPanelBorder(msg.getString("OptThemeLabel")));

		SelectableLabel label = new SelectableLabel();
		label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		label.setText(msg.getString("OptThemeDesc"));
		temp.add(label, BorderLayout.NORTH);

		themeCombo = new LabelValueComboBox<>();
		themeCombo.addLabelValuePair("Default", "default");
		themeCombo.addLabelValuePair("Eclipse", "eclipse");
		themeCombo.addLabelValuePair("Dark", "dark");
		themeCombo.addLabelValuePair("Dark (Monokai)", "monokai");
		UIUtil.fixComboOrientation(themeCombo);
		Box temp2 = createHorizontalBox();
		temp2.add(themeCombo);
		temp2.add(Box.createHorizontalStrut(5));
		applyButton = UIUtil.newButton(msg, "OptThemeApply", listener);
		temp2.add(applyButton);
		temp2.add(Box.createHorizontalGlue());
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
		mainView.setDocumentSelectionPlacement(getDocumentSelectionPlacement());	// Doesn't update if it doesn't have to.
		RTextUtilities.setLookAndFeel(rtext, getLookAndFeelClassName()); // Doesn't update if...
		rtext.setIconGroupByName(getIconGroupName());		// Doesn't update if it doesn't have to.
		mainView.setHighlightModifiedDocumentDisplayNames(highlightModifiedDocumentDisplayNames());
		mainView.setModifiedDocumentDisplayNamesColor(getModifiedDocumentDisplayNamesColor());
		rtext.setMainViewStyle(getMainViewStyle());			// Doesn't update if it doesn't have to.
		rtext.getStatusBar().setStyle(getStatusBarStyle());
		rtext.setShowHostName(getShowHostName());	// Doesn't update if doesn't have to.
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
		return documentSelectionPlacement;
	}


	/**
	 * Returns the icon style the user chose.
	 *
	 * @return The icon style.
	 * @see #setIconGroupByName
	 */
	private String getIconGroupName() {
		return imageLnFCombo.getSelectedValue();
	}


	/**
	 * Returns the selected Look and Feel.
	 *
	 * @return The look and feel.
	 * @see #setLookAndFeelByClassName
	 */
	private String getLookAndFeelClassName() {

		String value = lnfCombo.getSelectedValue();

		if (LNF_VALUE_SUBSTANCE.equals(value)) {
			return substanceSkinCombo.getSelectedValue();
		}

		return value;
	}


	/**
	 * Returns the main view style the user chose.
	 *
	 * @return The main view style, either <code>RText.TABBED_VIEW</code> or
	 *         <code>RText.SPLIT_PANE_VIEW</code>.
	 * @see #setMainViewStyle
	 */
	private int getMainViewStyle() {
		return mainViewStyle;
	}


	/**
	 * Gets the color the user chose to highlight modified documents'
	 * display names.
	 *
	 * @return color The color chosen.
	 * @see #setModifiedDocumentDisplayNamesColor
	 * @see #highlightModifiedDocumentDisplayNames
	 * @see #setHighlightModifiedDocumentDisplayNames
	 */
	private Color getModifiedDocumentDisplayNamesColor() {
		return hmColorButton.getColor();
	}


	/**
	 * Returns other colors to install that aren't tied to the LAF or the
	 * RSTA theme.
	 *
	 * @param theme The selected theme.
	 * @return The other colors.
	 */
	private static OtherColors getOtherColorsForTheme(String theme) {

		OtherColors colors = new OtherColors();

		Color darkModifiedDocumentNameColor = new Color(255, 128, 128);

		if ("eclipse".equals(theme)) {
			colors.setModifiedDocumentNameColor(Color.RED);
		}

		else if ("dark".equals(theme)) {
			colors.setModifiedDocumentNameColor(darkModifiedDocumentNameColor);
		}

		else if ("monokai".equals(theme)) {
			colors.setModifiedDocumentNameColor(darkModifiedDocumentNameColor);
		}

		else {//if ("default".equals(theme)) {
			colors.setModifiedDocumentNameColor(Color.RED);
		}

		return colors;

	}


	/**
	 * Returns whether the user wants the host name displayed in the title
	 * bar.
	 *
	 * @return Whether to show the host name.
	 * @see #setShowHostName(boolean)
	 */
	private boolean getShowHostName() {
		return showHostNameCheckBox.isSelected();
	}


	/**
	 * Returns the status bar style selected by the user.
	 *
	 * @return The status bar style selected.
	 * @see #setStatusBarStyle
	 */
	private int getStatusBarStyle() {
		return statusBarStyle;
	}


	@Override
	public JComponent getTopJComponent() {
		return themeCombo;
	}


	/**
	 * Returns whether the user chose to highlight documents' display names
	 * with a different color.
	 *
	 * @return Whether or not the user chose to highlight documents' display
	 *         names with a different color.
	 *
	 * @see #setHighlightModifiedDocumentDisplayNames
	 * @see #getModifiedDocumentDisplayNamesColor
	 * @see #setModifiedDocumentDisplayNamesColor
	 */
	private boolean highlightModifiedDocumentDisplayNames() {
		return highlightModifiedCheckBox.isSelected();
	}


	/**
	 * Installs all properties in an RSTA <code>Theme</code> instance properly
	 * into RText.
	 *
	 * @param rtext The application.
	 * @param theme The theme instance.
	 */
	private static void installRstaTheme(RText rtext, Theme theme) {

		rtext.setSyntaxScheme(theme.scheme);
		AbstractMainView mainView = rtext.getMainView();

		//themeObj.activeLineRangeColor;
		mainView.setBackgroundObject(theme.bgColor);
		mainView.setCaretColor(theme.caretColor);
		mainView.setCurrentLineHighlightColor(theme.currentLineHighlight);
		//themeObj.fadeCurrentLineHighlight
		//themeObj.foldBG
		mainView.setGutterBorderColor(theme.gutterBorderColor);
		mainView.setHyperlinkColor(theme.hyperlinkFG);
		//themeObj.iconRowHeaderInheritsGutterBG
		mainView.setLineNumberColor(theme.lineNumberColor);
		if (theme.lineNumberFont != null) {
			int fontSize = theme.lineNumberFontSize > 0 ? theme.lineNumberFontSize : 11;
			mainView.setLineNumberFont(new Font(theme.lineNumberFont, Font.PLAIN, fontSize));
		}
		mainView.setMarginLineColor(theme.marginLineColor);
		mainView.setMarkAllHighlightColor(theme.markAllHighlightColor);
		//themeObj.markOccurrencesBorder;
		mainView.setMarkOccurrencesColor(theme.markOccurrencesColor);
		//themeObj.matchedBracketAnimate;
		if (theme.matchedBracketBG != null) {
			mainView.setMatchedBracketBorderColor(theme.matchedBracketFG);
		}
		mainView.setMatchedBracketBGColor(theme.matchedBracketBG);
		if (theme.secondaryLanguages != null) {
			for (int i = 0; i < theme.secondaryLanguages.length; i++) {
				mainView.setSecondaryLanguageColor(i, theme.secondaryLanguages[i]);
			}
		}
		mainView.setSelectionColor(theme.selectionBG);
		if (theme.selectionFG != null) {
			mainView.setSelectedTextColor(theme.selectionFG);
		}
		mainView.setUseSelectedTextColor(theme.useSelctionFG);
		mainView.setRoundedSelectionEdges(theme.selectionRoundedEdges);

		mainView.setFoldBackground(theme.foldBG);
		mainView.setArmedFoldBackground(theme.armedFoldBG);

	}


	private void possiblyUpdateSubstanceThemeWidgets() {
		boolean substanceSelected = LNF_VALUE_SUBSTANCE.equals(lnfCombo.getSelectedValue());
		substanceSkinLabel.setVisible(substanceSelected);
		substanceSkinCombo.setVisible(substanceSelected);
	}


	/**
	 * Called whenever a property change occurs in this panel.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {

		String propName = e.getPropertyName();

		if (propName.equals(RColorSwatchesButton.COLOR_CHANGED_PROPERTY)) {
			hasUnsavedChanges = true;
			firePropertyChange("UIOptionPanel." + propName,
								e.getOldValue(), e.getNewValue());
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

		if (this.documentSelectionPlacement!=documentSelectionPlacement) {
			this.documentSelectionPlacement = documentSelectionPlacement;
			docSelCombo.setSelectedIndex(documentSelectionPlacement -
									JTabbedPane.TOP);
		}

	}


	/**
	 * Sets whether the "highlight modified documents' display names"
	 * checkbox is checked.
	 *
	 * @param highlight Whether or not the checkbox is to be checked.
	 * @see #highlightModifiedDocumentDisplayNames
	 * @see #getModifiedDocumentDisplayNamesColor
	 * @see #setModifiedDocumentDisplayNamesColor
	 */
	private void setHighlightModifiedDocumentDisplayNames(boolean highlight) {
		highlightModifiedCheckBox.setSelected(highlight);
		hmColorButton.setEnabled(highlight);
	}


	/**
	 * Sets the icon style displayed by this panel.
	 *
	 * @param name The name of the icon group.
	 * @see #getIconGroupName
	 */
	private void setIconGroupByName(String name) {
		int count = imageLnFCombo.getItemCount();
		for (int i=0; i<count; i++) {
			String specialValue = imageLnFCombo.getValueAt(i);
			if (specialValue.equals(name)) {
				imageLnFCombo.setSelectedIndex(i);
				return;
			}
		}
		imageLnFCombo.setSelectedIndex(0); // Default value???
	}


	/**
	 * Sets the look and feel displayed in this panel.
	 *
	 * @param name The class name for the Look.
	 * @see #getLookAndFeelClassName
	 */
	private void setLookAndFeelByClassName(String name) {

		// If we're a substance theme, set the main LaF dropdown to the "Substance" value,
		// ensure the Substance skin widgets are visible, and update them as well
		if (SubstanceUtil.isASubstanceLookAndFeel(name)) {

			for (int i = 0; i < substanceSkinCombo.getItemCount(); i++) {
				if (name.equals(substanceSkinCombo.getValueAt(i))) {
					substanceSkinCombo.setSelectedIndex(i);
					break;
				}
			}

			name = LNF_VALUE_SUBSTANCE;
		}

		int count = lnfCombo.getItemCount();
		for (int i=0; i<count; i++) {
			String specialValue = lnfCombo.getValueAt(i);
			if (specialValue.equals(name)) {
				lnfCombo.setSelectedIndex(i);
				possiblyUpdateSubstanceThemeWidgets();
				return;
			}
		}

		possiblyUpdateSubstanceThemeWidgets();
		lnfCombo.setSelectedIndex(0); // Default value???
	}


	/**
	 * Sets the main view style displayed by this panel.
	 *
	 * @param viewStyle The style displayed; either
	 *        <code>RText.TABBED_VIEW</code> or
	 *        <code>RText.SPLIT_PANE_VIEW</code>.
	 * @see #getMainViewStyle
	 */
	private void setMainViewStyle(final int viewStyle) {
		if (viewStyle==RText.TABBED_VIEW || viewStyle==RText.SPLIT_PANE_VIEW ||
			viewStyle==RText.MDI_VIEW)
			mainViewStyle = viewStyle;
		else
			mainViewStyle = RText.TABBED_VIEW;
		viewCombo.setSelectedIndex(mainViewStyle);
	}


	/**
	 * Sets the color for the button used for selecting the color used
	 * to highlight modified documents' display names.
	 *
	 * @param color The color to use.
	 * @throws NullPointerException If <code>color</code> is <code>null</code>.
	 * @see #getModifiedDocumentDisplayNamesColor
	 * @see #highlightModifiedDocumentDisplayNames
	 * @see #setHighlightModifiedDocumentDisplayNames
	 */
	private void setModifiedDocumentDisplayNamesColor(Color color) {
		if (color==null)
			throw new NullPointerException();
		hmColorButton.setColor(color);
	}


	/**
	 * Sets the "show hostname" checkbox's state.
	 *
	 * @param show Whether the checkbox should be enabled.
	 * @see #getShowHostName()
	 */
	private void setShowHostName(boolean show) {
		showHostNameCheckBox.setSelected(show);
	}


	/**
	 * Sets the status bar style selected.
	 *
	 * @param style The status bar style.
	 * @see #getStatusBarStyle
	 */
	private void setStatusBarStyle(int style) {
		if (style!=StatusBar.WINDOWS_98_STYLE &&
				style!=StatusBar.WINDOWS_XP_STYLE)
			style = StatusBar.WINDOWS_XP_STYLE;
		statusBarStyle = style;
		statusBarCombo.setSelectedIndex(statusBarStyle);
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
		setLookAndFeelByClassName(UIManager.getLookAndFeel().getClass().getName());
		setIconGroupByName(rtext.getIconGroup().getName());
		setMainViewStyle(rtext.getMainViewStyle());
		setHighlightModifiedDocumentDisplayNames(mainView.highlightModifiedDocumentDisplayNames());
		setStatusBarStyle(rtext.getStatusBar().getStyle());
		setModifiedDocumentDisplayNamesColor(mainView.getModifiedDocumentDisplayNamesColor());
		setShowHostName(rtext.getShowHostName());
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
		if (springPanel2!=null) {
			UIUtil.makeSpringCompactGrid(springPanel2,
								3,4,		// rows,cols,
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


	/**
	 * Miscellaneous extra colors set as part of an RText theme.
	 */
	private static class OtherColors {

		private Color modifiedDocumentNameColor;

		Color getModifiedDocumentNameColor() {
			return modifiedDocumentNameColor;
		}

		void setModifiedDocumentNameColor(Color color) {
			modifiedDocumentNameColor = color;
		}

	}
}
