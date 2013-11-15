/*
 * 03/01/2004
 *
 * RTextUtilities.java - Standard tools used by several pieces of RText.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;

import org.fife.jgoodies.looks.common.ShadowPopupBorder;
import org.fife.jgoodies.looks.common.ShadowPopupFactory;
import org.fife.rsta.ui.DecorativeIconPanel;
import org.fife.ui.SubstanceUtils;
import org.fife.ui.UIUtil;
import org.fife.ui.WebLookAndFeelUtils;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.PopupWindowDecorator;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.templates.CodeTemplate;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;
import org.fife.ui.rtextfilechooser.RTextFileChooser;
import org.fife.ui.rtextfilechooser.Utilities;
import org.fife.ui.rtextfilechooser.filters.ExtensionFileFilter;
import org.fife.ui.search.FindInFilesDialog;
import org.fife.util.DynamicIntArray;
import org.fife.util.TranslucencyUtil;


/**
 * Collection of tools for use by any of the RText components.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RTextUtilities {

	/**
	 * The extension at the end of all macro files.
	 */
	public static final String MACRO_EXTENSION		  = ".macro";

	private static final String FILE_FILTERS_FILE	  = "ExtraFileChooserFilters.xml";

	/**
	 * The last LookAndFeel (attempted to be) installed.  This may not be
	 * the actual currently-active LookAndFeel, if the user tries to install
	 * one with different window decoration properties.
	 */
	private static String currentLaF;

	/**
	 * Whether the experimental "drop shadows" option is enabled.
	 */
	private static boolean dropShadowsEnabledInEditor;

	private static final boolean CASE_SENSITIVE;


	/**
	 * Adds a set of "default" code templates to the text areas.
	 */
	public static final void addDefaultCodeTemplates() {

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
	 * @param extensions Either a string representing a single extension or
	 *        an array of strings containing multiple extensions.
	 */
	private static final void addFilter(RTextFileChooser chooser,
					ResourceBundle msg, String key, Object extensions) {
		ExtensionFileFilter filter = null;
		if (extensions instanceof String) {
			filter = new ExtensionFileFilter(msg.getString(key),
										(String)extensions);
		}
		else {
			filter = new ExtensionFileFilter(msg.getString(key),
										(String[])extensions);
		}
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

		Rectangle r = null;
		try {
			r = textArea.modelToView(textArea.getCaretPosition());
		} catch (BadLocationException ble) { // Never happens
			ble.printStackTrace();
			return;
		}
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
	public static final void configureFindInFilesDialog(FindInFilesDialog fnfd) {
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
	 * Creates and initializes a file chooser suitable for RText.
	 *
	 * @param rtext The RText instance that will own this file chooser.
	 * @return A file chooser for RText to use.
	 * @see #saveFileChooserFavorites(RText)
	 */
	public static final RTextFileChooser createFileChooser(RText rtext) {

		rtext.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		RTextFileChooser chooser = null;

		try {

			chooser = new RTextFileChooser();

			ResourceBundle msg = ResourceBundle.getBundle(
									"org.fife.rtext.FileFilters");

			// Add (localized) file filters.
			addFilter(chooser, msg, "ActionScript", new String[] { "as", "asc" });
			addFilter(chooser, msg, "AssemblerX86", "asm");
			addFilter(chooser, msg, "BBCode", "bbc");
			addFilter(chooser, msg, "CPlusPlus",
					new String[] { "c", "cpp", "cxx", "h" });
			addFilter(chooser, msg, "Clojure", "clj");
			addFilter(chooser, msg, "CSharp",	"cs");
			addFilter(chooser, msg, "CSS", "css");
			addFilter(chooser, msg, "Delphi", "pas");
			addFilter(chooser, msg, "DTD", "dtd");
			addFilter(chooser, msg, "Flex", "mxml");
			addFilter(chooser, msg, "Fortran",
					new String[] { "f", "for", "fort", "f77", "f90" });
			addFilter(chooser, msg, "Groovy",
					new String[] { "groovy", "grv" });
			addFilter(chooser, msg, "HTML",
					new String[] { "htm", "html" });
			addFilter(chooser, msg, "Java", "java");
			addFilter(chooser, msg, "JavaScript", "js");
			addFilter(chooser, msg, "JSON", "json");
			addFilter(chooser, msg, "JSP", "jsp");
			addFilter(chooser, msg, "LaTeX", new String[] { "tex", "ltx", "latex" });
			addFilter(chooser, msg, "Lisp",
					new String[] { "cl", "clisp", "el", "l", "lisp", "lsp", "ml" });
			addFilter(chooser, msg, "Lua", "lua");
			addFilter(chooser, msg, "Makefile", 
					new String[] { "Makefile", "makefile" });
			addFilter(chooser, msg, "Nsis", "nsi");
			addFilter(chooser, msg, "Perl",
					new String[] { "pl", "perl", "pm" });
			addFilter(chooser, msg, "PHP",
					new String[] { "php" });
			addFilter(chooser, msg, "PropertiesFiles", "properties");
			addFilter(chooser, msg, "Python", "py");
			addFilter(chooser, msg, "Ruby", "rb");
			addFilter(chooser, msg, "SAS", "sas");
			addFilter(chooser, msg, "Scala", "scala");
			addFilter(chooser, msg, "SQL", "sql");
			addFilter(chooser, msg, "PlainText", "txt");
			addFilter(chooser, msg, "Tcl", "tcl");
			addFilter(chooser, msg, "UnixShell",
					new String[] { "sh", "bsh", "csh", "ksh" });
			addFilter(chooser, msg, "VisualBasic", "vb");
			addFilter(chooser, msg, "WindowsBatch",
					new String[] { "bat", "cmd" });
			addFilter(chooser, msg, "XML",
					new String[] { "xml", "xsl", "xsd", "wsdl", "jnlp", "macro", "manifest" });

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
					new String[] {
						"as", "asc",
						"asm",
						"bbc",
						"c", "cpp", "cxx", "h",
						"clj",
						"cs",
						"css",
						"pas",
						"dtd",
						"mxml",
						"f", "for", "fort", "f77", "f90",
						"groovy", "grv",
						"htm", "html",
						"java",
						"js",
						"json",
						"jsp",
						"tex", "ltx", "latex",
						"cl", "clisp", "el", "l", "lisp", "lsp", "ml",
						"lua",
						"Makefile", "makefile",
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
						"xml", "xsl", "xsd", "wsdl", "jnlp", "macro", "manifest",
					},
					ExtensionFileFilter.SYSTEM_CASE_CHECK,
					false
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
				case '.':
					sb.append("\\.");
					break;
				case '*':
					sb.append(".*");
					break;
				case '?':
					sb.append('.');
					break;
				case '$':
					sb.append("\\$");
					break;
				default:
					sb.append(ch);
					break;
			}
		}
		return sb.append('$').toString();
	}


	/**
	 * Enables or disables template usage in RText text areas.
	 *
	 * @param enabled Whether templates should be enabled.
	 * @return <code>true</code> if everything went okay; <code>false</code>
	 *         if the method failed.
	 */
	public static boolean enableTemplates(RText rtext, boolean enabled) {
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
	 * Returns a string with characters that are special to HTML (such as
	 * <code>&lt;</code>, <code>&gt;</code> and <code>&amp;</code>) replaced
	 * by their HTML escape sequences.
	 *
	 * @param s The input string.
	 * @param newlineReplacement What to replace newline characters with.
	 *        If this is <code>null</code>, they are simply removed.
	 * @return The escaped version of <code>s</code>.
	 */
	public static final String escapeForHTML(String s,
									String newlineReplacement) {
		return escapeForHTML(s, newlineReplacement, false);
	}


	/**
	 * Returns a string with characters that are special to HTML (such as
	 * <code>&lt;</code>, <code>&gt;</code> and <code>&amp;</code>) replaced
	 * by their HTML escape sequences.
	 *
	 * @param s The input string.
	 * @param newlineReplacement What to replace newline characters with.
	 *        If this is <code>null</code>, they are simply removed.
	 * @param inPreBlock Whether this HTML will be in within <code>pre</code>
	 *        tags.  If this is <code>true</code>, spaces will be kept as-is;
	 *        otherwise, they will be converted to "<code>&nbsp;</code>".
	 * @return The escaped version of <code>s</code>.
	 */
	public static final String escapeForHTML(String s,
						String newlineReplacement, boolean inPreBlock) {

		if (newlineReplacement==null) {
			newlineReplacement = "";
		}
		String tabString = "   ";

		// TODO: When updating to 1.5, replace with StringBuilder, and change
		// loop to use new append(str, offs,len) method.
		StringBuilder sb = new StringBuilder();

		for (int i=0; i<s.length(); i++) {
			char ch = s.charAt(i);
			switch (ch) {
				case ' ':
					if (inPreBlock) {
						sb.append(' ');
					}
					else {
						sb.append("&nbsp;");
					}
					break;
				case '\n':
					sb.append(newlineReplacement);
					break;
				case '&':
					sb.append("&amp;");
					break;
				case '\t':
					sb.append(tabString);
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				default:
					sb.append(ch);
					break;
			}
		}

		return sb.toString();

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
	 * Returns an image from a file in a safe fashion.
	 *
	 * @param fileName The file from which to get the image (must be .jpg,
	 *        .gif or .png).
	 * @return The image contained in the file, or <code>null</code> if the
	 *         image file was invalid.
	 */
	public static Image getImageFromFile(String fileName) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new URL("file:///" + fileName));
		} catch (MalformedURLException mue) {
			mue.printStackTrace(); // This is our fault.
		} catch (IOException e) {
			// Do nothing.
		}
		return image; // null if there was an exception thrown.
	}


	/**
	 * Returns the name of the LookAndFeel to load RText with the next time it
	 * starts up.  This may not be the same thing as the currently active LAF,
	 * if the user chose a LAF that used custom window decorations, for example.
	 *
	 * @return The name of the LookAndFeel to save in the RText preferences.
	 */
	public static String getLookAndFeelToSave() {
		String laf = currentLaF;
		if (laf==null) {
			laf = UIManager.getLookAndFeel().getClass().getName();
		}
		return laf;
	}


	/**
	 * Returns the directory in which the user's macros are stored.
	 *
	 * @return The macro directory, or <code>null</code> if it cannot be found
	 *         or created.
	 */
	public static final File getMacroDirectory() {

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
	public static final String getMacroName(File macroFile) {
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
		int flags = isFileSystemCaseSensitive() ? 0 :
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
					"http://sourceforge.net/projects/rtext",
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
	public static final File[] getSavedMacroFiles() {

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
	 * Returns a translucent version of a given <code>java.awt.Image</code>.
	 *
	 * @param image The <code>java.awt.Image</code> on which to apply the
	 *        alpha filter.
	 * @param alpha The alpha value to use when defining how translucent you
	 *        want the image to be. This should be in the range 0.0f to 1.0f.
	 */
	public static BufferedImage getTranslucentImage(RText rtext, Image image,
												float alpha) {

		// Ensure valid alpha value
		alpha = Math.max(0, alpha);
		alpha = Math.min(alpha, 1);

		// Create fast image
		BufferedImage bi = null;
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		bi = rtext.getGraphicsConfiguration().createCompatibleImage(w, h);
		Graphics2D g2d = bi.createGraphics();
		try {
			g2d.setColor(Color.white);
			g2d.fillRect(0, 0, w, h);
			g2d.setComposite(AlphaComposite.getInstance(
									AlphaComposite.SRC_OVER, alpha));
			g2d.drawImage(image, 0,0, null);
		} finally {
			g2d.dispose();
		}

		return bi;

	}


	/**
	 * Returns whether the local file system is case-sensitive.
	 *
	 * @return Whether the local file system is case-sensitive.
	 */
	public static final boolean isFileSystemCaseSensitive() {
		return CASE_SENSITIVE;
	}


	/**
	 * Returns whether the Java Runtime that RText is running in is 1.4 or 1.5.
	 * 
	 * @return Whether the current JVM is pre-Java 6.
	 */
	public static final boolean isPreJava6() {
		String version = System.getProperty("java.specification.version");
		return "1.4".equals(version) || "1.5".equals(version);
	}


	/**
	 * Returns all elements in an array, joined by <code>", "</code>.
	 *
	 * @param array The array.  If this is <code>null</code> or has zero
	 *        length, <code>null</code> is returned.
	 * @return The joined text.
	 * @see #join(String[], String)
	 */
	public static final String join(String[] array) {
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
	public static final String join(String[] array, String connector) {
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
			int count = files.length;
			for (int i=0; i<count; i++) {
				if (files[i].isDirectory()) {
					openAllFilesIn(rtext, files[i]);
				}
				else {
					rtext.openFile(files[i].getAbsolutePath());
				}
			}
		}
	}


	/**
	 * Remove problematic actions that prevent Ctrl+PageUp/PageDown from
	 * being used for cycling through active documents.
	 *
	 * @param c The component to modify.
	 */
	public static void removeTabbedPaneFocusTraversalKeyBindings(JComponent c) {

		InputMap im = c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK), "nothing");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK), "nothing");

		im = c.getInputMap();
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK), "nothing");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK), "nothing");

	}


	/**
	 * Saves the "Favorite Directories" of RText's file chooser.  It is
	 * assumed that the file chooser has been created via
	 * {@link #createFileChooser(RText)} before calling this method.
	 *
	 * If an error occurs saving the favorites, an error message is
	 * displayed.
	 *
	 * @param rtext The parent RText instance.
	 * @see #createFileChooser(RText)
	 */
	public static void saveFileChooserFavorites(RText rtext) {
		RTextFileChooser chooser = rtext.getFileChooser();
		try {
			chooser.saveFavorites(getFileChooserFavoritesFile());
		} catch (IOException ioe) {
			rtext.displayException(ioe);
		}
	}


	/**
	 * Toggles whether the experimental "drop shadows" option is enabled.
	 * Note that this may do nothing if we're running on Java 1.4 or 1.5, or
	 * the current system does not support translucent windows.
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
	 * Sets the Look and Feel for all open RText instances.
	 *
	 * @param rtext An RText instance to display a message if an exception is
	 *        thrown.
	 * @param lnfClassName The class name of the Look and Feel to set.
	 */
	// TODO: Shouldn't this be in UIUtil somehow, for other GUIApplications?
	public static void setLookAndFeel(final RText rtext, String lnfClassName) {

		// Only set the Look and Feel if we're not already using that Look.
		// Compare against currently active one, not the one we want to change
		// to on restart, seems more logical to the end-user.
		//String current = currentLaF;
		String current = UIManager.getLookAndFeel().getClass().getName();

		if (lnfClassName!=null && !current.equals(lnfClassName)) {
			try {

				currentLaF = lnfClassName;

				// Use RText's LaF class loader, not a system one, as it
				// can access any additional 3rd-party LaF jars that
				// weren't on the classpath when RText started.  Also,
				// don't necessarily trust UIDefaults.get("ClassLoader") to
				// be this class loader, as on Windows if the user changes
				// the UxTheme the LaF is updated outside of this call,
				// and the property value is reset to null.
				ClassLoader cl = rtext.getLookAndFeelManager().
													getLAFClassLoader();

				// Set these properties before instantiating WebLookAndFeel.
				// Note it does its own menu shadowing
				if (WebLookAndFeelUtils.isWebLookAndFeel(lnfClassName)) {
					ShadowPopupFactory.uninstall();
					WebLookAndFeelUtils.installWebLookAndFeelProperties(cl);
				}
				else {
					ShadowPopupFactory.install();
				}

				// Load the Look and Feel class.  Note that we cannot
				// simply use its name for some reason (Exceptions are
				// thrown).
				Class<?> c = cl.loadClass(lnfClassName);
				LookAndFeel lnf = (LookAndFeel)c.newInstance();

				// If we're changing to a LAF that supports window decorations
				// and our current one doesn't, or vice versa, inform the
				// user that this change will occur on restart.  Substance
				// seems to be the only troublemaker here (Metal, for example,
				// supports window decorations, but works fine without special
				// logic).
				boolean curSubstance = SubstanceUtils.isSubstanceInstalled();
				boolean nextSubstance = SubstanceUtils.
											isASubstanceLookAndFeel(lnf);
				if (curSubstance!=nextSubstance) {
					String message = rtext.getString(
									"Info.LookAndFeel.LoadOnNextRestart");
					String title = rtext.getString("InfoDialogHeader");
					JOptionPane.showMessageDialog(rtext, message, title,
											JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				UIManager.setLookAndFeel(lnf);
				// Re-save the class loader BEFORE calling
				// updateLookAndFeels(), as the UIManager.setLookAndFeel()
				// call above resets this property to null, and we need
				// this class loader to be set as it's the one that's aware
				// of our 3rd party JARs.  Swing uses this property (if
				// non-null) to load classes from, and if we don't set it,
				// exceptions will be thrown.
				UIManager.getLookAndFeelDefaults().put("ClassLoader", cl);
				UIUtil.installOsSpecificLafTweaks();
				StoreKeeper.updateLookAndFeels(lnf);

			} catch (NoClassDefFoundError ncdfe) {
				// They missed a required LaF jar on in LookAndFeels.xml
				rtext.displayException(ncdfe);
			} catch (Exception e) {
				rtext.displayException(e);
			}

		}

	}


	/**
	 * Strips ".orig", ".bak", and ".old" from the end of a fileName, if they
	 * are there.
	 *
	 * @param fileName The file name.
	 * @return The same file name, with any of the above extensions removed.
	 */
	public static String stripBackupExtensions(String fileName) {
		if (fileName!=null) {
			if (fileName.endsWith(".bak") ||
					fileName.endsWith(".old")) {
				fileName = fileName.substring(0, fileName.length() - 4);
			}
			else if (fileName.endsWith(".orig")) {
				fileName = fileName.substring(0, fileName.length() - 5);
			}
		}
		return fileName;
	}


	static {
		String os = System.getProperty("os.name").toLowerCase();
		CASE_SENSITIVE = os.indexOf("windows")==-1 &&
				os.indexOf("mac os x")==-1;
	}


}