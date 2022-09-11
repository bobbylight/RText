/*
 * This is a JavaScript macro for RText that replaces any selected text
 * with a version of that text that is escaped for HTML.
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

function replaceMultipleSpaces(text) {
	const p = java.util.regex.Pattern.compile('  +');
	const m = p.matcher(text);
	const sb = new java.lang.StringBuilder();
	while (m.find()) {
		const spaces = m.group();
		m.appendReplacement(sb, spaces.replace(' ', '&nbsp;'));
	}
	m.appendTail(sb);
	return sb.toString();
}

textArea.beginAtomicEdit();
try {

	let text = textArea.getSelectedText();
	if (!text) {
		javax.swing.JOptionPane.showMessageDialog(rtext,
				'Error:  No selection.\n' +
				'Text must be selected to HTML-ify.',
				'Error', javax.swing.JOptionPane.ERROR_MESSAGE);
	}
	else {
		text = text.replace('&', '&amp;').replace('"', '&quot;').
				replace('<', '&lt;').replace('>', '&gt;').
				replace('\t', '&#009;').replace('\n', '<br>\n');
		if (text.indexOf('  ') > -1) { // Replace multiple spaces with &nbsp; sequences
			text = replaceMultipleSpaces(text);
		}
		const start = textArea.getSelectionStart();
		textArea.replaceSelection(text);
		textArea.setSelectionStart(start);
		textArea.setSelectionEnd(start + text.length);
	}

} finally {
	textArea.endAtomicEdit();
}
