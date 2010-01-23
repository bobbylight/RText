/*
 * 01/21/2010
 *
 * ToolDockableWindow.java - Dockable window that acts as a console for tool
 * output.
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
package org.fife.rtext.plugins.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ResourceBundle;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.fife.io.ProcessRunnerOutputListener;
import org.fife.ui.RScrollPane;
import org.fife.ui.dockablewindows.DockableWindow;


/**
 * The dockable window containing external tool output.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ToolDockableWindow extends DockableWindow
								implements ProcessRunnerOutputListener {

	private static final String MSG = "org.fife.rtext.plugins.tools.DockableWindow";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);

	private JTextPane textArea;


	/**
	 * Constructor.
	 */
	public ToolDockableWindow(ToolPlugin plugin) {

		super(new BorderLayout());
		setIcon(plugin.getPluginIcon());

		setDockableWindowName(msg.getString("Window.Name"));
		setActive(true);
		setPosition(BOTTOM);

		textArea = createTextArea();
		RScrollPane sp = new RScrollPane(textArea);
		add(sp);

	}


	/**
	 * Creates the text area, with default styles for stdout and stderr text.
	 */
	private JTextPane createTextArea() {

		JTextPane textArea = new JTextPane();

		Style stdout = textArea.addStyle("stdout", null);
		StyleConstants.setForeground(stdout, Color.blue);

		Style stderr = textArea.addStyle("stderr", null);
		StyleConstants.setForeground(stderr, Color.red);

		return textArea;

	}


	public void outputWritten(String output, boolean stdout) {

		Style style = textArea.getStyle(stdout ? "stdout" : "stderr");

		// The user can move the caret and type (stdin) so always append
		// to the end of the document.
		StyledDocument doc = (StyledDocument)textArea.getDocument();
		int end = doc.getLength();
		try {
			doc.insertString(end, output + "\n", null);
			doc.setLogicalStyle(doc.getLength()-1, style);
System.out.println("Jigga man");
		} catch (BadLocationException ble) {
			ble.printStackTrace();
		}
		textArea.setCaretPosition(doc.getLength());
	}


}