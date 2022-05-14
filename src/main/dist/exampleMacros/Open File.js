/*
 * This is a JavaScript macro for RText that checks the selection in the
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
 *   https://javadoc.fifesoft.com/rtext/
 *   https://javadoc.fifesoft.com/rsyntaxtextarea/
 *
 */

textArea.beginAtomicEdit();
try {

	var fileName = textArea.selectedText;
	if (fileName==null || fileName.isEmpty()) {
		javax.swing.JOptionPane.showMessageDialog(rtext,
				"Couldn't open file:  No selection.\n" +
				"A file name must be selected in the active editor to open a file.",
				"Error", javax.swing.JOptionPane.ERROR_MESSAGE);
	}
	else {

		var isUrl = fileName.startsWith("http://");

		var file = new java.io.File(fileName);
		if (!file.isAbsolute()) {
			var parentDir = new java.io.File(textArea.fileFullPath).parentFile;
			file = new java.io.File(parentDir, fileName);
		}

		// Easter egg - if this is a URL, open it in a browser
		if (isUrl) {
			java.awt.Desktop.getDesktop().browse(new java.net.URL(fileName).toURI());
		}
		else if (file.isFile()) {
			rtext.openFile(file);
		}
		else if (file.isDirectory()) {
			var chooser = rtext.fileChooser;
			chooser.currentDirectory = file;
			rtext.getAction(org.fife.rtext.RTextActionInfo.OPEN_ACTION).actionPerformed(null);
		}
		else {
			javax.swing.JOptionPane.showMessageDialog(rtext,
					"File does not exist:\n" + file.absolutePath, "Error",
					javax.swing.JOptionPane.ERROR_MESSAGE);
		}

	}

} finally {
	textArea.endAtomicEdit();
}
