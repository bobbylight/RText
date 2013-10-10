/*
 * 12/22/2010
 *
 * SystemShellTextArea.java - Text component simulating a system shell.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.console;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.TextAction;

import org.fife.io.ProcessRunner;
import org.fife.io.ProcessRunnerOutputListener;
import org.fife.rtext.RText;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextfilechooser.Utilities;


/**
 * A text area simulating a system shell.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SystemShellTextArea extends ConsoleTextArea {

	private File pwd;
	private File prevDir;
	private final boolean isWindows;
	private transient Thread activeProcessThread;

	private static final String CD						= "cd";
	private static final String CLS						= "cls";
	private static final String CLEAR					= "clear";
	private static final String EDIT					= "edit";
	private static final String LIST_COMMANDS			= "$list";
	private static final String OPEN					= "open";
	private static final String PWD						= "pwd";

	private static final boolean CASE_SENSITIVE =
			Utilities.isCaseSensitiveFileSystem();


	public SystemShellTextArea(Plugin plugin) {
		super(plugin);
		isWindows = plugin.getRText().getOS()==RText.OS_WINDOWS;
	}


	/**
	 * Appends the prompt to the console, and resets the starting location
	 * at which the user can input text.  This method is thread-safe.
	 */
	@Override
	public void appendPrompt() {
		String prompt = pwd.getName();
		if (prompt.length()==0) { // Root directory
			prompt = pwd.getAbsolutePath();
		}
		prompt += File.separatorChar=='/' ? "$ " : "> ";
		appendImpl(prompt, STYLE_PROMPT);
	}


	@Override
	protected void fixKeyboardShortcuts() {

		super.fixKeyboardShortcuts();
		InputMap im = getInputMap();
		ActionMap am = getActionMap();

		// Tab should offer list of matching filenames, if any, like bash. 
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "completeFileName");
		am.put("completeFileName", new CompleteFileNameAction());

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSyntaxStyle() {
		return plugin.getRText().getOS()==RText.OS_WINDOWS ?
				SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH :
				SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
	}


	/**
	 * Returns the root directory containing the specified file.
	 *
	 * @param file The file.
	 * @return The file's root directory, or null if we're on Windows and
	 *         this is an NFS path.
	 */
	private static final File getRootDir(File file) {

		File root = null;

		if (File.separatorChar=='/') {
			root = new File("/");
		}
		else {
			String temp = file.getAbsolutePath();
			int colon = temp.indexOf(':');
			if (colon>-1) { // Should be '1' except for NFS paths
				root = new File(temp.substring(0, colon+1) + "\\");
			}
		}

		return root;

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getUsageNote() {
		return plugin.getString("Usage.Note.SystemShell");
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

		// Get the directory name, stripping off any surrounding quotes
		String dir = m.group(1);
		if (dir.startsWith("\"") && dir.endsWith("\"")) {
			dir = dir.substring(1, dir.length()-1);
		}

		// Special directories
		if ("-".equals(dir)) {
			dir = prevDir.getAbsolutePath();
		}
		else if ("~".equals(dir)) {
			dir = System.getProperty("user.home");
		}
		else if ("\\".equals(dir) || "/".equals(dir)) {
			File temp = getRootDir(pwd);
			if (temp==null) {
				append(plugin.getString("Error.CantFindRootDir", CD),
										STYLE_STDERR);
				appendPrompt();
				return;
			}
			dir = temp.getAbsolutePath();
		}
		else if (plugin.getRText().getOS()==RText.OS_WINDOWS &&
				dir.length()==2 && Character.isLetter(dir.charAt(0)) &&
				dir.charAt(1)==':') {
			// e.g. "cd U:" converts to "cd U:\" so it actually does something;
			// Windows does nothing when you type "cd U:" for some reason
			dir += "\\";
		}

		// If the path entered is not absolute, it should be relative to the
		// current working directory.
		File temp = new File(dir);
		if (!temp.isAbsolute()) {
			temp = new File(pwd, dir).getAbsoluteFile();
		}

		if (temp.isDirectory()) {
			prevDir = pwd;
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
	 * Handles the built-in "$list" command.
	 */
	private void handleListCommands() {
		append(plugin.getString("Usage.CommandListing"), STYLE_STDOUT);
		appendPrompt();
	}


	/**
	 * Handles the built-in "open" and "edit" commands.
	 *
	 * @param text The full command line entered.
	 */
	private void handleOpen(String text) {

		Pattern p = Pattern.compile("(\\w+)\\s+([^\\s]+|\\\".+\\\")$");
		Matcher m = p.matcher(text);
		if (!m.matches()) {
			append(plugin.getString("Error.IncorrectParamCount", EDIT),
									STYLE_STDERR);
			appendPrompt();
			return;
		}

		// Get the command, since we allow both "open" and "edit".
		String cmd = m.group(1);

		// Get the file name, stripping off any surrounding quotes
		String dir = m.group(2);
		if (dir.startsWith("\"") && dir.endsWith("\"")) {
			dir = dir.substring(1, dir.length()-1);
		}

		// If the path entered is not absolute, it should be relative to the
		// current working directory.
		File temp = new File(dir);
		if (!temp.isAbsolute()) {
			temp = new File(pwd, dir).getAbsoluteFile();
		}

		if (temp.isFile()) {
			(plugin.getRText()).openFile(temp.getAbsolutePath());
		}
		else if (temp.exists()) {
			append(plugin.getString("Error.NotAFile", cmd, dir), STYLE_STDERR);
		}
		else {
			append(plugin.getString("Error.FileDoesNotExist", cmd, dir),
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
	 * Handles the submit of an arbitrary external process.
	 *
	 * @param text The text entered by the user.
	 */
	@Override
	protected void handleSubmit(String text) {

		if (plugin.getRText().getOS()==RText.OS_WINDOWS) {

			// On Windows, allow format e.g. "cd\temp" => "cd C:\temp".
			if (text.startsWith(CD + '\\')) {
				File root = getRootDir(pwd);
				text = CD + " " + root.getAbsolutePath() +
						text.substring(CD.length()+1);
			}

			// On Windows, allow typing a drive letter to switch to that drive
			else if (text.matches("[A-Za-z]:\\\\?")) {
				if (!text.endsWith("\\")) {
					text += "\\";
				}
				text = CD + " " + text;
			}

		}

		// Check for a built-in command first
		String[] input = text.split("\\s+");
		if (CD.equals(input[0])) {
			handleCd(text);
		}
		else if (CLS.equals(input[0]) || CLEAR.equals(input[0])) {
			clear();
		}
		else if (EDIT.equals(input[0]) || OPEN.equals(input[0])) {
			handleOpen(text);
		}
		else if (LIST_COMMANDS.equals(input[0])) {
			handleListCommands();
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

			List<String> cmdList = new ArrayList<String>();
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
			final String[] cmd = cmdList.toArray(new String[cmdList.size()]);

			setEditable(false);
			activeProcessThread = new Thread() {
				@Override
				public void run() {
					ProcessRunner pr = new ProcessRunner(cmd);
					pr.setDirectory(pwd);
					pr.setOutputListener(new ProcessOutputListener());
					pr.run();
				}
			};
			firePropertyChange(PROPERTY_PROCESS_RUNNING, false, true);
			activeProcessThread.start();

		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void init() {
		pwd = new File(System.getProperty("user.home"));
		prevDir = pwd;
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
	 * Completes the file name at the caret position, or displays a list of
	 * possible matches if there is more than one.
	 */
	private class CompleteFileNameAction extends TextAction {

		public CompleteFileNameAction() {
			super("completeFileName");
		}

		public void actionPerformed(ActionEvent e) {

			String possibleFileName = getPossibleFileName();
			final String fileNamePart = getFileNamePart(possibleFileName);

			File file = new File(possibleFileName);
			if (!file.isAbsolute()) {
				file = new File(pwd, possibleFileName);
			}

			File parent = null;
			// If they've just typed a file or folder name
			if (fileNamePart.length()==0) {
				parent = file;
			}
			else {
				parent = file.getParentFile();
				if (parent==null) {
					return;
				}
			}

			File[] siblings = parent.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (!CASE_SENSITIVE) {
						name = name.toLowerCase();
					}
					return name.startsWith(fileNamePart);
				}
			});

			if (siblings.length==0) {
				return;
			}
			if (siblings.length==1) {
				int dot = getCaretPosition();
				setSelectionStart(dot-fileNamePart.length());
				setSelectionEnd(dot);
				replaceSelection(siblings[0].getName());
			}
			else {
				showChoices(siblings, getCurrentInput());
			}

		}

		private String getFileNamePart(String fileName) {
			int lastSlash = fileName.lastIndexOf('/');
			int lastBackslash = fileName.lastIndexOf('\\');
			int lastSlashIndex = Math.max(lastSlash, lastBackslash);
			String temp = fileName.substring(lastSlashIndex+1).toLowerCase();
			if (!CASE_SENSITIVE) {
				temp = temp.toLowerCase();
			}
			return temp;
		}

		private String getPossibleFileName() {

			int dot = getCaretPosition();
			Document doc = getDocument();
			Element root = doc.getDefaultRootElement();
			Element elem = root.getElement(root.getElementIndex(dot));
			int start = elem.getStartOffset();
			String text = null;
			try {
				text = doc.getText(start, dot-start);
			} catch (BadLocationException ble) { // Never happens
				ble.printStackTrace();
				return "";
			}

			int pos = text.length();
			while (pos>=0) {
				char ch = text.charAt(pos-1);
				// TODO: support spaces in paths - check for wrapping quotes.
				if (isValidFilePathChar(ch)) {
					pos--;
				}
				else {
					break;
				}
			}

			return text.substring(pos);

		}

		private final boolean isValidFilePathChar(char ch) {
			return Character.isLetterOrDigit(ch) || ch=='-' || ch=='_' ||
					ch=='/' || ch=='.' || (isWindows && (ch=='\\' || ch==':'));
		}

		private void showChoices(File[] choices, String input) {
			StringBuilder sb = new StringBuilder("\n");
			for (int i=0; i<choices.length; i++) {
				sb.append(choices[i].getName());
				if (i<choices.length-1) {
					sb.append(", ");
				}
			}
			sb.append('\n');
			appendImpl(sb.toString(), STYLE_STDOUT);
			appendPrompt();
			appendImpl(input, STYLE_STDIN, true);
		}

	}


	/**
	 * Listens for output from the currently active process and appends it
	 * to the console.
	 */
	private class ProcessOutputListener implements ProcessRunnerOutputListener{

		public void outputWritten(Process p, String output, boolean stdout) {
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


}