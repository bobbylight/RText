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
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import javax.swing.JTabbedPane;

import org.fife.rtext.SearchManager.SearchingMode;
import org.fife.rtext.optionsdialog.UIOptionPanel;
import org.fife.ui.OS;
import org.fife.ui.StatusBar;
import org.fife.ui.app.GUIApplicationPrefs;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.CaretStyle;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.util.DarculaUtil;


/**
 * A class representing several properties of an RText session.  This class
 * is used for saving user preferences between RText sessions. It consists
 * only of public data members for ease of use.
 *
 * @author Robert Futrell
 * @version 0.3
 */
public class RTextPrefs extends GUIApplicationPrefs<RText>
		implements RTextActionInfo {

	/**
	 * The default Look and Feel.
	 */
	private static final String DEFAULT_LAF = DarculaUtil.CLASS_NAME;

	/**
	 * The default maximum number of spelling errors to display for a single
	 * file.
	 */
	public static final int DEFAULT_MAX_SPELLING_ERRORS = 30;

	/**
	 * The default color used to underline spelling errors.
	 */
	public static final Color DEFAULT_SPELLING_ERROR_COLOR = new Color(255,128,64);

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


	/**
	 * Constructor; initializes all values to "defaults."
	 */
	public RTextPrefs() {
		setDefaults();
	}


	@Override
	public RTextPrefs populate(RText rtext) {

		AbstractMainView mainView = rtext.getMainView();
		SpellingSupport spelling = mainView.getSpellingSupport();
		RTextMenuBar menuBar = (RTextMenuBar)rtext.getJMenuBar();

		//String lnfString = UIManager.getLookAndFeel().getClass().getName();
		String lnfString = RTextUtilities.getLookAndFeelToSave();

		populateCommonPreferences(rtext, lnfString);
		iconGroupName				= rtext.getIconGroup().getName();
		lineNumbersVisible			= mainView.getLineNumbersEnabled();
		tabSize					= mainView.getTabSize();
		emulateTabsWithSpaces		= mainView.areTabsEmulated();
		textMode					= mainView.getTextMode();
		tabPlacement				= mainView.getDocumentSelectionPlacement();
		printFont				= mainView.getPrintFont();
		backgroundObject			= mainView.getBackgroundObject();
		imageAlpha				= mainView.getBackgroundImageAlpha();
		wordWrap					= mainView.getLineWrap();
		caretColor				= mainView.getCaretColor();
		selectionColor			= mainView.getSelectionColor();
		selectedTextColor			= mainView.getSelectedTextColor();
		useSelectedTextColor		= mainView.getUseSelectedTextColor();
		colorScheme				= rtext.getSyntaxScheme();
		syntaxFiltersString		= mainView.getSyntaxFilters().toString();
		maxFileHistorySize			= menuBar.getMaximumFileHistorySize();
		fileHistoryString			= menuBar.getFileHistoryString();
		currentLineHighlightEnabled	= mainView.isCurrentLineHighlightEnabled();
		currentLineHighlightColor	= mainView.getCurrentLineHighlightColor();
		this.mainView				= rtext.getMainViewStyle();
		highlightModifiedDocNames	= mainView.highlightModifiedDocumentDisplayNames();
		modifiedDocumentNamesColor	= mainView.getModifiedDocumentDisplayNamesColor();
		bracketMatchingEnabled		= mainView.isBracketMatchingEnabled();
		matchBothBrackets			= mainView.getMatchBothBrackets();
		matchedBracketBGColor		= mainView.getMatchedBracketBGColor();
		matchedBracketBorderColor	= mainView.getMatchedBracketBorderColor();
		marginLineEnabled			= mainView.isMarginLineEnabled();
		marginLinePosition			= mainView.getMarginLinePosition();
		marginLineColor			= mainView.getMarginLineColor();
		highlightSecondaryLanguages = mainView.getHighlightSecondaryLanguages();
		secondaryLanguageColors = new Color[3];
		for (int i=0; i<secondaryLanguageColors.length; i++) {
			secondaryLanguageColors[i] = mainView.getSecondaryLanguageColor(i);
		}
		hyperlinksEnabled			= mainView.getHyperlinksEnabled();
		hyperlinkColor			= mainView.getHyperlinkColor();
		hyperlinkModifierKey		= mainView.getHyperlinkModifierKey();
		visibleWhitespace			= mainView.isWhitespaceVisible();
		showEOLMarkers			= mainView.getShowEOLMarkers();
		showTabLines				= mainView.getShowTabLines();
		tabLinesColor				= mainView.getTabLinesColor();
		rememberWhitespaceLines	= mainView.getRememberWhitespaceLines();
		autoInsertClosingCurlys	= mainView.getAutoInsertClosingCurlys();
		aaEnabled					= mainView.isAntiAliasEnabled();
		fractionalMetricsEnabled	= mainView.isFractionalFontMetricsEnabled();
		markAllHighlightColor		= mainView.getMarkAllHighlightColor();
		markOccurrences			= mainView.getMarkOccurrences();
		markOccurrencesColor		= mainView.getMarkOccurrencesColor();
		statusBarStyle			= rtext.getStatusBar().getStyle();
		roundedSelectionEdges		= mainView.getRoundedSelectionEdges();
		workingDirectory			= rtext.getWorkingDirectory();
		carets[RTextArea.INSERT_MODE]= mainView.getCaretStyle(RTextArea.INSERT_MODE).ordinal();
		carets[RTextArea.OVERWRITE_MODE]= mainView.getCaretStyle(RTextArea.OVERWRITE_MODE).ordinal();
		caretBlinkRate			= mainView.getCaretBlinkRate();
		searchToolBarVisible		= rtext.isSearchToolBarVisible();
		dividerLocations[RText.TOP]	= rtext.getSplitPaneDividerLocation(RText.TOP);
		dividerLocations[RText.LEFT] = rtext.getSplitPaneDividerLocation(RText.LEFT);
		dividerLocations[RText.BOTTOM] = rtext.getSplitPaneDividerLocation(RText.BOTTOM);
		dividerLocations[RText.RIGHT]= rtext.getSplitPaneDividerLocation(RText.RIGHT);
		dividerVisible[RText.TOP]	= rtext.isDockableWindowGroupExpanded(RText.TOP);
		dividerVisible[RText.LEFT] = rtext.isDockableWindowGroupExpanded(RText.LEFT);
		dividerVisible[RText.BOTTOM] = rtext.isDockableWindowGroupExpanded(RText.BOTTOM);
		dividerVisible[RText.RIGHT]= rtext.isDockableWindowGroupExpanded(RText.RIGHT);
		defaultLineTerminator		= mainView.getLineTerminator();
		defaultEncoding			= mainView.getDefaultEncoding();
		guessFileContentType		= mainView.getGuessFileContentType();
		doFileSizeCheck			= mainView.getDoFileSizeCheck();
		maxFileSize				= mainView.getMaxFileSize();
		maxFileSizeForCodeFolding = mainView.getMaxFileSizeForCodeFolding();
		ignoreBackupExtensions	= mainView.getIgnoreBackupExtensions();
		textAreaFont				= mainView.getTextAreaFont();
		textAreaUnderline			= mainView.getTextAreaUnderline();
		textAreaForeground			= mainView.getTextAreaForeground();
		textAreaOrientation		= mainView.getTextAreaOrientation();
		foldBackground			= mainView.getFoldBackground();
		armedFoldBackground		= mainView.getArmedFoldBackground();
		showHostName				= rtext.getShowHostName();
		bomInUtf8				= mainView.getWriteBOMInUtf8Files();
		bookmarksEnabled			= mainView.getBookmarksEnabled();
		lineNumberFont			= mainView.getLineNumberFont();
		lineNumberColor			= mainView.getLineNumberColor();
		gutterBorderColor			= mainView.getGutterBorderColor();
		spellCheckingEnabled		= spelling.isSpellCheckingEnabled();
		spellCheckingColor		= spelling.getSpellCheckingColor();
		spellingDictionary		= spelling.getSpellingDictionary();
		userDictionary			= spelling.getUserDictionary();
		maxSpellingErrors			= spelling.getMaxSpellingErrors();
		viewSpellingList			= rtext.isSpellingWindowVisible();
		searchWindowOpacityEnabled= rtext.isSearchWindowOpacityEnabled();
		searchWindowOpacity		= rtext.getSearchWindowOpacity();
		searchWindowOpacityRule	= rtext.getSearchWindowOpacityRule();
		dropShadowsInEditor		= RTextUtilities.getDropShadowsEnabledInEditor();
		codeFoldingEnabledFor		= mainView.getCodeFoldingEnabledForString();

		useSearchDialogs			= mainView.getSearchManager().
								getSearchingMode()==SearchingMode.DIALOGS;

		return this;

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
		return prefs.get("lookAndFeel", DEFAULT_LAF);
	}


	@Override
	public RTextPrefs load() {

		try {

			// Get all properties associated with the RText class.
			Preferences prefs = Preferences.userNodeForPackage(RText.class);
			loadCommonPreferences(prefs);
			iconGroupName				= prefs.get("iconGroupName", iconGroupName);
			lineNumbersVisible			= prefs.getBoolean("lineNumbersVisible", lineNumbersVisible);
			tabSize					= prefs.getInt("tabSize", tabSize);
			mainView					= prefs.getInt("mainView", mainView);
			if (colorScheme == null) { // Use the default, fall back to whatever was stored
				String temp = prefs.get("colorScheme", null);
				colorScheme = SyntaxScheme.loadFromString(temp);
			}
			statusBarStyle			= prefs.getInt("statusBarStyle", statusBarStyle);
			workingDirectory			= prefs.get("workingDirectory", workingDirectory);
			searchToolBarVisible		= prefs.getBoolean("searchToolBarVisible", searchToolBarVisible);
			dividerLocations[RText.TOP]	= prefs.getInt("pluginDividerLocation.top", dividerLocations[RText.TOP]);
			dividerLocations[RText.LEFT] = prefs.getInt("pluginDividerLocation.left", dividerLocations[RText.LEFT]);
			dividerLocations[RText.BOTTOM] = prefs.getInt("pluginDividerLocation.bottom", dividerLocations[RText.BOTTOM]);
			dividerLocations[RText.RIGHT]= prefs.getInt("pluginDividerLocation.right", dividerLocations[RText.RIGHT]);
			dividerVisible[RText.TOP]	= prefs.getBoolean("pluginDividerVisible.top", dividerVisible[RText.TOP]);
			dividerVisible[RText.LEFT] = prefs.getBoolean("pluginDividerVisible.left", dividerVisible[RText.LEFT]);
			dividerVisible[RText.BOTTOM] = prefs.getBoolean("pluginDividerVisible.bottom", dividerVisible[RText.BOTTOM]);
			dividerVisible[RText.RIGHT]= prefs.getBoolean("pluginDividerVisible.right", dividerVisible[RText.RIGHT]);
			showHostName				= prefs.getBoolean("showHostName", showHostName);
			bomInUtf8				= prefs.getBoolean("bomInUtf8", bomInUtf8);
			bookmarksEnabled			= prefs.getBoolean("bookmarksEnabled", bookmarksEnabled);
			String temp							= prefs.get("lineNumberFont", null);
			if (temp!=null) {
				lineNumberFont			= getFontImpl(temp);
			}
			lineNumberColor			= new Color(
				prefs.getInt("lineNumberColor", lineNumberColor.getRGB()));
			gutterBorderColor			= new Color(
				prefs.getInt("gutterBorderColor", gutterBorderColor.getRGB()));

			// Get all properties associated with the MainView class.
			prefs = Preferences.userNodeForPackage(AbstractMainView.class);
			bracketMatchingEnabled		= prefs.getBoolean("bracketMatchingEnabled", bracketMatchingEnabled);
			matchBothBrackets				= prefs.getBoolean("matchBothBrackets", matchBothBrackets);
			currentLineHighlightEnabled	= prefs.getBoolean("currentLineHighlightEnabled", currentLineHighlightEnabled);
			emulateTabsWithSpaces		= prefs.getBoolean("emulateTabs", emulateTabsWithSpaces);
			highlightModifiedDocNames	= prefs.getBoolean("highlightModifiedDocNames", highlightModifiedDocNames);
			imageAlpha				= prefs.getFloat("imageAlpha", imageAlpha);
			marginLineEnabled			= prefs.getBoolean("marginLineEnabled", marginLineEnabled);
			marginLinePosition			= prefs.getInt("marginLinePosition", marginLinePosition);
			syntaxFiltersString		= prefs.get("syntaxFilters", syntaxFiltersString);
			textMode					= prefs.getInt("textMode", textMode);
			tabPlacement				= prefs.getInt("tabPlacement", tabPlacement);
			wordWrap					= prefs.getBoolean("wordWrap", wordWrap);
			temp							= prefs.get("printFont", null);
			if (temp!=null) {
				printFont = getFontImpl(temp);
			}
			temp						= prefs.get("backgroundObject", null);
			if (temp!=null) {
				StringTokenizer t2 = new StringTokenizer(temp, ",");
				String temp2 = t2.nextToken();
				if (temp2.equals("color"))
					backgroundObject = new Color(
						Integer.parseInt(temp.substring(temp.indexOf(',')+1)));
				else if (temp2.equals("image"))
					backgroundObject = t2.nextToken();
				else
					backgroundObject = Color.WHITE;
			}
			possiblyFixSyntaxSchemeBackground(this);
			caretColor = new Color(
				prefs.getInt("caretColor", caretColor.getRGB()));
			selectionColor = new Color(
				prefs.getInt("selectionColor", selectionColor.getRGB()), true);
			selectedTextColor = new Color(
					prefs.getInt("selectedTextColor", selectedTextColor.getRGB()), true);
			useSelectedTextColor = prefs.getBoolean("useSelectedTextColor", useSelectedTextColor);
			currentLineHighlightColor = new Color(
				prefs.getInt("currentLineHighlightColor", currentLineHighlightColor.getRGB()), true);
			modifiedDocumentNamesColor = new Color(
				prefs.getInt("modifiedDocumentNamesColor", modifiedDocumentNamesColor.getRGB()));
			matchedBracketBGColor = new Color(
				prefs.getInt("matchedBracketBGColor", matchedBracketBGColor.getRGB()));
			matchedBracketBorderColor = new Color(
				prefs.getInt("matchedBracketBorderColor", matchedBracketBorderColor.getRGB()));
			marginLineColor = new Color(
				prefs.getInt("marginLineColor", marginLineColor.getRGB()));
			highlightSecondaryLanguages = prefs.getBoolean("highlightSecondaryLanguages", highlightSecondaryLanguages);
			for (int i=0; i<secondaryLanguageColors.length; i++) {
				secondaryLanguageColors[i] = new Color(prefs.getInt(
				"secondaryLanguageColor_" + i, secondaryLanguageColors[i].getRGB()));
			}
			hyperlinksEnabled = prefs.getBoolean("hyperlinksEnabled", hyperlinksEnabled);
			hyperlinkColor = new Color(
				prefs.getInt("hyperlinkColor", hyperlinkColor.getRGB()));
			hyperlinkModifierKey		= prefs.getInt("hyperlinkModifierKey", hyperlinkModifierKey);
			visibleWhitespace			= prefs.getBoolean("visibleWhitespace", visibleWhitespace);
			showEOLMarkers			= prefs.getBoolean("showEOL", showEOLMarkers);
			showTabLines				= prefs.getBoolean("showTabLines", showTabLines);
			tabLinesColor				= new Color(
				prefs.getInt("tabLinesColor", tabLinesColor.getRGB()));
			rememberWhitespaceLines	= prefs.getBoolean("rememberWhitespaceLines", rememberWhitespaceLines);
			autoInsertClosingCurlys	= prefs.getBoolean("autoInsertClosingCurlys", autoInsertClosingCurlys);
			aaEnabled					= prefs.getBoolean("editorAntiAlias", aaEnabled);
			fractionalMetricsEnabled	= prefs.getBoolean("fractionalMetrics", fractionalMetricsEnabled);
			markAllHighlightColor = new Color(
				prefs.getInt("markAllHighlightColor", markAllHighlightColor.getRGB()));
			markOccurrences		= prefs.getBoolean("markOccurrences", markOccurrences);
			markOccurrencesColor	= new Color(
				prefs.getInt("markOccurrencesColor", markOccurrencesColor.getRGB()));
			roundedSelectionEdges	= prefs.getBoolean("roundedSelectionEdges", roundedSelectionEdges);
			carets[RTextArea.INSERT_MODE]= prefs.getInt("insertCaretStyle", carets[RTextArea.INSERT_MODE]);
			carets[RTextArea.OVERWRITE_MODE]= prefs.getInt("overwriteCaretStyle", carets[RTextArea.OVERWRITE_MODE]);
			caretBlinkRate		= prefs.getInt("caretBlinkRate", caretBlinkRate);
			defaultLineTerminator	= prefs.get("defaultLineTerminator", defaultLineTerminator);
			if ("".equals(defaultLineTerminator)) {
				defaultLineTerminator = null;
			}
			defaultEncoding		= prefs.get("defaultEncoding", defaultEncoding);
			if ("".equals(defaultEncoding)) {
				defaultEncoding = null;
			}
			guessFileContentType	= prefs.getBoolean("guessFileContentType", guessFileContentType);
			doFileSizeCheck		= prefs.getBoolean("fileSizeCheck", doFileSizeCheck);
			maxFileSize			= prefs.getFloat("maxFileSize", maxFileSize);
			maxFileSizeForCodeFolding = prefs.getInt("maxFileSizeForCodeFolding", maxFileSizeForCodeFolding);
			ignoreBackupExtensions= prefs.getBoolean("ignoreBackupExtensions", ignoreBackupExtensions);
			temp						= prefs.get("textAreaFont", null);
			if (temp!=null) {
				textAreaFont = getFontImpl(temp);
			}
			textAreaUnderline		= prefs.getBoolean("textAreaUnderline", textAreaUnderline);
			textAreaForeground		= new Color(
				prefs.getInt("textAreaForeground", textAreaForeground.getRGB()));
			temp = prefs.get("textAreaOrientation", "ltr");
			textAreaOrientation	= "rtl".equals(temp) ?
						ComponentOrientation.RIGHT_TO_LEFT :
						ComponentOrientation.LEFT_TO_RIGHT;
			foldBackground			= new Color(
				prefs.getInt("foldBackground", foldBackground.getRGB()));
			armedFoldBackground		= new Color(
				prefs.getInt("armedFoldBackground", armedFoldBackground.getRGB()));
			spellCheckingEnabled	= prefs.getBoolean("spellCheckingEnabled", spellCheckingEnabled);
			spellCheckingColor	= new Color(
				prefs.getInt("spellCheckingColor", spellCheckingColor.getRGB()));
			spellingDictionary	= prefs.get("spellingDictionary", spellingDictionary);
			String tempVal = prefs.get("userDictionary", null);
			if (tempVal!=null) { // No previous value => use default already set
				if (tempVal.isEmpty()) { // Empty string => no user dictionary
					userDictionary = null;
				}
				else {
					userDictionary = new File(tempVal);
				}
			}
			maxSpellingErrors		= prefs.getInt("maxSpellingErrors", maxSpellingErrors);
			viewSpellingList	= prefs.getBoolean("viewSpellingList", viewSpellingList);
			searchWindowOpacityEnabled = prefs.getBoolean("searchWindowOpacityEnabled", searchWindowOpacityEnabled);
			searchWindowOpacity = prefs.getFloat("searchWindowOpacity", searchWindowOpacity);
			searchWindowOpacityRule = prefs.getInt("searchWindowOpacityRule", searchWindowOpacityRule);
			dropShadowsInEditor = prefs.getBoolean("dropShadowsInEditor", getDefaultDropShadowsInEditorValue());
			codeFoldingEnabledFor = prefs.get("codeFoldingEnabledFor", codeFoldingEnabledFor);
			useSearchDialogs = prefs.getBoolean("useSearchDialogs", useSearchDialogs);

			// Get all properties associated with the RTextMenuBar class.
			prefs = Preferences.userNodeForPackage(RTextMenuBar.class);
			fileHistoryString		= prefs.get("fileHistoryString", fileHistoryString);
			maxFileHistorySize		= prefs.getInt("maxFileHistorySize", maxFileHistorySize);

		// If anything at all goes wrong, just use default property values.
		} catch (RuntimeException re) {
			throw re; // Let RuntimeExceptions through (FindBugs warning)
		} catch (Exception e) {
			e.printStackTrace();
			setDefaults();
		}

		return this;

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


	/**
	 * A null-safe means of storing the integer value of a color.  If the color
	 * is {@code null}, any value for that key is removed from the preferences.
	 *
	 * @param prefs The preferences to check.
	 * @param key The key for the preference.
	 * @param color The color to store, which may be {@code null}.
	 */
	private void putOrRemoveInt(Preferences prefs, String key, Color color) {

		if (color != null) {
			prefs.putInt(key, color.getRGB());
		}
		else {
			prefs.remove(key); // There may be a prior value
		}
	}


	@Override
	public void save() {

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
		prefs.putBoolean("pluginDividerVisible.top",		dividerVisible[RText.TOP]);
		prefs.putBoolean("pluginDividerVisible.left",		dividerVisible[RText.LEFT]);
		prefs.putBoolean("pluginDividerVisible.bottom",		dividerVisible[RText.BOTTOM]);
		prefs.putBoolean("pluginDividerVisible.right",		dividerVisible[RText.RIGHT]);
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
		putOrRemoveInt(prefs, "matchedBracketBGColor", matchedBracketBGColor);
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
			// TODO
			//prefs.put("backgroundObject",				"image," + rtext.getMainView().getBackgroundImageFileName());
			prefs.put("backgroundObject", "color," + Color.WHITE);
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
		prefs.putInt("maxFileSizeForCodeFolding",		maxFileSizeForCodeFolding);
		prefs.putBoolean("ignoreBackupExtensions",		ignoreBackupExtensions);
		prefs.put("textAreaFont",					textAreaFont==null ? "null" : textAreaFont.getName() + ","
													+ textAreaFont.getSize() + "," + textAreaFont.isBold() +
													"," + textAreaFont.isItalic());
		prefs.putBoolean("textAreaUnderline",			textAreaUnderline);
		prefs.putInt("textAreaForeground",				textAreaForeground.getRGB());
		prefs.put("textAreaOrientation",				textAreaOrientation.isLeftToRight() ? "ltr" : "rtl");
		prefs.putInt("foldBackground",					foldBackground.getRGB());
		prefs.putInt("armedFoldBackground",				armedFoldBackground.getRGB());
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

		//prefs.flush();

	}


	/**
	 * Sets this preferences instance to contain all default values.
	 */
	@Override
	protected void setDefaults() {

		Theme theme = null;
		try {
			theme = Theme.load(getClass().getResourceAsStream(
				"/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		}

		location = new Point(0,0);
		size = new Dimension(650,500);
		lookAndFeel = DEFAULT_LAF;
		iconGroupName = IconGroupLoader.DEFAULT_ICON_GROUP_NAME;
		toolbarVisible = true;
		statusBarVisible = true;
		lineNumbersVisible = true;
		tabSize = 5;
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
		useSelectedTextColor = theme.useSelctionFG;
		colorScheme = theme.scheme;
		SyntaxFilters syntaxFilters = new SyntaxFilters();
		syntaxFilters.restoreDefaultFileFilters();
		syntaxFiltersString = syntaxFilters.toString();
		maxFileHistorySize = 20;
		fileHistoryString = null;
		currentLineHighlightEnabled = true;
		currentLineHighlightColor = theme.currentLineHighlight;
		mainView = RText.TABBED_VIEW;
		highlightModifiedDocNames = true;
		modifiedDocumentNamesColor = UIOptionPanel.DARK_MODIFIED_DOCUMENT_NAME_COLOR;
		language = "en";	// Default to English.
		bracketMatchingEnabled = true;
		matchBothBrackets = false;
		matchedBracketBGColor = theme.matchedBracketBG;
		matchedBracketBorderColor = theme.matchedBracketFG;
		marginLineEnabled = true;
		marginLinePosition = RTextArea.getDefaultMarginLinePosition();
		marginLineColor = RTextArea.getDefaultMarginLineColor();
		highlightSecondaryLanguages = false;
		secondaryLanguageColors = new Color[3];
		secondaryLanguageColors[0] = theme.secondaryLanguages[0];
		secondaryLanguageColors[1] = theme.secondaryLanguages[1];
		secondaryLanguageColors[2] = theme.secondaryLanguages[2];
		hyperlinksEnabled = true;
		hyperlinkColor = theme.hyperlinkFG;
		hyperlinkModifierKey = InputEvent.CTRL_DOWN_MASK;
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
		statusBarStyle = StatusBar.WINDOWS_XP_STYLE;
		roundedSelectionEdges = false;
		workingDirectory = System.getProperty("user.dir");
		carets = new int[2];
		carets[RTextArea.INSERT_MODE] = CaretStyle.THICK_VERTICAL_LINE_STYLE.ordinal();
		carets[RTextArea.OVERWRITE_MODE] = CaretStyle.BLOCK_STYLE.ordinal();
		caretBlinkRate	= 500;
		searchToolBarVisible = false;
		dividerLocations = new int[4];
		for (int i=0; i<4; i++) {
			dividerLocations[i] = -1; // negative => left components preferred size.
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
		bookmarksEnabled	= true;
		lineNumberFont		= new Font("Monospaced", Font.PLAIN, 12);
		lineNumberColor	= theme.lineNumberColor;
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
