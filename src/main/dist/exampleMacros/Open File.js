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

	const fileName = textArea.getSelectedText();
	if (fileName==null || fileName.isEmpty) {
		javax.swing.JOptionPane.showMessageDialog(rtext,
				"Couldn't open file:  No selection.\n" +
				'A file name must be selected in the active editor to open a file.',
				'Error', javax.swing.JOptionPane.ERROR_MESSAGE);
	}
	else {

		const isUrl = fileName.startsWith('http://') || fileName.startsWith('https://');

		let file = new java.io.File(fileName);
		if (!isUrl && !file.isAbsolute()) {
			const parentDir = new java.io.File(textArea.getFileFullPath()).parentFile;
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
			const chooser = rtext.getFileChooser();
			chooser.setCurrentDirectory(file);
			rtext.getAction(org.fife.rtext.RTextActionInfo.OPEN_ACTION).actionPerformed(null);
		}
		else {
			javax.swing.JOptionPane.showMessageDialog(rtext,
					`File does not exist:\n${file.getAbsolutePath()}`, 'Error',
					javax.swing.JOptionPane.ERROR_MESSAGE);
		}

	}

} finally {
	textArea.endAtomicEdit();
}
