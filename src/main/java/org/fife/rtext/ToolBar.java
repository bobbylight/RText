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

import javax.swing.Icon;
import javax.swing.JButton;

import org.fife.ui.CustomizableToolBar;
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
	ToolBar(String title, RText rtext, StatusBar mouseListener) {

		super(title);
		this.owner = rtext;

		newButton = createButton(rtext.getAction(RText.NEW_ACTION));
		add(newButton);

		openButton = createButton(rtext.getAction(RText.OPEN_ACTION));
		add(openButton);

		saveButton = createButton(rtext.getAction(RText.SAVE_ACTION));
		add(saveButton);

		addSeparator();

		printButton = createButton(rtext.getAction(RText.PRINT_ACTION));
		add(printButton);

		addSeparator();

		cutButton = createButton(RTextArea.getAction(RTextArea.CUT_ACTION));
		add(cutButton);

		copyButton = createButton(RTextArea.getAction(RTextArea.COPY_ACTION));
		add(copyButton);

		pasteButton = createButton(RTextArea.getAction(RTextArea.PASTE_ACTION));
		add(pasteButton);

		deleteButton = createButton(RTextArea.getAction(RTextArea.DELETE_ACTION));
		add(deleteButton);

		addSeparator();

		findButton = createButton(rtext.getAction(RText.FIND_ACTION));
		add(findButton);

		replaceButton = createButton(rtext.getAction(RText.REPLACE_ACTION));
		add(replaceButton);

		addSeparator();

		undoButton = createButton(RTextArea.getAction(RTextArea.UNDO_ACTION));
		// Necessary to keep button size from changing when undo text changes.
		undoButton.putClientProperty("hideActionText", Boolean.TRUE);
		add(undoButton);

		redoButton = createButton(RTextArea.getAction(RTextArea.REDO_ACTION));
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

}
