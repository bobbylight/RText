/*
 * 11/01/2009
 *
 * LanguageSupportFactory.java - Manages extra language support.
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
package org.fife.rtext.lang;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Manages language supports.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class LanguageSupportFactory {

	private static final LanguageSupportFactory INSTANCE =
									new LanguageSupportFactory();


	/**
	 * Private constructor to prevent instantiation.
	 */
	private LanguageSupportFactory() {
		
	}


	/**
	 * Returns the singleton instance of this class.
	 *
	 * @return The singleton instance.
	 */
	public static LanguageSupportFactory get() {
		return INSTANCE;
	}


	/**
	 * Gets the support for a specified language.
	 *
	 * @param language The programming language.  This should be one of the
	 *        values defined in {@link SyntaxConstants}.
	 * @return The support for the language, or <code>null</code> if none.
	 */
	public LanguageSupport getSupport(String language) {
		return null;
	}


}