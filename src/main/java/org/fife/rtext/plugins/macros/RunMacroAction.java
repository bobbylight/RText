/*
 * 07/23/2011
 *
 * RunMacroAction.java - Action that runs a macro.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.macros;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.regex.Pattern;

import javax.script.*;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.fife.rtext.RText;
import org.fife.ui.app.AppAction;


/**
 * Action that runs a macro (script).
 *
 * @author Robert Futrell
 * @version 1.0
 */
class RunMacroAction extends AppAction<RText> {

	/**
	 * The macro plugin.
	 */
	private final MacroPlugin plugin;

	/**
	 * The macro to run.
	 */
	private final Macro macro;

	/**
	 * The script engine for JavaScript, shared across all instances of this
	 * action.
	 */
	private static ScriptEngine jsEngine;

	/**
	 * The script engine for Groovy, shared across all instances of this
	 * action.
	 */
	private static ScriptEngine groovyEngine;

	private static final Pattern GROOVY_JAR_NAME_PATTERN =
			Pattern.compile("^groovy-[\\d.]+\\.jar$");


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 * @param plugin The plugin.
	 * @param macro The macro to run.
	 */
	RunMacroAction(RText app, MacroPlugin plugin, Macro macro) {
		super(app);
		setName(macro.getName());
		this.plugin = plugin;
		String shortcut = macro.getAccelerator();
		setAccelerator(shortcut==null ? null : KeyStroke.getKeyStroke(shortcut));
		setShortDescription(macro.getDesc());
		this.macro = macro;
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		handleSubmit(macro);
	}


	private File getGroovyJar() {
		File[] pluginFiles = getPluginDir().listFiles();
		for (File file : pluginFiles) {
			if (GROOVY_JAR_NAME_PATTERN.matcher(file.getName()).matches()) {
				return file;
			}
		}
		return null;
	}


	/**
	 * Returns the directory in which to look for plugin jars.
	 *
	 * @return The plugin jar directory.
	 */
	private File getPluginDir() {
		return new File(getApplication().getInstallLocation(), "plugins");
	}


	private void handleSubmit(Macro macro) {

		// Verify that the file exists before trying to run it.
		File file = new File(macro.getFile());
		if (!file.isFile()) {
			String text = plugin.getString("Error.ScriptDoesNotExist",
									file.getAbsolutePath());
			RText app = getApplication();
			String title = app.getString("ErrorDialogTitle");
			int rc = JOptionPane.showConfirmDialog(app, text, title,
					JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			if (rc==JOptionPane.YES_OPTION) {
				MacroManager.get().removeMacro(macro);
			}
			return;
		}

		try {
			try (BufferedReader r = new BufferedReader(new FileReader(file))) {
				handleSubmit(file.getName(), r);
			}
		} catch (IOException | ScriptException e) {
			getApplication().displayException(e);
		}

	}


	private void handleSubmit(String sourceName, BufferedReader r)
			throws ScriptException {

		RText app = getApplication();

		/*
		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine jsEngine = sem.getEngineByName("JavaScript");
		Bindings bindings = jsEngine.createBindings();
		bindings.put("data", data);
		jsEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		jsEngine.eval("println(data.str); alert(data.str); data.i = 999");
		 */

		ScriptEngine engine;
		if (sourceName.endsWith(".js")) {
			engine = initJavaScriptEngine();
			if (engine==null) { // An error message was already displayed
				return;
			}
		}
		else if (sourceName.endsWith(".groovy")) {
			engine = initGroovyEngine();
			if (engine==null) { // An error message was already displayed
				return;
			}
		}
		else {
			app.displayException(new Exception("Bad macro type: " + sourceName));
			return;
		}

		// Create our bindings and cache them for later.
		Bindings bindings = engine.createBindings();
		engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

		// We always reset the value of "rtext" and "textArea", but
		// all other variables they've modified are persistent.
		bindings.put("rtext", app);
		bindings.put("textArea", app.getMainView().getCurrentTextArea());

		engine.eval(r);

	}


	/**
	 * Returns the Groovy engine, lazily creating it if necessary.
	 *
	 * @return The script engine, or <code>null</code> if it cannot be created.
	 */
	private ScriptEngine initGroovyEngine() {

		File groovyJar = getGroovyJar();
		if (groovyJar==null || !groovyJar.isFile()) {
			String message = plugin.getString("Error.NoGroovyJar",
					getPluginDir().getAbsolutePath());
			RText app = getApplication();
			String title = app.getString("ErrorDialogTitle");
			JOptionPane.showMessageDialog(app, message, title,
					JOptionPane.ERROR_MESSAGE);
			return null;
		}

		if (groovyEngine==null) {
			groovyEngine = initScriptEngineImpl("Groovy");
		}

		return groovyEngine;

	}


	/**
	 * Returns the JS engine, lazily creating it if necessary.
	 *
	 * @return The script engine, or <code>null</code> if it cannot be created.
	 */
	private ScriptEngine initJavaScriptEngine() {
		if (jsEngine==null) {
			jsEngine = initScriptEngineImpl("JavaScript");
		}
		return jsEngine;
	}


	private ScriptEngine initScriptEngineImpl(String shortName) {

		ScriptEngine engine = null;

		try {

			ScriptEngineManager sem = new ScriptEngineManager(
					this.getClass().getClassLoader());
			engine = sem.getEngineByName(shortName);
			if (engine==null) {
				showLoadingEngineError(shortName);
				return null;
			}

			// Write stdout and stderr to this console.  Must wrap these in
			// PrintWriters for standard print() and println() methods to work.
			ScriptContext context = engine.getContext();
			PrintWriter w = new PrintWriter(new OutputStreamWriter(System.out));
			context.setWriter(w);
			w = new PrintWriter(new OutputStreamWriter(System.err));
			context.setErrorWriter(w);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return engine;

	}


	/**
	 * Displays an error dialog stating that an  unknown error occurred
	 * loading the scripting engine.
	 *
	 * @param engine The name of the engine we tried to load.
	 */
	private void showLoadingEngineError(String engine) {
		String message = plugin.getString("Error.LoadingEngine", engine);
		RText app = getApplication();
		String title = app.getString("ErrorDialogTitle");
		JOptionPane.showMessageDialog(app, message, title,
				JOptionPane.ERROR_MESSAGE);
	}


}
