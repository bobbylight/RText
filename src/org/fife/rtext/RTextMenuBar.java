/*
 * 11/14/2003
 *
 * RTextMenuBar.java - Menu bar used by RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.Action;

import org.fife.ui.RecentFilesMenu;
import org.fife.ui.UIUtil;
import org.fife.ui.app.MenuBar;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextAreaEditorKit;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit;


/**
 * The menu bar used by rtext.  The menu bar includes a "file history" feature,
 * where it can remember any number of recent files and display them as options
 * in the File menu.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class RTextMenuBar extends MenuBar implements PropertyChangeListener,
									PopupMenuListener {

	/**
	 * A key to get the File menu via {@link #getMenuByName(String)}.
	 */
	public static final String MENU_FILE		= "File";

	/**
	 * A key to get the Edit menu via {@link #getMenuByName(String)}.
	 */
	public static final String MENU_EDIT		= "Edit";

	/**
	 * A key to get the Search menu via {@link #getMenuByName(String)}.
	 */
	public static final String MENU_SEARCH		= "Search";

	/**
	 * A key to get the View menu via {@link #getMenuByName(String)}.
	 */
	public static final String MENU_VIEW		= "View";

	/**
	 * A key to get the "Docked Windows" menu via {@link #getMenuByName(String)}.
	 */
	public static final String MENU_DOCKED_WINDOWS	= "DockedWindows";

	/**
	 * A key to get the Macros menu via {@link #getMenuByName(String)}.
	 */
	public static final String MENU_MACROS		= "Macros";

	/**
	 * A key to get the Help menu via {@link #getMenuByName(String)}.
	 */
	public static final String MENU_HELP		= "Help";

	// These items correspond to actions belonging to RTextEditorPanes, and are
	// changed in disableEditorActions() below, so we need to remember them.
	private JMenuItem newItem;
	private JMenuItem openItem;
	private JMenuItem openInNewWindowItem;
	private JMenuItem openRemoteItem;
	private JMenuItem saveItem;
	private JMenuItem saveAsItem;
	private JMenuItem saveAsRemoteItem;
	private JMenuItem saveAsWebPageItem;
	private JMenuItem saveAllItem;
	private JMenuItem closeItem;
	private JMenuItem closeAllItem;
	private JMenuItem printItem;
	private JMenuItem printPreviewItem;
	private JMenuItem exitItem;
	private JMenuItem undoItem;
	private JMenuItem redoItem;
	private JMenuItem cutItem;
	private JMenuItem copyItem;
	private JMenuItem copyAsRtfItem;
	private JMenuItem pasteItem;
	private JMenuItem deleteItem;
	private JMenuItem findItem;
	private JMenuItem findNextItem;
	private JMenuItem replaceItem;
	private JMenuItem replaceNextItem;
	private JMenuItem findInFilesItem;
	private JMenuItem replaceInFilesItem;
	private JMenuItem goToItem;
	private JMenuItem selectAllItem;
	private JMenuItem timeDateItem;
	private JMenuItem optionsItem;
	private JCheckBoxMenuItem toolbarItem;
	private JCheckBoxMenuItem searchToolbarMenuItem;
	private JCheckBoxMenuItem statusBarItem;
	private JCheckBoxMenuItem lineNumbersItem;
	private JMenuItem nextDocItem;
	private JMenuItem prevDocItem;
	private JMenuItem increaseFontSizesItem;
	private JMenuItem decreaseFontSizesItem;
	//private JRadioButtonMenuItem ltrItem, rtlItem;
	//private JRadioButtonMenuItem splitHorizItem, splitVertItem, splitNoneItem;
	private JMenuItem helpItem;
	private JMenuItem homePageItem;
	private JMenuItem updatesItem;
	private JMenuItem aboutItem;
	private JMenuItem filePropItem;

	private JMenu fileMenu;
	private JMenu viewMenu;
	private JMenu windowMenu;
	private RecentFilesMenu recentFilesMenu;
	private JMenu savedMacroMenu;

	private RText rtext;

	/**
	 * Approximate maximum length, in pixels, of a File History entry.
	 * Note that this is only  GUIDELINE, and some filenames
	 * can (and will) exceed this limit.
	 */
	private final int MAX_FILE_PATH_LENGTH = 250;


	/**
	 * Creates an instance of the menu bar.
	 *
	 * @param rtext The instance of the <code>RText</code> editor that this
	 *        menu bar belongs to.
	 * @param lnfName The name for a look and feel; should be obtained from
	 *        <code>UIManager.getLookAndFeel().getName()</code>.
	 * @param properties The properties we'll be using to initialize the menu
	 *        bar.
	 */
	public RTextMenuBar(final RText rtext, String lnfName,
							RTextPreferences properties) {

		// Initialize some private variables.
		this.rtext = rtext;

		// Variables to create the menu.
		JMenu menu;
		JMenuItem menuItem;
		ResourceBundle msg = rtext.getResourceBundle();
		ResourceBundle menuMsg = ResourceBundle.getBundle("org.fife.rtext.MenuBar");

		// File submenu.
		fileMenu = createMenu(menuMsg, "MenuFile");
		registerMenuByName(MENU_FILE, fileMenu);
		add(fileMenu);

		// File submenu's items.
		newItem = createMenuItem(rtext.getAction(RText.NEW_ACTION));
		fileMenu.add(newItem);

		openItem = createMenuItem(rtext.getAction(RText.OPEN_ACTION));
		fileMenu.add(openItem);

		openInNewWindowItem = createMenuItem(rtext.getAction(RText.OPEN_NEWWIN_ACTION));
		fileMenu.add(openInNewWindowItem);

		openRemoteItem = createMenuItem(rtext.getAction(RText.OPEN_REMOTE_ACTION));
		fileMenu.add(openRemoteItem);

		closeItem = createMenuItem(rtext.getAction(RText.CLOSE_ACTION));
		fileMenu.add(closeItem);

		closeAllItem = createMenuItem(rtext.getAction(RText.CLOSE_ALL_ACTION));
		fileMenu.add(closeAllItem);

		fileMenu.addSeparator();

		saveItem = createMenuItem(rtext.getAction(RText.SAVE_ACTION));
		fileMenu.add(saveItem);

		saveAsItem = createMenuItem(rtext.getAction(RText.SAVE_AS_ACTION));
		fileMenu.add(saveAsItem);

		saveAsRemoteItem = createMenuItem(rtext.getAction(RText.SAVE_AS_REMOTE_ACTION));
		fileMenu.add(saveAsRemoteItem);

		saveAsWebPageItem = createMenuItem(rtext.getAction(RText.SAVE_WEBPAGE_ACTION));
		fileMenu.add(saveAsWebPageItem);

		saveAllItem = createMenuItem(rtext.getAction(RText.SAVE_ALL_ACTION));
		fileMenu.add(saveAllItem);

		fileMenu.addSeparator();

		printItem = createMenuItem(rtext.getAction(RText.PRINT_ACTION));
		fileMenu.add(printItem);

		printPreviewItem = createMenuItem(rtext.getAction(RText.PRINT_PREVIEW_ACTION));
		fileMenu.add(printPreviewItem);

		fileMenu.addSeparator();

		String[] initialContents = null;
		if (properties.fileHistoryString!=null) {
			initialContents = properties.fileHistoryString.split("<");
		}
		recentFilesMenu = new RecentFilesMenu(menuMsg.getString("RecentFiles"),
												initialContents) {
			@Override
			protected Action createOpenAction(String fileFullPath) {
				OpenFileFromHistoryAction action =
						new OpenFileFromHistoryAction(rtext);
				action.setName(getDisplayPath(fileFullPath));
				action.setFileFullPath(fileFullPath);
				return action;
			}
		};
		fileMenu.add(recentFilesMenu);

		// 1.5.2004/pwy: On OS X the Exit menu item is in the standard
		// application menu and is always generated by the system. No need to
		// have an additional Exit in the menu.
		if (rtext.getOS()!=RText.OS_MAC_OSX) {
			exitItem = createMenuItem(rtext.getAction(RText.EXIT_ACTION_KEY));
			fileMenu.addSeparator();
			fileMenu.add(exitItem);
		}

		// Edit submenu.
		menu = createMenu(menuMsg, "MenuEdit");
		registerMenuByName(MENU_EDIT, menu);
		add(menu);

		// Edit submenu's items.
		undoItem = createMenuItem(RTextArea.getAction(RTextArea.UNDO_ACTION));
		menu.add(undoItem);

		redoItem = createMenuItem(RTextArea.getAction(RTextArea.REDO_ACTION));
		menu.add(redoItem);

		menu.addSeparator();

		cutItem = createMenuItem(RTextArea.getAction(RTextArea.CUT_ACTION));
		menu.add(cutItem);

		copyItem = createMenuItem(RTextArea.getAction(RTextArea.COPY_ACTION));
		menu.add(copyItem);

		copyAsRtfItem = createMenuItem(rtext.getAction(RText.COPY_AS_RTF_ACTION));
		menu.add(copyAsRtfItem);

		pasteItem = createMenuItem(RTextArea.getAction(RTextArea.PASTE_ACTION));
		menu.add(pasteItem);

		deleteItem = createMenuItem(RTextArea.getAction(RTextArea.DELETE_ACTION));
		menu.add(deleteItem);

		menu.addSeparator();

		selectAllItem = createMenuItem(RTextArea.getAction(RTextArea.SELECT_ALL_ACTION));
		menu.add(selectAllItem);

		timeDateItem = createMenuItem(rtext.getAction(RText.TIME_DATE_ACTION));
		menu.add(timeDateItem);

		menu.addSeparator();

		// The "text" menu.  Note that keystrokes are okay here, because
		// these actions are also owned by RSyntaxTextArea and thus we
		// do not want to be able to change them.
		JMenu textMenu = createMenu(menuMsg, "MenuText");
		int defaultModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		menuItem = createMenuItem(
				new RSyntaxTextAreaEditorKit.ToggleCommentAction(),
				menuMsg, "ToggleComment", "ToggleCommentMnemonic",
				KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, defaultModifier));
		UIUtil.setDescription(menuItem, menuMsg, "DescToggleComment");
		textMenu.add(menuItem);
		textMenu.addSeparator();
		menuItem = createMenuItem(
				new RTextAreaEditorKit.DeleteRestOfLineAction(),
				menuMsg, "DeleteLineRemainder", "DeleteLineRemainderMnemonic",
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, defaultModifier));
		UIUtil.setDescription(menuItem, menuMsg, "DescDeleteLineRemainder");
		textMenu.add(menuItem);
		menuItem = createMenuItem(
				new RSyntaxTextAreaEditorKit.JoinLinesAction(),
				menuMsg, "JoinLines", "JoinLinesMnemonic",
				KeyStroke.getKeyStroke(KeyEvent.VK_J, defaultModifier));
		UIUtil.setDescription(menuItem, menuMsg, "DescJoinLines");
		textMenu.add(menuItem);
		textMenu.addSeparator();
		menuItem = createMenuItem(
				new RTextAreaEditorKit.UpperSelectionCaseAction(),
				menuMsg, "UpperCase", "UpperCaseMnemonic");
		UIUtil.setDescription(menuItem, menuMsg, "DescUpperCase");
		textMenu.add(menuItem);
		menuItem = createMenuItem(
				new RTextAreaEditorKit.LowerSelectionCaseAction(),
				menuMsg, "LowerCase", "LowerCaseMnemonic");
		UIUtil.setDescription(menuItem, menuMsg, "DescLowerCase");
		textMenu.add(menuItem);
		menuItem = createMenuItem(
				new RTextAreaEditorKit.InvertSelectionCaseAction(),
				menuMsg, "InvertCase", "InvertCaseMnemonic");
		UIUtil.setDescription(menuItem, menuMsg, "DescInvertCase");
		textMenu.add(menuItem);
		menu.add(textMenu);

		// The "indent" menu.  Note that keystrokes are okay here, because
		// these actions are also owned by the RSyntaxTextArea and thus we do
		// not want to be able to change them.
		JMenu indentMenu = createMenu(menuMsg, "MenuIndent");
		final int shift = InputEvent.SHIFT_MASK;
		menuItem = createMenuItem(
						//new RSyntaxTextAreaEditorKit.IncreaseIndentAction(),
						new RSyntaxTextAreaEditorKit.InsertTabAction(),
						menuMsg, "IncreaseIndent", "IncreaseIndentMnemonic",
						KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
		UIUtil.setDescription(menuItem, menuMsg, "DescIncreaseIndent");
		indentMenu.add(menuItem);
		menuItem = createMenuItem(
						new RSyntaxTextAreaEditorKit.DecreaseIndentAction(),
						menuMsg, "DecreaseIndent", "DecreaseIndentMnemonic",
						KeyStroke.getKeyStroke(KeyEvent.VK_TAB, shift));
		UIUtil.setDescription(menuItem, menuMsg, "DescDecreaseIndent");
		indentMenu.add(menuItem);
		menu.add(indentMenu);

		// 1.5.2004/pwy: On OS X the Options menu item is in the standard
		// application menu and is always generated by the system. No need
		// to have an additional Options in the menu.
		if (rtext.getOS()!=RText.OS_MAC_OSX) {
			menu.addSeparator();
			optionsItem = createMenuItem(rtext.getAction(RText.OPTIONS_ACTION));
			menu.add(optionsItem);
		}

		// Search menu.
		menu = createMenu(menuMsg, "MenuSearch");
		registerMenuByName(MENU_SEARCH, menu);
		add(menu);

		// Search menu's items.
		findItem = createMenuItem(rtext.getAction(RText.FIND_ACTION));
		menu.add(findItem);

		findNextItem = createMenuItem(rtext.getAction(RText.FIND_NEXT_ACTION));
		menu.add(findNextItem);

		replaceItem = createMenuItem(rtext.getAction(RText.REPLACE_ACTION));
		menu.add(replaceItem);

		replaceNextItem = createMenuItem(rtext.getAction(RText.REPLACE_NEXT_ACTION));
		menu.add(replaceNextItem);

		menu.addSeparator();

		findInFilesItem = createMenuItem(rtext.getAction(RText.FIND_IN_FILES_ACTION));
		menu.add(findInFilesItem);

		replaceInFilesItem = createMenuItem(
				rtext.getAction(RText.REPLACE_IN_FILES_ACTION));
		menu.add(replaceInFilesItem);

		menu.addSeparator();

		goToItem = createMenuItem(rtext.getAction(RText.GOTO_ACTION));
		menu.add(goToItem);

		menuItem = createMenuItem(
			new RSyntaxTextAreaEditorKit.GoToMatchingBracketAction(),
			menuMsg, "GoToMatchingBracket", "GoToMatchingBracketMnemonic",
			KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, defaultModifier));
		UIUtil.setDescription(menuItem, menuMsg, "DescGoToMatchingBracket");
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = createMenuItem(
				new RTextAreaEditorKit.NextBookmarkAction(
					RTextAreaEditorKit.rtaNextBookmarkAction, true),
				menuMsg, "NextBookmark", "NextBookmarkMnemonic",
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		UIUtil.setDescription(menuItem, menuMsg, "DescNextBookmark");
		menu.add(menuItem);

		menuItem = createMenuItem(
				new RTextAreaEditorKit.NextBookmarkAction(
					RTextAreaEditorKit.rtaNextBookmarkAction, false),
				menuMsg, "PreviousBookmark", "PreviousBookmarkMnemonic",
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, shift));
		UIUtil.setDescription(menuItem, menuMsg, "DescPreviousBookmark");
		menu.add(menuItem);

		menuItem = createMenuItem(
				new RTextAreaEditorKit.ToggleBookmarkAction(),
				menuMsg, "ToggleBookmark", "ToggleBookmarkMnemonic",
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, defaultModifier));
		UIUtil.setDescription(menuItem, menuMsg, "DescToggleBookmark");
		menu.add(menuItem);

		// View submenu.
		viewMenu = createMenu(menuMsg, "MenuView");
		registerMenuByName(MENU_VIEW, viewMenu);
		viewMenu.getPopupMenu().addPopupMenuListener(this);
		add(viewMenu);

		// View submenu's items.
		JMenu toolbarsMenu = new JMenu(menuMsg.getString("Toolbars"));
		Action a = rtext.getAction(RText.TOOL_BAR_ACTION);
		toolbarItem = new JCheckBoxMenuItem(a);
		toolbarItem.setToolTipText(null);
		toolbarItem.setSelected(properties.toolbarVisible);
		toolbarsMenu.add(toolbarItem);
		SearchToolBarAction stba = new SearchToolBarAction(rtext);
		stba.setName(menuMsg.getString("QuickSearchBar"));
		searchToolbarMenuItem = new JCheckBoxMenuItem(stba);
		searchToolbarMenuItem.setSelected(properties.searchToolBarVisible);
		UIUtil.setDescription(searchToolbarMenuItem, menuMsg,"DescQuickSearch");
		toolbarsMenu.add(searchToolbarMenuItem);
		viewMenu.add(toolbarsMenu);

		// Font sizes submenu.
		JMenu fontSizesMenu = createMenu(menuMsg, "MenuFontSizes");
		a = rtext.getAction(RText.DEC_FONT_SIZES_ACTION);
		decreaseFontSizesItem = createMenuItem(a);
		fontSizesMenu.add(decreaseFontSizesItem);
		a = rtext.getAction(RText.INC_FONT_SIZES_ACTION);
		increaseFontSizesItem = createMenuItem(a);
		fontSizesMenu.add(increaseFontSizesItem);
		viewMenu.add(fontSizesMenu);
