/*
 * 01/21/2010
 *
 * RunToolAction.java - Action that runs a tool.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import java.awt.Container;
import java.awt.Frame;
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
		super(app);
		setName(tool.getName());
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
		tool.setRText((RText)getApplication());
		String errorDesc = tool.checkForErrors();
		if (errorDesc!=null) {
			RText app = (RText)getApplication();
			String title = app.getString("ErrorDialogTitle");
			JOptionPane.showMessageDialog(app, errorDesc, title,
										JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Make visible if hidden but a tool starts running.
		if (!window.isActive()) {
			window.setActive(true);
		}
		showButDontFocus(window);
		// Call startingTool() before tool.execute() so threading doesn't
		// cause the window's title to get hosed.
		if (window.startingTool(tool)) {
			tool.setRText((RText)getApplication());
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

		JComponent editor = ((RText)getApplication()).getMainView().
				getCurrentTextArea();

		// This is tricky, because the component cannot be focused unless
		// its tab is the selected one, so we must figure out what tab we're
		// in and activate it.

		Container parent = comp.getParent();
		Container prev = comp;
		while (!(parent instanceof JTabbedPane)) {
			prev = parent;
			parent = parent.getParent();
			if (parent instanceof Frame) {
				// We're in a popup window - just make sure it's not minimized
				Frame frame = (Frame)parent;
				if (frame.getState()==Frame.ICONIFIED) {
					frame.setState(Frame.NORMAL);
					editor.requestFocusInWindow();
				}
				return;
			}
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
		editor.requestFocusInWindow();

	}


}