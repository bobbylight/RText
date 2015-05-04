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
 *   http://javadoc.fifesoft.com/rtext/
 *   http://javadoc.fifesoft.com/rsyntaxtextarea/
 *
 */

var removeDuplicates = true; // Change to "false" if you want to keep duplicates

function join(lines) {
	var sb = new java.lang.StringBuffer();
	if (lines!=null && lines.length>0) {
		for (var i=0; i<lines.length; i++) {
			//System.out.println(lines[i]);
			sb.append(lines[i]).append('\n');
		}
	}
	return sb.toString();
}


// Note: You'll want to consider wrapping your scripts inside calls to
// beginAtomicEdit() and endAtomicEdit(), so the actions they perform can
// be undone with a single Undo action.
textArea.beginAtomicEdit();
try {

	var lines = textArea.text.split("\n");

	if (removeDuplicates) {
		var ts = new java.util.TreeSet();
		for (var i=0; i<lines.length; i++) {
			ts.add(lines[i]);
		}
		lines = ts.toArray();
	}

	java.util.Arrays.sort(lines);
	textArea.text = join(lines);

} finally {
	textArea.endAtomicEdit();
}
