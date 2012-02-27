/*
 * 07/23/2011
 *
 * KeyStrokeField.java - A text field that lets you enter a keyboard shortcut.
 * Copyright (C) 2011 Robert Futrell
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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;



/**
 * A text field that lets a user enter a <code>KeyStroke</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class KeyStrokeField extends JTextField {

	private KeyStroke stroke;
	private FocusAdapter listener;


	public KeyStrokeField() {
		super(20);
		listener = new FocusHandler();
		addFocusListener(listener);
	}


	/**
	 * Returns the key stroke they've entered.
	 *
	 * @return The key stroke, or <code>null</code> if nothing is
	 *         entered.
	 */
	public KeyStroke getKeyStroke() {
		return stroke;
	}


	protected void processKeyEvent(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (e.getID()==KeyEvent.KEY_PRESSED &&
			keyCode!=KeyEvent.VK_ENTER &&
			keyCode!=KeyEvent.VK_BACK_SPACE) {
			int modifiers = e.getModifiers();
			setKeyStroke(KeyStroke.getKeyStroke(keyCode, modifiers));
			return;
		}
		else if (keyCode==KeyEvent.VK_BACK_SPACE) {
			stroke = null; // Not necessary; sanity check.
			setText(null);
		}
	}


	public void setKeyStroke(KeyStroke ks) {
		stroke = ks;
		setText(RTextUtilities.getPrettyStringFor(stroke));
	}


	private class FocusHandler extends FocusAdapter {

		public void focusGained(FocusEvent e) {
			selectAll();
		}

	}


}