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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

import org.fife.ui.OS;
import org.fife.ui.RecentFilesMenu;
import org.fife.ui.UIUtil;
import org.fife.ui.app.MenuBar;
import org.fife.ui.app.icons.IconGroup;
import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.fife.ui.rsyntaxtextarea.Theme;
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
public class RTextMenuBar extends MenuBar<RText>
	implements PropertyChangeListener, PopupMenuListener {

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

	private JMenuItem newItem;
	private JMenuItem openItem;
	private JMenuItem openInNewWindowItem;
	private JMenuItem openRecentItem;
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
	private JMenuItem copyAsStyledTextItem;
	private JMenuItem copyAsStyledTextMonokaiItem;
	private JMenuItem copyAsStyledTextEclipseItem;
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


	/**
	 * Creates an instance of the menu bar.
	 *
	 * @param rtext The instance of the <code>RText</code> editor that this
	 *        menu bar belongs to.
	 */
	public RTextMenuBar(final RText rtext) {
		super(rtext);
	}


	private void addCopyAsSubMenu(JMenu menu) {

		RText rtext = getApplication();
		ResourceBundle msg = ResourceBundle.getBundle("org.fife.rtext.actions.Actions");

		JMenu subMenu = new JMenu(msg.getString("CopyAs"));

		copyAsStyledTextItem = createMenuItem(rtext.getAction(RText.COPY_AS_STYLED_TEXT_ACTION));
		subMenu.add(copyAsStyledTextItem);

		String[] themes = { "dark", "default", "eclipse", "idea", "monokai" };
		try {

			for (String theme : themes) {

				String themeTitleCase = Character.toUpperCase(theme.charAt(0)) +
					theme.substring(1);
				String title = MessageFormat.format(msg.getString(
					"CopyAsStyledTextAction.Themed"), themeTitleCase);

				subMenu.add(createCopyAsStyledTextMenuItem(title, theme));
			}
		} catch (IOException ioe) {
			rtext.displayException(ioe);
		}

		menu.add(subMenu);
	}

	private JMenuItem createCopyAsStyledTextMenuItem(String title, String themeName)
		throws IOException {
		Theme theme = Theme.load(getClass().getResourceAsStream(
			"/org/fife/ui/rsyntaxtextarea/themes/" + themeName + ".xml"));
		RSyntaxTextAreaEditorKit.CopyCutAsStyledTextAction action =
			new RSyntaxTextAreaEditorKit.CopyCutAsStyledTextAction(themeName, theme, false);
		action.setName(title);
		return createMenuItem(action);
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
		RText rtext = getApplication();
		if (fileFullPath.endsWith(File.separatorChar + rtext.getNewFileName()) ||
				fileFullPath.equals("-")) {
			return;
		}
		recentFilesMenu.addFileToFileHistory(fileFullPath);
	}


	@Override
	public void addNotify() {

		super.addNotify();

		// Populate file history here to avoid issue with Darcula
		RText rtext = getApplication();
		List<FileLocation> recentFiles = rtext.getRecentFiles();
		List<FileLocation> recentFilesCopy = new ArrayList<>(recentFiles);
		Collections.reverse(recentFilesCopy);
		for (FileLocation file : recentFilesCopy) {
			recentFilesMenu.addFileToFileHistory(file.getFileFullPath());
		}
	}


	private JMenu createEditMenu(ResourceBundle menuMsg, int defaultModifier, int shift) {

		RText rtext = getApplication();
		JMenu menu = createMenu(menuMsg, "MenuEdit");

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

		addCopyAsSubMenu(menu);

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
		JMenuItem menuItem = createMenuItem(
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
		if (rtext.getOS()!=OS.MAC_OS_X) {
			menu.addSeparator();
			optionsItem = createMenuItem(rtext.getAction(RText.OPTIONS_ACTION));
			menu.add(optionsItem);
		}

		return menu;
	}


	private JMenu createFileMenu(ResourceBundle menuMsg) {

		RText rtext = getApplication();
		JMenu fileMenu = createMenu(menuMsg, "MenuFile");

		// File submenu's items.
		newItem = createMenuItem(rtext.getAction(RText.NEW_ACTION));
		fileMenu.add(newItem);

		openItem = createMenuItem(rtext.getAction(RText.OPEN_ACTION));
		fileMenu.add(openItem);

		openInNewWindowItem = createMenuItem(rtext.getAction(RText.OPEN_NEWWIN_ACTION));
		fileMenu.add(openInNewWindowItem);

		openRecentItem = createMenuItem(rtext.getAction(RText.OPEN_RECENT_ACTION));
		fileMenu.add(openRecentItem);

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

		recentFilesMenu = new RecentFilesMenu(menuMsg.getString("RecentFiles")) {
			@Override
			protected Action createOpenAction(String fileFullPath) {
				OpenFileFromHistoryAction action =
					new OpenFileFromHistoryAction(rtext);
				action.setName(UIUtil.getDisplayPathForFile(RTextMenuBar.this, fileFullPath));
				action.setFileFullPath(fileFullPath);
				return action;
			}
		};
		fileMenu.add(recentFilesMenu);

		// 1.5.2004/pwy: On OS X the Exit menu item is in the standard
		// application menu and is always generated by the system. No need to
		// have an additional Exit in the menu.
		if (rtext.getOS()!=OS.MAC_OS_X) {
			exitItem = createMenuItem(rtext.getAction(RText.EXIT_ACTION_KEY));
			fileMenu.addSeparator();
			fileMenu.add(exitItem);
		}

		return fileMenu;
	}


	private JMenu createHelpMenu(ResourceBundle menuMsg, RText rtext) {

		JMenu menu = createMenu(menuMsg, "MenuHelp");

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

		return menu;
	}


	private JMenu createSearchMenu(ResourceBundle menuMsg, int defaultModifier, int shift) {

		RText rtext = getApplication();
		JMenu menu = createMenu(menuMsg, "MenuSearch");

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

		JMenuItem menuItem = createMenuItem(
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

		return menu;
	}


	private JMenu createViewMenu(ResourceBundle menuMsg) {

		RText rtext = getApplication();
		JMenu viewMenu = createMenu(menuMsg, "MenuView");

		// View submenu's items.
		JMenu toolbarsMenu = new JMenu(menuMsg.getString("Toolbars"));
		Action a = rtext.getAction(RText.TOOL_BAR_ACTION);
		toolbarItem = new JCheckBoxMenuItem(a);
		toolbarItem.setToolTipText(null);
		toolbarItem.setSelected(rtext.getToolBarVisible());
		toolbarsMenu.add(toolbarItem);
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
		statusBarItem.setSelected(rtext.getStatusBarVisible());
		viewMenu.add(statusBarItem);

		lineNumbersItem = new JCheckBoxMenuItem(rtext.getAction(RText.LINE_NUMBER_ACTION));
		lineNumbersItem.setSelected(rtext.getMainView().getLineNumbersEnabled());
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

		return viewMenu;
	}


	private JMenu createWindowMenu(ResourceBundle menuMsg, ResourceBundle msg) {

		RText rtext = getApplication();
		JMenu windowMenu = createMenu(menuMsg, "MenuWindow");

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

		return windowMenu;
	}


	/**
	 * Returns the list of files in the "recent files" menu.
	 *
	 * @return The list of files in the "recent files" menu.
	 */
	public List<String> getFileHistory() {
		return recentFilesMenu.getFileHistory();
	}


	/**
	 * Returns a string representing all files in the file history separated by
	 * '&lt;' characters.  This character was chosen as the separator because
	 * it is a character that cannot be used in filenames in both Windows and
	 * UNIX/Linux.
	 *
	 * @return A <code>String</code> representing all files in the file
	 *         history, separated by '&lt;' characters.  If no files are in the
	 *         file history, then <code>null</code> is returned.
	 */
	public String getFileHistoryString() {

		StringBuilder retVal = new StringBuilder();

		int historyCount = recentFilesMenu.getItemCount();
		for (int i=historyCount-1; i>=0; i--) {
			retVal.append(recentFilesMenu.getFileFullPath(i)).append("<");
		}

		if (retVal.length()>0)
			retVal = new StringBuilder(retVal.substring(0, retVal.length() - 1)); // Remove trailing '>'.
		return retVal.toString();
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


	@Override
	protected void initializeUI() {

		RText app = getApplication();

		// Variables to create the menu.
		ResourceBundle msg = app.getResourceBundle();
		ResourceBundle menuMsg = ResourceBundle.getBundle("org.fife.rtext.MenuBar");
		int defaultModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		final int shift = InputEvent.SHIFT_DOWN_MASK;

		// File submenu.
		fileMenu = createFileMenu(menuMsg);
		registerMenuByName(MENU_FILE, fileMenu);
		add(fileMenu);

		// Edit submenu.
		JMenu menu = createEditMenu(menuMsg, defaultModifier, shift);
		registerMenuByName(MENU_EDIT, menu);
		add(menu);

		// Search menu.
		menu = createSearchMenu(menuMsg, defaultModifier, shift);
		registerMenuByName(MENU_SEARCH, menu);
		add(menu);

		// View submenu.
		viewMenu = createViewMenu(menuMsg);
		registerMenuByName(MENU_VIEW, viewMenu);
		viewMenu.getPopupMenu().addPopupMenuListener(this);
		add(viewMenu);

		// Window menu (only visible when in MDI mode).
		windowMenu = createWindowMenu(menuMsg, msg);
		add(windowMenu);

		// Help submenu.
		menu = createHelpMenu(menuMsg, app);
		registerMenuByName(MENU_HELP, menu);
		add(menu);
	}


	/**
	 * Called whenever a property changes on a component we're listening to.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {

		String prop = e.getPropertyName();

		if (prop.equals(AbstractMainView.TEXT_AREA_ADDED_PROPERTY)) {
			RTextEditorPane textArea = (RTextEditorPane)e.getNewValue();
			addFileToFileHistory(textArea.getFileFullPath());
		}

	}


	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}


	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}


	/**
	 * Called when one of the popup menus is about to become visible.
	 *
	 * @param e The popup menu event.
	 */
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

		RText rtext = getApplication();
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
				String text = (i+1) + " " + UIUtil.getDisplayPathForFile(
					this, mdiView.getRTextEditorPaneAt(i).getFileFullPath());
				final int index = i;
				JRadioButtonMenuItem menuItem =
					new JRadioButtonMenuItem(
						new AbstractAction() {
							@Override
							public void actionPerformed(ActionEvent e) {
								mdiView.setSelectedIndex(index);
							}
						});
				menuItem.setText(text);
				menuItem.setSelected(i==selectedIndex);
				currentMenu.add(menuItem);
				i++;
			} // End of while (i<count).

		}

		// If the "Macros" popup is becoming visible...
		else if (source==savedMacroMenu.getPopupMenu()) {

			// Clear old macros from menu (some may have been added/removed).
			savedMacroMenu.removeAll();

			// Get all saved macros (guaranteed non-null array).
			File[] files = RTextUtilities.getSavedMacroFiles();

			// Add all saved macros to the popup menu.
			for (File file : files) {
				String name = RTextUtilities.getMacroName(file);
				final File f = file;
				AbstractAction a = new AbstractAction(name) {
					@Override
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
		item.setAction(getApplication().getAction(key));
		item.setToolTipText(null);
	}


	@Override
	protected void updateIcons(IconGroup iconGroup) {

		newItem.setIcon(iconGroup.getNativeIcon("new"));
		openItem.setIcon(iconGroup.getNativeIcon("open"));
		openInNewWindowItem.setIcon(iconGroup.getNativeIcon("openinnewwindow"));
		saveItem.setIcon(iconGroup.getNativeIcon("saave"));
		saveAsItem.setIcon(iconGroup.getNativeIcon("saveas"));
		saveAllItem.setIcon(iconGroup.getNativeIcon("saveall"));
		closeItem.setIcon(iconGroup.getNativeIcon("close"));
		closeAllItem.setIcon(iconGroup.getNativeIcon("closeall"));
		printItem.setIcon(iconGroup.getNativeIcon("prin"));
		printPreviewItem.setIcon(iconGroup.getNativeIcon("printpreview"));
		undoItem.setIcon(iconGroup.getNativeIcon("undo"));
		redoItem.setIcon(iconGroup.getNativeIcon("redo"));
		cutItem.setIcon(iconGroup.getNativeIcon("cut"));
		copyItem.setIcon(iconGroup.getNativeIcon("copy"));
		pasteItem.setIcon(iconGroup.getNativeIcon("paste"));
		deleteItem.setIcon(iconGroup.getNativeIcon("delete"));
		findItem.setIcon(iconGroup.getNativeIcon("find"));
		findNextItem.setIcon(iconGroup.getNativeIcon("findnext"));
		replaceItem.setIcon(iconGroup.getNativeIcon("replace"));
		replaceNextItem.setIcon(iconGroup.getNativeIcon("replacenext"));
		goToItem.setIcon(iconGroup.getNativeIcon("goto"));
		selectAllItem.setIcon(iconGroup.getNativeIcon("selectall"));
		if (optionsItem != null) {
			optionsItem.setIcon(iconGroup.getNativeIcon("options"));
		}
		helpItem.setIcon(iconGroup.getNativeIcon("help"));
		aboutItem.setIcon(iconGroup.getNativeIcon("about"));
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
		RText rtext = getApplication();
		if (rtext!=null && rtext.getMainViewStyle()!=RText.MDI_VIEW &&
				windowMenu!=null) {
			SwingUtilities.updateComponentTreeUI(windowMenu);
		}

	}


}
