/*
 * 06/03/2010
 *
 * LangSupportPreferences.java - Preferences for this plugin.
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

import java.io.File;

import org.fife.rsta.ac.java.JarInfo;
import org.fife.rsta.ac.perl.PerlLanguageSupport;
import org.fife.ui.app.Prefs;


/**
 * Preferences for the plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class LangSupportPreferences extends Prefs {

	public boolean c_enabled;
	public boolean c_paramAssistance;
	public boolean c_showDescWindow;
	public boolean c_folding_enabled;

	public boolean cpp_folding_enabled;

	public boolean cs_folding_enabled;

	public boolean css_folding_enabled;

	public boolean groovy_folding_enabled;

	public boolean html_enabled;
	public boolean html_showDescWindow;
	public boolean html_autoActivation;
	public int html_autoActivationDelay;

	public boolean java_enabled;
	public boolean java_paramAssistance;
	public boolean java_showDescWindow;
	public boolean java_autoActivation;
	public int java_autoActivationDelay;
	public String[] java_classpath_jars;
	public String[] java_classpath_src; // Same length as java_classpath_jars
	public boolean java_checkForBuildPathMods;
	public boolean java_folding_enabled;

	public boolean js_folding_enabled;

	public boolean jsp_enabled;

	public boolean mxml_folding_enabled;

	public boolean perl_enabled;
	public boolean perl_paramAssistance;
	public boolean perl_showDescWindow;
	public boolean perl_useParens;
	public boolean perl_compile;
	public File perl_installLoc;
	public boolean perl_warnings;
	public boolean perl_taintMode;
	public boolean perl_override_perl5lib;
	public String perl_overridden_perl5lib;
	public boolean perl_folding_enabled;

	public boolean php_enabled;
	public boolean php_showDescWindow;
	public boolean php_autoActivation;
	public int php_autoActivationDelay;

	public boolean sh_enabled;
	public boolean sh_showDescWindow;
	public boolean sh_useSystemManPages;


	public void setDefaults() {

		final int AUTO_ACTIVATION_DELAY = 300;

		c_enabled = true;
		c_paramAssistance = true;
		c_showDescWindow = true;
		c_folding_enabled = true;

		cpp_folding_enabled = true;

		cs_folding_enabled = true;

		css_folding_enabled = true;

		groovy_folding_enabled = true;

		html_enabled = true;
		html_showDescWindow = true;
		html_autoActivation = true;
		html_autoActivationDelay = AUTO_ACTIVATION_DELAY;

		java_enabled = false;
		java_paramAssistance = true;
		java_showDescWindow = true;
		java_autoActivation = false;
		java_autoActivationDelay = AUTO_ACTIVATION_DELAY;
		JarInfo info = JarInfo.getMainJREJarInfo();
		if (info==null) {
			java_classpath_jars = null;
			java_classpath_src = null;
		}
		else {
			File jar = info.getJarFile();
			File src = info.getSourceLocation();
			java_classpath_jars = new String[] { jar.getAbsolutePath() };
			String srcFile = src==null ? null : src.getAbsolutePath();
			java_classpath_src = new String[] { srcFile };
		}
		java_checkForBuildPathMods = true;
		java_folding_enabled = true;

		jsp_enabled = false;

		mxml_folding_enabled = true;

		perl_enabled = true;
		perl_paramAssistance = true;
		perl_showDescWindow = true;
		perl_useParens = true;
		File installLoc = PerlLanguageSupport.getDefaultPerlInstallLocation();
		if (installLoc!=null) {
			perl_compile = true;
			perl_installLoc = installLoc;
		}
		else {
			perl_compile = false;
			perl_installLoc = null;
		}
		perl_warnings = true;
		perl_taintMode = false;
		perl_override_perl5lib = false;
		perl_overridden_perl5lib = null;
		perl_folding_enabled = true;

		php_enabled = true;
		php_showDescWindow = true;
		php_autoActivation = true;
		php_autoActivationDelay = AUTO_ACTIVATION_DELAY;

		sh_enabled = true;
		sh_showDescWindow = File.separatorChar=='/';
		sh_useSystemManPages = File.separatorChar=='/';

	}


}