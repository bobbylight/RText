/*
 * 04/04/2012
 *
 * GoToMemberAction.java - Wraps the "Go to Member" action in some
 * LanguageSupports.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.fife.rtext.CurrentTextAreaEvent;
import org.fife.rtext.CurrentTextAreaListener;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.ui.app.StandardAction;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Action that opens the active language support's "Go to Member" window, if
 * one is available.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class GoToMemberAction extends StandardAction
			implements CurrentTextAreaListener {


	public GoToMemberAction(RText app) {
		super(app, Plugin.msg, "Action.GoToMember");
		app.getMainView().addCurrentTextAreaListener(this);
		refreshEnabledState();
	}


	public void actionPerformed(ActionEvent e) {

		RText rtext = (RText)getApplication();
		RTextEditorPane textArea = rtext.getMainView().getCurrentTextArea();
		int c = textArea.getToolkit().getMenuShortcutKeyMask();
		int shift = InputEvent.SHIFT_MASK;
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_O, c|shift);
		boolean success = false;

		InputMap im = textArea.getInputMap();
		Object key = im.get(ks);
		if (key!=null) {
			ActionMap am = textArea.getActionMap();
			Action a = am.get(key);
			if (a instanceof org.fife.rsta.ac.GoToMemberAction) {
				((org.fife.rsta.ac.GoToMemberAction)a).actionPerformed(e);
				success = true;
			}
		}

		// Shouldn't happen unless they've mapped a different action to
		// C+Shift+O.
		if (!success) {
			UIManager.getLookAndFeel().provideErrorFeedback(textArea);
		}

	}


	/**
	 * Listens for text area events.  If the active text area changes, or the
	 * active text area's syntax style changes, we re-evaluate whether this
	 * action should be active.
	 *
	 * @param e The event.
	 */
	public void currentTextAreaPropertyChanged(CurrentTextAreaEvent e) {
		if (e.getType()==CurrentTextAreaEvent.TEXT_AREA_CHANGED ||
				e.getType()==CurrentTextAreaEvent.SYNTAX_STYLE_CNANGED) {
			refreshEnabledState();
		}
	}


	/**
	 * Returns whether the language support for the specified language has
	 * installed Go To Member on C+Shift+O.  This must be kept in sync with
	 * the <code>RSTALanguageSupport</code> project.
	 *
	 * @param style The syntax style.
	 * @return Whether the action is available.
	 */
	private static final boolean getStyleSupportsGoToMember(String style) {
		return style.equals(SyntaxConstants.SYNTAX_STYLE_JAVA) ||
				style.equals(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT) ||
				style.equals(SyntaxConstants.SYNTAX_STYLE_XML);
	}


	/**
	 * Refreshes whether this action is enabled, based on the currently active
	 * text area and its state.
	 */
	private void refreshEnabledState() {
		boolean enabled = false;
		RText rtext = (RText)getApplication();
		RTextEditorPane textArea = rtext.getMainView().getCurrentTextArea();
		if (textArea!=null) {
			String style = textArea.getSyntaxEditingStyle();
			if (getStyleSupportsGoToMember(style)) {
				enabled = true;
			}
		}
		setEnabled(enabled);
	}


}