/*
 * 10/10/2009
 *
 * NumberDocumentFilter.java - A document filter that only allows digits to
 * be typed.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import org.fife.ui.PickyDocumentFilter;


/**
 * A document filter that only allows digits to be entered.
 *
 * @author Robert Futrell
 * @version 1.1
 */
public class NumberDocumentFilter extends PickyDocumentFilter {


	/**
	 * Removes any characters in a string that aren't digits.
	 *
	 * @param text The string the user is trying to insert.
	 * @return The text, with any non-digit characters removed.
	 */
	@Override
	protected String cleanseImpl(String text) {
		int length = text.length();
		for (int i=0; i<length; i++) {
			if (!Character.isDigit(text.charAt(i))) {
				text = text.substring(0,i) + text.substring(i+1);
				i--;
				length--;
			}
		}
		return text;
	}


}