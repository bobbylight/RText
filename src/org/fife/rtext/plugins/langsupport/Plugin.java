/*
 * 05/20/2010
 *
 * Plugin.java - Entry point for the language support plugin.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.text.BadLocationException;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.html.HtmlLanguageSupport;
import org.fife.rsta.ac.java.JarManager;
import org.fife.rsta.ac.java.JavaLanguageSupport;
import org.fife.rsta.ac.java.buildpath.DirSourceLocation;
import org.fife.rsta.ac.java.buildpath.JarLibraryInfo;
import org.fife.rsta.ac.java.buildpath.LibraryInfo;
import org.fife.rsta.ac.java.buildpath.SourceLocation;
import org.fife.rsta.ac.java.buildpath.ZipSourceLocation;
import org.fife.rsta.ac.jsp.JspLanguageSupport;
import org.fife.rsta.ac.perl.PerlLanguageSupport;
import org.fife.rsta.ac.php.PhpLanguageSupport;
import org.fife.rsta.ac.sh.ShellLanguageSupport;
import org.fife.rsta.ac.xml.XmlLanguageSupport;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextMenuBar;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.plugins.sourcebrowser.SourceBrowserPlugin;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.AbstractPlugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.autocomplete.CompletionXMLParser;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.modes.XMLTokenMaker;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextAreaOptionPanel;
import org.fife.ui.rtextarea.RTextScrollPane;


/**
 * A plugin that enables code completion and language parsing support for
 * various programming languages.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Plugin extends AbstractPlugin {

	private RText rtext;
	private Listener listener;
	private Icon[] icons;

	private static final String PLUGIN_VERSION			= "2.5.2";
	private static final String PREFS_FILE_NAME			= "langSupport.properties";

	private static final String MSG = "org.fife.rtext.plugins.langsupport.Plugin";

	/**
	 * The resource bundle used across this plugin.
	 */
	static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	public Plugin(AbstractPluggableGUIApplication app) {
		setOptionsDialogPanelParentPanelID(RTextAreaOptionPanel.ID);
	}


	/**
	 * Adds plugin-specific actions to RText's menu bar.
	 */
	private void addActionsToMenus() {

		RTextMenuBar mb = (RTextMenuBar)rtext.getJMenuBar();

		// Add "Go to Member" action to the end of the "Go to..." menu section.
		JMenu searchMenu = mb.getMenuByName(RTextMenuBar.MENU_SEARCH);
		for (int i=searchMenu.getMenuComponentCount()-1; i>=0; i--) {
			Component c = searchMenu.getMenuComponent(i);
			if (c instanceof JSeparator) {
				GoToMemberAction gtma = new GoToMemberAction(rtext);
				JMenuItem item = new JMenuItem(gtma);
				item.setToolTipText(null);
				searchMenu.insert(item, i);
				break;
			}
		}

	}


	private void addSupport(RSyntaxTextArea textArea) {

		textArea.addPropertyChangeListener(
				RSyntaxTextArea.PARSER_NOTICES_PROPERTY, listener);

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		lsf.register(textArea);

	}


	/**
	 * Creates an icon from an image resource in this package.
	 *
	 * @param res The resource.
	 * @return The icon.
	 */
	private Icon createIcon(String res) {
		Icon icon = null;
		try {
			icon = new ImageIcon(ImageIO.read(getClass().getResource(res)));
		} catch (IOException ioe) { // Never happens
			ioe.printStackTrace();
		}
		return icon;
	}


	/**
	 * {@inheritDoc}
	 */
	public PluginOptionsDialogPanel getOptionsDialogPanel() {
		return new OptionsPanel(this);
	}


	/**
	 * {@inheritDoc}
	 */
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	/**
	 * {@inheritDoc}
	 */
	public Icon getPluginIcon() {
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getPluginName() {
		return msg.getString("Name");
	}


	/**
	 * {@inheritDoc}
	 */
	public String getPluginVersion() {
		return PLUGIN_VERSION;
	}


	/**
	 * {@inheritDoc}
	 */
	public void install(AbstractPluggableGUIApplication app) {

		icons = new Icon[3];
		icons[0] = createIcon("error_obj.gif");
		icons[1] = createIcon("warning_obj.gif");
		// Informational icons are annoying - spelling errors, etc.
		//icons[2] = createIcon("info_obj.gif");

		rtext = (RText)app;
		listener = new Listener();
		AbstractMainView view = rtext.getMainView();
		for (int i=0; i<view.getNumDocuments(); i++) {
			addSupport(view.getRTextEditorPaneAt(i));
		}
		view.addPropertyChangeListener(listener);

		// Custom FunctionCompletions can only be loaded from the plugin jar.
		CompletionXMLParser.setDefaultCompletionClassLoader(
											getClass().getClassLoader());

		loadPreferences();

		addActionsToMenus();

		// Install our custom source browser tree views for some languages.
		System.setProperty(
				SourceBrowserPlugin.CUSTOM_HANDLER_PREFIX + RSyntaxTextArea.SYNTAX_STYLE_JAVA,
				"org.fife.rtext.plugins.langsupport.JavaSourceBrowserTreeConstructor");
		System.setProperty(
				SourceBrowserPlugin.CUSTOM_HANDLER_PREFIX + RSyntaxTextArea.SYNTAX_STYLE_JAVASCRIPT,
				"org.fife.rtext.plugins.langsupport.JavaScriptSourceBrowserTreeConstructor");
		System.setProperty(
				SourceBrowserPlugin.CUSTOM_HANDLER_PREFIX + RSyntaxTextArea.SYNTAX_STYLE_XML,
				"org.fife.rtext.plugins.langsupport.XmlSourceBrowserTreeConstructor");


	}


	private void loadPreferences() {

		LangSupportPreferences prefs = new LangSupportPreferences();

		File file = new File(RTextUtilities.getPreferencesDirectory(),
				PREFS_FILE_NAME);
		if (file.isFile()) {
			try {
				prefs.load(file);
			} catch (IOException ioe) {
				rtext.displayException(ioe);
			}
		}

		LanguageSupportFactory fact = LanguageSupportFactory.get();
		AbstractMainView view = rtext.getMainView();

		String language = SyntaxConstants.SYNTAX_STYLE_C;
		LanguageSupport ls = fact.getSupportFor(language);
		ls.setAutoCompleteEnabled(prefs.c_enabled);
		ls.setParameterAssistanceEnabled(prefs.c_paramAssistance);
		ls.setShowDescWindow(prefs.c_showDescWindow);
		view.setCodeFoldingEnabledFor(language, prefs.c_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
		view.setCodeFoldingEnabledFor(language, prefs.cpp_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_CSHARP;
		view.setCodeFoldingEnabledFor(language, prefs.cs_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_CSS;
		view.setCodeFoldingEnabledFor(language, prefs.css_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_GROOVY;
		view.setCodeFoldingEnabledFor(language, prefs.groovy_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_HTML;
		ls = fact.getSupportFor(language);
		HtmlLanguageSupport hls = (HtmlLanguageSupport)ls;
		hls.setAutoCompleteEnabled(prefs.html_enabled);
		hls.setShowDescWindow(prefs.html_showDescWindow);
		hls.setAutoActivationDelay(prefs.html_autoActivationDelay);
		hls.setAutoActivationEnabled(prefs.html_autoActivation);
		hls.setAutoAddClosingTags(prefs.html_autoAddClosingTags);
		view.setCodeFoldingEnabledFor(language, prefs.html_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_JAVA;
		ls = fact.getSupportFor(language);
		JavaLanguageSupport jls = (JavaLanguageSupport)ls;
		jls.setAutoCompleteEnabled(prefs.java_enabled);
		jls.setParameterAssistanceEnabled(prefs.java_paramAssistance);
		jls.setShowDescWindow(prefs.java_showDescWindow);
		JarManager.setCheckModifiedDatestamps(prefs.java_checkForBuildPathMods);
		view.setCodeFoldingEnabledFor(language, prefs.java_folding_enabled);

		jls.setAutoActivationDelay(prefs.java_autoActivationDelay);
		jls.setAutoActivationEnabled(prefs.java_autoActivation);
		JarManager jarMan = jls.getJarManager();
		jarMan.clearClassFileSources();
		int count = prefs.java_classpath_jars==null ? 0 :
						prefs.java_classpath_jars.length;
		for (int i=0; i<count; i++) {
			File jar = new File(prefs.java_classpath_jars[i]);
			JarLibraryInfo info = new JarLibraryInfo(jar);
			if (prefs.java_classpath_src[i]!=null) {
				File src = new File(prefs.java_classpath_src[i]);
				if (src.isFile()) {
					info.setSourceLocation(new ZipSourceLocation(src));
				}
				else { // Assume source folder
					info.setSourceLocation(new DirSourceLocation(src));
				}
			}
			try {
				jarMan.addClassFileSource(info);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		language = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
		view.setCodeFoldingEnabledFor(language, prefs.js_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_JSON;
		view.setCodeFoldingEnabledFor(language, prefs.json_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_JSP;
		ls = fact.getSupportFor(language);
		JspLanguageSupport jspls = (JspLanguageSupport)ls;
		jspls.setAutoCompleteEnabled(prefs.jsp_enabled);
		jspls.setAutoAddClosingTags(prefs.jsp_autoAddClosingTags);
		view.setCodeFoldingEnabledFor(language, prefs.jsp_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_LATEX;
		view.setCodeFoldingEnabledFor(language, prefs.latex_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_MXML;
		view.setCodeFoldingEnabledFor(language, prefs.mxml_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_NSIS;
		view.setCodeFoldingEnabledFor(language, prefs.nsis_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_PERL;
		ls = fact.getSupportFor(language);
		PerlLanguageSupport pls = (PerlLanguageSupport)ls;
		pls.setParsingEnabled(prefs.perl_compile);
		pls.setAutoCompleteEnabled(prefs.perl_enabled);
		PerlLanguageSupport.setPerlInstallLocation(prefs.perl_installLoc);
		pls.setParameterAssistanceEnabled(prefs.perl_paramAssistance);
		pls.setShowDescWindow(prefs.perl_showDescWindow);
		pls.setTaintModeEnabled(prefs.perl_taintMode);
		pls.setUseParensWithFunctions(prefs.perl_useParens);
		pls.setWarningsEnabled(prefs.perl_warnings);
		if (!prefs.perl_override_perl5lib) {
			pls.setPerl5LibOverride(null);
		}
		else {
			String override = prefs.perl_overridden_perl5lib;
			if (override!=null && override.length()==0) {
				override = null;
			}
			pls.setPerl5LibOverride(override);
		}
		view.setCodeFoldingEnabledFor(language, prefs.perl_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_PHP;
		ls = fact.getSupportFor(language);
		ls.setAutoCompleteEnabled(prefs.php_enabled);
		ls.setShowDescWindow(prefs.php_showDescWindow);
		ls.setAutoActivationEnabled(prefs.php_autoActivation);
		ls.setAutoActivationDelay(prefs.php_autoActivationDelay);
		PhpLanguageSupport phpls = (PhpLanguageSupport)ls;
		phpls.setAutoAddClosingTags(prefs.php_autoAddClosingTags);
		view.setCodeFoldingEnabledFor(language, prefs.php_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_SCALA;
		view.setCodeFoldingEnabledFor(language, prefs.scala_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
		ls = fact.getSupportFor(language);
		ShellLanguageSupport sls = (ShellLanguageSupport)ls;
		sls.setAutoCompleteEnabled(prefs.sh_enabled);
		sls.setShowDescWindow(prefs.sh_showDescWindow);
		sls.setUseLocalManPages(prefs.sh_useSystemManPages);

		language = SyntaxConstants.SYNTAX_STYLE_XML;
		ls = fact.getSupportFor(language);
		XmlLanguageSupport xls = (XmlLanguageSupport)ls;
		view.setCodeFoldingEnabledFor(language, prefs.xml_folding_enabled);
		XMLTokenMaker.setCompleteCloseTags(prefs.xml_autoCloseTags);
		xls.setShowSyntaxErrors(prefs.xml_showSyntaxErrors);

	}

	private void removeSupport(RSyntaxTextArea textArea) {

		textArea.addPropertyChangeListener(
				RSyntaxTextArea.PARSER_NOTICES_PROPERTY, listener);

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		lsf.unregister(textArea);

	}


	public void savePreferences() {

		LangSupportPreferences prefs = new LangSupportPreferences();
		LanguageSupportFactory fact = LanguageSupportFactory.get();
		AbstractMainView view = rtext.getMainView();

		String language = SyntaxConstants.SYNTAX_STYLE_C;
		LanguageSupport ls = fact.getSupportFor(language);
		prefs.c_enabled = ls.isAutoCompleteEnabled();
		prefs.c_paramAssistance = ls.isParameterAssistanceEnabled();
		prefs.c_showDescWindow = ls.getShowDescWindow();
		prefs.c_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
		prefs.cpp_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_CSHARP;
		prefs.cs_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_CSS;
		prefs.css_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_GROOVY;
		prefs.groovy_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_HTML;
		ls = fact.getSupportFor(language);
		HtmlLanguageSupport hls = (HtmlLanguageSupport)ls;
		prefs.html_enabled = hls.isAutoCompleteEnabled();
		prefs.html_showDescWindow = hls.getShowDescWindow();
		prefs.html_autoActivation = hls.isAutoActivationEnabled();
		prefs.html_autoAddClosingTags = hls.getAutoAddClosingTags();
		prefs.html_autoActivationDelay = hls.getAutoActivationDelay();
		prefs.html_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_JAVA;
		ls = fact.getSupportFor(language);
		JavaLanguageSupport jls = (JavaLanguageSupport)ls;
		prefs.java_enabled = jls.isAutoCompleteEnabled();
		prefs.java_paramAssistance = jls.isParameterAssistanceEnabled();
		prefs.java_showDescWindow = jls.getShowDescWindow();
		prefs.java_autoActivation = jls.isAutoActivationEnabled();
		prefs.java_autoActivationDelay = jls.getAutoActivationDelay();
		List<LibraryInfo> jars = jls.getJarManager().getClassFileSources();
		int count = jars==null ? 0 : jars.size();
		if (count==0) {
			prefs.java_classpath_jars = null;
			prefs.java_classpath_src = null;
		}
		else {
			prefs.java_classpath_jars = new String[count];
			prefs.java_classpath_src = new String[count];
			for (int i=0; i<count; i++) {
				LibraryInfo info = jars.get(i);
				String jarFile = info.getLocationAsString();
				prefs.java_classpath_jars[i] = jarFile;
				SourceLocation srcLocFile = info.getSourceLocation();
				if (srcLocFile!=null) {
					prefs.java_classpath_src[i] = srcLocFile.getLocationAsString();
				}
			}
		}
		prefs.java_checkForBuildPathMods = JarManager.getCheckModifiedDatestamps();
		prefs.java_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
		prefs.js_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_JSON;
		prefs.json_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_JSP;
		ls = fact.getSupportFor(language);
		JspLanguageSupport jspls = (JspLanguageSupport)ls;
		prefs.jsp_enabled = jspls.isAutoCompleteEnabled();
		prefs.jsp_autoAddClosingTags = jspls.getAutoAddClosingTags();
		prefs.jsp_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_LATEX;
		prefs.latex_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_MXML;
		prefs.mxml_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_NSIS;
		prefs.nsis_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_PERL;
		ls = fact.getSupportFor(language);
		PerlLanguageSupport pls = (PerlLanguageSupport)ls;
		prefs.perl_compile = pls.isParsingEnabled();
		prefs.perl_enabled = pls.isAutoCompleteEnabled();
		prefs.perl_installLoc = PerlLanguageSupport.getPerlInstallLocation();
		prefs.perl_paramAssistance = pls.isParameterAssistanceEnabled();
		prefs.perl_showDescWindow = pls.getShowDescWindow();
		prefs.perl_taintMode = pls.isTaintModeEnabled();
		prefs.perl_useParens = pls.getUseParensWithFunctions();
		prefs.perl_warnings = pls.getWarningsEnabled();
		prefs.perl_overridden_perl5lib = pls.getPerl5LibOverride();
		prefs.perl_override_perl5lib = prefs.perl_overridden_perl5lib!=null;
		prefs.perl_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_PHP;
		ls = fact.getSupportFor(language);
		prefs.php_enabled = ls.isAutoCompleteEnabled();
		prefs.php_showDescWindow = ls.getShowDescWindow();
		prefs.php_autoActivation = ls.isAutoActivationEnabled();
		prefs.php_autoActivationDelay = ls.getAutoActivationDelay();
		PhpLanguageSupport phpls = (PhpLanguageSupport)ls;
		prefs.php_autoAddClosingTags = phpls.getAutoAddClosingTags();
		prefs.php_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_SCALA;
		prefs.scala_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
		ls = fact.getSupportFor(language);
		ShellLanguageSupport sls = (ShellLanguageSupport)ls;
		prefs.sh_enabled = sls.isAutoCompleteEnabled();
		prefs.sh_showDescWindow = sls.getShowDescWindow();
		prefs.sh_useSystemManPages = sls.getUseLocalManPages();

		language = SyntaxConstants.SYNTAX_STYLE_XML;
		ls = fact.getSupportFor(language);
		XmlLanguageSupport xls = (XmlLanguageSupport)ls;
		prefs.xml_folding_enabled = view.isCodeFoldingEnabledFor(language);
		prefs.xml_autoCloseTags = XMLTokenMaker.getCompleteCloseMarkupTags();
		prefs.xml_showSyntaxErrors = xls.getShowSyntaxErrors();

		File file = new File(RTextUtilities.getPreferencesDirectory(),
								PREFS_FILE_NAME);
		try {
			prefs.save(file);
		} catch (IOException ioe) {
			rtext.displayException(ioe);
		}

	}


	/**
	 * {@inheritDoc}
	 */
	public boolean uninstall() {
		AbstractMainView view = rtext.getMainView();
		for (int i=0; i<view.getNumDocuments(); i++) {
			removeSupport(view.getRTextEditorPaneAt(i));
		}
		view.removePropertyChangeListener(listener);
		return true;
	}


	/**
	 * Listens for events in RText.
	 */
	private class Listener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent e) {

			String name = e.getPropertyName();

			// A text area has been re-parsed
			if (RSyntaxTextArea.PARSER_NOTICES_PROPERTY.equals(name)) {
				RSyntaxTextArea textArea = (RSyntaxTextArea)e.getSource();
				RTextScrollPane sp = (RTextScrollPane)textArea.getParent().
															getParent();
				Gutter g = sp.getGutter();
				// TODO: Note this isn't entirely correct; if some other
				// component has added tracking icons to the gutter, this will
				// remove those as well!
				g.removeAllTrackingIcons();
				List<ParserNotice> notices = textArea.getParserNotices();
				for (ParserNotice notice : notices) {
					int line = notice.getLine();
					Icon icon = icons[notice.getLevel()];
					try {
						g.addLineTrackingIcon(line, icon, notice.getMessage());
					} catch (BadLocationException ble) { // Never happens
						System.err.println("*** Error adding notice:\n" +
								notice + ":");
						ble.printStackTrace();
					}
				}
				
			}

			else if (AbstractMainView.TEXT_AREA_ADDED_PROPERTY.equals(name)) {
				RSyntaxTextArea textArea = (RSyntaxTextArea)e.getNewValue();
				addSupport(textArea);
			}

			else if (AbstractMainView.TEXT_AREA_REMOVED_PROPERTY.equals(name)) {
				RSyntaxTextArea old = (RSyntaxTextArea)e.getNewValue();
				removeSupport(old);
			}
			
		}

	}


}