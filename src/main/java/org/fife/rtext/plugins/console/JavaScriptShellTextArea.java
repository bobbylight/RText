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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.fife.ui.UIUtil;
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
	public JavaScriptShellTextArea(Plugin plugin) {
		super(plugin);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void appendPrompt() {
		appendImpl("JS> ", STYLE_PROMPT);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSyntaxStyle() {
		return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
	}


	/**
	 * {@inheritDoc}
	 */
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
	 * @param text The text to submit.
	 * @param appendPrompt Whether another prompt should be added to the text
	 *        area after the command completes.
	 */
	private void handleSubmitImpl(String text, boolean appendPrompt) {

		possiblyInitialize();

		// Failed to initialize
		if (jsEngine==null) {
			if (appendPrompt) {
				append(plugin.getString("Error.NotInitialized"), STYLE_EXCEPTION);
				appendPrompt();
			}
			return;
		}

		String code = text;

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
	private static final String massageScriptException(ScriptException e) {

		String text = e.getMessage();

		String[] rhinoStarts = {
			"sun.org.mozilla.javascript.internal.EcmaError: ",
			"sun.org.mozilla.javascript.internal.EvaluatorException: ",
		};
		for (int i=0; i<rhinoStarts.length; i++) {
			if (text.startsWith(rhinoStarts[i])) {
				text = text.substring(rhinoStarts[i].length());
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
		StringBuilder imports = new StringBuilder();
		if (!UIUtil.isPreJava8()) {
			// Nashorn requires this for Rhino compatibility functions
			// such as importPackage
			imports.append("load('nashorn:mozilla_compat.js');");
		}
		imports.append("importPackage(java.lang, java.io, " +
				"java.util, java.awt, javax.swing, org.fife.rtext, " +
				"org.fife.ui.rtextarea, org.fife.ui.rsyntaxtextarea)");
		handleSubmitImpl(imports.toString(), false);

	}


	/**
	 * Listens for output from the script and prints it to this console.
	 */
	private class OutputWriter extends Writer {

		private String style;

		public OutputWriter(String style) {
			this.style = style;
		}

		@Override
		public void close() throws IOException {
			// Do nothing
		}

		@Override
		public void flush() throws IOException {
			// Do nothing
		}

		@Override
		public void write(char[] buf, int off, int len) {
			JavaScriptShellTextArea.this.
							append(new String(buf, off, len), style);
		}
		
	}


}