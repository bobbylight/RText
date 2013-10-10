/*
 * 11/05/2009
 *
 * Tool.java - An "external tool."
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fife.io.ProcessRunner;
import org.fife.io.ProcessRunnerOutputListener;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;


/**
 * An "external tool."
 *
 * @author Robert Futrell
 * @version 1.0
 */
/*
 * NOTE: In 1.5, most of these fields could be replaced with a single
 * ProcessBuilder instance.
 */
public class Tool implements Comparable<Tool> {

	private String name;
	private String desc;
	private String dir; // Not File, to ease serialization.
	private String program;
	private List<String> args;
	private Map<String, String> env;
	private boolean appendEnv;
	private String accelerator; // String to ease serialization
	private transient RText rtext;
	private Thread runThread;

	/**
	 * Synchronizes access to {@link #runThread}.
	 */
	private static Object RUN_THREAD_LOCK = new Object();

	private static final Pattern VAR_PATTERN =
			Pattern.compile("\\$\\{file_(?:name|name_no_ext|dir|full_path)\\}");


	/**
	 * Constructor.  This is really only here to make this class a JavaBean
	 * to facilitate easy serializing; the {@link #Tool(String, String)}
	 * constructor is preferred over this one.
	 */
	public Tool() {
		init();
	}


	/**
	 * Constructor.
	 *
	 * @param name The name of this tool.
	 * @param desc A description of this tool.  This may be <code>null</code>.
	 */
	public Tool(String name, String desc) {
		setName(name);
		setDescription(desc);
		init();
	}


	/**
	 * Adds a command line argument for this tool.
	 *
	 * @param arg The argument.  This cannot be <code>null</code>.
	 * @see #clearArgs()
	 * @see #setProgram(String)
	 */
	public void addArg(String arg) {
		if (arg==null) {
			throw new IllegalArgumentException("arg cannot be null");
		}
		args.add(arg);
	}


	/**
	 * Does basic checking to ensure that this program can run (the program
	 * exists, the directory to run in exists, etc.).  Applications can call
	 * this method before running this tool as a sanity check, and to give
	 * nice error messages for common failure cases.
	 *
	 * @return A localized error message, or <code>null</code> if the program
	 *         should be able to run.
	 * @see #execute(ProcessRunnerOutputListener)
	 */
	public String checkForErrors() {

		String error = null;

		// Ensure the program to run exists.
		File file = new File(varSubstitute(program));
		if (!file.isFile()) {
			error = ToolPlugin.msg.getString("Error.ProgramNotFound");
			error = MessageFormat.format(error, file.getAbsolutePath());
		}

		else {

			// Ensure the directory to run in exists
			File dir = new File(varSubstitute(getDirectory()));
			if (!dir.isDirectory()) {
				error = ToolPlugin.msg.getString("Error.NoSuchDirectory");
				error = MessageFormat.format(error, dir.getAbsolutePath());
			}

		}

		return error;

	}


	/**
	 * Clears the command line arguments.
	 *
	 * @see #addArg(String)
	 */
	public void clearArgs() {
		args.clear();
	}


	/**
	 * Clears the environment variables associated with this tool.
	 * Note that if this tool is appending its environment to RText's
	 * environment, this does not clear the RText environment that is
	 * appended to; it only clears the environment variables to add.
	 *
	 * @see #putEnvVar(String, String)
	 */
	public void clearEnvVars() {
		env.clear();
	}


	/**
	 * Compares this tool to another by name, lexicographically.
	 *
	 * @param t2 The other tool.
	 * @return The sort order of this tool, compared to another.
	 */
	public int compareTo(Tool t2) {
		int val = 1;
		if (t2==this) {
			val = 0;
		}
		else if (t2!=null) {
			val = String.CASE_INSENSITIVE_ORDER.compare(getName(),t2.getName());
		}
		return val;
	}


	/**
	 * Returns whether this tool and another have the same name.
	 *
	 * @return Whether this tool and another have the same name.
	 */
	@Override
	public boolean equals(Object o) {
		return o instanceof Tool && compareTo((Tool)o)==0;
	}


