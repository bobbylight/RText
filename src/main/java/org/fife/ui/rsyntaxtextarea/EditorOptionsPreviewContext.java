/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

import javax.swing.event.EventListenerList;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

import org.fife.rtext.RText;
import org.fife.rtext.RTextAppThemes;
import org.fife.ui.app.AbstractGUIApplication;
import org.fife.ui.rtextarea.CaretStyle;
import org.fife.ui.rtextarea.FoldIndicatorStyle;


/**
 * A wrapper around the styles currently being used in the "preview" text areas
 * in the options dialog. This is necessary to keep the "preview" text areas
 * in the various text area-related option panels in sync.
 */
final class EditorOptionsPreviewContext {

	private boolean dirty;

	private boolean overrideEditorTheme;
	private String previewLanguage;

	// Options in the main "text area" option panel
	private boolean wordWrap;
	private boolean highlightCurrentLine;
	private boolean marginLineEnabled;
	private int marginLinePosition;
	private boolean autoInsertClosingCurly;
	private boolean clearWhitespaceLines;
	private boolean antiAliasingEnabled;
	private boolean fractionalFontMetricsEnabled;
	private boolean highlightMatchingBrackets;
	private boolean highlightBothBrackets;

	// Options in the child "font" option panel
	private Font font;
	private Color fontColor;
	private int tabSize;
	private boolean emulateTabs;
	private boolean showWhitespace;
	private boolean showEolMarkers;
	private boolean showIndentGuides;

	// Options in the child "syntax highlighting" option panel
	private Color backgroundColor;
	private SyntaxScheme syntaxScheme;

	// Options in the child "caret and selection" option panel
	private CaretStyle insertCaret;
	private CaretStyle overwriteCaret;
	private int caretBlinkRate;
	private Color caretColor;
	private Color selectionColor;
	private Color selectedTextColor;
	private boolean useSelectedTextColor;

	// Options in the child "highlights" option panel
	private Color currentLineHighlightColor;
	private Color markAllHighlightColor;
	private boolean markOccurrences;
	private Color markOccurrencesColor;
	private boolean highlightSecondaryLanguages;
	private Color[] secondaryLanguages;

	// Options in the child "gutter" option panel
	private boolean lineNumbersEnabled;
	private Font lineNumberFont;
	private Color lineNumberColor;
	private FoldIndicatorStyle foldIndicatorStyle;
	private Color foldForeground;
	private Color armedFoldForeground;
	private Color foldBackground;
	private Color armedFoldBackground;

	private EventListenerList listeners;

	private static final EditorOptionsPreviewContext INSTANCE = new EditorOptionsPreviewContext();


	/**
	 * Private constructor to prevent instantiation.
	 */
	private EditorOptionsPreviewContext() {
		listeners = new EventListenerList();
		secondaryLanguages = new Color[3];
	}


	/**
	 * Adds a listener.
	 *
	 * @param listener The new listener.
	 * @see #removeListener(EditorOptionsPreviewContextListener)
	 */
	void addListener(EditorOptionsPreviewContextListener listener) {
		listeners.add(EditorOptionsPreviewContextListener.class, listener);
	}


	/**
	 * Returns the singleton instance of this class.
	 *
	 * @return The singleton instance.
	 */
	public static EditorOptionsPreviewContext get() {
		return INSTANCE;
	}


	public boolean getAntiAliasingEnabled() {
		return antiAliasingEnabled;
	}


	public Color getArmedFoldBackground() {
		return armedFoldBackground;
	}


	public Color getArmedFoldForeground() {
		return armedFoldForeground;
	}


	public boolean getAutoInsertClosingCurly() {
		return autoInsertClosingCurly;
	}


	public Color getBackgroundColor() {
		return backgroundColor;
	}


	public int getCaretBlinkRate() {
		return caretBlinkRate;
	}


	public Color getCaretColor() {
		return caretColor;
	}


	public boolean getClearWhitespaceLines() {
		return clearWhitespaceLines;
	}


	public Color getCurrentLineHighlightColor() {
		return currentLineHighlightColor;
	}


	/**
	 * Returns the RSTA theme the application should use, based on the current
	 * application state and the state of this preview context.
	 *
	 * @param app The application.
	 * @return The editor theme.
	 */
	public Theme getEditorTheme(AbstractGUIApplication<?> app) {
		try {
			return RTextAppThemes.getRstaTheme(app.getTheme(), getFont());
		} catch (IOException ioe) { // Never happens
			app.displayException(ioe);
		}
		return null;
	}


