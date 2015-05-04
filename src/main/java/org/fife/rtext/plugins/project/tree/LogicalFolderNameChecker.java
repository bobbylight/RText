package org.fife.rtext.plugins.project.tree;

/**
 * Ensures that proposed project names are valid.
 */
class LogicalFolderNameChecker implements NameChecker {

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