/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import org.fife.ui.app.AppContext;
import org.fife.ui.app.AppTheme;
import org.fife.ui.dockablewindows.DockableWindowConstants;
import org.fife.ui.rtextarea.RTextArea;

import java.awt.*;
import java.io.File;
import java.util.List;


/**
 * The application context for {@code RText}.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RTextAppContext extends AppContext<RText, RTextPrefs> {


	@Override
	public List<AppTheme> getAvailableAppThemes() {
		return RTextAppThemes.get();
	}


	@Override
	protected String getPreferencesClassName() {
		return "org.fife.rtext.RTextPrefs";
	}


	@Override
	public File getPreferencesDir() {
		return RTextUtilities.getPreferencesDirectory();
	}


	@Override
	public String getPreferencesFileName() {
		return "rtext4.properties";
	}


	@Override
	protected RText createApplicationImpl(String[] filesToOpen, RTextPrefs preferences) {

		RText rtext = new RText(this, filesToOpen, preferences);

		// For some reason, when using MDI_VIEW, the first window
		// isn't selected (although it is activated)...
		// INVESTIGATE ME!!
		if (rtext.getMainViewStyle()==RText.MDI_VIEW) {
			rtext.getMainView().setSelectedIndex(0);
		}

		// We currently have one RText instance running.
		StoreKeeper.addRTextInstance(rtext);

		return rtext;
	}


	@Override
	protected void populatePrefsFromApplication(RText rtext, RTextPrefs prefs) {

		// The "common" preferences
		super.populatePrefsFromApplication(rtext, prefs);
		prefs.appTheme = RTextUtilities.getAppThemeToSave(rtext);

		// Stuff specific to this application.
		AbstractMainView mainView = rtext.getMainView();
		SpellingSupport spelling = mainView.getSpellingSupport();
		RTextMenuBar menuBar = (RTextMenuBar)rtext.getJMenuBar();

		prefs.lineNumbersVisible			= mainView.getLineNumbersEnabled();
		prefs.tabSize					= mainView.getTabSize();
		prefs.emulateTabsWithSpaces		= mainView.areTabsEmulated();
		prefs.textMode					= mainView.getTextMode();
		prefs.tabPlacement				= mainView.getDocumentSelectionPlacement();
		prefs.printFont				= mainView.getPrintFont();
		prefs.backgroundObject			= mainView.getBackgroundObject();
		prefs.imageAlpha				= mainView.getBackgroundImageAlpha();
		prefs.wordWrap					= mainView.getLineWrap();
		prefs.caretColor				= mainView.getCaretColor();
		prefs.selectionColor			= mainView.getSelectionColor();
		prefs.selectedTextColor			= mainView.getSelectedTextColor();
		prefs.useSelectedTextColor		= mainView.getUseSelectedTextColor();
		prefs.colorScheme				= rtext.getSyntaxScheme();
		prefs.syntaxFiltersString		= mainView.getSyntaxFilters().toString();
		prefs.maxFileHistorySize			= menuBar.getMaximumFileHistorySize();
		prefs.fileHistoryString			= menuBar.getFileHistoryString();
		prefs.currentLineHighlightEnabled	= mainView.isCurrentLineHighlightEnabled();
		prefs.currentLineHighlightColor	= mainView.getCurrentLineHighlightColor();
		prefs.mainView					= rtext.getMainViewStyle();
		prefs.bracketMatchingEnabled		= mainView.isBracketMatchingEnabled();
		prefs.matchBothBrackets			= mainView.getMatchBothBrackets();
		prefs.matchedBracketBGColor		= mainView.getMatchedBracketBGColor();
		prefs.matchedBracketBorderColor	= mainView.getMatchedBracketBorderColor();
		prefs.marginLineEnabled			= mainView.isMarginLineEnabled();
		prefs.marginLinePosition			= mainView.getMarginLinePosition();
		prefs.marginLineColor			= mainView.getMarginLineColor();
		prefs.highlightSecondaryLanguages = mainView.getHighlightSecondaryLanguages();
		prefs.secondaryLanguageColors = new Color[3];
		for (int i=0; i<prefs.secondaryLanguageColors.length; i++) {
			prefs.secondaryLanguageColors[i] = mainView.getSecondaryLanguageColor(i);
		}
		prefs.visibleWhitespace			= mainView.isWhitespaceVisible();
		prefs.showEOLMarkers			= mainView.getShowEOLMarkers();
		prefs.showTabLines				= mainView.getShowTabLines();
		prefs.tabLinesColor				= mainView.getTabLinesColor();
		prefs.rememberWhitespaceLines	= mainView.getRememberWhitespaceLines();
		prefs.autoInsertClosingCurlys	= mainView.getAutoInsertClosingCurlys();
		prefs.aaEnabled					= mainView.isAntiAliasEnabled();
		prefs.fractionalMetricsEnabled	= mainView.isFractionalFontMetricsEnabled();
		prefs.markAllHighlightColor		= mainView.getMarkAllHighlightColor();
		prefs.markOccurrences			= mainView.getMarkOccurrences();
		prefs.markOccurrencesColor		= mainView.getMarkOccurrencesColor();
		prefs.roundedSelectionEdges		= mainView.getRoundedSelectionEdges();
		prefs.workingDirectory			= rtext.getWorkingDirectory();
		prefs.carets[RTextArea.INSERT_MODE]= mainView.getCaretStyle(RTextArea.INSERT_MODE).ordinal();
		prefs.carets[RTextArea.OVERWRITE_MODE]= mainView.getCaretStyle(RTextArea.OVERWRITE_MODE).ordinal();
		prefs.caretBlinkRate			= mainView.getCaretBlinkRate();
		prefs.dividerLocations[DockableWindowConstants.TOP]	= rtext.getSplitPaneDividerLocation(DockableWindowConstants.TOP);
		prefs.dividerLocations[DockableWindowConstants.LEFT] = rtext.getSplitPaneDividerLocation(DockableWindowConstants.LEFT);
		prefs.dividerLocations[DockableWindowConstants.BOTTOM] = rtext.getSplitPaneDividerLocation(DockableWindowConstants.BOTTOM);
		prefs.dividerLocations[DockableWindowConstants.RIGHT]= rtext.getSplitPaneDividerLocation(DockableWindowConstants.RIGHT);
		prefs.dividerVisible[DockableWindowConstants.TOP]	= rtext.isDockableWindowGroupExpanded(DockableWindowConstants.TOP);
		prefs.dividerVisible[DockableWindowConstants.LEFT] = rtext.isDockableWindowGroupExpanded(DockableWindowConstants.LEFT);
		prefs.dividerVisible[DockableWindowConstants.BOTTOM] = rtext.isDockableWindowGroupExpanded(DockableWindowConstants.BOTTOM);
		prefs.dividerVisible[DockableWindowConstants.RIGHT]= rtext.isDockableWindowGroupExpanded(DockableWindowConstants.RIGHT);
		prefs.defaultLineTerminator		= mainView.getLineTerminator();
		prefs.defaultEncoding			= mainView.getDefaultEncoding();
		prefs.guessFileContentType		= mainView.getGuessFileContentType();
		prefs.doFileSizeCheck			= mainView.getDoFileSizeCheck();
		prefs.maxFileSize				= mainView.getMaxFileSize();
		prefs.maxFileSizeForCodeFolding = mainView.getMaxFileSizeForCodeFolding();
		prefs.ignoreBackupExtensions	= mainView.getIgnoreBackupExtensions();
		prefs.textAreaFont				= mainView.getTextAreaFont();
		prefs.textAreaUnderline			= mainView.getTextAreaUnderline();
		prefs.textAreaForeground			= mainView.getTextAreaForeground();
		prefs.textAreaOrientation		= mainView.getTextAreaOrientation();
		prefs.foldBackground			= mainView.getFoldBackground();
		prefs.armedFoldBackground		= mainView.getArmedFoldBackground();
		prefs.showHostName				= rtext.getShowHostName();
		prefs.bomInUtf8				= mainView.getWriteBOMInUtf8Files();
		prefs.bookmarksEnabled			= mainView.getBookmarksEnabled();
		prefs.lineNumberFont			= mainView.getLineNumberFont();
		prefs.lineNumberColor			= mainView.getLineNumberColor();
		prefs.gutterBorderColor			= mainView.getGutterBorderColor();
		prefs.spellCheckingEnabled		= spelling.isSpellCheckingEnabled();
		prefs.spellCheckingColor		= spelling.getSpellCheckingColor();
		prefs.spellingDictionary		= spelling.getSpellingDictionary();
		prefs.userDictionary			= spelling.getUserDictionary();
		prefs.maxSpellingErrors			= spelling.getMaxSpellingErrors();
		prefs.viewSpellingList			= rtext.isSpellingWindowVisible();
		prefs.searchWindowOpacityEnabled= rtext.isSearchWindowOpacityEnabled();
		prefs.searchWindowOpacity		= rtext.getSearchWindowOpacity();
		prefs.searchWindowOpacityRule	= rtext.getSearchWindowOpacityRule();
		prefs.dropShadowsInEditor		= RTextUtilities.getDropShadowsEnabledInEditor();
		prefs.codeFoldingEnabledFor		= mainView.getCodeFoldingEnabledForString();

		prefs.useSearchDialogs			= mainView.getSearchManager().
			getSearchingMode()== SearchManager.SearchingMode.DIALOGS;

	}
}
