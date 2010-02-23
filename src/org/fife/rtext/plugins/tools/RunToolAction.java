/*
 * 01/21/2010
 *
 * RunToolAction.java - Action that runs a tool.
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This file is a part of RText.
 *
 * RText is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.rtext.plugins.tools;

import java.awt.Container;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;


/**
 * Action that runs an external tool.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class RunToolAction extends StandardAction {

	private Tool tool;
	private ToolDockableWindow window;


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 * @param tool The tool to run.
	 */
	public RunToolAction(RText app, Tool tool, ToolDockableWindow l) {
		super(app, tool.getName());
		setAccelerator(KeyStroke.getKeyStroke(tool.getAccelerator()));
		setShortDescription(tool.getDescription());
		this.tool = tool;
		this.window = l;
	}


	/**
	 * Runs the tool.
	 *
	 * @param e The action event.
	 */
	public void actionPerformed(ActionEvent e) {

		// Make sure the program and working directory exist.
		String errorDesc = tool.checkForErrors();
		if (errorDesc!=null) {
			RText app = (RText)getApplication();
			String title = app.getString("ErrorDialogTitle");
			JOptionPane.showMessageDialog(app, errorDesc, title,
										JOptionPane.ERROR_MESSAGE);
			return;
		}

		showButDontFocus(window);
		// Call startingTool() before tool.execute() so threading doesn't
		// cause the window's title to get hosed.
		if (window.startingTool(tool)) {
			tool.execute(window);
		}

	}


	/**
	 * Makes sure that the specified component is visible, but that the focus
	 * stays on the current text area.  This is useful if the component is in
	 * an unselected tab in a tabbed pane, for example.
	 *
	 * @param comp The component that should be visible.
	 */
	private void showButDontFocus(JComponent comp) {

		// This is tricky, because the component cannot be focused unless
		// its tab is the selected one, so we must figure out what tab we're
		// in and activate it.

		Container parent = comp.getParent();
		Container prev = comp;
		while (!(parent instanceof JTabbedPane)) {
			prev = parent;
			parent = parent.getParent();
		}

		// Now, parent is the JTabbedPane and prev is the component at the
		// index we want to select.
		JTabbedPane tp = (JTabbedPane)parent;
		for (int i=0; i<tp.getTabCount(); i++) {
			if (prev==tp.getComponentAt(i)) {
				tp.setSelectedIndex(i);
				comp.requestFocusInWindow();
				break;
			}
		}

		// Give focus back to the text area, so the user can keep typing.
		((RText)getApplication()).getMainView().getCurrentTextArea().
													requestFocusInWindow();

	}


}