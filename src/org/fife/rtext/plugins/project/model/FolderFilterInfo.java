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
	private Pattern[] allowedFilePatterns;
	private Pattern[] disallowedFilePatterns;
	private Pattern[] disallowedDirPatterns;


	public FolderFilterInfo() {
	}


	public FolderFilterInfo(String[] allowedFileFilters,
			String[] disallowedFileFilters, String[] disallowedDirectories) {
		setAllowedFileFilters(allowedFileFilters);
		setDisallowedDirectoryFilters(disallowedDirectories);
		setDisallowedFileFilters(disallowedFileFilters);
	}


	public String[] getAllowedFileFilters() {
		return allowedFileFilters==null ? null :
			(String[])allowedFileFilters.clone();
	}


	public String[] getDisallowedDirectories() {
		return disallowedDirectories==null ? null :
			(String[])disallowedDirectories.clone();
	}


	public String[] getDisallowedFileFilters() {
		return disallowedFileFilters==null ? null :
			(String[])disallowedFileFilters.clone();
	}


	/**
	 * Returns whether a file or directory should be displayed.
	 *
	 * @param file The file or directory.
	 * @return Whether this filter set allows it to be displayed.
	 */
	public boolean isAllowed(File file) {

		String name = file.getName();

		if (allowedFileFilters!=null) {
			if (allowedFilePatterns==null) {
				allowedFilePatterns = wildcardToRegex(allowedFileFilters);
			}
			if (!matches(name, allowedFilePatterns)) {
				return false;
			}
		}

		if (disallowedDirectories!=null && file.isDirectory()) {
			if (disallowedDirPatterns==null) {
				disallowedDirPatterns = wildcardToRegex(disallowedDirectories);
			}
			if (matches(name, disallowedDirPatterns)) {
				return false;
			}
		}
		else if (disallowedFileFilters!=null) {
			if (disallowedFilePatterns==null) {
				disallowedFilePatterns = wildcardToRegex(disallowedFileFilters);
			}
			if (matches(name, disallowedFilePatterns)) {
				return false;
			}
		}

		return true;

	}


	private boolean matches(String fileName, Pattern[] patterns) {
		for (int i=0; i<patterns.length; i++) {
			if (patterns[i].matcher(fileName).matches()) {
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


	public void setDisallowedDirectoryFilters(String[] filters) {
		if (filters!=null && filters.length==1 && filters[0].length()==0) {
			filters = null;
		}
		this.disallowedDirectories = filters;
		this.disallowedDirPatterns = null;
	}


	public void setDisallowedFileFilters(String[] filters) {
		if (filters!=null && filters.length==1 && filters[0].length()==0) {
			filters = null;
		}
		this.disallowedFileFilters = filters;
		this.disallowedFilePatterns = null;
	}


	/**
	 * Converts an array of wildcard file filters into an array of regex
	 * patterns matching them.
	 *
	 * @param filters The file filters.
	 * @return The equivalent regex patterns.
	 */
	private static final Pattern[] wildcardToRegex(String[] filters) {
		Pattern[] patterns = null;
		if (filters!=null && filters.length>0) {
			patterns = new Pattern[filters.length];
			for (int i=0; i<filters.length; i++) {
				patterns[i] = RTextUtilities.
						getPatternForFileFilter(filters[i], true);
			}
		}
		return patterns;
	}


}