/*
		// Text orientation submenu
		JMenu orientMenu = createMenu(menuMsg, "TextOrientation");
		ltrItem = createRadioButtonMenuItem(
				rtext.getAction(RText.LTR_ACTION),
				null, menuMsg.getString("DescLeftToRight"));
		orientMenu.add(ltrItem);
		rtlItem = createRadioButtonMenuItem(
				rtext.getAction(RText.RTL_ACTION),
				null, menuMsg.getString("DescRightToLeft"));
		orientMenu.add(rtlItem);
		ButtonGroup bg = new ButtonGroup();
		bg.add(ltrItem);
		bg.add(rtlItem);
		viewMenu.add(orientMenu);

		viewMenu.addSeparator();

		// Split view submenu.
		JMenu splitViewMenu = createMenu(menuMsg, "MenuSplitView",
									"MenuSplitViewMnemonic");
		splitHorizItem = createRadioButtonMenuItem(
				rtext.getAction(RText.VIEW_SPLIT_HORIZ_ACTION),
				null, menuMsg.getString("DescSplitViewHoriz"));
		splitViewMenu.add(splitHorizItem);
		splitVertItem = createRadioButtonMenuItem(
				rtext.getAction(RText.VIEW_SPLIT_VERT_ACTION),
				null, menuMsg.getString("DescSplitViewVert"));
		splitViewMenu.add(splitVertItem);
		splitNoneItem = createRadioButtonMenuItem(
				rtext.getAction(RText.VIEW_SPLIT_NONE_ACTION),
				null, menuMsg.getString("DescSplitViewNone"));
		splitViewMenu.add(splitNoneItem);
		viewMenu.add(splitViewMenu);
*/

		JMenu dwMenu = createMenu(menuMsg, "MenuDockedWindows");
		registerMenuByName(MENU_DOCKED_WINDOWS, dwMenu);
		viewMenu.add(dwMenu);

		statusBarItem = new JCheckBoxMenuItem(rtext.getAction(RText.STATUS_BAR_ACTION));
		statusBarItem.setToolTipText(null);
		statusBarItem.setSelected(properties.statusBarVisible);
		viewMenu.add(statusBarItem);

		lineNumbersItem = new JCheckBoxMenuItem(rtext.getAction(RText.LINE_NUMBER_ACTION));
		lineNumbersItem.setSelected(properties.lineNumbersVisible);
		lineNumbersItem.setToolTipText(null);
