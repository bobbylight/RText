/*
 * 07/08/2004
 *
 * SourceBrowserPlugin.java - A panel that uses Exuberant Ctags to keep a list
 * of variables, functions, etc. in the currently open source file in RText.
 * Copyright (C) 2004 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.sourcebrowser;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.*;

import org.fife.ctags.TagEntry;
import org.fife.rsta.ac.AbstractSourceTree;
import org.fife.rtext.*;
import org.fife.rtext.optionsdialog.OptionsDialog;
import org.fife.ui.ImageTranscodingUtil;
import org.fife.ui.RScrollPane;
import org.fife.ui.UIUtil;
import org.fife.ui.WebLookAndFeelUtils;
import org.fife.ui.app.*;
import org.fife.ui.app.icons.IconGroup;
import org.fife.ui.app.themes.FlatDarkTheme;
import org.fife.ui.app.themes.FlatLightTheme;
import org.fife.ui.app.themes.NativeTheme;
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
public class SourceBrowserPlugin extends GUIPlugin<RText>
				implements CurrentTextAreaListener, PropertyChangeListener {

	public static final String CTAGS_TYPE_EXUBERANT	= "Exuberant";
	public static final String CTAGS_TYPE_STANDARD	= "Standard";

	public static final String CUSTOM_HANDLER_PREFIX = "sbp.customHandler.";

	private final String name;
	private JTree sourceTree;
	private RScrollPane scrollPane;
	private final ResourceBundle msg;
	private Map<String, Icon> icons;
	private boolean useHTMLToolTips;
	private final SourceBrowserThread sourceBrowserThread;
	private final SourceTreeNode workingRoot;
	private JToolBar dockableWindowTB;

	private String ctagsExecutableLocation;
	private File ctagsFile;				// Just for speed.
	private String ctagsType;

	private ConfigureAction configureAction;

	private SortAction sortAction;
	private JToggleButton sortButton;

	private final ViewAction viewAction;
	private SourceBrowserOptionPanel optionPanel;

	static final String BUNDLE_NAME		=
					"org.fife.rtext.plugins.sourcebrowser.SourceBrowser";

	private static final String VERSION_STRING	= "6.0.1";

	private static final String VIEW_SB_ACTION	= "ViewSourceBrowserAction";

	private static final String CACHED_SOURCE_TREE = "sourceBrowser.fileSystemTree";


	/**
	 * Creates a new <code>SourceBrowserPlugin</code>.
	 *
	 * @param app The RText instance.
	 */
	public SourceBrowserPlugin(RText app) {

		super(app);
		app.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, this);
		loadIcons();

		msg = ResourceBundle.getBundle(BUNDLE_NAME);
		this.name = msg.getString("Name");

		SourceBrowserPrefs sbp = loadPrefs();

		viewAction = new ViewAction(app, msg);
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
	 * Creates the single dockable window used by this plugin.
	 *
	 * @param sbp Preferences for this plugin.
	 * @return The dockable window.
	 */
	private DockableWindow createDockableWindow(SourceBrowserPrefs sbp) {

		RText owner = getApplication();
		DockableWindow wind = new DockableWindow(this.name, new BorderLayout());

		dockableWindowTB = new JToolBar();
		dockableWindowTB.setFloatable(false);
		wind.add(dockableWindowTB, BorderLayout.NORTH);

		dockableWindowTB.add(Box.createHorizontalGlue());
		configureAction = new ConfigureAction(owner, msg);
		JButton configureButton = new JButton(configureAction);
		dockableWindowTB.add(configureButton);
		sortAction = new SortAction(owner, msg);
		sortButton = new JToggleButton(sortAction);
		dockableWindowTB.add(sortButton);
		// Allow tool bar to be resized very small so we don't hog space
		dockableWindowTB.setMinimumSize(new Dimension(8, 8));
		dockableWindowTB.setBorder(new BottomLineBorder(3));
		WebLookAndFeelUtils.fixToolbar(dockableWindowTB, false, true);

		sourceTree = new DefaultSourceTree(this, owner);
		wind.setPrimaryComponent(sourceTree);
		UIUtil.removeTabbedPaneFocusTraversalKeyBindings(sourceTree);

		scrollPane = new DockableWindowScrollPane(sourceTree);
		UIUtil.removeTabbedPaneFocusTraversalKeyBindings(scrollPane);
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
	@Override
	public void currentTextAreaPropertyChanged(CurrentTextAreaEvent e) {

		// Don't worry about it if we're not visible.
		final DockableWindow wind = getDockableWindow(getPluginName());
		if (!wind.isActive()/* || !wind.isShowing()*/)
			return;

		int type = e.getType();
		boolean switchedToAnotherTextArea = type==CurrentTextAreaEvent.TEXT_AREA_CHANGED &&
			e.getNewValue() != null;
		boolean doChange = switchedToAnotherTextArea ||
				(type==CurrentTextAreaEvent.IS_MODIFIED_CHANGED &&
					(Boolean.FALSE.equals(e.getNewValue()))) ||
				(type==CurrentTextAreaEvent.SYNTAX_STYLE_CHANGED);
		RText owner = getApplication();

		// If we should parse the file...
		if (doChange) {

			// Stop the thread if it's still parsing the previous
			// request for some reason. Note that it's okay to
			// interrupt it even if it's already done (e.g. not running).
			if (sourceBrowserThread!=null) {
				sourceBrowserThread.interrupt();
			}

			RTextEditorPane textArea = owner.getMainView().getCurrentTextArea();

			JTree prevSourceTree = (JTree)textArea.
				getClientProperty(CACHED_SOURCE_TREE);
			if (switchedToAnotherTextArea && prevSourceTree != null) {
				sourceTree = prevSourceTree;
				scrollPane.setViewportView(sourceTree);
				return;
			}
			textArea.putClientProperty(CACHED_SOURCE_TREE, null);

			// If the user has registered a special handler for this particular
			// language, use it instead.
			String style = textArea.getSyntaxEditingStyle();
			final String customHandlerName = System.getProperty(
									CUSTOM_HANDLER_PREFIX + style);
			if (customHandlerName!=null) {
				SwingUtilities.invokeLater(() -> {
					try {
						Class<?> clazz = Class.forName(customHandlerName);
						Object handler = clazz.getDeclaredConstructor().newInstance();
						Method m = clazz.getMethod(
								"constructSourceBrowserTree", RText.class);
						sourceTree = (JTree)m.invoke(handler,
								new Object[] { owner });
						wind.setPrimaryComponent(sourceTree);
						UIUtil.removeTabbedPaneFocusTraversalKeyBindings(sourceTree);
						ensureSourceTreeSortedProperly();
						scrollPane.setViewportView(sourceTree);
						textArea.putClientProperty(CACHED_SOURCE_TREE, sourceTree);
					} catch (RuntimeException re) { // FindBugs
						throw re;
					} catch (Exception ex) {
						owner.displayException(ex);
					}
				});
				return;
			}

			if (!(sourceTree instanceof DefaultSourceTree)) {
				sourceTree = new DefaultSourceTree(this, owner);
				wind.setPrimaryComponent(sourceTree);
				UIUtil.removeTabbedPaneFocusTraversalKeyBindings(sourceTree);
				scrollPane.setViewportView(sourceTree);
				textArea.putClientProperty(CACHED_SOURCE_TREE, sourceTree);
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
			Icon fileIcon = owner.getMainView().getIconFor(textArea);
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
	void ensureSourceTreeSortedProperly() {

		Class<?> clazz = sourceTree.getClass();
		Method sortMethod;
		try {
			sortMethod = clazz.getMethod("setSorted", boolean.class);
		} catch (NoSuchMethodException nsme) {
			nsme.printStackTrace();
			return;
		}

		Object[] args = {sortButton.isSelected()};
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
	private static String ensureValidCTagsType(String type) {
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
	String getHTMLForLine(int line) {
		RSyntaxTextArea textArea = getApplication().getMainView().getCurrentTextArea();
		Token t = textArea.getTokenListForLine(line);
		StringBuilder text = new StringBuilder("<html>");
		while (t!=null && t.isPaintable()) {
			t.appendHTMLRepresentation(text, textArea, true);
			t = t.getNextToken();
		}
		text.append("</html>");
		return text.toString();
	}


	private static String getLanguageForStyle(String style) {
		return switch (style) {
			// Same as MXML to Exuberant Ctags
			case SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT -> "Flex";
			case SyntaxConstants.SYNTAX_STYLE_C -> "C";
			case SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS -> "C++";
			case SyntaxConstants.SYNTAX_STYLE_CSHARP -> "C#";
			case SyntaxConstants.SYNTAX_STYLE_DELPHI -> "Pascal";
			case SyntaxConstants.SYNTAX_STYLE_FORTRAN -> "Fortran";
			case SyntaxConstants.SYNTAX_STYLE_GROOVY -> "Groovy";
			case SyntaxConstants.SYNTAX_STYLE_HTML -> "HTML";
			case SyntaxConstants.SYNTAX_STYLE_JAVA -> "Java";
			case SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT -> "JavaScript";
			case SyntaxConstants.SYNTAX_STYLE_LISP -> "Lisp";
			case SyntaxConstants.SYNTAX_STYLE_LUA -> "Lua";
			case SyntaxConstants.SYNTAX_STYLE_MAKEFILE -> "Make";
			// Same as ActionScript to Exuberant Ctags
			case SyntaxConstants.SYNTAX_STYLE_MXML -> "Flex";
			case SyntaxConstants.SYNTAX_STYLE_PERL -> "Perl";
			case SyntaxConstants.SYNTAX_STYLE_PHP -> "PHP";
			case SyntaxConstants.SYNTAX_STYLE_PYTHON -> "Python";
			case SyntaxConstants.SYNTAX_STYLE_RUBY -> "Ruby";
			case SyntaxConstants.SYNTAX_STYLE_SQL -> "SQL";
			case SyntaxConstants.SYNTAX_STYLE_TCL -> "Tcl";
			case SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL -> "Sh";
			case SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH -> "DosBatch";
			default -> null;
		};
	}


	/**
	 * Returns the options panel for this source browser.
	 *
	 * @return The options panel.
	 */
	@Override
	public synchronized PluginOptionsDialogPanel<SourceBrowserPlugin> getOptionsDialogPanel() {
		if (optionPanel==null) {
			optionPanel = new SourceBrowserOptionPanel(getApplication(), this);
		}
		return optionPanel;
	}


	/**
	 * Returns the author of the plugin.
	 *
	 * @return The plugin's author.
	 */
	@Override
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	@Override
	public Icon getPluginIcon() {
		return getPluginIcon("plugin");
	}


	/**
	 * Returns a plugin-specific icon, relevant to the current application
	 * theme and icon group.
	 *
	 * @param name The icon name.
	 * @return The icon.
	 */
	Icon getPluginIcon(String name) {
		return icons.get(getApplication().getTheme().getId() + "-" + name);
	}


	/**
	 * Returns the name of this <code>GUIPlugin</code>.
	 *
	 * @return This plugin's name.
	 */
	@Override
	public String getPluginName() {
		return name;
	}


	/**
	 * Returns the plugin version.
	 */
	@Override
	public String getPluginVersion() {
		return VERSION_STRING;
	}


	/**
	 * Returns the file preferences for this plugin are saved in.
	 *
	 * @return The file.
	 */
	private static File getPrefsFile() {
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


	@Override
	public void iconGroupChanged(IconGroup iconGroup) {
		optionPanel.setIcon(getPluginIcon());
		getDockableWindow(getPluginName()).setIcon(getPluginIcon());
	}


	/**
	 * Called just after a plugin is added to a GUI application.<p>
	 *
	 * This method adds a listener to RText's main view so that we are
	 * notified when the current document changes (so we can update the
	 * displayed ctags).
	 *
	 * @see #uninstall
	 */
	@Override
	public void install() {

		RText owner = getApplication();
		owner.getMainView().addCurrentTextAreaListener(this);
		owner.getMainView().addPropertyChangeListener(AbstractMainView.TEXT_AREA_REMOVED_PROPERTY, this);

		// Add a menu item to toggle the visibility of the dockable window
		owner.addAction(VIEW_SB_ACTION, viewAction);
		RTextMenuBar mb = (RTextMenuBar)owner.getJMenuBar();
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(viewAction);
		item.setSelected(getDockableWindow(getPluginName()).isActive());
		item.applyComponentOrientation(owner.getComponentOrientation());
		JMenu viewMenu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		viewMenu.add(item);

	}


	private void loadIcons() {

		icons = new HashMap<>();
		Class<?> clazz = getClass();

		// Load the plugin icon and the blue/green "bullet" icons for the default source browser.
		try {

			icons.put(NativeTheme.ID + "-plugin", new ImageIcon(clazz.getResource("eclipse/source_browser.png")));
			icons.put(NativeTheme.ID + "-blue", new ImageIcon(clazz.getResource("eclipse/bullet_blue.gif")));
			icons.put(NativeTheme.ID + "-green", new ImageIcon(clazz.getResource("eclipse/bullet_green.gif")));

			Image darkThemeImage = ImageTranscodingUtil.rasterize("source browser dark",
				clazz.getResourceAsStream("flat-dark/source_browser.svg"), 16, 16);
			icons.put(FlatDarkTheme.ID + "-plugin", new ImageIcon(darkThemeImage));
			darkThemeImage = ImageTranscodingUtil.rasterize("source browser dark",
				clazz.getResourceAsStream("flat-dark/bullet_blue.svg"), 8, 8);
			icons.put(FlatDarkTheme.ID + "-blue", new ImageIcon(darkThemeImage));
			darkThemeImage = ImageTranscodingUtil.rasterize("source browser dark",
				clazz.getResourceAsStream("flat-dark/bullet_green.svg"), 8, 8);
			icons.put(FlatDarkTheme.ID + "-green", new ImageIcon(darkThemeImage));

			Image lightThemeImage = ImageTranscodingUtil.rasterize("source browser light",
				clazz.getResourceAsStream("flat-light/source_browser.svg"), 16, 16);
			icons.put(FlatLightTheme.ID + "-plugin", new ImageIcon(lightThemeImage));
			lightThemeImage = ImageTranscodingUtil.rasterize("source browser light",
				clazz.getResourceAsStream("flat-light/bullet_blue.svg"), 8, 8);
			icons.put(FlatLightTheme.ID + "-blue", new ImageIcon(lightThemeImage));
			lightThemeImage = ImageTranscodingUtil.rasterize("source browser light",
				clazz.getResourceAsStream("flat-light/bullet_green.svg"), 8, 8);
			icons.put(FlatLightTheme.ID + "-green", new ImageIcon(lightThemeImage));

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
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
				getApplication().displayException(ioe);
				// (Some) defaults will be used
			}
		}
		return prefs;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {

		String propertyName = e.getPropertyName();

		if (RText.ICON_STYLE_PROPERTY.equals(propertyName)) {
			sortAction.refreshIcon();
			configureAction.refreshIcon();
		}

		// Clean up cached source tree and listeners to aid GC
		else if (AbstractMainView.TEXT_AREA_REMOVED_PROPERTY.equals(propertyName)) {
			RTextEditorPane textArea = (RTextEditorPane)e.getNewValue();
			uninstallSourceTree(textArea);
		}
	}


	/**
	 * Refreshes the tag list for the current document.
	 */
	private void refresh() {
		// Tricks this browser into refreshing its tree using the new
		// ctags location.
		RText owner = getApplication();
		currentTextAreaPropertyChanged(new CurrentTextAreaEvent(
					owner.getMainView(),
					CurrentTextAreaEvent.TEXT_AREA_CHANGED,
					null, owner.getMainView().getCurrentTextArea()));
	}


	@Override
	public void savePreferences() {
		RText owner = getApplication();
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
	 * @param message The message to display.
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
	 * @param use whether to use HTML tooltips.
	 * @see #getUseHTMLToolTips
	 */
	public void setUseHTMLToolTips(boolean use) {
		useHTMLToolTips = use;
	}


	/**
	 * Shows this plugin's options in the Options dialog.
	 */
	void showOptions() {
		OptionsDialog od = (OptionsDialog)getApplication().getOptionsDialog();
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
	@Override
	public boolean uninstall() {
		RText owner = getApplication();
		owner.getMainView().removeCurrentTextAreaListener(this);
		owner.getMainView().removePropertyChangeListener(AbstractMainView.TEXT_AREA_REMOVED_PROPERTY, this);
		return true;
	}


	/**
	 * Removes and uninstalls the cached source tree, if any.  This is called whenever
	 * a text area is closed.
	 *
	 * @param textArea The text area that was closed.
	 */
	private void uninstallSourceTree(RSyntaxTextArea textArea) {
		Object tree = textArea.getClientProperty(CACHED_SOURCE_TREE);
		if (tree instanceof AbstractSourceTree) {
			((AbstractSourceTree)tree).uninstall();
		}
		textArea.putClientProperty(CACHED_SOURCE_TREE, null);
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
	 * Opens the options dialog to the configuration options for this plugin.
	 */
	private class ConfigureAction extends AppAction<RText> {

		ConfigureAction(RText app, ResourceBundle msg) {
			super(app, msg, "Action.Configure");
			refreshIcon();
			setName(null); // No text on button
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			org.fife.ui.OptionsDialog od = getApplication().getOptionsDialog();
			od.initialize();
			od.setSelectedOptionsPanel(msg.getString("Name"));
			od.setVisible(true);
		}

		void refreshIcon() {
			Icon icon = getApplication().getIconGroup().getIcon("options");
			if (icon != null) {
				setIcon(icon);
			}
			else {
				setIcon(new EmptyIcon(16, 16));
			}
		}
	}


	/**
	 * A tag entry with an extra field to cache the tool tip text.
	 */
	static class ExtendedTagEntry extends TagEntry {

		String cachedToolTipText;

		ExtendedTagEntry(String line) {
			super(line);
		}
	}


	/**
	 * Toggles whether the source tree is sorted alphabetically.
	 */
	private class SortAction extends AppAction<RText> {

		SortAction(RText app, ResourceBundle msg) {
			super(app, msg, "Action.Sort");
			refreshIcon();
			setName(null); // No text on button
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			ensureSourceTreeSortedProperly();
		}

		void refreshIcon() {
			setIcon(getApplication().getIconGroup().getIcon("sorted"));
		}
	}


	/**
	 * Toggles the visibility of this source browser.
	 */
	private class ViewAction extends AppAction<RText> {

		ViewAction(RText app, ResourceBundle msg) {
			super(app, msg, "MenuItem.View");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			DockableWindow wind = getDockableWindow(getPluginName());
			wind.setActive(!wind.isActive());
		}

	}


}
