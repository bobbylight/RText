/*
 * 02/15/2013
 *
 * JsonPrettyPrinter - Pretty-prints JSON.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tidy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Pretty-prints a JSON string.  Currently delegates to the
 * <a href="jsonbeans.googlecode.com">jsonbeans</a> library.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class JsonPrettyPrinter implements PrettyPrinter {

	private Plugin plugin;

	private static final String JSON_CLASS =
			"com.esotericsoftware.jsonbeans.Json";
	private static final String OUTPUT_TYPE_CLASS =
			"com.esotericsoftware.jsonbeans.OutputType";


	JsonPrettyPrinter(Plugin plugin) {
		this.plugin = plugin;
	}


	/**
	 * Returns the output format to use.
	 *
	 * @return The output format.
	 */
	private Object getFormat(String format) throws Exception {

		// TODO: Read this from preferences in Options dialog!

		Class clazz = Class.forName(OUTPUT_TYPE_CLASS, true,
				plugin.getClass().getClassLoader());
		Field field = clazz.getDeclaredField(format);
		return field.get(null);

	}


	public PrettyPrintResult prettyPrint(String json) throws Exception {

		Object format = getFormat("json");

		// We use reflection since jsonbeans is built with Java 6.
		Class clazz = Class.forName(JSON_CLASS, true,
				plugin.getClass().getClassLoader());
		Object obj = clazz.newInstance();
		Method sotm = clazz.getMethod("setOutputType",
				new Class[] { format.getClass() });
		Method ppm  = clazz.getMethod("prettyPrint",
				new Class[] { String.class });
		try {
			sotm.invoke(obj, new Object[] { format });
			String result = (String)ppm.invoke(obj,
					new Object[] { json });
			return new PrettyPrintResult(RESULT_OK, result, null);
		} catch (Exception e) {
			return new PrettyPrintResult(RESULT_ERRORS, null, e.getMessage());
		}

	}


}
