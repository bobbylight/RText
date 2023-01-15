/*
 * 11/14/2003
 *
 * ToolBar.java - Toolbar used by RText.
 * Copyright (C) 2003 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import javax.swing.*;

import org.fife.ui.CustomizableToolBar;
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


	/**
	 * Creates the toolbar.
	 *
	 * @param title The title of this toolbar when it is floating.
	 * @param rtext The main application that owns this toolbar.
	 */
	ToolBar(String title, RText rtext) {

		super(title);

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

}
