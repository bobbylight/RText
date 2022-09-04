/*
 * 12/22/2010
 *
 * JavaScriptShellTextArea.java - A shell allowing you to muck with RText's
 * innards via Java's JavaScript engine.
 * Copyright (C) 2010 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.console;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.script.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;


/**
 * A shell allowing you to muck with RText's innards via JRuby.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class RubyShellTextArea extends ConsoleTextArea {

	/**
	 * Cached script bindings.
	 */
	private Bindings bindings;

	/**
	 * Script Engine for Ruby.
	 */
	private ScriptEngine rubyEngine;

	/**
	 * Used for {@code "_"} variable support.
	 */
	private Object lastResult;

	/**
	 * Whether this engine has been initialized.
	 */
	private boolean initialized;

	/**
	 * The version of Ruby supported by JRuby.
	 */
	private String rubyVersion;


	/**
	 * Constructor.
	 *
	 * @param plugin The console plugin.
	 */
	RubyShellTextArea(Plugin plugin) {
		super(plugin);
	}


	@Override
	public void appendPrompt() {
		appendImpl("ruby> ", STYLE_PROMPT);
	}


	private String getRubyVersion() {
		if (rubyVersion == null) {
			rubyVersion = "2.6.8";
			/*
			try {
				rubyVersion = rubyEngine.eval("RUBY_VERSION").toString();
			} catch (ScriptException se) {
				se.printStackTrace();
				rubyVersion = "unknown";
			}
		 	*/
		}
		return rubyVersion;
	}


	@Override
	protected String getSyntaxStyle() {
		return SyntaxConstants.SYNTAX_STYLE_RUBY;
	}


	@Override
	protected String getUsageNote() {
		return plugin.getString("Usage.Note.RubyShell", getRubyVersion());
	}


	/**
	 * Submits the entered JavaScript code.
	 */
	@Override
	protected void handleSubmit(String text) {
		handleSubmitImpl(text, true);
	}


	/**
	 * Submits the entered JavaScript code.
	 *
	 * @param code The text to submit.
	 * @param appendPrompt Whether another prompt should be added to the text
	 *        area after the command completes.
	 */
	private void handleSubmitImpl(String code, boolean appendPrompt) {

		possiblyInitialize();

		// Failed to initialize
		if (rubyEngine == null) {
			if (appendPrompt) {
				append(plugin.getString("Error.NotInitialized"), STYLE_EXCEPTION);
				appendPrompt();
			}
			return;
		}

		try {

			// We always reset the value of "rtext" and "textArea", but
			// all other variables they've modified are persistent.
			bindings.put("rtext", plugin.getApplication());
			bindings.put("textArea", plugin.getApplication().getMainView().getCurrentTextArea());
			bindings.put("_", lastResult);

			lastResult = rubyEngine.eval(code);
			if (lastResult!=null) {
				String str = lastResult.toString();
				if ((lastResult instanceof Double || lastResult instanceof Float) &&
						str.endsWith(".0")) {
					str = str.substring(0, str.length() - 2);
				}
				append(str, STYLE_STDOUT);
			}

		} catch (Exception e) {
			// Peel off wrapper ScriptException
			if (e instanceof ScriptException) {
				append(massageScriptException((ScriptException)e), STYLE_STDERR);
			}
			else {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				append(sw.toString(), STYLE_EXCEPTION);
			}
		}

		if (appendPrompt) {
			appendPrompt();
		}

	}


	/**
	 * Tries to strip the name of the exception that was wrapped by the
	 * <code>javax.script.ScriptException</code>.
	 *
	 * @param e The exception to massage.
	 * @return The error message to display for the exception in the console.
	 */
	private static String massageScriptException(ScriptException e) {

		String text = e.getMessage();

		String[] rubyErrorStarts = {
			"org.jruby.exceptions.SyntaxError: ",
			"Error during evaluation of Ruby in <script> at line 1: ",
		};
		for (String rhinoStart : rubyErrorStarts) {
			if (text.startsWith(rhinoStart)) {
				text = text.substring(rhinoStart.length());
				break;
			}
		}

		return text;

	}


	/**
	 * Initializes the Ruby scripting engine, if it hasn't already been
	 * initialized.
	 */
	private void possiblyInitialize() {

		if (initialized) {
			return;
		}
		initialized = true;

		ScriptEngineManager sem = new ScriptEngineManager();
		rubyEngine = sem.getEngineByName("jruby");
		bindings = rubyEngine.createBindings();
		rubyEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

		// Write stdout and stderr to this console.  Must wrap these in
		// PrintWriters for standard print() and println() methods to work.
		ScriptContext context = rubyEngine.getContext();
		context.setWriter(new PrintWriter(new OutputWriter(STYLE_STDOUT)));
		context.setErrorWriter(new PrintWriter(new OutputWriter(STYLE_STDERR)));

		String imports = "require 'java'";
		handleSubmitImpl(imports, false);
	}


	/**
	 * Listens for output from the script and prints it to this console.
	 */
	private class OutputWriter extends Writer {

		private final String style;

		OutputWriter(String style) {
			this.style = style;
		}

		@Override
		public void close() {
			// Do nothing
		}

		@Override
		public void flush() {
			// Do nothing
		}

		@Override
		public void write(char[] buf, int off, int len) {
			RubyShellTextArea.this.
							append(new String(buf, off, len), style);
		}

	}


}
