/*
 * 09/08/2012
 *
 * LogicalFolderNameChecker.java - Ensures that proposed project names are valid.
 * Copyright (C) 2012 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project.tree;

/**
 * Ensures that proposed project names are valid.
 */
class LogicalFolderNameChecker implements NameChecker {

	@Override
	public String isValid(String text) {
		int length = text.length();
		if (length==0) {
			return "empty";
		}
		for (int i=0; i<length; i++) {
			char ch = text.charAt(i);
			if (ch=='<' || ch=='>' || ch=='&') {
				return "invalidLogicalFolderNameChars";
			}
		}
		return null;
	}

}
