/*
 * 12/22/2004
 *
 * RTextActionInfo.java - Information on the actions owned by RText.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;


/**
 * An interface containing all information about RText's actions.<p>
 *
 * Note that these actions do NOT include those owned by
 * <code>AbstractMainView</code>.<p>
 *
 * Since RText extends <code>AbstractGUIApplication</code>, its
 * actions are kept in a hash map.  Thus, this interface contains the keys
 * for all actions, an array of those keys for looping, and the default
 * values for accelerators for all of the actions.<p>
 *
 * @author Robert Futrell
 * @version 0.1
 */
public interface RTextActionInfo {

	// Constants specifying the available actions (for getAction).
	String NEW_ACTION				  = "newAction";
	String OPEN_ACTION				  = "openAction";
	String OPEN_NEWWIN_ACTION		  = "openNewWinAction";
	String OPEN_RECENT_ACTION		  = "openRecentAction";
	String OPEN_REMOTE_ACTION		  = "openRemoteAction";
	String SAVE_ACTION				  = "saveAction";
	String SAVE_AS_ACTION			  = "saveAsAction";
	String SAVE_AS_REMOTE_ACTION	  = "saveAsRemoteAction";
	String SAVE_WEBPAGE_ACTION		  = "saveWebPageAction";
	String SAVE_ALL_ACTION			  = "saveAllAction";
	String COPY_AS_STYLED_TEXT_ACTION = "copyAsStyledText";
	String TIME_DATE_ACTION			  = "timeDateAction";
	String TOOL_BAR_ACTION			  = "toolBarAction";
	String STATUS_BAR_ACTION		  = "statusBarAction";
	String LINE_NUMBER_ACTION		  = "lineNumberAction";
	String NEXT_DOCUMENT_ACTION		  = "nextDocumentAction";
	String PREVIOUS_DOCUMENT_ACTION   = "prevDocumentAction";
	String FILE_PROPERTIES_ACTION	  = "filePropertiesAction";
	String OPTIONS_ACTION			  = "optionsAction";
	String HOME_PAGE_ACTION			  = "homePageAction";
	String UPDATES_ACTION			  = "checkForUpdatesAction";
	String INC_FONT_SIZES_ACTION	  = "incFontSizesAction";
	String DEC_FONT_SIZES_ACTION	  = "decFontSizesAction";

	String FIND_ACTION				= "findAction";
	String FIND_NEXT_ACTION			= "findNextAction";
	String REPLACE_ACTION			= "replaceAction";
	String REPLACE_NEXT_ACTION		= "replaceNextAction";
	String REPLACE_ALL_ACTION		= "replaceAllAction";
	String FIND_IN_FILES_ACTION		= "findInFilesAction";
	String REPLACE_IN_FILES_ACTION	= "replaceInFilesAction";
	String PRINT_ACTION				= "printAction";
	String PRINT_PREVIEW_ACTION		= "printPreviewAction";
	String CLOSE_ACTION				= "closeAction";
	String CLOSE_ALL_ACTION			= "closeAllAction";
	String GOTO_ACTION				= "gotoAction";
	String LTR_ACTION				= "leftToRightAction";
	String RTL_ACTION				= "rightToLeftAction";
	String MOVE_FOCUS_LEFT_ACTION	= "moveFocusLeftAction";
	String MOVE_FOCUS_RIGHT_ACTION	= "moveFocusRightAction";
	String MOVE_FOCUS_UP_ACTION		= "moveFocusUpAction";
	String MOVE_FOCUS_DOWN_ACTION	= "moveFocusDownAction";
	String VIEW_SPLIT_HORIZ_ACTION	= "viewSplitHorizontallyAction";
	String VIEW_SPLIT_NONE_ACTION	= "viewSplitNoneAction";
	String VIEW_SPLIT_VERT_ACTION	= "viewSplitVerticallyAction";

	String[] ACTION_NAMES = {
		NEW_ACTION,
		OPEN_ACTION,
		OPEN_NEWWIN_ACTION,
		OPEN_RECENT_ACTION,
		OPEN_REMOTE_ACTION,
		SAVE_ACTION,
		SAVE_AS_ACTION,
		SAVE_AS_REMOTE_ACTION,
		SAVE_WEBPAGE_ACTION,
		SAVE_ALL_ACTION,
		RText.EXIT_ACTION_KEY,
		COPY_AS_STYLED_TEXT_ACTION,
		TIME_DATE_ACTION,
		TOOL_BAR_ACTION,
		STATUS_BAR_ACTION,
		LINE_NUMBER_ACTION,
		NEXT_DOCUMENT_ACTION,
		PREVIOUS_DOCUMENT_ACTION,
		FILE_PROPERTIES_ACTION,
		RText.HELP_ACTION_KEY,
		RText.ABOUT_ACTION_KEY,
		OPTIONS_ACTION,
		HOME_PAGE_ACTION,
		UPDATES_ACTION,
		DEC_FONT_SIZES_ACTION,
		INC_FONT_SIZES_ACTION,

		FIND_ACTION,
		FIND_NEXT_ACTION,
		REPLACE_ACTION,
		REPLACE_NEXT_ACTION,
		REPLACE_ALL_ACTION,
		FIND_IN_FILES_ACTION,
		REPLACE_IN_FILES_ACTION,
		PRINT_ACTION,
		PRINT_PREVIEW_ACTION,
		CLOSE_ACTION,
		CLOSE_ALL_ACTION,
		GOTO_ACTION,
		LTR_ACTION,
		RTL_ACTION,
		MOVE_FOCUS_LEFT_ACTION,
		MOVE_FOCUS_RIGHT_ACTION,
		MOVE_FOCUS_UP_ACTION,
		MOVE_FOCUS_DOWN_ACTION,
		VIEW_SPLIT_HORIZ_ACTION,
		VIEW_SPLIT_NONE_ACTION,
		VIEW_SPLIT_VERT_ACTION,

	};
}
