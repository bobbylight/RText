/*
 * 08/28/2012
 *
 * ProjectPlugin.java - Plugin adding "project" support to RText.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.fife.rtext.RText;
import org.fife.rtext.RTextMenuBar;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.plugins.project.model.Workspace;
import org.fife.rtext.plugins.project.tree.AbstractWorkspaceTreeNode;
import org.fife.rtext.plugins.project.tree.WorkspaceTree;
import org.fife.ui.ImageTranscodingUtil;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.GUIPlugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.AppAction;


/**
 * A plugin that adds a very simple "project" system into RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ProjectPlugin extends GUIPlugin {

	/**
	 * System property that, if defined, overrides the workspace initially
	 * opened by this property.  Should be the full path to the workspace XML
	 * file.
	 */
	private static final String PROPERTY_INITIAL_WORKSPACE = "workspace.override";

	private final RText rtext;
	private Icon darkThemeIcon;
	private Icon lightThemeIcon;
	private Workspace workspace;
	private ProjectPluginOptionPanel optionPanel;

	private static final String VIEW_CONSOLE_ACTION	= "viewProjectWindowAction";
	private static final String DOCKABLE_WINDOW_PROJECTS = "projectsDockableWindow";
	private static final String VERSION_STRING = "3.0.3";


	public ProjectPlugin(AbstractPluggableGUIApplication<?> app) {

		rtext = (RText)app; // Needed now in case of XML errors below.

		loadIcons();
		ProjectPluginPrefs prefs = loadPrefs();

		AppAction<RText> a = new ViewProjectsAction((RText)app, this);
		a.setAccelerator(prefs.windowVisibilityAccelerator);
		app.addAction(VIEW_CONSOLE_ACTION, a);

		String workspaceOverride = System.getProperty(PROPERTY_INITIAL_WORKSPACE);
		if (workspaceOverride!=null) {
			File wsOverrideFile = new File(workspaceOverride);
			if (wsOverrideFile.isFile()) {
				prefs.openWorkspaceName = wsOverrideFile.getAbsolutePath();
			}
		}
		loadInitialWorkspace(prefs.openWorkspaceName);

		// Window MUST always be created for preference saving on shutdown
		ProjectWindow window = new ProjectWindow(rtext, this, prefs);
		ComponentOrientation o = ComponentOrientation.
				getOrientation(Locale.getDefault());
		window.applyComponentOrientation(o);
		putDockableWindow(DOCKABLE_WINDOW_PROJECTS, window);

		getTree().setExpandsSelectedPaths(true);

	}


	/**
	 * Returns the dockable window for this plugin.
	 *
	 * @return This plugin's dockable window.
	 */
	public ProjectWindow getDockableWindow() {
		return (ProjectWindow)getDockableWindow(DOCKABLE_WINDOW_PROJECTS);
	}


	@Override
	public PluginOptionsDialogPanel<ProjectPlugin> getOptionsDialogPanel() {
		if (optionPanel==null) {
			optionPanel = new ProjectPluginOptionPanel(this);
		}
		return optionPanel;
	}


	@Override
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	@Override
	public Icon getPluginIcon(boolean darkLookAndFeel) {
		return darkLookAndFeel ? darkThemeIcon : lightThemeIcon;
	}


	@Override
	public String getPluginName() {
		return Messages.getString("ProjectPlugin.Name");
	}


	@Override
	public String getPluginVersion() {
		return VERSION_STRING;
	}


	/**
	 * Returns the file containing the preferences for this plugin.
	 *
	 * @return The preferences file for this plugin.
	 */
	private static File getPrefsFile() {
		return new File(RTextUtilities.getPreferencesDirectory(),
				"projects.properties");
	}


	/**
	 * Returns the parent RText instance.
	 *
	 * @return The parent RText instance.
	 */
	public RText getRText() {
		return rtext;
	}


	/**
	 * Returns the tree view of the active workspace.
	 *
	 * @return The tree view.
	 */
	public WorkspaceTree getTree() {
		return getDockableWindow().getTree();
	}


	/**
	 * Returns the directory that workspaces are saved to.
	 *
	 * @return The directory.
	 */
	public File getWorkspacesDir() {
		return new File(RTextUtilities.getPreferencesDirectory(), "workspaces");
	}


	/**
	 * Returns the active workspace.
	 *
	 * @return The active workspace, or <code>null</code> if none.
	 */
	public Workspace getWorkspace() {
		return workspace;
	}


	/**
	 * Adds a new child to the parent tree node.
	 *
	 * @param child The new child node.
	 * @param parent The parent node.
	 * @see #insertTreeNodeInto(MutableTreeNode, MutableTreeNode, int)
	 */
	public void insertTreeNodeInto(MutableTreeNode child,
			MutableTreeNode parent) {
		insertTreeNodeInto(child, parent, parent.getChildCount());
	}


	/**
	 * Adds a new child to the parent tree node.
	 *
	 * @param child The new child node.
	 * @param parent The parent node.
	 * @param index The index at which to insert the child node.
	 * @see #insertTreeNodeInto(MutableTreeNode, MutableTreeNode)
	 */
	private void insertTreeNodeInto(final MutableTreeNode child,
									MutableTreeNode parent, int index) {
		final DefaultTreeModel model = (DefaultTreeModel)getTree().getModel();
		model.insertNodeInto(child, parent, index);
		final TreePath path = new TreePath(model.getPathToRoot(parent));
		getTree().expandPath(path);

		SwingUtilities.invokeLater(() -> getTree().setSelectionPath(new TreePath(model.getPathToRoot(child))));
	}


	@Override
	public void install(AbstractPluggableGUIApplication<?> app) {

		File workspaceDir = getWorkspacesDir();
		if (!workspaceDir.isDirectory()) {
			workspaceDir.mkdirs();
		}

		RTextMenuBar mb = (RTextMenuBar)app.getJMenuBar();

		// Add an item to the "View" menu to toggle console visibility
		final JMenu menu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		Action a = rtext.getAction(VIEW_CONSOLE_ACTION);
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(a);
		item.setToolTipText(null);
		item.applyComponentOrientation(app.getComponentOrientation());
		menu.add(item);
		JPopupMenu popup = menu.getPopupMenu();
		popup.pack();
		// Only needed for pre-1.6 support
		popup.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				item.setSelected(isProjectWindowVisible());
			}
		});

	}


	/**
	 * Returns whether the console window is visible.
	 *
	 * @return Whether the console window is visible.
	 * @see #setProjectWindowVisible(boolean)
	 */
	boolean isProjectWindowVisible() {
		return getDockableWindow().isActive();
	}


	private void loadIcons() {

		try {

			lightThemeIcon = new ImageIcon(getClass().getResource("application_side_list.png"));

			Image darkThemeImage = ImageTranscodingUtil.rasterize("projectStructure dark",
				getClass().getResourceAsStream("projectStructure_dark.svg"), 16, 16);
			darkThemeIcon = new ImageIcon(darkThemeImage);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Loads the initial workspace as previously saved in the preferences.
	 *
	 * @param workspaceName The name of the workspace to load.
	 */
	private void loadInitialWorkspace(String workspaceName) {
		if (workspaceName!=null) {
			File workspaceFile = new File(workspaceName);
			if (workspaceFile.isFile()) {
				// Always true unless user manually removed it
				try {
					workspace = Workspace.load(this, workspaceFile);
				} catch (IOException ioe) {
					rtext.displayException(ioe);
				}
			}
		}
		if (workspace==null) {
			File defaultWorkspace = new File(getWorkspacesDir(), "Workspace.xml");
			workspace = new Workspace(this, defaultWorkspace);
		}
	}


	/**
	 * Loads saved preferences for this plugin.  If this is the first
	 * time through, default values will be returned.
	 *
	 * @return The preferences.
	 */
	private ProjectPluginPrefs loadPrefs() {
		ProjectPluginPrefs prefs = new ProjectPluginPrefs();
		File prefsFile = getPrefsFile();
		if (prefsFile.isFile()) {
			try {
				prefs.load(prefsFile);
			} catch (IOException ioe) {
				rtext.displayException(ioe);
				// (Some) defaults will be used
			}
		}
		return prefs;
	}


	/**
	 * Moves a tree node down in its parent's list of children, if possible.
	 *
	 * @param node The node to move down.
	 * @see #moveTreeNodeUp(AbstractWorkspaceTreeNode)
	 */
	public void moveTreeNodeDown(AbstractWorkspaceTreeNode node) {
		DefaultTreeModel model = (DefaultTreeModel)getTree().getModel();
		MutableTreeNode parent = (MutableTreeNode)node.getParent();
		int index = parent.getIndex(node);
		if (index<parent.getChildCount()-1) {
			model.removeNodeFromParent(node);
			insertTreeNodeInto(node, parent, index+1);
			node.moveProjectEntityDown(false);
		}
		else {
			UIManager.getLookAndFeel().provideErrorFeedback(rtext);
		}
	}


	/**
	 * Moves a tree node to the bottom of its parent's list of children, if
	 * possible.
	 *
	 * @param node The node to move down.
	 * @see #moveTreeNodeToTop(AbstractWorkspaceTreeNode)
	 */
	public void moveTreeNodeToBottom(AbstractWorkspaceTreeNode node) {
		DefaultTreeModel model = (DefaultTreeModel)getTree().getModel();
		MutableTreeNode parent = (MutableTreeNode)node.getParent();
		int index = parent.getIndex(node);
		if (index<parent.getChildCount()-1) {
			model.removeNodeFromParent(node);
			insertTreeNodeInto(node, parent, parent.getChildCount());
			node.moveProjectEntityDown(true);
		}
		else {
			UIManager.getLookAndFeel().provideErrorFeedback(rtext);
		}
	}


	/**
	 * Moves a tree node to the top of its parent's list of children, if
	 * possible.
	 *
	 * @param node The node to move up.
	 * @see #moveTreeNodeToBottom(AbstractWorkspaceTreeNode)
	 */
	public void moveTreeNodeToTop(AbstractWorkspaceTreeNode node) {
		DefaultTreeModel model = (DefaultTreeModel)getTree().getModel();
		MutableTreeNode parent = (MutableTreeNode)node.getParent();
		int index = parent.getIndex(node);
		if (index>0) {
			model.removeNodeFromParent(node);
			insertTreeNodeInto(node, parent, 0);
			node.moveProjectEntityUp(true);
		}
		else {
			UIManager.getLookAndFeel().provideErrorFeedback(rtext);
		}
	}


	/**
	 * Moves a tree node up in its parent's list of children, if possible.
	 *
	 * @param node The node to move up.
	 * @see #moveTreeNodeDown(AbstractWorkspaceTreeNode)
	 */
	public void moveTreeNodeUp(AbstractWorkspaceTreeNode node) {
		DefaultTreeModel model = (DefaultTreeModel)getTree().getModel();
		MutableTreeNode parent = (MutableTreeNode)node.getParent();
		int index = parent.getIndex(node);
		if (index>0) {
			model.removeNodeFromParent(node);
			insertTreeNodeInto(node, parent, index-1);
			node.moveProjectEntityUp(false);
		}
		else {
			UIManager.getLookAndFeel().provideErrorFeedback(rtext);
		}
	}


	/**
	 * Refreshes the workspace name displayed in the dockable window.
	 */
	public void refreshWorkspaceName() {
		getDockableWindow().refreshWorkspaceName();
	}


	/**
	 * Refreshes the workspace tree from the specified node down.
	 *
	 * @param fromNode The node to start the refreshing from.
	 */
	public void refreshTree(TreeNode fromNode) {
		getDockableWindow().refreshTree(fromNode);
	}


	@Override
	public void savePreferences() {

		ProjectWindow window = getDockableWindow();

		ProjectPluginPrefs prefs = new ProjectPluginPrefs();
		prefs.windowPosition = window.getPosition();
		AppAction<?> a = (AppAction<?>)rtext.getAction(VIEW_CONSOLE_ACTION);
		prefs.windowVisibilityAccelerator = a.getAccelerator();
		prefs.windowVisible = window.isActive();
		prefs.openWorkspaceName = workspace==null ? null :
			workspace.getFileFullPath();
		prefs.treeRootVisible = getTree().isRootVisible();

		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			rtext.displayException(ioe);
		}

		if (workspace!=null) {
			try {
				workspace.save();
			} catch (IOException ioe) {
				rtext.displayException(ioe);
			}
		}

	}


	/**
	 * Sets the visibility of the console window.
	 *
	 * @param visible Whether the window should be visible.
	 * @see #isProjectWindowVisible()
	 */
	void setProjectWindowVisible(boolean visible) {
		if (visible!=isProjectWindowVisible()) {
			getDockableWindow().setActive(visible);
		}
	}


	/**
	 * Sets the active workspace.
	 *
	 * @param workspace The new active workspace.
	 */
	void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		getTree().setWorkspace(workspace);
		refreshWorkspaceName();
	}


	/**
	 * Toggles whether the workspace tree's root node is visible.
	 *
	 * @return Whether the root node is visible after this call.
	 */
	boolean toggleTreeRootVisible() {
		boolean newValue = !getTree().isRootVisible();
		getTree().setRootVisible(newValue);
		return newValue;
	}


	@Override
	public boolean uninstall() {
		return false;
	}


}
