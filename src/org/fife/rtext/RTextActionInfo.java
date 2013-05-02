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

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;


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
	public static final String NEW_ACTION				= "newAction";
	public static final String OPEN_ACTION				= "openAction";
	public static final String OPEN_NEWWIN_ACTION		= "openNewWinAction";
	public static final String OPEN_REMOTE_ACTION		= "openRemoteAction";
	public static final String SAVE_ACTION				= "saveAction";
	public static final String SAVE_AS_ACTION			= "saveAsAction";
	public static final String SAVE_AS_REMOTE_ACTION	= "saveAsRemoteAction";
	public static final String SAVE_WEBPAGE_ACTION		= "saveWebPageAction";
	public static final String SAVE_ALL_ACTION			= "saveAllAction";
	public static final String COPY_AS_RTF_ACTION		= "copyAsRtf";
	public static final String TIME_DATE_ACTION			= "timeDateAction";
	public static final String TOOL_BAR_ACTION			= "toolBarAction";
	public static final String STATUS_BAR_ACTION		= "statusBarAction";
	public static final String LINE_NUMBER_ACTION		= "lineNumberAction";
	public static final String NEXT_DOCUMENT_ACTION		= "nextDocumentAction";
	public static final String PREVIOUS_DOCUMENT_ACTION = "prevDocumentAction";
	public static final String FILE_PROPERTIES_ACTION	= "filePropertiesAction";
	public static final String OPTIONS_ACTION			= "optionsAction";
	public static final String HOME_PAGE_ACTION			= "homePageAction";
	public static final String UPDATES_ACTION			= "checkForUpdatesAction";
	public static final String INC_FONT_SIZES_ACTION	= "incFontSizesAction";
	public static final String DEC_FONT_SIZES_ACTION	= "decFontSizesAction";

	public static final String FIND_ACTION				= "findAction";
	public static final String FIND_NEXT_ACTION			= "findNextAction";
	public static final String REPLACE_ACTION			= "replaceAction";
	public static final String REPLACE_NEXT_ACTION		= "replaceNextAction";
	public static final String REPLACE_ALL_ACTION		= "replaceAllAction";
	public static final String FIND_IN_FILES_ACTION		= "findInFilesAction";
	public static final String REPLACE_IN_FILES_ACTION	= "replaceInFilesAction";
	public static final String PRINT_ACTION				= "printAction";
	public static final String PRINT_PREVIEW_ACTION		= "printPreviewAction";
	public static final String CLOSE_ACTION				= "closeAction";
	public static final String CLOSE_ALL_ACTION			= "closeAllAction";
	public static final String GOTO_ACTION				= "gotoAction";
	public static final String LTR_ACTION				= "leftToRightAction";
	public static final String RTL_ACTION				= "rightToLeftAction";
	public static final String MOVE_FOCUS_LEFT_ACTION	= "moveFocusLeftAction";
	public static final String MOVE_FOCUS_RIGHT_ACTION	= "moveFocusRightAction";
	public static final String MOVE_FOCUS_UP_ACTION		= "moveFocusUpAction";
	public static final String MOVE_FOCUS_DOWN_ACTION	= "moveFocusDownAction";
	public static final String VIEW_SPLIT_HORIZ_ACTION	= "viewSplitHorizontallyAction";
	public static final String VIEW_SPLIT_NONE_ACTION	= "viewSplitNoneAction";
	public static final String VIEW_SPLIT_VERT_ACTION	= "viewSplitVerticallyAction";

	/**
	 * The names of all actions in an array.  Note that the order of these
	 * action names MUST be kept in-synch with the default accelerators
	 * array below.
	 */
	// TODO: Remove me; should no longer be needed with ActionRegistry
	public static final String[] actionNames = {
		NEW_ACTION,
		OPEN_ACTION,
		OPEN_NEWWIN_ACTION,
		OPEN_REMOTE_ACTION,
		SAVE_ACTION,
		SAVE_AS_ACTION,
		SAVE_AS_REMOTE_ACTION,
		SAVE_WEBPAGE_ACTION,
		SAVE_ALL_ACTION,
		RText.EXIT_ACTION_KEY,
		COPY_AS_RTF_ACTION,
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

	static final int defaultModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	static final int alt = InputEvent.ALT_MASK;
	static final int shift = InputEvent.SHIFT_MASK;
	static final int defaultAlt = defaultModifier|alt;
	static final int defaultShift = defaultModifier|shift;


	/**
	 * The default accelerator for all RText actions.  Note that this
	 * array MUST be kept in-synch with the actionNames array
	 * above.
	 */
	// TODO: Remove me; should no longer be needed with ActionRegistry
	public static final KeyStroke[] defaultActionAccelerators = {
		KeyStroke.getKeyStroke(KeyEvent.VK_N, defaultModifier),
		KeyStroke.getKeyStroke(KeyEvent.VK_O, defaultModifier),
		KeyStroke.getKeyStroke(KeyEvent.VK_I, defaultModifier),
		null,
		KeyStroke.getKeyStroke(KeyEvent.VK_S, defaultModifier),
		null,
		null,
		null,
		null,
		null,
		KeyStroke.getKeyStroke(KeyEvent.VK_C, defaultModifier|shift),
		KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
		null,
		null,
		KeyStroke.getKeyStroke(KeyEvent.VK_1, defaultModifier),
		KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, defaultModifier),
		KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, defaultModifier),
		KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, alt),
		KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
		null,
		null,
		null,
		null,
		KeyStroke.getKeyStroke(KeyEvent.VK_F6, defaultModifier),
		KeyStroke.getKeyStroke(KeyEvent.VK_F7, defaultModifier),

		KeyStroke.getKeyStroke(KeyEvent.VK_F, defaultModifier),
		KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
		KeyStroke.getKeyStroke(KeyEvent.VK_H, defaultModifier),
		KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0),
		null,
		KeyStroke.getKeyStroke(KeyEvent.VK_F, defaultShift),
		null,
		KeyStroke.getKeyStroke(KeyEvent.VK_P, defaultModifier),
		null,
		KeyStroke.getKeyStroke(KeyEvent.VK_W, defaultModifier),
		null,
		KeyStroke.getKeyStroke(KeyEvent.VK_L, defaultModifier),
		null,
		null,
		KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, defaultAlt),
		KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, defaultAlt),
		KeyStroke.getKeyStroke(KeyEvent.VK_UP, defaultAlt),
		KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, defaultAlt),
		null,
		null,
		null,

	};

}