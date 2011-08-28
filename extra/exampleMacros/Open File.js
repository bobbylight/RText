/*
 * This is a Rhino (Javascript) macro for RText that checks the selection in the
 * active editor and, if there is one, tries to open it as a file in its own
 * editor.
 *
 * Global variables include:
 *   - rtext:           The focused application window, an instance of
 *                      org.fife.rtext.RText.
 *   - textArea:        The active text area, an instance of
 *                      org.fife.rtext.RTextEditorPane.
 *
 * You can use the entire RText and RSyntaxTextArea public API's:
 *   http://javadoc.fifesoft.com/rtext/
 *   http://javadoc.fifesoft.com/rsyntaxtextarea/
 *
 */
importPackage(java.awt, java.io, java.lang, java.net, javax.swing);
importPackage(org.fife.rtext);

textArea.beginAtomicEdit();
try {

	var fileName = textArea.selectedText;
	if (fileName==null || fileName.length()==0) {
		JOptionPane.showMessageDialog(rtext,
				"Couldn't open file:  No selection.\n" +
				"A file name must be selected in the active editor to open a file.",
				"Error", JOptionPane.ERROR_MESSAGE);
	}
	else {

		var isUrl = fileName.startsWith("http://");

		var file = new File(fileName);
		if (!file.isAbsolute()) {
			var parentDir = new File(textArea.fileFullPath).parentFile;
			file = new File(parentDir, fileName);
		}

		// Easter egg - if this is a URL, open it in a browser
		if (isUrl) {
			Desktop.getDesktop().browse(new URL(fileName).toURI());
		}
		else if (file.isFile()) {
			rtext.openFile(file.absolutePath);
		}
		else if (file.isDirectory()) {
			var chooser = rtext.fileChooser;
			chooser.currentDirectory = file;
			rtext.getAction(RTextActionInfo.OPEN_ACTION).actionPerformed(null);
		}
		else {
			JOptionPane.showMessageDialog(rtext,
					"File does not exist:\n" + file.absolutePath, "Error",
					JOptionPane.ERROR_MESSAGE);
		}

	}

} finally {
	textArea.endAtomicEdit();
}
