/*
 * 12/22/2010
 *
 * JavaScriptShellTextArea.java - A shell allowing you to muck with RText's
 * innards via Rhino.
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fife.rtext.RTextUtilities;


/**
 * A shell allowing you to muck with RText's innards via Rhino.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class JavaScriptShellTextArea extends ConsoleTextArea {

	/**
	 * The javax.script.Bindings instance.
	 */
	private Object bindings;

	/**
	 * The javax.script.Bindings#put() method.
	 */
	private Method bindingsPut;

	/**
	 * javax.script.ScriptEngine class.
	 */
	private Class seClazz;

	/**
	 * javax.script.ScriptEngine instance for JavaScript.
	 */
	private Object jsEngine;

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
	public void appendPrompt() {
		appendImpl("Rhino> ", STYLE_PROMPT);
	}


	/**
	 * {@inheritDoc}
	 */
	protected String getUsageNote() {
		return plugin.getString("Usage.Note.JsShell");
	}


	/**
	 * Submits the entered JavaScript code.
	 */
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

		// Failed to initialize - likely Java 1.4 or 1.5
		if (jsEngine==null) {
			if (appendPrompt) {
				append(plugin.getString("Error.NotInitialized"), STYLE_EXCEPTION);
				appendPrompt();
			}
			return;
		}

		String code = text;

		/*
		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine jsEngine = sem.getEngineByName("JavaScript");
		Bindings bindings = jsEngine.createBindings();
		bindings.put("data", data);
		jsEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		jsEngine.eval("println(data.str); alert(data.str); data.i = 999");
		 */

		// Run everything with reflection so we compile with Java 1.4.
		try {

			// We always reset the value of "rtext" and "textArea", but
			// all other variables they've modified are persistent.
			bindingsPut.invoke(bindings,
					new Object[] { "rtext", plugin.getRText() });
			bindingsPut.invoke(bindings, new Object[] { "textArea",
					plugin.getRText().getMainView().getCurrentTextArea() });

			Method m = seClazz.getDeclaredMethod("eval",
								new Class[] { String.class });
			m.invoke(jsEngine, new Object[] { code });

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			append(sw.toString(), STYLE_EXCEPTION);
		}

		if (appendPrompt) {
			appendPrompt();
		}

	}


	protected void init() {
		// Do nothing; we do this lazily on the first statement submitted to
		// avoid loading the JS scripting engine.
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

		if (!RTextUtilities.isPreJava6()) {

			// Run everything with reflection so we compile with Java 1.4.
			try {

				Class semClazz = Class.forName("javax.script.ScriptEngineManager");
				Object sem = semClazz.newInstance();

				// Get the Rhino engine.
				Method m = semClazz.getDeclaredMethod("getEngineByName",
											new Class[] { String.class });
				jsEngine = m.invoke(sem, new Object[] { "JavaScript" });
				seClazz = Class.forName("javax.script.ScriptEngine");

				// Create our bindings and cache them for later.
				m = seClazz.getDeclaredMethod("createBindings", null);
				bindings = m.invoke(jsEngine, null);
				Class bindingsClazz = Class.forName("javax.script.Bindings");
				m = seClazz.getDeclaredMethod("setBindings",
								new Class[] { bindingsClazz, int.class });
				Class scriptContextClazz = Class.forName(
											"javax.script.ScriptContext");
				Field scopeField = scriptContextClazz.
											getDeclaredField("ENGINE_SCOPE");
				int scope = scopeField.getInt(scriptContextClazz);
				m.invoke(jsEngine, new Object[] { bindings, new Integer(scope) });

				// To be used in handleSubmit().
				bindingsPut = bindingsClazz.getDeclaredMethod("put",
								new Class[] { String.class, Object.class });

				// Write stdout and stderr to this console.  Must wrap these in
				// PrintWriters for standard print() and println() methods to work.
				m = seClazz.getDeclaredMethod("getContext", null);
				Object context = m.invoke(jsEngine, null);
				m = scriptContextClazz.getDeclaredMethod("setWriter",
											new Class[] { Writer.class });
				PrintWriter w = new PrintWriter(new OutputWriter(STYLE_STDOUT));
				m.invoke(context, new Object[] { w });
				m = scriptContextClazz.getDeclaredMethod("setErrorWriter",
						new Class[] { Writer.class });
				w = new PrintWriter(new OutputWriter(STYLE_STDERR));
				m.invoke(context, new Object[] { w });

				// Import commonly-used packages.  Do this before stdout and
				// stderr redirecting so the user won't see it in their console.
				handleSubmitImpl("importPackage(java.lang, java.io, java.util, " +
					"java.awt, java.swing, org.fife.rtext, org.fife.ui.rtextarea, " +
					"org.fife.ui.rsyntaxtextarea)", false);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}


	/**
	 * Listens for output from the script and prints it to this console.
	 */
	private class OutputWriter extends Writer {

		private String style;

		public OutputWriter(String style) {
			this.style = style;
		}

		public void close() throws IOException {
			// Do nothing
		}

		public void flush() throws IOException {
			// Do nothing
		}

		public void write(char[] buf, int off, int len) {
			// "JavaScriptShellTextArea.this." needed to
			// compile in Java 5+, but but in Java 1.4.
			JavaScriptShellTextArea.this.
							append(new String(buf, off, len), style);
		}
		
	}


}