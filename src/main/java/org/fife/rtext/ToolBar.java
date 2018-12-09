/*
 * 11/14/2003
 *
 * ToolBar.java - Toolbar used by RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;

import org.fife.ui.CustomizableToolBar;
import org.fife.ui.OS;
import org.fife.ui.rtextarea.IconGroup;
import org.fife.ui.rtextarea.RTextArea;


/**
 * The toolbar used by {@link RText}.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ToolBar extends CustomizableToolBar {

	private JButton newButton;
	private JButton openButton;
	private JButton saveButton;
	private JButton printButton;
	private JButton cutButton;
	private JButton copyButton;
	private JButton pasteButton;
	private JButton deleteButton;
	private JButton findButton;
	private JButton replaceButton;
	private JButton undoButton;
	private JButton redoButton;

	private RText owner;
	private boolean mouseInNewButton;


	/**
	 * Creates the tool bar.
	 *
	 * @param title The title of this toolbar when it is floating.
	 * @param rtext The main application that owns this toolbar.
	 * @param mouseListener The status bar that displays a status message
	 *        when the mouse hovers over this toolbar.
	 */
	public ToolBar(String title, RText rtext, StatusBar mouseListener) {

		super(title);

		this.owner = rtext;

		// Add the standard buttons.

		newButton = createButton(rtext.getAction(RText.NEW_ACTION));
		configure(newButton, mouseListener);
		add(newButton);
		NewButtonListener listener = new NewButtonListener();
		newButton.addFocusListener(listener);
		newButton.addMouseListener(listener);

		openButton = createButton(rtext.getAction(RText.OPEN_ACTION));
		configure(openButton, mouseListener);
		add(openButton);

		saveButton = createButton(rtext.getAction(RText.SAVE_ACTION));
		configure(saveButton, mouseListener);
		add(saveButton);

		addSeparator();

		printButton = createButton(rtext.getAction(RText.PRINT_ACTION));
		configure(printButton, mouseListener);
		add(printButton);

		addSeparator();

		cutButton = createButton(RTextArea.getAction(RTextArea.CUT_ACTION));
		configure(cutButton, mouseListener);
		add(cutButton);

		copyButton = createButton(RTextArea.getAction(RTextArea.COPY_ACTION));
		configure(copyButton, mouseListener);
		add(copyButton);

		pasteButton = createButton(RTextArea.getAction(RTextArea.PASTE_ACTION));
		configure(pasteButton, mouseListener);
		add(pasteButton);

		deleteButton = createButton(RTextArea.getAction(RTextArea.DELETE_ACTION));
		configure(deleteButton, mouseListener);
		add(deleteButton);

		addSeparator();

		findButton = createButton(rtext.getAction(RText.FIND_ACTION));
		configure(findButton, mouseListener);
		add(findButton);

		replaceButton = createButton(rtext.getAction(RText.REPLACE_ACTION));
		configure(replaceButton, mouseListener);
		add(replaceButton);

		addSeparator();

		undoButton = createButton(RTextArea.getAction(RTextArea.UNDO_ACTION));
		configure(undoButton, mouseListener);
		// Necessary to keep button size from changing when undo text changes.
		undoButton.putClientProperty("hideActionText", Boolean.TRUE);
		add(undoButton);

		redoButton = createButton(RTextArea.getAction(RTextArea.REDO_ACTION));
		configure(redoButton, mouseListener);
		// Necessary to keep button size from changing when undo text changes.
		redoButton.putClientProperty("hideActionText", Boolean.TRUE);
		add(redoButton);

		// Make the toolbar have the right-click customize menu.
		makeCustomizable();

	}


	/**
	 * Checks whether the current icon group has large icons, and if it does,
	 * uses these large icons for the toolbar.
	 */
	void checkForLargeIcons() {
		IconGroup group = owner.getIconGroup();
		if (group.hasSeparateLargeIcons()) {
			Icon icon = group.getLargeIcon("new");
			newButton.setIcon(icon);
			icon = group.getLargeIcon("open");
			openButton.setIcon(icon);
			icon = group.getLargeIcon("save");
			saveButton.setIcon(icon);
			icon = group.getLargeIcon("print");
			printButton.setIcon(icon);
			icon = group.getLargeIcon("cut");
			cutButton.setIcon(icon);
			icon = group.getLargeIcon("copy");
			copyButton.setIcon(icon);
			icon = group.getLargeIcon("paste");
			pasteButton.setIcon(icon);
			icon = group.getLargeIcon("delete");
			deleteButton.setIcon(icon);
			icon = group.getLargeIcon("find");
			findButton.setIcon(icon);
			icon = group.getLargeIcon("replace");
			replaceButton.setIcon(icon);
			icon = group.getLargeIcon("undo");
			undoButton.setIcon(icon);
			icon = group.getLargeIcon("redo");
			redoButton.setIcon(icon);
		}
	}


	/**
	 * Configures the specified menu bar button.
	 *
	 * @param button The button.
	 * @param mouseListener A mouse listener to add.
	 */
	private void configure(JButton button, StatusBar mouseListener) {
		// Bug in Windows 1.4 and some 1.5 JRE's - changing LAF to
		// windows LAF causes margin to become much too wide.
		if (owner.getOS()==OS.WINDOWS) {
			button.setMargin(new Insets(0,0,0,0));
		}
		button.addMouseListener(mouseListener);
	}


	/**
	 * This class keeps track of whether or not the mouse position is inside
	 * the "New" button's bounds.  This is part of an elaborate hack to fix
	 * what seems to be a focus issue (bug) in JRE1.4.  Note that in JRE 1.5,
	 * it does not happen.
	 *
	 * What happens is this:  Whenever the user clicks on a tab to change the
	 * current document, the focused component gets switched not to the text
	 * area corresponding to the tab they clicked, but rather the "New" button
	 * on the toolbar (actually, it gets switched to the text area, then to the
	 * New button).  This behavior stops if the user changes the Look and Feel.
	 *
	 * This is the second part of the elaborate focus hack.  Whenever the New
	 * toolbar button gets focus, we check to see if the mouse position is
	 * inside the New button's bounds.  If it isn't, then we assume that the
	 * event was fired as a result of the situation described in
	 * NewButtonMouseListener's blurb, and so we give focus back to the current
	 * text area.
	 */
	private class NewButtonListener extends MouseAdapter
										implements FocusListener {

		@Override
		public void focusGained(FocusEvent e) {
			RTextEditorPane textArea = owner.getMainView().getCurrentTextArea();
			if (!mouseInNewButton && textArea!=null) {
				textArea.requestFocusInWindow();
			}
		}

		@Override
		public void focusLost(FocusEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			mouseInNewButton = true;
		}

		@Override
		public void mouseExited(MouseEvent e) {
			mouseInNewButton = false;
		}

	}


}
