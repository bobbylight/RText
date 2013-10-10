/*
 * 10/03/2005
 *
 * FindInFilesThread.java - Thread that does the searching for a
 * Find in Files dialog.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;

import org.fife.io.UnicodeReader;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.GUIWorkerThread;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;


/**
 * A thread created by a <code>FindInFilesDialog</code> to do the searching.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see FindInFilesDialog
 */
class FindInFilesThread extends GUIWorkerThread {

	protected static final String NO_LINE_NUMBER	= "--";

	protected FindInFilesDialog dialog;
	protected File directory;

	private String verboseLabelString;
	private String errorLabelString;
	protected String verboseNoFiltMatchString;
	protected String dontSearchSubfoldersString;
	protected String newFilesToExamineString;	
	protected String occurrencesString;

//	private static final Pattern TAB_PATTERN	= Pattern.compile("\\t");


	/**
	 * Constructor.
	 *
	 * @param dialog The "find in files" dialog.
	 * @param directory The directory in which to search.
	 */
	public FindInFilesThread(FindInFilesDialog dialog, File directory) {

		this.dialog = dialog;
		this.directory = directory;

		verboseLabelString = "<html><em>" + dialog.getString2("VerboseLabel") +
							"</em>";
		errorLabelString = "<html><em>" + dialog.getString2("ErrorLabel") +
							"</em>";
		verboseNoFiltMatchString = dialog.getString2("VerboseNoFiltMatch");
		dontSearchSubfoldersString = dialog.getString2("SearchSubFoldUnchecked");
		newFilesToExamineString = dialog.getString2("NewFilesToExamine");
		occurrencesString = dialog.getString2("Occurrences");

	}


	protected MatchData createErrorMatchData(String filePath, String msg) {
		return new MatchData(filePath, NO_LINE_NUMBER, errorLabelString + msg,
							MatchData.TYPE_ERROR);
	}


	protected MatchData createVerboseMatchData(String filePath, String msg) {
		return new MatchData(filePath, NO_LINE_NUMBER,
					verboseLabelString + msg, MatchData.TYPE_VERBOSE);
	}


