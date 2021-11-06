/*
 * 11/14/2003
 *
 * RText.java - A syntax highlighting programmer's text editor written in Java.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.Element;

import org.fife.help.HelpDialog;
import org.fife.rsta.ui.CollapsibleSectionPanel;
import org.fife.rtext.actions.ActionFactory;
import org.fife.ui.CustomizableToolBar;
import org.fife.ui.OptionsDialog;
import org.fife.ui.SplashScreen;
import org.fife.ui.app.*;
import org.fife.ui.app.icons.IconGroup;
import org.fife.ui.app.icons.RasterImageIconGroup;
import org.fife.ui.app.icons.SvgIconGroup;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowConstants;
import org.fife.ui.dockablewindows.DockableWindowPanel;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rtextfilechooser.FileChooserOwner;
import org.fife.ui.rtextfilechooser.RTextFileChooser;
import org.fife.util.TranslucencyUtil;


/**
 * An instance of the RText text editor.  <code>RText</code> is a programmer's
 * text editor with the following features:
 *
 * <ul>
 *   <li>Syntax highlighting for 45+ languages.
 *   <li>Code folding.
 *   <li>Edit multiple documents simultaneously.
 *   <li>Auto-indent.
 *   <li>Find/Replace/Find in Files, with regular expression functionality.
 *   <li>Printing and Print Preview.
 *   <li>Online help.
 *   <li>Intelligent source browsing via Exuberant Ctags.
 *   <li>Macros.
 *   <li>Code templates.
 *   <li>Many other features.
 * </ul>
 *
 * At the heart of this program is
 * {@link org.fife.ui.rsyntaxtextarea.RSyntaxTextArea}, a fully-featured,
 * syntax highlighting text component.  That's where most of the meat is.
 * All text areas are contained in a subclass of
 * {@link org.fife.rtext.AbstractMainView}, which keeps the state of all of the
 * text areas in synch (fonts used, colors, etc.).  This class (RText) contains
 * an instance of a subclass of {@link org.fife.rtext.AbstractMainView} (which
 * contains all of the text areas) as well as the menu, source browser, and
 * status bar.
 *
 * @author Robert Futrell
 * @version 3.0.1
 */
