/*
 * 11/14/2003
 *
 * RText.java - A syntax highlighting programmer's text editor written in Java.
 * Copyright (C) 2003 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This file is a part of RText.
 *
 * RText is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.rtext;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.Element;

import org.fife.help.HelpDialog;
import org.fife.ui.CustomizableToolBar;
import org.fife.ui.OptionsDialog;
import org.fife.ui.SplashScreen;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.ExtendedLookAndFeelInfo;
import org.fife.ui.app.GUIApplicationPreferences;
import org.fife.ui.app.Plugin;
import org.fife.ui.app.StandardAction;
import org.fife.ui.app.ThirdPartyLookAndFeelManager;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowPanel;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rtextarea.IconGroup;
import org.fife.ui.rtextarea.RTextAreaEditorKit;
import org.fife.ui.rtextfilechooser.FileChooserOwner;
import org.fife.ui.rtextfilechooser.RTextFileChooser;


/**
 * An instance of the RText text editor.  <code>RText</code> is a programmer's
 * text editor with the following features:
 *
 * <ul>
 *   <li>Syntax highlighting for 15+ languages.
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
 * @version 0.9.9.9
 */
public class RText extends AbstractPluggableGUIApplication
				implements ActionListener, CaretListener,
					KeyListener, PropertyChangeListener, RTextActionInfo,
					FileChooserOwner {

	// Constants specifying the current view style.
	public static final int TABBED_VIEW				= 0;
	public static final int SPLIT_PANE_VIEW				= 1;
	public static final int MDI_VIEW					= 2;

	// Properties fired.
	public static final String ICON_STYLE_PROPERTY		= "RText.iconStyle";
	public static final String MAIN_VIEW_STYLE_PROPERTY	= "RText.mainViewStyle";

	private Map iconGroupMap;

	private RTextMenuBar menuBar;

	public OptionsDialog optionsDialog;

	private AbstractMainView mainView;	// Component showing all open documents.
	private int mainViewStyle;

	private RTextFileChooser chooser;
	private RemoteFileChooser rfc;

	private HelpDialog helpDialog;

	private SpellingErrorWindow spellingWindow;

	private boolean aboutDialogCreated;

	private SyntaxScheme colorScheme;

	private IconGroup iconGroup;

	private String workingDirectory;	// The directory for new empty files.

	private String newFileName;		// The name for new empty text files.

	private SearchToolBar searchBar;

	private boolean showHostName;

	/**
	 * Whether <code>searchWindowOpacityListener</code> has been attempted to be
	 * created yet. This is kept in a variable instead of checking for
	 * <code>null</code> because the creation is done via reflection (since
	 * we're 1.4-compatible), so it is a fairly common case that creation is
	 * attempted but fails.
	 */
	private boolean windowListenersInited;

	/**
	 * Listens for focus events of certain child windows (those that can
	 * be made translucent on focus lost).
	 */
	private ChildWindowListener searchWindowOpacityListener;

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

	/**
	 * System property that, if set, causes RText to print timing information
	 * while it is starting up.
	 */
	public static final String PROPERTY_PRINT_START_TIMES = "printStartTimes";

	private static final String VERSION_STRING		= "1.0.0.????????-beta";


	/**
	 * Creates an instance of the <code>RText</code> editor.
	 *
	 * @param filesToOpen Array of <code>java.lang.String</code>s containing
	 *        the files we want to open initially.  This can be
	 *        <code>null</code> if no files are to be opened.
	 */
	public RText(String[] filesToOpen) {
		super("rtext", "RText.jar");
		init(filesToOpen);
	}


	/**
	 * Creates an instance of the <code>RText</code> editor.
	 *
	 * @param filesToOpen Array of <code>java.lang.String</code>s containing
	 *        the files we want to open initially.  This can be
	 *        <code>null</code> if no files are to be opened.
	 * @param preferences The preferences with which to initialize this RText.
	 */
	public RText(String[] filesToOpen, RTextPreferences preferences) {
		super("rtext", "RText.jar", preferences);
		init(filesToOpen);
	}


	// What to do when user does something.
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals("TileVertically")) {
			((RTextMDIView)mainView).tileWindowsVertically();
		}

		else if (command.equals("TileHorizontally")) {
			((RTextMDIView)mainView).tileWindowsHorizontally();
		}

		else if (command.equals("Cascade")) {
			((RTextMDIView)mainView).cascadeWindows();
		}

	}


	// TODO
	void addDockableWindow(DockableWindow wind) {
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
	public void convertOpenFilesSpacesToTabs() {
		mainView.convertOpenFilesSpacesToTabs();
	}


	/**
	 * Converts all tabs in all open documents into an equivalent number of
	 * spaces.
	 *
	 * @see #convertOpenFilesSpacesToTabs
	 */
	public void convertOpenFilesTabsToSpaces() {
		mainView.convertOpenFilesTabsToSpaces();
	}


	/**
	 * Returns the About dialog for this application.
	 *
	 * @return The About dialog.
	 */
	protected org.fife.ui.AboutDialog createAboutDialog() {
		aboutDialogCreated = true;
		return new org.fife.rtext.AboutDialog(this);
	}


	/**
	 * Creates the array of actions used by this RText.
	 *
	 * @param prefs The RText properties for this RText instance.
	 */
	protected void createActions(GUIApplicationPreferences prefs) {

		// We use a different resource bundle so we don't needlessly keep
		// all of this stuff in memory in the main RText bundle.
		ResourceBundle msg = ResourceBundle.getBundle(
									"org.fife.rtext.RTextActions");

		ClassLoader cl = this.getClass().getClassLoader();
		String commonIconPath = "org/fife/rtext/graphics/common_icons/";

		try {
			this.setIconImage(ImageIO.read(cl.getResource(
						"org/fife/rtext/graphics/rtexticon.gif")));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		StandardAction action = new NewAction(this, msg, null);
		action.setAccelerator(prefs.getAccelerator(NEW_ACTION));
		addAction(NEW_ACTION, action);

		action = new OpenAction(this, msg, null);
		action.setAccelerator(prefs.getAccelerator(OPEN_ACTION));
		addAction(OPEN_ACTION, action);

		action = new OpenInNewWindowAction(this, msg, null);
		action.setAccelerator(prefs.getAccelerator(OPEN_NEWWIN_ACTION));
		addAction(OPEN_NEWWIN_ACTION, action);

		action = new OpenRemoteAction(this, msg, null);
		action.setAccelerator(prefs.getAccelerator(OPEN_REMOTE_ACTION));
		addAction(OPEN_REMOTE_ACTION, action);

		action = new SaveAction(this, msg, null);
		action.setAccelerator(prefs.getAccelerator(SAVE_ACTION));
		addAction(SAVE_ACTION, action);

		action = new SaveAsAction(this, msg, null);
		action.setAccelerator(prefs.getAccelerator(SAVE_AS_ACTION));
		addAction(SAVE_AS_ACTION, action);

		action = new SaveAsRemoteAction(this, msg, null);
		action.setAccelerator(prefs.getAccelerator(SAVE_AS_REMOTE_ACTION));
		addAction(SAVE_AS_REMOTE_ACTION, action);

		action = new SaveAsWebPageAction(this, msg, null);
		action.setAccelerator(prefs.getAccelerator(SAVE_WEBPAGE_ACTION));
		addAction(SAVE_WEBPAGE_ACTION, action);

		action = new SaveAllAction(this, msg, null);
		action.setAccelerator(prefs.getAccelerator(SAVE_ALL_ACTION));
		addAction(SAVE_ALL_ACTION, action);

		String temp = msg.getString("CopyAsRtfAction");
		addAction(COPY_AS_RTF_ACTION, new RSyntaxTextAreaEditorKit.CopyAsRtfAction(temp,
			null,
			msg.getString("CopyAsRtfAction.ShortDesc"),
			new Integer(msg.getString("CopyAsRtfAction.Mnemonic").charAt(0)),
			prefs.getAccelerator(COPY_AS_RTF_ACTION)));

		temp = msg.getString("TimeAction");
		addAction(TIME_DATE_ACTION, new RTextAreaEditorKit.TimeDateAction(temp,
			new ImageIcon(cl.getResource(commonIconPath+"timedate16.gif")),
			msg.getString("TimeAction.ShortDesc"),
			new Integer(msg.getString("TimeAction.Mnemonic").charAt(0)),
			prefs.getAccelerator(TIME_DATE_ACTION)));

		action = new LineNumberAction(this, msg, null);
		action.setAccelerator(prefs.getAccelerator(LINE_NUMBER_ACTION));
		addAction(LINE_NUMBER_ACTION, action);

		boolean visible = true;//prefs.tasksWindowVisible;
		action = new ViewTasksAction(this, msg, null, visible);
		action.setAccelerator(prefs.getAccelerator(VIEW_TASKS_ACTION));
		addAction(VIEW_TASKS_ACTION, action);

		action = new NewToolAction(this, msg, null);
		action.setAccelerator(prefs.getAccelerator(NEW_TOOL_ACTION));
		addAction(NEW_TOOL_ACTION, action);

		action = new HelpAction(this, msg, "HelpAction");
		action.setAccelerator(prefs.getAccelerator(HELP_ACTION_KEY));
		addAction(HELP_ACTION_KEY, action);

		action = new AboutAction(this, msg, "AboutAction");
		action.setAccelerator(prefs.getAccelerator(ABOUT_ACTION_KEY));
		addAction(ABOUT_ACTION_KEY, action);

		action = new OptionsAction(this, msg, null);
		action.setAccelerator(prefs.getAccelerator(OPTIONS_ACTION));
		addAction(OPTIONS_ACTION, action);

		action = new HomePageAction(this, msg, null);
		action.setAccelerator(prefs.getAccelerator(HOME_PAGE_ACTION));
		addAction(HOME_PAGE_ACTION, action);

		msg = null; // May help with GC.

	}


	/**
	 * Creates and returns the menu bar used in this application.
	 *
	 * @param prefs This GUI application's preferences.
	 * @return The menu bar.
	 */
	protected JMenuBar createMenuBar(GUIApplicationPreferences prefs) {

		RTextPreferences properties = (RTextPreferences)prefs;

		//splashScreen.updateStatus(msg.getString("CreatingMenuBar"), 75);

		// Create the menu bar.
		menuBar = new RTextMenuBar(this, UIManager.getLookAndFeel().getName(),
									properties);
		mainView.addPropertyChangeListener(menuBar);

		menuBar.setWindowMenuVisible(properties.mainView==MDI_VIEW);

		return menuBar;

	}


	/**
	 * Returns the splash screen to display while this GUI application is
	 * loading.
	 *
	 * @return The splash screen.  If <code>null</code> is returned, no
	 *         splash screen is displayed.
	 */
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
	protected org.fife.ui.StatusBar createStatusBar(
							GUIApplicationPreferences prefs) {
		RTextPreferences properties = (RTextPreferences)prefs;
		StatusBar sb = new StatusBar(this, getString("Ready"),
					!properties.wordWrap, 1,1,
					properties.textMode==RTextEditorPane.OVERWRITE_MODE);
		sb.setStyle(properties.statusBarStyle);
		return sb;
	}


	/**
	 * Creates and returns the toolbar to be used by this application.
	 *
	 * @param prefs This GUI application's preferences.
	 * @return The toolbar.
	 */
	protected CustomizableToolBar createToolBar(
						GUIApplicationPreferences prefs) {

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
	public void doExit() {

		// Attempt to close all open documents.
		boolean allDocumentsClosed = getMainView().closeAllDocuments();

		// Assuming all documents closed okay (ie, the user
		// didn't click "Cancel")...
		if (allDocumentsClosed==true) {

			// If there will be no more rtext's running, stop the JVM.
			if (StoreKeeper.getInstanceCount()==1) {
				saveRTextPreferences();	// Save the user's running preferences.
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
				System.exit(0);				// And quit.
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
	 * Returns the filename used for newly created, empty text files.  This
	 * value is locale-specific.
	 *
	 * @return The new text file name.
	 */
	public String getNewFileName() {
		return newFileName;
	}


	/**
	 * Returns the file chooser being used by this RText instance.
	 *
	 * @return The file chooser.
	 * @see #getRemoteFileChooser()
	 */
	public RTextFileChooser getFileChooser() {
		if (chooser==null) {
			chooser = RTextUtilities.createFileChooser(this);
		}
		return chooser;
	}


	/**
	 * Returns the Help dialog for RText.
	 *
	 * @return The Help dialog.
	 * @see org.fife.ui.app.GUIApplication#getHelpDialog
	 */
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
			helpDialog.setBackButtonIcon(iconGroup.getIcon("back"));
			helpDialog.setForwardButtonIcon(iconGroup.getIcon("forward"));
		}
		helpDialog.setLocationRelativeTo(this);
		return helpDialog;
	}


	/**
	 * Returns the name of the local host.  This is lazily discovered.
	 *
	 * @return The name of the local host.
	 */
	public synchronized String getHostName() {
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
	 * Returns the icon group being used for icons for actions.
	 *
	 * @return The icon group.
	 */
	public IconGroup getIconGroup() {
		return iconGroup;
	}


	/**
	 * Returns the icon groups available to RText.
	 *
	 * @return The icon groups.
	 */
	Map getIconGroupMap() {
		return iconGroupMap;
	}


	/**
	 * Returns an array of information on JAR files containing 3rd party Look
	 * and Feels.  These JAR files will be added to the
	 * <code>UIManager</code>'s classpath so that these LnFs can be used in
	 * this GUI application.<p>
	 *
	 * RText reads all 3rd party Look and Feels from an XML file, so this stuff
	 * is done dynamically and can be configured by the user.
	 *
	 * @return An array of URLs for JAR files containing Look and Feels.
	 */
	protected ExtendedLookAndFeelInfo[] get3rdPartyLookAndFeelInfo() {
		try {
			return ThirdPartyLookAndFeelManager.get3rdPartyLookAndFeelInfo(
									this, "lnfs/lookandfeels.xml");
		} catch (IOException ioe) {
			return null;
		}
	}


	/**
	 * Returns the actual main view.
	 *
	 * @return The main view.
	 * @see #getMainViewStyle
	 * @see #setMainViewStyle
	 */
	public AbstractMainView getMainView() {
		return mainView;
	}


	/**
	 * Returns the main view style.
	 *
	 * @return The main view style, one of <code>TABBED_VIEW</code>,
	 *         <code>SPLIT_PANE_VIEW</code>, or <code>MDI_VIEW</code>.
	 * @see #setMainViewStyle
	 * @see #getMainView
	 */
	public int getMainViewStyle() {
		return mainViewStyle;
	}


	/**
	 * Returns the name of the preferences class for this application.  This
	 * class must be a subclass of <code>GUIApplicationPreferences</code>.
	 *
	 * @return The class name, or <code>null</code> if this GUI application
	 *         does not save preferences.
	 */
	protected String getPreferencesClassName() {
		return "org.fife.rtext.RTextPreferences";
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
	 */
	public SyntaxScheme getSyntaxScheme() {
		return colorScheme;
	}


	/**
	 * Returns the tab size (in spaces) currently being used.
	 *
	 * @return The tab size (in spaces) currently being used.
	 * @see #setTabSize
	 */
	public int getTabSize() {
		return mainView.getTabSize();
	}


	/**
	 * Returns the title of this window, less any "header" information
	 * (e.g. without the leading "<code>rtext - </code>").
	 *
	 * @return The title of this window.
	 * @see #setTitle(String)
	 */
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
	 * Called at the end of RText constructors.  Does common initialization
	 * for RText.
	 *
	 * @param filesToOpen Any files to open.  This can be <code>null</code>.
	 */
	private void init(String[] filesToOpen) {
		openFiles(filesToOpen);
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
		return searchBar==null ? false : searchBar.isVisible();
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
	 * Called whenever the user presses a key in the current text area.
	 *
	 * @param e The key event.
	 */
	public void keyPressed(KeyEvent e) {

		int keyCode = e.getKeyCode();

		switch (keyCode) {

			// If they're releasing the Insert key, toggle between
			// insert/overwrite mode for all editors OTHER THAN the one in
			// which the key was pressed (it is done for that one already).
			case KeyEvent.VK_INSERT:
				StatusBar statusBar = (StatusBar)getStatusBar();
				boolean isInsertMode = mainView.getTextMode()==
									RTextEditorPane.INSERT_MODE;
				statusBar.setOverwriteModeIndicatorEnabled(isInsertMode);
				// Toggle all of the other text areas.
				mainView.setTextMode(isInsertMode ?
									RTextEditorPane.OVERWRITE_MODE :
									RTextEditorPane.INSERT_MODE);
				break;

			// If they're releasing the Caps Lock key, toggle caps lock
			// in the status bar to reflect the actual state.
			case KeyEvent.VK_CAPS_LOCK:
				if (getOS()!=OS_MAC_OSX) {
					try {
						boolean state = Toolkit.getDefaultToolkit().
							getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
						statusBar = (StatusBar)getStatusBar();
						statusBar.setCapsLockIndicatorEnabled(state);
					} catch (UnsupportedOperationException uoe) {
						// Swallow; some OS's (OSX, some Linux) just
						// don't support this.
					}
				}
				break;

			default:
				// You cannot modify a read-only document.
				if (mainView.getCurrentTextArea().isReadOnly()) {
					getStatusBar().setStatusMessage(
										"Document is read only!");
				}

		} // End of switch (keyCode).

	}

	public void keyReleased(KeyEvent e) {
	}


	public void keyTyped(KeyEvent e) {
	}


	/**
	 * Loads and validates the icon groups available to RText.
	 */
	private void loadPossibleIconGroups() {
		iconGroupMap = IconGroupLoader.loadIconGroups(this,
					getInstallLocation() + "/icongroups/ExtraIcons.xml");
	}


	/**
	 * Thanks to Java Bug ID 5026829, JMenuItems (among other Swing components)
	 * don't update their accelerators, etc. when the properties on which they
	 * were created update them.  Thus, we have to do this manually.  This is
	 * still broken as of 1.5.
	 */
	protected void menuItemAcceleratorWorkaround() {
		menuBar.menuItemAcceleratorWorkaround();
	}


	/**
	 * Opens the specified files.
	 *
	 * @param filesToOpen The files to open.  This can be <code>null</code>.
	 * @see #openFile
	 */
	public void openFiles(String[] filesToOpen) {
		int count = filesToOpen==null ? 0 : filesToOpen.length;
		for (int i=0; i<count; i++) {
			openFile(filesToOpen[i]);
		}
	}


	/**
	 * This is called in the GUI application's constructor.  It is a chance
	 * to do initialization of stuff that will be needed before RText is
	 * displayed on-screen.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be <code>null</code>.
	 */
	public void preDisplayInit(GUIApplicationPreferences prefs,
								SplashScreen splashScreen) {

		long start = System.currentTimeMillis();

		// Some stuff down the line may assume this directory exists!
		File prefsDir = RTextUtilities.getPreferencesDirectory();
		if (!prefsDir.isDirectory()) {
			prefsDir.mkdirs();
		}

		// Install any plugins.
		super.preDisplayInit(prefs, splashScreen);

		RTextPreferences properties = (RTextPreferences)prefs;

		if (properties.searchToolBarVisible) {
			addToolBar(getSearchToolBar(), BorderLayout.SOUTH);
			searchBar.setVisible(true);
		}

		splashScreen.updateStatus(getString("AddingFinalTouches"), 90);

		// If the user clicks the "X" in the top-right of the window, do nothing.
		// (We'll clean up in our window listener).
		addWindowListener( new RTextWindowListener(this) );
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		mainView.setLineNumbersEnabled(properties.lineNumbersVisible);

		// Enable templates in text areas.
		if (RTextUtilities.enableTemplates(this, true)) {
			// If there are no templates, assume this is the user's first
			// time in RText and add some "standard" templates.
			CodeTemplateManager ctm = RTextEditorPane.getCodeTemplateManager();
			if (ctm.getTemplateCount()==0) {
				RTextUtilities.addDefaultCodeTemplates();
			}
		}

setSearchWindowOpacity(0.5f);

		if (Boolean.getBoolean(PROPERTY_PRINT_START_TIMES)) {
			System.err.println("preDisplayInit: " + (System.currentTimeMillis()-start));
		}

	}


	/**
	 * This is called in the GUI application's constructor.  It is a chance
	 * to do initialization of stuff that will be needed by the menu bar
	 * before it gets created.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be <code>null</code>.
	 */
	protected void preMenuBarInit(GUIApplicationPreferences prefs,
							SplashScreen splashScreen) {

		long start = System.currentTimeMillis();

		// Make the split pane positions same as last time.
		RTextPreferences rtp = (RTextPreferences)prefs;
		setSplitPaneDividerLocation(TOP, rtp.dividerLocations[TOP]);
		setSplitPaneDividerLocation(LEFT, rtp.dividerLocations[LEFT]);
		setSplitPaneDividerLocation(BOTTOM, rtp.dividerLocations[BOTTOM]);
		setSplitPaneDividerLocation(RIGHT, rtp.dividerLocations[RIGHT]);

		// Show any docked windows
		setSpellingWindowVisible(rtp.viewSpellingList);

		setShowHostName(rtp.showHostName);

		if (Boolean.getBoolean(PROPERTY_PRINT_START_TIMES)) {
			System.err.println("preMenuBarInit: " + (System.currentTimeMillis()-start));
		}

	}


	/**
	 * This is called in the GUI application's constructor.  It is a chance
	 * to do initialization of stuff that will be needed by the status bar
	 * bar before it gets created.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be <code>null</code>.
	 */
	protected void preStatusBarInit(GUIApplicationPreferences prefs,
							SplashScreen splashScreen) {

		long start = System.currentTimeMillis();

		final RTextPreferences properties = (RTextPreferences)prefs;
		final String[] filesToOpen = null;

		// Initialize our "new, empty text file" name.
		newFileName = getString("NewFileName");

		splashScreen.updateStatus(getString("SettingSHColors"), 10);
		setSyntaxScheme(properties.colorScheme);

		setWorkingDirectory(properties.workingDirectory);

		splashScreen.updateStatus(getString("CreatingView"), 20);

		// Initialize our view object.
		switch (properties.mainView) {
			case TABBED_VIEW:
				mainViewStyle = TABBED_VIEW;
				mainView = new RTextTabbedPaneView(RText.this, filesToOpen, properties);
				break;
			case SPLIT_PANE_VIEW:
				mainViewStyle = SPLIT_PANE_VIEW;
				mainView = new RTextSplitPaneView(RText.this, filesToOpen, properties);
				break;
			default:
				mainViewStyle = MDI_VIEW;
				mainView = new RTextMDIView(RText.this, filesToOpen, properties);
				break;
		}
		getContentPane().add(mainView);

		splashScreen.updateStatus(getString("CreatingStatusBar"), 25);

		if (Boolean.getBoolean(PROPERTY_PRINT_START_TIMES)) {
			System.err.println("preStatusBarInit: " + (System.currentTimeMillis()-start));
		}

	}


	/**
	 * This is called in the GUI application's constructor.  It is a chance
	 * for to do initialization of stuff that will be needed by the toolbar
	 * before it gets created.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be <code>null</code>.
	 */
	protected void preToolBarInit(GUIApplicationPreferences prefs,
							final SplashScreen splashScreen) {

		long start = System.currentTimeMillis();

		final RTextPreferences properties = (RTextPreferences)prefs;

		StatusBar statusBar = (StatusBar)getStatusBar();
		mainView.addPropertyChangeListener(statusBar);

//		// Initialize any actions.
// NOW DONE IN AbstractGUIApplication
//		splashScreen.updateStatus(getString("CreatingActions"), 30);
//		createActions(properties);

		loadPossibleIconGroups();
		try {
			setIconGroupByName(properties.iconGroupName);
		} catch (InternalError ie) {
			displayException(ie);
			System.exit(0);
		}

		splashScreen.updateStatus(getString("CreatingToolBar"), 60);
		if (Boolean.getBoolean(PROPERTY_PRINT_START_TIMES)) {
			System.err.println("preToolbarInit: " + (System.currentTimeMillis()-start));
		}

	}


	/**
	 * Called whenever a property changes for a component we are registered
	 * as listening to.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String propertyName = e.getPropertyName();

		// If the file's path is changing (must be caused by the file being saved(?))...
		if (propertyName.equals(RTextEditorPane.FULL_PATH_PROPERTY)) {
			setTitle((String)e.getNewValue());
		}

		// If the file's modification status is changing...
		else if (propertyName.equals(RTextEditorPane.DIRTY_PROPERTY)) {
			String oldTitle = getTitle();
			boolean newValue = ((Boolean)e.getNewValue()).booleanValue();
			if (newValue==false) {
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
			if (ChildWindowListener.isTranslucencySupported()) {
				searchWindowOpacityListener = new ChildWindowListener(this);
			}
		}

		if (searchWindowOpacityListener!=null) {
			w.addWindowFocusListener(searchWindowOpacityListener);
w.addComponentListener(searchWindowOpacityListener);
		}

	}


	/**
	 * Makes all actions use default accelerators.
	 */
	void restoreDefaultAccelerators() {

		int num = defaultActionAccelerators.length;
		for (int i=0; i<num; i++) {
			Action a = getAction(actionNames[i]);
			// Check for a null action because sometimes we have new actions
			// "declared" but not "defined" (e.g. OpenRemote).
			if (a!=null) {
				a.putValue(Action.ACCELERATOR_KEY,
						defaultActionAccelerators[i]);
			}
		}

		mainView.restoreDefaultAccelerators();

		menuItemAcceleratorWorkaround();

	}


	/**
	 * Attempts to write this RText instance's properties to wherever the OS
	 * writes Java Preferences stuff.
	 */
	public void saveRTextPreferences() {

		// Save preferences for RText itself.
		RTextPreferences prefs = (RTextPreferences)RTextPreferences.
										generatePreferences(this);
		prefs.savePreferences(this);

		// Save preferences for any plugins.
		Plugin[] plugins = getPlugins();
		int count = plugins.length;
		for (int i=0; i<count; i++) {
			plugins[i].savePreferences();
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
	public void setIconGroupByName(String name) {

		IconGroup newGroup = (IconGroup)iconGroupMap.get(name);
		if (newGroup==null)
			newGroup = (IconGroup)iconGroupMap.get(
							IconGroupLoader.DEFAULT_ICON_GROUP_NAME);
		if (newGroup==null)
			throw new InternalError("No icon groups!");
		if (iconGroup!=null && iconGroup.equals(newGroup))
			return;

		Dimension size = getSize();
		IconGroup old = iconGroup;
		iconGroup = newGroup;

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

		// Fix the icons for all actions owned by the tabbed pane.
		mainView.refreshIcons();

		// The toolbar uses the large versions of the icons, if available.
		// FIXME:  Make this toggle-able.
		ToolBar toolBar = (ToolBar)getToolBar();
		if (toolBar!=null)
			toolBar.checkForLargeIcons();

		// Do this because the toolbar has changed it's size.
		pack();
		setSize(size);

		// Make the help dialog use appropriate "back" and "forward" icons.
		if (helpDialog!=null) {
			helpDialog.setBackButtonIcon(iconGroup.getIcon("back"));
			helpDialog.setForwardButtonIcon(iconGroup.getIcon("forward"));
		}

		firePropertyChange(ICON_STYLE_PROPERTY, old, iconGroup);

	}


	/**
	 * Sets the main view style.  This method fires a property change of type
	 * <code>MAIN_VIEW_STYLE_PROPERTY</code>.
	 *
	 * @param viewStyle One of <code>TABBED_VIEW</code>,
	 *        <code>SPLIT_PANE_VIEW</code>, or <code>MDI_VIEW</code>.  If
	 *        this value is invalid, nothing happens.
	 * @see #getMainViewStyle
	 */
	public void setMainViewStyle(int viewStyle) {

		// Only do the update if viewStyle is different from the current viewStyle.
		if ((viewStyle==TABBED_VIEW || viewStyle==SPLIT_PANE_VIEW || viewStyle==MDI_VIEW)
			&& viewStyle!=mainViewStyle) {

			int oldMainViewStyle = mainViewStyle;
			mainViewStyle = viewStyle;
			AbstractMainView fromPanel = mainView;

			RTextPreferences props = (RTextPreferences)RTextPreferences.
									generatePreferences(this);

			// Create the new view.
			switch (viewStyle) {
				case TABBED_VIEW:
					mainView = new RTextTabbedPaneView(this, null, props);
					menuBar.setWindowMenuVisible(false);
					break;
				case SPLIT_PANE_VIEW:
					mainView = new RTextSplitPaneView(this, null, props);
					menuBar.setWindowMenuVisible(false);
					break;
				case MDI_VIEW:
					mainView = new RTextMDIView(this, null, props);
					menuBar.setWindowMenuVisible(true);
					break;
			}

			// Update property change listeners.
			PropertyChangeListener[] propertyChangeListeners =
								fromPanel.getPropertyChangeListeners();
			int length = propertyChangeListeners.length;
			for (int i=0; i<length; i++) {
				fromPanel.removePropertyChangeListener(propertyChangeListeners[i]);
				mainView.addPropertyChangeListener(propertyChangeListeners[i]);
			}

			// Keep find/replace dialogs working, if they've been created.
			// Make the new dialog listen to actions from the find/replace
			// dialogs.
			// NOTE:  The find and replace dialogs will be moved to mainView
			// in the copyData method below.
			if (fromPanel.findDialog!=null) {

				fromPanel.findDialog.changeActionListener(fromPanel, mainView);
				fromPanel.replaceDialog.changeActionListener(fromPanel, mainView);

				fromPanel.findDialog.addPropertyChangeListener(mainView);
				fromPanel.replaceDialog.addPropertyChangeListener(mainView);
				fromPanel.findDialog.removePropertyChangeListener(fromPanel);
				fromPanel.replaceDialog.removePropertyChangeListener(fromPanel);

			}

			// Make mainView have all the properties of the old panel.
			mainView.copyData(fromPanel);

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
			Container contentPane = getContentPane();
			contentPane.remove(fromPanel);
			contentPane.add(mainView);
			fromPanel = null;
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
	 * @see #getTabSize
	 */
	public void setTabSize(int newSize) {
		mainView.setTabSize(newSize);
	}


	/**
	 * Sets the title of the application window.  This title is prefixed
	 * with the application name.
	 *
	 * @param title The new title.
	 * @see #getTitle()
	 */
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
	}


	/**
	 * Toggles whether certain child windows should be made translucent.
	 *
	 * @param rule The new translucency rule.
	 * @see #getSearchWindowOpacityRule()
	 * @see #setSearchWindowOpacity(float)
	 */
	public void setSearchWindowOpacityRule(int rule) {
		searchWindowOpacityRule = rule;
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


	/**
	 * Updates the look and feel for all components and windows in
	 * this <code>RText</code> instance.  This method assumes that
	 * <code>UIManager.setLookAndFeel(lnf)</code> has already been called.
	 *
	 * @param lnf The new look and feel.
	 */
	public void updateLookAndFeel(LookAndFeel lnf) {

		try {

			Dimension size = this.getSize();

			// Update all components in this frame.
			SwingUtilities.updateComponentTreeUI(this);
			this.pack();
			this.setSize(size);

			// So mainView knows to update it's popup menus, etc.
			mainView.updateLookAndFeel();

			// Update any dialogs.
			if (optionsDialog != null) {
				SwingUtilities.updateComponentTreeUI(optionsDialog);
				optionsDialog.pack();
			}
			OptionsDialog pluginOptDialog = getPluginOptionsDialog(false);
			if (pluginOptDialog != null) {
				SwingUtilities.updateComponentTreeUI(pluginOptDialog);
				pluginOptDialog.pack();
			}
			if (helpDialog != null) {
				SwingUtilities.updateComponentTreeUI(helpDialog);
				helpDialog.pack();
			}
			if (aboutDialogCreated) {
				org.fife.ui.AboutDialog aboutDialog = getAboutDialog();
				SwingUtilities.updateComponentTreeUI(aboutDialog);
				aboutDialog.pack();
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


	/**
	 * 1.5.2004/pwy: The following two functions are called from the
	 * OSXAdapter and provide the hooks for the functions from the standard
	 * Apple application menu.  The "about()" OSX hook is in
	 * AbstractGUIApplication.
	 */
	public void preferences() {
		getAction(OPTIONS_ACTION).actionPerformed(new ActionEvent(this,0,"unused"));
	}

	public void openFile(final String filename) {
		//gets called when we receive an open event from the finder on OS X
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// null encoding means check for Unicode before using
				// system default encoding.
				mainView.openFile(filename, null);
			}
		});
	}


	/**
	 * Program entry point.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(final String[] args) {

		// 1.5.2004/pwy: Setting this property makes the menu appear on top
		// of the screen on Apple Mac OS X systems. It is ignored by all other
		// other Java implementations.
		System.setProperty("apple.laf.useScreenMenuBar","true");

		// 1.5.2004/pwy: Setting this property defines the standard
		// Application menu name on Apple Mac OS X systems. It is ignored by
		// all other Java implementations.
		// NOTE: Although you can set the useScreenMenuBar property above at
		// runtime, it appears that for this one, you must set it before
		// (such as in your *.app definition).
		//System.setProperty("com.apple.mrj.application.apple.menu.about.name", "RText");

		// Swing stuff should always be done on the EDT...
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				RText rtext = new RText(args);

				// For some reason, when using MDI_VIEW, the first window
				// isn't selected (although it is activated)...
				// INVESTIGATE ME!!
				if (rtext.getMainViewStyle()==MDI_VIEW) {
					rtext.getMainView().setSelectedIndex(0);
				}

				// We currently have one RText instance running.
				StoreKeeper.addRTextInstance(rtext);

			}
		});

	}


}