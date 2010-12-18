/*
 * 12/17/2010
 *
 * ConsoleTextArea.java - Text component for the console.
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
package org.fife.rtext.plugins.console;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.TextAction;

import org.fife.io.ProcessRunner;
import org.fife.io.ProcessRunnerOutputListener;
import org.fife.ui.rtextarea.RTextArea;


/**
 * Text component that displays the output of a tool.  This component tries
 * to mimic jEdit's "Console" behavior, since that seemed to work pretty well.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ConsoleTextArea extends JTextPane {

	/**
	 * Property change event fired whenever a process is launched or
	 * completes.
	 */
	public static final String PROPERTY_PROCESS_RUNNING	= "ProcessRunning";

	private static final String STYLE_PROMPT			= "prompt";
	private static final String STYLE_STDIN				= "stdin";
	private static final String STYLE_STDOUT			= "stdout";
	private static final String STYLE_STDERR			= "stderr";
	private static final String STYLE_EXCEPTION			= "exception";

	private Plugin plugin;
	private JPopupMenu popup;
	private Listener listener;
	private transient Thread activeProcessThread;
	private File pwd;
	private int inputMinOffs;

	private static final String CD						= "cd";
	private static final String PWD						= "pwd";


	/**
	 * Constructor.
	 */
	public ConsoleTextArea(Plugin plugin) {
		this.plugin = plugin;
		installStyles();
		setTabSize(4); // Do after installStyles()
		fixKeyboardShortcuts();
		listener = new Listener();
		addMouseListener(listener);
		pwd = new File(System.getProperty("user.home"));
		appendPrompt();
	}


	/**
	 * Appends text in the given style.  This method is thread-safe.
	 *
	 * @param text The text to append.
	 * @param style The style to use.
	 */
	public void append(String text, String style) {
		if (text==null) {
			return;
		}
		if (!text.endsWith("\n")) {
			text += "\n";
		}
		appendImpl(text, style);
	}


	/**
	 * Handles updating of the text component.  This method is thread-safe.
	 *
	 * @param text The text to append.
	 * @param style The style to apply to the appended text.
	 */
	private void appendImpl(final String text, final String style) {
		if (SwingUtilities.isEventDispatchThread()) {
			Document doc = getDocument();
			int end = doc.getLength();
			try {
				doc.insertString(end, text, getStyle(style));
			} catch (BadLocationException ble) { // Never happens
				ble.printStackTrace();
			}
			setCaretPosition(doc.getLength());
			inputMinOffs = getCaretPosition();
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					appendImpl(text, style);
				}
			});
		}
	}


	/**
	 * Appends the prompt to the console, and resets the starting location
	 * at which the user can input text.  This method is thread-safe.
	 */
	private void appendPrompt() {
		String prompt = pwd.getName();
		if (prompt.length()==0) { // Root directory
			prompt = pwd.getAbsolutePath();
		}
		prompt += File.separatorChar=='/' ? "$ " : "> ";
		appendImpl(prompt, STYLE_PROMPT);
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

		int mod = 0;//InputEvent.CTRL_MASK;
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, mod);
		im.put(ks, "Submit");
		am.put("Submit", new SubmitAction());

	}


	/**
	 * Handles the built-in "cd" command.
	 *
	 * @param text The full command line entered.
	 */
	private void handleCd(String text) {

		Pattern p = Pattern.compile("cd\\s+([^\\s]+|\\\".+\\\")$");
		Matcher m = p.matcher(text);
		if (!m.matches()) {
			append(plugin.getString("Error.IncorrectParamCount", CD),
									STYLE_STDERR);
			appendPrompt();
			return;
		}

		String dir = m.group(1);
		if (dir.startsWith("\"") && dir.endsWith("\"")) {
			dir = dir.substring(1, dir.length()-1);
		}
		File temp = new File(pwd, dir).getAbsoluteFile();
		if (temp.isDirectory()) {
			// Pretty up the directory path, needed for cd's when launching
			try {
				pwd = temp.getCanonicalFile();
			} catch (IOException ioe) {
				StringWriter sw = new StringWriter();
				ioe.printStackTrace(new PrintWriter(sw));
				text = sw.toString();
				append(text, STYLE_EXCEPTION);
			}
		}
		else if (temp.exists()) {
			append(plugin.getString("Error.NotADirectory", CD, dir),
					STYLE_STDERR);
		}
		else {
			append(plugin.getString("Error.DirDoesNotExist", CD, dir),
					STYLE_STDERR);
		}

		appendPrompt();

	}


	/**
	 * Handles the built-in "pwd" command.
	 *
	 * @param text The full command line entered.
	 */
	private void handlePwd(String text) {

		if (!text.equals(PWD)) {
			append(plugin.getString("Error.IncorrectParamCount", PWD),
					STYLE_STDERR);
			appendPrompt();
			return;
		}

		append(pwd.getAbsolutePath(), STYLE_STDOUT);
		appendPrompt();

	}


	/**
	 * Installs the styles used by this text component.
	 */
	private void installStyles() {

		setFont(RTextArea.getDefaultFont());

		Style prompt = addStyle(STYLE_PROMPT, null);
		StyleConstants.setForeground(prompt, new Color(0,192,0));

		/*Style stdin = */addStyle(STYLE_STDIN, null); // Default text color

		Style stdout = addStyle(STYLE_STDOUT, null);
		StyleConstants.setForeground(stdout, Color.blue);

		Style stderr = addStyle(STYLE_STDERR, null);
		StyleConstants.setForeground(stderr, Color.red);

		Style exception = addStyle(STYLE_EXCEPTION, null);
		StyleConstants.setForeground(exception, new Color(111, 49, 152));

	}


	/**
	 * Overridden to only allow the user to edit text they have entered (i.e.
	 * they can only edit "stdin").
	 *
	 * @param text The text to replace the selection with.
	 */
	public void replaceSelection(String text) {

		int start = getSelectionStart();
		Document doc = getDocument();

		// Don't let the user remove any text they haven't typed (stdin).
		if (start<inputMinOffs) {
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
	 * Stops the currently running process, if any.
	 */
	public void stopCurrentProcess() {
		if (activeProcessThread!=null && activeProcessThread.isAlive()) {
			activeProcessThread.interrupt();
			activeProcessThread = null;
		}
	}


	/**
	 * Overridden to also update the UI of the popup menu.
	 */
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
			if (start<=inputMinOffs) {
				UIManager.getLookAndFeel().
							provideErrorFeedback(ConsoleTextArea.this);
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
			if (start<inputMinOffs) {
				UIManager.getLookAndFeel().
							provideErrorFeedback(ConsoleTextArea.this);
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

		public void mouseClicked(MouseEvent e) {
			handleMouseEvent(e);
		}

		public void mousePressed(MouseEvent e) {
			handleMouseEvent(e);
		}

		public void mouseReleased(MouseEvent e) {
			handleMouseEvent(e);
		}

	}


	private class ProcessOutputListener implements ProcessRunnerOutputListener{

		public void outputWritten(Process p, final String output, final boolean stdout) {
			append(output, stdout ? STYLE_STDOUT : STYLE_STDERR);
		}

		public void processCompleted(Process p, int rc, final Throwable e) {
			// Required because of other Swing calls we make inside
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (e!=null) {
						String text = null;
						if (e instanceof InterruptedException) {
							text = plugin.getString("ProcessForciblyTerminated");
						}
						else {
							StringWriter sw = new StringWriter();
							e.printStackTrace(new PrintWriter(sw));
							text = sw.toString();
						}
						append(text, STYLE_EXCEPTION);
					}
					// Not really necessary, should allow GC of Process resources
					activeProcessThread = null;
					appendPrompt();
					setEditable(true);
					firePropertyChange(PROPERTY_PROCESS_RUNNING, true, false);
				}
			});
		}

	}


	/**
	 * Called when the user presses Enter.  Submits the command they entered.
	 */
	private class SubmitAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {

			int dot = getCaretPosition();
			if (dot<inputMinOffs) {
				UIManager.getLookAndFeel().provideErrorFeedback(
													ConsoleTextArea.this);
				return;
			}

			Document doc = getDocument();
			int startOffs = inputMinOffs;
			int len = doc.getLength() - startOffs;
			ConsoleTextArea.super.replaceSelection("\n");

			// If they didn't enter any text, don't launch a process
			if (len==0) {
				return;
			}
			String text = null;
			try {
				text = getText(startOffs, len).trim();
			} catch (BadLocationException ble) { // Never happens
				ble.printStackTrace();
				return;
			}
			if (text.length()==0) {
				return;
			}

			// Check for a built-in command first
			String[] input = text.split("\\s+");
			if (CD.equals(input[0])) {
				handleCd(text);
			}
			else if (PWD.equals(input[0])) {
				handlePwd(text);
			}

			// Not a built-in command - launch an external process in a shell
			else {

				// Ensure our directory wasn't deleted out from under us.
				if (!pwd.isDirectory()) {
					append(plugin.getString("Error.CurrentDirectoryDNE",
							pwd.getAbsolutePath()), STYLE_STDERR);
					appendPrompt();
					return;
				}

				List cmdList = new ArrayList();
				if (File.separatorChar=='/') {
					cmdList.add("/bin/sh");
					cmdList.add("-c");
				}
				else {
					cmdList.add("cmd.exe");
					cmdList.add("/c");
				}
				text = "cd " + pwd.getAbsolutePath() + " && " + text;

				cmdList.add(text);
				final String[] cmd = (String[])cmdList.toArray(new String[] {});

				setEditable(false);
				activeProcessThread = new Thread() {
					public void run() {
						ProcessRunner pr = new ProcessRunner(cmd);
						pr.setDirectory(pwd);
						pr.setOutputListener(new ProcessOutputListener());
						pr.run();
					}
				};
				ConsoleTextArea.this.firePropertyChange(
									PROPERTY_PROCESS_RUNNING, false, true);
				activeProcessThread.start();

			}

		}

	}


}