	/**
	 * Runs this tool in a separate thread.
	 *
	 * @param l Listens for events as this tool runs.  This may be
	 *        <code>null</code>.
	 * @see #checkForErrors()
	 */
	public void execute(final ProcessRunnerOutputListener l) {

		// Replace any ${file_XXX} "variables" in the command line.
		final String[] cmd = new String[1 + args.size()];
		cmd[0] = program;
		for (int i=0; i<args.size(); i++) {
			cmd[i+1] = varSubstitute(args.get(i));
		}

		// Replace any ${file_XXX} "variables" in the working directory.
		final String dir = varSubstitute(getDirectory());

		// Replace any ${file_XXX} "variables" in the environment.
		final Map<String, String> env2 = env==null ? null :
			new HashMap<String, String>(env);
		if (env2!=null) {
			for (String key : env2.keySet()) {
				env2.put(key, varSubstitute(env2.get(key)));
			}
		}

		// Run this tool in a separate thread.
		synchronized (RUN_THREAD_LOCK) {
			runThread = new Thread() {
				@Override
				public void run() {
					ProcessRunner pr = new ProcessRunner(cmd);
					pr.setDirectory(new File(dir));
					pr.setEnvironmentVars(env2, appendEnv);
					pr.setOutputListener(l);
					pr.run();
					synchronized (RUN_THREAD_LOCK) {
						runThread = null;
					}
				}
			};
			runThread.start();
		}

	}


	/**
	 * Returns the accelerator to use to activate this tool in a menu.
	 *
	 * @return The accelerator, or <code>null</code> if there is none.
	 * @see #setAccelerator(String)
	 */
	public String getAccelerator() {
		return accelerator;
	}


	/**
	 * Returns whether this tool should append any environment variables
	 * it defines to RText's current environment.
	 *
	 * @return Whether to append the environment variables defined.  If this
	 *         value is <code>false</code>, RText's environment is not
	 *         appended.
	 * @see #setAppendEnvironmentVars(boolean)
	 */
	public boolean getAppendEnvironmentVars() {
		return appendEnv;
	}


	/**
	 * Returns the command line arguments for this Tool, as an array.
	 *
	 * @return An array of command line arguments, or an empty array if there
	 *         are none.
	 * @see #setArgs(String[])
	 */
	public String[] getArgs() {
		String[] args = new String[this.args.size()];
		return this.args.toArray(args);
	}


	/**
	 * Returns a description of this tool.
	 *
	 * @return A description of this tool, or <code>null</code> if none
	 *         is defined.
	 * @see #setDescription(String)
	 */
	public String getDescription() {
		return desc;
	}


	/**
	 * Returns a copy of the environment variable map for this tool.
	 *
	 * @return The environment variables.
	 * @see #setEnvVars(Map)
	 */
	public Map<String, String> getEnvVars() {
		return new HashMap<String, String>(env);
	}


	/**
	 * Returns the name of this tool.
	 *
	 * @return The name of this tool.
	 * @see #setName(String)
	 */
	public String getName() {
		return name;
	}


	/**
	 * Returns the program to launch.
	 *
	 * @return The program to launch.
	 * @see #setProgram(String)
	 */
	public String getProgram() {
		return program;
	}


	/**
	 * Returns the directory the tool will run in.
	 *
	 * @return The directory.
	 * @see #setDirectory(String)
	 */
	public String getDirectory() {
		return dir;
	}


	/**
	 * Returns the hash code of this tool.
	 *
	 * @return This tool's hash code.
	 */
	@Override
	public int hashCode() {
		return getName().hashCode();
	}


	/**
	 * Initializes this tool.
	 */
	private void init() {
		args = new ArrayList<String>(3);
		env = new HashMap<String, String>();
	}


	/**
	 * Forcibly terminates this tool's external process, if it is running.
	 *
	 * @return If the process was running and killed.
	 */
	public boolean kill() {
		synchronized (RUN_THREAD_LOCK) {
			if (runThread!=null) {
				runThread.interrupt();
				try { // Be nice, just in case it's finishing up some work
					runThread.join(1000);
				} catch (InterruptedException ie) { // No biggie
					ie.printStackTrace();
				}
				runThread = null;
				return true;
			}
		}
		return false;
	}


	/**
	 * Sets an environment variable for this tool.
	 *
	 * @param name The name of the environment variable.
	 * @param value The value of the variable.  If this is <code>null</code>,
	 *        then this variable will not be set with a special value.
	 * @see #clearEnvVars()
	 */
	public void putEnvVar(String name, String value) {
		// env.put(name, null) will store a null value into a HashMap
		if (value!=null) {
			env.put(name, value);
		}
		else {
			env.remove(name);
		}
	}


