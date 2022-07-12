/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

import javax.swing.event.EventListenerList;
import java.awt.*;
import java.util.Objects;

import org.fife.rtext.RText;


/**
 * A wrapper around the styles currently being used in the "preview" text areas
 * in the options dialog. This is necessary to keep the "preview" text areas
 * in the various text area-related option panels in sync.
 */
final class EditorOptionsPreviewContext {

	private boolean overrideTheme;
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
	private Object backgroundObject;
	private SyntaxScheme syntaxScheme;

	private EventListenerList listeners;

	private static final EditorOptionsPreviewContext INSTANCE = new EditorOptionsPreviewContext();


	/**
	 * Private constructor to prevent instantiation.
	 */
	private EditorOptionsPreviewContext() {
		listeners = new EventListenerList();
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


	private void fireChangeEvent() {

		// Guaranteed to return a non-null array
		Object[] listeners = this.listeners.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i >= 0; i -= 2) {
			if (listeners[i] == EditorOptionsPreviewContextListener.class) {
				((EditorOptionsPreviewContextListener)listeners[i + 1]).editorOptionsPreviewContextChanged(this);
			}
		}
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


	public boolean getAutoInsertClosingCurly() {
		return autoInsertClosingCurly;
	}


	public Object getBackgroundObject() {
		return backgroundObject;
	}


	public boolean getClearWhitespaceLines() {
		return clearWhitespaceLines;
	}


	public boolean getEmulateTabs() {
		return emulateTabs;
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


	public boolean getMarginLineEnabled() {
		return marginLineEnabled;
	}


	public int getMarginLinePosition() {
		return marginLinePosition;
	}


	public boolean getOverrideTheme() {
		return overrideTheme;
	}


	public String getPreviewLanguage() {
		return previewLanguage;
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


	public boolean getWordWrap() {
		return wordWrap;
	}


	public void initialize(RText rtext) {
		font = rtext.getMainView().getTextAreaFont();
		syntaxScheme = rtext.getSyntaxScheme();
		previewLanguage = PreviewPanel.DEFAULT_PREVIEW_LANGUAGE;
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
			fireChangeEvent();
		}
	}


	public void setAutoInsertClosingCurly(boolean autoInsertClosingCurly) {
		if (this.autoInsertClosingCurly != autoInsertClosingCurly) {
			this.autoInsertClosingCurly = autoInsertClosingCurly;
			fireChangeEvent();
		}
	}


	public void setBackgroundObject(Object backgroundObject) {
		if (!Objects.equals(this.backgroundObject, backgroundObject)) {
			this.backgroundObject = backgroundObject;
			fireChangeEvent();
		}
	}


	public void setClearWhitespaceLines(boolean remove) {
		if (this.clearWhitespaceLines != remove) {
			this.clearWhitespaceLines = remove;
			fireChangeEvent();
		}
	}


	public void setEmulateTabs(boolean emulateTabs) {
		if (this.emulateTabs != emulateTabs) {
			this.emulateTabs = emulateTabs;
			fireChangeEvent();
		}
	}


	public void setFont(Font font) {
		if (!Objects.equals(this.font, font)) {
			this.font = font;
			fireChangeEvent();
		}
	}


	public void setFontColor(Color color) {
		if (!Objects.equals(fontColor, color)) {
			fontColor = color;
			fireChangeEvent();
		}
	}


	public void setFractionalFontMetricsEnabled(boolean enable) {
		if (this.fractionalFontMetricsEnabled != enable) {
			this.fractionalFontMetricsEnabled = enable;
			fireChangeEvent();
		}
	}


	public void setHighlightBothBrackets(boolean highlightBothBrackets) {
		if (this.highlightBothBrackets != highlightBothBrackets) {
			this.highlightBothBrackets = highlightBothBrackets;
			fireChangeEvent();
		}
	}


	public void setHighlightCurrentLine(boolean highlightCurrentLine) {
		if (this.highlightCurrentLine != highlightCurrentLine) {
			this.highlightCurrentLine = highlightCurrentLine;
			fireChangeEvent();
		}
	}


	public void setHighlightMatchingBrackets(boolean highlightMatchingBrackets) {
		if (this.highlightMatchingBrackets != highlightMatchingBrackets) {
			this.highlightMatchingBrackets = highlightMatchingBrackets;
			fireChangeEvent();
		}
	}


	public void setMarginLineEnabled(boolean marginLineEnabled) {
		if (this.marginLineEnabled != marginLineEnabled) {
			this.marginLineEnabled = marginLineEnabled;
			fireChangeEvent();
		}
	}


	public void setMarginLinePosition(int marginLinePosition) {
		if (this.marginLinePosition != marginLinePosition) {
			this.marginLinePosition = marginLinePosition;
			fireChangeEvent();
		}
	}


	public void setOverrideTheme(boolean overrideTheme) {
		if (this.overrideTheme != overrideTheme) {
			this.overrideTheme = overrideTheme;
			fireChangeEvent();
		}
	}


	public void setPreviewLanguage(String previewLanguage) {
		if (!Objects.equals(this.previewLanguage, previewLanguage)) {
			this.previewLanguage = previewLanguage;
			fireChangeEvent();
		}
	}


	public void setShowEolMarkers(boolean showEolMarkers) {
		if (this.showEolMarkers != showEolMarkers) {
			this.showEolMarkers = showEolMarkers;
			fireChangeEvent();
		}
	}


	public void setShowIndentGuides(boolean showIndentGuides) {
		if (this.showIndentGuides != showIndentGuides) {
			this.showIndentGuides = showIndentGuides;
			fireChangeEvent();
		}
	}


	public void setShowWhitespace(boolean showWhitespace) {
		if (this.showWhitespace != showWhitespace) {
			this.showWhitespace = showWhitespace;
			fireChangeEvent();
		}
	}


	public void setSyntaxScheme(SyntaxScheme scheme) {
		if (!Objects.equals(this.syntaxScheme, scheme)) {
			this.syntaxScheme = scheme;
			fireChangeEvent();
		}
	}


	public void setTabSize(int tabSize) {
		if (this.tabSize != tabSize) {
			this.tabSize = tabSize;
			fireChangeEvent();
		}
	}


	public void setWordWrap(boolean wordWrap) {
		if (this.wordWrap != wordWrap) {
			this.wordWrap = wordWrap;
			fireChangeEvent();
		}
	}
}