	/**
	 * Runs the search.
	 */
	@Override
	public Object construct() {

		RText parent = (RText)dialog.getOwner();
		AbstractMainView view = parent.getMainView();

		// Get the string to search for and filters for the files to search.
		String searchString = dialog.getSearchString();
		Pattern[] filterStrings = getFilterStrings();
		if (filterStrings==null) {
			dialog.searchCompleted("");
			return null;
		}

		// Then, do the search.
		dialog.clearSearchResults();
		File[] files = directory.listFiles();
		List<File> fileList = new ArrayList<File>();
		fileList.addAll(Arrays.asList(files));

		boolean checkSubfolders = dialog.getCheckSubfolders();
		boolean matchingLines = dialog.getShowMatchingLines();
		boolean matchCase = dialog.getMatchCase();
		boolean wholeWord = dialog.getMatchWholeWord();
		boolean useRegex = dialog.getUseRegEx();
		boolean doVerboseOutput = dialog.getDoVerboseOutput();
		Segment seg = new Segment();
		String searchingFile = dialog.getString2("SearchingFile");

		if (!useRegex && !matchCase)
			searchString = searchString.toLowerCase();

		RSyntaxTextArea textArea = new RSyntaxTextArea();
		long startMillis = System.currentTimeMillis();

		// Keep looping while there are more files to search.
		int numFiles = fileList.size();
		for (int i=0; i<numFiles; i++) {

			// If the user canceled the search...
			if (Thread.currentThread().isInterrupted()) {
				dialog.searchCompleted(dialog.getString2("SearchTerminated"));
				return null;
			}

			File temp = fileList.get(i);
			String fileFullPath = temp.getAbsolutePath();

			// If temp is a regular file (i.e., non-directory) AND exists...
			if (temp.isFile()) {

				// If the file doesn't match one of the filters from
				// "In files:", skip it.
				if (isFilteredOut(temp.getName(), filterStrings)) {
					if (doVerboseOutput) {
						MatchData data = createVerboseMatchData(
								fileFullPath, verboseNoFiltMatchString);
						dialog.addMatchData(data);
					}
					continue;
				}

				// Display the file we're searching in the status bar.
				// Note that this method postpones the update to the EDT.
				dialog.setStatusText(searchingFile + i + "/" + numFiles +
								": " + fileFullPath);

				try {
					// Use a UnicodeReader to auto-detect whether this
					// is a Unicode file.
					// FIXME:  Allow the user to specify the default
					// encoding, instead of assuming system default,
					// somehow.
					Reader r = new BufferedReader(new UnicodeReader(temp));
					String style = view.getSyntaxStyleForFile(temp.getName());
					textArea.read(r, null);	// Clears all old text.
					// Important!  Clear undo history, or RSTA's undo manager
					// will keep all old text (i.e. copies of ALL previous
					// files searched)!
					textArea.discardAllEdits();
					if (!style.equals(textArea.getSyntaxEditingStyle())) {
						textArea.setSyntaxEditingStyle(style);
					}
					r.close();
				} catch (IOException ioe) {
					MatchData data = createErrorMatchData(fileFullPath,
								"IOException reading file: " + ioe);
					dialog.addMatchData(data);
					continue;
				} catch (OutOfMemoryError oome) {
					MatchData data = createErrorMatchData(fileFullPath,
											"OutOfMemoryError");
					dialog.addMatchData(data);
					// Bail out.
					dialog.searchCompleted(
							System.currentTimeMillis() - startMillis);
					return null;
				}

				String buffer = textArea.getText();

				// If we got some text out of the file...
				if (buffer!=null) {
					try {
						if (useRegex) {
							doSearchRegex(buffer, searchString, textArea,
								matchCase, wholeWord, matchingLines,
								fileFullPath, seg);
						}
						else {
							doSearchNoRegex(buffer, searchString, textArea,
								matchCase, wholeWord, matchingLines,
								fileFullPath, seg);
						}
					} catch (Exception e) {
						// Shouldn't happen...
						e.printStackTrace();
						continue;
					}
				}

			} // End of if (temp.isFile()).

			// Otherwise, if the file is a directory...
			else if (temp.isDirectory()) {

				// Ignore this (sub)directory if the user doesn't want
				// to search subdirectories.
				if (!checkSubfolders) {
					if (doVerboseOutput) {
						MatchData data = createVerboseMatchData(
							fileFullPath, dontSearchSubfoldersString);
						dialog.addMatchData(data);
					}
					continue;
				}

				// Add any files in this subdirectory to the master list
				// of files to search.
				List<File> moreFilesList = getFilesFromDirectory(temp);
				int count = moreFilesList==null ? 0 : moreFilesList.size();
				if (count>0) {
					fileList.addAll(moreFilesList);
					numFiles += count;
				}
				if (doVerboseOutput) {
					MatchData data = createVerboseMatchData(
						fileFullPath, newFilesToExamineString +
						": " + count);
					dialog.addMatchData(data);
				}

			} // End of else if (temp.isDirectory()).

		} // End of for (int i=0; i<numFiles; i++).

		dialog.searchCompleted(System.currentTimeMillis() - startMillis);
		return null;

	}


