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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.TreeNode;

import org.fife.rtext.plugins.project.Messages;
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
		ArrayList dirList = new ArrayList();
		ArrayList fileList = new ArrayList();
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

		Collections.sort(dirList, c);
		for (Iterator i=dirList.iterator(); i.hasNext(); ) {
			add(createFileTreeNode((File)i.next(), true));
		}
		Collections.sort(fileList, c);
		for (Iterator i=fileList.iterator(); i.hasNext(); ) {
			add(createFileTreeNode((File)i.next(), false));
		}

	}


	private FileTreeNode createFileTreeNode(File file, boolean folder) {
		FileTreeNode ftn = new FileTreeNode(plugin, file);
		if (folder) {
			ftn.setFilterInfo(getFilterInfo());
		}
		return ftn;
	}


	public String getDisplayName() {
		return ((FolderProjectEntry)entry).getDisplayName();
	}


	protected NameChecker createNameChecker() {
		return new FolderProjectEntryNameChecker();
	}


	public FolderFilterInfo getFilterInfo() {
		return ((FolderProjectEntry)entry).getFilterInfo();
	}


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


	protected void handleRenameImpl(String newName) {
		setDisplayName(newName);
		plugin.refreshTree(getParent());
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