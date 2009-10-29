/*
 * 10/29/2009
 *
 * SpellingSupport.java - Handles spell checking options in RText.
 * Copyright (C) 2009 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This file is a part of RText.
 *
 * RText is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.rtext;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;

import org.fife.ui.rsyntaxtextarea.spell.SpellingParser;
import org.fife.ui.rsyntaxtextarea.spell.event.SpellingParserEvent;
import org.fife.ui.rsyntaxtextarea.spell.event.SpellingParserListener;


/**
 * Handles spell checking options in RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SpellingSupport implements SpellingParserListener {

	/**
	 * Dictionaries for the supported languages.
	 */
	public static final String[] DICTIONARIES	= {
		"BritishEnglish",
		"AmericanEnglish",
	};

	private RText rtext;
	private SpellingParser spellingParser;
	private boolean spellCheckingEnabled;
	private Color spellCheckingColor;
	private String spellingDictionary;
	private File userDictionary;
	private int maxSpellingErrors;


	/**
	 * Constructor.
	 *
	 * @param rtext The parent application.
	 */
	public SpellingSupport(RText rtext) {
		this.rtext = rtext;
	}


	/**
	 * Configures the spelling support.
	 *
	 * @param prefs The application preferences.
	 */
	void configure(RTextPreferences prefs) {
		setSpellCheckingEnabled(prefs.spellCheckingEnabled);
		setSpellCheckingColor(prefs.spellCheckingColor);
		setSpellingDictionary(prefs.spellingDictionary);
		setMaxSpellingErrors(prefs.maxSpellingErrors);
		setUserDictionary(prefs.userDictionary);
System.out.println("Setting user dictionary to: " + prefs.userDictionary);
	}


	/**
	 * Creates a spelling parser using the current spelling preferences set
	 * in this view, and assigns {@link #spellingParser} to it.
	 *
	 * @throws IOException If an IO error occurs.
	 */
	private void createSpellingParser() throws IOException {
		File file = new File("english_dic.zip").getAbsoluteFile();
		boolean american = DICTIONARIES[1].equals(spellingDictionary);
		spellingParser = SpellingParser.
					createEnglishSpellingParser(file, american);
		spellingParser.setSquiggleUnderlineColor(getSpellCheckingColor());
		spellingParser.setMaxErrorCount(getMaxSpellingErrors());
		spellingParser.setAllowAdd(true);//userDictionary!=null);
		spellingParser.setAllowIgnore(true);
		spellingParser.addSpellingParserListener(this);
		try {
			spellingParser.setUserDictionary(userDictionary);
		} catch (IOException ioe) {
			String desc = rtext.getString("Error.LoadingUserDictionary.txt",
				userDictionary==null ? "null" :userDictionary.getAbsolutePath(),
				ioe.getMessage());
			rtext.displayException(ioe, desc);
		}
	}


	/**
	 * Forces a spell check to be done in the text area.
	 *
	 * @param textArea The text area.
	 */
	private void forceSpellCheck(RTextEditorPane textArea) {
		for (int i=0; i<textArea.getParserCount(); i++) {
			// Should be in the list somewhere...
			if (textArea.getParser(i)==spellingParser) {
				textArea.forceReparsing(i);
				break;
			}
		}
	}


	/**
	 * Returns the maximum number of spelling errors to report for a file.
	 *
	 * @return The maximum number of spelling errors.
	 * @see #setMaxSpellingErrors(int)
	 */
	public int getMaxSpellingErrors() {
		return maxSpellingErrors;
	}


	/**
	 * Sets the color to use when squiggle underlining spelling errors.
	 *
	 * @return The color to use.
	 * @see #setSpellCheckingColor(Color)
	 */
	public Color getSpellCheckingColor() {
		return spellCheckingColor;
	}


	/**
	 * Returns the spelling dictionary to use.
	 *
	 * @return The spelling dictionary.
	 * @see #setSpellingDictionary(String)
	 */
	public String getSpellingDictionary() {
		return spellingDictionary;
	}


	/**
	 * Returns the spelling parser.  Note that this may be <code>null</code>
	 * if spell checking has not yet been enabled.
	 *
	 * @return The spelling parser.
	 */
	SpellingParser getSpellingParser() {
		return spellingParser;
	}


	/**
	 * Returns the file used to store words the user chooses to "add to the
	 * dictionary."
	 *
	 * @return The user dictionary file, or <code>null</code> if there is none.
	 * @see #setUserDictionary(File)
	 */
	public File getUserDictionary() {
		return userDictionary;
	}


	/**
	 * Returns whether spell checking is enabled.
	 *
	 * @return Whether spell checking is enabled.
	 * @see #setSpellCheckingEnabled(boolean)
	 */
	public boolean isSpellCheckingEnabled() {
		return spellCheckingEnabled;
	}


	/**
	 * Forces all opened documents to be re-spell checked.
	 */
	private void recheckSpelling() {
		AbstractMainView view = rtext.getMainView();
		for (int i=0; i<view.getNumDocuments(); i++) {
			RTextEditorPane textArea = view.getRTextEditorPaneAt(i);
			for (int j=0; j<textArea.getParserCount(); j++) {
				if (textArea.getParser(j)==spellingParser) {
					textArea.forceReparsing(j);
					break;
				}
			}
		}
	}


	/**
	 * Changes the maximum number of spelling errors reported for a file.
	 *
	 * @param max The maximum number of spelling errors.
	 * @see #getMaxSpellingErrors()
	 */
	public void setMaxSpellingErrors(int max) {
		if (max>=0 && max!=maxSpellingErrors) {
			maxSpellingErrors = max;
			if (spellingParser!=null && isSpellCheckingEnabled()) {
				spellingParser.setMaxErrorCount(maxSpellingErrors);
				recheckSpelling();
			}
		}
	}


	/**
	 * Toggles the color used for squiggle underlining spelling errors.
	 *
	 * @param color The new color to use.
	 * @see #getSpellCheckingColor()
	 */
	public void setSpellCheckingColor(Color color) {
		if (color!=null && color!=spellCheckingColor) {
			spellCheckingColor = color;
			if (spellingParser!=null) {
				spellingParser.setSquiggleUnderlineColor(spellCheckingColor);
				rtext.getMainView().repaint(); // Change all squiggles' colors
			}
		}
	}


	/**
	 * Toggles whether spell checking is enabled.
	 *
	 * @param enabled Whether spell checking is enabled.
	 * @see #isSpellCheckingEnabled()
	 */
	public void setSpellCheckingEnabled(boolean enabled) {

		if (enabled!=spellCheckingEnabled) {

			spellCheckingEnabled = enabled;

			// Lazily create the spelling parser.
			if (enabled && spellingParser==null) {
				new Thread(new CreateParserRunnable()).start();
			}
			else {
				toggleSpellingParserInstalled(); // Already created
			}

		}

	}


	/**
	 * Sets the spelling dictionary to use.
	 *
	 * @param dict The dictionary.  If this is unknown, American English is
	 *        used.
	 * @see #getSpellingDictionary()
	 */
	public void setSpellingDictionary(String dict) {

		if (dict!=null && !dict.equals(spellingDictionary)) {

			// Set the dictionary file, ensuring it is valid.
			boolean valid = false;
			for (int i=0; i<DICTIONARIES.length; i++) {
				if (DICTIONARIES[i].equals(dict)) {
					valid = true;
					break;
				}
			}
			if (!valid) {
				dict = DICTIONARIES[1]; // Default to American English
			}
			spellingDictionary = dict;

			AbstractMainView view = rtext.getMainView();

			// Remove the old spelling parser, if necessary.
			// Note that view is null the first time we're called (in RText's
			// constructor, before the AbstractMainView is attached).
			if (view!=null &&spellingParser!=null && isSpellCheckingEnabled()) {
				for (int i=0; i<view.getNumDocuments(); i++) {
					RTextEditorPane textArea = view.getRTextEditorPaneAt(i);
					textArea.removeParser(spellingParser);
				}
			}

			// Create the new spelling parser, if necessary (if it doesn't
			// exist yet, spell checking hasn't been enabled yet, thus no need
			// to create it).
			if (spellingParser!=null) { // spell checking may be disabled
				try {
					createSpellingParser();
				} catch (IOException ioe) {
					String desc = rtext.getString(
							"Error.LoadingSpellingParser.txt");
					rtext.displayException(ioe, desc);
					return;
				}
			}

			// Add the new spelling parser to all text areas, if necessary.
			// Note that view is null the first time we're called (in RText's
			// constructor, before the AbstractMainView is attached).
			if (view!=null && isSpellCheckingEnabled()) {
				for (int i=0; i<view.getNumDocuments(); i++) {
					RTextEditorPane textArea = view.getRTextEditorPaneAt(i);
					textArea.addParser(spellingParser);
				}
			}

		}

	}


	/**
	 * Sets the dictionary that "added words" are added to.
	 *
	 * @param dict The new user dictionary.  If this is <code>null</code>, then
	 *        there will be no user dictionary.
	 * @see #getUserDictionary()
	 */
	public void setUserDictionary(File dict) {
		if ((dict==null && userDictionary!=null) ||
				(dict!=null && !dict.equals(userDictionary))) {
			userDictionary = dict;
			if (spellingParser!=null) {
				try {
					spellingParser.setUserDictionary(userDictionary);
					recheckSpelling();
				} catch (IOException ioe) {
					rtext.displayException(ioe);
				}
			}
		}
	}


	/**
	 * Called when the user adds a word to their user dictionary, or chooses
	 * to ignore a word.
	 *
	 * @param e The event.
	 */
	public void spellingParserEvent(SpellingParserEvent e) {

		int type = e.getType();

		if (SpellingParserEvent.WORD_ADDED==type ||
				SpellingParserEvent.WORD_IGNORED==type) {

			// Re-spell check opened files.
			AbstractMainView view = rtext.getMainView();
			for (int i=0; i<view.getNumDocuments(); i++) {
				RTextEditorPane textArea = view.getRTextEditorPaneAt(i);
				// currentTextArea already done by the SpellingParser itself
				if (textArea!=view.getCurrentTextArea()) {
					forceSpellCheck(textArea);
				}
			}

		}

	}


	/**
	 * Toggles whether the spelling parser is installed on all currently
	 * visible text areas.  This should only be called on the EDT.
	 */
	private void toggleSpellingParserInstalled() {

		if (spellingParser!=null) { // Should always be true.

			//spellCheckingEnabled = !spellCheckingEnabled; // Already done

			AbstractMainView view = rtext.getMainView();
			for (int i=0; i<view.getNumDocuments(); i++) {
				RTextEditorPane textArea = view.getRTextEditorPaneAt(i);
				if (spellCheckingEnabled) {
					textArea.addParser(spellingParser);
				}
				else {
					textArea.removeParser(spellingParser);
				}
			}
		}
		else {
			Exception e = new Exception("Internal error: Spelling parser is null!");
			rtext.displayException(e);
		}
	}


	/**
	 * Creates the spelling parser off the EDT, then safely starts it on the
	 * EDT.
	 */
	private class CreateParserRunnable implements Runnable {

		public void run() {
			try {
				createSpellingParser();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						toggleSpellingParserInstalled();
					}
				});
			} catch (final IOException ioe) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String desc= rtext.getString(
									"Error.LoadingSpellingParser.txt");
						rtext.displayException(ioe, desc);
					}
				});
			}
		}

	}


}