/*
 * 09/08/2012
 *
 * ProjectEntryTreeNode.java - Base class for project entry tree nodes.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeModel;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.BaseAction;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.model.LogicalFolderProjectEntry;
import org.fife.rtext.plugins.project.model.ProjectEntry;
import org.fife.rtext.plugins.project.model.ProjectEntryParent;


/**
 * Base class for project entry tree nodes.
 *
 * @author Robert Futrell
 * @version 1.0
 */
abstract class ProjectEntryTreeNode extends AbstractWorkspaceTreeNode {

	protected ProjectEntry entry;


	protected ProjectEntryTreeNode(ProjectPlugin plugin, ProjectEntry entry) {
		super(plugin);
		this.entry = entry;
	}


	/**
	 * Prompts for verification, then removes this project entry from its
	 * parent project.
	 */
	public void handleRemove() {

		RText rtext = plugin.getRText();
		String title = rtext.getString("ConfDialogTitle");
		String selectedEntry = entry.getSaveData();
		String key = (entry instanceof LogicalFolderProjectEntry) ?
				"Action.RemoveLogicalProjectEntry.Confirm" :
				"Action.RemoveProjectEntry.Confirm";
		String text = Messages.getString(key, selectedEntry);

		int rc = JOptionPane.showConfirmDialog(rtext, text, title,
				JOptionPane.YES_NO_OPTION);
		if (rc==JOptionPane.YES_OPTION) {
			entry.removeFromParent();
			((DefaultTreeModel)plugin.getTree().getModel()).removeNodeFromParent(this);
		}

	}


	@Override
	public boolean moveProjectEntityDown() {
		ProjectEntryParent parent = entry.getParent();
		return parent.moveProjectEntryDown(entry);
	}


	@Override
	public boolean moveProjectEntityUp() {
		ProjectEntryParent parent = entry.getParent();
		return parent.moveProjectEntryUp(entry);
	}


	/**
	 * Removes this entry from its parent.
	 */
	protected class RemoveAction extends BaseAction {

		public RemoveAction() {
			super("Action.RemoveProjectEntry");
			KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
			putValue(ACCELERATOR_KEY, delete);
		}

		public void actionPerformed(ActionEvent e) {
			handleRemove();
		}

	}


}