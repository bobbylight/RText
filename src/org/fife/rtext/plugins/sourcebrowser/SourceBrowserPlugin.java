/*
 * 07/08/2004
 *
 * SourceBrowserPlugin.java - A panel that uses Exuberant Ctags to keep a list
 * of variables, functions, etc. in the currently open source file in RText.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.sourcebrowser;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreeCellRenderer;

import org.fife.ctags.TagEntry;
import org.fife.rtext.*;
import org.fife.rtext.optionsdialog.OptionsDialog;
import org.fife.ui.RScrollPane;
import org.fife.ui.SubstanceUtils;
import org.fife.ui.WebLookAndFeelUtils;
import org.fife.ui.app.*;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowScrollPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Token;


/**
 * A panel that uses Exuberant CTags (installed separately from RText) to keep
 * a list of all variables, functions, classes, methods, etc. defined in the
 * currently-opened source file.  Clicking on an item in the Source Browser
 * moves the cursor to that item's position in the source file; also, right-
 * clicking on an item displays a popup menu.
 *
 * @author Robert Futrell
 * @version 1.2
 */
public class SourceBrowserPlugin extends GUIPlugin
				implements CurrentTextAreaListener {

	public static final String CTAGS_TYPE_EXUBERANT	= "Exuberant";
	public static final String CTAGS_TYPE_STANDARD	= "Standard";

	public static final String CUSTOM_HANDLER_PREFIX = "sbp.customHandler.";


	private RText owner;
	private String name;
	private JTree sourceTree;
	private RScrollPane scrollPane;
	private ResourceBundle msg;
	private Icon pluginIcon;
	private boolean useHTMLToolTips;
	private SourceBrowserThread sourceBrowserThread;
	private SourceTreeNode workingRoot;
	private JToolBar dockableWindowTB;

	private String ctagsExecutableLocation;
	private File ctagsFile;				// Just for speed.
	private String ctagsType;

	private SortAction sortAction;
	private JToggleButton sortButton;

	private ViewAction viewAction;
	private SourceBrowserOptionPanel optionPanel;

	static final String BUNDLE_NAME		=
					"org.fife.rtext.plugins.sourcebrowser.SourceBrowser";

	private static final String VERSION_STRING	= "2.5.2";

	private static final String VIEW_SB_ACTION	= "ViewSourceBrowserAction";

	private static final String RENDERER_WRAPPER_CLASS_NAME =
		"org.fife.rtext.plugins.sourcebrowser.SubstanceTreeCellRendererWrapper";


	/**
	 * Creates a new <code>SourceBrowserPlugin</code>.
	 *
	 * @param app The RText instance.
	 */
	public SourceBrowserPlugin(AbstractPluggableGUIApplication app) {

		this.owner = (RText)app;

		URL url = getClass().getResource("source_browser.png");
		pluginIcon = new ImageIcon(url);

		msg = ResourceBundle.getBundle(BUNDLE_NAME);
		this.name = msg.getString("Name");

		SourceBrowserPrefs sbp = loadPrefs();

		viewAction = new ViewAction(owner, msg);
		viewAction.setAccelerator(sbp.windowVisibilityAccelerator);

		// Set any preferences saved from the last time this plugin was used.
		DockableWindow wind = createDockableWindow(sbp);
		putDockableWindow(getPluginName(), wind);
		setCTagsExecutableLocation(sbp.ctagsExecutable);
		setCTagsType(sbp.ctagsType);
		setUseHTMLToolTips(sbp.useHTMLToolTips);

		sourceBrowserThread = new SourceBrowserThread(this);
		workingRoot = new SourceTreeNode(msg.getString("Working"));

	}


	/**
	 * If the Substance Look and Feel is installed, wraps a tree's renderer in
	 * a Substance-happy renderer.
	 *
	 * @param tree The tree whose renderer should be checked.
	 */
	private static final void checkTreeCellRenderer(JTree tree) {
		if (SubstanceUtils.isSubstanceInstalled()) {
			TreeCellRenderer renderer = tree.getCellRenderer();
			try {
				Class<?> clazz = Class.forName(RENDERER_WRAPPER_CLASS_NAME);
				Constructor<?> cons = clazz.getConstructor(
						new Class[] { TreeCellRenderer.class });
				renderer = (TreeCellRenderer)cons.newInstance(
						new Object[] { renderer });
				tree.setCellRenderer(renderer);
			} catch (Exception e) { // Never happens
				e.printStackTrace();
			}
		}
	}


	/**
	 * Creates the single dockable window used by this plugin.
	 *
	 * @param sbp Preferences for this plugin.
	 * @return The dockable window.
	 */
	private DockableWindow createDockableWindow(SourceBrowserPrefs sbp) {

		DockableWindow wind = new DockableWindow(this.name, new BorderLayout());

		dockableWindowTB = new JToolBar();
		dockableWindowTB.setFloatable(false);
		wind.add(dockableWindowTB, BorderLayout.NORTH);

		dockableWindowTB.add(Box.createHorizontalGlue());
		sortAction = new SortAction(owner, msg);
		sortButton = new JToggleButton(sortAction);
		dockableWindowTB.add(sortButton);
		// Allow tool bar to be resized very small so we don't hog space
		dockableWindowTB.setMinimumSize(new Dimension(8, 8));
		dockableWindowTB.setBorder(new BottomLineBorder(3));
		WebLookAndFeelUtils.fixToolbar(dockableWindowTB, false, true);

		sourceTree = new DefaultSourceTree(this, owner);
		wind.setPrimaryComponent(sourceTree);
		RTextUtilities.removeTabbedPaneFocusTraversalKeyBindings(sourceTree);

		scrollPane = new DockableWindowScrollPane(sourceTree);
		RTextUtilities.removeTabbedPaneFocusTraversalKeyBindings(scrollPane);
		//scrollPane.setViewportBorder(
		//					BorderFactory.createEmptyBorder(3,3,3,3));
		wind.add(scrollPane);

		wind.setActive(sbp.active);
		wind.setPosition(sbp.position);
		wind.setIcon(getPluginIcon());
		ComponentOrientation o = ComponentOrientation.
									getOrientation(Locale.getDefault());
		wind.applyComponentOrientation(o);

		return wind;

	}


	/**
	 * Called whenever the currently-active document in RText changes, or
	 * one of its properties changes.  We are looking for cues to update
	 * our source browser tree.
	 */
	public void currentTextAreaPropertyChanged(CurrentTextAreaEvent e) {

		// Don't worry about it if we're not visible.
		final DockableWindow wind = getDockableWindow(getPluginName());
		if (!wind.isActive()/* || !wind.isShowing()*/)
			return;

		int type = e.getType();
		boolean doChange = 
				(type==CurrentTextAreaEvent.TEXT_AREA_CHANGED &&
					((RTextEditorPane)e.getNewValue()!=null)) ||
				(type==CurrentTextAreaEvent.IS_MODIFIED_CHANGED &&
					(Boolean.FALSE.equals(e.getNewValue()))) ||
				(type==CurrentTextAreaEvent.SYNTAX_STYLE_CNANGED);

		// If we should parse the file...
		if (doChange) {

			// Stop the thread if it's still parsing the previous
			// request for some reason. Note that it's okay to
			// interrupt it even if it's already done (e.g. not running).
			if (sourceBrowserThread!=null) {
				sourceBrowserThread.interrupt();
			}

			// If the user has registered a special handler for this particular
			// language, use it instead.
			RTextEditorPane textArea = owner.getMainView().getCurrentTextArea();
			String style = textArea.getSyntaxEditingStyle();
			final String customHandlerName = System.getProperty(
									CUSTOM_HANDLER_PREFIX + style);
			if (customHandlerName!=null) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							Class<?> clazz = Class.forName(customHandlerName);
							Object handler = clazz.newInstance();
							java.lang.reflect.Method m = clazz.getMethod(
									"constructSourceBrowserTree",
									new Class[] { RText.class });
							sourceTree = (JTree)m.invoke(handler,
									new Object[] { owner });
							checkTreeCellRenderer(sourceTree);
							wind.setPrimaryComponent(sourceTree);
							RTextUtilities.removeTabbedPaneFocusTraversalKeyBindings(sourceTree);
							ensureSourceTreeSortedProperly();
							scrollPane.setViewportView(sourceTree);
						} catch (RuntimeException re) { // FindBugs
							throw re;
						} catch (Exception ex) {
							owner.displayException(ex);
						}
					}});
				return;
			}

			if (!(sourceTree instanceof DefaultSourceTree)) {
				sourceTree = new DefaultSourceTree(this, owner);
				wind.setPrimaryComponent(sourceTree);
				RTextUtilities.removeTabbedPaneFocusTraversalKeyBindings(sourceTree);
				scrollPane.setViewportView(sourceTree);
			}

			// If we cannot find the ctags executable, quit now.
			if (ctagsFile==null || !ctagsFile.isFile()) {
				setErrorMessage(msg.getString("Error.ExeNotFound"));
				return;
			}

			// First, determine what language the user is programming in
			// (via the text editor's syntax highlighting style).  We do
			// it this way because the user may have some odd extension
			// (like .abc) mapped to say C source files.
			Icon fileIcon = FileTypeIconManager.get().
										getIconFor(textArea);
			((DefaultSourceTree)sourceTree).setRootIcon(fileIcon);
			String language = getLanguageForStyle(style);
			if (language==null) {
				// Language not supported by ctags.
				((DefaultSourceTree)sourceTree).setRoot(null);
				return;
			}
			((DefaultSourceTree)sourceTree).setRoot(workingRoot);

			// Start a new process in a separate thread to parse the
			// file.  When the thread completes it will automatically
			// update our source tree.
			if (sourceBrowserThread!=null) {
				sourceBrowserThread.reset();
				sourceBrowserThread.start(10000, textArea, style, language,
												(DefaultSourceTree)sourceTree);
			}

		}

	}


	/**
	 * Ensures that a source tree is sorted or not sorted, to match the
	 * sorting button's current state.
	 */
	protected void ensureSourceTreeSortedProperly() {

		Class<?> clazz = sourceTree.getClass();
		Method sortMethod = null;
		try {
			sortMethod = clazz.getMethod("setSorted",
										new Class[] { boolean.class });
		} catch (NoSuchMethodException nsme) {
			nsme.printStackTrace();
			return;
		}

		Object[] args = { Boolean.valueOf(sortButton.isSelected()) };
		try {
			sortMethod.invoke(sourceTree, args);
		} catch (RuntimeException re) { // FindBugs
			throw re;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	/**
	 * Ensures the specified "type" of ctags is supported.
	 *
	 * @param type The "type" of ctags.
	 * @return Whether that type is supported.
	 * @see #CTAGS_TYPE_EXUBERANT
	 * @see #CTAGS_TYPE_STANDARD
	 */
	private static final String ensureValidCTagsType(String type) {
		if (type==null) {
			type = SourceBrowserPlugin.CTAGS_TYPE_EXUBERANT;
		}
		else if (!SourceBrowserPlugin.CTAGS_TYPE_EXUBERANT.equals(type) &&
				!SourceBrowserPlugin.CTAGS_TYPE_STANDARD.equals(type)) {
			type = SourceBrowserPlugin.CTAGS_TYPE_EXUBERANT;
		}
		return type;
	}


	/**
	 * Returns this plugin's resource bundle.
	 *
	 * @return This plugin's resource bundle.
	 */
	ResourceBundle getBundle() {
		return msg;
	}


	/**
	 * Returns the path used to run the ctags executable.
	 *
	 * @return The path used.
	 * @see #setCTagsExecutableLocation
	 */
	public String getCTagsExecutableLocation() {
		return ctagsExecutableLocation;
	}


	/**
	 * Returns a string representation of the type of Ctags specified by
	 * the user.
	 *
	 * @return The type of Ctags specified.
	 * @see #setCTagsType(String)
	 * @see SourceBrowserPlugin#CTAGS_TYPE_EXUBERANT
	 * @see SourceBrowserPlugin#CTAGS_TYPE_STANDARD
	 */
	public String getCTagsType() {
		return ctagsType;
	}


	/**
	 * Returns HTML (to use in tool tips) representing the specified line
	 * in the current text area.
	 *
	 * @param line The line.
	 * @return An HTML representation of the line.
	 */
	protected String getHTMLForLine(int line) {
		RSyntaxTextArea textArea = owner.getMainView().getCurrentTextArea();
		Token t = textArea.getTokenListForLine(line);
		StringBuilder text = new StringBuilder("<html>");
		while (t!=null && t.isPaintable()) {
			t.appendHTMLRepresentation(text, textArea, true);
			t = t.getNextToken();
		}
		text.append("</html>");
		return text.toString();
	}


	private static final String getLanguageForStyle(String style) {
		String language = null;
		if (style.equals(SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT)) {
			language = "Flex"; // Same as MXML to Exuberant Ctags
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_C)) {
			language = "C";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS)) {
			language = "C++";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_CSHARP)) {
			language = "C#";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_DELPHI)) {
			language = "Pascal";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_FORTRAN)) {
			language = "Fortran";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_GROOVY)) {
			language = "Groovy";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_HTML)) {
			language = "HTML";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_JAVA)) {
			language = "Java";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT)) {
			language = "JavaScript";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_LISP)) {
			language = "Lisp";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_LUA)) {
			language = "Lua";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_MAKEFILE)) {
			language = "Make";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_MXML)) {
			language = "Flex"; // Same as ActionScript to Exuberant Ctags
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_PERL)) {
			language = "Perl";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_PHP)) {
			language = "PHP";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_PYTHON)) {
			language = "Python";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_RUBY)) {
			language = "Ruby";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_SQL)) {
			language = "SQL";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_TCL)) {
			language = "Tcl";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL)) {
			language = "Sh";
		}
		else if (style.equals(SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH)) {
			language = "DosBatch";
		}
		return language;
	}


	/**
	 * Returns the options panel for this source browser.
	 *
	 * @return The options panel.
	 */
	public synchronized PluginOptionsDialogPanel getOptionsDialogPanel() {
		if (optionPanel==null) {
			optionPanel = new SourceBrowserOptionPanel(owner, this);
		}
		return optionPanel;
	}


	/**
	 * Returns the author of the plugin.
	 *
	 * @return The plugin's author.
	 */
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	/**
	 * Returns the icon for this plugin.
	 *
	 * @return The icon for this plugin.
	 */
	public Icon getPluginIcon() {
		return pluginIcon;
	}


	/**
	 * Returns the name of this <code>GUIPlugin</code>.
	 *
	 * @return This plugin's name.
	 */
	public String getPluginName() {
		return name;
	}


	/**
	 * Returns the plugin version.
	 */
	public String getPluginVersion() {
		return VERSION_STRING;
	}


	/**
	 * Returns the file preferences for this plugin are saved in.
	 *
	 * @return The file.
	 */
	private static final File getPrefsFile() {
		return new File(RTextUtilities.getPreferencesDirectory(),
						"sourceBrowser.properties");
	}


	/**
	 * Return whether HTML tooltips are being used by the source browser.
	 *
	 * @return Whether HTML tooltips are used.
	 * @see #setUseHTMLToolTips
	 */
	public boolean getUseHTMLToolTips() {
		return useHTMLToolTips;
	}


	/**
	 * Called just after a plugin is added to a GUI application.<p>
	 *
	 * This method adds a listener to RText's main view so that we are
	 * notified when the current document changes (so we can update the
	 * displayed ctags).
	 *
	 * @param app The application to which this plugin was just added.
	 * @see #uninstall
	 */
	public void install(AbstractPluggableGUIApplication app) {

		owner.getMainView().addCurrentTextAreaListener(this);

		// Add a menu item to toggle the visibility of the dockable window
		owner.addAction(VIEW_SB_ACTION, viewAction);
		RTextMenuBar mb = (RTextMenuBar)owner.getJMenuBar();
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(viewAction);
		item.setSelected(getDockableWindow(getPluginName()).isActive());
		item.applyComponentOrientation(app.getComponentOrientation());
		JMenu viewMenu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		viewMenu.add(item);
		JPopupMenu popup = viewMenu.getPopupMenu();
		popup.pack();
		// Only needed for pre-1.6 support
		popup.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				item.setSelected(getDockableWindow(getPluginName()).isActive());
			}
		});

	}


	/**
	 * Loads saved preferences into the <code>prefs</code> member.  If this
	 * is the first time through, default values will be returned.
	 *
	 * @return The preferences.
	 */
	private SourceBrowserPrefs loadPrefs() {
		SourceBrowserPrefs prefs = new SourceBrowserPrefs();
		File prefsFile = getPrefsFile();
		if (prefsFile.isFile()) {
			try {
				prefs.load(prefsFile);
			} catch (IOException ioe) {
				owner.displayException(ioe);
				// (Some) defaults will be used
			}
		}
		return prefs;
	}


	/**
	 * Refreshes the tag list for the current document.
	 */
	public void refresh() {
		// Tricks this browser into refreshing its tree using the new
		// ctags location.
		currentTextAreaPropertyChanged(new CurrentTextAreaEvent(
					owner.getMainView(),
					CurrentTextAreaEvent.TEXT_AREA_CHANGED,
					null, owner.getMainView().getCurrentTextArea()));
	}


	/**
	 * {@inheritDoc}
	 */
	public void savePreferences() {
		SourceBrowserPrefs prefs = new SourceBrowserPrefs();
		prefs.active = getDockableWindow(name).isActive();
		prefs.position = getDockableWindow(name).getPosition();
		ViewAction a = (ViewAction)owner.getAction(VIEW_SB_ACTION);
		prefs.windowVisibilityAccelerator = a.getAccelerator();
		prefs.ctagsExecutable = getCTagsExecutableLocation();
		prefs.ctagsType = getCTagsType();
		prefs.useHTMLToolTips = getUseHTMLToolTips();
		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			owner.displayException(ioe);
		}
	}


	/**
	 * Sets the path used to run the ctags executable.
	 *
	 * @param location The path to use.
	 * @see #getCTagsExecutableLocation
	 */
	public void setCTagsExecutableLocation(String location) {
		if (ctagsExecutableLocation==null ||
				!ctagsExecutableLocation.equals(location)) {
			ctagsExecutableLocation = location;
			ctagsFile = location==null ? null : new File(location);
			refresh(); // Redo ctags list with new executable.
		}
	}


	/**
	 * Sets the type of Ctags specified by the user.
	 *
	 * @param type The type of Ctags specified.  If this is <code>null</code>
	 *        or invalid, {@link SourceBrowserPlugin#CTAGS_TYPE_EXUBERANT} is
	 *        used.
	 * @see #getCTagsType()
	 * @see SourceBrowserPlugin#CTAGS_TYPE_EXUBERANT
	 * @see SourceBrowserPlugin#CTAGS_TYPE_STANDARD
	 */
	public void setCTagsType(String type) {
		this.ctagsType = ensureValidCTagsType(type);
	}


	/**
	 * Makes the "tags tree" display an error message.
	 *
	 * @param errorMessage The message to display.
	 */
	private void setErrorMessage(String message) {
		SourceTreeNode root = new SourceTreeNode(null, false);
		SourceTreeNode node = new SourceTreeNode(message);
		root.add(node);
		((DefaultSourceTree)sourceTree).setRoot(root);
	}


	/**
	 * Sets whether HTML tooltips are used in this source browser.
	 *
	 * @param use Whether or not to use HTML tooltips.
	 * @see #getUseHTMLToolTips
	 */
	public void setUseHTMLToolTips(boolean use) {
		useHTMLToolTips = use;
	}


	/**
	 * Shows this plugin's options in the Options dialog.
	 */
	void showOptions() {
		OptionsDialog od = (OptionsDialog)owner.getOptionsDialog();
		od.initialize();
		od.setSelectedOptionsPanel(msg.getString("Name"));
		od.setVisible(true);
	}


	/**
	 * Called just before this <code>Plugin</code> is removed from an
	 * RText instance.  Here we uninstall any listeners we registered.
	 *
	 * @return Whether the uninstall went cleanly.
	 */
	public boolean uninstall() {
		owner.getMainView().removeCurrentTextAreaListener(this);
		return true;
	}


	/**
	 * This method is overridden so that the embedded tree and its right-
	 * click popup menu are updated.
	 */
	public void updateUI() {
		DockableWindow wind = getDockableWindow(getPluginName());
		SwingUtilities.updateComponentTreeUI(wind);
		WebLookAndFeelUtils.fixToolbar(dockableWindowTB);
	}


	/**
	 * A tag entry with an extra field to cache the tool tip text.
	 */
	static class ExtendedTagEntry extends TagEntry {

		public String cachedToolTipText;

		public ExtendedTagEntry(String line) {
			super(line);
		}
	}


	/**
	 * Toggles whether the source tree is sorted alphabetically.
	 */
	private class SortAction extends StandardAction {

		public SortAction(RText app, ResourceBundle msg) {
			super(app, msg, "Action.Sort");
			setIcon("alphab_sort_co.gif");
			setName(null); // No text on button
		}

		public void actionPerformed(ActionEvent e) {
			ensureSourceTreeSortedProperly();
		}

	}


	/**
	 * Toggles the visibility of this source browser.
	 */
	private class ViewAction extends StandardAction {

		public ViewAction(RText app, ResourceBundle msg) {
			super(app, msg, "MenuItem.View");
		}

		public void actionPerformed(ActionEvent e) {
			DockableWindow wind = getDockableWindow(getPluginName());
			wind.setActive(!wind.isActive());
		}

	}


}