/*
 * This is a JavaScript macro for RText that sorts all lines in the
 * active editor.
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

const removeDuplicates = false; // Change to "true" if you want to remove duplicates

// Note: You'll want to consider wrapping your scripts inside calls to
// beginAtomicEdit() and endAtomicEdit(), so the actions they perform can
// be undone with a single Undo action.
textArea.beginAtomicEdit();
try {

	let lines = textArea.getText().split('\n');
	if (removeDuplicates) {
		lines = [...new Set(lines)];
	}

	textArea.setText(lines.sort().join('\n'));

} finally {
	textArea.endAtomicEdit();
}
