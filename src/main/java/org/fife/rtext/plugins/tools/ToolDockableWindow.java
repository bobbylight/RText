/*
 * 01/21/2010
 *
 * ToolDockableWindow.java - Dockable window that acts as a console for tool
 * output.
 * Copyright (C) 2010 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.fife.io.ProcessRunnerOutputListener;
import org.fife.ui.UIUtil;
import org.fife.ui.app.console.AbstractConsoleTextArea;
import org.fife.ui.RScrollPane;
import org.fife.ui.WebLookAndFeelUtils;
import org.fife.ui.dockablewindows.DockableWindow;


/**
 * The dockable window containing external tool output.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ToolDockableWindow extends DockableWindow
								implements ProcessRunnerOutputListener {

	private static final String MSG_BUNDLE = "org.fife.rtext.plugins.tools.DockableWindow";
	private static final ResourceBundle MSG = ResourceBundle.getBundle(MSG_BUNDLE);

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
	private final OutputTextPane textArea;

	private final JToolBar toolbar;

	/**
	 * Used by toolbar button to stop the currently running tool.
	 */
	private final StopAction stopAction;


	/**
	 * Constructor.
	 *
	 * @param plugin The tool plugin.
	 */
	public ToolDockableWindow(ToolPlugin plugin) {

		super(new BorderLayout());
		setIcon(plugin.getPluginIcon());
		setDockableWindowName(MSG.getString("Window.Name"));

		// Set via preferences
		//setActive(true);
		//setPosition(BOTTOM);

		textArea = new OutputTextPane(plugin);
		setPrimaryComponent(textArea);
		RScrollPane sp = new RScrollPane(textArea);
		UIUtil.removeTabbedPaneFocusTraversalKeyBindings(sp);
		add(sp);

		// Create a toolbar.
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(Box.createHorizontalGlue());

		stopAction = new StopAction(plugin, ToolPlugin.MSG);
		JButton b = new JButton(stopAction);
		b.setText(null);
		toolbar.add(b);
		WebLookAndFeelUtils.fixToolbar(toolbar);
		add(toolbar, BorderLayout.NORTH);

		ComponentOrientation o = ComponentOrientation.getOrientation(getLocale());
		applyComponentOrientation(o);

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

		SwingUtilities.invokeLater(() -> textArea.setCaretPosition(doc.getLength()));

	}


	/**
	 * Returns the currently running tool, if any.  This method should only be
	 * called on the EDT.
	 *
	 * @return The currently running tool, or <code>null</code> if a tool
	 *         isn't running.
	 */
	public Tool getActiveTool() {
		return tool;
	}


	/**
	 * Returns the color used for a given type of text in the consoles.
	 *
	 * @param style The style; e.g. {@link AbstractConsoleTextArea#STYLE_STDOUT}.
	 * @return The color, or <code>null</code> if the system default color
	 *         is being used.
	 * @see #setForeground(String, Color)
	 */
	public Color getForeground(String style) {
		Color c = null;
		Style s = textArea.getStyle(style);
		if (s!=null) {
			c = StyleConstants.getForeground(s);
		}
		return c;
	}


	/**
	 * Returns whether a special style is used for a given type of text in
	 * the consoles.
	 *
	 * @param style The style of text.
	 * @return Whether a special style is used.
	 */
	public boolean isStyleUsed(String style) {
		return textArea.getStyle(style).isDefined(StyleConstants.Foreground);
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


	@Override
	public void outputWritten(Process p, String output, boolean stdout) {
		Style style = textArea.getStyle(stdout ?
				OutputTextPane.STYLE_STDOUT : OutputTextPane.STYLE_STDERR);
		appendWithStyle(output, style); // thread safe Swing calls
	}


	@Override
	public void processCompleted(final Process p, final int rc,
								final Throwable e) {

		// Note that this isn't called on the EDT
		SwingUtilities.invokeLater(() -> {

			if (e==null) {
				float time = (System.currentTimeMillis()-startTime)/1000f;
				String title = MSG.getString("Window.Title.CompletedTool");
				title = MessageFormat.format(title,
					tool.getName(), Integer.toString(rc), Float.toString(time));
				setDockableWindowTitle(title);
			}
			else if (e instanceof InterruptedException) { // User killed
				String title = MSG.getString("Window.Title.ProcessTerminated");
				title = MessageFormat.format(title, tool.getName());
				setDockableWindowTitle(title);
				String text = MSG.getString("Window.ProcessTerminated");
				appendWithStyle(text,
						textArea.getStyle(OutputTextPane.STYLE_EXCEPTION));
			}
			else {
				String title = MSG.getString("Window.Title.ToolError");
				title = MessageFormat.format(title, tool.getName());
				setDockableWindowTitle(title);
				outputStackTrace(e);
			}

			stopAction.setEnabled(false);
			tool = null;

		});

	}


	/**
	 * Changes all consoles to use the default colors for the current
	 * application theme.
	 */
	public void restoreDefaultColors() {
		textArea.restoreDefaultColors();
	}


	/**
	 * Sets the color used for a given type of text in the consoles.
	 *
	 * @param style The style; e.g. {@link AbstractConsoleTextArea#STYLE_STDOUT}.
	 * @param fg The new foreground color to use, or <code>null</code> to
	 *        use the system default foreground color.
	 * @see #getForeground(String)
	 */
	public void setForeground(String style, Color fg) {
		Style s = textArea.getStyle(style);
		if (s!=null) {
			if (fg!=null) {
				StyleConstants.setForeground(s, fg);
			}
			else {
				s.removeAttribute(StyleConstants.Foreground);
			}
		}
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
			String title = MSG.getString("ErrorDialog.Title");
			String message = MSG.getString("ErrorDialog.ToolAlreadyRunning");
			JOptionPane.showMessageDialog(this, message, title,
											JOptionPane.ERROR_MESSAGE);
			return false;
		}
		this.tool = tool;
		startTime = System.currentTimeMillis();
		String title = MSG.getString("Window.Title.StartingTool");
		title = MessageFormat.format(title, tool.getName(),
			new SimpleDateFormat().format(new Date()));
		setDockableWindowTitle(title);
		textArea.setText(null);
		stopAction.setEnabled(true);
		return true;
	}


	@Override
	public void updateUI() {
		super.updateUI();
		if (toolbar!=null) {
			WebLookAndFeelUtils.fixToolbar(toolbar);
		}
	}


}
