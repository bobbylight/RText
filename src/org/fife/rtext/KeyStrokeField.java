/*
 * 07/23/2011
 *
 * KeyStrokeField.java - A text field that lets you enter a keyboard shortcut.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.fife.ui.UIUtil;



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
		setText(UIUtil.getPrettyStringFor(stroke));
	}


	private class FocusHandler extends FocusAdapter {

		public void focusGained(FocusEvent e) {
			selectAll();
		}

	}


}