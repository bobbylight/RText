/*
 * 08/28/2012
 *
 * ProjectWindow.java - Dockable window displaying the project structure.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.awt.BorderLayout;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.tree.WorkspaceTree;
import org.fife.ui.RScrollPane;
import org.fife.ui.dockablewindows.DockableWindow;


/**
 * The dockable window displaying project contents.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ProjectWindow extends DockableWindow {

	private WorkspaceTree tree;


	public ProjectWindow(RText app, ProjectPlugin plugin) {

		//this.plugin = plugin;
		setDockableWindowName(Messages.getString("Project.DockableWindow.Title"));
		setIcon(plugin.getPluginIcon());
		setPosition(DockableWindow.LEFT);
		setLayout(new BorderLayout());

		Workspace workspace = new Workspace("A Workspace!");
		tree = new WorkspaceTree(plugin, workspace);
		RScrollPane sp = new RScrollPane(tree);
		add(sp);

	}


}