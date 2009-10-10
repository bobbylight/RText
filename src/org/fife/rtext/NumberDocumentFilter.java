/*
 * 10/10/2009
 *
 * NumberDocumentFilter.java - A document filter that only allows digits to
 * be typed.
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

import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;


/**
 * A document filter that only allows digits to be entered.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class NumberDocumentFilter extends DocumentFilter {


	private String cleanse(String text) {
		boolean beep = false;
		if (text!=null) {
			int length = text.length();
			for (int i=0; i<length; i++) {
				if (!Character.isDigit(text.charAt(i))) {
					text = text.substring(0,i) + text.substring(i+1);
					i--;
					length--;
					beep = true;
				}
			}
		}
		if (beep) {
			UIManager.getLookAndFeel().provideErrorFeedback(null);
		}
		return text;
	}


	public void insertString(DocumentFilter.FilterBypass fb, int offset,
				String text, AttributeSet attr) throws BadLocationException {
		fb.insertString(offset, cleanse(text), attr);
	}


	public void remove(DocumentFilter.FilterBypass fb,
				int offset, int length)
					throws BadLocationException {
		fb.remove(offset, length);
	}


	public void replace(DocumentFilter.FilterBypass fb, int offset, int length,
			String text, AttributeSet attr) throws BadLocationException {
		fb.replace(offset, length, cleanse(text), attr);
	}


}