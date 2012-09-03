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

import java.util.List;
import javax.swing.Icon;

import org.fife.rtext.plugins.project.FolderProjectEntry;
import org.fife.rtext.plugins.project.ProjectPlugin;


/**
 * The tree node used for folder project entries.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FolderProjectEntryTreeNode extends FileProjectEntryTreeNode {


	public FolderProjectEntryTreeNode(ProjectPlugin plugin, FolderProjectEntry entry) {
		super(plugin, entry);
		add(new NotYetPopulatedChild(plugin));
	}


	public boolean isNotPopulated() {
		int childCount = getChildCount();
		return childCount==1 && (getFirstChild() instanceof NotYetPopulatedChild);
	}


	/**
	 * Dummy class signifying that this tree node has not yet had its children
	 * calculated.
	 */
	private static class NotYetPopulatedChild extends AbstractWorkspaceTreeNode {

		public NotYetPopulatedChild(ProjectPlugin plugin) {
			super(plugin);
		}

		public List getPopupActions() {
			return null;
		}

		public Icon getIcon() {
			return null;
		}

		protected void handleProperties() {}

		protected void handleRename() {}
		
	}


}