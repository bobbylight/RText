/*
 * 07/08/2004
 *
 * SourceBrowserPlugin.java - A panel that uses Exuberant Ctags to keep a list
 * of variables, functions, etc. in the currently open source file in RText.
 * Copyright (C) 2004 Robert Futrell
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
package org.fife.rtext.plugins.sourcebrowser;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.tree.*;

import org.fife.ctags.TagEntry;
import org.fife.rtext.*;
import org.fife.ui.RScrollPane;
import org.fife.ui.RTreeSelectionModel;
import org.fife.ui.UIUtil;
import org.fife.ui.app.*;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowScrollPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.SearchEngine;


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
				implements CurrentTextAreaListener, MouseListener,
				MouseMotionListener, TreeSelectionListener, ActionListener {

	public static final String CTAGS_TYPE_EXUBERANT	= "Exuberant";
	public static final String CTAGS_TYPE_STANDARD	= "Standard";

	private RText owner;
	private String name;
	private JTree sourceTree;
	private SourceTreeCellRenderer treeRenderer;
	private DefaultTreeModel treeModel;
	private RScrollPane scrollPane;
	private ResourceBundle msg;
	private String lineFoundText;
	private String cantFindLineText;
	private Icon fileIcon;
	private Icon pluginIcon;
	private boolean useHTMLToolTips;
	private SourceBrowserThread sourceBrowserThread;
	private DefaultMutableTreeNode workingRoot;

	private String ctagsExecutableLocation;
	private File ctagsFile;				// Just for speed.
	private String ctagsType;

	private JPopupMenu rightClickMenu;		// For the user right-clicking a node.
	private int mouseX;
	private int mouseY;					// Where mouse was right-clicked.

	private ViewAction viewAction;
	private SourceBrowserOptionPanel optionPanel;

	static final String BUNDLE_NAME		=
					"org.fife.rtext.plugins.sourcebrowser.SourceBrowser";

	private static final String YELLOW_BULLET	= "bullet_blue.gif";
	private static final String GREEN_BULLET	= "bullet_green.gif";

	private static final String VERSION_STRING	= "1.1.0";

	private static final String VIEW_SB_ACTION	= "ViewSourceBrowserAction";


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
		this.lineFoundText = msg.getString("StatusBarMsg.FoundLine");
		this.cantFindLineText = msg.getString("StatusBarMsg.CantFindLine");

		viewAction = new ViewAction(msg);

		// Set any preferences saved from the last time this plugin was used.
		SourceBrowserPrefs sbp = loadPrefs();
		DockableWindow wind = createDockableWindow(sbp);
		putDockableWindow(getPluginName(), wind);
		setCTagsExecutableLocation(sbp.ctagsExecutable);
		setCTagsType(sbp.ctagsType);
		setUseHTMLToolTips(sbp.useHTMLToolTips);

		sourceBrowserThread = new SourceBrowserThread(this);
		workingRoot = new DefaultMutableTreeNode(msg.getString("Working"));

	}


	/**
	 * Listens for actions in this component.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		int row = sourceTree.getClosestRowForLocation(mouseX, mouseY);

		// Go to the tag's location in the current file.
		if (command.equals("JumpToTag")) {
			sourceTree.setSelectionRow(row);
		}

		// Insert the tag at the current caret position.
		else if (command.equals("InsertAtCaret")) {
			RTextEditorPane editor = owner.getMainView().getCurrentTextArea();
			editor.replaceSelection(getTagTextForRow(row));
			editor.requestFocusInWindow();
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

		sourceTree = new JTree();
		sourceTree.setToggleClickCount(1);
		treeRenderer = new SourceTreeCellRenderer();
		sourceTree.setCellRenderer(treeRenderer);
		sourceTree.setSelectionModel(new RTreeSelectionModel());
		sourceTree.addTreeSelectionListener(this);
		treeModel = new DefaultTreeModel(null);
		sourceTree.setModel(treeModel);
		ToolTipManager.sharedInstance().registerComponent(sourceTree);
		sourceTree.addMouseMotionListener(this);
		sourceTree.addMouseListener(this);

		scrollPane = new DockableWindowScrollPane(sourceTree);
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
	 * Creates the right-click menu.  This menu contains options for things
	 * such as going to the specified tag, inserting the tag at the current
	 * caret position, etc.
	 */
	private void createRightClickMenu() {

		rightClickMenu = new JPopupMenu();

		// Create an item for inserting the tag at the caret location.
		JMenuItem menuItem = new JMenuItem("foo");
		menuItem.setActionCommand("InsertAtCaret");
		menuItem.addActionListener(this);
		rightClickMenu.add(menuItem);

		// Create a menu item for jumping to a tag's location.
		menuItem = new JMenuItem("bar");
		menuItem.setActionCommand("JumpToTag");
		menuItem.addActionListener(this);
		rightClickMenu.add(menuItem);

		ComponentOrientation o = ComponentOrientation.
										getOrientation(Locale.getDefault());
		rightClickMenu.applyComponentOrientation(o);

	}


	/**
	 * Called whenever the currently-active document in RText changes, or
	 * one of its properties changes.  We are looking for cues to update
	 * our source browser tree.
	 */
	public void currentTextAreaPropertyChanged(CurrentTextAreaEvent e) {

		// Don't worry about it if we're not visible.
		DockableWindow wind = getDockableWindow(getPluginName());
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

			// If we cannot find the ctags executable, quit now.
			if (ctagsFile==null || !ctagsFile.isFile()) {
				setErrorMessage(msg.getString("Error.ExeNotFound"));
				return;
			}

			// First, determine what language the user is programming in
			// (via the text editor's syntax highlighting style).  We do
			// it this way because the user may have some odd extension
			// (like .abc) mapped to say C source files.
			RTextEditorPane textArea = owner.getMainView().
										getCurrentTextArea();
			fileIcon = FileTypeIconManager.getInstance().
										getIconFor(textArea);
			String style = textArea.getSyntaxEditingStyle();
			String language = getLanguageForStyle(style);
			if (language==null) {
				// Language not supported by ctags.
				setTreeRoot(null);
				return;
			}
			setTreeRoot(workingRoot);

			// Start a new process in a separate thread to parse the
			// file.  When the thread completes it will automatically
			// update our source tree.
			if (sourceBrowserThread!=null) {
				sourceBrowserThread.reset();
				sourceBrowserThread.start(10000, textArea, style, language);
			}

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
	private String ensureValidCTagsType(String type) {
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
	 * Returns HTML (to use in tooltips) representing the specified line
	 * in the current text area.
	 *
	 * @param line The line.
	 * @return An HTML representation of the line.
	 */
	protected String getHTMLForLine(int line) {
		RSyntaxTextArea textArea = owner.getMainView().getCurrentTextArea();
		Token t = textArea.getTokenListForLine(line);
		StringBuffer text = new StringBuffer("<html>");
		while (t!=null && t.isPaintable()) {
			text.append(t.getHTMLRepresentation(textArea));
			t = t.getNextToken();
		}
		text.append("</html>");
		return text.toString();
	}


	protected String getLanguageForStyle(String style) {
		String language = null;
		if (style.equals(SyntaxConstants.SYNTAX_STYLE_C)) {
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
	private File getPrefsFile() {
		return new File(RTextUtilities.getPreferencesDirectory(),
						"sourceBrowser.properties");
	}


	/**
	 * Returns the text being displayed for the specified row in the ctags
	 * tree.
	 *
	 * @param row The for for which to return text.
	 * @return The text.
	 */
	private final String getTagTextForRow(int row) {
		TreePath path = sourceTree.getPathForRow(row);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.
											getLastPathComponent();
		return node.toString();
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
		JMenu viewMenu = mb.getMenuByName(RTextMenuBar.MENU_VIEW);
		viewMenu.insert(item, viewMenu.getMenuComponentCount()-2);
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
	 * Decides whether or not to display the popup menu.
	 *
	 * @param e The mouse event to check.
	 */
	public void maybeShowPopup(MouseEvent e) {

		if (e.isPopupTrigger()) {

			mouseX = e.getX();
			mouseY = e.getY();
			int row = sourceTree.getClosestRowForLocation(mouseX, mouseY);
			if (row==-1)
				return; // If tree isn't visible (e.g. plain text file).

			DefaultMutableTreeNode node = (DefaultMutableTreeNode)sourceTree.
							getPathForRow(row).getLastPathComponent();

			String tagText = node.toString();

			// Ignore the expandable "tag type" nodes. Special care must be
			// taken here because empty tag-type nodes are considered
			// "leaves."  Also display no menu if "<ctags not found>" message
			// is displayed.
			if (!sourceTree.getModel().isLeaf(node) ||
					tagText.indexOf("(0)")>-1 || tagText.startsWith("<")) {
				return;
			}

			// Create the right-click menu if it isn't already done.
			if (rightClickMenu==null) {
				createRightClickMenu();
			}

			// Prepare the "Insert tag" menu item.
			JMenuItem menuItem = (JMenuItem)rightClickMenu.getComponent(0);
			menuItem.setText(msg.getString("InsertTag") + tagText);
			menuItem.setMnemonic(
						msg.getString("InsertTagMnemonic").charAt(0));

			// Prepare the "Jump to tag" menu item.
			menuItem = (JMenuItem)rightClickMenu.getComponent(1);
			menuItem.setText(msg.getString("JumpToTag") + tagText);
			menuItem.setMnemonic(
						msg.getString("JumpToTagMnemonic").charAt(0));
			rightClickMenu.show(sourceTree, mouseX, mouseY);

		}

	}


	/**
	 * Called when the user clicks in the ctags tree.
	 */
	public void mouseClicked(MouseEvent e) {
		maybeShowPopup(e);
	}


	/**
	 * Called when the mouse is dragged in the ctags tree.
	 */
	public void mouseDragged(MouseEvent e) {
		mouseMovedImpl(e);
	}


	/**
	 * Called when the mouse enters the ctags tree.
	 */
	public void mouseEntered(MouseEvent e) {
	}


	/**
	 * Called when the mouse exits the ctags tree.
	 */
	public void mouseExited(MouseEvent e) {
	}


	/**
	 * Called when the mouse is moved in the ctags tree.
	 */
	public void mouseMoved(MouseEvent e) {
		mouseMovedImpl(e);
	}


	/**
	 * Called when the mouse moves/is dragged over the source browser tree.
	 * This method sets the tooltip displayed for the source browser tree.
	 */
	private void mouseMovedImpl(MouseEvent e) {

		// Get the item the mouse is pointing at.  It is possible that
		// They are pointing at no item; in that case, just quit now.
		int row = sourceTree.getRowForLocation(e.getX(), e.getY());
		if (row==-1)
			return;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)sourceTree.
							getPathForRow(row).getLastPathComponent();
		if (node==null)
			return;
		Object object = node.getUserObject();
		String text = null;

		// If it's a high-level node (e.g. "Methods (4)").
		if (object instanceof String) {
			text = (String)object;
		}

		// If it's a low-level node (function, variable, etc.).  Note
		// that this should always be a TagEntry, but we're just being
		// safe by checking here.
		else if (object instanceof TagEntry) {
			ExtendedTagEntry entry = (ExtendedTagEntry)object;
			text = mouseMovedTagEntry(entry);
		}

		// Set the tooltip text.
		treeRenderer.setToolTipText(text);

	}


	private String mouseMovedTagEntry(ExtendedTagEntry entry) {

		String text = null;

		// If we've already generated the (probably HTML) tooltip text,
		// use it.
		if (entry.cachedToolTipText!=null) {
			text = entry.cachedToolTipText;
		}

		// Create the tooltip text (HTML, or just text if only a line
		// number was found (such as #defines for C/C++).
		else {

			text = entry.getPlainTextPattern();

			// If we have a pattern, try to create an HTML tooltip.
			if (text!=null) {

				// To trim off the "regular expression" parts.
				text = text.substring(2, text.length()-2);

				if (getUseHTMLToolTips()) {
					// FIXME: Fix me to look line by line (as it's
					// guaranteed to be just a a line!).
					RTextEditorPane textArea = owner.getMainView().
												getCurrentTextArea();
					int pos = SearchEngine.getNextMatchPos(text,
							textArea.getText(), true, true, false);
					if (pos>-1) {
						try {
							int line = textArea.getLineOfOffset(pos);
							text = getHTMLForLine(line);
							entry.cachedToolTipText = text;
						} catch (BadLocationException ble) {
							owner.displayException(ble);
						}
					}
				}

			}

			// If all we have is a line number, use the tag's element's
			// name as the tooltip.
			else {
				text = entry.name.replaceAll("\t", " ");
			}

		}

		return text;

	}


	/**
	 * Called when the user downclicks in the ctags tree.
	 */
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}


	/**
	 * Called when the user up-clicks in the ctags tree.
	 */
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
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
	void setErrorMessage(String message) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(message);
		root.add(node);
		treeModel.setRoot(root);
	}


	/**
	 * Sets the root of the source browser tree.
	 *
	 * @param newRoot The new root.
	 */
	void setTreeRoot(TreeNode newRoot) {
		treeModel.setRoot(newRoot);
		UIUtil.expandAllNodes(sourceTree);
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
		if (sourceTree!=null) {
			treeRenderer = new SourceTreeCellRenderer();
			sourceTree.setCellRenderer(treeRenderer); // So it picks up new LnF's colors??
		}
		if (rightClickMenu!=null) {
			SwingUtilities.updateComponentTreeUI(rightClickMenu);
		}
	}


	/**
	 * Called when the user clicks on a tree node.  This method attempts to
	 * find the declaration of the selected item in the current text area, and
	 * if it is found (which it should be, unless it was removed, it is
	 * selected.
	 *
	 * @param e The tree selection event.
	 */
	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = sourceTree.getSelectionPath();
		// Must check for null, otherwise we get an exception on shutdown.
		if (path!=null) {
			Object object = ((DefaultMutableTreeNode)path.
								getLastPathComponent()).getUserObject();
			if (object instanceof TagEntry) {
				TagEntry entry = (TagEntry)object;
				RTextEditorPane editor=owner.getMainView().getCurrentTextArea();
				String pattern = entry.getPlainTextPattern();
				if (pattern!=null) {
					// FIXME: Fix me to look line by line (as it's
					// guaranteed to be just a a line!).
					int pos = SearchEngine.getNextMatchPos(pattern,
								editor.getText(), true, true, false);
					if (pos>-1) {
						editor.setCaretPosition(pos);
						editor.moveCaretPosition(pos+pattern.length());
						owner.setMessages(null, lineFoundText);
					}
					else {
						DockableWindow wind = getDockableWindow(getPluginName());
						UIManager.getLookAndFeel().provideErrorFeedback(wind);
						owner.setMessages(null, cantFindLineText);
					}

				}
				else { // Must use line number (used by C macros, for instance).
					Element map = editor.getDocument().getDefaultRootElement();
					Element line = map.getElement((int)entry.lineNumber-1);
					editor.setCaretPosition(line.getStartOffset());
					editor.moveCaretPosition(line.getEndOffset()-1);
				}
				editor.requestFocusInWindow();	// So we can see the highlighted line.
			}
		}
	}


	/**
	 * A tag entry with an extra field to cache the tooltip text.
	 */
	static class ExtendedTagEntry extends TagEntry {

		public String cachedToolTipText;

		public ExtendedTagEntry(String line) {
			super(line);
		}
	}


	/**
	 * Sets the appropriate icons for tree nodes (the '+' and '-' icons
	 * for nodes that can be expanded/contracted).
	 */
	class SourceTreeCellRenderer extends DefaultTreeCellRenderer {

		private Icon yellowBullet;
		private Icon greenBullet;

		public SourceTreeCellRenderer() {
			Class clazz = getClass();
			yellowBullet = new ImageIcon(clazz.getResource(YELLOW_BULLET));
			greenBullet = new ImageIcon(clazz.getResource(GREEN_BULLET));
		}

		public Component getTreeCellRendererComponent(JTree tree,
							Object value, boolean sel, boolean expanded,
							boolean leaf, int row, boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel,
									expanded, leaf, row, hasFocus);

			DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)value;
			Object obj = dmtn.getUserObject();

			if (obj instanceof String) { // As opposed to TagEntry.
				String str = (String)obj;
				int index = str.indexOf('(');
				if (index>-1) { // Not true if ctags not found.
					setText("<html>" + str.substring(0,index) + "<b>" +
						str.substring(index) + "</b></html>");
				}
			}

			// Determine what icon to use.
			Icon icon = null;
			if (dmtn instanceof GroupTreeNode) {
				GroupTreeNode gtn = (GroupTreeNode)dmtn;
				if (gtn.getIcon()!=null) {
					icon = gtn.getIcon();
				}
			}
			else {
				TreeNode parent = dmtn.getParent();
				if (parent instanceof GroupTreeNode) {
					GroupTreeNode gtn = (GroupTreeNode)parent;
					if (gtn.getIcon()!=null) {
						icon = gtn.getIcon();
					}
				}
			}
			if (icon==null) { // Languages without custom icons.
				if (leaf && value.toString().indexOf("(0)")==-1) {
					setIcon(greenBullet);
				}
				else {
					setIcon(row==0 ? fileIcon : yellowBullet);
				}
			}
			else {
				setIcon(icon);
			}

			return this;

		}

	}


	/**
	 * Toggles the visibility of this source browser.
	 */
	private class ViewAction extends AbstractAction {

		public ViewAction(ResourceBundle msg) {
			putValue(NAME, msg.getString("MenuItem.View"));
			putValue(MNEMONIC_KEY, new Integer(
					msg.getString("MenuItem.View.Mnemonic").charAt(0)));
			putValue(SHORT_DESCRIPTION, msg.getString("MenuItem.View.Desc"));
		}

		public void actionPerformed(ActionEvent e) {
			DockableWindow wind = getDockableWindow(getPluginName());
			wind.setActive(!wind.isActive());
		}

	}


}