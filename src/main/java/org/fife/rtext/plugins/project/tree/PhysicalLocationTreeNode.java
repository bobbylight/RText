package org.fife.rtext.plugins.project.tree;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.tree.TreeNode;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.BaseAction;
import org.fife.rtext.plugins.project.NewFileOrFolderDialog;
import org.fife.rtext.plugins.project.PopupContent;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.tree.FileTreeNode.FileNameChecker;


/**
 * Marker interface for a tree node representing a physical file or folder.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface PhysicalLocationTreeNode extends TreeNode {


	/**
	 * Returns the file or folder represented by this node.
	 *
	 * @return The file or folder.
	 */
	File getFile();


	/**
	 * Returns the plugin.
	 *
	 * @return The plugin.
	 */
	ProjectPlugin getPlugin();


	/**
	 * Refreshes the children of this node.
	 */
	void handleRefresh();


	/**
	 * Returns whether this folder tree node has not yet been populated
	 * (expanded).
	 *
	 * @return Whether this node has not yet been populated.
	 */
	boolean isNotPopulated();


	/**
	 * Refreshes the child nodes of this node.  The tree model will need to be
	 * reloaded after making this call.
	 */
	void refreshChildren();


	/**
	 * Dummy class signifying that this tree node has not yet had its children
	 * calculated.
	 */
	static class NotYetPopulatedChild extends AbstractWorkspaceTreeNode {

		public NotYetPopulatedChild(ProjectPlugin plugin) {
			super(plugin);
		}

		@Override
		public String getDisplayName() { return null; }

		@Override
		public Icon getIcon() {
			return null;
		}

		@Override
		public List<PopupContent> getPopupActions() { return null; }

		@Override
		public String getToolTipText() { return null; }

		@Override
		protected void handleDelete() {}

		@Override
		protected void handleProperties() {}

		protected void handleRefresh() {}

		@Override
		protected void handleRename() {}
		
	}


	/**
	 * Creates a new child file or folder whose parent is this node's file
	 * (which must be a directory).
	 */
	public static class NewFileOrFolderAction extends BaseAction {

		private PhysicalLocationTreeNode node;
		private boolean isFile;

		public NewFileOrFolderAction(PhysicalLocationTreeNode node,
				boolean isFile) {
			super(isFile ? "Action.NewFile": "Action.NewFolder",
				isFile ? "page_white_add.png" : "folder_add.png");
			this.node = node;
			this.isFile = isFile;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			File parent = node.getFile();
			if (parent==null || !parent.isDirectory()) {
				UIManager.getLookAndFeel().provideErrorFeedback(null);
				return;
			}

			RText rtext = node.getPlugin().getRText();
			NameChecker nameChecker = new FileNameChecker(parent, !isFile);
			NewFileOrFolderDialog dialog = new NewFileOrFolderDialog(rtext,
					!isFile, nameChecker);
			dialog.setFileName(null); // Force focus on the text field
			dialog.setVisible(true);

			String newName = dialog.getFileName();
			if (newName!=null) {
				boolean success = createFileOrFolderImpl(newName);
				if (!success) {
					UIManager.getLookAndFeel().provideErrorFeedback(rtext);
					return;
				}
				node.handleRefresh();
				if (dialog.getOpenOnCreate()) {
					rtext.openFile(createFileObject(newName).getAbsolutePath());
				}
			}
			
		}

		private File createFileObject(String name) {
			return new File(node.getFile(), name);
		}

		private boolean createFileOrFolderImpl(String name) {
			boolean success = false;
			if (isFile) {
				try {
					createFileObject(name).createNewFile();
					success = true;
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			else {
				success = createFileObject(name).mkdir();
			}
			return success;
		}

	}


}