/*
 * 12/22/2010
 *
 * JavaScriptShellTextArea.java - A shell allowing you to muck with RText's
 * innards via Java's JavaScript engine.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.console;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * A shell allowing you to muck with RText's innards via Java's JavaScript
 * engine.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class JavaScriptShellTextArea extends ConsoleTextArea {

	/**
	 * Cached script bindings.
	 */
	private Bindings bindings;

	/**
	 * Script Engine for JavaScript.
	 */
	private ScriptEngine jsEngine;

	/**
	 * Whether this engine has been initialized.
	 */
	private boolean initialized;


	/**
	 * Constructor.
	 *
	 * @param plugin The console plugin.
	 */
	JavaScriptShellTextArea(Plugin plugin) {
		super(plugin);
	}


	@Override
	public void appendPrompt() {
		appendImpl("JS> ", STYLE_PROMPT);
	}


	@Override
	protected String getSyntaxStyle() {
		return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
	}


	@Override
	protected String getUsageNote() {
		return plugin.getString("Usage.Note.JsShell");
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
		if (jsEngine==null) {
			if (appendPrompt) {
				append(plugin.getString("Error.NotInitialized"), STYLE_EXCEPTION);
				appendPrompt();
			}
			return;
		}

		try {

			// We always reset the value of "rtext" and "textArea", but
			// all other variables they've modified are persistent.
			bindings.put("rtext", plugin.getRText());
			bindings.put("textArea", plugin.getRText().getMainView().getCurrentTextArea());

			Object obj = jsEngine.eval(code);
			if (obj!=null) {
				String str = obj.toString();
				if ((obj instanceof Double || obj instanceof Float) &&
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

		String[] rhinoStarts = {
			"sun.org.mozilla.javascript.internal.EcmaError: ",
			"sun.org.mozilla.javascript.internal.EvaluatorException: ",
		};
		for (String rhinoStart : rhinoStarts) {
			if (text.startsWith(rhinoStart)) {
				text = text.substring(rhinoStart.length());
				break;
			}
		}

		return text;

	}


	/**
	 * Initializes the JS scripting engine, if it hasn't already been
	 * initialized.
	 */
	private void possiblyInitialize() {

		if (initialized) {
			return;
		}
		initialized = true;

		ScriptEngineManager sem = new ScriptEngineManager();
		jsEngine = sem.getEngineByName("JavaScript");
		bindings = jsEngine.createBindings();
		jsEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

		// Write stdout and stderr to this console.  Must wrap these in
		// PrintWriters for standard print() and println() methods to work.
		ScriptContext context = jsEngine.getContext();
		context.setWriter(new PrintWriter(new OutputWriter(STYLE_STDOUT)));
		context.setErrorWriter(new PrintWriter(new OutputWriter(STYLE_STDERR)));

		// Import commonly-used packages.  Do this before stdout and
		// stderr redirecting so the user won't see it in their console.
		// Also, Nashorn requires the load() below for Rhino compatibility
		// functions such as importPackage.
		String imports = "load('nashorn:mozilla_compat.js');" +
			"importPackage(java.lang, java.io, " +
			"java.util, java.awt, javax.swing, org.fife.rtext, " +
			"org.fife.ui.rtextarea, org.fife.ui.rsyntaxtextarea)";
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
			JavaScriptShellTextArea.this.
							append(new String(buf, off, len), style);
		}
		
	}


}
