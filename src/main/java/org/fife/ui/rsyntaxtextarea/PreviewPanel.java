/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

import org.fife.rtext.RTextAppThemes;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;
import org.fife.ui.app.AbstractGUIApplication;
import org.fife.ui.app.AppTheme;
import org.fife.ui.rtextarea.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.ResourceBundle;


/**
 * A text area that renders itself using styles and fonts defined
 * in a "preview context" (values defined in the Options dialog).
 *
 * @author Robert Futrell
 * @version 1.0
 */
final class PreviewPanel extends JPanel
		implements EditorOptionsPreviewContextListener, PropertyChangeListener, ActionListener {

	private JComboBox<String> sampleCombo;
	private RSyntaxTextArea textArea;
	private RTextScrollPane scrollPane;
	private AbstractGUIApplication<?> app;
	private ResourceBundle msg;
	private boolean firstTime;

	private static final String[] SAMPLE_LANGUAGES = { "Java", "JavaScript", "Perl", "PHP", "Ruby", "XML", };

	private static final String[] SAMPLES = {
		"previewJava.txt", "previewJavaScript.txt", "previewPerl.txt", "previewPhp.txt", "previewRuby.txt", "previewXml.txt",
	};

	private static final String[] SAMPLE_STYLES = {
		SyntaxConstants.SYNTAX_STYLE_JAVA, SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT, SyntaxConstants.SYNTAX_STYLE_PERL,
		SyntaxConstants.SYNTAX_STYLE_PHP, SyntaxConstants.SYNTAX_STYLE_RUBY, SyntaxConstants.SYNTAX_STYLE_XML,
	};

	static final String DEFAULT_PREVIEW_LANGUAGE = SAMPLE_LANGUAGES[0];


	PreviewPanel(ResourceBundle msg, int rows, int cols) {
		this.msg = msg;
		EditorOptionsPreviewContext.get().addListener(this);
		createUI(rows, cols);
		firstTime = true;
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		// Changing the canned sample text.
		if (sampleCombo==e.getSource()) {
			refreshDisplayedSample();
			EditorOptionsPreviewContext.get().setPreviewLanguage((String)sampleCombo.getSelectedItem());
		}
	}


	@Override
	public void addNotify() {
		super.addNotify();
		if (firstTime) {
			Window optionsDialog = SwingUtilities.getWindowAncestor(this);
			app = (AbstractGUIApplication<?>) optionsDialog.getParent();
			app.addPropertyChangeListener(AbstractGUIApplication.THEME_PROPERTY, this);
			initializeForTheme(app.getTheme());
			firstTime = false;
		}
	}


	/**
	 * Returns a horizontal box that respects component orientation, which
	 * <code>Box.createHorizontalBox()</code> does not, for backward
	 * compatibility reasons (!).
	 *
	 * @return The horizontal box.
	 */
	private static Box createHorizontalBox() {
		return new Box(BoxLayout.LINE_AXIS);
	}


	private void createUI(int rows, int cols) {

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		putClientProperty(UIUtil.PROPERTY_ALWAYS_IGNORE, Boolean.TRUE);
		setBorder(new OptionsDialogPanel.OptionPanelBorder(msg.getString("Preview")));

		Box horizBox = createHorizontalBox();
		JLabel sampleTextLabel = new JLabel(msg.getString("SampleTextLabel"));
		horizBox.add(sampleTextLabel);
		horizBox.add(Box.createHorizontalStrut(5));
		sampleCombo = new JComboBox<>(SAMPLE_LANGUAGES);
		sampleCombo.setEditable(false);
		sampleCombo.addActionListener(this);
		horizBox.add(sampleCombo);
		horizBox.add(Box.createHorizontalGlue());
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(horizBox, BorderLayout.LINE_START);
		add(temp);
		add(Box.createVerticalStrut(5));

		textArea = new RSyntaxTextArea(rows, cols);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		textArea.setCodeFoldingEnabled(true);
		textArea.setPopupMenu(null);
		scrollPane = new RTextScrollPane(textArea);
		add(scrollPane);
		add(Box.createVerticalStrut(3));

		refreshDisplayedSample();
	}


	@Override
	public void editorOptionsPreviewContextChanged(EditorOptionsPreviewContext context) {

		Theme editorTheme = context.getEditorTheme(app);
		Gutter gutter = scrollPane.getGutter();

		// Options related to this component, not the preview text area
		if (!Objects.equals(sampleCombo.getSelectedItem(), context.getPreviewLanguage())) {
			sampleCombo.setSelectedItem(context.getPreviewLanguage());
			refreshDisplayedSample();
		}

		// Options from the main "Text Area" option panel
		scrollPane.setLineNumbersEnabled(context.getLineNumbersEnabled());
		textArea.setLineWrap(context.getWordWrap());
		textArea.setHighlightCurrentLine(context.getHighlightCurrentLine());
		textArea.setMarginLineEnabled(context.getMarginLineEnabled());
		textArea.setMarginLinePosition(context.getMarginLinePosition());
		textArea.setCloseCurlyBraces(context.getAutoInsertClosingCurly());
		textArea.setClearWhitespaceLinesEnabled(context.getClearWhitespaceLines());
		textArea.setAntiAliasingEnabled(context.getAntiAliasingEnabled());
		textArea.setFractionalFontMetricsEnabled(context.getFractionalFontMetricsEnabled());
		textArea.setBracketMatchingEnabled(context.getHighlightMatchingBrackets());
		textArea.setPaintMatchedBracketPair(context.getHighlightBothBrackets());
		gutter.setFoldIndicatorStyle(context.getFoldIndicatorStyle());

		// Options from the "Font" child option panel
		textArea.setFont(context.getFont());
		textArea.setForeground(context.getFontColor());
		textArea.setTabSize(context.getTabSize());
		textArea.setTabsEmulated(context.getEmulateTabs());
		textArea.setWhitespaceVisible(context.getShowWhitespace());
		textArea.setEOLMarkersVisible(context.getShowEolMarkers());
		textArea.setPaintTabLines(context.getShowIndentGuides());

		// The child "Syntax Highlighting" panel is entirely driven by whether we
		// override the theme
		if (context.getOverrideEditorTheme()) {
			textArea.setBackgroundObject(context.getBackgroundColor());
			textArea.setSyntaxScheme(context.getSyntaxScheme());
		}
		else {
			textArea.setBackgroundObject(editorTheme.bgColor);
			textArea.setSyntaxScheme(editorTheme.scheme);
		}

		// Options from the "Caret and Selection" child option panel
		if (context.getOverrideEditorTheme()) {
			textArea.setCaretStyle(RTextArea.INSERT_MODE, context.getInsertCaret());
			textArea.setCaretStyle(RTextArea.OVERWRITE_MODE, context.getOverwriteCaret());
			textArea.setCaretColor(context.getCaretColor());
			textArea.getCaret().setBlinkRate(context.getCaretBlinkRate());
			textArea.setSelectionColor(context.getSelectionColor());
			textArea.setSelectedTextColor(context.getSelectedTextColor());
			textArea.setUseSelectedTextColor(context.getUseSelectedTextColor());
		}
		else {
			textArea.setCaretStyle(RTextArea.INSERT_MODE, CaretStyle.THICK_VERTICAL_LINE_STYLE);
			textArea.setCaretStyle(RTextArea.OVERWRITE_MODE, CaretStyle.BLOCK_STYLE);
			textArea.setCaretColor(editorTheme.caretColor);
			textArea.getCaret().setBlinkRate(500);
			textArea.setSelectionColor(editorTheme.selectionBG);
			textArea.setSelectedTextColor(editorTheme.selectionFG);
			textArea.setUseSelectedTextColor(editorTheme.useSelectionFG);
		}

		// Options from the "Highlights" child option panel
		if (context.getOverrideEditorTheme()) {
			if (context.getCurrentLineHighlightColor() != null) { // ugh
				textArea.setCurrentLineHighlightColor(context.getCurrentLineHighlightColor());
			}
			textArea.setMarkAllHighlightColor(context.getMarkAllHighlightColor());
			textArea.setMarkOccurrences(context.getMarkOccurrences());
			textArea.setMarkOccurrencesColor(context.getMarkOccurrencesColor());
			textArea.setHighlightSecondaryLanguages(context.getHighlightSecondaryLanguages());
			for (int i = 0; i < textArea.getSecondaryLanguageCount(); i++) {
				textArea.setSecondaryLanguageBackground(i + 1, context.getSecondaryLanguageBackground(i));
			}
		}
		else {
			textArea.setCurrentLineHighlightColor(editorTheme.currentLineHighlight);
			textArea.setMarkAllHighlightColor(editorTheme.markAllHighlightColor);
			textArea.setMarkOccurrences(true);
			textArea.setMarkOccurrencesColor(editorTheme.markOccurrencesColor);
			textArea.setHighlightSecondaryLanguages(true);
			for (int i = 0; i < textArea.getSecondaryLanguageCount(); i++) {
				textArea.setSecondaryLanguageBackground(i + 1, editorTheme.secondaryLanguages[i]);
			}
		}

		// Options from the "Gutter" child option panel
		if (context.getOverrideEditorTheme()) {
			gutter.setLineNumberFont(context.getLineNumberFont());
			gutter.setLineNumberColor(context.getLineNumberColor());
			gutter.setFoldIndicatorForeground(context.getFoldForeground());
			gutter.setFoldIndicatorArmedForeground(context.getArmedFoldForeground());
			gutter.setFoldBackground(context.getFoldBackground());
			gutter.setArmedFoldBackground(context.getArmedFoldBackground());
		}
		else {
			gutter.setLineNumberFont(RTextArea.getDefaultFont());
			gutter.setLineNumberColor(editorTheme.lineNumberColor);
			gutter.setFoldIndicatorForeground(editorTheme.foldIndicatorFG);
			gutter.setFoldIndicatorArmedForeground(editorTheme.foldIndicatorArmedFG);
			gutter.setFoldBackground(editorTheme.foldBG);
			gutter.setArmedFoldBackground(editorTheme.armedFoldBG);
		}
	}


	/**
	 * Called when the application theme changes. This allows us to refresh colors that
	 * aren't configurable in the application, but should take the theme defaults. It
	 * is assumed that {@link #editorOptionsPreviewContextChanged(EditorOptionsPreviewContext)}
	 * will be called after this to override anything customized beyond the theme's
	 * defaults.
	 *
	 * @param theme The new theme.
	 */
	private void initializeForTheme(AppTheme theme) {
		try {
			Theme rstaTheme = RTextAppThemes.getRstaTheme(theme, textArea.getFont());
			rstaTheme.apply(textArea);
		} catch (IOException ioe) {
			app.displayException(ioe); // Never happens
		}
	}


	@Override
	public void propertyChange(PropertyChangeEvent e) {

		String propertyName = e.getPropertyName();

		if (AbstractGUIApplication.THEME_PROPERTY.equals(propertyName)) {
			initializeForTheme((AppTheme)e.getNewValue());
		}
	}


	/**
	 * Refreshes the displayed sample text.
	 */
	private void refreshDisplayedSample() {

		int index = sampleCombo.getSelectedIndex();
		if (index<0 || index>SAMPLES.length) {
			index = 0;
		}

		InputStream in = getClass().getResourceAsStream(SAMPLES[index]);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			textArea.read(br, null);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		textArea.setCaretPosition(0);
		textArea.setSyntaxEditingStyle(SAMPLE_STYLES[index]);
		textArea.discardAllEdits();
	}


	@Override
	public void removeNotify() {
		super.removeNotify();
		app.removePropertyChangeListener(AbstractGUIApplication.THEME_PROPERTY, this);
		this.app = null;
	}
}