	public boolean getEmulateTabs() {
		return emulateTabs;
	}


	public Color getFoldBackground() {
		return foldBackground;
	}


	public Color getFoldForeground() {
		return foldForeground;
	}


	public FoldIndicatorStyle getFoldIndicatorStyle() {
		return foldIndicatorStyle;
	}


	public Font getFont() {
		return font;
	}


	public Color getFontColor() {
		return fontColor;
	}


	public boolean getFractionalFontMetricsEnabled() {
		return fractionalFontMetricsEnabled;
	}


	public boolean getHighlightBothBrackets() {
		return highlightBothBrackets;
	}


	public boolean getHighlightCurrentLine() {
		return highlightCurrentLine;
	}


	public boolean getHighlightMatchingBrackets() {
		return highlightMatchingBrackets;
	}


	public boolean getHighlightSecondaryLanguages() {
		return highlightSecondaryLanguages;
	}


	public CaretStyle getInsertCaret() {
		return insertCaret;
	}


	public Color getLineNumberColor() {
		return lineNumberColor;
	}


	public Font getLineNumberFont() {
		return lineNumberFont;
	}


	public boolean getLineNumbersEnabled() {
		return lineNumbersEnabled;
	}


	public boolean getMarginLineEnabled() {
		return marginLineEnabled;
	}


	public int getMarginLinePosition() {
		return marginLinePosition;
	}


	public Color getMarkAllHighlightColor() {
		return markAllHighlightColor;
	}


	public boolean getMarkOccurrences() {
		return markOccurrences;
	}


	public Color getMarkOccurrencesColor() {
		return markOccurrencesColor;
	}


	public boolean getOverrideEditorTheme() {
		return overrideEditorTheme;
	}


	public CaretStyle getOverwriteCaret() {
		return overwriteCaret;
	}


	public String getPreviewLanguage() {
		return previewLanguage;
	}


	public Color getSecondaryLanguageBackground(int index) {
		return secondaryLanguages[index];
	}


	public Color getSelectedTextColor() {
		return selectedTextColor;
	}


	public Color getSelectionColor() {
		return selectionColor;
	}


	public boolean getShowEolMarkers() {
		return showEolMarkers;
	}


	public boolean getShowIndentGuides() {
		return showIndentGuides;
	}


	public boolean getShowWhitespace() {
		return showWhitespace;
	}


	public SyntaxScheme getSyntaxScheme() {
		return syntaxScheme;
	}


	public int getTabSize() {
		return tabSize;
	}


	public boolean getUseSelectedTextColor() {
		return useSelectedTextColor;
	}


	public boolean getWordWrap() {
		return wordWrap;
	}


	public void initialize(RText rtext) {
		font = rtext.getMainView().getTextAreaFont();
		lineNumberFont = font;
		syntaxScheme = rtext.getSyntaxScheme();
		previewLanguage = PreviewPanel.DEFAULT_PREVIEW_LANGUAGE;
		foldIndicatorStyle = FoldIndicatorStyle.MODERN;
	}


	public void possiblyFireChangeEventAndReset() {

		if (dirty) {

			// Guaranteed to return a non-null array
			Object[] listeners = this.listeners.getListenerList();

			// Process the listeners last to first, notifying
			// those that are interested in this event
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == EditorOptionsPreviewContextListener.class) {
					((EditorOptionsPreviewContextListener) listeners[i + 1]).editorOptionsPreviewContextChanged(this);
				}
			}

