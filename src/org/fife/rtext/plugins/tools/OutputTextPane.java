/*
 * 01/26/2010
 *
 * OutputTextPane.java - Text component that shows the output of external
 * processes.
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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.TextAction;


/**
 * Text component that displays the output of a tool.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class OutputTextPane extends JTextPane {

	static final String STYLE_STDIN				= "stdin";
	static final String STYLE_STDOUT			= "stdout";
	static final String STYLE_STDERR			= "stderr";


	/**
	 * Constructor.
	 */
	public OutputTextPane() {
		installStyles();
		fixKeyboardShortcuts();
	}


	/**
	 * Fixes the keyboard shortcuts for this text component so the user cannot
	 * accidentally delete any stdout or stderr, only stdin.
	 */
	private void fixKeyboardShortcuts() {

		InputMap im = getInputMap();
		ActionMap am = getActionMap();

		// backspace
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "backspace");
		Action delegate = am.get(DefaultEditorKit.deletePrevCharAction);
		am.put("backspace", new BackspaceAction(delegate));

		// Just remove "delete previous word" for now, since DefaultEditorKit
		// doesn't expose the delegate for us to call into. 
		int ctrl = InputEvent.CTRL_MASK;
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, ctrl), "invalid");

		// delete
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		delegate = am.get(DefaultEditorKit.deleteNextCharAction);
		am.put("delete", new DeleteAction(delegate));

		// Just remove "delete next word" for now, since DefaultEditorKit
		// doesn't expose the delegate for us to call into. 
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ctrl), "invalid");

	}


	/**
	 * Installs the styles used by this text component.
	 */
	private void installStyles() {

		Font old = getFont();
		setFont(new Font("Monospaced", old.getStyle(), old.getSize()));

		Style stdin = addStyle(STYLE_STDIN, null);
		StyleConstants.setForeground(stdin, new Color(0,192,0));

		Style stdout = addStyle(STYLE_STDOUT, null);
		StyleConstants.setForeground(stdout, Color.blue);

		Style stderr = addStyle(STYLE_STDERR, null);
		StyleConstants.setForeground(stderr, Color.red);

	}


	/**
	 * Returns whether the specified offset is the beginning of the last line.
	 *
	 * @param offs the offset to check.
	 * @return Whether the offset is the start offset of the last line.
	 */
	private boolean isLastLineStartOffs(int offs) {
		Document doc = getDocument();
		Element root = doc.getDefaultRootElement();
		int line = root.getElementIndex(offs);
		Element elem = root.getElement(line);
		return line==root.getElementCount()-1 && offs==elem.getStartOffset();
	}


	/**
	 * Returns whether the specified offset is on the last line of this text
	 * component.
	 *
	 * @param offs The offset.
	 * @return Whether the offset is on the last line.
	 */
	private boolean isOnLastLine(int offs) {
		Document doc = getDocument();
		Element root = doc.getDefaultRootElement();
		int lastLine = root.getElementCount() - 1;
		return root.getElementIndex(offs)==lastLine;
	}


	/**
	 * Overridden to only allow the user to edit text they have entered (i.e.
	 * they can only edit "stdin").
	 *
	 * @param text The text to replace the selection with.
	 */
	public void replaceSelection(String text) {

		int start = getSelectionStart();
		int end = getSelectionEnd();
		Document doc = getDocument();

		// Don't let the user remove any text they haven't typed (stdin).
		if (!(isOnLastLine(start) && isOnLastLine(end))) {
			setCaretPosition(doc.getLength());
		}

		// JUST IN CASE we aren't an AbstractDocument (paranoid), use remove()
		// and insertString() separately.
		try {
			start = getSelectionStart();
			doc.remove(start, getSelectionEnd()-start);
			doc.insertString(start, text, getStyle(STYLE_STDIN));
		} catch (BadLocationException ble) {
			UIManager.getLookAndFeel().provideErrorFeedback(this);
			ble.printStackTrace();
		}

	}


	/**
	 * Action performed when backspace is pressed.
	 */
	private class BackspaceAction extends TextAction {

		/**
		 * DefaultEditorKit's DeletePrevCharAction.
		 */
		private Action delegate;

		public BackspaceAction(Action delegate) {
			super("backspace");
			this.delegate = delegate;
		}

		public void actionPerformed(ActionEvent e) {
			int start = getSelectionStart();
			int end = getSelectionEnd();
			if ((start==end && isLastLineStartOffs(start)) ||
					!(isOnLastLine(start) && isOnLastLine(end))) {
				UIManager.getLookAndFeel().
							provideErrorFeedback(OutputTextPane.this);
				setCaretPosition(getDocument().getLength());
			}
			else {
				delegate.actionPerformed(e);
			}
		}

	}


	/**
	 * Action performed when delete 
	 */
	private class DeleteAction extends TextAction {

		/**
		 * DefaultEditorKit's DeleteNextCharAction.
		 */
		private Action delegate;

		public DeleteAction(Action delegate) {
			super("delete");
			this.delegate = delegate;
		}

		public void actionPerformed(ActionEvent e) {
			int start = getSelectionStart();
			int end = getSelectionEnd();
			if (!(isOnLastLine(start) && isOnLastLine(end))) {
				UIManager.getLookAndFeel().
							provideErrorFeedback(OutputTextPane.this);
			}
			else {
				delegate.actionPerformed(e);
			}
		}

	}


}