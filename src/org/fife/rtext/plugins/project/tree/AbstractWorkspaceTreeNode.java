/*
 * 08/28/2012
 *
 * AbstractWorkspaceTreeNode.java - Base class for workspace tree nodes.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.fife.rsta.ac.java.DecoratableIcon;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.plugins.project.BaseAction;
import org.fife.rtext.plugins.project.LogicalFolderNameDialog;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.NewFolderDialog;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.model.FileProjectEntry;
import org.fife.rtext.plugins.project.model.FolderProjectEntry;
import org.fife.rtext.plugins.project.model.LogicalFolderProjectEntry;
import org.fife.rtext.plugins.project.model.ProjectEntry;
import org.fife.rtext.plugins.project.model.ProjectEntryParent;
import org.fife.ui.rtextfilechooser.Actions;
import org.fife.ui.rtextfilechooser.RTextFileChooser;


/**
 * All nodes in a workspace tree extend this class.
 *
 * @author Robert Futrell
 * @version 1.0
 */
abstract class AbstractWorkspaceTreeNode extends DefaultMutableTreeNode {

	protected ProjectPlugin plugin;
	private static RTextFileChooser chooser;


	public AbstractWorkspaceTreeNode(ProjectPlugin plugin) {
		this.plugin = plugin;
	}


	protected String escapeForHtml(String text) {
		return RTextUtilities.escapeForHTML(text, null);
	}


	/**
	 * Returns the name to display for this node in the workspace tree.
	 *
	 * @return The display name.
	 */
	public abstract String getDisplayName();


	/**
	 * Returns the file chooser to use when adding files to a project.
	 *
	 * @return The file chooser.
	 */
	private static RTextFileChooser getFileChooser() {
		if (chooser==null) {
			chooser = new RTextFileChooser();
			chooser.setShowHiddenFiles(true);
			chooser.setMultiSelectionEnabled(true);
		}
		return chooser;
	}


	/**
	 * Returns the icon for this tree node.
	 *
	 * @return The icon for this tree node.
	 */
	public abstract Icon getIcon();


	public abstract List getPopupActions();


	public abstract String getToolTipText();


	protected abstract void handleDelete();


	protected abstract void handleProperties();


	protected abstract void handleRename();


	/**
	 * Adds menu items to open in the system's default editor and viewer
	 * applications, if we're running in Java 6 or later.
	 *
	 * @param actions The action list to add to.
	 */
	protected void possiblyAddOpenInActions(List actions) {
		if (!RTextUtilities.isPreJava6()) {
			WorkspaceTree tree = plugin.getTree();
			JMenu openInMenu = new JMenu(Messages.getString("Action.OpenIn"));
			openInMenu.add(new Actions.SystemOpenAction(tree, "edit"));
			openInMenu.add(new Actions.SystemOpenAction(tree, "open"));
			actions.add(openInMenu);
			actions.add(null);
		}
	}


	public final String toString() {
		return getDisplayName();
	}


	/**
	 * Action for deleting a tree node.
	 */
	protected class DeleteAction extends BaseAction {

		public DeleteAction() {
			this(true);
		}

		public DeleteAction(boolean enabled) {
			super("Action.Delete");
			setEnabled(enabled);
		}