//		UIUtil.setDescription(lineNumbersItem, msg, "DescLineNumbers");
		viewMenu.add(lineNumbersItem);

		viewMenu.addSeparator();

		JMenu focusDwMenu = createMenu(menuMsg, "MenuFocusDockableWindowGroup");
		focusDwMenu.add(createMenuItem(rtext.getAction(RText.MOVE_FOCUS_LEFT_ACTION)));
		focusDwMenu.add(createMenuItem(rtext.getAction(RText.MOVE_FOCUS_RIGHT_ACTION)));
		focusDwMenu.add(createMenuItem(rtext.getAction(RText.MOVE_FOCUS_UP_ACTION)));
		focusDwMenu.add(createMenuItem(rtext.getAction(RText.MOVE_FOCUS_DOWN_ACTION)));
		viewMenu.add(focusDwMenu);

		viewMenu.addSeparator();

		nextDocItem = createMenuItem(rtext.getAction(RText.NEXT_DOCUMENT_ACTION));
		viewMenu.add(nextDocItem);
		prevDocItem = createMenuItem(rtext.getAction(RText.PREVIOUS_DOCUMENT_ACTION));
		viewMenu.add(prevDocItem);

		viewMenu.addSeparator();

		filePropItem = createMenuItem(rtext.getAction(
										RText.FILE_PROPERTIES_ACTION));
		viewMenu.add(filePropItem);

		// Window menu (only visible when in MDI mode).
		windowMenu = createMenu(menuMsg, "MenuWindow");
		add(windowMenu);

		JMenuItem item = new JMenuItem(menuMsg.getString("TileVertically"));
		UIUtil.setDescription(item, msg, "DescTileVertically");
		item.setActionCommand("TileVertically");
		item.addActionListener(rtext);
		windowMenu.add(item);

		item = new JMenuItem(menuMsg.getString("TileHorizontally"));
		UIUtil.setDescription(item, msg, "DescTileHorizontally");
		item.setActionCommand("TileHorizontally");
		item.addActionListener(rtext);
		windowMenu.add(item);

		item = new JMenuItem(menuMsg.getString("Cascade"));
		UIUtil.setDescription(item, msg, "DescCascade");
		item.setActionCommand("Cascade");
		item.addActionListener(rtext);
		windowMenu.add(item);

		windowMenu.addSeparator();

		// Add listener that will create open document list.
		windowMenu.getPopupMenu().addPopupMenuListener(this);

		// Help submenu.
		menu = createMenu(menuMsg, "MenuHelp");
		registerMenuByName(MENU_HELP, menu);
		add(menu);

		// Help submenu's items.
		helpItem = createMenuItem(rtext.getAction(RText.HELP_ACTION_KEY));
		menu.add(helpItem);

		homePageItem = createMenuItem(rtext.getAction(RText.HOME_PAGE_ACTION));
		menu.add(homePageItem);

		updatesItem = createMenuItem(rtext.getAction(RText.UPDATES_ACTION));
		menu.add(updatesItem);

		menu.addSeparator();

		aboutItem = createMenuItem(rtext.getAction(RText.ABOUT_ACTION_KEY));
		menu.add(aboutItem);

	}


	/**
	 * Adds the file specified to the file history.
	 *
	 * @param fileFullPath Full path to a file to add to the file history in
	 *        the File menu.
	 * @see #getFileHistoryString
	 */
	private void addFileToFileHistory(String fileFullPath) {
		// We don't remember just-created empty text files.
		// Also, due to the Preferences API needing a non-null key for all
		// values, a "-" filename means no files were found for the file
		// history.  So, we won't add this file in either.
		if (fileFullPath.endsWith(File.separatorChar + rtext.getNewFileName())
				|| fileFullPath.equals("-")) {
			return;
		}
		recentFilesMenu.addFileToFileHistory(fileFullPath);
	}


	/**
	 * Attempts to return an "attractive" shortened version of
	 * <code>fullPath</code>.  For example,
	 * <code>/home/lobster/dir1/dir2/dir3/dir4/file.out</code> could be
	 * abbreviated as <code>/home/lobster/dir1/.../file.out</code>.  Note that
	 * this method is still in the works, and isn't fully cooked yet.
	 */
	private String getDisplayPath(String longPath) {

		// Initialize some variables.
		FontMetrics fontMetrics = getFontMetrics(getFont());
		int textWidth = getTextWidth(longPath, fontMetrics);

		// If the text width is already short enough to fit, don't do anything to it.		
		if (textWidth <= MAX_FILE_PATH_LENGTH) {
			return longPath;
		}

		// If it's too long, we'll have to trim it down some...

		// Will be '\' for Windows, '/' for Unix and derivatives.
		String separator = System.getProperty("file.separator");

		// What we will eventually return.
		String displayString = longPath;

		// If there is no directory separator, then the string is just a file name,
		// and so we can't shorten it.  Just return the sucker.
		int lastSeparatorPos = displayString.lastIndexOf(separator);
		if (lastSeparatorPos==-1)
			return displayString;

		// Get the length of just the file name.
		String justFileName = displayString.substring(
						lastSeparatorPos+1, displayString.length());
		int justFileNameLength = getTextWidth(justFileName, fontMetrics);

		// If even just the file name is too long, return it.
		if (justFileNameLength > MAX_FILE_PATH_LENGTH)
			return "..." + separator + justFileName;

		// Otherwise, just keep adding levels in the directory hierarchy
		// until the name gets too long.
		String endPiece = "..." + separator + justFileName;
		int endPieceLength = getTextWidth(endPiece, fontMetrics);
		int separatorPos = displayString.indexOf(separator, 0);
		String firstPart = displayString.substring(0, separatorPos+1);
		int firstPartLength = getTextWidth(firstPart, fontMetrics);
		String tempFirstPart = firstPart;
		int tempFirstPartLength = firstPartLength;
		while (tempFirstPartLength+endPieceLength < MAX_FILE_PATH_LENGTH) {
			firstPart  = tempFirstPart;
			separatorPos = displayString.indexOf(separator, separatorPos+1);
			if (separatorPos==-1)
				endPieceLength = 9999999;
			else {
				tempFirstPart = displayString.substring(0, separatorPos+1);
				tempFirstPartLength = getTextWidth(tempFirstPart, fontMetrics);
			}
		}

		return firstPart+endPiece;

	}


	/**
	 * Returns a string representing all files in the file history separated by
	 * '<' characters.  This character was chosen as the separator because it
	 * is a character that cannot be used in filenames in both Windows and
	 * UNIX/Linux.
	 *
	 * @return A <code>String</code> representing all files in the file
	 *         history, separated by '<' characters.  If no files are in the
	 *         file history, then <code>null</code> is returned.
	 */
	public String getFileHistoryString() {
		String retVal = "";
		int historyCount = recentFilesMenu.getItemCount();
		for (int i=historyCount-1; i>=0; i--) {
			retVal += recentFilesMenu.getFileFullPath(i) + "<";
		}
		if (retVal.length()>0)
			retVal = retVal.substring(0, retVal.length()-1); // Remove trailing '>'.
		return retVal;
	}


	/**
	 * Returns the maximum number of files the file history in the File menu
	 * will remember.
	 *
	 * @return The maximum size of the file history.
	 */
	public int getMaximumFileHistorySize() {
		return recentFilesMenu.getMaximumFileHistorySize();
	}


	/**
	 * Determines the width of the given <code>String</code> containing no
	 * tabs.  Note that this is simply a trimmed-down version of
	 * <code>javax.swing.text.getTextWidth</code> that has been
	 * optimized for our use.
	 *
	 * @param s  the source of the text
	 * @param metrics the font metrics to use for the calculation
	 * @return  the width of the text
	 */
	private static final int getTextWidth(String s, FontMetrics metrics) {

		int textWidth = 0;

		char[] txt = s.toCharArray();
		int n = txt.length;
		for (int i=0; i<n; i++) {
			// Ignore newlines, they take up space and we shouldn't be
			// counting them.
			if(txt[i] != '\n')
				textWidth += metrics.charWidth(txt[i]);
		}
		return textWidth;
	}


	/**
	 * Thanks to Java Bug ID 5026829, JMenuItems (among other Swing components)
	 * don't update their accelerators, etc. when the properties on which they
	 * were created update them.  Thus, we have to do this manually.  This is
	 * still broken as of 1.5.
	 */
	protected void menuItemAcceleratorWorkaround() {

		updateAction(newItem, RText.NEW_ACTION);
		updateAction(openItem, RText.OPEN_ACTION);
		updateAction(openInNewWindowItem, RText.OPEN_NEWWIN_ACTION);
		updateAction(openRemoteItem, RText.OPEN_REMOTE_ACTION);
		updateAction(closeItem, RText.CLOSE_ACTION);
		updateAction(closeAllItem, RText.CLOSE_ALL_ACTION);
		if (rtext.getOS()!=RText.OS_MAC_OSX) {
			updateAction(exitItem, RText.EXIT_ACTION_KEY);
		}
		updateAction(saveItem, RText.SAVE_ACTION);
		updateAction(saveAsItem, RText.SAVE_AS_ACTION);
		updateAction(saveAsRemoteItem, RText.SAVE_AS_REMOTE_ACTION);
		updateAction(saveAsWebPageItem, RText.SAVE_WEBPAGE_ACTION);
		updateAction(saveAllItem, RText.SAVE_ALL_ACTION);
		updateAction(printItem, RText.PRINT_ACTION);
		updateAction(printPreviewItem, RText.PRINT_PREVIEW_ACTION);
		updateAction(findItem, RText.FIND_ACTION);
		updateAction(findNextItem, RText.FIND_NEXT_ACTION);
		updateAction(replaceItem, RText.REPLACE_ACTION);
		updateAction(replaceNextItem, RText.REPLACE_NEXT_ACTION);
		updateAction(findInFilesItem, RText.FIND_IN_FILES_ACTION);
		updateAction(replaceInFilesItem, RText.REPLACE_IN_FILES_ACTION);
		updateAction(goToItem, RText.GOTO_ACTION);
		updateAction(copyAsRtfItem, RText.COPY_AS_RTF_ACTION);
		updateAction(timeDateItem, RText.TIME_DATE_ACTION);
		if (rtext.getOS()!=RText.OS_MAC_OSX) {
			updateAction(optionsItem, RText.OPTIONS_ACTION);
		}
		updateAction(toolbarItem, RText.TOOL_BAR_ACTION);
		updateAction(statusBarItem, RText.STATUS_BAR_ACTION);
		updateAction(lineNumbersItem, RText.LINE_NUMBER_ACTION);
		updateAction(nextDocItem, RText.NEXT_DOCUMENT_ACTION);
		updateAction(prevDocItem, RText.PREVIOUS_DOCUMENT_ACTION);
		updateAction(filePropItem, RText.FILE_PROPERTIES_ACTION);
		updateAction(helpItem, RText.HELP_ACTION_KEY);
		updateAction(homePageItem, RText.HOME_PAGE_ACTION);
		updateAction(updatesItem, RText.UPDATES_ACTION);
		updateAction(aboutItem, RText.ABOUT_ACTION_KEY);

	}


	/**
	 * Called whenever a property changes on a component we're listening to.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String prop = e.getPropertyName();

		if (prop.equals(AbstractMainView.TEXT_AREA_ADDED_PROPERTY)) {
			RTextEditorPane textArea = (RTextEditorPane)e.getNewValue();
			addFileToFileHistory(textArea.getFileFullPath());
		}

	}


	public void popupMenuCanceled(PopupMenuEvent e) {
	}


	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}


	/**
	 * Called when one of the popup menus is about to become visible.
	 *
	 * @param e The popup menu event.
	 */
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

		Object source = e.getSource();

		// Ensure checkmarks for visible stuff are correct.
		if (source==viewMenu.getPopupMenu()) {
			AbstractMainView mainView = rtext.getMainView();
			lineNumbersItem.setSelected(mainView.getLineNumbersEnabled());
		}

		// If the "window" menu is becoming visible (MDI view only)...
		else if (source==windowMenu.getPopupMenu()) {

			JPopupMenu popupMenu = windowMenu.getPopupMenu();
			// 4: Cascade, Tile Vertically & horizontally and the separator.
			int count = popupMenu.getComponentCount() - 4;

			// Remove the old menu items for each open document.
			for (int i=0; i<count; i++)
				windowMenu.remove(4);

			// Since we only listen for the "Window" menu, and the Window
			// menu is only available when the AbstractMainView is the MDI
			// view...
			if (!(rtext.getMainView() instanceof RTextMDIView))
				return;
			final RTextMDIView mdiView = (RTextMDIView)rtext.getMainView();

			// Add a menu item for each open document.
			JMenu currentMenu = windowMenu;	// So our menu doesn't get too long.
			int i = 0;
			count = mdiView.getNumDocuments();
			int selectedIndex = mdiView.getSelectedIndex();
			while (i<count) {
				if ((i+1)%15 == 0) {
					currentMenu.add(new JMenu("More..."));
					currentMenu = (JMenu)currentMenu.getItem(currentMenu.getItemCount()-1);
				}
				String text = (i+1) + " " + getDisplayPath(
					mdiView.getRTextEditorPaneAt(i).getFileFullPath());
				final int index = i;
				JRadioButtonMenuItem menuItem =
					new JRadioButtonMenuItem(
						new AbstractAction() {
							public void actionPerformed(ActionEvent e) {
								mdiView.setSelectedIndex(index);
							}
						});
				menuItem.setText(text);
				menuItem.setSelected(i==selectedIndex);
				currentMenu.add(menuItem);
				i++;
			} // End of while (i<count).
			currentMenu = null;

		}

		// If the "Macros" popup is becoming visible...
		else if (source==savedMacroMenu.getPopupMenu()) {

			// Clear old macros from menu (some may have been added/removed).
			savedMacroMenu.removeAll();

			// Get all saved macros (guaranteed non-null array).
			File[] files = RTextUtilities.getSavedMacroFiles();

			// Add all saved macros to the popup menu.
			int count = files.length;
			for (int i=0; i<count; i++) {
				String name = RTextUtilities.getMacroName(files[i]);
				final File f = files[i];
				AbstractAction a = new AbstractAction(name) {
						public void actionPerformed(ActionEvent e) {
							rtext.getMainView().loadMacro(f);
						}
				};
				savedMacroMenu.add(new JMenuItem(a));
			}

			savedMacroMenu.applyComponentOrientation(
										getComponentOrientation());

		}

	}


	/**
	 * Sets whether the "QuickSearch toolbar" menu item is selected.
	 *
	 * @param selected Whether the QuickSearch toolbar menu item is
	 *        selected.
	 */
	public void setSearchToolbarMenuItemSelected(boolean selected) {
		searchToolbarMenuItem.setSelected(selected);
	}


	/**
	 * Sets whether or not the "Window" menu is visible.  This menu should
	 * only be visible on the MDI view.
	 *
	 * @param visible Whether or not the menu should be visible.
	 */
	public void setWindowMenuVisible(boolean visible) {
		if (visible)
			add(windowMenu, 4);
		else
			remove(windowMenu);
		validate(); // Must call validate() to repaint menu bar.
	}


	private void updateAction(JMenuItem item, String key) {
		item.setAction(null);
		item.setAction(rtext.getAction(key));
		item.setToolTipText(null);
	}


	/**
	 * Overridden to make sure that the "Window" menu gets its look-and-feel
	 * updated too, even if it currently isn't visible.
	 */
	@Override
	public void updateUI() {

		super.updateUI();

		// Update the Window menu only if we're NOT in MDI view (otherwise, it
		// would have been updated by super.updateUI(ui)).  We must also check
		// windowMenu for null as this is called during initialization.
		if (rtext!=null && rtext.getMainViewStyle()!=RText.MDI_VIEW &&
				windowMenu!=null) {
			SwingUtilities.updateComponentTreeUI(windowMenu);
		}

	}


}