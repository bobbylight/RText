/*
 * 08/28/2012
 *
 * WorkspaceTree.java - A tree representation of a workspace.
 * Copyright (C) 2012 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextActionInfo;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.PopupContent;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.PopupContent.PopupSubMenu;
import org.fife.rtext.plugins.project.model.Workspace;
import org.fife.ui.rtextfilechooser.FileSelector;


/**
 * A tree view of a workspace and its projects.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class WorkspaceTree extends JTree implements FileSelector {

	private final ProjectPlugin plugin;
	private final DefaultTreeModel model;
	private JPopupMenu popup;


	public WorkspaceTree(ProjectPlugin plugin, Workspace workspace) {

		this.plugin = plugin;
		WorkspaceRootTreeNode root = new WorkspaceRootTreeNode(plugin, workspace);
		model = new DefaultTreeModel(root);
		installActions();
		setModel(model);
		setWorkspace(workspace);
		setCellRenderer(WorkspaceTreeRenderer.create());

		// Add a needed extra bit of space at the top.
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(3, 3, 0, 3),
				getBorder()));

		ToolTipManager.sharedInstance().registerComponent(this);

	}


	private void configurePopupMenu(Object node) {

		if (node!=null) {

			if (popup==null) {
				popup = new JPopupMenu();
			}
			else {
				popup.removeAll();
			}

			AbstractWorkspaceTreeNode treeNode =
					(AbstractWorkspaceTreeNode)node;
			List<PopupContent> actions = treeNode.getPopupActions();
			for (PopupContent obj : actions) {
				if (obj instanceof PopupSubMenu) {
					popup.add((PopupSubMenu)obj);
				}
				else {
					Action action = (Action)obj;
					if (action==null) {
						popup.addSeparator();
					}
					else {
						popup.add(new JMenuItem(action));
					}
				}
			}

			ComponentOrientation o = ComponentOrientation.
					getOrientation(Locale.getDefault());
			popup.applyComponentOrientation(o);

		}

	}


	/**
	 * Copies the full path of the selected file to the clipboard.
	 *
	 * @return Whether the selected tree node was a file.  If this is
	 *         <code>null</code>, the clipboard was not updated.
	 */
	boolean copySelectedFilePathToClipboard() {
		File file = getSelectedFile();
		if (file != null && file.exists()) {
			StringSelection sel = new StringSelection(
					file.getAbsolutePath());
			Toolkit.getDefaultToolkit().getSystemClipboard().
					setContents(sel, sel);
			return true;
		}
		UIManager.getLookAndFeel().provideErrorFeedback(this);
		return false;
	}


	/**
	 * Displays the popup menu at the specified location.
	 *
	 * @param p The location at which to display the popup.
	 */
	private void displayPopupMenu(Point p) {

		Object selectedNode;

		// Select the tree node at the mouse position.
		TreePath path = getPathForLocation(p.x, p.y);
		if (path!=null) {
			setSelectionPath(path);
			scrollPathToVisible(path);
			selectedNode = getSelectionPath().getLastPathComponent();
		}
		else {
			clearSelection();
			selectedNode = model.getRoot();
		}

		// Configure and display it!
		configurePopupMenu(selectedNode);
		if (popup.getComponentCount()!=0) {
			popup.show(this, p.x, p.y);
		}

	}


	/**
	 * Expands all project nodes in the tree.
	 */
	private void expandAllProjects() {
		WorkspaceRootTreeNode root = (WorkspaceRootTreeNode)model.getRoot();
		for (int i=0; i<root.getChildCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					root.getChildAt(i);
			expandPath(new TreePath(node.getPath()));
		}
	}


	void findInFilesFromSelectedDir() {

		File dir = getSelectedFile();
		Object sel = getLastSelectedPathComponent();

		if (dir != null && dir.isDirectory()) {
			RText rtext = plugin.getApplication();
			rtext.getMainView().getFindInFilesDialog().setSearchIn(dir);
			// Note the ActionEvent isn't actually used; we're just doing
			// this for completeness.
			ActionEvent ae = new ActionEvent(this, 0, "ignored");
			rtext.getAction(RTextActionInfo.FIND_IN_FILES_ACTION).
					actionPerformed(ae);
		}

		else if (dir != null && !dir.exists() &&
			sel instanceof FileProjectEntryTreeNode fpetn) {
			// Directory was deleted out from under us
			promptForRemoval(fpetn);
		}

		else { // Directory changed to a file out from under us?
			UIManager.getLookAndFeel().provideErrorFeedback(this);
		}

	}


	/**
	 * Called when a node is about to be expanded.  This method is overridden
	 * so that the node that is being expanded will be populated with its
	 * subdirectories, if necessary.
	 */
	@Override
	public void fireTreeWillExpand(TreePath e) throws ExpandVetoException {

		super.fireTreeWillExpand(e);

		AbstractWorkspaceTreeNode awtn =
					(AbstractWorkspaceTreeNode)e.getLastPathComponent();

		// If the only child is the dummy one, we know we haven't populated
		// this node with true children yet.
		if (awtn instanceof PhysicalLocationTreeNode pltn) {
			if (pltn.isNotPopulated()) {
				Cursor orig = getCursor();
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					refreshChildren(pltn);
				} finally {
					setCursor(orig);
				}
			}
		}

	}


	/**
	 * Returns the file currently selected by the user.
	 *
	 * @return The file currently selected, or <code>null</code>
	 *         if no file is selected.
	 * @see #getSelectedFiles()
	 */
	@Override
	public File getSelectedFile() {
		TreePath path = getSelectionPath();
		if (path!=null) {
			Object comp = path.getLastPathComponent();
			if (comp instanceof FileTreeNode node) {
				return node.getFile();
			}
			else if (comp instanceof FileProjectEntryTreeNode node) {
				return node.getFile();
			}
		}
		return null;
	}


	/**
	 * Returns any selected files.
	 *
	 * @return The selected files, or a zero-length array if no files are
	 *         selected.
	 */
	@Override
	public File[] getSelectedFiles() {
		File file = getSelectedFile();
		if (file!=null) {
			return new File[] { file };
		}
		return new File[0];
	}


	@Override
	public String getToolTipText(MouseEvent e) {
		TreePath path = getPathForLocation(e.getX(), e.getY());
		if (path!=null) {
			Object last = path.getLastPathComponent();
			if (last instanceof AbstractWorkspaceTreeNode) {
				return ((AbstractWorkspaceTreeNode)last).getToolTipText();
			}
		}
		return null;
	}


	/**
	 * Opens the selected file in RText, if any.
	 */
	private void handleOpenFile() {
		File file = getSelectedFile();
		if (file!=null) {
			// We'll make sure the file exists and is a regular file
			// (as opposed to a directory) before attempting to open it.
			if (file.isFile()) {
				AbstractMainView mainView = plugin.getApplication().getMainView();
				mainView.openFile(file.getAbsolutePath(), null, true);
			}
			else if (getLastSelectedPathComponent() instanceof FileProjectEntryTreeNode node) {
				promptForRemoval(node);
			}
		}
	}


	/**
	 * Adds wrapper actions to the tree item-specific actions, so users can
	 * use keyboard shortcuts to activate them.
	 */
	private void installActions() {

		InputMap im = getInputMap();
		ActionMap am = getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "Rename");
		am.put("Rename", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object selected = getLastSelectedPathComponent();
				if (selected instanceof AbstractWorkspaceTreeNode) {
					((AbstractWorkspaceTreeNode)selected).handleRename();
				}
			}
		});

		int mods = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() |
				InputEvent.SHIFT_DOWN_MASK;
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, mods), "CopyPath");
		am.put("CopyPath", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copySelectedFilePathToClipboard();
			}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
		am.put("Delete", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object selected = getLastSelectedPathComponent();
				if (selected instanceof ProjectEntryTreeNode) {
					((ProjectEntryTreeNode)selected).handleRemove();
				}
			}
		});

		mods = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() |
				InputEvent.SHIFT_DOWN_MASK;
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, mods), "FindInFilesFH");
		am.put("FindInFilesFH", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				findInFilesFromSelectedDir();
			}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "Refresh");
		am.put("Refresh", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object selected = getLastSelectedPathComponent();
				if (selected instanceof PhysicalLocationTreeNode) {
					refreshChildren((PhysicalLocationTreeNode)selected);
				}
			}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
		am.put("Enter", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = getSelectedFile();
				if (file != null && file.isFile()) {
					plugin.getApplication().openFile(file);
				}
				else {
					UIManager.getLookAndFeel().provideErrorFeedback(null);
				}
			}
		});

		int alt = InputEvent.ALT_DOWN_MASK;
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, alt), "Properties");
		am.put("Properties", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object selected = getLastSelectedPathComponent();
				if (selected instanceof AbstractWorkspaceTreeNode) {
					((AbstractWorkspaceTreeNode)selected).handleProperties();
				}
			}
		});

	}


	/**
	 * Refreshes the display of a specific tree node.
	 *
	 * @param node The tree node to refresh.
	 */
	public void nodeChanged(TreeNode node) {
		model.nodeChanged(node);
	}


	/**
	 * Overridden to display our popup menu if necessary.
	 *
	 * @param e The mouse event.
	 */
	@Override
	protected void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
		if (e.isPopupTrigger()) {
			displayPopupMenu(e.getPoint());
		}
		else if (e.getID()==MouseEvent.MOUSE_CLICKED && e.getClickCount()==2) {
			handleOpenFile();
		}
	}


	/**
	 * Prompts the user that a file does not exist, and asks whether they want
	 * to remove the tree node for it.  The node is removed (and the project
	 * updated) if the user selects "yes."
	 *
	 * @param node The node to prompt about and possibly remove.
	 */
	public void promptForRemoval(FileProjectEntryTreeNode node) {
		File file = node.getFile();
		if (file.isDirectory()) {
			// FolderProjectEntryTreeNode extends FileProjectEntryTreeNode,
			// and careless callers might not realize this.
			return;
		}
		String msg = Messages.getString("Prompt.FileDoesntExist.Remove",
				node.getFile().getAbsolutePath());
		RText rtext = plugin.getApplication();
		String title = rtext.getString("ConfDialogTitle");
		int rc = JOptionPane.showConfirmDialog(rtext, msg, title,
				JOptionPane.YES_NO_OPTION);
		if (rc==JOptionPane.YES_OPTION) {
			model.removeNodeFromParent(node);
			node.entry.removeFromParent();
		}
	}


	/**
	 * Refreshes the children of the specified node (representing a directory)
	 * to accurately reflect the files inside of it.
	 *
	 * @param node The node whose children should be refreshed.
	 */
	void refreshChildren(PhysicalLocationTreeNode node) {
		node.refreshChildren();
		model.reload(node);
	}


	/**
	 * Sets the workspace to display.
	 *
	 * @param workspace The new workspace to display.
	 */
	public void setWorkspace(Workspace workspace) {
		WorkspaceTreeRootCreator creator = new WorkspaceTreeRootCreator(plugin);
		workspace.accept(creator);
		model.setRoot(creator.getRoot());
		expandAllProjects();
	}


	/**
	 * Overridden to also update this tree's popup menu.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		if (popup!=null) {
			SwingUtilities.updateComponentTreeUI(popup);
		}
		// Explicitly set DefaultTreeCellRenderers cache fonts, colors, etc.,
		// and don't get updated by the JTree on LaF updates, so we must do
		// so ourselves.
		setCellRenderer(WorkspaceTreeRenderer.create());
	}


}