			dirty = false;
		}
	}


	private void registerChanges() {
		dirty = true;
	}


	/**
	 * Removes a listener.
	 *
	 * @param listener The listener to remove.
	 * @see #addListener(EditorOptionsPreviewContextListener)
	 */
	void removeListener(EditorOptionsPreviewContextListener listener) {
		listeners.remove(EditorOptionsPreviewContextListener.class, listener);
	}


	public void setAntiAliasingEnabled(boolean enable) {
		if (this.antiAliasingEnabled != enable) {
			this.antiAliasingEnabled = enable;
			registerChanges();
		}
	}


	public void setArmedFoldBackground(Color armedFoldBackground) {
		if (!Objects.equals(this.armedFoldBackground, armedFoldBackground)) {
			this.armedFoldBackground = armedFoldBackground;
			registerChanges();
		}
	}


	public void setArmedFoldForeground(Color armedFoldForeground) {
		if (!Objects.equals(this.armedFoldForeground, armedFoldForeground)) {
			this.armedFoldForeground = armedFoldForeground;
			registerChanges();
		}
	}


	public void setAutoInsertClosingCurly(boolean autoInsertClosingCurly) {
		if (this.autoInsertClosingCurly != autoInsertClosingCurly) {
			this.autoInsertClosingCurly = autoInsertClosingCurly;
			registerChanges();
		}
	}


	public void setBackgroundColor(Color backgroundColor) {
		if (!Objects.equals(this.backgroundColor, backgroundColor)) {
			this.backgroundColor = backgroundColor;
			registerChanges();
		}
	}


	public void setCaretBlinkRate(int caretBlinkRate) {
		if (this.caretBlinkRate != caretBlinkRate) {
			this.caretBlinkRate = caretBlinkRate;
			registerChanges();
		}
	}


	public void setCaretColor(Color caretColor) {
		if (!Objects.equals(this.caretColor, caretColor)) {
			this.caretColor = caretColor;
			registerChanges();
		}
	}


	public void setClearWhitespaceLines(boolean remove) {
		if (this.clearWhitespaceLines != remove) {
			this.clearWhitespaceLines = remove;
			registerChanges();
		}
	}


	public void setCurrentLineHighlightColor(Color color) {
		if (this.currentLineHighlightColor != color) {
			this.currentLineHighlightColor = color;
			registerChanges();
		}
	}


	public void setEmulateTabs(boolean emulateTabs) {
		if (this.emulateTabs != emulateTabs) {
			this.emulateTabs = emulateTabs;
			registerChanges();
		}
	}


	public void setFoldBackground(Color foldBackground) {
		if (!Objects.equals(this.foldBackground, foldBackground)) {
			this.foldBackground = foldBackground;
			registerChanges();
		}
	}


	public void setFoldForeground(Color foldForeground) {
		if (!Objects.equals(this.foldForeground, foldForeground)) {
			this.foldForeground = foldForeground;
			registerChanges();
		}
	}


	public void setFoldIndicatorStyle(FoldIndicatorStyle style) {
		if (this.foldIndicatorStyle != style) {
			this.foldIndicatorStyle = style;
			registerChanges();
		}
	}


	public void setFont(Font font) {
		if (!Objects.equals(this.font, font)) {
			this.font = font;
			registerChanges();
		}
	}


	public void setFontColor(Color color) {
		if (!Objects.equals(fontColor, color)) {
			fontColor = color;
			registerChanges();
		}
	}


	public void setFractionalFontMetricsEnabled(boolean enable) {
		if (this.fractionalFontMetricsEnabled != enable) {
			this.fractionalFontMetricsEnabled = enable;
			registerChanges();
		}
	}


	public void setHighlightBothBrackets(boolean highlightBothBrackets) {
		if (this.highlightBothBrackets != highlightBothBrackets) {
			this.highlightBothBrackets = highlightBothBrackets;
			registerChanges();
		}
	}


	public void setHighlightCurrentLine(boolean highlightCurrentLine) {
		if (this.highlightCurrentLine != highlightCurrentLine) {
			this.highlightCurrentLine = highlightCurrentLine;
			registerChanges();
		}
	}


	public void setHighlightMatchingBrackets(boolean highlightMatchingBrackets) {
		if (this.highlightMatchingBrackets != highlightMatchingBrackets) {
			this.highlightMatchingBrackets = highlightMatchingBrackets;
			registerChanges();
		}
	}


	public void setHighlightSecondaryLanguages(boolean highlightSecondaryLanguages) {
		if (this.highlightSecondaryLanguages != highlightSecondaryLanguages) {
			this.highlightSecondaryLanguages = highlightSecondaryLanguages;
			registerChanges();
		}
	}


	public void setInsertCaret(CaretStyle insertCaret) {
		if (this.insertCaret != insertCaret) {
			this.insertCaret = insertCaret;
			registerChanges();
		}
	}


	public void setLineNumberColor(Color lineNumberColor) {
		if (!Objects.equals(this.lineNumberColor, lineNumberColor)) {
			this.lineNumberColor = lineNumberColor;
			registerChanges();
		}
	}


	public void setLineNumberFont(Font font) {
		if (!Objects.equals(this.lineNumberFont, font)) {
			this.lineNumberFont = font;
			registerChanges();
		}
	}


	public void setLineNumbersEnabled(boolean enabled) {
		if (this.lineNumbersEnabled != enabled) {
			this.lineNumbersEnabled = enabled;
			registerChanges();
		}
	}


	public void setMarginLineEnabled(boolean marginLineEnabled) {
		if (this.marginLineEnabled != marginLineEnabled) {
			this.marginLineEnabled = marginLineEnabled;
			registerChanges();
		}
	}


	public void setMarginLinePosition(int marginLinePosition) {
		if (this.marginLinePosition != marginLinePosition) {
			this.marginLinePosition = marginLinePosition;
			registerChanges();
		}
	}


	public void setMarkAllHighlightColor(Color markAllHighlightColor) {
		if (!Objects.equals(this.markAllHighlightColor, markAllHighlightColor)) {
			this.markAllHighlightColor = markAllHighlightColor;
			registerChanges();
		}
	}


	public void setMarkOccurrences(boolean markOccurrences) {
		if (this.markOccurrences != markOccurrences) {
			this.markOccurrences = markOccurrences;
			registerChanges();
		}
	}


	public void setMarkOccurrencesColor(Color markOccurrencesColor) {
		if (!Objects.equals(this.markOccurrencesColor, markOccurrencesColor)) {
			this.markOccurrencesColor = markOccurrencesColor;
			registerChanges();
		}
	}


	public void setOverrideEditorTheme(boolean overrideEditorTheme) {
		if (this.overrideEditorTheme != overrideEditorTheme) {
			this.overrideEditorTheme = overrideEditorTheme;
			registerChanges();
		}
	}


	public void setOverwriteCaret(CaretStyle overwriteCaret) {
		if (this.overwriteCaret != overwriteCaret) {
			this.overwriteCaret = overwriteCaret;
			registerChanges();
		}
	}


	public void setPreviewLanguage(String previewLanguage) {
		if (!Objects.equals(this.previewLanguage, previewLanguage)) {
			this.previewLanguage = previewLanguage;
			registerChanges();
		}
	}


	public void setSecondaryLanguageBackground(int index, Color color) {
		if (!Objects.equals(this.secondaryLanguages[index], color)) {
			this.secondaryLanguages[index] = color;
			registerChanges();
		}
	}


	public void setSelectedTextColor(Color selectedTextColor) {
		if (!Objects.equals(this.selectedTextColor, selectedTextColor)) {
			this.selectedTextColor = selectedTextColor;
			registerChanges();
		}
	}


	public void setSelectionColor(Color selectionColor) {
		if (!Objects.equals(this.selectionColor, selectionColor)) {
			this.selectionColor = selectionColor;
			registerChanges();
		}
	}


	public void setShowEolMarkers(boolean showEolMarkers) {
		if (this.showEolMarkers != showEolMarkers) {
			this.showEolMarkers = showEolMarkers;
			registerChanges();
		}
	}


	public void setShowIndentGuides(boolean showIndentGuides) {
		if (this.showIndentGuides != showIndentGuides) {
			this.showIndentGuides = showIndentGuides;
			registerChanges();
		}
	}


	public void setShowWhitespace(boolean showWhitespace) {
		if (this.showWhitespace != showWhitespace) {
			this.showWhitespace = showWhitespace;
			registerChanges();
		}
	}


	public void setSyntaxScheme(SyntaxScheme scheme) {
		if (!Objects.equals(this.syntaxScheme, scheme)) {
			this.syntaxScheme = scheme;
			registerChanges();
		}
	}


	public void setTabSize(int tabSize) {
		if (this.tabSize != tabSize) {
			this.tabSize = tabSize;
			registerChanges();
		}
	}


	public void setUseSelectedTextColor(boolean useSelectedTextColor) {
		if (this.useSelectedTextColor != useSelectedTextColor) {
			this.useSelectedTextColor = useSelectedTextColor;
			registerChanges();
		}
	}


	public void setWordWrap(boolean wordWrap) {
		if (this.wordWrap != wordWrap) {
			this.wordWrap = wordWrap;
			registerChanges();
		}
	}
}
