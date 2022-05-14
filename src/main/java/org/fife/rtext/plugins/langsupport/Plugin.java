/*
 * 05/20/2010
 *
 * Plugin.java - Entry point for the language support plugin.
 * Copyright (C) 2010 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Icon;
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
import org.fife.rsta.ac.js.JavaScriptLanguageSupport;
import org.fife.rsta.ac.js.JsErrorParser;
import org.fife.rsta.ac.jsp.JspLanguageSupport;
import org.fife.rsta.ac.perl.PerlLanguageSupport;
import org.fife.rsta.ac.php.PhpLanguageSupport;
import org.fife.rsta.ac.sh.ShellLanguageSupport;
import org.fife.rsta.ac.xml.XmlLanguageSupport;
import org.fife.rtext.*;
import org.fife.rtext.plugins.langsupport.typescript.TypeScriptSupport;
import org.fife.rtext.plugins.sourcebrowser.SourceBrowserPlugin;
import org.fife.ui.app.GUIPlugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.icons.IconGroup;
import org.fife.ui.autocomplete.CompletionXMLParser;
import org.fife.ui.dockablewindows.DockableWindow;
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
public class Plugin extends GUIPlugin<RText> {

	private OptionsPanel optionsPanel;
	private Listener listener;
	private Map<ParserNotice.Level, Icon> icons;
	private TypeScriptSupport typeScriptSupport;

	private static final String PLUGIN_VERSION			= "5.0.0";
	private static final String PREFS_FILE_NAME			= "langSupport.properties";

	private static final String MSG_BUNDLE = "org.fife.rtext.plugins.langsupport.Plugin";

	/**
	 * The resource bundle used across this plugin.
	 */
	static final ResourceBundle MSG = ResourceBundle.getBundle(MSG_BUNDLE);


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	public Plugin(RText app) {
		super(app);
		setOptionsDialogPanelParentPanelID(RTextAreaOptionPanel.OPTION_PANEL_ID);
	}


	/**
	 * Adds plugin-specific actions to RText's menu bar.
	 */
	private void addActionsToMenus() {

		RText rtext = getApplication();
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
	 * Returns the resource bundle used by this plugin.
	 *
	 * @return The resource bundle.
	 */
	public ResourceBundle getBundle() {
		return MSG;
	}


	/**
	 * Changing visibility to public so sub-packages can register dockable
	 * windows.
	 *
	 * @param id The ID for the dockable window.
	 * @return The dockable window.
	 * @see #putDockableWindow(String, DockableWindow)
	 */
	@Override
	public DockableWindow getDockableWindow(String id) {
		return super.getDockableWindow(id);
	}


	@Override
	public PluginOptionsDialogPanel<Plugin> getOptionsDialogPanel() {
		if (optionsPanel == null) {
			optionsPanel = new OptionsPanel(this);
		}
		return optionsPanel;
	}


	@Override
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	@Override
	public Icon getPluginIcon() {
		return null;
	}


	@Override
	public String getPluginName() {
		return MSG.getString("Name");
	}


	@Override
	public String getPluginVersion() {
		return PLUGIN_VERSION;
	}


	/**
	 * Returns the TypeScript support object.  This has methods to control
	 * TypeScript-specific features of this plugin.
	 *
	 * @return The TypeScript support object.
	 */
	public TypeScriptSupport getTypeScriptSupport() {
		return typeScriptSupport;
	}


	@Override
	public void install() {

		icons = new HashMap<>();

		RText rtext = getApplication();
		listener = new Listener();
		rtext.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, listener);
		loadWarningAndErrorIcons(); // Have to manually load them the first time

		AbstractMainView view = rtext.getMainView();
		for (int i=0; i<view.getNumDocuments(); i++) {
			addSupport(view.getRTextEditorPaneAt(i));
		}
		view.addPropertyChangeListener(listener);

		// Custom FunctionCompletions can only be loaded from the plugin jar.
		CompletionXMLParser.setDefaultCompletionClassLoader(
											getClass().getClassLoader());

		LangSupportPreferences prefs = loadPreferences();

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

		// Language-specific tweaks
		typeScriptSupport = new TypeScriptSupport();
		typeScriptSupport.install(rtext, this, prefs);

	}


	private LangSupportPreferences loadPreferences() {

		RText rtext = getApplication();
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

		language = SyntaxConstants.SYNTAX_STYLE_D;
		view.setCodeFoldingEnabledFor(language, prefs.d_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_DART;
		view.setCodeFoldingEnabledFor(language, prefs.dart_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_GO;
		view.setCodeFoldingEnabledFor(language, prefs.go_folding_enabled);

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
			// Just in case the file went away out from under us
			if (jar.isFile()) {
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
		}

		language = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
		ls = fact.getSupportFor(language);
		JavaScriptLanguageSupport jsls = (JavaScriptLanguageSupport)ls;
		jsls.setAutoCompleteEnabled(prefs.js_enabled);
		jsls.setParameterAssistanceEnabled(prefs.js_paramAssistance);
		jsls.setShowDescWindow(prefs.js_showDescWindow);
		jsls.setAutoActivationEnabled(prefs.js_autoActivation);
		jsls.setAutoActivationDelay(prefs.js_autoActivationDelay);
		JsErrorParser jsErrorParser = JsErrorParser.RHINO;
		if (prefs.js_syntaxCheckingEngine!=null) {
			try {
				jsErrorParser = JsErrorParser.valueOf(
						prefs.js_syntaxCheckingEngine);
			} catch (Exception e) {
				e.printStackTrace(); // Keep default
			}
		}
		jsls.setErrorParser(jsErrorParser);
		if (prefs.js_jshintRcFile!=null &&
				prefs.js_jshintRcFile.isFile()) {
			jsls.setDefaultJsHintRCFile(prefs.js_jshintRcFile);
		}
		jsls.setStrictMode(prefs.js_rhinoStrictSyntaxChecking);
		jsls.setXmlAvailable(prefs.js_rhinoAllowE4x);
		view.setCodeFoldingEnabledFor(language, prefs.js_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS;
		view.setCodeFoldingEnabledFor(language, prefs.jshintrc_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_JSON;
		view.setCodeFoldingEnabledFor(language, prefs.json_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_JSP;
		ls = fact.getSupportFor(language);
		JspLanguageSupport jspls = (JspLanguageSupport)ls;
		jspls.setAutoCompleteEnabled(prefs.jsp_enabled);
		jspls.setAutoAddClosingTags(prefs.jsp_autoAddClosingTags);
		view.setCodeFoldingEnabledFor(language, prefs.jsp_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_KOTLIN;
		view.setCodeFoldingEnabledFor(language, prefs.kotlin_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_LATEX;
		view.setCodeFoldingEnabledFor(language, prefs.latex_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_LESS;
		view.setCodeFoldingEnabledFor(language, prefs.less_folding_enabled);

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
			if (override!=null && override.isEmpty()) {
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

		language = SyntaxConstants.SYNTAX_STYLE_PYTHON;
		view.setCodeFoldingEnabledFor(language, prefs.python_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_SCALA;
		view.setCodeFoldingEnabledFor(language, prefs.scala_folding_enabled);

		language = SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT;
		view.setCodeFoldingEnabledFor(language, prefs.ts_folding_enabled);

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

		return prefs;
	}


	private void loadWarningAndErrorIcons() {
		icons.put(ParserNotice.Level.ERROR,
			getApplication().getIconGroup().getIcon("error_annotation", 14, 14));
		icons.put(ParserNotice.Level.WARNING,
			getApplication().getIconGroup().getIcon("warning_annotation", 14, 14));
		// Informational icons are annoying - spelling errors, etc.
		//icons.put(ParserNotice.Level.INFO, createIcon("info_obj.gif"));
	}


	/**
	 * Changing visibility to public so sub-packages can register dockable
	 * windows.
	 *
	 * @param id The ID for the dockable window.
	 * @param window The dockable window to register.
	 * @see #getDockableWindow(String)
	 */
	@Override
	public void putDockableWindow(String id, DockableWindow window) {
		super.putDockableWindow(id, window);
	}


	private void removeSupport(RSyntaxTextArea textArea) {

		textArea.addPropertyChangeListener(
				RSyntaxTextArea.PARSER_NOTICES_PROPERTY, listener);

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		lsf.unregister(textArea);

	}


	@Override
	public void savePreferences() {

		LangSupportPreferences prefs = new LangSupportPreferences();
		LanguageSupportFactory fact = LanguageSupportFactory.get();
		RText rtext = getApplication();
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

		language = SyntaxConstants.SYNTAX_STYLE_D;
		prefs.d_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_DART;
		prefs.dart_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_GO;
		prefs.go_folding_enabled = view.isCodeFoldingEnabledFor(language);

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
		ls = fact.getSupportFor(language);
		JavaScriptLanguageSupport jsls = (JavaScriptLanguageSupport)ls;
		prefs.js_enabled = jsls.isAutoCompleteEnabled();
		prefs.js_paramAssistance = jsls.isParameterAssistanceEnabled();
		prefs.js_showDescWindow = jsls.getShowDescWindow();
		prefs.js_autoActivation = jsls.isAutoActivationEnabled();
		prefs.js_autoActivationDelay = jsls.getAutoActivationDelay();
		prefs.js_syntaxCheckingEngine = jsls.getErrorParser().name();
		prefs.js_jshintRcFile = jsls.getDefaultJsHintRCFile();
		prefs.js_rhinoStrictSyntaxChecking = jsls.isStrictMode();
		prefs.js_rhinoAllowE4x = jsls.isXmlAvailable();
		prefs.js_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS;
		prefs.jshintrc_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_JSON;
		prefs.json_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_JSP;
		ls = fact.getSupportFor(language);
		JspLanguageSupport jspls = (JspLanguageSupport)ls;
		prefs.jsp_enabled = jspls.isAutoCompleteEnabled();
		prefs.jsp_autoAddClosingTags = jspls.getAutoAddClosingTags();
		prefs.jsp_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_KOTLIN;
		prefs.kotlin_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_LATEX;
		prefs.latex_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_LESS;
		prefs.less_folding_enabled = view.isCodeFoldingEnabledFor(language);

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

		language = SyntaxConstants.SYNTAX_STYLE_PYTHON;
		prefs.python_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_SCALA;
		prefs.scala_folding_enabled = view.isCodeFoldingEnabledFor(language);

		language = SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT;
		prefs.ts_folding_enabled = view.isCodeFoldingEnabledFor(language);
		typeScriptSupport.save(rtext, prefs);

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


	@Override
	public boolean uninstall() {
		AbstractMainView view = getApplication().getMainView();
		for (int i=0; i<view.getNumDocuments(); i++) {
			removeSupport(view.getRTextEditorPaneAt(i));
		}
		view.removePropertyChangeListener(listener);
		getApplication().removePropertyChangeListener(RText.ICON_STYLE_PROPERTY, listener);
		return true;
	}


	@Override
	public void updateIconsForNewIconGroup(IconGroup iconGroup) {
		if (optionsPanel != null) {
			optionsPanel.setIcon(getPluginIcon());
		}
	}


	/**
	 * Listens for events in RText.
	 */
	private class Listener implements PropertyChangeListener {

		@Override
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
					Icon icon = icons.get(notice.getLevel());
					if (icon!=null) {
						try {
							g.addLineTrackingIcon(line, icon,
									notice.getMessage());
						} catch (BadLocationException ble) { // Never happens
							System.err.println("*** Error adding notice:\n" +
									notice + ":");
							ble.printStackTrace();
						}
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

			else if (RText.ICON_STYLE_PROPERTY.equals(name)) {
				loadWarningAndErrorIcons();
			}
		}

	}


}
