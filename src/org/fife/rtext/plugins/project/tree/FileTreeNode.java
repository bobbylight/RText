/*
 * 09/03/2012
 *
 * FileTreeNode.java - A tree node for regular files and directories.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.TreeNode;

import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.RenameDialog;
import org.fife.rtext.plugins.project.model.FolderFilterInfo;
import org.fife.ui.rtextfilechooser.extras.FileIOExtras;


/**
 * A tree node for regular files and directories.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FileTreeNode extends AbstractWorkspaceTreeNode
		implements PhysicalLocationTreeNode {

	/**
	 * Only used if this tree node represents a folder.
	 */
	private FolderFilterInfo filterInfo;
	private Icon icon;


	public FileTreeNode(ProjectPlugin plugin, File file) {
		super(plugin);
		setUserObject(file);
		if (file.isDirectory()) {
			add(new NotYetPopulatedChild(plugin));
			filterInfo = new FolderFilterInfo();
		}
		icon = FileSystemView.getFileSystemView().getSystemIcon(file);
	}


	private FileTreeNode createFileTreeNode(File file) {
		FileTreeNode ftn = new FileTreeNode(plugin, file);
		ftn.setFilterInfo(filterInfo);
		return ftn;
	}


	/**
	 * Does any filtering and sorting of an array of files so that they will
	 * be displayed properly.
	 *
	 * @param files The array of files to filter and sort.
	 * @return The filtered and sorted array of files.
	 */
	private File[] filterAndSort(File[] files) {

		int num = files.length;
		ArrayList dirList = new ArrayList();
		ArrayList fileList = new ArrayList();

		// First, separate the directories from regular files so we can
		// sort them individually.  This part could be made more compact,
		// but it isn't just for a tad more speed.
		for (int i=0; i<num; i++) {
			boolean isDir = files[i].isDirectory();
			if (filterInfo!=null && filterInfo.isAllowed(files[i], isDir)) {
				if (isDir)
					dirList.add(files[i]);
				else
					fileList.add(files[i]);
			}
		}

		// On Windows and OS X, comparison is case-insensitive.
		Comparator c = null;
		String os = System.getProperty("os.name");
		boolean isOSX = os!=null ? os.toLowerCase().indexOf("os x")>-1 : false;
		if (File.separatorChar=='\\' || isOSX) {
			c = new Comparator() {
				public int compare(Object o1, Object o2) {
					File f1 = (File)o1;
					File f2 = (File)o2;
					return f1.getName().compareToIgnoreCase(f2.getName());
				}
			};
		}

		Collections.sort(fileList, c);
		Collections.sort(dirList, c);
		dirList.addAll(fileList);

		File[] fileArray = new File[dirList.size()];
		return (File[])dirList.toArray(fileArray);

	}


	public File getFile() {
		return (File)getUserObject();
	}


	/**
	 * Returns a string representation of the specified array of filters.
	 *
	 * @param filters The array of filters, may be <code>null</code>.
	 * @return A string representation of the filters.
	 */
	public static final String getFilterString(String[] filters) {
		return getFilterString(filters, "&nbsp;");
	}


	/**
	 * Returns a string representation of the specified array of filters.
	 *
	 * @param filters The array of filters, may be <code>null</code>.
	 * @param def The value to display if the filter array is <code>null</code>.
	 * @return A string representation of the filters.
	 */
	public static final String getFilterString(String[] filters, String def) {
		return filters==null ? def : RTextUtilities.join(filters);
	}


	public List getPopupActions() {
		List actions = new ArrayList();
		if (!getFile().isDirectory()) {
			actions.add(new OpenAction());
		}
		possiblyAddOpenInActions(actions);
		actions.add(new DeleteAction());
		actions.add(null);
		actions.add(new RenameAction());
		actions.add(null);
		if (getFile().isDirectory()) {
			actions.add(new RefreshAction());
			actions.add(null);
		}
		actions.add(new PropertiesAction());
		return actions;
	}


	public Icon getIcon() {
		return icon;
	}


	public String getToolTipText() {
		File file = getFile();
		if (file.isFile()) {
			return Messages.getString("ProjectPlugin.ToolTip.File",
					getFile().getAbsolutePath());
		}
		else if (file.isDirectory()) {
			return Messages.getString("ProjectPlugin.ToolTip.Folder",
				new String[] { file.getAbsolutePath(),
					getFilterString(filterInfo.getAllowedFileFilters(), "*"),
					getFilterString(filterInfo.getDisallowedFileFilters()),
					getFilterString(filterInfo.getDisallowedDirectories())
				}
			);
		}
		return null; // File does not exist
	}


	protected void handleDelete() {

		final boolean hard = false;
		File file = getFile();
		File[] files = new File[1];
		files[0] = file;

		FileIOExtras extras = FileIOExtras.getInstance();
		if (!hard && extras!=null) {
			if (handleDeleteNative(files, plugin)) {
				plugin.refreshTree(getParent());
			}
			return;
		}

		String text = Messages.getString("Action.DeleteFile.Confirm",
				file.getName());
		RText rtext = plugin.getRText();
		String title = rtext.getString("ConfDialogTitle");

		int rc = JOptionPane.showConfirmDialog(rtext, text, title,
				JOptionPane.YES_NO_OPTION);
		if (rc==JOptionPane.YES_OPTION) {
			if (!file.delete()) {
				text = Messages.getString("ProjectPlugin.Error.DeletingFile",
						file.getName());
				title = rtext.getString("ErrorDialogTitle");
				JOptionPane.showMessageDialog(rtext, text, title,
						JOptionPane.ERROR_MESSAGE);
			}
			plugin.refreshTree(getParent());
		}

	}


	public static final boolean handleDeleteNative(File[] files,
										ProjectPlugin plugin) {
		FileIOExtras extras = FileIOExtras.getInstance();
		RText rtext = plugin.getRText();
		if (!extras.moveToRecycleBin(rtext, files, true, true)) {
			UIManager.getLookAndFeel().provideErrorFeedback(rtext);
			return false;
		}
		return true;
	}


	protected void handleProperties() {
		// TODO Auto-generated method stub
	}


	public void handleRefresh() {
		plugin.getTree().refreshChildren(this);
	}


	protected void handleRename() {
		RText rtext = plugin.getRText();
		boolean directory = getFile().isDirectory();
		String key = "ProjectPlugin." + (directory ? "Folder" : "File");
		String type = Messages.getString(key);
		RenameDialog dialog = new RenameDialog(rtext, type,
				new FileNameChecker(getFile().getParentFile(), directory));
		dialog.setName(getFile().getName());
		dialog.setVisible(true);
		String newName = dialog.getName();
		if (newName!=null) {
			File old = getFile();
			File newFile = new File(old.getParentFile(), newName);
			boolean success = old.renameTo(newFile);
			if (success) {
				plugin.refreshTree(getParent());
			}
			else {
				UIManager.getLookAndFeel().provideErrorFeedback(null);
			}
		}
	}


	public boolean isNotPopulated() {
		int childCount = getChildCount();
		return childCount==1 && (getFirstChild() instanceof NotYetPopulatedChild);
	}


	public void refreshChildren() {
		File file = getFile();
		if (file.isDirectory()) { // Should always be true
			removeAllChildren();
			FileSystemView fsv = FileSystemView.getFileSystemView();
			File[] children = fsv.getFiles(file, false);
			File[] filteredChildren = filterAndSort(children);
			for (int i=0; i<filteredChildren.length; i++) {
				add(createFileTreeNode(filteredChildren[i]));
			}
		}
	}


	public void setFilterInfo(FolderFilterInfo info) {
		this.filterInfo = info;
		for (int i=0; i<getChildCount(); i++) {
			TreeNode child = getChildAt(i);
			if (child instanceof FileTreeNode) { // i.e. not NotYetPopulated...
				FileTreeNode ftn = (FileTreeNode)child;
				ftn.setFilterInfo(filterInfo);
			}
		}
		handleRefresh();
	}


	public String toString() {
		return getFile().getName();
	}
	

	/**
	 * Ensures that proposed file names are valid.
	 */
	private static class FileNameChecker implements NameChecker {

		private File parentDir;
		private boolean folder;

		public FileNameChecker(File parentDir, boolean folder) {
			this.parentDir = parentDir;
			this.folder = folder;
		}

		public String isValid(String text) {
			int length = text.length();
			if (length==0) {
				return "empty";
			}
			for (int i=0; i<length; i++) {
				char ch = text.charAt(i);
				if (!(Character.isLetterOrDigit(ch) || ch=='_' || ch=='-' ||
						ch==' ' || ch=='.')) {
					return folder ? "invalidFolderName" : "invalidFileName";
				}
			}
			if (text.endsWith(".")) {
				return folder ? "folderNameCannotEndWithDot" :
					"fileNameCannotEndWithDot";
			}
			if (parentDir!=null) {
				File test = new File(parentDir, text);
				if (test.exists()) {
					return "alreadyExists";
				}
			}
			return null;
		}

	}


}