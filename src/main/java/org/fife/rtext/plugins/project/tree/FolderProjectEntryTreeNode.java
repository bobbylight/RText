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
import java.util.List;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.TreeNode;

import org.fife.rtext.plugins.project.BaseAction;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.NewExistingFolderDialog;
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
		ArrayList<File> dirList = new ArrayList<>();
		ArrayList<File> fileList = new ArrayList<>();
		FolderFilterInfo filterInfo = getFilterInfo();

		// First, separate the directories from regular files so we can
		// sort them individually.  This part could be made more compact,
		// but it isn't just for a tad more speed.
		for (File file1 : files) {
			boolean isDir = file1.isDirectory();
			if (filterInfo != null && filterInfo.isAllowed(file1, isDir)) {
				if (isDir)
					dirList.add(file1);
				else
					fileList.add(file1);
			}
		}

		Collections.sort(dirList);
		for (File dir : dirList) {
			add(createFileTreeNode(dir, true));
		}
		Collections.sort(fileList);
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


	private FolderFilterInfo getFilterInfo() {
		return ((FolderProjectEntry)entry).getFilterInfo();
	}


	/**
	 * Overridden to add a menu items specific to folders.
	 */
	@Override
	public List<PopupContent> getPopupActions() {

		List<PopupContent> actions = super.getPopupActions();

		// "Find in Files from here" strategically between copy/paste/delete
		// action group and rename group
		actions.add(11, new FindInFilesFromHereAction());
		actions.add(12, null);

		PopupContent.PopupSubMenu newMenu = new PopupContent.PopupSubMenu(
				Messages.getString("Action.New"));
		newMenu.add(new NewFileOrFolderAction(this, true));
		newMenu.add(new NewFileOrFolderAction(this, false));
		actions.add(0, newMenu);

		actions.add(actions.size()-1, new ConfigureFiltersAction());

		return actions;

	}


	@Override
	public String getToolTipText() {
		File file = getFile();
		FolderFilterInfo filterInfo = getFilterInfo();
		return Messages.getString("ProjectPlugin.ToolTip.FolderProjectEntry",
			escapeForHtml(getDisplayName()),
			escapeForHtml(file.getAbsolutePath()),
			escapeForHtml(FileTreeNode.getFilterString(filterInfo.getAllowedFileFilters(), "*")),
			escapeForHtml(FileTreeNode.getFilterString(filterInfo.getHiddenFileFilters())),
			escapeForHtml(FileTreeNode.getFilterString(filterInfo.getHiddenFolderFilters())));
	}


	@Override
	public void handleRefresh() {
		plugin.getTree().refreshChildren(this);
	}


	@Override
	protected void handleRenameImpl(String newName) {
		setDisplayName(newName);
		plugin.getTree().nodeChanged(this);
	}


	@Override
	public boolean isNotPopulated() {
		int childCount = getChildCount();
		return childCount==1 && (getFirstChild() instanceof NotYetPopulatedChild);
	}


	@Override
	public void refreshChildren() {
		File file = getFile();
		if (file.isDirectory()) { // Should always be true
			removeAllChildren();
			FileSystemView fsv = FileSystemView.getFileSystemView();
			File[] children = fsv.getFiles(file, false);
			addChildrenFilteredAndSorted(children);
		}
	}


	private void setDisplayName(String displayName) {
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

		ConfigureFiltersAction() {
			super("Action.ConfigureFilters");
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			FolderProjectEntry fpe = (FolderProjectEntry)entry;
			NewExistingFolderDialog dialog = new NewExistingFolderDialog(plugin.getRText(),fpe);
			dialog.setVisible(true);

			FolderFilterInfo info = dialog.getFilterInfo();
			if (info!=null) {
				fpe.setFilterInfo(info);
				handleRefresh();
			}

		}

	}


	/**
	 * Checks the name of a folder project entry.
	 */
	private static class FolderProjectEntryNameChecker implements NameChecker {

		@Override
		public String isValid(String text) {
			int length = text.length();
			if (length==0) {
				return "empty";
			}
			return null;
		}

	}


}
