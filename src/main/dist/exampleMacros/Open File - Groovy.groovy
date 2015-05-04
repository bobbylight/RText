/*
 * This is a Groovy macro for RText that checks the selection in the active
 * editor and, if there is one, tries to open it as a file in its own editor.
 *
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
import java.awt.*
import javax.swing.*
import org.fife.rtext.*

textArea.beginAtomicEdit()
try {

	def fileName = textArea.selectedText
	if (fileName==null || fileName.length()==0) {
		JOptionPane.showMessageDialog(rtext,
				"""Couldn't open file:  No selection.
A file name must be selected in the active editor to open a file.""",
				"Error", JOptionPane.ERROR_MESSAGE)
	}
	else {

		def isUrl = fileName.startsWith("http://")

		def file = new File(fileName)
		if (!file.isAbsolute()) {
			def parentDir = new File(textArea.fileFullPath).parentFile
			file = new File(parentDir, fileName)
		}

		// Easter egg - if this is a URL, open it in a browser
		if (isUrl) {
			Desktop.desktop.browse(new URL(fileName).toURI())
		}
		else if (file.isFile()) {
			rtext.openFile(file.absolutePath)
		}
		else if (file.isDirectory()) {
			def chooser = rtext.fileChooser
			chooser.currentDirectory = file
			rtext.getAction(RTextActionInfo.OPEN_ACTION).actionPerformed(null)
		}
		else {
			JOptionPane.showMessageDialog(rtext,
					"File does not exist:\n" + file.absolutePath, "Error",
					JOptionPane.ERROR_MESSAGE)
		}

	}

} finally {
	textArea.endAtomicEdit()
}
