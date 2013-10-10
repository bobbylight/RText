/*
 * 03/08/2004
 *
 * SyntaxFilters.java - Manages a list of wildcard file filters and what
 * syntax highlighting styles they map to.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Manages a list of wildcard file filters and what syntax highlighting
 * styles they map to.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class SyntaxFilters implements SyntaxConstants {

	/**
	 * One filter set for every file type except plain text.  This is a
	 * mapping from styles to <code>List</code>s of filename patterns.
	 */
	private Map<String, List<String>> filters;

	/**
	 * Filters added by plugins for additional languages.
	 */
	private Map<String, List<String>> addedFilters;

	/**
	 * Creates a new <code>SyntaxFilters</code> with default values for
	 * all filters.
	 */
	public SyntaxFilters() {
		this(null);
	}


	/**
	 * Creates a new <code>SyntaxFilters</code> from the given string.
	 *
	 * @param filterStr Must be a <code>String</code> generated from
	 *        <code>SyntaxFilter.toString()</code>.  If its format is invalid,
	 *        then default filter strings are used for all syntax styles.
	 */
	public SyntaxFilters(String filterStr) {

		// One filter set for every file type except plain text.
		filters = new HashMap<String, List<String>>();
		restoreDefaultFileFilters();

		if (filterStr!=null) {
			int oldCommaPos = 0;
			int commaPos = filterStr.indexOf(',', 0);
			try {
				while (commaPos!=-1) {
					String temp = filterStr.substring(oldCommaPos,commaPos);
					// Windows registry replaces '/' with '\'.
					int slash = temp.indexOf('\\');
					if (slash>-1) {
						temp = temp.substring(0, slash) + "/" + temp.substring(slash+1);
					}
					int colon = temp.indexOf(':');
					if (colon>-1) {
						String style = temp.substring(0, colon);
						setFiltersForSyntaxStyle(style, temp.substring(colon+1));
					}
					else { // Should never happen (except updating older RTexts)
						System.err.println("Invalid saved filter string.  Using default syntax filters");
						restoreDefaultFileFilters();
						return;
					}
					oldCommaPos = commaPos + 1;
					commaPos = filterStr.indexOf(',', oldCommaPos);
				}
				// Get the last one (with no trailing comma).
				int colon = filterStr.indexOf(':', oldCommaPos);
				if (colon>-1) {
					String style = filterStr.substring(oldCommaPos, colon);
					setFiltersForSyntaxStyle(style, filterStr.substring(colon+1)); 
				}
				else {
					System.err.println("Invalid saved filter string.  Using default syntax filters");
					restoreDefaultFileFilters();
				}
			} catch (IllegalArgumentException iae) {
				// This can happen if a newer RText is opened and closed (so
				// its filters are saved), then an old one is opened - the old
				// one has problems parsing the new one's saved filters.
				System.err.println("Error with syntax style, using defaults: " + iae.getMessage());
				restoreDefaultFileFilters();
			}
		}

	}


	/**
	 * Adds a file filter for a given syntax style.
	 *
	 * @param style The syntax style to add a file filter to.
	 * @param filter The filter to add.  If <code>null</code>, nothing will
	 *        be done.
	 * @throws IllegalArgumentException If <code>style</code> is invalid.
	 */
	public void addFileFilter(String style, String filter) {
		getFiltersForStyle(style).add(filter);
	}


	/**
	 * Creates a value for an entry in the <code>filters</code>
	 * <code>Map</code>.
	 *
	 * @param values An array of values to go into the value.
	 * @return The value.
	 */
	private static final List<String> createValue(String[] values) {
		List<String> list = new ArrayList<String>();
		for (String value : values) {
			list.add(value);
		}
		return list;
	}


	/**
	 * Returns the list of filename filters for highlighting with the given
	 * style.
	 *
	 * @param style The style.
	 * @return The list of filters.
	 */
	private List<String> getFiltersForStyle(String style) {
		List<String> l = filters.get(style);
		if (l==null) {
			// Allow plugins to add filters for new languages not built into
			// RSyntaxTextArea.
			if (addedFilters==null) {
				addedFilters = new HashMap<String, List<String>>();
			}
			else {
				l = addedFilters.get(style);
			}
			if (l==null) {
				l = new ArrayList<String>();
				addedFilters.put(style, l);
			}
		}
		return l;
	}


	/**
	 * Returns a list of all wildcard file filters associated with this
	 * syntax type, separated by spaces.  For example, if the C++ syntax
	 * style has filters <code>*.cpp</code> and <code>*.h</code> associated
	 * with it, then <code>getFilterString(SYNTAX_STYLE_CPLUSPLUS)</code>
	 * would return <code>"*.cpp *.h"</code>.
	 *
	 * @param style The syntax style to check.
	 * @throws IllegalArgumentException If <code>style</code> is invalid.
	 */
	public String getFilterString(String style) {
		StringBuilder filterString = new StringBuilder();
		List<String> filters = getFiltersForStyle(style);
		for (String filter : filters) {
			filterString.append(filter).append(' ');
		}
		return filterString.toString();
	}


	/**
	 * Returns the type of syntax highlighting to use for the given text
	 * file based on its extension and the current filters.
	 *
	 * @param fileName The file to syntax highlight.
	 * @param ignoreBackupExtensions Whether to ignore ".bak", ".old" and
	 *        ".orig" extensions, if they exist.
	 * @return The syntax highlighting scheme to use, for example,
	 *         <code>SYNTAX_STYLE_JAVA</code> or
	 *         <code>SYNTAX_STYLE_CPLUSPLUS</code>.
	 * @see org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
	 * @see org.fife.ui.rsyntaxtextarea.SyntaxConstants
	 */
	public String getSyntaxStyleForFile(String fileName,
									boolean ignoreBackupExtensions) {

		if (fileName==null) {
			return SYNTAX_STYLE_NONE;
		}

		// fileName is usually the full path to the file from RText, so
		// shorten it to make regex faster (and work correctly for things
		// without wildcards like "makefile").
		int lastSlash = fileName.lastIndexOf(File.separatorChar);
		if (lastSlash>-1) {
			fileName = fileName.substring(lastSlash+1);
		}
		fileName = fileName.toLowerCase(); // Ignore casing of extensions

		// Ignore extensions that mean "this is a backup", but don't
		// denote the actual file type.
		if (ignoreBackupExtensions) {
			fileName = RTextUtilities.stripBackupExtensions(fileName);
		}

		String style = getSyntaxStyleForFileImpl(fileName, filters);
		if (style==null && addedFilters!=null) {
			style = getSyntaxStyleForFileImpl(fileName, addedFilters);
		}
		return style!=null ? style : SYNTAX_STYLE_NONE;

	}


	/**
	 * Looks for a syntax style for a file name in a given map.
	 *
	 * @param fileName The file name.
	 * @param filters The map of filters.
	 * @return The syntax style for the file, or <code>null</code> if not found
	 *         in that map.
	 */
	private static final String getSyntaxStyleForFileImpl(String fileName,
			Map<String, List<String>> filters) {
		for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
			for (String filter : entry.getValue()) {
				Pattern p = RTextUtilities.getPatternForFileFilter(
									filter, true);
				if (p!=null && p.matcher(fileName).matches()) {
					return entry.getKey();
				}
			}
		}
		return null;
	}


	/**
	 * Returns <code>true</code> if and only if the file filter string passed
	 * in is "valid".  Currently valid file filter strings contain only the
	 * following characters: A-Z, a-z, 0-9, '*', '?', '.', '-', '_', ' ', '$'.
	 *
	 * @param fileFilterString The file filter string to test.
	 * @return <code>true</code> if the file filter string is valid, false
	 *         otherwise.
	 */
	public static boolean isValidFileFilterString(String fileFilterString) {
		int length = fileFilterString.length();
		for (int i=0; i<length; i++) {
			char c = fileFilterString.charAt(i);
			switch (c) {
				case '*':
				case '?':
				case '.':
				case '-':
				case '_':
				case '$':
				case ' ':
					continue;
				default:
					if (!Character.isLetterOrDigit(c)) {
						return false;
					}
					break;
			}
		}
		return true;
	}


	/**
	 * Sets default values for syntax filters.
	 */
	public void restoreDefaultFileFilters() {

		filters.clear();

		//filters.put(SYNTAX_STYLE_NONE,			createValue());
		filters.put(SYNTAX_STYLE_ACTIONSCRIPT,		createValue(new String[] { "*.as", "*.asc" }));
		filters.put(SYNTAX_STYLE_ASSEMBLER_X86,		createValue(new String[] { "*.asm" }));
		filters.put(SYNTAX_STYLE_BBCODE,			createValue(new String[] { "*.bbc" }));
		filters.put(SYNTAX_STYLE_C,				createValue(new String[] { "*.c" }));
		filters.put(SYNTAX_STYLE_CLOJURE,			createValue(new String[] { "*.clj" }));
		filters.put(SYNTAX_STYLE_CPLUSPLUS,		createValue(new String[] { "*.cpp", "*.cxx", "*.h" }));
		filters.put(SYNTAX_STYLE_CSHARP,			createValue(new String[] { "*.cs" }));
		filters.put(SYNTAX_STYLE_CSS,				createValue(new String[] { "*.css" }));
		filters.put(SYNTAX_STYLE_DELPHI,				createValue(new String[] { "*.pas" }));
		filters.put(SYNTAX_STYLE_DTD,				createValue(new String[] { "*.dtd" }));
		filters.put(SYNTAX_STYLE_FORTRAN,			createValue(new String[] { "*.f", "*.for", "*.fort", "*.f77", "*.f90" }));
		filters.put(SYNTAX_STYLE_GROOVY,			createValue(new String[] { "*.groovy", "*.grv" }));
		filters.put(SYNTAX_STYLE_HTML,			createValue(new String[] { "*.htm", "*.html" }));
		filters.put(SYNTAX_STYLE_JAVA,			createValue(new String[] { "*.java" }));
		filters.put(SYNTAX_STYLE_JAVASCRIPT,		createValue(new String[] { "*.js" }));
		filters.put(SYNTAX_STYLE_JSP,				createValue(new String[] { "*.jsp" }));
		filters.put(SYNTAX_STYLE_JSON,				createValue(new String[] { "*.json" }));
		filters.put(SYNTAX_STYLE_LATEX,				createValue(new String[] { "*.tex", "*.ltx", "*.latex" }));
		filters.put(SYNTAX_STYLE_LISP, 				createValue(new String[] { "*.cl", "*.clisp", "*.el", "*.l", "*.lisp", "*.lsp", "*.ml" }));
		filters.put(SYNTAX_STYLE_LUA,				createValue(new String[] { "*.lua" }));
		filters.put(SYNTAX_STYLE_MAKEFILE,			createValue(new String[] { "Makefile", "makefile" }));
		filters.put(SYNTAX_STYLE_MXML,				createValue(new String[] { "*.mxml" }));
		filters.put(SYNTAX_STYLE_NSIS,				createValue(new String[] { "*.nsi" }));
		filters.put(SYNTAX_STYLE_PERL,			createValue(new String[] { "*.perl", "*.pl", "*.pm" }));
		filters.put(SYNTAX_STYLE_PHP,				createValue(new String[] { "*.php" }));
		filters.put(SYNTAX_STYLE_PROPERTIES_FILE,	createValue(new String[] { "*.properties", "*.ini" }));
		filters.put(SYNTAX_STYLE_PYTHON,			createValue(new String[] { "*.py" }));
		filters.put(SYNTAX_STYLE_RUBY,			createValue(new String[] { "*.rb" }));
		filters.put(SYNTAX_STYLE_SAS,				createValue(new String[] { "*.sas" }));
		filters.put(SYNTAX_STYLE_SCALA,				createValue(new String[] { "*.scala" }));
		filters.put(SYNTAX_STYLE_SQL,				createValue(new String[] { "*.sql" }));
		filters.put(SYNTAX_STYLE_TCL,				createValue(new String[] { "*.tcl", "*.tk" }));
		filters.put(SYNTAX_STYLE_UNIX_SHELL,		createValue(new String[] { "*.sh", "*.?sh" }));
		filters.put(SYNTAX_STYLE_VISUAL_BASIC,		createValue(new String[] { "*.vb" }));
		filters.put(SYNTAX_STYLE_WINDOWS_BATCH,		createValue(new String[] { "*.bat", "*.cmd" }));
		filters.put(SYNTAX_STYLE_XML,				createValue(new String[] { "*.xml", "*.xsl", "*.xsd", "*.wsdl", "*.macro", "*.manifest" }));

		// Keep any filters added by the user

	}


	/**
	 * Sets all file filters for a given syntax style.
	 *
	 * @param style The syntax style to add a file filter to.
	 * @param filterString A string representing the file filters separated
	 *        by spaces.  If <code>null</code>, nothing happens.
	 * @throws IllegalArgumentException If <code>style</code> is invalid.
	 */
	public void setFiltersForSyntaxStyle(String style, String filterString) {

		List<String> filters = getFiltersForStyle(style);
		filters.clear();
		
		int oldSpacePos = 0;
		int spacePos = filterString.indexOf(' ', 0);
		while (spacePos!=-1) {
			if (spacePos>oldSpacePos+1) {
				filters.add(filterString.substring(oldSpacePos,spacePos));
			}
			oldSpacePos = spacePos + 1;
			spacePos = filterString.indexOf(' ', oldSpacePos);
		}
		if (oldSpacePos<filterString.length()-1) {
			filters.add(filterString.substring(oldSpacePos));
		}
		
	}


	/**
	 * Sets values for all the filters not added by plugins.
	 *
	 * @param filters The new values for filters.
	 */
	public void setPreservingPluginAdded(SyntaxFilters filters) {
		this.filters = new HashMap<String, List<String>>(filters.filters);
	}


	/**
	 * Returns this object as a string.  Note that we currently do not save
	 * plugin-defined syntax filters.
	 *
	 * @return A string representing this <code>SyntaxFilters</code>.
	 */
	@Override
	public String toString() {
		String retVal = "";
		for (String style : filters.keySet()) {
			retVal += style + ":" + getFilterString(style) + ",";
		}
		// Get rid of the last comma.
		retVal = retVal.substring(0,retVal.length()-1);
		return retVal;
	}


}