/*
 * 01/21/2004
 *
 * RTextPreferences.java - A class representing several properties of
 * an RText session.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.io.File;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.fife.rtext.SearchManager.SearchingMode;
import org.fife.ui.StatusBar;
import org.fife.ui.app.GUIApplicationPreferences;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rtextarea.ConfigurableCaret;
import org.fife.ui.rtextarea.RTextArea;


/**
 * A class representing several properties of an RText session.  This class
 * is used for saving user preferences between RText sessions. It consists
 * only of public data members for ease of use.
 *
 * @author Robert Futrell
 * @version 0.3
 */
public class RTextPreferences extends GUIApplicationPreferences
							implements RTextActionInfo {

	/**
	 * The default maximum number of spelling errors to display for a single
	 * file.
	 */
	public static final int DEFAULT_MAX_SPELLING_ERRORS = 30;

	/**
	 * The default color used to underline spelling errors.
	 */
	public static final Color DEFAULT_SPELLING_ERROR_COLOR = new Color(255,128,64);

	private static final String NOTHING_STRING = "-";


	public String iconGroupName;
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
	public boolean highlightModifiedDocNames;
	public Color modifiedDocumentNamesColor;
	public boolean bracketMatchingEnabled;
	public boolean matchBothBrackets;
	public Color matchedBracketBGColor;
	public Color matchedBracketBorderColor;
	public boolean marginLineEnabled;
	public int marginLinePosition;
	public Color marginLineColor;
	public boolean highlightSecondaryLanguages;
	public Color[] secondaryLanguageColors;
	public boolean hyperlinksEnabled;
	public Color hyperlinkColor;
	public int hyperlinkModifierKey;
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
	public int statusBarStyle;
	public boolean roundedSelectionEdges;
	public String workingDirectory;
	public int[] carets;						// Index 0=>insert, 1=>overwrite.
	public int caretBlinkRate;
	public boolean searchToolBarVisible;
	public int[] dividerLocations;				// Dividers for plugin JSplitPanes.
	public String defaultLineTerminator;
	public String defaultEncoding;				// Encoding of new text files.
	public boolean guessFileContentType;
	public boolean doFileSizeCheck;
	public float maxFileSize;					// In MB.
	public boolean ignoreBackupExtensions;
	public Font textAreaFont;					// Default text area font.
	public boolean textAreaUnderline;				// Is default font underlined?
	public Color textAreaForeground;
	public ComponentOrientation textAreaOrientation;
	public boolean showHostName;
	public boolean bomInUtf8;
	public boolean bookmarksEnabled;
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

	public KeyStroke[] mainViewActionAccelerators;


	/**
	 * Constructor; initializes all values to "defaults."
	 */
	private RTextPreferences() {
		setDefaults();
	}


	/**
	 * Creates a properties object with all fields initialized to the values
	 * that the specified RText instance is currently running with.
	 *
	 * @return An <code>RTextPreferences</code> object initialized to contain
	 *         the properties the specified RText instance is running with.
	 */
	public static GUIApplicationPreferences generatePreferences(RText rtext) {

		AbstractMainView mainView = rtext.getMainView();
		SpellingSupport spelling = mainView.getSpellingSupport();
		RTextMenuBar menuBar = (RTextMenuBar)rtext.getJMenuBar();

		//String lnfString = UIManager.getLookAndFeel().getClass().getName();
		String lnfString = RTextUtilities.getLookAndFeelToSave();

		RTextPreferences props = new RTextPreferences();
		props.location				= rtext.getLocation();
		props.location.translate(15,15);
		props.size					= rtext.isMaximized() ? new Dimension(-1,-1) : rtext.getSize();
		props.lookAndFeel				= lnfString;
		props.iconGroupName				= rtext.getIconGroup().getName();
		props.toolbarVisible			= rtext.getToolBar().isVisible();
		props.statusBarVisible			= rtext.getStatusBar().isVisible();
		props.lineNumbersVisible			= mainView.getLineNumbersEnabled();
		props.tabSize					= mainView.getTabSize();
		props.emulateTabsWithSpaces		= mainView.areTabsEmulated();
		props.textMode					= mainView.getTextMode();
		props.tabPlacement				= mainView.getDocumentSelectionPlacement();
		props.printFont				= mainView.getPrintFont();
		props.backgroundObject			= mainView.getBackgroundObject();
		props.imageAlpha				= mainView.getBackgroundImageAlpha();
		props.wordWrap					= mainView.getLineWrap();
		props.caretColor				= mainView.getCaretColor();
		props.selectionColor			= mainView.getSelectionColor();
		props.selectedTextColor			= mainView.getSelectedTextColor();
		props.useSelectedTextColor		= mainView.getUseSelectedTextColor();
		props.colorScheme				= rtext.getSyntaxScheme();
		props.syntaxFiltersString		= mainView.getSyntaxFilters().toString();
		props.maxFileHistorySize			= menuBar.getMaximumFileHistorySize();
		props.fileHistoryString			= menuBar.getFileHistoryString();
		props.currentLineHighlightEnabled	= mainView.isCurrentLineHighlightEnabled();
		props.currentLineHighlightColor	= mainView.getCurrentLineHighlightColor();
		props.mainView					= rtext.getMainViewStyle();
		props.highlightModifiedDocNames	= mainView.highlightModifiedDocumentDisplayNames();
		props.modifiedDocumentNamesColor	= mainView.getModifiedDocumentDisplayNamesColor();
		props.language					= rtext.getLanguage();
		props.bracketMatchingEnabled		= mainView.isBracketMatchingEnabled();
		props.matchBothBrackets			= mainView.getMatchBothBrackets();
		props.matchedBracketBGColor		= mainView.getMatchedBracketBGColor();
		props.matchedBracketBorderColor	= mainView.getMatchedBracketBorderColor();
		props.marginLineEnabled			= mainView.isMarginLineEnabled();
		props.marginLinePosition			= mainView.getMarginLinePosition();
		props.marginLineColor			= mainView.getMarginLineColor();
		props.highlightSecondaryLanguages = mainView.getHighlightSecondaryLanguages();
		props.secondaryLanguageColors = new Color[3];
		for (int i=0; i<props.secondaryLanguageColors.length; i++) {
			props.secondaryLanguageColors[i] = mainView.getSecondaryLanguageColor(i);
		}
		props.hyperlinksEnabled			= mainView.getHyperlinksEnabled();
		props.hyperlinkColor			= mainView.getHyperlinkColor();
		props.hyperlinkModifierKey		= mainView.getHyperlinkModifierKey();
		props.visibleWhitespace			= mainView.isWhitespaceVisible();
		props.showEOLMarkers			= mainView.getShowEOLMarkers();
		props.showTabLines				= mainView.getShowTabLines();
		props.tabLinesColor				= mainView.getTabLinesColor();
		props.rememberWhitespaceLines	= mainView.getRememberWhitespaceLines();
		props.autoInsertClosingCurlys	= mainView.getAutoInsertClosingCurlys();
		props.aaEnabled					= mainView.isAntiAliasEnabled();
		props.fractionalMetricsEnabled	= mainView.isFractionalFontMetricsEnabled();
		props.markAllHighlightColor		= mainView.getMarkAllHighlightColor();
		props.markOccurrences			= mainView.getMarkOccurrences();
		props.markOccurrencesColor		= mainView.getMarkOccurrencesColor();
		props.statusBarStyle			= rtext.getStatusBar().getStyle();
		props.roundedSelectionEdges		= mainView.getRoundedSelectionEdges();
		props.workingDirectory			= rtext.getWorkingDirectory();
		props.carets[RTextArea.INSERT_MODE]= mainView.getCaretStyle(RTextArea.INSERT_MODE);
		props.carets[RTextArea.OVERWRITE_MODE]= mainView.getCaretStyle(RTextArea.OVERWRITE_MODE);
		props.caretBlinkRate			= mainView.getCaretBlinkRate();
		props.searchToolBarVisible		= rtext.isSearchToolBarVisible();
		props.dividerLocations[RText.TOP]	= rtext.getSplitPaneDividerLocation(RText.TOP);
		props.dividerLocations[RText.LEFT] = rtext.getSplitPaneDividerLocation(RText.LEFT);
		props.dividerLocations[RText.BOTTOM] = rtext.getSplitPaneDividerLocation(RText.BOTTOM);
		props.dividerLocations[RText.RIGHT]= rtext.getSplitPaneDividerLocation(RText.RIGHT);
		props.defaultLineTerminator		= mainView.getLineTerminator();
		props.defaultEncoding			= mainView.getDefaultEncoding();
		props.guessFileContentType		= mainView.getGuessFileContentType();
		props.doFileSizeCheck			= mainView.getDoFileSizeCheck();
		props.maxFileSize				= mainView.getMaxFileSize();
		props.ignoreBackupExtensions	= mainView.getIgnoreBackupExtensions();
		props.textAreaFont				= mainView.getTextAreaFont();
		props.textAreaUnderline			= mainView.getTextAreaUnderline();
		props.textAreaForeground			= mainView.getTextAreaForeground();
		props.textAreaOrientation		= mainView.getTextAreaOrientation();
		props.showHostName				= rtext.getShowHostName();
		props.bomInUtf8				= mainView.getWriteBOMInUtf8Files();
		props.bookmarksEnabled			= mainView.getBookmarksEnabled();
		props.lineNumberFont			= mainView.getLineNumberFont();
		props.lineNumberColor			= mainView.getLineNumberColor();
		props.gutterBorderColor			= mainView.getGutterBorderColor();
		props.spellCheckingEnabled		= spelling.isSpellCheckingEnabled();
		props.spellCheckingColor		= spelling.getSpellCheckingColor();
		props.spellingDictionary		= spelling.getSpellingDictionary();
		props.userDictionary			= spelling.getUserDictionary();
		props.maxSpellingErrors			= spelling.getMaxSpellingErrors();
		props.viewSpellingList			= rtext.isSpellingWindowVisible();
		props.searchWindowOpacityEnabled= rtext.isSearchWindowOpacityEnabled();
		props.searchWindowOpacity		= rtext.getSearchWindowOpacity();
		props.searchWindowOpacityRule	= rtext.getSearchWindowOpacityRule();
		props.dropShadowsInEditor		= RTextUtilities.getDropShadowsEnabledInEditor();
		props.codeFoldingEnabledFor		= mainView.getCodeFoldingEnabledForString();

		props.useSearchDialogs			= mainView.getSearchManager().
								getSearchingMode()==SearchingMode.DIALOGS;

		// Save the actions.
		props.accelerators = new HashMap<String, KeyStroke>();
		for (int i=0; i<RText.actionNames.length; i++) {
			String actionName = RText.actionNames[i];
			Action a = rtext.getAction(actionName);
			KeyStroke ks = null;
			// Check for null action here as sometimes we have actions in
			// RText that aren't yet "defined" but are "declared".
			if (a!=null) {
				ks = (KeyStroke)a.getValue(Action.ACCELERATOR_KEY);
			}
			props.accelerators.put(actionName, ks);
		}

		return props;

	}


	private static boolean getDefaultDropShadowsInEditorValue() {
		return !RTextUtilities.isPreJava6() && File.separatorChar=='\\';
	}


	private static Font getFontImpl(String str) {
		StringTokenizer t2 = new StringTokenizer(str, ",");
		String fontName = t2.nextToken();
		Font font = null;
		if (!fontName.equals("null")) {
			int fontSize = Integer.parseInt(t2.nextToken());
			boolean isBold = Boolean.valueOf(t2.nextToken()).booleanValue();
			boolean isItalic = Boolean.valueOf(t2.nextToken()).booleanValue();
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
	 * Returns the keystroke from the passed-in string of the form
	 * "&lt;keycode&gt; &lt;modifiers&gt;".
	 *
	 * @param string The string from which to get the keystroke.  This string
	 *        was saved by a previous <code>RTextPreferences</code>.
	 * @return The keystroke.
	 * @see #getKeyStrokeString
	 */
	private static final KeyStroke getKeyStrokeFromString(String string) {
		int space = string.indexOf(' ');
		if (space>-1) {
			return KeyStroke.getKeyStroke(
						Integer.parseInt(string.substring(0,space)),
						Integer.parseInt(string.substring(space+1)));
		}
		return null;
	}


	/**
	 * Returns a string suitable for saving this keystroke via the Java
	 * Preferences API of the form "<keycode> <modifiers>".
	 *
	 * @param stroke The keystroke for which to get the string.
	 * @return A <code>String</code> representing the keystroke.
	 * @see #getKeyStrokeFromString
	 */
	private static final String getKeyStrokeString(KeyStroke stroke) {
		if (stroke!=null) {
			return stroke.getKeyCode() + " " + stroke.getModifiers();
		}
		return NOTHING_STRING;
	}


	/**
	 * Returns just the LookAndFeel saved in the application preferences.
	 * This is so we can set the LAF before loading the application, to allow
	 * finnicky LAF's like Substance to work properly (Substance only works if
	 * it's set before the first JFrame is created).
	 *
	 * @return The name of the LookAndFeel to load, or the system default LAF
	 *         if no LAF is currently saved.
	 */
	public static String getLookAndFeelToLoad() {
		Preferences prefs = Preferences.userNodeForPackage(RText.class);
		String defaultLAF = UIManager.getSystemLookAndFeelClassName();
		String laf = prefs.get("lookAndFeel", defaultLAF);
		return laf;
	}


	/**
	 * Returns a preferences instance with data saved previously via the
	 * Java Preferences API.  If the load fails, this preferences instance
	 * will be populated with default values.
	 *
	 * @return If the load went okay, <code>true</code> is returned.  If the
	 *         load failed default values will be set.
	 */
	public static GUIApplicationPreferences loadPreferences() {

		RTextPreferences props = new RTextPreferences();

		try {

			// Get all properties associated with the RText class.
			Preferences prefs = Preferences.userNodeForPackage(RText.class);
			loadCommonPreferences(props, prefs);
			props.iconGroupName				= prefs.get("iconGroupName", props.iconGroupName);
			props.lineNumbersVisible			= prefs.getBoolean("lineNumbersVisible", props.lineNumbersVisible);
			props.tabSize					= prefs.getInt("tabSize", props.tabSize);
			props.mainView					= prefs.getInt("mainView", props.mainView);
			String temp					= prefs.get("colorScheme", null);
			props.colorScheme				= SyntaxScheme.loadFromString(temp);
			props.statusBarStyle			= prefs.getInt("statusBarStyle", props.statusBarStyle);
			props.workingDirectory			= prefs.get("workingDirectory", props.workingDirectory);
			props.searchToolBarVisible		= prefs.getBoolean("searchToolBarVisible", props.searchToolBarVisible);
			props.dividerLocations[RText.TOP]	= prefs.getInt("pluginDividerLocation.top", props.dividerLocations[RText.TOP]);
			props.dividerLocations[RText.LEFT] = prefs.getInt("pluginDividerLocation.left", props.dividerLocations[RText.LEFT]);
			props.dividerLocations[RText.BOTTOM] = prefs.getInt("pluginDividerLocation.bottom", props.dividerLocations[RText.BOTTOM]);
			props.dividerLocations[RText.RIGHT]= prefs.getInt("pluginDividerLocation.right", props.dividerLocations[RText.RIGHT]);
			props.showHostName				= prefs.getBoolean("showHostName", props.showHostName);
			props.bomInUtf8				= prefs.getBoolean("bomInUtf8", props.bomInUtf8);
			props.bookmarksEnabled			= prefs.getBoolean("bookmarksEnabled", props.bookmarksEnabled);
			temp							= prefs.get("lineNumberFont", null);
			if (temp!=null) {
				props.lineNumberFont			= getFontImpl(temp);
			}
			props.lineNumberColor			= new Color(
				prefs.getInt("lineNumberColor", props.lineNumberColor.getRGB()));
			props.gutterBorderColor			= new Color(
				prefs.getInt("gutterBorderColor", props.gutterBorderColor.getRGB()));

			// Get all properties associated with the MainView class.
			prefs = Preferences.userNodeForPackage(AbstractMainView.class);
			props.bracketMatchingEnabled		= prefs.getBoolean("bracketMatchingEnabled", props.bracketMatchingEnabled);
			props.matchBothBrackets				= prefs.getBoolean("matchBothBrackets", props.matchBothBrackets);
			props.currentLineHighlightEnabled	= prefs.getBoolean("currentLineHighlightEnabled", props.currentLineHighlightEnabled);
			props.emulateTabsWithSpaces		= prefs.getBoolean("emulateTabs", props.emulateTabsWithSpaces);
			props.highlightModifiedDocNames	= prefs.getBoolean("highlightModifiedDocNames", props.highlightModifiedDocNames);
			props.imageAlpha				= prefs.getFloat("imageAlpha", props.imageAlpha);
			props.marginLineEnabled			= prefs.getBoolean("marginLineEnabled", props.marginLineEnabled);
			props.marginLinePosition			= prefs.getInt("marginLinePosition", props.marginLinePosition);
			props.syntaxFiltersString		= prefs.get("syntaxFilters", props.syntaxFiltersString);
			props.textMode					= prefs.getInt("textMode", props.textMode);
			props.tabPlacement				= prefs.getInt("tabPlacement", props.tabPlacement);
			props.wordWrap					= prefs.getBoolean("wordWrap", props.wordWrap);
			temp							= prefs.get("printFont", null);
			if (temp!=null) {
				props.printFont = getFontImpl(temp);
			}
			temp						= prefs.get("backgroundObject", null);
			if (temp!=null) {
				StringTokenizer t2 = new StringTokenizer(temp, ",");
				String temp2 = t2.nextToken();
				if (temp2.equals("color"))
					props.backgroundObject = new Color(
						Integer.parseInt(temp.substring(temp.indexOf(',')+1)));
				else if (temp2.equals("image"))
					props.backgroundObject = t2.nextToken();
				else
					props.backgroundObject = Color.WHITE;
			}
			possiblyFixSyntaxSchemeBackground(props);
			props.caretColor = new Color(
				prefs.getInt("caretColor", props.caretColor.getRGB()));
			props.selectionColor = new Color(
				prefs.getInt("selectionColor", props.selectionColor.getRGB()), true);
			props.selectedTextColor = new Color(
					prefs.getInt("selectedTextColor", props.selectedTextColor.getRGB()), true);
			props.useSelectedTextColor = prefs.getBoolean("useSelectedTextColor", props.useSelectedTextColor);
			props.currentLineHighlightColor = new Color(
				prefs.getInt("currentLineHighlightColor", props.currentLineHighlightColor.getRGB()), true);
			props.modifiedDocumentNamesColor = new Color(
				prefs.getInt("modifiedDocumentNamesColor", props.modifiedDocumentNamesColor.getRGB()));
			props.matchedBracketBGColor = new Color(
				prefs.getInt("matchedBracketBGColor", props.matchedBracketBGColor.getRGB()));
			props.matchedBracketBorderColor = new Color(
				prefs.getInt("matchedBracketBorderColor", props.matchedBracketBorderColor.getRGB()));
			props.marginLineColor = new Color(
				prefs.getInt("marginLineColor", props.marginLineColor.getRGB()));
			props.highlightSecondaryLanguages = prefs.getBoolean("highlightSecondaryLanguages", props.highlightSecondaryLanguages);
			for (int i=0; i<props.secondaryLanguageColors.length; i++) {
				props.secondaryLanguageColors[i] = new Color(prefs.getInt(
				"secondaryLanguageColor_" + i, props.secondaryLanguageColors[i].getRGB()));
			}
			props.hyperlinksEnabled = prefs.getBoolean("hyperlinksEnabled", props.hyperlinksEnabled);
			props.hyperlinkColor = new Color(
				prefs.getInt("hyperlinkColor", props.hyperlinkColor.getRGB()));
			props.hyperlinkModifierKey		= prefs.getInt("hyperlinkModifierKey", props.hyperlinkModifierKey);
			props.visibleWhitespace			= prefs.getBoolean("visibleWhitespace", props.visibleWhitespace);
			props.showEOLMarkers			= prefs.getBoolean("showEOL", props.showEOLMarkers);
			props.showTabLines				= prefs.getBoolean("showTabLines", props.showTabLines);
			props.tabLinesColor				= new Color(
				prefs.getInt("tabLinesColor", props.tabLinesColor.getRGB()));
			props.rememberWhitespaceLines	= prefs.getBoolean("rememberWhitespaceLines", props.rememberWhitespaceLines);
			props.autoInsertClosingCurlys	= prefs.getBoolean("autoInsertClosingCurlys", props.autoInsertClosingCurlys);
			props.aaEnabled					= prefs.getBoolean("editorAntiAlias", props.aaEnabled);
			props.fractionalMetricsEnabled	= prefs.getBoolean("fractionalMetrics", props.fractionalMetricsEnabled);
			props.markAllHighlightColor = new Color(
				prefs.getInt("markAllHighlightColor", props.markAllHighlightColor.getRGB()));
			props.markOccurrences		= prefs.getBoolean("markOccurrences", props.markOccurrences);
			props.markOccurrencesColor	= new Color(
				prefs.getInt("markOccurrencesColor", props.markOccurrencesColor.getRGB()));
			props.roundedSelectionEdges	= prefs.getBoolean("roundedSelectionEdges", props.roundedSelectionEdges);
			props.carets[RTextArea.INSERT_MODE]= prefs.getInt("insertCaretStyle", props.carets[RTextArea.INSERT_MODE]);
			props.carets[RTextArea.OVERWRITE_MODE]= prefs.getInt("overwriteCaretStyle", props.carets[RTextArea.OVERWRITE_MODE]);
			props.caretBlinkRate		= prefs.getInt("caretBlinkRate", props.caretBlinkRate);
			props.defaultLineTerminator	= prefs.get("defaultLineTerminator", props.defaultLineTerminator);
			if ("".equals(props.defaultLineTerminator)) {
				props.defaultLineTerminator = null;
			}
			props.defaultEncoding		= prefs.get("defaultEncoding", props.defaultEncoding);
			if ("".equals(props.defaultEncoding)) {
				props.defaultEncoding = null;
			}
			props.guessFileContentType	= prefs.getBoolean("guessFileContentType", props.guessFileContentType);
			props.doFileSizeCheck		= prefs.getBoolean("fileSizeCheck", props.doFileSizeCheck);
			props.maxFileSize			= prefs.getFloat("maxFileSize", props.maxFileSize);
			props.ignoreBackupExtensions= prefs.getBoolean("ignoreBackupExtensions", props.ignoreBackupExtensions);
			temp						= prefs.get("textAreaFont", null);
			if (temp!=null) {
				props.textAreaFont = getFontImpl(temp);
			}
			props.textAreaUnderline		= prefs.getBoolean("textAreaUnderline", props.textAreaUnderline);
			props.textAreaForeground		= new Color(
				prefs.getInt("textAreaForeground", props.textAreaForeground.getRGB()));
			temp = prefs.get("textAreaOrientation", "ltr");
			props.textAreaOrientation	= "rtl".equals(temp) ?
						ComponentOrientation.RIGHT_TO_LEFT :
						ComponentOrientation.LEFT_TO_RIGHT;
			props.spellCheckingEnabled	= prefs.getBoolean("spellCheckingEnabled", props.spellCheckingEnabled);
			props.spellCheckingColor	= new Color(
				prefs.getInt("spellCheckingColor", props.spellCheckingColor.getRGB()));
			props.spellingDictionary	= prefs.get("spellingDictionary", props.spellingDictionary);
			String tempVal = prefs.get("userDictionary", null);
			if (tempVal!=null) { // No previous value => use default already set
				if (tempVal.length()==0) { // Empty string => no user dictioanry
					props.userDictionary = null;
				}
				else {
					props.userDictionary = new File(tempVal);
				}
			}
			props.maxSpellingErrors		= prefs.getInt("maxSpellingErrors", props.maxSpellingErrors);
			props.viewSpellingList	= prefs.getBoolean("viewSpellingList", props.viewSpellingList);
			props.searchWindowOpacityEnabled = prefs.getBoolean("searchWindowOpacityEnabled", props.searchWindowOpacityEnabled);
			props.searchWindowOpacity = prefs.getFloat("searchWindowOpacity", props.searchWindowOpacity);
			props.searchWindowOpacityRule = prefs.getInt("searchWindowOpacityRule", props.searchWindowOpacityRule);
			props.dropShadowsInEditor = prefs.getBoolean("dropShadowsInEditor", getDefaultDropShadowsInEditorValue());
			props.codeFoldingEnabledFor = prefs.get("codeFoldingEnabledFor", props.codeFoldingEnabledFor);
			props.useSearchDialogs = prefs.getBoolean("useSearchDialogs", props.useSearchDialogs);

			// Get all properties associated with the RTextMenuBar class.
			prefs = Preferences.userNodeForPackage(RTextMenuBar.class);
			props.fileHistoryString		= prefs.get("fileHistoryString", props.fileHistoryString);
			props.maxFileHistorySize		= prefs.getInt("maxFileHistorySize", props.maxFileHistorySize);

			// Get the accelerators for all actions.
			for (int i=0; i<RText.actionNames.length; i++) {
				String actionName = RText.actionNames[i];
				temp = prefs.get(actionName, null);
				if (temp!=null)
					props.accelerators.put(actionName,
								getKeyStrokeFromString(temp));
			}

		// If anything at all goes wrong, just use default property values.
		} catch (RuntimeException re) {
			throw re; // Let RuntimeExceptions through (FindBugs warning)
		} catch (Exception e) {
			e.printStackTrace();
			props.setDefaults();
		}

		return props;

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
												RTextPreferences props) {
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


	/**
	 * Saves this preferences instance via the Java Preferences API.
	 *
	 * @param rtext The rtext instance for which you are saving preferences.
	 */
	@Override
	public void savePreferences(Object rtext) {

		// Save all properties related to the RText class.
		Preferences prefs = Preferences.userNodeForPackage(RText.class);
		saveCommonPreferences(prefs);
		prefs.put("colorScheme",						colorScheme.toCommaSeparatedString());
		prefs.put("iconGroupName",					iconGroupName);
		prefs.putBoolean("lineNumbersVisible",			lineNumbersVisible);
		prefs.putInt("mainView",						mainView);
		prefs.putInt("statusBarStyle",				statusBarStyle);
		prefs.put("workingDirectory",				workingDirectory);
		prefs.putBoolean("searchToolBarVisible",		searchToolBarVisible);
		prefs.putInt("pluginDividerLocation.top",		dividerLocations[RText.TOP]);
		prefs.putInt("pluginDividerLocation.left",		dividerLocations[RText.LEFT]);
		prefs.putInt("pluginDividerLocation.bottom",		dividerLocations[RText.BOTTOM]);
		prefs.putInt("pluginDividerLocation.right",		dividerLocations[RText.RIGHT]);
		prefs.putBoolean("showHostName",				showHostName);
		prefs.putBoolean("bomInUtf8",					bomInUtf8);
		prefs.putBoolean("bookmarksEnabled",			bookmarksEnabled);
		prefs.put("lineNumberFont",					lineNumberFont==null ? "null" : lineNumberFont.getName() + ","
													+ lineNumberFont.getSize() + "," + lineNumberFont.isBold() +
													"," + lineNumberFont.isItalic());
		prefs.putInt("lineNumberColor",				lineNumberColor.getRGB());
		prefs.putInt("gutterBorderColor",				gutterBorderColor.getRGB());

		// Save all properties related to the AbstractMainView class.
		prefs = Preferences.userNodeForPackage(AbstractMainView.class);
		prefs.putBoolean("bracketMatchingEnabled",		bracketMatchingEnabled);
		prefs.putBoolean("matchBothBrackets",			matchBothBrackets);
		prefs.putInt("caretColor",				caretColor.getRGB());
		prefs.putInt("currentLineHighlightColor",	currentLineHighlightColor.getRGB());
		prefs.putBoolean("currentLineHighlightEnabled",	currentLineHighlightEnabled);
		prefs.putBoolean("emulateTabs",				emulateTabsWithSpaces);
		prefs.putBoolean("highlightModifiedDocNames",highlightModifiedDocNames);
		prefs.putFloat("imageAlpha",					imageAlpha);
		prefs.putInt("marginLineColor",				marginLineColor.getRGB());
		prefs.putBoolean("highlightSecondaryLanguages",highlightSecondaryLanguages);
		for (int i=0; i<3; i++) {
			prefs.putInt("secondaryLanguageColor_" + i, secondaryLanguageColors[i].getRGB());
		}
		prefs.putBoolean("hyperlinksEnabled",			hyperlinksEnabled);
		prefs.putInt("hyperlinkColor",				hyperlinkColor.getRGB());
		prefs.putInt("hyperlinkModifierKey",			hyperlinkModifierKey);
		prefs.putBoolean("marginLineEnabled",			marginLineEnabled);
		prefs.putInt("marginLinePosition",				marginLinePosition);
		prefs.putInt("matchedBracketBGColor",		matchedBracketBGColor.getRGB());
		prefs.putInt("matchedBracketBorderColor",		matchedBracketBorderColor.getRGB());
		prefs.putInt("modifiedDocumentNamesColor",	modifiedDocumentNamesColor.getRGB());
		prefs.put("printFont",						printFont==null ? "null" : printFont.getName() + ","
													+ printFont.getSize() + "," + printFont.isBold() +
													"," + printFont.isItalic());
		prefs.putInt("selectionColor",				selectionColor.getRGB());
		prefs.putInt("selectedTextColor",			selectedTextColor.getRGB());
		prefs.putBoolean("useSelectedTextColor",	useSelectedTextColor);
		prefs.put("syntaxFilters",					syntaxFiltersString);
		prefs.putInt("tabSize",						tabSize);
		prefs.putInt("tabPlacement",					tabPlacement);
		prefs.putInt("textMode",						textMode);
		prefs.putBoolean("wordWrap",					wordWrap);
		if (backgroundObject instanceof Color) {
			Color c = (Color)backgroundObject;
			prefs.put("backgroundObject",				"color," + c.getRGB());
		}
		else if (backgroundObject instanceof Image) {
			prefs.put("backgroundObject",				"image," + ((RText)rtext).getMainView().getBackgroundImageFileName());
		}
		prefs.putBoolean("visibleWhitespace",			visibleWhitespace);
		prefs.putBoolean("showEOL",						showEOLMarkers);
		prefs.putBoolean("showTabLines",				showTabLines);
		prefs.putInt("tabLinesColor",					tabLinesColor.getRGB());
		prefs.putBoolean("rememberWhitespaceLines",		rememberWhitespaceLines);
		prefs.putBoolean("autoInsertClosingCurlys",		autoInsertClosingCurlys);
		prefs.putBoolean("editorAntiAlias",					aaEnabled);
		prefs.putBoolean("fractionalMetrics",			fractionalMetricsEnabled);
		prefs.putInt("markAllHighlightColor",			markAllHighlightColor.getRGB());
		prefs.putBoolean("markOccurrences",			markOccurrences);
		prefs.putInt("markOccurrencesColor",			markOccurrencesColor.getRGB());
		prefs.putBoolean("roundedSelectionEdges",		roundedSelectionEdges);
		prefs.putInt("insertCaretStyle",				carets[RTextArea.INSERT_MODE]);
		prefs.putInt("overwriteCaretStyle",			carets[RTextArea.OVERWRITE_MODE]);
		prefs.putInt("caretBlinkRate",				caretBlinkRate);
		prefs.put("defaultLineTerminator",				defaultLineTerminator==null ? "" : defaultLineTerminator);
		prefs.put("defaultEncoding",					defaultEncoding==null ? "" : defaultEncoding);
		prefs.putBoolean("guessFileContentType",		guessFileContentType);
		prefs.putBoolean("fileSizeCheck",				doFileSizeCheck);
		prefs.putFloat("maxFileSize",					maxFileSize);
		prefs.putBoolean("ignoreBackupExtensions",		ignoreBackupExtensions);
		prefs.put("textAreaFont",					textAreaFont==null ? "null" : textAreaFont.getName() + ","
													+ textAreaFont.getSize() + "," + textAreaFont.isBold() +
													"," + textAreaFont.isItalic());
		prefs.putBoolean("textAreaUnderline",			textAreaUnderline);
		prefs.putInt("textAreaForeground",				textAreaForeground.getRGB());
		prefs.put("textAreaOrientation",				textAreaOrientation.isLeftToRight() ? "ltr" : "rtl");
		prefs.putBoolean("spellCheckingEnabled",		spellCheckingEnabled);
		prefs.putInt("spellCheckingColor",				spellCheckingColor.getRGB());
		prefs.put("spellingDictionary", 				spellingDictionary);
		prefs.put("userDictionary",						userDictionary==null ? "" : userDictionary.getAbsolutePath());
		prefs.putInt("maxSpellingErrors",				maxSpellingErrors);
		prefs.putBoolean("viewSpellingList",			viewSpellingList);
		prefs.putBoolean("searchWindowOpacityEnabled",	searchWindowOpacityEnabled);
		prefs.putFloat("searchWindowOpacity",			searchWindowOpacity);
		prefs.putInt("searchWindowOpacityRule",			searchWindowOpacityRule);
		prefs.putBoolean("dropShadowsInEditor",			dropShadowsInEditor);
		prefs.put("codeFoldingEnabledFor",				codeFoldingEnabledFor);
		prefs.putBoolean("useSearchDialogs",			useSearchDialogs);

		// Save all properties related to the RTextMenuBar class.
		prefs = Preferences.userNodeForPackage(RTextMenuBar.class);
		prefs.put("fileHistoryString",				(fileHistoryString==null ? "-" : fileHistoryString));
		prefs.putInt("maxFileHistorySize",				maxFileHistorySize);

		// Save all accelerators.
		for (int i=0; i<RText.actionNames.length; i++) {
			String actionName = RText.actionNames[i];
			prefs.put(actionName,
				getKeyStrokeString(getAccelerator(actionName)));
		}

		//prefs.flush();
		prefs = null;

	}


	/**
	 * Sets this preferences instance to contain all default values.
	 */
	@Override
	protected void setDefaults() {
		location = new Point(0,0);
		size = new Dimension(650,500);
		lookAndFeel = UIManager.getSystemLookAndFeelClassName(); //1.5.2004/pwy: use the system default
		iconGroupName = IconGroupLoader.DEFAULT_ICON_GROUP_NAME;
		toolbarVisible = true;
		statusBarVisible = true;
		lineNumbersVisible = false;
		tabSize = 5;
		emulateTabsWithSpaces = false;
		textMode = RTextArea.INSERT_MODE;
		tabPlacement = JTabbedPane.TOP;
		printFont = null;	// i.e., use RText's font.
		backgroundObject = Color.WHITE;
		imageAlpha = 0.3f;	// Arbitrary initial value.
		wordWrap = false;
		caretColor = RTextArea.getDefaultCaretColor();
		selectionColor = RSyntaxTextArea.getDefaultSelectionColor();
		selectedTextColor = Color.white;
		useSelectedTextColor = false;
		colorScheme = new SyntaxScheme(true);
		SyntaxFilters syntaxFilters = new SyntaxFilters();
		syntaxFilters.restoreDefaultFileFilters();
		syntaxFiltersString = syntaxFilters.toString();
		maxFileHistorySize = 20;
		fileHistoryString = null;
		currentLineHighlightEnabled = true;
		currentLineHighlightColor = RTextArea.getDefaultCurrentLineHighlightColor();
		mainView = RText.TABBED_VIEW;
		highlightModifiedDocNames = true;
		modifiedDocumentNamesColor = Color.RED;
		language = "en";	// Default to English.
		bracketMatchingEnabled = true;
		matchBothBrackets = false;
		matchedBracketBGColor = RSyntaxTextArea.getDefaultBracketMatchBGColor();
		matchedBracketBorderColor = RSyntaxTextArea.getDefaultBracketMatchBorderColor();
		marginLineEnabled = true;
		marginLinePosition = RTextArea.getDefaultMarginLinePosition();
		marginLineColor = RTextArea.getDefaultMarginLineColor();
		highlightSecondaryLanguages = false;
		secondaryLanguageColors = new Color[3];
		secondaryLanguageColors[0] = new Color(0xfff0cc);
		secondaryLanguageColors[1] = new Color(0xdafeda);
		secondaryLanguageColors[2] = new Color(0xffe0f0);
		hyperlinksEnabled = true;
		hyperlinkColor = Color.BLUE;
		hyperlinkModifierKey = InputEvent.CTRL_DOWN_MASK;
		visibleWhitespace = false;
		showEOLMarkers = false;
		showTabLines = false;
		tabLinesColor = Color.gray;
		rememberWhitespaceLines = true;
		autoInsertClosingCurlys = false;
		aaEnabled = File.separatorChar=='\\' ||
				System.getProperty("os.name").indexOf("mac os x")>-1;
		fractionalMetricsEnabled = false;
		markAllHighlightColor = RTextArea.getDefaultMarkAllHighlightColor();
		markOccurrences = true;
		markOccurrencesColor = new Color(224, 224, 224);
		statusBarStyle = StatusBar.WINDOWS_XP_STYLE;
		roundedSelectionEdges = false;
		workingDirectory = System.getProperty("user.dir");
		carets = new int[2];
		carets[RTextArea.INSERT_MODE] = ConfigurableCaret.THICK_VERTICAL_LINE_STYLE;
		carets[RTextArea.OVERWRITE_MODE] = ConfigurableCaret.BLOCK_STYLE;
		caretBlinkRate	= 500;
		searchToolBarVisible = false;
		dividerLocations = new int[4];
		for (int i=0; i<4; i++) {
			dividerLocations[i] = -1; // negative => left components preferred size.
		}
		defaultLineTerminator = null; // Use system default.
		defaultEncoding = null; // Use system default encoding.
		guessFileContentType = true;
		doFileSizeCheck	= true;
		maxFileSize		= 10f;	// MB.
		ignoreBackupExtensions = true;
		textAreaFont		= RTextArea.getDefaultFont();
		textAreaUnderline	= false;
		textAreaForeground	= RTextArea.getDefaultForeground();
		textAreaOrientation = ComponentOrientation.LEFT_TO_RIGHT;
		showHostName		= false;
		bomInUtf8			= false;
		bookmarksEnabled	= true;
		lineNumberFont		= new Font("Monospaced", Font.PLAIN, 12);
		lineNumberColor	= Color.GRAY;
		gutterBorderColor	= new Color(221, 221, 221);
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

		accelerators = new HashMap<String, KeyStroke>();
		for (int i=0; i<actionNames.length; i++) {
			String actionName = actionNames[i];
			accelerators.put(actionName, defaultActionAccelerators[i]);
		}

	}


	/**
	 * Returns a <code>String</code> representation of this object.
	 *
	 * @return A string representation of this object.
	 */
	@Override
	public String toString() {
		return "[Class: RTextPreferences]";
	}


}