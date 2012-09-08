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
import javax.swing.JOptionPane;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.model.ProjectEntry;


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
	 * Removes this entry from its parent.
	 */
	protected class RemoveAction extends BaseAction {

		public RemoveAction() {
			super("Action.RemoveProjectEntry");
		}

		public void actionPerformed(ActionEvent e) {

			RText rtext = plugin.getRText();
			String title = rtext.getString("ConfDialogTitle");
			String selectedEntry = entry.getSaveData();
			String text = Messages.getString(
					"Action.RemoveProjectEntry.Confirm", selectedEntry);

			int rc = JOptionPane.showConfirmDialog(rtext, text, title,
					JOptionPane.YES_NO_OPTION);
			if (rc==JOptionPane.YES_OPTION) {
				removeFromParent();
				entry.removeFromParent();
				plugin.refreshTree(parent);
			}

		}

	}


}