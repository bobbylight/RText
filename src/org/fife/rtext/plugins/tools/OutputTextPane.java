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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.TextAction;

import org.fife.ui.rtextarea.RTextArea;


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
	static final String STYLE_EXCEPTION			= "exception";

	private ToolPlugin plugin;
	private JPopupMenu popup;
	private Listener listener;


	/**
	 * Constructor.
	 */
	public OutputTextPane(ToolPlugin plugin) {
		this.plugin = plugin;
		installStyles();
		setTabSize(4); // Do after installStyles()
		fixKeyboardShortcuts();
		listener = new Listener();
		addMouseListener(listener);
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

		setFont(RTextArea.getDefaultFont());

		Style stdin = addStyle(STYLE_STDIN, null);
		StyleConstants.setForeground(stdin, new Color(0,192,0));

		Style stdout = addStyle(STYLE_STDOUT, null);
		StyleConstants.setForeground(stdout, Color.blue);

		Style stderr = addStyle(STYLE_STDERR, null);
		StyleConstants.setForeground(stderr, Color.red);

		Style exception = addStyle(STYLE_EXCEPTION, null);
		StyleConstants.setForeground(exception, new Color(111, 49, 152));

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
	 * Sets the tab size in this text pane.
	 *
	 * @param tabSize The new tab size, in characters.
	 */
	public void setTabSize(int tabSize) {

		FontMetrics fm = getFontMetrics(getFont());
		int charWidth = fm.charWidth('m');
		int tabWidth = charWidth * tabSize;

		// NOTE: Array length is arbitrary, represents the maximum number of
		// tabs handled on a single line.
		TabStop[] tabs = new TabStop[50];
		for (int j=0; j<tabs.length; j++) {
			tabs[j] = new TabStop((j+1)*tabWidth);
		}

		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setTabSet(attributes, tabSet);

		int length = getDocument().getLength();
		getStyledDocument().setParagraphAttributes(0, length, attributes, true);

	}


	/**
	 * Displays this text area's popup menu.
	 *
	 * @param e The location at which to display the popup.
	 */
	private void showPopupMenu(MouseEvent e) {

		if (popup==null) {
			popup = new JPopupMenu();
			popup.add(new JMenuItem(new CopyAllAction()));
			popup.addSeparator();
			popup.add(new JMenuItem(new ClearAllAction()));
		}

		popup.show(this, e.getX(), e.getY());

	}


	/**
	 * Overridden to also update the UI of the popup menu.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		if (popup!=null) {
			SwingUtilities.updateComponentTreeUI(popup);
		}
	}


	/**
	 * Clears all text from this text area.
	 */
	private class ClearAllAction extends AbstractAction {

		public ClearAllAction() {
			putValue(NAME, plugin.getString("Action.ClearAll"));
		}

		public void actionPerformed(ActionEvent e) {
			setText(null);
		}
	}


	/**
	 * Copies all text from this text area.
	 */
	private class CopyAllAction extends AbstractAction {

		public CopyAllAction() {
			putValue(NAME, plugin.getString("Action.CopyAll"));
		}

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