	/**
	 * Copied from 1.5's Matcher.quoteReplacement() method, since we work in
	 * 1.4.
	 *
	 * @param s The string to be used in a Matcher.appendReplacement() call.
	 * @return The string, with slashes ('<tt>\</tt>') and dollar signs
	 * ('<tt>$</tt>') quoted.
	 */
	// TODO: When we drop 1.4 support, remove this method.
	private static String quoteReplacement(String s) {
		if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1))
			return s;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\\') {
				sb.append('\\');
				sb.append('\\');
			} else if (c == '$') {
				sb.append('\\');
				sb.append('$');
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}


	/**
	 * Sets the accelerator to use to activate this tool in a menu.
	 *
	 * @param accelerator The accelerator to use, or <code>null</code> for
	 *        none.
	 * @see #getAccelerator()
	 */
	public void setAccelerator(String accelerator) {
		this.accelerator = accelerator;
	}


	/**
	 * Sets whether this tool should append any environment variables
	 * it defines to RText's current environment.
	 *
	 * @param append Whether to append the environment variables defined.  If
	 *        this value is <code>false</code>, RText's environment is not
	 *        appended.
	 * @see #getAppendEnvironmentVars()
	 */
	public void setAppendEnvironmentVars(boolean append) {
		this.appendEnv = append;
	}


	/**
	 * Sets the command line arguments to this tool.  Old command line arguments
	 * are discarded.
	 *
	 * @param args The new command line arguments.
	 * @see #getArgs()
	 */
	public void setArgs(String[] args) {
		clearArgs();
		if (args!=null) {
			for (int i=0; i<args.length; i++) {
				addArg(args[i]);
			}
		}
	}


	/**
	 * Sets a description of this tool.
	 *
	 * @param desc A description of this tool.  This may be <code>null</code>.
	 * @see #getDescription()
	 */
	public void setDescription(String desc) {
		this.desc = desc;
	}


	/**
	 * Sets the directory for this tool to run in.
	 *
	 * @param dir The directory.  This cannot be <code>null</code>.
	 * @see #getDirectory()
	 */
	public void setDirectory(String dir) {
		if (dir==null) {
			throw new IllegalArgumentException("dir cannot be null");
		}
		this.dir = dir;
	}


	/**
	 * Sets the environment variables for this tool.
	 *
	 * @param vars The new environment variables for this tool.
	 * @see #getEnvVars()
	 */
	public void setEnvVars(Map<String, String> vars) {
		env.clear();
		env.putAll(vars);
	}


	/**
	 * Sets the name of this tool.
	 *
	 * @param name The name of this tool.
	 * @see #getName()
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * Sets the program to launch.
	 *
	 * @param program The program.  This cannot be <code>null</code>.
	 * @see #getProgram()
	 * @see #addArg(String)
	 */
	public void setProgram(String program) {
		if (program==null) {
			throw new IllegalArgumentException("program cannot be null");
		}
		this.program = program;
	}


	/**
	 * Sets the parent application that handles "variable" substitution in
	 * arguments and environment variables before execution.
	 *
	 * @param rtext The parent application.
	 */
	void setRText(RText rtext) {
		this.rtext = rtext;
	}


	/**
	 * Replaces any "variables" having to do with the current text area
	 * in the specified string.
	 *
	 * @param str The string.
	 * @return The same string, with any variables replaced with their values.
	 */
	private String varSubstitute(String str) {

		// Bail early if on (likely) variables
		if (str.indexOf("${file_")==-1) {
			return str;
		}

		RTextEditorPane textArea = rtext.getMainView().getCurrentTextArea();
		Matcher m = VAR_PATTERN.matcher(str);
		StringBuffer sb = new StringBuffer();

		while (m.find()) {
			String var = m.group(0);
			String temp = null;
			if ("${file_name}".equals(var)) {
				temp = textArea.getFileName();
			}
			else if ("${file_name_no_ext}".equals(var)) {
				temp = textArea.getFileName();
				int dot = temp.lastIndexOf('.');
				if (dot>-1) {
					temp = temp.substring(0, dot);
				}
			}
			else if ("${file_dir}".equals(var)) {
				temp = textArea.getFileFullPath();
				int slash = temp.lastIndexOf('/');
				slash = Math.max(slash, temp.lastIndexOf('\\'));
				temp = temp.substring(0, slash);
			}
			else if ("${file_full_path}".equals(var)) {
				temp = textArea.getFileFullPath();
			}
			m.appendReplacement(sb, quoteReplacement(temp));
		}

		m.appendTail(sb);
		return sb.toString();

	}


	public static void main(String[] args) {
		Tool tool = new Tool("Name", "Desc");
		tool.setProgram("C:/temp/test.bat");
		tool.execute(new ProcessRunnerOutputListener() {
			public void outputWritten(Process p, String output, boolean stdout){
				System.out.println(output);
			}
			public void processCompleted(Process p, int rc, Throwable e) {
				if (e!=null) {
					System.out.println("Error completing process:");
					e.printStackTrace();
				}
				else {
					System.out.println("Completed, rc=" + rc);
				}
			}
		});
	}


}