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
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.ui.rtextfilechooser.Actions;
import org.fife.ui.rtextfilechooser.extras.FileIOExtras;


/**
 * A tree node for regular files and directories.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FileTreeNode extends AbstractWorkspaceTreeNode
		implements PhysicalLocationTreeNode {

	private Icon icon;

	/**
	 * Whether we're running in a Java 6 or higher JVM.
	 */
	private static final boolean IS_JAVA_6_PLUS;


	public FileTreeNode(ProjectPlugin plugin, File file) {
		super(plugin);
		setUserObject(file);
		if (file.isDirectory()) {
			add(new NotYetPopulatedChild(plugin));
		}
		icon = FileSystemView.getFileSystemView().getSystemIcon(file);
	}


	private FileTreeNode createFileTreeNode(File file) {
		return new FileTreeNode(plugin, file);
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


	public File getFile() {
		return (File)getUserObject();
	}


	public List getPopupActions() {

		List actions = new ArrayList();

		if (IS_JAVA_6_PLUS) {
			Action a = new Actions.SystemOpenAction(plugin.getTree(), "view");
			// Make the name a little better for this popup.
			a.putValue(Action.NAME,
					Messages.getString("Action.OpenInSystemViewer"));
			actions.add(a);
			a = new Actions.SystemOpenAction(plugin.getTree(), "edit");
			// Make the name a little better for this popup.
			a.putValue(Action.NAME,
					Messages.getString("Action.OpenInSystemEditor"));
			actions.add(a);
			actions.add(null);
		}

		actions.add(new DeleteAction());
		actions.add(null);
		actions.add(new RenameAction());
		actions.add(null);
		actions.add(new PropertiesAction());

		return actions;

	}


	public Icon getIcon() {
		return icon;
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

		String text = Messages.getString("Action.Delete.Confirm",
				"ProjectPlugin.File", file.getName());
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


	protected void handleRefresh() {
		plugin.getTree().refreshChildren(this);
	}


	protected void handleRename() {
		// TODO Auto-generated method stub
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


	public String toString() {
		return getFile().getName();
	}
	

	static {
		// Some actions only work with Java 6+.
		String ver = System.getProperty("java.specification.version");
		IS_JAVA_6_PLUS = !ver.startsWith("1.4") && !ver.startsWith("1.5");
	}


}