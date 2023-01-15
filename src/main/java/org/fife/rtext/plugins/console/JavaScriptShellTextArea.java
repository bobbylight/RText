/*
 * 12/22/2010
 *
 * JavaScriptShellTextArea.java - A shell allowing you to muck with RText's
 * innards via JavaScript (via Graal).
 * Copyright (C) 2010 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.console;

import java.io.*;
import java.nio.charset.Charset;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;


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
	private Value bindings;

	/**
	 * The GraalVM JavaScript context.
	 */
	private Context context;

	private ConsoleOutputStream stdout;

	private ConsoleOutputStream stderr;

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
		appendImpl("js> ", STYLE_PROMPT);
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

		try {
			possiblyInitialize();
		} catch (Throwable t) {
			// Shouldn't happen, but Graal uses e.g. sun.misc.unsafe, so we need all the info we can
			// get to debug issues with new JRE versions, etc.
			plugin.getApplication().displayException(t);
		}

		// Failed to initialize
		if (context == null) {
			if (appendPrompt) {
				append(plugin.getString("Error.NotInitialized"), STYLE_STDERR);
				appendPrompt();
			}
			return;
		}

		try {

			// We always reset the value of "rtext" and "textArea", but
			// all other variables they've modified are persistent.
			bindings.putMember("rtext", plugin.getApplication());
			bindings.putMember("textArea", plugin.getApplication().getMainView().getCurrentTextArea());

			Value obj = context.eval("js", code);
			stdout.flush();
			stderr.flush();

			if (obj != null) {
				String str = obj.toString();
				append(str, STYLE_RESULT);
			}

		} catch (PolyglotException pe) {
			append(pe.getMessage(), STYLE_STDERR);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			append(sw.toString(), STYLE_STDERR);
		}

		if (appendPrompt) {
			appendPrompt();
		}

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

		stdout = new ConsoleOutputStream(STYLE_STDOUT);
		stderr = new ConsoleOutputStream(STYLE_STDERR);

		context = Context.newBuilder("js")
			.allowAllAccess(true)
			.option("engine.WarnInterpreterOnly", "false")
			.out(stdout)
			.err(stderr)
			.build();
		bindings = context.getBindings("js");

	}



	/**
	 * Listens for output from the script and prints it to this console.
	 */
	private class ConsoleOutputStream extends OutputStream {

		private final ByteArrayOutputStream baos;
		private final String style;

		ConsoleOutputStream(String style) {
			this.style = style;
			baos = new ByteArrayOutputStream();
		}

		@Override
		public void close() {
			// Do nothing
		}

		@Override
		public void flush() {
			if (baos.size() > 0) {
				String content = baos.toString(Charset.defaultCharset());
				append(content, style);
				baos.reset();
			}
		}

		@Override
		public void write(int b) {
			baos.write(b);
			// Be nice and write each line. This does assume all output
			// is based on ASCII, e.g. cp1252 or a Unicode, but the
			// final flush would take care of anything that isn't anyway
			if (b == '\n') {
				flush();
			}
		}

		@Override
		public void write(byte[] b, int off, int len) {
			baos.write(b, off, len);
		}

	}

}
