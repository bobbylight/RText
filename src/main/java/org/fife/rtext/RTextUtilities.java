/*
 * 03/01/2004
 *
 * RTextUtilities.java - Standard tools used by several pieces of RText.
 * Copyright (C) 2004 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.*;
import javax.swing.text.BadLocationException;

import org.fife.jgoodies.looks.common.ShadowPopupBorder;
import org.fife.jgoodies.looks.common.ShadowPopupFactory;
import org.fife.rsta.ui.DecorativeIconPanel;
import org.fife.ui.OS;
import org.fife.ui.UIUtil;
import org.fife.ui.WebLookAndFeelUtils;
import org.fife.ui.app.AppTheme;
import org.fife.ui.app.icons.IconGroup;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.PopupWindowDecorator;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.templates.CodeTemplate;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;
import org.fife.ui.rtextfilechooser.RTextFileChooser;
import org.fife.ui.rtextfilechooser.Utilities;
import org.fife.ui.rtextfilechooser.filters.ExtensionFileFilter;
import org.fife.ui.search.FindInFilesDialog;
import org.fife.util.DarculaUtil;
import org.fife.util.DynamicIntArray;
import org.fife.util.TranslucencyUtil;


/**
 * Collection of tools for use by any of the RText components.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public final class RTextUtilities {

	/**
	 * The extension at the end of all macro files.
	 */
	private static final String MACRO_EXTENSION		  = ".macro";

	private static final String FILE_FILTERS_FILE	  = "ExtraFileChooserFilters.xml";

	/**
	 * The last application theme (attempted to be) installed.  This may not be
	 * the actual currently-active theme, if the user tries to install one with
	 * different window decoration properties.
	 */
	private static String currentAppTheme;

	/**
	 * Whether the experimental "drop shadows" option is enabled.
	 */
	private static boolean dropShadowsEnabledInEditor;


	/**
	 * Private constructor to prevent instantiation.
	 */
	private RTextUtilities() {
		// Do nothing (comment for Sonar)
	}


	/**
	 * Adds a set of "default" code templates to the text areas.
	 */
	static void addDefaultCodeTemplates() {

		CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();

		CodeTemplate t = new StaticCodeTemplate("dob", "do {\n\t", "\n} while ();");
		ctm.addTemplate(t);
		t = new StaticCodeTemplate("forb", "for (", ") {\n\t\n}");
		ctm.addTemplate(t);
		t = new StaticCodeTemplate("ifb", "if (", ") {\n\t\n}");
		ctm.addTemplate(t);
		t = new StaticCodeTemplate("pb", "public ", "");
		ctm.addTemplate(t);
		t = new StaticCodeTemplate("pkgb", "package ", "");
		ctm.addTemplate(t);
		t = new StaticCodeTemplate("stb", "static ", "");
		ctm.addTemplate(t);
		t = new StaticCodeTemplate("whileb", "while (", ") {\n\t\n}");
		ctm.addTemplate(t);

	}


	/**
	 * Adds an extension file filter to the specified file chooser.
	 *
	 * @param chooser The file chooser.
	 * @param msg The resource bundle.
	 * @param key The key to use in <code>msg</code> for the file filter's
	 *        description.
	 * @param extensions The extensions for this filter.
	 */
	private static void addFilter(RTextFileChooser chooser, ResourceBundle msg,
								  String key, String... extensions) {
		ExtensionFileFilter filter = new ExtensionFileFilter(msg.getString(key), extensions);
		chooser.addChoosableFileFilter(filter);
	}


	/**
	 * Scrolls the selected text of a text area so that it is centered
	 * vertically.
	 *
	 * @param textArea The text area.
	 */
	public static void centerSelectionVertically(RSyntaxTextArea textArea) {

		Rectangle visible = textArea.getVisibleRect();

		Rectangle2D r2d;
		try {
			r2d = textArea.modelToView2D(textArea.getCaretPosition());
		} catch (BadLocationException ble) { // Never happens
			ble.printStackTrace();
			return;
		}
		Rectangle r = new Rectangle((int)r2d.getX(), (int)r2d.getY(), (int)r2d.getWidth(),
			(int)r2d.getHeight());
		visible.x = r.x - (visible.width - r.width) / 2;
		visible.y = r.y - (visible.height - r.height) / 2;

		Rectangle bounds = textArea.getBounds();
		Insets i = textArea.getInsets();
		bounds.x = i.left;
		bounds.y = i.top;
		bounds.width -= i.left + i.right;
		bounds.height -= i.top + i.bottom;

		if (visible.x < bounds.x) {
			visible.x = bounds.x;
		}

		if (visible.x + visible.width > bounds.x + bounds.width) {
			visible.x = bounds.x + bounds.width - visible.width;
		}

		if (visible.y < bounds.y) {
			visible.y = bounds.y;
		}

		if (visible.y + visible.height > bounds.y + bounds.height) {
			visible.y = bounds.y + bounds.height - visible.height;
		}

		textArea.scrollRectToVisible(visible);

	}


	/**
	 * Configures a find-in-files dialog for RText.
	 *
	 * @param fnfd The <code>FindInFilesDialog</code> to configure.
	 */
	public static void configureFindInFilesDialog(FindInFilesDialog fnfd) {
		fnfd.addInFilesComboBoxFilter("*.asm");
		fnfd.addInFilesComboBoxFilter("*.bat *.cmd");
		fnfd.addInFilesComboBoxFilter("*.c *.cpp *.cxx *.h");
		fnfd.addInFilesComboBoxFilter("*.cs");
		fnfd.addInFilesComboBoxFilter("*.htm *.html");
		fnfd.addInFilesComboBoxFilter("*.java");
		fnfd.addInFilesComboBoxFilter("*.js");
		fnfd.addInFilesComboBoxFilter("*.pl *.perl *.pm");
		fnfd.addInFilesComboBoxFilter("*.py");
		fnfd.addInFilesComboBoxFilter("*.sh *.bsh *.csh *.ksh");
		fnfd.addInFilesComboBoxFilter("*.txt");
	}


	/**
	 * Creates a panel containing the specified component and an (optional)
	 * decorative (or assistance) icon panel.
	 *
	 * @param comp The component.
	 * @param iconPanel The icon panel.  If this is <code>null</code>, then a
	 *        spacer is used.
	 * @return The panel.
	 * @see #createAssistancePanel(JComponent, int)
	 */
	public static JPanel createAssistancePanel(JComponent comp,
											DecorativeIconPanel iconPanel) {
		if (iconPanel==null) {
			iconPanel = new DecorativeIconPanel();
		}
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(iconPanel, BorderLayout.LINE_START);
		panel.add(comp);
		return panel;
	}


	/**
	 * Creates a panel containing the specified component and some leading
	 * padding.  Used to make text fields <em>without</em> assistance
	 * align properly with text fields in {@code DecorativeIconPanel}s.
	 *
	 * @param comp The component.
	 * @param iconWidth A spacer for the decorative icon panel width.
	 * @return The panel.
	 * @see #createAssistancePanel(JComponent, DecorativeIconPanel)
	 */
	public static JPanel createAssistancePanel(JComponent comp, int iconWidth) {
		DecorativeIconPanel iconPanel = new DecorativeIconPanel(iconWidth);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(iconPanel, BorderLayout.LINE_START);
		panel.add(comp);
		return panel;
	}


	/**
	 * Creates and initializes a file chooser suitable for RText.
	 *
	 * @param rtext The RText instance that will own this file chooser.
	 * @return A file chooser for RText to use.
	 * @see #saveFileChooserFavorites(RText)
	 */
	static RTextFileChooser createFileChooser(RText rtext) {

		rtext.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		RTextFileChooser chooser;

		try {

			chooser = new RTextFileChooser();

			ResourceBundle msg = ResourceBundle.getBundle(
									"org.fife.rtext.FileFilters");

			// Add (localized) file filters.
			addFilter(chooser, msg, "ActionScript", "as", "asc");
			addFilter(chooser, msg, "Assembler6502", "s");
			addFilter(chooser, msg, "AssemblerX86", "asm");
			addFilter(chooser, msg, "BBCode", "bbc");
			addFilter(chooser, msg, "CPlusPlus",
				"c", "cpp", "cxx", "h");
			addFilter(chooser, msg, "Clojure", "clj");
			addFilter(chooser, msg, "CSharp",	"cs");
			addFilter(chooser, msg, "CSS", "css");
			addFilter(chooser, msg, "D", "d");
			addFilter(chooser, msg, "Dart", "dart");
			addFilter(chooser, msg, "Delphi", "pas");
			addFilter(chooser, msg, "DTD", "dtd");
			addFilter(chooser, msg, "Flex", "mxml");
			addFilter(chooser, msg, "Fortran",
				"f", "for", "fort", "f77", "f90");
			addFilter(chooser, msg, "Go", "go");
			addFilter(chooser, msg, "Groovy",
				"groovy", "grv");
			addFilter(chooser, msg, "HTML",
				"htm", "html");
			addFilter(chooser, msg, "INI",
				"ini");
			addFilter(chooser, msg, "Java", "java");
			addFilter(chooser, msg, "JavaScript", "js");
			addFilter(chooser, msg, "JSON", "json");
			addFilter(chooser, msg, "JSP", "jsp");
			addFilter(chooser, msg, "Kotlin", "kt", "ktm", "kts");
			addFilter(chooser, msg, "LaTeX", "tex", "ltx", "latex");
			addFilter(chooser, msg, "Lisp",
				"cl", "clisp", "el", "l", "lisp", "lsp", "ml");
			addFilter(chooser, msg, "Lua", "lua");
			addFilter(chooser, msg, "Makefile",
				"Makefile", "makefile");
			addFilter(chooser, msg, "Markdown", "md");
			addFilter(chooser, msg, "Nsis", "nsi");
			addFilter(chooser, msg, "Perl",
				"pl", "perl", "pm");
			addFilter(chooser, msg, "PHP",
				"php");
			addFilter(chooser, msg, "PropertiesFiles", "properties");
			addFilter(chooser, msg, "Protobuf", "proto");
			addFilter(chooser, msg, "Python", "py");
			addFilter(chooser, msg, "Ruby", "rb");
			addFilter(chooser, msg, "SAS", "sas");
			addFilter(chooser, msg, "Scala", "scala");
			addFilter(chooser, msg, "SQL", "sql");
			addFilter(chooser, msg, "PlainText", "txt");
			addFilter(chooser, msg, "Tcl", "tcl");
			addFilter(chooser, msg, "TypeScript", "ts");
			addFilter(chooser, msg, "UnixShell",
				"sh", "bsh", "csh", "ksh");
			addFilter(chooser, msg, "VisualBasic", "vb");
			addFilter(chooser, msg, "WindowsBatch",
				"bat", "cmd");
			addFilter(chooser, msg, "XML",
				"xml", "xsl", "xsd", "xslt", "wsdl", "jnlp", "macro", "manifest");
			addFilter(chooser, msg, "Yaml",
				"yml", "yaml");

			// Add any user-defined file filters.
			File file = new File(rtext.getInstallLocation(), FILE_FILTERS_FILE);
			try {
				Utilities.addFileFilters(file, chooser);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Add "all supported" filter, but keep "All files" the default.
			chooser.addChoosableFileFilter(new ExtensionFileFilter(
					msg.getString("AllSupported"),
					ExtensionFileFilter.CaseCheck.SYSTEM_CASE_CHECK,
					false,
						"as", "asc",
						"s",
						"asm",
						"bbc",
						"c", "cpp", "cxx", "h",
						"clj",
						"cs",
						"css",
						"csv",
						"d",
						"dart",
						"pas",
						"Dockerfile", "dockerfile",
						"dtd",
						"f", "for", "fort", "f77", "f90",
						"go",
						"groovy", "grv",
						"hbs",
						"hosts",
						"htm", "html",
						"ini",
						"java",
						"js",
						"json",
						"jsp",
						"kt", "kts",
						"tex", "ltx", "latex",
						"cl", "clisp", "el", "l", "lisp", "lsp", "ml",
						"lua",
						"Makefile", "makefile",
						"md",
						"mxml",
						"nsi",
						"perl", "pl", "pm",
						"php",
						"properties",
						"py",
						"rb",
						"sas",
						"scala",
						"sql",
						"tcl",
						"txt",
						"sh", "bsh", "csh", "ksh",
						"vb",
						"bat", "cmd",
						"xml", "xsl", "xsd", "xslt", "wsdl", "svg", "tmx", "tsx", "pom", "manifest",
						"yml", "yaml"
			));
			chooser.setFileFilter(null);

			// Have the chooser open initially to RText's working directory.
			chooser.setCurrentDirectory(rtext.getWorkingDirectory());

			// Load any "Favorite directories."
			File favoritesFile = getFileChooserFavoritesFile();
			if (favoritesFile.isFile()) {
				try {
					chooser.loadFavorites(favoritesFile);
				} catch (IOException ioe) {
					rtext.displayException(ioe);
				}
			}

		} finally {
			// Make sure cursor returns to normal.
			rtext.setCursor(Cursor.getPredefinedCursor(
											Cursor.DEFAULT_CURSOR));
		}

		return chooser;

	}


	/**
	 * Creates a regular expression for a file filter.
	 *
	 * @param filter The file filter.
	 * @return The regular expression.
	 */
	private static String createRegexForFileFilter(String filter) {
		StringBuilder sb = new StringBuilder("^");
		for (int i=0; i<filter.length(); i++) {
			char ch = filter.charAt(i);
			switch (ch) {
				case '.' -> sb.append("\\.");
				case '*' -> sb.append(".*");
				case '?' -> sb.append('.');
				case '$' -> sb.append("\\$");
				default -> sb.append(ch);
			}
		}
		return sb.append('$').toString();
	}


	/**
	 * Enables or disables template usage in RText text areas.
	 *
	 * @param rtext The application.
	 * @param enabled Whether templates should be enabled.
	 * @return <code>true</code> if everything went okay; <code>false</code>
	 *         if the method failed.
	 */
	static boolean enableTemplates(RText rtext, boolean enabled) {
		boolean old = RSyntaxTextArea.getTemplatesEnabled();
		if (old!=enabled) {
			RSyntaxTextArea.setTemplatesEnabled(enabled);
			if (enabled) {
				File f = new File(getPreferencesDirectory(), "templates");
				if (!f.isDirectory() && !f.mkdirs()) {
					return false;
				}
				return RSyntaxTextArea.setTemplateDirectory(
										f.getAbsolutePath());
			}
		}
		return true;
	}


	/**
	 * Returns whether the experimental "drop shadows" option is enabled.
	 *
	 * @return Whether drop shadows are enabled.
	 * @see #setDropShadowsEnabledInEditor(boolean)
	 */
	public static boolean getDropShadowsEnabledInEditor() {
		return dropShadowsEnabledInEditor;
	}


	/**
	 * Returns the file in which to save file chooser favorite directories.
	 * This file should have the encoding UTF-8.
	 *
	 * @return The file.
	 */
	private static File getFileChooserFavoritesFile() {
		return new File(getPreferencesDirectory(), "fileChooser.favorites");
	}


	/**
	 * Returns the name of the theme to load RText with the next time it
	 * starts up.  This may not be the same thing as the currently active theme,
	 * if the user chose a theme that used custom window decorations, for example.
	 *
	 * @param rtext The parent application.
	 * @return The name of the theme to save in the RText preferences.
	 */
	static String getAppThemeToSave(RText rtext) {
		String appoTheme = currentAppTheme;
		if (appoTheme ==null) {
			appoTheme = rtext.getTheme().getName();
		}
		return appoTheme;
	}


	/**
	 * Returns the directory in which the user's macros are stored.
	 *
	 * @return The macro directory, or <code>null</code> if it cannot be found
	 *         or created.
	 */
	private static File getMacroDirectory() {

		File f = new File(getPreferencesDirectory(), "macros");
		if (!f.isDirectory() && !f.mkdirs()) {
			return null;
		}
		return f;

	}


	/**
	 * Returns the name of the macro in the specified file.
	 *
	 * @param macroFile A file containing an <code>RTextArea</code> macro.
	 *        If this file is <code>null</code>, then <code>null</code>
	 *        is returned.
	 * @return The name of the macro.
	 */
	static String getMacroName(File macroFile) {
		String name = null;
		if (macroFile!=null) {
			name = macroFile.getName();
			if (name.endsWith(MACRO_EXTENSION)) { // Should always happen.
				name = name.substring(0,
							name.length()-MACRO_EXTENSION.length());
			}
		}
		return name;
	}


	/**
	 * Returns the directory in which to load and save user preferences
	 * (beyond those saved via the Java preferences API).
	 *
	 * @return The directory.
	 */
	public static File getPreferencesDirectory() {
		return new File(System.getProperty("user.home"), ".rtext");
	}


	/**
	 * Converts a <code>String</code> representing a wildcard file filter into
	 * a <code>Pattern</code> containing a regular expression good for
	 * finding files that match the wildcard expression.<p>
	 *
	 * Example: For<p>
	 * <code>String regEx = RTextUtilities.getPatternForFileFilter("*.c", false);
	 * </code><p>
	 * the returned pattern will match <code>^.*\.c$</code>.<p>
	 *
	 * Case-sensitivity is taken into account appropriately.
	 *
	 * @param fileFilter The file filter for which to create equivalent regular
	 *        expressions.  This filter can currently only contain the
	 *        wildcards '*' and '?'.
	 * @param showErrorDialog If <code>true</code>, an error dialog is
	 *        displayed if an error occurs.
	 * @return A <code>Pattern</code> representing an equivalent regular
	 *         expression for the string passed in.  If an error occurs,
	 *         <code>null</code> is returned.
	 */
	public static Pattern getPatternForFileFilter(String fileFilter,
										boolean showErrorDialog) {

		String pattern = createRegexForFileFilter(fileFilter);
		int flags = OS.get().isCaseSensitive() ? 0 :
				(Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
		try {
			return Pattern.compile(pattern, flags);
		} catch (PatternSyntaxException pse) {
			if (showErrorDialog) {
				String text = pse.getMessage();
				if (text==null) {
					text = pse.toString();
				}
				JOptionPane.showMessageDialog(null,
					"Error in the regular expression '" + pattern +
					"' formed from parameter '" + fileFilter + "':\n" +
					text + "\nPlease use only valid filename characters " +
					"or wildcards (* or ?).\n" +
					"If you have, please report this error at: " +
					"https://github.com/bobbylight/RText",
					"Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		return null;

	}


	/**
	 * Returns all macro files saved in the macro directory.
	 *
	 * @return An array of files containing macros in the macro directory.  If
	 *         the macro directory cannot be found or is empty, an empty array
	 *         is returned.
	 */
	/*
	 * FIXME:  Have me return the file list in alphabetical order (as this is
	 *         not guaranteed by File.listFiles()).
	 */
	static File[] getSavedMacroFiles() {

		File macroDir = getMacroDirectory();

		// If the macro directory exists...
		if (macroDir!=null && macroDir.isDirectory()) {

			File[] allFiles = macroDir.listFiles();

			// And if there are files in it...
			if (allFiles!=null && allFiles.length>0) {

				// Remember all of the files that end in ".macro".
				int count = allFiles.length;
				DynamicIntArray dia = new DynamicIntArray();
				for (int i=0; i<count; i++) {
					if (allFiles[i].getName().endsWith(MACRO_EXTENSION))
						dia.add(i);
				}

				// Put the ".macro" files into their own array.
				count = dia.getSize();
				File[] macroFiles = new File[count];
				for (int i=0; i<count; i++)
					macroFiles[i] = allFiles[dia.get(i)];

				return macroFiles;

			}

		}

		// Otherwise, the macro directory couldn't be created for some reason
		// or it was empty.
		return new File[0];

	}


	/**
	 * Returns whether the current Look and Feel (the one that will be saved,
	 * not necessarily the active one) is primarily dark.
	 *
	 * @return Whether the current Look and Feel is primarily dark.
	 * @see #getAppThemeToSave(RText)
	 */
	public static boolean isDarkLookAndFeel() {

		String laf = UIManager.getLookAndFeel().getClass().getName();

		return laf != null && (
				laf.contains("Darcula") ||
				laf.contains("FlatDark")
				);
	}


	/**
	 * Returns all elements in an array, joined by <code>", "</code>.
	 *
	 * @param array The array.  If this is <code>null</code> or has zero
	 *        length, <code>null</code> is returned.
	 * @return The joined text.
	 * @see #join(String[], String)
	 */
	public static String join(String[] array) {
		return join(array, ", ");
	}


	/**
	 * Returns all elements in an array, joined by a specific sequence.
	 *
	 * @param array The array.  If this is <code>null</code> or has zero
	 *        length, <code>null</code> is returned.
	 * @param connector Text to connect each array element with.
	 * @return The joined text.
	 * @see #join(String[])
	 */
	public static String join(String[] array, String connector) {
		if (array==null || array.length==0) {
			return null;
		}
		StringBuilder sb = new StringBuilder(array[0]);
		for (int i=1; i<array.length; i++) {
			sb.append(connector).append(array[i]);
		}
		return sb.toString();
	}


	/**
	 * Opens all files in the specified directory tree in RText.
	 *
	 * @param rtext The RText instance in which to open the files.
	 * @param directory The top of the directory tree, all files in which
	 *        you want opened in RText.
	 */
	public static void openAllFilesIn(RText rtext, File directory) {
		if (directory!=null && directory.isDirectory()) {
			File[] files = directory.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						openAllFilesIn(rtext, file);
					}
					else {
						rtext.openFile(file);
					}
				}
			}
		}
	}


	/**
	 * Saves the "Favorite Directories" of RText's file chooser.  It is
	 * assumed that the file chooser has been created via
	 * {@link #createFileChooser(RText)} before calling this method.
	 * <p>
	 * If an error occurs saving the favorites, an error message is
	 * displayed.
	 *
	 * @param rtext The parent RText instance.
	 * @see #createFileChooser(RText)
	 */
	static void saveFileChooserFavorites(RText rtext) {
		RTextFileChooser chooser = rtext.getFileChooser();
		try {
			chooser.saveFavorites(getFileChooserFavoritesFile());
		} catch (IOException ioe) {
			rtext.displayException(ioe);
		}
	}


	/**
	 * Toggles whether the experimental "drop shadows" option is enabled.
	 * Note that this will do nothing if the current system does not support
	 * translucent windows.
	 *
	 * @param enabled Whether the option is enabled.
	 * @see #getDropShadowsEnabledInEditor()
	 */
	public static void setDropShadowsEnabledInEditor(boolean enabled) {

		if (enabled!=dropShadowsEnabledInEditor) {

			dropShadowsEnabledInEditor = enabled;

			if (dropShadowsEnabledInEditor) {
				TranslucencyUtil util = TranslucencyUtil.get();
				if (util!=null) {
					if (util.isTranslucencySupported(true)) {
						PopupWindowDecorator.set(new PopupWindowDecorator() {
							@Override
							public void decorate(JWindow window) {
								Container cp = window.getContentPane();
								if (cp instanceof JComponent) {
									TranslucencyUtil util =
										TranslucencyUtil.get();
									util.setOpaque(window, false);
									((JComponent)cp).setBorder(
										BorderFactory.createCompoundBorder(
											ShadowPopupBorder.getInstance(),
											((JComponent)cp).getBorder()));
								}
							}
						});
					}
				}
			}
			else { // Not enabled
				PopupWindowDecorator.set(null);
			}

		}

	}


	/**
	 * Sets the theme for all open RText instances.
	 *
	 * @param rtext An RText instance to display a message if an exception is
	 *        thrown.
	 * @param theme The theme to install.
	 */
	// TODO: Shouldn't this be in UIUtil somehow, for other GUIApplications?
	public static void setThemeForAllOpenAppInstances(final RText rtext, AppTheme theme) {

		// Only set the Look and Feel if we're not already using that Look.
		// Compare against currently active one, not the one we want to change
		// to on restart, seems more logical to the end-user.
		//String current = currentLaF;
		String current = UIManager.getLookAndFeel().getClass().getName();
		String lnfClassName = theme.getLookAndFeel();
		currentAppTheme = theme.getName();

		if (lnfClassName!=null && !current.equals(lnfClassName)) {
			try {

				// Set these properties before instantiating WebLookAndFeel.
				// Note it does its own menu shadowing
				if (WebLookAndFeelUtils.isWebLookAndFeel(lnfClassName)) {
					ShadowPopupFactory.uninstall();
					WebLookAndFeelUtils.installWebLookAndFeelProperties();
				}
				else {
					ShadowPopupFactory.install();
				}

				// Java 11+ won't let us reflectively access system LookAndFeels,
				// so in that case we keep lnf == null and have more complicated
				// logic below.
				LookAndFeel lnf = null;
				if (!UIManager.getSystemLookAndFeelClassName().equals(lnfClassName)) {
					Class<?> c = RTextUtilities.class.getClassLoader().loadClass(lnfClassName);
					lnf = (LookAndFeel)c.getDeclaredConstructor().newInstance();
				}

				// If we're changing to a LAF that supports window decorations
				// and our current one doesn't, or vice versa, inform the
				// user that this change will occur on restart.
				boolean curDarcula = DarculaUtil.isDarculaInstalled();
				boolean nextDarcula = DarculaUtil.isDarculaLookAndFeel(lnf);
				if (curDarcula || nextDarcula) {
					String message = rtext.getString(
									"Info.LookAndFeel.LoadOnNextRestart");
					String title = rtext.getString("InfoDialogHeader");
					JOptionPane.showMessageDialog(rtext, message, title,
											JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				if (lnf != null) {
					UIManager.setLookAndFeel(lnf);
				}
				else { // System LaF
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}

				UIUtil.installOsSpecificLafTweaks();

			} catch (Exception e) {
				rtext.displayException(e);
			}

		}

		// Once the LookAndFeel is (possibly) updated, update every RText instance with other
		// theme info.  This is always done, even if the same AppTheme is reapplied, since it
		// may override some previously user-specified values.
		StoreKeeper.updateAppThemes(theme);
	}


	/**
	 * Returns an icon group fit for {@code RSyntaxTextArea} from an application
	 * icon group.
	 *
	 * @param iconGroup The application's icon group.
	 * @return The text area icon group.
	 */
	static org.fife.ui.rtextarea.IconGroup toRstaIconGroup(IconGroup iconGroup) {
		return new org.fife.ui.rtextarea.IconGroup(iconGroup.getName(), "") {
			@Override
			public Icon getIconImpl(String iconFullPath) {
				iconFullPath = iconFullPath.substring(0, iconFullPath.lastIndexOf('.'));
				return iconGroup.getIcon(iconFullPath);
			}
		};
	}
}
