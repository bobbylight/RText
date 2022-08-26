/*
 * 01/21/2004
 *
 * RTextPreferences.java - A class representing several properties of
 * an RText session.
 * Copyright (C) 2004 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JTabbedPane;

import org.fife.ui.OS;
import org.fife.ui.app.prefs.AppPrefs;
import org.fife.ui.app.prefs.TypeLoader;
import org.fife.ui.app.themes.FlatDarkTheme;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.CaretStyle;
import org.fife.ui.rtextarea.RTextArea;


/**
 * A class representing several properties of an RText session.  This class
 * is used for saving user preferences between RText sessions. It consists
 * only of public data members for ease of use.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RTextPrefs extends AppPrefs implements RTextActionInfo {

	/**
	 * The default Look and Feel.
	 */
	private static final String DEFAULT_APP_THEME = FlatDarkTheme.NAME;

	/**
	 * The default maximum number of spelling errors to display for a single
	 * file.
	 */
	public static final int DEFAULT_MAX_SPELLING_ERRORS = 30;

	/**
	 * The default color used to underline spelling errors.
	 */
	public static final Color DEFAULT_SPELLING_ERROR_COLOR = new Color(255,128,64);

	public boolean lineNumbersVisible;
	public int tabSize;							// In spaces.
	public boolean emulateTabsWithSpaces;			// Whether or not to emulate tabs with spaces.
	public int textMode;						// Either RTextArea.INSERT_MODE (1) or RTextArea.OVERWRITE_MODE (2).
	public int tabPlacement;						// One of JTabbedPane.TOP/LEFT/BOTTOM/RIGHT.
	public Font printFont;						// The font ot use when printing.
	public Object backgroundObject;				// Either a Color or an Image.
	public float imageAlpha;						// How "translucent" to make a background image (0.0f - 1.0f).
	public boolean wordWrap;						// Whether or not word wrap is enabled.
	public Color caretColor;
	public Color selectionColor;
	public Color selectedTextColor;
	public boolean useSelectedTextColor;
	public SyntaxScheme colorScheme;	// Color scheme used in syntax highlighting.
	public String syntaxFiltersString;				// String representing the syntax filters.
	public int maxFileHistorySize;
	public String fileHistoryString;				// String representing the file history.
	public boolean currentLineHighlightEnabled;
	public Color currentLineHighlightColor;
	public int mainView;						// Either RText.TABBED_VIEW or RText.SPLIT_PANE_VIEW.
	public boolean overrideEditorStyles;
	public boolean bracketMatchingEnabled;
	public boolean matchBothBrackets;
	public Color matchedBracketBGColor;
	public Color matchedBracketBorderColor;
	public boolean marginLineEnabled;
	public int marginLinePosition;
	public Color marginLineColor;
	public boolean highlightSecondaryLanguages;
	public Color[] secondaryLanguageColors;
	public boolean visibleWhitespace;
	public boolean showEOLMarkers;
	public boolean showTabLines;
	public Color tabLinesColor;
	public boolean rememberWhitespaceLines;
	public boolean autoInsertClosingCurlys;
	public boolean aaEnabled;
	public boolean fractionalMetricsEnabled;
	public Color markAllHighlightColor;
	public boolean markOccurrences;
	public Color markOccurrencesColor;
	public boolean roundedSelectionEdges;
	public String workingDirectory;
	public int[] carets;						// Index 0=>insert, 1=>overwrite.
	public int caretBlinkRate;
	public int[] dividerLocations;				// Dividers for plugin JSplitPanes.
	public boolean[] dividerVisible;
	public String defaultLineTerminator;
	public String defaultEncoding;				// Encoding of new text files.
	public boolean guessFileContentType;
	public boolean doFileSizeCheck;
	public float maxFileSize;					// In MB
	public int maxFileSizeForCodeFolding;		// In MB
	public boolean ignoreBackupExtensions;
	public Font textAreaFont;					// Default text area font.
	public boolean textAreaUnderline;				// Is default font underlined?
	public Color textAreaForeground;
	public ComponentOrientation textAreaOrientation;
	public Color foldBackground;
	public Color armedFoldBackground;
	public boolean showHostName;
	public boolean bomInUtf8;
	public Font lineNumberFont;
	public Color lineNumberColor;
	public Color gutterBorderColor;
	public boolean spellCheckingEnabled;
	public Color spellCheckingColor;
	public String spellingDictionary;
	public File userDictionary;
	public int maxSpellingErrors;
	public boolean viewSpellingList;
	public boolean searchWindowOpacityEnabled;
	public float searchWindowOpacity;
	public int searchWindowOpacityRule;
	public boolean dropShadowsInEditor;
	public String codeFoldingEnabledFor;
	public boolean useSearchDialogs;


	/**
	 * Constructor; initializes all values to "defaults."
	 */
	public RTextPrefs() {
		setDefaults();
		addAppSpecificTypeLoaders();
	}


	private void addAppSpecificTypeLoaders() {
		addTypeLoader(SyntaxScheme.class, new SyntaxSchemeTypeLoader());
	}


	private static boolean getDefaultDropShadowsInEditorValue() {
		return OS.get() == OS.WINDOWS;
	}


	private static Font getFontImpl(String str) {
		StringTokenizer t2 = new StringTokenizer(str, ",");
		String fontName = t2.nextToken();
		Font font = null;
		if (!fontName.equals("null")) {
			int fontSize = Integer.parseInt(t2.nextToken());
			boolean isBold = Boolean.parseBoolean(t2.nextToken());
			boolean isItalic = Boolean.parseBoolean(t2.nextToken());
			int fontStyle = Font.PLAIN;
			if (isBold) {
				if (isItalic)
					fontStyle = Font.BOLD | Font.ITALIC;
				else
					fontStyle = Font.BOLD;
			}
			else if (isItalic)
				fontStyle = Font.ITALIC;
			font = new Font(fontName, fontStyle, fontSize);
		}
		return font;
	}


	/**
	 * Tokens with background colors set have serious performance implications
	 * on certain platforms (OS X, and even now Windows with JDK6u10+).  The
	 * problem is the use of Graphics#setXORMode(), which is almost unusably
	 * slow on these platforms.  For this reason, we check for any token types
	 * that have a background color set and equal to the text area background
	 * color.  If any matches are found, these tokens have their backgrounds
	 * instead set to <code>null</code>, as their background painting wouldn't
	 * be seen anyway, to improve performance.<p>
	 *
	 * Older RText releases might have had a problem where certain actions in
	 * the Options dialog would erroneously cause tokens to have a Color.WHITE
	 * background when in fact they should be <code>null</code>, so this
	 * check is particularly important moving forward.
	 *
	 * @param props The preferences object to check.
	 */
	private static void possiblyFixSyntaxSchemeBackground(
												RTextPrefs props) {
		Object bgObj = props.backgroundObject;
		if (bgObj instanceof Color) {
			Color color = (Color)bgObj;
			for (int i=0; i<props.colorScheme.getStyleCount(); i++) {
				Style s = props.colorScheme.getStyle(i);
				// Some schemes are null (generic token types)
				if (s!=null && color.equals(s.background)) {
					s.background = null;
				}
			}
		}
	}


	@Override
	public void setDefaults() {

		Theme theme;
		try {
			theme = Theme.load(getClass().getResourceAsStream(
				"/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		}

		location = new Point(0,0);
		size = new Dimension(650,500);
		appTheme = DEFAULT_APP_THEME;
		toolbarVisible = true;
		statusBarVisible = true;
		lineNumbersVisible = true;
		tabSize = RTextArea.getDefaultTabSize();
		emulateTabsWithSpaces = false;
		textMode = RTextArea.INSERT_MODE;
		tabPlacement = JTabbedPane.TOP;
		printFont = null;	// i.e., use RText's font.
		backgroundObject = theme.bgColor;
		imageAlpha = 0.3f;	// Arbitrary initial value.
		wordWrap = false;
		caretColor = theme.caretColor;
		selectionColor = theme.selectionBG;
		selectedTextColor = theme.selectionFG;
		useSelectedTextColor = theme.useSelectionFG;
		colorScheme = theme.scheme;
		SyntaxFilters syntaxFilters = new SyntaxFilters();
		syntaxFilters.restoreDefaultFileFilters();
		syntaxFiltersString = syntaxFilters.toString();
		maxFileHistorySize = 20;
		fileHistoryString = null;
		currentLineHighlightEnabled = true;
		currentLineHighlightColor = theme.currentLineHighlight;
		mainView = RText.TABBED_VIEW;
		overrideEditorStyles = false;
		language = "en";	// Default to English.
		bracketMatchingEnabled = true;
		matchBothBrackets = false;
		matchedBracketBGColor = theme.matchedBracketBG;
		matchedBracketBorderColor = theme.matchedBracketFG;
		marginLineEnabled = true;
		marginLinePosition = RTextArea.getDefaultMarginLinePosition();
		marginLineColor = theme.marginLineColor;
		highlightSecondaryLanguages = true;
		secondaryLanguageColors = new Color[3];
		secondaryLanguageColors[0] = theme.secondaryLanguages[0];
		secondaryLanguageColors[1] = theme.secondaryLanguages[1];
		secondaryLanguageColors[2] = theme.secondaryLanguages[2];
		visibleWhitespace = false;
		showEOLMarkers = false;
		showTabLines = false;
		tabLinesColor = theme.marginLineColor;
		rememberWhitespaceLines = true;
		autoInsertClosingCurlys = false;
		aaEnabled = File.separatorChar=='\\' || OS.get() == OS.MAC_OS_X;
		fractionalMetricsEnabled = false;
		markAllHighlightColor = theme.markAllHighlightColor;
		markOccurrences = true;
		markOccurrencesColor = theme.markOccurrencesColor;
		roundedSelectionEdges = false;
		workingDirectory = System.getProperty("user.dir");
		carets = new int[2];
		carets[RTextArea.INSERT_MODE] = CaretStyle.THICK_VERTICAL_LINE_STYLE.ordinal();
		carets[RTextArea.OVERWRITE_MODE] = CaretStyle.BLOCK_STYLE.ordinal();
		caretBlinkRate = 500;
		dividerLocations = new int[4];
		for (int i=0; i<4; i++) {
			// negative => left components preferred size.
			// A negative value also helps the component know when it isn't expanded
			dividerLocations[i] = -1;
		}
		dividerVisible = new boolean[4];
		for (int i = 0; i < 4; i++) {
			dividerVisible[i] = false;
		}
		defaultLineTerminator = null; // Use system default.
		defaultEncoding = null; // Use system default encoding.
		guessFileContentType = true;
		doFileSizeCheck	= true;
		maxFileSize		= 10f;	// MB
		maxFileSizeForCodeFolding = 10; // MB
		ignoreBackupExtensions = true;
		textAreaFont		= RTextArea.getDefaultFont();
		textAreaUnderline	= false;
		textAreaForeground	= RTextArea.getDefaultForeground();
		textAreaOrientation = ComponentOrientation.LEFT_TO_RIGHT;
		foldBackground		= theme.foldBG;
		armedFoldBackground	= theme.armedFoldBG;
		showHostName		= false;
		bomInUtf8			= false;
		lineNumberFont		= new Font(textAreaFont.getName(), Font.PLAIN, 12);
		lineNumberColor	= theme.lineNumberColor;
		gutterBorderColor	= theme.gutterBorderColor;
		spellCheckingEnabled = File.separatorChar=='\\';
		spellCheckingColor   = DEFAULT_SPELLING_ERROR_COLOR;
		spellingDictionary   = SpellingSupport.DICTIONARIES[1];
		userDictionary       = new File(RTextUtilities.getPreferencesDirectory(),
										"userDictionary.txt");
		maxSpellingErrors    = DEFAULT_MAX_SPELLING_ERRORS;
		viewSpellingList   = false;
		searchWindowOpacityEnabled = false;
		searchWindowOpacity		= 0.6f;
		searchWindowOpacityRule = ChildWindowListener.
										TRANSLUCENT_WHEN_OVERLAPPING_APP;
		dropShadowsInEditor = getDefaultDropShadowsInEditorValue();
		codeFoldingEnabledFor = "";
		useSearchDialogs = true;

	}


	/**
	 * Returns a <code>String</code> representation of this object.
	 *
	 * @return A string representation of this object.
	 */
	@Override
	public String toString() {
		return "[Class: RTextPrefs]";
	}


	private static class SyntaxSchemeTypeLoader implements TypeLoader<SyntaxScheme> {

		@Override
		public SyntaxScheme load(String name, String value, Properties props) {
			return SyntaxScheme.loadFromString(value); // Handles nulls
		}

		@Override
		public String save(String name, Object value, Properties props) {
			return value != null ? ((SyntaxScheme)value).toCommaSeparatedString() : null;
		}
	}
}
