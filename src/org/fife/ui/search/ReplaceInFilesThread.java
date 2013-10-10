/*
 * 09/19/2006
 *
 * ReplaceInFilesThread.java - Thread that replaces text in files.
 * Copyright (C) 2006 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.search;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.*;

import org.fife.io.*;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.ui.rtextarea.SearchEngine;


/**
 * A thread created by a <code>ReplaceInFilesDialog</code> to do the
 * replacing.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ReplaceInFilesThread extends FindInFilesThread {


	/**
	 * Constructor.
	 *
	 * @param dialog The "find in files" dialog.
	 * @param directory The directory in which to search.
	 */
	public ReplaceInFilesThread(FindInFilesDialog dialog, File directory) {
		super(dialog, directory);
	}


	/**
	 * Runs the search.
	 */
	@Override
	public Object construct() {

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
		boolean matchCase = dialog.getMatchCase();
		boolean wholeWord = dialog.getMatchWholeWord();
		boolean useRegex = dialog.getUseRegEx();
		boolean doVerboseOutput = dialog.getDoVerboseOutput();
		String replaceString = ((ReplaceInFilesDialog)dialog).getReplaceString();
		String searchingFile = dialog.getString2("SearchingFile");

		if (!useRegex && !matchCase)
			searchString = searchString.toLowerCase();

		long startMillis = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[4096];
		StringBuilder replaceSB = new StringBuilder();

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
				String encoding = null;

				try {
					// Use a UnicodeReader to auto-detect whether this
					// is a Unicode file.
					// FIXME:  Allow the user to specify the default
					// encoding, instead of assuming system default,
					// somehow.
					UnicodeReader ur = new UnicodeReader(temp);
					encoding = ur.getEncoding();
					Reader r = new BufferedReader(ur);
					try {
						int count = 0;
						sb.setLength(0);
						while ((count=r.read(buf))!=-1) {
							sb.append(buf,0,count);
						}
					} finally {
						r.close();
					}
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

				// If we got some text out of the file...
				if (sb.length()>0) {

					try {

						int repCount = 0;
						replaceSB.setLength(0);

						if (useRegex) {
							repCount = doSearchRegex(sb, searchString,
								replaceString, matchCase, wholeWord,
								fileFullPath, replaceSB);
						}
						else {
							repCount = doSearchNoRegex(sb, searchString,
								replaceString, matchCase, wholeWord,
								fileFullPath, replaceSB);
						}

						// If text was replaced, rewrite the file with
						// its new contents.
						if (repCount>0) {

							PrintWriter w = new PrintWriter(new BufferedWriter(
								new UnicodeWriter(fileFullPath, encoding)));
							w.print(replaceSB.toString());
							w.close();

							String text = MessageFormat.format(occurrencesString,
									new Object[] { new Integer(repCount) });
							MatchData data = new MatchData(fileFullPath,
											NO_LINE_NUMBER, text);
							dialog.addMatchData(data);

						}
						else if (doVerboseOutput) { // repCount==0
							String text = MessageFormat.format(occurrencesString,
									new Object[] { new Integer(repCount) });
							MatchData data = createVerboseMatchData(
											fileFullPath, text);
							dialog.addMatchData(data);
						}

					} catch (/*IO*/Exception ioe) {
						ioe.printStackTrace();
						String desc = ioe.getMessage();
						MatchData data = createErrorMatchData(
										fileFullPath, desc);
						dialog.addMatchData(data);
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
	 *
	 * @return The number of replacements.
	 */
	private static final int doSearchNoRegex(CharSequence sb,
			String searchString, String replaceString, boolean matchCase,
			boolean wholeWord, String fileFullPath, StringBuilder replaceSB) {

		String origBuffer = sb.toString();
		String buffer = origBuffer;

		// If search is not case-sensitive, lower-case text to search in
		// (searchString is already done).
		if (!matchCase) {
			buffer = buffer.toLowerCase();
		}

		// Some stuff we'll use below.
		int i = 0;
		int start = 0;
		int len = searchString.length();
		int numMatches = 0;

		// Loop through all matches in the file.
		while ((i=buffer.indexOf(searchString, i))!=-1) {

			// If we found a match...
			if (!wholeWord || FindDialog.isWholeWord(buffer, i, len)) {

				replaceSB.append(origBuffer.substring(start,i));
				replaceSB.append(replaceString);

				numMatches++;
				i += len;
				start = i;

			// We found a potential match, but "whole word" is enabled
			// and this match isn't "whole word."
			}
			else {
				// We could probably say "i += len" and get away with it,
				// but it is possible for someone to search "whole word"
				// for strings with spaces in them, in which case we
				// should simply increment (for example, searching for
				// "a a " in "ba a a ").
				i++;
			}

		}

		// Any characters at the end.
		if (start!=buffer.length()) {
			replaceSB.append(origBuffer.substring(start,buffer.length()));
		}

		return numMatches;

	}


	/**
	 * Performs a regex "Find in Files" operation on a single file.
	 *
	 * @return The number of replacements.
	 */
	private static final int doSearchRegex(StringBuilder sb, String searchString,
					String replaceString, boolean matchCase,
					boolean wholeWord, String fileFullPath,
					StringBuilder replaceSB) {

		int numMatches = 0;

		// Create a Matcher to find the text we're looking for.
		int flags = matchCase ? 0 : (Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
		Pattern pattern = Pattern.compile(searchString, flags);
		Matcher m = pattern.matcher(sb);
		int lastEnd = 0;

		// Loop through all matches.
		// NOTE: Instead of using m.replaceAll() (and thus
		// m.appendReplacement() and m.appendTail()), we do this
		// ourselves since we have our own method of getting the
		// "replacement text" which converts "\n" to newlines and
		// "\t" to tabs.
		while (m.find()) {

			int start = m.start();
			int end = m.end();

			// If we found a match...
			if (!wholeWord || FindDialog.isWholeWord(sb, start, end-start)) {
				replaceSB.append(sb.substring(lastEnd, m.start()));
				replaceSB.append(SearchEngine.getReplacementText(m, replaceString));
				lastEnd = m.end();
				numMatches++;
			}

		} // End of while (m.find())

		// Any text at the end.
		replaceSB.append(sb.substring(lastEnd));

		return numMatches;

	}


}