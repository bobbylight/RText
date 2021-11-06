/*
 * 01/26/2010
 *
 * OutputTextPane.java - Text component that shows the output of external
 * processes.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.TextAction;

import org.fife.ui.OptionsDialog;
import org.fife.ui.StandardAction;
import org.fife.ui.app.console.AbstractConsoleTextArea;
import org.fife.ui.rtextarea.RTextArea;


/**
 * Text component that displays the output of a tool.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class OutputTextPane extends AbstractConsoleTextArea {

	private final ToolPlugin plugin;


	/**
	 * Constructor.
	 */
	OutputTextPane(ToolPlugin plugin) {
		this.plugin = plugin;
		installDefaultStyles(false);
		fixKeyboardShortcuts();
		Listener listener = new Listener();
		addMouseListener(listener);
	}


	@Override
	protected JPopupMenu createPopupMenu() {
		JPopupMenu popup = new JPopupMenu();
		popup.add(new JMenuItem(new CopyAllAction()));
		popup.addSeparator();
		popup.add(new JMenuItem(new ClearAllAction()));
		popup.addSeparator();
		popup.add(new JMenuItem(new ConfigureAction()));
		return popup;
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
		int ctrl = InputEvent.CTRL_DOWN_MASK;
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, ctrl), "invalid");

		// delete
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		delegate = am.get(DefaultEditorKit.deleteNextCharAction);
		am.put("delete", new DeleteAction(delegate));

		// Just remove "delete next word" for now, since DefaultEditorKit
		// doesn't expose the delegate for us to call into.
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ctrl), "invalid");

	}


	@Override
	protected Font getDefaultFont() {
		return RTextArea.getDefaultFont();
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
	@Override
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
	 * Clears all text from this text area.
	 */
	private class ClearAllAction extends StandardAction {

		ClearAllAction() {
			setName(plugin.getString("Action.ClearAll"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setText(null);
		}
	}


	/**
	 * Brings up the options dialog panel for this plugin.
	 */
	private class ConfigureAction extends AbstractAction {

		ConfigureAction() {
			putValue(NAME, plugin.getString("Action.Configure"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			OptionsDialog od = plugin.getApplication().getOptionsDialog();
			od.initialize();
			od.setSelectedOptionsPanel(plugin.getString("Plugin.Name"));
			od.setVisible(true);
		}

	}


	/**
	 * Copies all text from this text area.
	 */
	private class CopyAllAction extends StandardAction {

		CopyAllAction() {
			setName(plugin.getString("Action.CopyAll"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int dot = getSelectionStart();
			int mark = getSelectionEnd();
			setSelectionStart(0);
			setSelectionEnd(getDocument().getLength());
			copy();
			setSelectionStart(dot);
			setSelectionEnd(mark);
		}
	}


	/**
	 * Action performed when backspace is pressed.
	 */
	private class BackspaceAction extends TextAction {

		/**
		 * DefaultEditorKit's DeletePrevCharAction.
		 */
		private final Action delegate;

		BackspaceAction(Action delegate) {
			super("backspace");
			this.delegate = delegate;
		}

		@Override
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
	 * Action performed when delete.
	 */
	private class DeleteAction extends TextAction {

		/**
		 * DefaultEditorKit's DeleteNextCharAction.
		 */
		private final Action delegate;

		DeleteAction(Action delegate) {
			super("delete");
			this.delegate = delegate;
		}

		@Override
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


	/**
	 * Listens for events in this text area.
	 */
	private class Listener extends MouseAdapter {

		private void handleMouseEvent(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			handleMouseEvent(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			handleMouseEvent(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			handleMouseEvent(e);
		}

	}


}
