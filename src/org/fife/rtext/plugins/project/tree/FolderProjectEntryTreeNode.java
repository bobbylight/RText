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
import javax.swing.filechooser.FileSystemView;

import org.fife.rtext.plugins.project.ProjectPlugin;
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


	private FileTreeNode createFileTreeNode(File file) {
		return new FileTreeNode(plugin, file);
	}


	protected NameChecker createNameChecker() {
		return new FolderProjectEntryNameChecker();
	}


	/**
	 * Does any filtering and sorting of an array of files so that they will
	 * be displayed properly.
	 *
	 * @param files The array of files to filter and sort.
	 * @return The filtered and sorted array of files.
	 */
	// TODO: Have FolderProjectEntrys contain filters.
	private File[] filterAndSort(File[] files) {

		int num = files.length;
		ArrayList dirList = new ArrayList();
		ArrayList fileList = new ArrayList();

		// First, separate the directories from regular files so we can
		// sort them individually.  This part could be made more compact,
		// but it isn't just for a tad more speed.
		for (int i=0; i<num; i++) {
			if (files[i].isDirectory())
				dirList.add(files[i]);
			else
				fileList.add(files[i]);
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


	public void handleRefresh() {
		plugin.getTree().refreshChildren(this);
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


	/**
	 * Ensures that proposed file project entry names are valid.
	 */
	static class FolderProjectEntryNameChecker implements NameChecker {

		public String isValid(String text) {
			int length = text.length();
			if (length==0) {
				return "empty";
			}
			for (int i=0; i<length; i++) {
				char ch = text.charAt(i);
				if (!(Character.isLetterOrDigit(ch) || ch=='_' || ch=='-' ||
						ch==' ' || ch=='.')) {
					return "invalidFolderName";
				}
			}
			if (text.endsWith(".")) {
				return "folderNameCannotEndWithDot";
			}
			return null;
		}

	}


}