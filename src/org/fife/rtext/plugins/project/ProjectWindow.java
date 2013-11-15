/*
 * 08/28/2012
 *
 * ProjectWindow.java - Dockable window displaying the project structure.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.fife.help.HelpDialog;
import org.fife.rtext.BottomLineBorder;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.plugins.project.model.Workspace;
import org.fife.rtext.plugins.project.tree.PhysicalLocationTreeNode;
import org.fife.rtext.plugins.project.tree.WorkspaceRootTreeNode;
import org.fife.rtext.plugins.project.tree.WorkspaceTree;
import org.fife.ui.MenuButton;
import org.fife.ui.WebLookAndFeelUtils;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowScrollPane;
import org.fife.ui.rtextfilechooser.RTextFileChooser;
import org.fife.ui.rtextfilechooser.filters.ExtensionFileFilter;


/**
 * The dockable window displaying project contents.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ProjectWindow extends DockableWindow {

	private ProjectPlugin plugin;
	private JLabel workspaceNameLabel;
	private WorkspaceTree tree;
	private JToolBar toolbar;


	public ProjectWindow(RText app, ProjectPlugin plugin, ProjectPluginPrefs prefs) {

		this.plugin = plugin;
		setDockableWindowName(Messages.getString("Project.DockableWindow.Title"));
		setIcon(plugin.getPluginIcon());
		setPosition(DockableWindow.LEFT);
		setLayout(new BorderLayout());

		toolbar = createToolBar(prefs);
		add(toolbar, BorderLayout.NORTH);

		tree = new WorkspaceTree(plugin, plugin.getWorkspace());
		RTextUtilities.removeTabbedPaneFocusTraversalKeyBindings(tree);
		setPrimaryComponent(tree);
		DockableWindowScrollPane sp = new DockableWindowScrollPane(tree);
		RTextUtilities.removeTabbedPaneFocusTraversalKeyBindings(sp);
		add(sp);

		setPosition(prefs.windowPosition);
		setActive(prefs.windowVisible);
		tree.setRootVisible(prefs.treeRootVisible);

	}


	private JToolBar createToolBar(ProjectPluginPrefs prefs) {

		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		workspaceNameLabel = new JLabel();
		refreshWorkspaceName();
		toolbar.add(workspaceNameLabel);

		toolbar.add(Box.createHorizontalGlue());

		MenuButton mb = new MenuButton(plugin.getPluginIcon());
		mb.addMenuItem(new NewWorkspaceAction());
		mb.addMenuItem(new OpenWorkspaceAction());
		mb.addSeparator();
		mb.addMenuItem(new JCheckBoxMenuItem(
				new ShowWorkspaceTreeNodeAction(prefs.treeRootVisible)));
		mb.addSeparator();
		mb.addMenuItem(new PluginHelpAction());
		mb.setMinimumSize(new Dimension(8, 8)); // Allow small resize
		toolbar.add(mb);

		toolbar.setMinimumSize(new Dimension(8, 8)); // Allow small resize
		toolbar.setBorder(new BottomLineBorder(3));

		WebLookAndFeelUtils.fixToolbar(toolbar);
		return toolbar;

	}


	/**
	 * Returns the tree view of the active workspace.
	 *
	 * @return The tree view.
	 */
	public WorkspaceTree getTree() {
		return tree;
	}


	/**
	 * Refreshes the workspace name label to reflect that of the current
	 * workspace.
	 */
	void refreshWorkspaceName() {
		workspaceNameLabel.setText(plugin.getWorkspace().getName());
		// Don't let it hog space in the tool bar.
		Dimension size = workspaceNameLabel.getMinimumSize();
		if (size!=null) {
			size.width = 1;
			workspaceNameLabel.setMinimumSize(size);
		}
	}


	/**
	 * Refreshes the workspace tree from the specified node down.
	 *
	 * @param fromNode The node to start the refreshing from.
	 */
	void refreshTree(TreeNode fromNode) {
		if (fromNode instanceof PhysicalLocationTreeNode) {
			((PhysicalLocationTreeNode)fromNode).handleRefresh();
		}
		else {
			DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
			if (fromNode!=null) {
				model.nodeStructureChanged(fromNode);
				//reload(fromNode);
				//tree.expandPath(new TreePath(fromNode));
			}
			else {
				model.reload();
				//UIUtil.expandAllNodes(tree);
			}
		}
	}


	@Override
	public void updateUI() {
		super.updateUI();
		if (toolbar!=null) {
			WebLookAndFeelUtils.fixToolbar(toolbar);
		}
	}


	/**
	 * A base class for actions dealing with workspaces.
	 */
	private abstract class AbstractWorkspaceAction extends BaseAction {

		protected AbstractWorkspaceAction(String key) {
			super(key);
		}

		protected boolean saveWorkspace(Workspace workspace) {

			RText rtext = plugin.getRText();
			boolean success = true;

			try {
				workspace.save();
			} catch (IOException ioe) {
				rtext.displayException(ioe);
				String msg = Messages.getString(
						"NewWorkspaceDialog.ErrorSavingWorkspace.ConfirmDiscard");
				String title = rtext.getString("ConfDialogTitle");
				int rc = JOptionPane.showConfirmDialog(rtext, msg, title,
						JOptionPane.YES_NO_OPTION);
				success = rc==JOptionPane.YES_OPTION;
			}

			return success;
		}

	}


	/**
	 * Opens the Help dialog to the information about this plugin.
	 */
	private class PluginHelpAction extends AbstractWorkspaceAction {

		public PluginHelpAction() {
			super("Action.Help");
		}

		public void actionPerformed(ActionEvent e) {
			RText rtext = plugin.getRText();
			HelpDialog helpDialog = rtext.getHelpDialog();
			// TODO: Open to documentation specific to the workspace plugin.
			helpDialog.setVisible(true);
		}

	}


	/**
	 * Action that creates and opens a new workspace.
	 */
	private class NewWorkspaceAction extends AbstractWorkspaceAction {

		public NewWorkspaceAction() {
			super("Action.NewWorkspace");
		}

		public void actionPerformed(ActionEvent e) {

			// Save the currently active workspace.
			RText rtext = plugin.getRText();
			Workspace workspace = plugin.getWorkspace();
			if (!saveWorkspace(workspace)) {
				return;
			}

			// Get the name for the new workspace.
			RenameDialog dialog = new RenameDialog(rtext, "Workspace",
					new WorkspaceRootTreeNode.WorkspaceNameChecker());
			Icon icon = WorkspaceRootTreeNode.getWorkspaceIcon();
			dialog.setDescription(icon, Messages.getString("NewWorkspaceDialog.Desc"));
			dialog.setTitle(Messages.getString("NewWorkspaceDialog.Title"));
			dialog.setName(null); // Move focus from desc SelectableLabel to field.
			dialog.setVisible(true);
			String newName = dialog.getName();

			if (newName!=null) {
				File newLoc = new File(plugin.getWorkspacesDir(), newName + ".xml");
				Workspace newWorkspace = new Workspace(plugin, newLoc);
				try {
					newWorkspace.save();
				} catch (IOException ioe) {
					// If saving the new workspace fails, don't switch to it.
					String msg = Messages.getString(
							"NewWorkspaceDialog.ErrorCreatingNewWorkspace");
					rtext.displayException(ioe, msg);
					return;
				}

				plugin.setWorkspace(newWorkspace);

			}

		}

	}


	/**
	 * Opens an existing workspace file.
	 */
	private class OpenWorkspaceAction extends AbstractWorkspaceAction {

		private RTextFileChooser chooser;

		public OpenWorkspaceAction() {
			super("Action.OpenWorkspace");
		}

		public void actionPerformed(ActionEvent e) {

			// Save the currently active workspace.
			RText rtext = plugin.getRText();
			Workspace workspace = plugin.getWorkspace();
			if (!saveWorkspace(workspace)) {
				return;
			}

			// Get the name of the workspace to open.
			RTextFileChooser chooser = getFileChooser();
			int rc = chooser.showOpenDialog(rtext);
			if (rc!=RTextFileChooser.APPROVE_OPTION) {
				return;
			}
			File wsFile = chooser.getSelectedFile();

			Workspace newWorkspace = null;
			try {
				newWorkspace = Workspace.load(plugin, wsFile);
			} catch (IOException ioe) {
				// If loading the new workspace fails, don't open it.
				String msg = Messages.getString(
						"OpenWorkspaceDialog.ErrorOpeningWorkspace");
				rtext.displayException(ioe, msg);
				return;
			}

			plugin.setWorkspace(newWorkspace);

		}

		private RTextFileChooser getFileChooser() {
			if (chooser==null) {
				chooser = new RTextFileChooser();
				String title = Messages.getString("OpenWorkspaceDialog.Title");
				chooser.setCustomTitle(title);
				ResourceBundle msg = ResourceBundle.getBundle(
						"org.fife.rtext.FileFilters");
				String desc = msg.getString("XML");
				ExtensionFileFilter filter = new ExtensionFileFilter(desc, "xml");
				chooser.setFileFilter(filter);
			}
			// Always start in the "workspaces" directory, to encourage the
			// user to use it.
			chooser.setCurrentDirectory(plugin.getWorkspacesDir());
			return chooser;
		}

	}


	/**
	 * Toggles the visibility of the workspace tree view's root node.
	 */
	private class ShowWorkspaceTreeNodeAction extends AbstractWorkspaceAction {

		public ShowWorkspaceTreeNodeAction(boolean selected) {
			super("Action.ShowWorkspaceTreeRootNode");
			setSelected(selected);
		}

		public void actionPerformed(ActionEvent e) {
			setSelected(plugin.toggleTreeRootVisible());
		}

		private void setSelected(boolean selected) {
			// TODO: Use actual Action static field when we no longer require
			// Java 1.4 & 1.5 compatibility.
			final String ACTION_SELECTED_KEY = "SwingSelectedKey";
			putValue(ACTION_SELECTED_KEY, Boolean.valueOf(selected));
		}

	}


}