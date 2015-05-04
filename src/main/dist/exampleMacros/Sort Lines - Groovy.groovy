/*
 * This is a Groovy macro for RText that sorts all lines in the active editor.
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
final def removeDuplicates = true // Change to "false" if you want to keep duplicates

// Note: You'll want to consider wrapping your scripts inside calls to
// beginAtomicEdit() and endAtomicEdit(), so the actions they perform can
// be undone with a single Undo action.
textArea.beginAtomicEdit()
try {

	def lines = textArea.text.split("\n")

	if (removeDuplicates) {
		def ts = new TreeSet()
		lines.each {
			ts.add(it)
		}
		lines = ts.toArray()
	}

	Arrays.sort(lines)
	textArea.text = lines.join("\n")

} finally {
	textArea.endAtomicEdit()
}
