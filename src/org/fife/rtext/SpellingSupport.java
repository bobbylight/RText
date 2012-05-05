/*
 * 10/29/2009
 *
 * SpellingSupport.java - Handles spell checking options in RText.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.fife.ui.app.StandardAction;
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
	private ViewSpellingErrorWindowAction vsewAction;

	private static final String VIEW_SPELLING_ERROR_WINDOW
									= "viewSpellingErrorWindowAction";


	/**
	 * Constructor.
	 *
	 * @param rtext The parent application.
	 */
	public SpellingSupport(RText rtext) {
		this.rtext = rtext;
	}


	/**
	 * Adds a menu item that toggles whether the spelling error window is
	 * visible.
	 */
	private void addViewErrorWindowMenuItem() {

		vsewAction = new ViewSpellingErrorWindowAction(rtext);

		// Add a menu item to toggle the visibility of the dockable window
		rtext.addAction(VIEW_SPELLING_ERROR_WINDOW, vsewAction);
		RTextMenuBar mb = (RTextMenuBar)rtext.getJMenuBar();
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(vsewAction);
		item.applyComponentOrientation(rtext.getComponentOrientation());
		JMenu viewMenu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		viewMenu.add(item);
		JPopupMenu popup = viewMenu.getPopupMenu();
		popup.pack();
		// Only needed for pre-1.6 support
		popup.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				item.setSelected(rtext.isSpellingWindowVisible());
			}
		});

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

		// Add menu item later since menu bar not yet created(!)
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				addViewErrorWindowMenuItem();
			}
		});

	}


	/**
	 * Creates a spelling parser using the current spelling preferences set
	 * in this view, and assigns {@link #spellingParser} to it.
	 *
	 * @throws IOException If an IO error occurs.
	 */
	private void createSpellingParser() throws IOException {
		String fileName = "english_dic.zip";
		if (rtext.getOS()==RText.OS_MAC_OSX) {
			fileName = "RText.app/Contents/Resources/Java/" + fileName;
		}
		File file = new File(fileName).getAbsoluteFile();
		boolean american = DICTIONARIES[1].equals(spellingDictionary);
		spellingParser = SpellingParser.
					createEnglishSpellingParser(file, american);
		spellingParser.setSquiggleUnderlineColor(getSpellCheckingColor());
		spellingParser.setMaxErrorCount(getMaxSpellingErrors());
		spellingParser.setAllowAdd(true);//userDictionary!=null);
		spellingParser.setAllowIgnore(true);
		spellingParser.addSpellingParserListener(this);
		try {
			if (userDictionary!=null && !userDictionary.exists()) {
				// First time running RText
				userDictionary.getParentFile().mkdirs();
				userDictionary.createNewFile();
			}
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
	public void forceSpellCheck(RTextEditorPane textArea) {
		textArea.forceReparsing(spellingParser);
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
			textArea.forceReparsing(spellingParser);
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


	/**
	 * Toggles the visibility of the spelling error window.
	 */
	private class ViewSpellingErrorWindowAction extends StandardAction {

		public ViewSpellingErrorWindowAction(RText app) {
			super(app, app.getResourceBundle(), "SpellingErrorList.MenuItem");
		}

		public void actionPerformed(ActionEvent e) {
			rtext.setSpellingWindowVisible(!rtext.isSpellingWindowVisible());
		}

	}


}