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
import java.util.*;

import org.fife.ui.rsyntaxtextarea.FileTypeUtil;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Manages a list of wildcard file filters and what syntax highlighting
 * styles they map to.
 *
 * @author Robert Futrell
 * @version 1.0
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
		filters = new HashMap<>();
		restoreDefaultFileFilters();

		if (filterStr!=null) {
			int oldCommaPos = 0;
			int commaPos = filterStr.indexOf(',');
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
	 * Returns the list of filename filters for highlighting with the given
	 * style.
	 *
	 * @param style The style.
	 * @return The list of filters.  This may be an empty list but will
	 *         never be {@code null}.
	 * @see #setFiltersForSyntaxStyle(String, String)
	 */
	private List<String> getFiltersForStyle(String style) {

		List<String> l = filters.get(style);

		// Allow plugins to add filters for new languages not built into
		// RSyntaxTextArea.  They're kept in a separate Map to avoid
		// clearing them out when resetting the main app's filters.
		if (l == null) {
			if (addedFilters==null) {
				addedFilters = new HashMap<>();
			}
			l = addedFilters.computeIfAbsent(style, s -> new ArrayList<>());
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
	 * @return The list of wildcard file filters.
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
	 * Looks for the syntax style to use for a given file.
	 *
	 * @param fileName The file name.
	 * @param ignoreBackupExtensions Whether to ignore backup extensions.
	 * @return The syntax style for the file, or <code>null</code> if not found
	 *         in that map.
	 */
	public String getSyntaxStyleForFile(String fileName,
										boolean ignoreBackupExtensions) {

		if (fileName == null) {
			return SyntaxConstants.SYNTAX_STYLE_NONE;
		}

		Map<String, List<String>> allFilters = new HashMap<>(filters);
		if (addedFilters != null) {
			allFilters.putAll(addedFilters);
		}
		return FileTypeUtil.get().guessContentType(new File(fileName),
			allFilters, ignoreBackupExtensions);
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
		filters = FileTypeUtil.get().getDefaultContentTypeToFilterMap();
		// Keep any filters added by the user
	}


	/**
	 * Sets all file filters for a given syntax style.
	 *
	 * @param style The syntax style to add a file filter to.
	 * @param filterString A string representing the file filters separated
	 *        by spaces.  If <code>null</code>, nothing happens.
	 * @throws IllegalArgumentException If <code>style</code> is invalid.
	 * @see #getFiltersForStyle(String)
	 */
	public void setFiltersForSyntaxStyle(String style, String filterString) {

		List<String> filters = getFiltersForStyle(style);
		filters.clear();

		int oldSpacePos = 0;
		int spacePos = filterString.indexOf(' ');
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
		this.filters = new HashMap<>(filters.filters);
	}


	/**
	 * Returns this object as a string.  Note that we currently do not save
	 * plugin-defined syntax filters.
	 *
	 * @return A string representing this <code>SyntaxFilters</code>.
	 */
	@Override
	public String toString() {

		StringBuilder retVal = new StringBuilder();

		for (String style : filters.keySet()) {
			retVal.append(style).append(":").append(getFilterString(style)).append(",");
		}

		// Get rid of the last comma.
		retVal = new StringBuilder(retVal.substring(0, retVal.length() - 1));
		return retVal.toString();
	}


}
