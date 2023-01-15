/*
 * 02/15/2013
 *
 * JsonPrettyPrinter - Pretty-prints JSON.
 * Copyright (C) 2013 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tidy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.swing.JOptionPane;

import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.jsonbeans.OutputType;
import org.fife.rtext.RText;


/**
 * Pretty-prints a JSON string.  Currently delegates to the
 * <a href="jsonbeans.googlecode.com">jsonbeans</a> library.
 *
 * @author Robert Futrell
 * @version 1.1
 */
class JsonPrettyPrinter implements PrettyPrinter {

	private final Plugin plugin;


	JsonPrettyPrinter(Plugin plugin) {
		this.plugin = plugin;
	}


	/**
	 * Converts any leading tabs on each line of the specified text into
	 * spaces, using a "tab replacement" string.
	 *
	 * @param text The text to scan for leading tabs.
	 * @param tabReplacement The string of space characters to replace
	 *        leading tabs with.
	 * @return The new string.
	 */
	private static String convertLeadingTabs(String text,
			String tabReplacement) {

		BufferedReader r = new BufferedReader(new StringReader(text));
		StringWriter w = new StringWriter();

		try {
			String line;
			while ((line=r.readLine())!=null) {
				int index = 0;
				while (index<line.length() && line.charAt(index)=='\t') {
					w.write(tabReplacement);
					index++;
				}
				w.write(line.substring(index));
				w.write('\n');
			}
			r.close();
		} catch (IOException ioe) { // Never happens
			ioe.printStackTrace();
		}

		return w.toString();

	}


	/**
	 * Creates a string of space characters of a specific length.
	 *
	 * @param spaceCount The number of spaces.
	 * @return The string of space characters.
	 */
	private static String createSpacer(int spaceCount) {
		return " ".repeat(spaceCount);
	}


	/**
	 * Returns the first non-whitespace character in a string.
	 *
	 * @param text The string to search through.
	 * @return The first non-whitespace character, or <code>'\0'</code> if none
	 *         is found.
	 */
	private static char getFirstNonWhitespaceChar(String text) {
		for (int i=0; i<text.length(); i++) {
			char ch = text.charAt(i);
			if (!Character.isWhitespace(ch)) {
				return ch;
			}
		}
		return 0;
	}


	/**
	 * Returns the output format to use.
	 *
	 * @return The output format.
	 */
	private OutputType getOutputType(String format) {

		try {
			return OutputType.valueOf(format.toLowerCase());
		} catch (IllegalArgumentException e) {
			return OutputType.json;
		}
	}


	/**
	 * Indents the "first level" of the top-level JSON object.  This method
	 * assumes that the text is already-formatted JSON, and is an object (as
	 * opposed to an array).
	 *
	 * @param text The JSON to indent.
	 * @param indenter The text to use as the indention (a tab or spaces).
	 * @return The indented text.
	 */
	private static String indentFirstLevel(String text, String indenter) {
		String[] lines = text.split("\n");
		for (int i=1; i<lines.length-1; i++) {
			lines[i] = indenter + lines[i];
		}
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line).append('\n');
		}
		return sb.toString();
	}


	@Override
	public PrettyPrintResult prettyPrint(String json) {

		// First, do a sanity check to ensure everything is wrapped in curlys.
		char ch = getFirstNonWhitespaceChar(json);
		if (ch!='{' && ch!='[') {
			RText rtext = plugin.getApplication();
			String msg = Plugin.MSG.getString("Dialog.Confirm.Json.WrapInCurlys");
			String title = rtext.getString("ConfDialogTitle");
			int rc = JOptionPane.showConfirmDialog(rtext, msg, title,
					JOptionPane.YES_NO_OPTION);
			if (rc!=JOptionPane.YES_OPTION) {
				return new PrettyPrintResult(RESULT_ERRORS, null, null);
			}
			json = '{' + json + '}';
		}

		// jsonbeans doesn't pretty-print arrays (only objects), see Issue 2
		// at http://code.google.com/p/jsonbeans
		boolean curlysAddedForParsingOnly = false;
		if (ch=='[') {
			curlysAddedForParsingOnly = true;
			json = "{\"foo\":" + json + '}';
		}

		JsonOptions opts = plugin.getJsonOptions();

		Json jsonFormatter = new Json();
		jsonFormatter.setOutputType(getOutputType(opts.getOutputStyle()));

		try {

			String result = jsonFormatter.prettyPrint(json);

			int spaceCount = opts.getSpaceCount();
			if (spaceCount>-1) {
				String spacer = createSpacer(spaceCount);
				result = convertLeadingTabs(result, spacer);
			}

			// Complete the workaround for jsonbeans not formatting arrays
			if (curlysAddedForParsingOnly) {
				int arrayStart = result.indexOf('[');
				if (arrayStart>-1) { // Should always be true
					result = result.substring(arrayStart);
				}
				int arrayEnd = result.lastIndexOf(']');
				if (arrayEnd>-1) { // Should always be true too
					result = result.substring(0, arrayEnd+1);
				}
			}
			else if (opts.getIndentFirstLevel()) {
				String indenter = spaceCount==-1 ? "\t" :
					createSpacer(spaceCount);
				result = indentFirstLevel(result, indenter);
			}

			return new PrettyPrintResult(RESULT_OK, result, null);

		} catch (Throwable e) {
			return new PrettyPrintResult(RESULT_ERRORS, null, e.getMessage());
		}

	}


}
