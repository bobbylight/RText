/*
 * 08/28/2012
 *
 * AbstractWorkspaceTreeNode.java - Base class for workspace tree nodes.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectPlugin;


/**
 * All nodes in a workspace tree extend this class.
 *
 * @author Robert Futrell
 * @version 1.0
 */
abstract class AbstractWorkspaceTreeNode extends DefaultMutableTreeNode {

	protected ProjectPlugin plugin;


	public AbstractWorkspaceTreeNode(ProjectPlugin plugin) {
		this.plugin = plugin;
	}


	public abstract List getPopupActions();


	/**
	 * Returns the icon for this tree node.
	 *
	 * @return The icon for this tree node.
	 */
	public abstract Icon getIcon();


	protected abstract void handleProperties();


	protected abstract void handleRename();


	protected abstract class BaseAction extends AbstractAction {

		protected BaseAction(String keyRoot) {
			putValue(NAME, Messages.getString(keyRoot));
			String temp = Messages.getString(keyRoot + ".Mnemonic");
			putValue(MNEMONIC_KEY, new Integer(temp.charAt(0)));
		}

	}


	protected class PropertiesAction extends BaseAction {

		public PropertiesAction() {
			super("Action.Properties");
		}

		public void actionPerformed(ActionEvent e) {
			handleProperties();
		}

	}


	protected class RenameAction extends BaseAction {

		public RenameAction() {
			super("Action.Rename");
		}

		public void actionPerformed(ActionEvent e) {
			handleRename();
		}

	}


}