public class RText extends AbstractPluggableGUIApplication<RTextPrefs>
			implements ActionListener, CaretListener, PropertyChangeListener,
						RTextActionInfo, FileChooserOwner {

	// Constants specifying the current view style.
	public static final int TABBED_VIEW				= 0;
	public static final int SPLIT_PANE_VIEW				= 1;
	public static final int MDI_VIEW					= 2;

	// Properties fired.
	private static final String MAIN_VIEW_STYLE_PROPERTY	= "RText.mainViewStyle";

	private Map<String, IconGroup> iconGroupMap;

	private RTextMenuBar menuBar;

	private OptionsDialog optionsDialog;

	private CollapsibleSectionPanel csp; // Contains the AbstractMainView
	private AbstractMainView mainView;	// Component showing all open documents.
	private int mainViewStyle;

	private RTextFileChooser chooser;
	private RemoteFileChooser rfc;

	private HelpDialog helpDialog;

	private SpellingErrorWindow spellingWindow;

	private SyntaxScheme colorScheme;

	private String workingDirectory;	// The directory for new empty files.

	private String newFileName;		// The name for new empty text files.

	private SearchToolBar searchBar;

	private boolean showHostName;

	/**
	 * Whether <code>searchWindowOpacityListener</code> has been attempted to be
	 * created yet.
	 */
	private boolean windowListenersInited;

	/**
	 * Listens for focus events of certain child windows (those that can
	 * be made translucent on focus lost).
	 */
	private ChildWindowListener searchWindowOpacityListener;

	/**
	 * Whether the Find and Replace dialogs can have their opacity changed.
	 */
	private boolean searchWindowOpacityEnabled;

	/**
	 * The opacity with which to render unfocused child windows that support
	 * opacity changes.
	 */
	private float searchWindowOpacity;

	/**
	 * The rule used for making certain unfocused child windows translucent.
	 */
	private int searchWindowOpacityRule;

	/**
	 * The (lazily created) name of localhost.  Do not access this field
	 * directly; instead, use {@link #getHostName()}.
	 */
	private String hostName;

	private RecentFileManager recentFileManager;

	/**
	 * Used as a "hack" to re-load the Options dialog if the user opens it
	 * too early, before all plugins have added their options to it.
	 */
	private int lastPluginCount;

	private static final String DEFAULT_ICON_GROUP_NAME = "IntelliJ Icons (Dark)";

	/**
	 * System property that, if set, causes RText to print timing information
	 * while it is starting up.
	 */
	private static final String PROPERTY_PRINT_START_TIMES = "printStartTimes";

	public static final String VERSION_STRING		= "4.0.1";


	/**
	 * Creates an instance of the <code>RText</code> editor.
	 *
	 * @param context The application context that started this instance.
	 * @param filesToOpen Array of <code>java.lang.String</code>s containing
	 *        the files we want to open initially.  This can be
	 *        <code>null</code> if no files are to be opened.
	 * @param preferences The preferences with which to initialize this RText.
	 */
	public RText(RTextAppContext context, String[] filesToOpen, RTextPrefs preferences) {
		super(context, "rtext", preferences);
		init(filesToOpen);
	}


	// What to do when user does something.
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		switch (command) {
			case "TileVertically" -> ((RTextMDIView)mainView).tileWindowsVertically();
			case "TileHorizontally" -> ((RTextMDIView)mainView).tileWindowsHorizontally();
			case "Cascade" -> ((RTextMDIView)mainView).cascadeWindows();
		}

	}


	// TODO
	public void addDockableWindow(DockableWindow wind) {
		((DockableWindowPanel)mainContentPanel).addDockableWindow(wind);
	}


	/**
	 * Returns whether or not tabs are emulated with spaces (i.e. "soft" tabs).
	 * This simply calls <code>mainView.areTabsEmulated</code>.
	 *
	 * @return <code>true</code> if tabs are emulated with spaces;
	 *         <code>false</code> if they aren't.
	 */
	public boolean areTabsEmulated() {
		return mainView.areTabsEmulated();
	}


	/**
	 * Called when cursor in text editor changes position.
	 *
	 * @param e The caret event.
	 */
	@Override
	public void caretUpdate(CaretEvent e) {

		// NOTE: e may be "null"; we do this sometimes to force caret
		// updates to update e.g. the current line highlight.
		RTextEditorPane textArea = mainView.getCurrentTextArea();
		int dot = textArea.getCaretPosition();//e.getDot();

		// Update row/column information in status field.
		Element map = textArea.getDocument().getDefaultRootElement();
		int line = map.getElementIndex(dot);
		int lineStartOffset = map.getElement(line).getStartOffset();
		((StatusBar)getStatusBar()).setRowAndColumn(
									line+1, dot-lineStartOffset+1);

	}


	/**
	 * Converts all instances of a number of spaces equal to a tab in all open
	 * documents into tabs.
	 *
	 * @see #convertOpenFilesTabsToSpaces
	 */
	private void convertOpenFilesSpacesToTabs() {
		mainView.convertOpenFilesSpacesToTabs();
	}


	/**
	 * Converts all tabs in all open documents into an equivalent number of
	 * spaces.
	 *
	 * @see #convertOpenFilesSpacesToTabs
	 */
	private void convertOpenFilesTabsToSpaces() {
		mainView.convertOpenFilesTabsToSpaces();
	}


	/**
	 * Returns the About dialog for this application.
	 *
	 * @return The About dialog.
	 */
	@Override
	protected JDialog createAboutDialog() {
		return new AboutDialog(this);
	}


	/**
	 * Creates the array of actions used by this RText.
	 *
	 * @param prefs The RText properties for this RText instance.
	 */
	@Override
	protected void createActions(RTextPrefs prefs) {
		ActionFactory.addActions(this, prefs);
		loadActionShortcuts(getShortcutsFile());
	}


	/**
	 * Creates and returns the menu bar used in this application.
	 *
	 * @param prefs This GUI application's preferences.
	 * @return The menu bar.
	 */
	@Override
	protected JMenuBar createMenuBar(RTextPrefs prefs) {

		//splashScreen.updateStatus(msg.getString("CreatingMenuBar"), 75);

		menuBar = new RTextMenuBar(this, prefs);
		mainView.addPropertyChangeListener(menuBar);

		menuBar.setWindowMenuVisible(prefs.mainView==MDI_VIEW);

		return menuBar;

	}


	/**
	 * Creates a new instance of an RText window with properties identical
	 * to this one.
	 *
	 * @param filesToOpen The set of files to open.  Can be {@code null}.
	 * @return The instance.
	 */
	public RText createNewInstance(String[] filesToOpen) {
		RTextAppContext context = (RTextAppContext)getAppContext();
		return new RText(context, filesToOpen, getActivePreferencesState());
	}


	/**
	 * Returns the splash screen to display while this GUI application is
	 * loading.
	 *
	 * @return The splash screen.  If <code>null</code> is returned, no
	 *          splash screen is displayed.
	 */
	@Override
	protected SplashScreen createSplashScreen() {
		String img = "org/fife/rtext/graphics/" + getString("Splash");
		return new SplashScreen(img, getString("Initializing"));
	}


	/**
	 * Returns the status bar to be used by this application.
	 *
	 * @param prefs This GUI application's preferences.
	 * @return The status bar.
	 */
	@Override
	protected org.fife.ui.StatusBar createStatusBar(RTextPrefs prefs) {
		StatusBar sb = new StatusBar(this, getString("Ready"),
					!prefs.wordWrap, 1,1,
					prefs.textMode==RTextEditorPane.OVERWRITE_MODE);
		sb.setStyle(prefs.statusBarStyle);
		return sb;
	}


	/**
	 * Creates and returns the toolbar to be used by this application.
	 *
	 * @param prefs This GUI application's preferences.
	 * @return The toolbar.
	 */
	@Override
	protected CustomizableToolBar createToolBar(RTextPrefs prefs) {

		ToolBar toolBar = new ToolBar("rtext - Toolbar", this,
								(StatusBar)getStatusBar());

		// Make the toolbar use the large versions of the icons if available.
		// FIXME:  Make toggle-able.
		toolBar.checkForLargeIcons();

		return toolBar;

	}


	/**
	 * Overridden so we can syntax highlight the Java exception displayed.
	 *
	 * @param owner The dialog that threw the Exception.
	 * @param t The exception/throwable that occurred.
	 * @param desc A short description of the error.  This can be
	 *        <code>null</code>.
	 */
	@Override
	public void displayException(Dialog owner, Throwable t, String desc) {
		ExceptionDialog ed = new ExceptionDialog(owner, t);
		if (desc!=null) {
			ed.setDescription(desc);
		}
		ed.setLocationRelativeTo(owner);
		ed.setTitle(getString("ErrorDialogTitle"));
		ed.setVisible(true);
	}


	/**
	 * Overridden so we can syntax highlight the Java exception displayed.
	 *
	 * @param owner The child frame that threw the Exception.
	 * @param t The exception/throwable that occurred.
	 * @param desc A short description of the error.  This can be
	 *        <code>null</code>.
	 */
	@Override
	public void displayException(Frame owner, Throwable t, String desc) {
		ExceptionDialog ed = new ExceptionDialog(owner, t);
		if (desc!=null) {
			ed.setDescription(desc);
		}
		ed.setLocationRelativeTo(owner);
		ed.setTitle(getString("ErrorDialogTitle"));
		ed.setVisible(true);
	}


	/**
	 * Called when the user attempts to close the application, whether from
	 * an "Exit" menu item, closing the main application window, or any other
	 * means.  The user is prompted to save any dirty documents, and this
	 * RText instance is closed.
	 */
	@Override
	public void doExit() {

		// Attempt to close all open documents.
		boolean allDocumentsClosed = getMainView().closeAllDocuments();

		// Assuming all documents closed okay (ie, the user
		// didn't click "Cancel")...
		if (allDocumentsClosed) {

			// If there will be no more rtext's running, stop the JVM.
			if (StoreKeeper.getInstanceCount()==1) {
				savePreferences();
				boolean saved = RTextEditorPane.saveTemplates();
				if (!saved) {
					String title = getString("ErrorDialogTitle");
					String text = getString("TemplateSaveError");
					JOptionPane.showMessageDialog(this, text, title,
										JOptionPane.ERROR_MESSAGE);
				}
				// Save file chooser "Favorite Directories".  It is
				// important to check that the chooser exists here, as
				// if it doesn't, there's no need to do this!  If we
				// don't, the saveFileChooseFavorites() method will
				// create the file chooser itself just to save the
				// favorites!
				if (chooser!=null) {
					RTextUtilities.saveFileChooserFavorites(this);
				}
				System.exit(0);
			}

			// If there will still be some RText instances running, just
			// stop this instance.
			else {
				setVisible(false);
				StoreKeeper.removeRTextInstance(this);
				this.dispose();
			}

		}

	}


	/**
	 * Focuses the specified dockable window group.  Does nothing if there
	 * are no dockable windows at the location specified.
	 *
	 * @param group The dockable window group to focus.
	 */
	public void focusDockableWindowGroup(int group) {
		DockableWindowPanel dwp = (DockableWindowPanel)mainContentPanel;
		if (!dwp.focusDockableWindowGroup(group)) { // Should never happen
			UIManager.getLookAndFeel().provideErrorFeedback(this);
		}
	}


	/**
	 * Returns the filename used for newly created, empty text files.  This
	 * value is locale-specific.
	 *
	 * @return The new text file name.
	 */
	public String getNewFileName() {
		return newFileName;
	}


	@Override
	public OptionsDialog getOptionsDialog() {

		int pluginCount = getPlugins().length;

		// Check plugin count and re-create dialog if it has changed.  This
		// is because the user can be quick and open the Options dialog before
		// all plugins have loaded.  A real solution is to have some sort of
		// options manager that plugins can add options panels to.
		if (optionsDialog==null || pluginCount!=lastPluginCount) {
			optionsDialog = new org.fife.rtext.optionsdialog.
												OptionsDialog(this);
			optionsDialog.setLocationRelativeTo(this);
		}

		return optionsDialog;

	}


	/**
	 * Returns the application's "collapsible section panel;" that is, the
	 * panel containing the main view and possible find/replace tool bars.
	 *
	 * @return The collapsible section panel.
	 * @see #getMainView()
	 */
	CollapsibleSectionPanel getCollapsibleSectionPanel() {
		return csp;
	}


	/**
	 * Returns the file chooser being used by this RText instance.
	 *
	 * @return The file chooser.
	 * @see #getRemoteFileChooser()
	 */
	@Override
	public RTextFileChooser getFileChooser() {
		if (chooser==null) {
			chooser = RTextUtilities.createFileChooser(this);
		}
		return chooser;
	}


	/**
	 * Returns the focused dockable window group.
	 *
	 * @return The focused window group, or <code>-1</code> if no dockable
	 *         window group is focused.
	 * @see DockableWindowConstants
	 */
	public int getFocusedDockableWindowGroup() {
		DockableWindowPanel dwp = (DockableWindowPanel)mainContentPanel;
		return dwp.getFocusedDockableWindowGroup();
	}


	/**
	 * Returns the Help dialog for RText.
	 *
	 * @return The Help dialog.
	 */
	@Override
	public HelpDialog getHelpDialog() {
		// Create the help dialog if it hasn't already been.
		if (helpDialog==null) {
			String contentsPath = getInstallLocation() + "/doc/";
			String helpPath = contentsPath + getLanguage() + "/";
			// If localized help does not exist, default to English.
			File test = new File(helpPath);
			if (!test.isDirectory())
				helpPath = contentsPath + "en/";
			helpDialog = new HelpDialog(this,
						contentsPath + "HelpDialogContents.xml",
						helpPath);
			helpDialog.setBackButtonIcon(getIconGroup().getIcon("back"));
			helpDialog.setForwardButtonIcon(getIconGroup().getIcon("forward"));
		}
		helpDialog.setLocationRelativeTo(this);
		return helpDialog;
	}


	/**
	 * Returns the name of the local host.  This is lazily discovered.
	 *
	 * @return The name of the local host.
	 */
	private synchronized String getHostName() {
		if (hostName==null) {
			try {
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException uhe) { // Should never happen
				hostName = "Unknown";
			}
		}
		return hostName;
	}


	/**
	 * Returns the actual main view.
	 *
	 * @return The main view.
	 * @see #getMainViewStyle()
	 * @see #setMainViewStyle(int)
	 */
	public AbstractMainView getMainView() {
		return mainView;
	}


	/**
	 * Returns the main view style.
	 *
	 * @return The main view style, one of {@link #TABBED_VIEW},
	 *         {@link #SPLIT_PANE_VIEW} or {@link #MDI_VIEW}.
	 * @see #setMainViewStyle(int)
	 * @see #getMainView()
	 */
	public int getMainViewStyle() {
		return mainViewStyle;
	}


	/**
	 * Returns the list of most recently opened files, least-recently opened
	 * first.
	 *
	 * @return The list of files.  This may be empty but will never be
	 *         <code>null</code>.
	 */
	java.util.List<FileLocation> getRecentFiles() {
		return recentFileManager.getRecentFiles();
	}


	/**
	 * Returns the file chooser used to select remote files.
	 *
	 * @return The file chooser.
	 * @see #getFileChooser()
	 */
	public RemoteFileChooser getRemoteFileChooser() {
		if (rfc==null) {
			rfc = new RemoteFileChooser(this);
		}
		return rfc;
	}


	/**
	 * Returns the fully-qualified class name of the resource bundle for this
	 * application.  This is used by {@link #getResourceBundle()} to locate
	 * the class.
	 *
	 * @return The fully-qualified class name of the resource bundle.
	 * @see #getResourceBundle()
	 */
	@Override
	public String getResourceBundleClassName() {
		return "org.fife.rtext.RText";
	}


	/**
	 * Returns the QuickSearch toolbar.
	 *
	 * @return The QuickSearch toolbar.
	 * @see #isSearchToolBarVisible
	 */
	public SearchToolBar getSearchToolBar() {
		if (searchBar==null) {
			searchBar = new SearchToolBar("Search", this,
						(org.fife.rtext.StatusBar)getStatusBar());
			searchBar.setVisible(false);
			addToolBar(searchBar, BorderLayout.SOUTH);
		}
		return searchBar;
	}


	/**
	 * Returns the opacity with which to render unfocused child windows, if
	 * this option is enabled.
	 *
	 * @return The opacity.
	 * @see #setSearchWindowOpacity(float)
	 */
	public float getSearchWindowOpacity() {
		return searchWindowOpacity;
	}


	/**
	 * Returns the rule used for making certain child windows translucent.
	 *
	 * @return The rule.
	 * @see #setSearchWindowOpacityRule(int)
	 * @see #getSearchWindowOpacity()
	 */
	public int getSearchWindowOpacityRule() {
		return searchWindowOpacityRule;
	}


	/**
	 * Returns the file in which to load and save user-customized keyboard
	 * shortcuts.
	 *
	 * @return The shortcuts file.
	 */
	private static File getShortcutsFile() {
		return new File(RTextUtilities.getPreferencesDirectory(),
				"shortcuts.properties");
	}


	/**
	 * Returns whether the hostname should be shown in the title of the
	 * main RText window.
	 *
	 * @return Whether the hostname should be shown.
	 * @see #setShowHostName(boolean)
	 */
	public boolean getShowHostName() {
		return showHostName;
	}


	/**
	 * Returns the syntax highlighting color scheme being used.
	 *
	 * @return The syntax highlighting color scheme.
	 * @see #setSyntaxScheme(SyntaxScheme)
	 */
	public SyntaxScheme getSyntaxScheme() {
		return colorScheme;
	}


	/**
	 * Returns the tab size (in spaces) currently being used.
	 *
	 * @return The tab size (in spaces) currently being used.
	 * @see #setTabSize(int)
	 */
	private int getTabSize() {
		return mainView.getTabSize();
	}


	/**
	 * Returns the title of this window, less any "header" information
	 * (e.g. without the leading "<code>rtext - </code>").
	 *
	 * @return The title of this window.
	 * @see #setTitle(String)
	 */
	@Override
	public String getTitle() {
		String title = super.getTitle();
		int hyphen = title.indexOf("- ");
		if (hyphen>-1) { // Should always be true
			title = title.substring(hyphen+2);
		}
		return title;
	}


	/**
	 * Returns the version string for this application.
	 *
	 * @return The version string.
	 */
	@Override
	public String getVersionString() {
		return VERSION_STRING;
	}


	/**
	 * Returns the "working directory;" that is, the directory that new, empty
	 * files are created in.
	 *
	 * @return The working directory.  There will be no trailing '/' or '\'.
	 * @see #setWorkingDirectory
	 */
	public String getWorkingDirectory() {
		return workingDirectory;
	}


	/**
	 * Does the dirty work of actually installing a plugin.  This method
	 * ensures the current text area retains focus even after a GUI plugin
	 * is added.
	 *
	 * @param plugin The plugin to install.
	 */
	@Override
	protected void handleInstallPlugin(Plugin plugin) {
		// Normally we don't have to check currentTextArea for null, but in
		// this case, we do.  Plugins are installed at startup, after the main
		// window is displayed.  If the user passes in a filename to open, but
		// that file doesn't exist, RText will prompt with "File XXX does not
		// exist, create it?", and in that time, currentTextArea will be null.
		// Plugins, in the meantime, will try to load and find the null value.
		RTextEditorPane textArea = getMainView().getCurrentTextArea();
		if (textArea!=null) {
			textArea.requestFocusInWindow();
		}
	}


	/**
	 * Returns whether dockable windows are at the specified location.
	 *
	 * @param group A constant from {@link DockableWindowConstants}
	 * @return Whether dockable windows are at the specified location.
	 */
	public boolean hasDockableWindowGroup(int group) {
		DockableWindowPanel dwp = (DockableWindowPanel)mainContentPanel;
		return dwp.hasDockableWindowGroup(group);
	}


	/**
	 * Called at the end of RText constructors.  Does common initialization
	 * for RText.
	 *
	 * @param filesToOpen Any files to open.  This can be <code>null</code>.
	 */
	private void init(String[] filesToOpen) {
		lastPluginCount = -1;
		openFiles(filesToOpen);
	}


	/**
	 * Initializes the "recent files" manager.
	 *
	 * @param prefs The preferences for the application.
	 */
	private void initRecentFileManager(RTextPrefs prefs) {

		String fileHistoryStr = prefs.fileHistoryString;
		java.util.List<String> recentFiles = new ArrayList<>();
		if (fileHistoryStr != null && fileHistoryStr.length() > 0) {
			String[] initialContents = fileHistoryStr.split("<");
			recentFiles.addAll(Arrays.asList(initialContents));
		}

		recentFileManager = new RecentFileManager(this, recentFiles);
	}


	/**
	 * Installs all properties in an RSTA <code>Theme</code> instance properly.
	 *
	 * @param theme The theme instance.
	 */
	private void installRstaTheme(Theme theme) {

		setSyntaxScheme(theme.scheme);

		if (mainView == null) {
			return;
		}

		//themeObj.activeLineRangeColor;
		mainView.setBackgroundObject(theme.bgColor);
		mainView.setCaretColor(theme.caretColor);
		mainView.setCurrentLineHighlightColor(theme.currentLineHighlight);
		//themeObj.fadeCurrentLineHighlight
		//themeObj.foldBG
		mainView.setGutterBorderColor(theme.gutterBorderColor);
		mainView.setHyperlinkColor(theme.hyperlinkFG);
		//themeObj.iconRowHeaderInheritsGutterBG
		mainView.setLineNumberColor(theme.lineNumberColor);
		if (theme.lineNumberFont != null) {
			int fontSize = theme.lineNumberFontSize > 0 ? theme.lineNumberFontSize : 11;
			mainView.setLineNumberFont(new Font(theme.lineNumberFont, Font.PLAIN, fontSize));
		}
		mainView.setMarginLineColor(theme.marginLineColor);
		mainView.setMarkAllHighlightColor(theme.markAllHighlightColor);
		//themeObj.markOccurrencesBorder;
		mainView.setMarkOccurrencesColor(theme.markOccurrencesColor);
		//themeObj.matchedBracketAnimate;
		if (theme.matchedBracketBG != null) {
			mainView.setMatchedBracketBorderColor(theme.matchedBracketFG);
		}
		mainView.setMatchedBracketBGColor(theme.matchedBracketBG);
		if (theme.secondaryLanguages != null) {
			for (int i = 0; i < theme.secondaryLanguages.length; i++) {
				mainView.setSecondaryLanguageColor(i, theme.secondaryLanguages[i]);
			}
		}
		mainView.setSelectionColor(theme.selectionBG);
		if (theme.selectionFG != null) {
			mainView.setSelectedTextColor(theme.selectionFG);
		}
		mainView.setUseSelectedTextColor(theme.useSelectionFG);
		mainView.setRoundedSelectionEdges(theme.selectionRoundedEdges);

		mainView.setFoldBackground(theme.foldBG);
		mainView.setArmedFoldBackground(theme.armedFoldBG);

	}


	/**
	 * Returns whether or not the QuickSearch toolbar is visible.  This
	 * method should be used over <code>getSearchToolBar().isVisible()</code>
	 * because the latter will allocate the toolbar if it isn't already
	 * created, but this method won't.
	 *
	 * @return Whether or not the QuickSearch toolbar is visible.
	 * @see #getSearchToolBar
	 */
	public boolean isSearchToolBarVisible() {
		return searchBar != null && searchBar.isVisible();
	}


	/**
	 * Returns whether search window opacity is enabled.
	 *
	 * @return Whether search window opacity is enabled.
	 * @see #setSearchWindowOpacityEnabled(boolean)
	 */
	public boolean isSearchWindowOpacityEnabled() {
		return searchWindowOpacityEnabled;
	}


	/**
	 * Returns whether the spelling window is visible.
	 *
	 * @return Whether the spelling window is visible.
	 * @see #setSpellingWindowVisible(boolean)
	 */
	public boolean isSpellingWindowVisible() {
		return spellingWindow!=null && spellingWindow.isActive();
	}


	/**
	 * Loads and validates the icon groups available to RText.
	 */
	private void loadPossibleIconGroups() {

		iconGroupMap = new HashMap<>();

		String root = getInstallLocation();
		iconGroupMap.put(DEFAULT_ICON_GROUP_NAME,
			new SvgIconGroup(this, DEFAULT_ICON_GROUP_NAME, "icongroups/intellij-icons-dark.jar"));
		iconGroupMap.put("IntelliJ Icons (Light)",
			new SvgIconGroup(this, "IntelliJ Icons (Light)", "icongroups/intellij-icons-light.jar"));
		iconGroupMap.put("Eclipse Icons", new RasterImageIconGroup("Eclipse Icons",
			"", null, "gif",
			new File(root, "icongroups/EclipseIcons.jar").getAbsolutePath()));

	}


	/**
	 * Opens the specified files.
	 *
	 * @param filesToOpen The files to open.  This can be <code>null</code>.
	 * @see #openFile
	 */
	private void openFiles(String[] filesToOpen) {
		int count = filesToOpen==null ? 0 : filesToOpen.length;
		for (int i=0; i<count; i++) {
			openFile(new File(filesToOpen[i]));
		}
	}


	@Override
	protected void preDisplayInit(RTextPrefs prefs, SplashScreen splashScreen) {

		long start = System.currentTimeMillis();

		// Some stuff down the line may assume this directory exists!
		File prefsDir = RTextUtilities.getPreferencesDirectory();
		if (!prefsDir.isDirectory()) {
			prefsDir.mkdirs();
		}

		// Install any plugins.
		super.preDisplayInit(prefs, splashScreen);

		if (prefs.searchToolBarVisible) {
			addToolBar(getSearchToolBar(), BorderLayout.SOUTH);
			searchBar.setVisible(true);
		}

		splashScreen.updateStatus(getString("AddingFinalTouches"), 90);

		// If the user clicks the "X" in the top-right of the window, do nothing.
		// (We'll clean up in our window listener).
		addWindowListener(new RTextWindowListener(this));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		mainView.setLineNumbersEnabled(prefs.lineNumbersVisible);

		// Enable templates in text areas.
		if (RTextUtilities.enableTemplates(this, true)) {
			// If there are no templates, assume this is the user's first
			// time in RText and add some "standard" templates.
			CodeTemplateManager ctm = RTextEditorPane.getCodeTemplateManager();
			if (ctm.getTemplates().length==0) {
				RTextUtilities.addDefaultCodeTemplates();
			}
		}

		setSearchWindowOpacityEnabled(prefs.searchWindowOpacityEnabled);
		setSearchWindowOpacity(prefs.searchWindowOpacity);
		setSearchWindowOpacityRule(prefs.searchWindowOpacityRule);

		if (Boolean.getBoolean(PROPERTY_PRINT_START_TIMES)) {
			System.err.println("preDisplayInit: " + (System.currentTimeMillis()-start));
		}

		RTextUtilities.setDropShadowsEnabledInEditor(prefs.dropShadowsInEditor);

		setWindowDraggableByMenuBarAndToolBar();
	}


	@Override
	protected void preMenuBarInit(RTextPrefs prefs, SplashScreen splashScreen) {

		long start = System.currentTimeMillis();

		initRecentFileManager(prefs);

		// Make the split pane positions same as last time.
		setSplitPaneDividerLocation(DockableWindowConstants.TOP,
			prefs.dividerLocations[DockableWindowConstants.TOP],
			prefs.dividerVisible[DockableWindowConstants.TOP]);
		setSplitPaneDividerLocation(DockableWindowConstants.LEFT,
			prefs.dividerLocations[DockableWindowConstants.LEFT],
			prefs.dividerVisible[DockableWindowConstants.LEFT]);
		setSplitPaneDividerLocation(DockableWindowConstants.BOTTOM,
			prefs.dividerLocations[DockableWindowConstants.BOTTOM],
			prefs.dividerVisible[DockableWindowConstants.BOTTOM]);
		setSplitPaneDividerLocation(DockableWindowConstants.RIGHT,
			prefs.dividerLocations[DockableWindowConstants.RIGHT],
			prefs.dividerVisible[DockableWindowConstants.RIGHT]);

		// Show any docked windows
		setSpellingWindowVisible(prefs.viewSpellingList);

		setShowHostName(prefs.showHostName);

		if (Boolean.getBoolean(PROPERTY_PRINT_START_TIMES)) {
			System.err.println("preMenuBarInit: " + (System.currentTimeMillis()-start));
		}

	}


	@Override
	protected void preStatusBarInit(RTextPrefs prefs,
							SplashScreen splashScreen) {

		long start = System.currentTimeMillis();

		final String[] filesToOpen = null;

		// Initialize our "new, empty text file" name.
		newFileName = getString("NewFileName");

		splashScreen.updateStatus(getString("SettingSHColors"), 10);
		setSyntaxScheme(prefs.colorScheme);

		setWorkingDirectory(prefs.workingDirectory);

		splashScreen.updateStatus(getString("CreatingView"), 20);

		loadPossibleIconGroups();
		setIconGroupByName((String)getTheme().getExtraUiDefaults().get("rtext.iconGroupName"));

		// Initialize our view object.
		switch (prefs.mainView) {
			case TABBED_VIEW:
				mainViewStyle = TABBED_VIEW;
				mainView = new RTextTabbedPaneView(RText.this, filesToOpen, prefs);
				break;
			case SPLIT_PANE_VIEW:
				mainViewStyle = SPLIT_PANE_VIEW;
				mainView = new RTextSplitPaneView(RText.this, filesToOpen, prefs);
				break;
			default:
				mainViewStyle = MDI_VIEW;
				mainView = new RTextMDIView(RText.this, filesToOpen, prefs);
				break;
		}

		// Change all RTextAreas' open documents' icon sets.  This must be done
		// after the main view is instantiated.
		RTextEditorPane.setIconGroup(RTextUtilities.toRstaIconGroup(getIconGroup()));

		csp = new CollapsibleSectionPanel(false);
		csp.add(mainView);
		getContentPane().add(csp);

		splashScreen.updateStatus(getString("CreatingStatusBar"), 25);

		if (Boolean.getBoolean(PROPERTY_PRINT_START_TIMES)) {
			System.err.println("preStatusBarInit: " + (System.currentTimeMillis()-start));
		}

	}


	@Override
	protected void preToolBarInit(RTextPrefs prefs, SplashScreen splashScreen) {

		long start = System.currentTimeMillis();

		StatusBar statusBar = (StatusBar)getStatusBar();
		mainView.addPropertyChangeListener(statusBar);

		splashScreen.updateStatus(getString("CreatingToolBar"), 60);
		if (Boolean.getBoolean(PROPERTY_PRINT_START_TIMES)) {
			System.err.println("preToolbarInit: " + (System.currentTimeMillis()-start));
		}

	}


	/**
	 * Called whenever a property changes for a component we are registered
	 * as listening to.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {

		String propertyName = e.getPropertyName();

		// If the file's path is changing (must be caused by the file being saved(?))...
		if (propertyName.equals(RTextEditorPane.FULL_PATH_PROPERTY)) {
			setTitle((String)e.getNewValue());
		}

		// If the file's modification status is changing...
		else if (propertyName.equals(RTextEditorPane.DIRTY_PROPERTY)) {
			String oldTitle = getTitle();
			boolean newValue = (Boolean)e.getNewValue();
			if (!newValue) {
				setTitle(oldTitle.substring(0,oldTitle.length()-1));
			}
			else {
				setTitle(oldTitle + '*');
			}
		}

	}


	void registerChildWindowListeners(Window w) {

		if (!windowListenersInited) {
			windowListenersInited = true;
			if (TranslucencyUtil.get().isTranslucencySupported(false)) {
				searchWindowOpacityListener = new ChildWindowListener(this);
				searchWindowOpacityListener.setTranslucencyRule(
												searchWindowOpacityRule);
			}
		}

		if (searchWindowOpacityListener!=null) {
			w.addWindowFocusListener(searchWindowOpacityListener);
			w.addComponentListener(searchWindowOpacityListener);
		}

	}


	// TODO
	void removeDockableWindow(DockableWindow wind) {
		((DockableWindowPanel)mainContentPanel).removeDockableWindow(wind);
	}


	/**
	 * Saves the user's preferences.
	 */
	public void savePreferences() {

		// Save preferences for RText itself.
		super.savePreferences();
		saveActionShortcuts(getShortcutsFile());

		// Save preferences for any plugins.
		Plugin[] plugins = getPlugins();
		for (Plugin plugin : plugins) {
			plugin.savePreferences();
		}

		// Save the file chooser's properties, if it has been instantiated.
		if (chooser!=null)
			chooser.savePreferences();

	}


	/**
	 * Changes the style of icons used by <code>rtext</code>.<p>
	 *
	 * This method fires a property change of type
	 * <code>ICON_STYLE_PROPERTY</code>.
	 *
	 * @param name The name of the icon group to use.  If this name is not
	 *        recognized, a default icon set will be used.
	 */
	private void setIconGroupByName(String name) {

		IconGroup newGroup = iconGroupMap.get(name);
		if (newGroup==null)
			newGroup = iconGroupMap.get(DEFAULT_ICON_GROUP_NAME);
		IconGroup iconGroup = getIconGroup();
		if (iconGroup!=null && iconGroup.equals(newGroup))
			return;

		setIconGroup(newGroup);
	}


	/**
	 * Sets the main view style.  This method fires a property change of type
	 * {@link #MAIN_VIEW_STYLE_PROPERTY}.
	 *
	 * @param viewStyle One of {@link #TABBED_VIEW}, {@link #SPLIT_PANE_VIEW}
	 *        or {@link #MDI_VIEW}.  If this value is invalid, nothing happens.
	 * @see #getMainViewStyle()
	 */
	public void setMainViewStyle(int viewStyle) {

		if ((viewStyle==TABBED_VIEW || viewStyle==SPLIT_PANE_VIEW ||
				viewStyle==MDI_VIEW) && viewStyle!=mainViewStyle) {

			int oldMainViewStyle = mainViewStyle;
			mainViewStyle = viewStyle;
			AbstractMainView fromView = mainView;

			RTextPrefs prefs = getActivePreferencesState();

			// Create the new view.
			switch (viewStyle) {
				case TABBED_VIEW:
					mainView = new RTextTabbedPaneView(this, null, prefs);
					menuBar.setWindowMenuVisible(false);
					break;
				case SPLIT_PANE_VIEW:
					mainView = new RTextSplitPaneView(this, null, prefs);
					menuBar.setWindowMenuVisible(false);
					break;
				case MDI_VIEW:
					mainView = new RTextMDIView(this, null, prefs);
					menuBar.setWindowMenuVisible(true);
					break;
			}

			// Update property change listeners.
			PropertyChangeListener[] propertyChangeListeners =
								fromView.getPropertyChangeListeners();
			for (PropertyChangeListener listener : propertyChangeListeners) {
				fromView.removePropertyChangeListener(listener);
				mainView.addPropertyChangeListener(listener);
			}

			// Keep find/replace dialogs working, if they've been created.
			// Make the new dialog listen to actions from the find/replace
			// dialogs.
			// NOTE:  The find and replace dialogs will be moved to mainView
			// in the copyData method below.
			mainView.getSearchManager().changeSearchListener(fromView);

			// Make mainView have all the properties of the old panel.
			mainView.copyData(fromView);

			// If we have switched to a tabbed view, artificially
			// fire stateChanged if the last document is selected,
			// because it isn't fired naturally if this is so.
			if ((mainView instanceof RTextTabbedPaneView) &&
				mainView.getSelectedIndex()==mainView.getNumDocuments()-1)
				((RTextTabbedPaneView)mainView).stateChanged(new ChangeEvent(mainView));


			// Physically replace the old main view with the new one.
			// NOTE: We need to remember previous size and restore it
			// because center collapses if changed to MDI otherwise.
			Dimension size = getSize();
			csp.remove(fromView);
			csp.add(mainView);
			fromView.dispose();
			//contentPane.add(mainView, BorderLayout.CENTER);
			pack();
			setSize(size);

			// For some reason we have to reselect the currently-selected
			// window to have it actually active in an MDI view.
			if (mainView instanceof RTextMDIView)
				mainView.setSelectedIndex(mainView.getSelectedIndex());


			firePropertyChange(MAIN_VIEW_STYLE_PROPERTY, oldMainViewStyle,
												mainViewStyle);

		} // End of if ((viewStyle==TABBED_VIEW || ...

	}


	/**
	 * This method changes both the active file name in the title bar, and the
	 * status message in the status bar.
	 *
	 * @param fileFullPath Full path to the text file currently being edited
	 *        (to be displayed in the window's title bar).  If
	 *        <code>null</code>, the currently displayed message is not
	 *        changed.
	 * @param statusMessage The message to be displayed in the status bar.
	 *        If <code>null</code>, the status bar message is not changed.
	 */
	public void setMessages(String fileFullPath, String statusMessage) {
		if (fileFullPath != null)
			setTitle(fileFullPath);
		StatusBar statusBar = (StatusBar)getStatusBar();
		if (statusBar!=null && statusMessage != null)
			statusBar.setStatusMessage(statusMessage);
	}


	/**
	 * Enables or disables the row/column indicator in the status bar.
	 *
	 * @param isVisible Whether or not the row/column indicator should be
	 *        visible.
	 */
	public void setRowColumnIndicatorVisible(boolean isVisible) {
		((StatusBar)getStatusBar()).setRowColumnIndicatorVisible(isVisible);
	}


	/**
	 * Sets whether the hostname should be shown in the title of the main
	 * RText window.
	 *
	 * @param show Whether the hostname should be shown.
	 * @see #getShowHostName()
	 */
	public void setShowHostName(boolean show) {
		if (this.showHostName!=show) {
			this.showHostName = show;
			setTitle(getTitle()); // Cause title to refresh.
		}
	}


	/**
	 * Sets whether the read-only indicator in the status bar is enabled.
	 *
	 * @param enabled Whether or not the read-only indicator is enabled.
	 */
	public void setStatusBarReadOnlyIndicatorEnabled(boolean enabled) {
		((StatusBar)getStatusBar()).setReadOnlyIndicatorEnabled(enabled);
	}


	/**
	 * Sets the syntax highlighting color scheme being used.
	 *
	 * @param colorScheme The new color scheme to use.  If
	 *        <code>null</code>, nothing changes.
	 * @see #getSyntaxScheme()
	 */
	public void setSyntaxScheme(SyntaxScheme colorScheme) {
		if (colorScheme!=null && !colorScheme.equals(this.colorScheme)) {
			// Make a deep copy for our copy.  We must be careful to do this
			// and pass our newly-created deep copy to mainView so that we
			// do not end up with the same copy passed to us (which could be
			// in the process of being edited in an options dialog).
			this.colorScheme = (SyntaxScheme)colorScheme.clone();
			if (mainView!=null)
				mainView.setSyntaxScheme(this.colorScheme);
		}
	}


	/**
	 * Changes whether or not tabs should be emulated with spaces
	 * (i.e., soft tabs).
	 * This simply calls <code>mainView.setTabsEmulated</code>.
	 *
	 * @param areEmulated Whether or not tabs should be emulated with spaces.
	 */
	public void setTabsEmulated(boolean areEmulated) {
		mainView.setTabsEmulated(areEmulated);
	}


	/**
	 * Sets the tab size to be used on all documents.
	 *
	 * @param newSize The tab size to use.
	 * @see #getTabSize()
	 */
	private void setTabSize(int newSize) {
		mainView.setTabSize(newSize);
	}


	/**
	 * Sets the title of the application window.  This title is prefixed
	 * with the application name.
	 *
	 * @param title The new title.
	 * @see #getTitle()
	 */
	@Override
	public void setTitle(String title) {
		if (getShowHostName()) {
			title = "rtext (" + getHostName() + ") - " + title;
		}
		else {
			title = "rtext - " + title;
		}
		super.setTitle(title);
	}


	/**
	 * Sets the opacity with which to render unfocused child windows, if this
	 * option is enabled.
	 *
	 * @param opacity The opacity.  This should be between <code>0</code> and
	 *        <code>1</code>.
	 * @see #getSearchWindowOpacity()
	 * @see #setSearchWindowOpacityRule(int)
	 */
	public void setSearchWindowOpacity(float opacity) {
		searchWindowOpacity = Math.max(0, Math.min(opacity, 1));
		if (windowListenersInited && isSearchWindowOpacityEnabled()) {
			searchWindowOpacityListener.refreshTranslucencies();
		}
	}


	/**
	 * Toggles whether search window opacity is enabled.
	 *
	 * @param enabled Whether search window opacity should be enabled.
	 * @see #isSearchWindowOpacityEnabled()
	 */
	public void setSearchWindowOpacityEnabled(boolean enabled) {
		if (enabled!=searchWindowOpacityEnabled) {
			searchWindowOpacityEnabled = enabled;
			// Toggled either on or off
			// Must check searchWindowOpacityListener since in pre 6u10,
			// we'll be inited, but listener isn't created.
			if (windowListenersInited &&
					searchWindowOpacityListener!=null) {
				searchWindowOpacityListener.refreshTranslucencies();
			}
		}
	}


	/**
	 * Toggles whether certain child windows should be made translucent.
	 *
	 * @param rule The new opacity rule.
	 * @see #getSearchWindowOpacityRule()
	 * @see #setSearchWindowOpacity(float)
	 */
	public void setSearchWindowOpacityRule(int rule) {
		if (rule!=searchWindowOpacityRule) {
			searchWindowOpacityRule = rule;
			if (windowListenersInited) {
				searchWindowOpacityListener.setTranslucencyRule(rule);
			}
		}
	}


	/**
	 * Toggles whether the spelling error window is visible.
	 *
	 * @param visible Whether the spelling error window is visible.
	 * @see #isSpellingWindowVisible()
	 */
	public void setSpellingWindowVisible(boolean visible) {
		if (visible) {
			if (spellingWindow==null) {
				spellingWindow = new SpellingErrorWindow(this);
				DockableWindowPanel dwp = (DockableWindowPanel)mainContentPanel;
				dwp.addDockableWindow(spellingWindow);
			}
			else {
				spellingWindow.setActive(true);
			}
		}
		else {
			if (spellingWindow!=null) {
				spellingWindow.setActive(false);
			}
		}
	}


	@Override
	protected void setThemeAdditionalProperties(AppTheme theme) {

		if (iconGroupMap != null) {
			setIconGroupByName((String)theme.getExtraUiDefaults().get("rtext.iconGroupName"));
		}

		String editorTheme = (String)theme.getExtraUiDefaults().get("rtext.editorTheme");
		if (editorTheme != null) {
			Theme themeObj;
			try {
				themeObj = Theme.load(getClass().getResourceAsStream(editorTheme));
				installRstaTheme(themeObj);
			} catch (Exception ioe) {
				displayException(ioe);
				return;
			}
		}

		if (mainView != null) {
			Color labelErrorForeground = (Color)theme.getExtraUiDefaults().get("rtext.labelErrorForeground");
			mainView.setHighlightModifiedDocumentDisplayNames(labelErrorForeground != null);
			mainView.setModifiedDocumentDisplayNamesColor(labelErrorForeground);
		}
	}


	/**
	 * Sets the "working directory;" that is, the directory in which
	 * new, empty files are placed.
	 *
	 * @param directory The new working directory.  If this directory does
	 *        not exist, the Java property "user.dir" is used.
	 * @see #getWorkingDirectory
	 */
	public void setWorkingDirectory(String directory) {
		File test = new File(directory);
		if (test.isDirectory())
			workingDirectory = directory;
		else
			workingDirectory = System.getProperty("user.dir");
	}


	@Override
	protected void updateIconsForNewIconGroup(IconGroup iconGroup) {

		super.updateIconsForNewIconGroup(iconGroup);

		Dimension size = getSize();

		// Text area icons
		updateTextAreaIcon(RTextArea.CUT_ACTION, "cut");
		updateTextAreaIcon(RTextArea.COPY_ACTION, "copy");
		updateTextAreaIcon(RTextArea.PASTE_ACTION, "paste");
		updateTextAreaIcon(RTextArea.DELETE_ACTION, "delete");
		updateTextAreaIcon(RTextArea.UNDO_ACTION, "undo");
		updateTextAreaIcon(RTextArea.REDO_ACTION, "redo");
		updateTextAreaIcon(RTextArea.SELECT_ALL_ACTION, "selectall");

		// All other icons
		Icon icon = iconGroup.getIcon("new");
		getAction(NEW_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("open");
		getAction(OPEN_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("save");
		getAction(SAVE_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("saveall");
		getAction(SAVE_ALL_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("openinnewwindow");
		getAction(OPEN_NEWWIN_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("saveas");
		getAction(SAVE_AS_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("options");
		getAction(OPTIONS_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("help");
		getAction(HELP_ACTION_KEY).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("about");
		getAction(ABOUT_ACTION_KEY).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("close");
		getAction(CLOSE_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("find");
		getAction(FIND_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("findnext");
		getAction(FIND_NEXT_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("replace");
		getAction(REPLACE_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("replacenext");
		getAction(REPLACE_NEXT_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("print");
		getAction(PRINT_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("printpreview");
		getAction(PRINT_PREVIEW_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("closeall");
		getAction(CLOSE_ALL_ACTION).putValue(Action.SMALL_ICON, icon);

		Icon gotoIcon = iconGroup.getIcon("goto");
		if (gotoIcon == null) {
			gotoIcon = ActionFactory.getDefaultGoToActionIcon();
		}
		getAction(GOTO_ACTION).putValue(Action.SMALL_ICON, gotoIcon);

		// The toolbar uses the large versions of the icons, if available.
		// FIXME:  Make this toggle-able.
		ToolBar toolBar = (ToolBar)getToolBar();
		if (toolBar!=null)
			toolBar.checkForLargeIcons();

		// Do this because the toolbar has changed it's size.
		if (isDisplayable()) {
			pack();
			setSize(size);
		}

		// Make the help dialog use appropriate "back" and "forward" icons.
		if (helpDialog!=null) {
			helpDialog.setBackButtonIcon(iconGroup.getIcon("back"));
			helpDialog.setForwardButtonIcon(iconGroup.getIcon("forward"));
		}
	}


	@Override
	public void updateLookAndFeel(LookAndFeel lnf) {

		super.updateLookAndFeel(lnf);

		try {

			Dimension size = this.getSize();

			// Update all components in this frame.
			SwingUtilities.updateComponentTreeUI(this);
			this.pack();
			this.setSize(size);

			// So mainView knows to update it's popup menus, etc.
			if (mainView != null) {
				mainView.updateLookAndFeel();
			}

			// Update any dialogs.
			if (optionsDialog != null) {
				SwingUtilities.updateComponentTreeUI(optionsDialog);
				optionsDialog.pack();
			}
			if (helpDialog != null) {
				SwingUtilities.updateComponentTreeUI(helpDialog);
				helpDialog.pack();
			}

			if (chooser!=null) {
				SwingUtilities.updateComponentTreeUI(chooser);
				chooser.updateUI(); // So the popup menu gets updated.
	 		}
			if (rfc!=null) {
				SwingUtilities.updateComponentTreeUI(rfc);
				rfc.updateUI(); // Not JDialog API; specific to this class
			}

		} catch (Exception f) {
			displayException(f);
		}

	}


	@Override
	public void preferences() {
		getAction(OPTIONS_ACTION).actionPerformed(new ActionEvent(this,0,"unused"));
	}

	@Override
	public void openFile(File file) {
		//gets called when we receive an open event from the finder on OS X
		SwingUtilities.invokeLater(() -> {
			// null encoding means check for Unicode before using
			// system default encoding.
			mainView.openFile(file.getAbsolutePath(), null, true);
		});
	}

	private void updateTextAreaIcon(int actionName, String iconName) {

		Icon icon = getIconGroup().getIcon(iconName);

		Action action = RTextArea.getAction(actionName);
		if (action != null) { // Can be null when the app is first starting up
			action.putValue(Action.SMALL_ICON, icon);
		}
	}


}
