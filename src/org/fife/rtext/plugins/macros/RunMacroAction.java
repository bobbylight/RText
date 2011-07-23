/*
 * 07/23/2011
 *
 * RunMacroAction.java - Action that runs a macro.
 * Copyright (C) 2011 Robert Futrell
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
package org.fife.rtext.plugins.macros;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.KeyStroke;

import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;


/**
 * Action that runs a macro (script).  <code>javax.script</code> classes
 * are referenced via reflection, since we support Java 1.4 and 1.5.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RunMacroAction extends StandardAction {

	/**
	 * The macro to run.
	 */
	private Macro macro;

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
	 * The javax.script.ScriptEngineManager class.
	 */
	private Class semClazz;

	/**
	 * The javax.script.ScriptEngineManager instance.
	 */
	private Object sem;

	/**
	 * the javax.script.Scriptcontext class.
	 */
	private Class scriptContextClazz;

	/**
	 * javax.script.ScriptEngine instance for JavaScript.
	 */
	private Object jsEngine;


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 * @param tool The tool to run.
	 */
	public RunMacroAction(RText app, Macro macro) {
		super(app, macro.getName());
		setAccelerator(KeyStroke.getKeyStroke(macro.getAccelerator()));
		setShortDescription(macro.getDesc());
		this.macro = macro;
	}



	public void actionPerformed(ActionEvent e) {
		handleSubmit(macro);
	}


	private void handleSubmit(Macro macro) {

		File file = macro.getFile();
		
		try {
			BufferedReader r = new BufferedReader(new FileReader(file));
			try {
				handleSubmit(file.getName(), r);
			} finally {
				r.close();
			}
		} catch (IOException ioe) {
			getApplication().displayException(ioe);
		}

	}


	private void handleSubmit(String sourceName, BufferedReader r) {

		RText app = (RText)getApplication();

		/*
		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine jsEngine = sem.getEngineByName("JavaScript");
		Bindings bindings = jsEngine.createBindings();
		bindings.put("data", data);
		jsEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		jsEngine.eval("println(data.str); alert(data.str); data.i = 999");
		 */

		Object engine = null;
		if (sourceName.endsWith(".js")) {
			engine = initJavaScriptEngine();
		}
		else if (sourceName.endsWith(".groovy")) {
			engine = null;
		}
		if (engine==null) {
			app.displayException(new Exception("Bad macro type: " + sourceName));
			return;
		}

		// Run everything with reflection so we compile with Java 1.4.
		try {

			// Create our bindings and cache them for later.
			Method m = seClazz.getDeclaredMethod("createBindings", null);
			bindings = m.invoke(jsEngine, null);
			Class bindingsClazz = Class.forName("javax.script.Bindings");
			m = seClazz.getDeclaredMethod("setBindings",
							new Class[] { bindingsClazz, int.class });
			Field scopeField = scriptContextClazz.
										getDeclaredField("ENGINE_SCOPE");
			int scope = scopeField.getInt(scriptContextClazz);
			m.invoke(jsEngine, new Object[] { bindings, new Integer(scope) });

			// To be used in handleSubmit().
			bindingsPut = bindingsClazz.getDeclaredMethod("put",
								new Class[] { String.class, Object.class });

			// We always reset the value of "rtext" and "textArea", but
			// all other variables they've modified are persistent.
			bindingsPut.invoke(bindings,
					new Object[] { "rtext", app });
			bindingsPut.invoke(bindings, new Object[] { "textArea",
					app.getMainView().getCurrentTextArea() });

			m = seClazz.getDeclaredMethod("eval",
								new Class[] { Reader.class });
			m.invoke(jsEngine, new Object[] { r });

		} catch (Throwable ex) {
			// Since we launch via reflection, peel off top-level Exception
			if (ex instanceof InvocationTargetException) {
				ex = ex.getCause();
			}
			app.displayException(ex);
		}

	}


	protected void init() {
		// Run everything with reflection so we compile with Java 1.4.
		try {
			semClazz = Class.forName("javax.script.ScriptEngineManager");
			sem = semClazz.newInstance();
			seClazz = Class.forName("javax.script.ScriptEngine");
			scriptContextClazz = Class.forName("javax.script.ScriptContext");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Returns the Rhino engine, lazily creating it if necessary.
	 *
	 * @return The script engine, or <code>null</code> if it cannot be created.
	 */
	private Object initJavaScriptEngine() {

		// Get the Rhino engine.
		if (jsEngine==null) {

			try {

				Method m = semClazz.getDeclaredMethod("getEngineByName",
										new Class[] { String.class });
				jsEngine = m.invoke(sem, new Object[] { "JavaScript" });

				// Write stdout and stderr to this console.  Must wrap these in
				// PrintWriters for standard print() and println() methods to work.
				m = seClazz.getDeclaredMethod("getContext", null);
				Object context = m.invoke(jsEngine, null);
				m = scriptContextClazz.getDeclaredMethod("setWriter",
												new Class[] { Writer.class });
				PrintWriter w = new PrintWriter(new OutputStreamWriter(System.out));
				m.invoke(context, new Object[] { w });
				m = scriptContextClazz.getDeclaredMethod("setErrorWriter",
						new Class[] { Writer.class });
				w = new PrintWriter(new OutputStreamWriter(System.err));
				m.invoke(context, new Object[] { w });

				// Import commonly-used packages.  Do this before stdout and
				// stderr redirecting so the user won't see it in their console.
				String imports = "importPackage(java.lang);" +
							"importPackage(java.io);" +
							"importPackage(java.util);" +
							"importPackage(java.awt);" +
							"importPackage(javax.swing);" +
							"importPackage(org.fife.rtext);" +
							"importPackage(org.fife.ui.rtextarea);" +
							"importPackage(org.fife.ui.rsyntaxtextarea);";
				m = seClazz.getDeclaredMethod("eval", new Class[] { String.class });
				m.invoke(jsEngine, new Object[] { imports });

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return jsEngine;

	}


}