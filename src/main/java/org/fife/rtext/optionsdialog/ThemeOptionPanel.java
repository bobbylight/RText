/*
 * 05/14/2016
 *
 * Copyright (C) 2016 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.optionsdialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.plugins.console.ConsoleOptionPanel;
import org.fife.ui.LabelValueComboBox;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaOptionPanel;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextAreaOptionPanel;


/**
 * An option panel allowing the user to toggle the LookAndFeel, editor fonts
 * and colors, etc. all together as a "theme" of the application.
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public class ThemeOptionPanel extends OptionsDialogPanel {

	/**
	 * ID used to identify this option panel.
	 */
	public static final String OPTION_PANEL_ID = "ThemeOptionPanel";

	private LabelValueComboBox<String, String> themeCombo;
	private JButton applyButton;


	/**
	 * Constructor.
	 *
	 * @param rtext The parent application.
	 * @param msg The resource bundle to use when localizing this panel.
	 */
	public ThemeOptionPanel(RText rtext, final ResourceBundle msg) {

		super(msg.getString("OptThemeName"));
		setId(OPTION_PANEL_ID);
		Listener listener = new Listener();

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());
		Box cp = Box.createVerticalBox();
		add(cp, BorderLayout.NORTH);

		SelectableLabel label = new SelectableLabel();
		label.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
		label.setText(msg.getString("OptThemeDesc"));
		cp.add(label);
		cp.add(Box.createVerticalStrut(5));

		Box temp = Box.createVerticalBox();
		temp.setBorder(new OptionPanelBorder(msg.getString("OptThemeLabel")));

		themeCombo = new LabelValueComboBox<String, String>();
		themeCombo.addLabelValuePair("Default", "default");
		themeCombo.addLabelValuePair("Eclipse", "eclipse");
		themeCombo.addLabelValuePair("Dark", "dark");
		UIUtil.fixComboOrientation(themeCombo);
		Box temp2 = createHorizontalBox();
		temp2.add(themeCombo);
		temp2.add(Box.createHorizontalStrut(5));
		applyButton = UIUtil.newButton(msg, "OptThemeApply", listener);
		temp2.add(applyButton);
		temp2.add(Box.createHorizontalGlue());
		temp.add(temp2);
		cp.add(temp);
		cp.add(Box.createVerticalStrut(10));

		cp.add(Box.createVerticalGlue());

		ComponentOrientation o = ComponentOrientation.getOrientation(getLocale());
		applyComponentOrientation(o);

	}


	private void applySelectedTheme() {

		String theme = themeCombo.getSelectedValue();
		String laf = null;
		String editorTheme = null;

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
		}

		RText rtext = (RText)getOptionsDialog().getOwner();
		if (laf != null) {
			RTextUtilities.setLookAndFeel(rtext, laf); // Doesn't update if...
		}

		if (editorTheme != null) {
			Theme themeObj = null;
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
		rtext.getMainView().setModifiedDocumentDisplayNamesColor(
				otherColors.getModifiedDocumentNameColor());

		// Refresh other option panels whose properties were affected
		org.fife.ui.OptionsDialog dialog = getOptionsDialog();
		dialog.getPanelById(UIOptionPanel.OPTION_PANEL_ID).setValues(rtext);
		dialog.getPanelById(RTextAreaOptionPanel.OPTION_PANEL_ID).setValues(rtext);
		dialog.getPanelById(RSyntaxTextAreaOptionPanel.OPTION_PANEL_ID).setValues(rtext);
		// Options panels installed by plugins weren't loaded by the same
		// ClassLoader, so we can't reference them directly, but we can
		// broadcast events to them.
		getOptionsDialog().broadcast("appTheme:" + theme);
	}


	@Override
	protected void doApplyImpl(Frame owner) {
		// Everything in this panel is applied automatically
	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
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

		if ("eclipse".equals(theme)) {
			colors.setModifiedDocumentNameColor(Color.RED);
		}

		else if ("dark".equals(theme)) {
			colors.setModifiedDocumentNameColor(new Color(255, 128, 128));
		}

		else {//if ("default".equals(theme)) {
			colors.setModifiedDocumentNameColor(Color.RED);
		}

		return colors;

	}


	@Override
	public JComponent getTopJComponent() {
		return themeCombo;
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

	}


	@Override
	protected void setValuesImpl(Frame owner) {
		// Everything in this panel is applied automatically
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

		public Color getModifiedDocumentNameColor() {
			return modifiedDocumentNameColor;
		}

		public void setModifiedDocumentNameColor(Color color) {
			modifiedDocumentNameColor = color;
		}

	}


}