	/**
	 * Performs a non-regex "Find in Files" operation on a single file.
	 */
	private void doSearchNoRegex(String buffer, String searchString,
							RSyntaxTextArea textArea,
							boolean matchCase, boolean wholeWord,
							boolean matchingLines, String fileFullPath,
							Segment seg) {

		// If search is not case-sensitive, lower-case text to search in
		// (searchString is already done).
		if (!matchCase)
			buffer = buffer.toLowerCase();

		// Some stuff we'll use below.
		int bufferLength = buffer.length();
		String lineText = null;
		RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
		Element map = doc.getDefaultRootElement();
		Element elem = null;
		int lineCount = map.getElementCount();
		int i = 0;
		int len = searchString.length();
		int numMatches = 0;

		// Loop through all matches in the file.
		while ((i=buffer.indexOf(searchString, i))!=-1) {

			// If we found a match...
			if (!wholeWord || FindDialog.isWholeWord(buffer, i, len)) {

				numMatches++;
				if (matchingLines) {
					int line = -1;
					int lineEnd = bufferLength-1;
					line = map.getElementIndex(i);
					elem = map.getElement(line);
					lineEnd = line==lineCount-1 ? elem.getEndOffset()-1 :
											elem.getEndOffset();
					Token t = textArea.getTokenListForLine(line);
					lineText = getHtml(t, textArea);
					dialog.addMatchData(new MatchData(fileFullPath,
									Integer.toString(line+1), lineText));
					// Since a single line may have more than one match,
					// skip to the next line's start.
					i = lineEnd/* + 1*/;

				}
				else {
					i += len;
				}

			// We found a potential match, but "whole word" is enabled and
			// this match isn't "whole word."
			} else {
				// We could probably say "i += len" and get away with it,
				// but it is possible for someone to search "whole word" for
				// strings with spaces in them, in which case we should
				// simply increment (for example, searching for "a a " in
				// "ba a a ").
				i++;
			}

		}

		// If we're only interested in the match count, not individual
		// matches, add an entry for this file.
		if (matchingLines==false && numMatches>0) {
			String text = MessageFormat.format(occurrencesString,
							new Object[] { new Integer(numMatches) });
			MatchData data = new MatchData(fileFullPath, NO_LINE_NUMBER,
									text);
			dialog.addMatchData(data);
		}

	}


