/*
 * 12/22/2010
 *
 * SystemShellTextArea.java - Text component simulating a system shell.
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;

import org.fife.io.ProcessRunner;
import org.fife.io.ProcessRunnerOutputListener;


/**
 * A text area simulating a system shell.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SystemShellTextArea extends ConsoleTextArea {

	private File pwd;
	private File prevDir;
	private transient Thread activeProcessThread;

	private static final String CD						= "cd";
	private static final String CLS						= "cls";
	private static final String CLEAR					= "clear";
	private static final String EDIT					= "edit";
	private static final String LIST_COMMANDS			= "$list";
	private static final String OPEN					= "open";
	private static final String PWD						= "pwd";


	public SystemShellTextArea(Plugin plugin) {
		super(plugin);
	}


	/**
	 * Appends the prompt to the console, and resets the starting location
	 * at which the user can input text.  This method is thread-safe.
	 */
	public void appendPrompt() {
		String prompt = pwd.getName();
		if (prompt.length()==0) { // Root directory
			prompt = pwd.getAbsolutePath();
		}
		prompt += File.separatorChar=='/' ? "$ " : "> ";
		appendImpl(prompt, STYLE_PROMPT);
	}


	/**
	 * Returns the root directory containing the specified file.
	 *
	 * @param file The file.
	 * @return The file's root directory, or null if we're on Windows and
	 *         this is an NFS path.
	 */
	private File getRootDir(File file) {

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
	protected void handleSubmit(String text) {

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
			firePropertyChange(PROPERTY_PROCESS_RUNNING, false, true);
			activeProcessThread.start();

		}

	}


	/**
	 * {@inheritDoc}
	 */
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