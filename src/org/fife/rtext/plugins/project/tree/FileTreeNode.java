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

import java.awt.Window;
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
import org.fife.rtext.plugins.project.PopupContent;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.RenameDialog;
import org.fife.rtext.plugins.project.model.FolderFilterInfo;
import org.fife.ui.rtextfilechooser.FileDisplayNames;
import org.fife.ui.rtextfilechooser.Utilities;
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


	/**
	 * Filters and sorts the list of child files, and adds child tree nodes
	 * for the children not filtered out.
	 *
	 * @param files The array of files to filter, and add (sorted) child tree
	 *        nodes for those not filtered out.
	 */
	private void addChildrenFilteredAndSorted(File[] files) {

		int num = files.length;
		ArrayList<File> dirList = new ArrayList<File>();
		ArrayList<File> fileList = new ArrayList<File>();

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
		Comparator<File> c = null;
		String os = System.getProperty("os.name");
		boolean isOSX = os!=null ? os.toLowerCase().indexOf("os x")>-1 : false;
		if (File.separatorChar=='\\' || isOSX) {
			c = new Comparator<File>() {
				public int compare(File f1, File f2) {
					return f1.getName().compareToIgnoreCase(f2.getName());
				}
			};
		}

		Collections.sort(dirList, c);
		for (File dir : dirList) {
			add(createFileTreeNode(dir, true));
		}
		Collections.sort(fileList, c);
		for (File file : fileList) {
			add(createFileTreeNode(file, false));
		}

	}


	private FileTreeNode createFileTreeNode(File file, boolean folder) {
		FileTreeNode ftn = new FileTreeNode(plugin, file);
		if (folder) {
			ftn.setFilterInfo(filterInfo);
		}
		return ftn;
	}


	@Override
	public String getDisplayName() {
		return FileDisplayNames.get().getName(getFile());
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
		return getFilterString(filters, "");
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


	@Override
	public List<PopupContent> getPopupActions() {
		List<PopupContent> actions = new ArrayList<PopupContent>();
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
		actions.add(new PropertiesAction(true));
		return actions;
	}


	@Override
	public Icon getIcon() {
		return icon;
	}


	@Override
	public String getToolTipText() {
		File file = getFile();
		if (file.isFile()) {
			return Messages.getString("ProjectPlugin.ToolTip.File",
					getFile().getAbsolutePath(),
					Utilities.getFileSizeStringFor(file));
		}
		else if (file.isDirectory()) {
			return Messages.getString("ProjectPlugin.ToolTip.Folder",
				new String[] { file.getAbsolutePath(),
					getFilterString(filterInfo.getAllowedFileFilters(), "*"),
					getFilterString(filterInfo.getHiddenFileFilters()),
					getFilterString(filterInfo.getHiddenFolderFilters())
				}
			);
		}
		return null; // File does not exist
	}


	@Override
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


	@Override
	protected void handleProperties() {
		handleProperties(plugin.getRText(), getFile());
	}


	static void handleProperties(Window parent, File file) {
		FileIOExtras extras = FileIOExtras.getInstance();
		if (extras!=null) {
			extras.showFilePropertiesDialog(parent, file);
		}
		else {
			UIManager.getLookAndFeel().provideErrorFeedback(null);
		}
	}


	public void handleRefresh() {
		plugin.getTree().refreshChildren(this);
	}


	@Override
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
				setUserObject(newFile);
				icon = FileSystemView.getFileSystemView().getSystemIcon(newFile);
				plugin.getTree().nodeChanged(this);
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
			addChildrenFilteredAndSorted(children);
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
		if (!isNotPopulated()) {
			handleRefresh();
		}
	}


	/**
	 * Ensures that proposed file names are valid.
	 */
	public static class FileNameChecker implements NameChecker {

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