		public void actionPerformed(ActionEvent e) {
			// Run later to allow popup to hide
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					handleDelete();
				}
			});
		}

	}


	/**
	 * Action for a menu item that adds a file to this project.
	 */
	protected class AddFileAction extends BaseAction {

		private ProjectEntryParent parent;
		private MutableTreeNode node;

		public AddFileAction(ProjectEntryParent parent, MutableTreeNode node) {
			super("Action.NewFiles", "page_white_add.png");
			this.parent = parent;
			this.node = node;
		}

		public void actionPerformed(ActionEvent e) {
			RTextFileChooser chooser = getFileChooser();
			int rc = chooser.showOpenDialog(plugin.getRText());
			if (rc==RTextFileChooser.APPROVE_OPTION) {
				File[] toAdd = chooser.getSelectedFiles();
				for (int i=0; i<toAdd.length; i++) {
					ProjectEntry entry = new FileProjectEntry(parent, toAdd[i]);
					parent.addEntry(entry);
					FileProjectEntryTreeNode childNode =
							new FileProjectEntryTreeNode(plugin, entry);
					plugin.insertTreeNodeInto(childNode, node);
				}
			}
		}

	}


	/**
	 * Action for a menu item that adds a folder to this project.
	 */
	protected class AddFolderAction extends BaseAction {

		private ProjectEntryParent parent;
		private MutableTreeNode node;

		public AddFolderAction(ProjectEntryParent parent, MutableTreeNode node) {
			super("Action.NewFolder", "folder_add.png");
			this.parent = parent;
			this.node = node;
		}

		public void actionPerformed(ActionEvent e) {
			RText rtext = plugin.getRText();
			NewFolderDialog chooser = new NewFolderDialog(rtext);
			chooser.setVisible(true);
			String dir = chooser.getChosenDirectory();
			if (dir!=null) {
				File dirFile = new File(dir);
				FolderProjectEntry entry = new FolderProjectEntry(
						parent, dirFile);
				parent.addEntry(entry);
				FolderProjectEntryTreeNode childNode =
						new FolderProjectEntryTreeNode(plugin, entry);
				childNode.setFilterInfo(chooser.getFilterInfo());
				plugin.insertTreeNodeInto(childNode, node);
			}
		}

	}


	/**
	 * Action for a menu item that adds a logical folder to this project.
	 */
	protected class AddLogicalFolderAction extends BaseAction {

		private ProjectEntryParent parent;
		private MutableTreeNode node;

		public AddLogicalFolderAction(ProjectEntryParent parent,
				MutableTreeNode node) {

			super("Action.NewLogicalFolder", "folder_add.png");
			this.parent = parent;
			this.node = node;

			// Add the "logical folder" decoration to our icon.
			Icon icon = (Icon)getValue(SMALL_ICON);
			DecoratableIcon di = new DecoratableIcon(16, icon);
			URL decorationRes = RText.class.
					getResource("/org/fife/rsta/ui/search/lightbulb.png");
			di.addDecorationIcon(new ImageIcon(decorationRes));
			putValue(SMALL_ICON, di);

		}

		public void actionPerformed(ActionEvent e) {
			Frame parent = plugin.getRText();
			LogicalFolderNameDialog dialog = new LogicalFolderNameDialog(
					parent, null, new LogicalFolderNameChecker());
			dialog.setVisible(true);
			String name = dialog.getLogicalFolderName();
			if (name!=null) {
				LogicalFolderProjectEntry entry =
						new LogicalFolderProjectEntry(this.parent, name);
				this.parent.addEntry(entry);
				MutableTreeNode child =
						new LogicalFolderProjectEntryTreeNode(plugin, entry);
				plugin.insertTreeNodeInto(child, this.node);
			}
		}

	}


	/**
	 * Opens the selected file in RText.
	 */
	protected class OpenAction extends BaseAction {

		public OpenAction() {
			super("Action.Open");
			int enter = KeyEvent.VK_ENTER;
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(enter, 0));
		}

		public void actionPerformed(ActionEvent e) {
			WorkspaceTree tree = plugin.getTree();
			Object selected = tree.getLastSelectedPathComponent();
			File file = null;
			if (selected instanceof FileTreeNode) {
				file = ((FileTreeNode)selected).getFile();
			}
			else if (selected instanceof FileProjectEntryTreeNode) {
				FileProjectEntryTreeNode node = (FileProjectEntryTreeNode)selected;
				file = node.getFile();
				if (!file.isFile()) {
					promptForRemoval(node);
					return;
				}
			}
			plugin.getRText().openFile(file.getAbsolutePath());
		}

		private void promptForRemoval(FileProjectEntryTreeNode node) {
			String msg = Messages.getString("Prompt.FileDoesntExist.Remove",
					node.getFile().getAbsolutePath());
			RText rtext = plugin.getRText();
			String title = rtext.getString("ConfDialogTitle");
			int rc = JOptionPane.showConfirmDialog(rtext, msg, title,
					JOptionPane.YES_NO_OPTION);
			if (rc==JOptionPane.YES_OPTION) {
				TreeNode parent = node.getParent();
				node.removeFromParent();
				plugin.refreshTree(parent);
			}
		}

	}


	/**
	 * Action for getting the properties of a tree node.
	 */
	protected class PropertiesAction extends BaseAction {

		public PropertiesAction() {
			super("Action.Properties");
			int alt = InputEvent.ALT_MASK;
			KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, alt);
			putValue(ACCELERATOR_KEY, ks);
		}

		public void actionPerformed(ActionEvent e) {
			handleProperties();
		}

	}


	/**
	 * Refreshes the selected tree node.
	 */
	protected class RefreshAction extends BaseAction {

		public RefreshAction() {
			super("Action.Refresh");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		}

		public void actionPerformed(ActionEvent e) {
			WorkspaceTree tree = plugin.getTree();
			Object obj = tree.getLastSelectedPathComponent();
			if (obj instanceof PhysicalLocationTreeNode) {
				((PhysicalLocationTreeNode)obj).handleRefresh();
			}
			else {
				UIManager.getLookAndFeel().provideErrorFeedback(tree);
			}
		}

	}


	/**
	 * Action for renaming a tree node.
	 */
	protected class RenameAction extends BaseAction {

		public RenameAction() {
			super("Action.Rename");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		}

		public void actionPerformed(ActionEvent e) {
			handleRename();
		}

	}


}