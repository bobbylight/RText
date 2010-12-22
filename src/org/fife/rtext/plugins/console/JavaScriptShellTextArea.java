/*
 * 12/22/2010
 *
 * JavaScriptShellTextArea.java - A shell allowing you to muck with RText's
 * innards via Rhino.
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


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

		appendPrompt();

	}


	protected void init() {

		// Run everything with reflection so we compile with Java 1.4.
		try {

			Class semClazz = Class.forName("javax.script.ScriptEngineManager");
			Object sem = semClazz.newInstance();

			Method m = semClazz.getDeclaredMethod("getEngineByName",
										new Class[] { String.class });
			jsEngine = m.invoke(sem, new Object[] { "JavaScript" });
			seClazz = Class.forName("javax.script.ScriptEngine");

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

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}