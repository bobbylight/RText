/*
 * 05/20/2010
 *
 * Plugin.java - Entry point for the language support plugin.
 * Copyright (C) 2010 Robert Futrell
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
package org.fife.rtext.plugins.langsupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.java.JarInfo;
import org.fife.rsta.ac.java.JarManager;
import org.fife.rsta.ac.java.JavaLanguageSupport;
import org.fife.rsta.ac.perl.PerlLanguageSupport;
import org.fife.rsta.ac.sh.ShellLanguageSupport;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.autocomplete.CompletionXMLParser;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;


/**
 * A plugin that enables code completion and language parsing support for
 * various programming languages.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Plugin implements org.fife.ui.app.Plugin {

	private RText rtext;
	private Listener listener;
	private Icon[] icons;

	private static final String PLUGIN_VERSION			= "1.3.0";
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

		LanguageSupport ls = fact.getSupportFor(SyntaxConstants.SYNTAX_STYLE_C);
		ls.setAutoCompleteEnabled(prefs.c_enabled);
		ls.setParameterAssistanceEnabled(prefs.c_paramAssistance);
		ls.setShowDescWindow(prefs.c_showDescWindow);

		ls = fact.getSupportFor(SyntaxConstants.SYNTAX_STYLE_HTML);
		ls.setAutoCompleteEnabled(prefs.html_enabled);
		ls.setShowDescWindow(prefs.html_showDescWindow);
		ls.setAutoActivationDelay(prefs.html_autoActivationDelay);
		ls.setAutoActivationEnabled(prefs.html_autoActivation);

		ls = fact.getSupportFor(SyntaxConstants.SYNTAX_STYLE_JAVA);
		JavaLanguageSupport jls = (JavaLanguageSupport)ls;
		jls.setAutoCompleteEnabled(prefs.java_enabled);
		jls.setParameterAssistanceEnabled(prefs.java_paramAssistance);
		jls.setShowDescWindow(prefs.java_showDescWindow);
		JarManager.setCheckModifiedDatestamps(prefs.java_checkForBuildPathMods);

		jls.setAutoActivationDelay(prefs.java_autoActivationDelay);
		jls.setAutoActivationEnabled(prefs.java_autoActivation);
		JarManager jarMan = jls.getJarManager();
		jarMan.clearJars();
		int count = prefs.java_classpath_jars==null ? 0 :
						prefs.java_classpath_jars.length;
		for (int i=0; i<count; i++) {
			File jar = new File(prefs.java_classpath_jars[i]);
			JarInfo info = new JarInfo(jar);
			if (prefs.java_classpath_src[i]!=null) {
				File src = new File(prefs.java_classpath_src[i]);
				info.setSourceLocation(src);
			}
			try {
				jarMan.addJar(info);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		ls = fact.getSupportFor(SyntaxConstants.SYNTAX_STYLE_PERL);
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

		ls = fact.getSupportFor(SyntaxConstants.SYNTAX_STYLE_PHP);
		ls.setAutoCompleteEnabled(prefs.php_enabled);
		ls.setShowDescWindow(prefs.php_showDescWindow);
		ls.setAutoActivationEnabled(prefs.php_autoActivation);
		ls.setAutoActivationDelay(prefs.php_autoActivationDelay);

		ls = fact.getSupportFor(SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
		ShellLanguageSupport sls = (ShellLanguageSupport)ls;
		sls.setAutoCompleteEnabled(prefs.sh_enabled);
		sls.setShowDescWindow(prefs.sh_showDescWindow);
		sls.setUseLocalManPages(prefs.sh_useSystemManPages);

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

		LanguageSupport ls = fact.getSupportFor(SyntaxConstants.SYNTAX_STYLE_C);
		prefs.c_enabled = ls.isAutoCompleteEnabled();
		prefs.c_paramAssistance = ls.isParameterAssistanceEnabled();
		prefs.c_showDescWindow = ls.getShowDescWindow();

		ls = fact.getSupportFor(SyntaxConstants.SYNTAX_STYLE_HTML);
		prefs.html_enabled = ls.isAutoCompleteEnabled();
		prefs.html_showDescWindow = ls.getShowDescWindow();
		prefs.html_autoActivation = ls.isAutoActivationEnabled();
		prefs.html_autoActivationDelay = ls.getAutoActivationDelay();

		ls = fact.getSupportFor(SyntaxConstants.SYNTAX_STYLE_JAVA);
		JavaLanguageSupport jls = (JavaLanguageSupport)ls;
		prefs.java_enabled = jls.isAutoCompleteEnabled();
		prefs.java_paramAssistance = jls.isParameterAssistanceEnabled();
		prefs.java_showDescWindow = jls.getShowDescWindow();
		prefs.java_autoActivation = jls.isAutoActivationEnabled();
		prefs.java_autoActivationDelay = jls.getAutoActivationDelay();
		List jars = jls.getJarManager().getJars();
		int count = jars==null ? 0 : jars.size();
		if (count==0) {
			prefs.java_classpath_jars = null;
			prefs.java_classpath_src = null;
		}
		else {
			prefs.java_classpath_jars = new String[count];
			prefs.java_classpath_src = new String[count];
			for (int i=0; i<count; i++) {
				JarInfo info = (JarInfo)jars.get(i);
				File jarFile = info.getJarFile();
				prefs.java_classpath_jars[i] = jarFile.getAbsolutePath();
				File srcLocFile = info.getSourceLocation();
				if (srcLocFile!=null) {
					prefs.java_classpath_src[i] = srcLocFile.getAbsolutePath();
				}
			}
		}
		prefs.java_checkForBuildPathMods = JarManager.getCheckModifiedDatestamps();

		ls = fact.getSupportFor(SyntaxConstants.SYNTAX_STYLE_PERL);
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

		ls = fact.getSupportFor(SyntaxConstants.SYNTAX_STYLE_PHP);
		prefs.php_enabled = ls.isAutoCompleteEnabled();
		prefs.php_showDescWindow = ls.getShowDescWindow();
		prefs.php_autoActivation = ls.isAutoActivationEnabled();
		prefs.php_autoActivationDelay = ls.getAutoActivationDelay();

		ls = fact.getSupportFor(SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
		ShellLanguageSupport sls = (ShellLanguageSupport)ls;
		prefs.sh_enabled = sls.isAutoCompleteEnabled();
		prefs.sh_showDescWindow = sls.getShowDescWindow();
		prefs.sh_useSystemManPages = sls.getUseLocalManPages();

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
				List notices = textArea.getParserNotices();
				for (Iterator i=notices.iterator(); i.hasNext(); ) {
					ParserNotice notice = (ParserNotice)i.next();
					int line = notice.getLine();
					Icon icon = icons[notice.getLevel()];
					try {
						g.addLineTrackingIcon(line, icon);
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