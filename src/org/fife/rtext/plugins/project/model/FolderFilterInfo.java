/*
 * 09/12/2012
 *
 * FolderFilterInfo.java - Information about what files a folder in the
 * project tree should display.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.model;

import java.io.File;
import java.util.regex.Pattern;

import org.fife.rtext.RTextUtilities;


/**
 * Information about what children to display in a folder tree view.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FolderFilterInfo {

	private String[] allowedFileFilters;
	private String[] disallowedFileFilters;
	private String[] disallowedDirectories;
	private NameMatcher[] allowedFilePatterns;
	private NameMatcher[] disallowedFilePatterns;
	private NameMatcher[] disallowedDirPatterns;

	
	public FolderFilterInfo() {
	}


	public FolderFilterInfo(String[] allowedFileFilters,
			String[] hiddenFileFilters, String[] hiddenFolderFilters) {
		setAllowedFileFilters(allowedFileFilters);
		setHiddenFolderFilters(hiddenFolderFilters);
		setHiddenFileFilters(hiddenFileFilters);
	}


	public String[] getAllowedFileFilters() {
		return allowedFileFilters==null ? null :
			(String[])allowedFileFilters.clone();
	}


	public String[] getHiddenFolderFilters() {
		return disallowedDirectories==null ? null :
			(String[])disallowedDirectories.clone();
	}


	public String[] getHiddenFileFilters() {
		return disallowedFileFilters==null ? null :
			(String[])disallowedFileFilters.clone();
	}


	/**
	 * Returns whether a file or directory should be displayed.
	 *
	 * @param file The file or directory.
	 * @param isDir Whether the file is a directory (passed in for performance).
	 * @return Whether this filter set allows it to be displayed.
	 */
	public boolean isAllowed(File file, boolean isDir) {

		String name = file.getName();

		if (isDir) {
			if (disallowedDirectories!=null && isDir) {
				if (disallowedDirPatterns==null) {
					disallowedDirPatterns = wildcardToMatcher(disallowedDirectories);
				}
				if (matches(name, disallowedDirPatterns)) {
					return false;
				}
			}
		}

		else { // Regular file

			if (allowedFileFilters!=null) {
				if (allowedFilePatterns==null) {
					allowedFilePatterns = wildcardToMatcher(allowedFileFilters);
				}
				if (!matches(name, allowedFilePatterns)) {
					return false;
				}
			}

			if (disallowedFileFilters!=null) {
				if (disallowedFilePatterns==null) {
					disallowedFilePatterns = wildcardToMatcher(disallowedFileFilters);
				}
				if (matches(name, disallowedFilePatterns)) {
					return false;
				}
			}

		}

		return true;

	}


	/**
	 * Returns whether a filter can be checked via a string literal.
	 *
	 * @param filter The filter.
	 * @return Whether a string literal comparison can be used for the filter.
	 */
	private static final boolean isStringLiteral(String filter) {
		for (int i=0; i<filter.length(); i++) {
			char ch = filter.charAt(i);
			if (!(Character.isLetterOrDigit(ch) || ch=='.' || ch==' ' ||
					ch=='-' || ch=='_')) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Returns whether a file name matches any of an array of name matchers.
	 *
	 * @param fileName The file name to check.
	 * @param matchers The array of matchers to check against.
	 * @return Whether the file name matched any of the matchers.
	 */
	private static final boolean matches(String fileName,
			NameMatcher[] matchers) {
		for (int i=0; i<matchers.length; i++) {
			if (matchers[i].matches(fileName)) {
				return true;
			}
		}
		return false;
	}


	public void setAllowedFileFilters(String[] filters) {
		if (filters!=null && filters.length==1 &&
				("*".equals(filters[0]) || filters[0].length()==0)) {
			filters = null;
		}
		this.allowedFileFilters = filters;
		this.allowedFilePatterns = null;
	}


	public void setHiddenFolderFilters(String[] filters) {
		if (filters!=null && filters.length==1 && filters[0].length()==0) {
			filters = null;
		}
		this.disallowedDirectories = filters;
		this.disallowedDirPatterns = null;
	}


	public void setHiddenFileFilters(String[] filters) {
		if (filters!=null && filters.length==1 && filters[0].length()==0) {
			filters = null;
		}
		this.disallowedFileFilters = filters;
		this.disallowedFilePatterns = null;
	}


	/**
	 * Converts an array of wildcard file filters into an array of matchers
	 * for them.
	 *
	 * @param filters The file filters.
	 * @return The equivalent matchers
	 */
	private static final NameMatcher[] wildcardToMatcher(String[] filters) {
		NameMatcher[] matchers = null;
		if (filters!=null && filters.length>0) {
			matchers = new NameMatcher[filters.length];
			for (int i=0; i<filters.length; i++) {
				if (isStringLiteral(filters[i])) {
					matchers[i] = new StringLiteralNameMatcher(filters[i]);
				}
				else {
					Pattern pattern = RTextUtilities.
							getPatternForFileFilter(filters[i], true);
					matchers[i] = new RegexNameMatcher(pattern);
				}
			}
		}
		return matchers;
	}


	/**
	 * Returns whether a file name is acceptable by some criteria.
	 */
	private static interface NameMatcher {

		boolean matches(String text);

	}


	/**
	 * Returns whether a file name matches a regular expression.
	 */
	private static class RegexNameMatcher implements NameMatcher {

		private Pattern pattern;

		public RegexNameMatcher(Pattern pattern) {
			this.pattern = pattern;
		}

		public boolean matches(String text) {
			return pattern.matcher(text).matches();
		}

	}


	/**
	 * Returns whether a file name matches a string literal.
	 */
	private static class StringLiteralNameMatcher implements NameMatcher {

		private String literal;

		public StringLiteralNameMatcher(String literal) {
			this.literal = literal;
		}

		public boolean matches(String text) {
			return RTextUtilities.isFileSystemCaseSensitive() ?
					literal.equals(text) : literal.equalsIgnoreCase(text);
		}

	}


}