	/**
	 * Performs a regex "Find in Files" operation on a single file.
	 */
	private void doSearchRegex(String buffer, String searchString,
							RSyntaxTextArea textArea, boolean matchCase,
							boolean wholeWord, boolean matchingLines,
							String fileFullPath, Segment seg) {

		Document doc = textArea.getDocument();
		Element map = doc.getDefaultRootElement();
		Element elem = null;
		int lineCount = map.getElementCount();
		int numMatches = 0;
		int lastStartLine = -1;

		// Create a Matcher to find the text we're looking for.
		int flags = matchCase ? 0 : (Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
		Pattern pattern = Pattern.compile(searchString, flags);
		Matcher m = pattern.matcher(buffer);

		// Loop through all matches.
		while (m.find()) {

			int start = m.start();
			int end = m.end();

			// If we found a match...
			if (!wholeWord || FindDialog.isWholeWord(buffer, start, end-start)) {

				numMatches++;

				// If we're interested in seeing each match...
				if (matchingLines) {

					// Get the text of the first line of the match.
					int startLine = map.getElementIndex(start);
					if (startLine==lastStartLine) {
						// If a single line has > 1 match, don't show
						// the same line multiple times.
						continue;
					}
					lastStartLine = startLine;
					int endLine = map.getElementIndex(end);
					elem = map.getElement(startLine);
					start = elem.getStartOffset();
					end = startLine==lineCount-1 ? elem.getEndOffset()-1 :
											elem.getEndOffset();
					Token t = textArea.getTokenListForLine(startLine);
					String text = getHtml(t, textArea);

					// Add an item to our results.
					boolean oneLine = startLine==endLine;
					String lineStr = oneLine ? Integer.toString(startLine+1) :
								((startLine+1) + "-" + (endLine+1));
					if (!oneLine) {
						text += " <em>" +
								dialog.getString2("MultiLineMatch") +
								"</em>";
					}
					MatchData data = new MatchData(
										fileFullPath, lineStr, text);
					dialog.addMatchData(data);

				} // End of if (matchingLines)
		
			} // End of if (!wholeWord || FindDialog.isWholeWord(...))

		} // End of while (m.find())

		// If we're only interested in the match count, not individual
		// matches, add an entry for this file.
		if (matchingLines==false && numMatches>0) {
			String text = MessageFormat.format(occurrencesString,
							new Object[] { new Integer(numMatches) });
			MatchData data = new MatchData(fileFullPath,
							NO_LINE_NUMBER, text);
			dialog.addMatchData(data);
		}

	}


	/**
	 * Returns the files contained in the specified directory as a list.
	 *
	 * @param dir The directory.
	 * @return The files in the directory, as a list.
	 */
	protected static final List<File> getFilesFromDirectory(File dir) {
		// Get the list of files in this directory.
		File[] moreFiles = dir.listFiles();
		if (moreFiles==null) {
			// Should never happen (as dirs return empty arrays).
			return new ArrayList<File>(0);
		}
		return Arrays.asList(moreFiles);
	}


	protected Pattern[] getFilterStrings() {

		// Get the list of regular expressions to apply when deciding
		// whether or not to look in a file.  If we're on Windows, or OS X,
		// do case-insensitive regexes.
		String[] tokens = dialog.getInFilesComboBoxContents().trim().
									split("\\s*,?\\s+");
		if (tokens==null || tokens.length==0) {
			return null;
		}
		int tokenCount = tokens.length;
		Pattern[] filterStrings = new Pattern[tokenCount];
		int flags = 0;
		String os = System.getProperty("os.name");
		if (os!=null) { // Windows and OS X need case-insensitive searching.
			os = os.toLowerCase();
			if (os.indexOf("windows")>-1 || os.indexOf("mac")>-1)
				flags = Pattern.CASE_INSENSITIVE;
		}
		try {
			for (int i=0; i<tokenCount; i++) {
				String pattern = getRegexForFileFilter(tokens[i]);
				filterStrings[i] = Pattern.compile(pattern, flags);
			}
		} catch (PatternSyntaxException e) {
			e.printStackTrace(); // Never happens.
			return null;
		}

		return filterStrings;

	}


	/**
	 * Gets an HTML string for a token list, stripping off leading whitespace.
	 *
	 * @param t
	 * @param textArea
	 * @return
	 */
	private static final String getHtml(Token t, RSyntaxTextArea textArea) {

		// HTML rendering in Swing is very slow, and we've also seen OOME's
		// from trying render lines that were too long in the Find in Files
		// table, so we'll limit how much we display.
		final int maxLen = 1280;

		StringBuilder sb = new StringBuilder("<html><nobr><font face=\"Monospaced\">");
		boolean firstNonWhitespace = false; // Skip leading whitespace

		while (t!=null && t.isPaintable() && sb.length()<maxLen) {
			if (firstNonWhitespace || (firstNonWhitespace |= !t.isWhitespace())) {
				t.appendHTMLRepresentation(sb, textArea, false);
			}
			t = t.getNextToken();
		}

		if (sb.length()>=maxLen) {
			sb.append("...");
		}
//System.out.println(sb.toString());
		return sb.toString();

	}


	/**
	 * Converts a <code>String</code> representing a wildcard file filter into
	 * another <code>String</code> containing a regular expression good for
	 * finding files that match the wildcard expressions.<br><br>
	 * Example: For<br><br>
	 * <code>String regEx = getRegexForFileFilter("*.c");</code>
	 * <br><br>
	 * <code>regEx</code> will contain <code>^.*\.c$</code>.
	 *
	 * @param fileFilter The file filter for which to create equivalent
	 *        regular expressions.  This filter can currently only contain
	 *        the wildcards '*' and '?'.
	 * @return A <code>String</code> representing an equivalent regular
	 *         expression for the string passed in.  If an error occurs,
	 *         <code>null</code> is returned.
	 */
	protected static final String getRegexForFileFilter(String filter) {
		filter = filter.replaceAll("\\.", "\\\\.");		// '.' => '\.'
		filter = filter.replaceAll("\\*", ".*");		// '*' => '.*'
		filter = filter.replaceAll("\\?", ".");			// '?' => '.'
		filter = filter.replaceAll("\\$", "\\\\\\$");	// '$' => '\$'
		return "^" + filter + "$";
	}


	/**
	 * Returns whether the specified file is "filtered out" and should
	 * not be searched.
	 *
	 * @param file The file name.
	 * @param filters The filters for files to search.
	 * @return Whether the file is filtered out.
	 */
	protected static final boolean isFilteredOut(String file,
										Pattern[] filters) {
		for (int j=0; j<filters.length; j++) {
			if (filters[j].matcher(file).matches()) {
				return false;
			}
		}
		return true;
	}


}