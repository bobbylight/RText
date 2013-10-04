/*
 * 09/03/2012
 *
 * FolderProjectEntryTreeNode.java - Tree node for folder project entries.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.TreeNode;

import org.fife.rtext.plugins.project.BaseAction;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.NewFolderDialog;
import org.fife.rtext.plugins.project.PopupContent;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.model.FolderFilterInfo;
import org.fife.rtext.plugins.project.model.FolderProjectEntry;


/**
 * The tree node used for folder project entries.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FolderProjectEntryTreeNode extends FileProjectEntryTreeNode
		implements PhysicalLocationTreeNode {


	public FolderProjectEntryTreeNode(ProjectPlugin plugin,
			FolderProjectEntry entry) {
		super(plugin, entry);
		add(new NotYetPopulatedChild(plugin));
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
		FolderFilterInfo filterInfo = getFilterInfo();

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
			ftn.setFilterInfo(getFilterInfo());
		}
		return ftn;
	}


	@Override
	public String getDisplayName() {
		return ((FolderProjectEntry)entry).getDisplayName();
	}


	@Override
	protected NameChecker createNameChecker() {
		return new FolderProjectEntryNameChecker();
	}


	public FolderFilterInfo getFilterInfo() {
		return ((FolderProjectEntry)entry).getFilterInfo();
	}


	/**
	 * Overridden to add a menu item to configure the filters for this
	 * project entry.
	 */
	@Override
	public List<PopupContent> getPopupActions() {
		List<PopupContent> actions = super.getPopupActions();
		actions.add(actions.size()-1, new ConfigureFiltersAction());
		return actions;
	}


	@Override
	public String getToolTipText() {
		File file = getFile();
		FolderFilterInfo filterInfo = getFilterInfo();
		return Messages.getString("ProjectPlugin.ToolTip.FolderProjectEntry",
			new String[] { escapeForHtml(getDisplayName()),
				escapeForHtml(file.getAbsolutePath()),
				escapeForHtml(FileTreeNode.getFilterString(filterInfo.getAllowedFileFilters(), "*")),
				escapeForHtml(FileTreeNode.getFilterString(filterInfo.getHiddenFileFilters())),
				escapeForHtml(FileTreeNode.getFilterString(filterInfo.getHiddenFolderFilters()))
			}
		);
	}


	public void handleRefresh() {
		plugin.getTree().refreshChildren(this);
	}


	@Override
	protected void handleRenameImpl(String newName) {
		setDisplayName(newName);
		plugin.getTree().nodeChanged(this);
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


	public void setDisplayName(String displayName) {
		((FolderProjectEntry)entry).setDisplayName(displayName);
	}


	public void setFilterInfo(FolderFilterInfo info) {
		((FolderProjectEntry)entry).setFilterInfo(info);
		for (int i=0; i<getChildCount(); i++) {
			TreeNode child = getChildAt(i);
			if (child instanceof FileTreeNode) { // i.e. not NotYetPopulated...
				FileTreeNode ftn = (FileTreeNode)child;
				ftn.setFilterInfo(info);
			}
		}
		if (!isNotPopulated()) {
			handleRefresh();
		}
	}


	/**
	 * Configures the filters for this project entry.
	 */
	private class ConfigureFiltersAction extends BaseAction {

		public ConfigureFiltersAction() {
			super("Action.ConfigureFilters");
		}

		public void actionPerformed(ActionEvent e) {

			FolderProjectEntry fpe = (FolderProjectEntry)entry;
			NewFolderDialog dialog = new NewFolderDialog(plugin.getRText(),fpe);
			dialog.setVisible(true);

			FolderFilterInfo info = dialog.getFilterInfo();
			if (info!=null) {
				fpe.setFilterInfo(info);
				handleRefresh();
			}

		}

	}


	private class FolderProjectEntryNameChecker implements NameChecker {

		public String isValid(String text) {
			int length = text.length();
			if (length==0) {
				return "empty";
			}
			return null;
		}
		
	}


}