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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
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

	/**
	 * The tool currently running.  This should only be manipulated on the
	 * EDT.
	 */
	private Tool tool;

	/**
	 * The time the last tool was started, in milliseconds.
	 */
	private long startTime;

	/**
	 * Where the output of the tool goes.
	 */
	private OutputTextPane textArea;


	/**
	 * Constructor.
	 */
	public ToolDockableWindow(ToolPlugin plugin) {

		super(new BorderLayout());
		setIcon(plugin.getPluginIcon());

		setDockableWindowName(msg.getString("Window.Name"));
		setActive(true);
		setPosition(BOTTOM);

		textArea = new OutputTextPane();
		RScrollPane sp = new RScrollPane(textArea);
		add(sp);

	}


	/**
	 * Appends text to the output text component with a given style.
	 *
	 * @param text The text to append.
	 * @param style The style to apply to the text.
	 */
	private void appendWithStyle(String text, Style style) {

		// The user can move the caret and type (stdin) so always append
		// to the end of the document.
		final StyledDocument doc = (StyledDocument)textArea.getDocument();
		int end = doc.getLength();
		try {
			// Thread safe since we're using an AbstractDocument
			doc.insertString(end, text + "\n", style);
			//doc.setLogicalStyle(doc.getLength()-1, style);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textArea.setCaretPosition(doc.getLength());
			}
		});

	}


	/**
	 * Prints an exception to the output text component.  This is called
	 * when an error occurs trying to launch or run a process.
	 *
	 * @param e The throwable that occurred.
	 */
	private void outputStackTrace(Throwable e) {
		Style style = textArea.getStyle(OutputTextPane.STYLE_EXCEPTION);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.close();
		appendWithStyle(sw.toString(), style);
	}


	/**
	 * {@inheritDoc}
	 */
	public void outputWritten(Process p, String output, boolean stdout) {
		Style style = textArea.getStyle(stdout ?
				OutputTextPane.STYLE_STDOUT : OutputTextPane.STYLE_STDERR);
		appendWithStyle(output, style); // thread safe Swing calls
	}


	/**
	 * {@inheritDoc}
	 */
	public void processCompleted(final Process p, final int rc,
								final Throwable e) {

		// Note that this isn't called on the EDT
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				if (e==null) {
					float time = (System.currentTimeMillis()-startTime)/1000f;
					String title = msg.getString("Window.Title.CompletedTool");
					title = MessageFormat.format(title,
						new Object[] { tool.getName(),
							Integer.toString(rc), Float.toString(time) });
					setDockableWindowTitle(title);
				}
				else {
					String title = msg.getString("Window.Title.ToolError");
					title = MessageFormat.format(title,
						new Object[] { tool.getName() });
					setDockableWindowTitle(title);
					outputStackTrace(e);
				}

				tool = null;

			}
		});

	}


	/**
	 * Called just before a tool is launched.
	 *
	 * @param tool The tool being launched.
	 * @return Whether the tool should be launched.  This will return
	 *         <code>false</code> if another tool is currently running.
	 */
	public boolean startingTool(Tool tool) {
		if (this.tool!=null) {
			String title = msg.getString("ErrorDialog.Title");
			String message = msg.getString("ErrorDialog.ToolAlreadyRunning");
			JOptionPane.showMessageDialog(this, message, title,
											JOptionPane.ERROR_MESSAGE);
			return false;
		}
		this.tool = tool;
		startTime = System.currentTimeMillis();
		String title = msg.getString("Window.Title.StartingTool");
		title = MessageFormat.format(title, new Object[] { tool.getName(),
								new SimpleDateFormat().format(new Date()) });
		setDockableWindowTitle(title);
		textArea.setText(null);
		return true;
	}


}