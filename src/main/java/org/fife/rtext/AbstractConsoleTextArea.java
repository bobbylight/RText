/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import org.fife.ui.rtextarea.RTextArea;
import org.fife.util.SubstanceUtil;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * A base class for text areas that render input and output from a command line,
 * or a command line-like environment.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class AbstractConsoleTextArea extends JTextPane {

	public static final Color DEFAULT_PROMPT_FG		= new Color(0,192,0);

	public static final Color DEFAULT_STDOUT_FG		= Color.blue;

	public static final Color DEFAULT_STDERR_FG		= Color.red;

	public static final Color DEFAULT_EXCEPTION_FG	= new Color(111, 49, 152);

	public static final Color DEFAULT_DARK_PROMPT_FG	= new Color(0x30, 0xff, 0x2f);

	public static final Color DEFAULT_DARK_STDOUT_FG	= new Color(0, 255, 255);

	public static final Color DEFAULT_DARK_STDERR_FG	= new Color(0xff, 0x80, 0x80);

	public static final Color DEFAULT_DARK_EXCEPTION_FG = new Color(0xa0649a);

	public static final String STYLE_PROMPT			= "prompt";
	public static final String STYLE_STDIN			= "stdin";
	public static final String STYLE_STDOUT			= "stdout";
	public static final String STYLE_STDERR			= "stderr";
	public static final String STYLE_EXCEPTION		= "exception";

	private JPopupMenu popup;


	/**
	 * Creates the popup menu for this text area.
	 *
	 * @return The popup menu.
	 */
	protected abstract JPopupMenu createPopupMenu();


	/**
	 * Installs the styles used by this text component.
	 *
	 * @param checkForSubstance Whether to work around a Substance oddity.
	 */
	protected void installDefaultStyles(boolean checkForSubstance) {

		Font font = RTextArea.getDefaultFont();
		if (!SubstanceUtil.isSubstanceInstalled()) {
			// If we do this with a SubstanceLookAndFeel installed, we go into
			// an infinite loop of updateUI()'s called (in calls to
			// SwingUtilities.invokeLater()).  For some reason, Substance has
			// to update JTextPaneUI's whenever the font changes.  Sigh...
			setFont(font);
		}

		restoreDefaultColors();
		setTabSize(4); // Do last

	}


	/**
	 * Changes all consoles to use the default colors for the current
	 * application theme.
	 */
	public void restoreDefaultColors() {

		Font font = RTextArea.getDefaultFont();
		boolean isDark = RTextUtilities.isDarkLookAndFeel();

		Style defaultStyle = getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(defaultStyle, font.getFamily());
		StyleConstants.setFontSize(defaultStyle, font.getSize());

		Style prompt = addStyle(STYLE_PROMPT, defaultStyle);
		Color promptColor = isDark ? DEFAULT_DARK_PROMPT_FG : DEFAULT_PROMPT_FG;
		StyleConstants.setForeground(prompt, promptColor);

		/*Style stdin = */addStyle(STYLE_STDIN, defaultStyle);

		Style stdout = addStyle(STYLE_STDOUT, defaultStyle);
		Color stdoutColor = isDark ? DEFAULT_DARK_STDOUT_FG : DEFAULT_STDOUT_FG;
		StyleConstants.setForeground(stdout, stdoutColor);

		Style stderr = addStyle(STYLE_STDERR, defaultStyle);
		Color stderrColor = isDark ? DEFAULT_DARK_STDERR_FG : DEFAULT_STDERR_FG;
		StyleConstants.setForeground(stderr, stderrColor);

		Style exception = addStyle(STYLE_EXCEPTION, defaultStyle);
		Color exceptionColor = isDark ? DEFAULT_DARK_EXCEPTION_FG : DEFAULT_EXCEPTION_FG;
		StyleConstants.setForeground(exception, exceptionColor);

	}


	/**
	 * Sets the tab size in this text pane.
	 *
	 * @param tabSize The new tab size, in characters.
	 */
	private void setTabSize(int tabSize) {

		FontMetrics fm = getFontMetrics(getFont());
		int charWidth = fm.charWidth('m');
		int tabWidth = charWidth * tabSize;

		// NOTE: Array length is arbitrary, represents the maximum number of
		// tabs handled on a single line.
		TabStop[] tabs = new TabStop[50];
		for (int j=0; j<tabs.length; j++) {
			tabs[j] = new TabStop((j+1)*tabWidth);
		}

		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setTabSet(attributes, tabSet);

		int length = getDocument().getLength();
		getStyledDocument().setParagraphAttributes(0, length, attributes, true);

	}


	/**
	 * Displays this text area's popup menu.
	 *
	 * @param e The location at which to display the popup.
	 */
	protected void showPopupMenu(MouseEvent e) {
		if (popup==null) {
			popup = createPopupMenu();
		}
		popup.show(this, e.getX(), e.getY());
	}


	/**
	 * Overridden to also update the UI of the popup menu.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		installDefaultStyles(true);
		if (popup!=null) {
			SwingUtilities.updateComponentTreeUI(popup);
		}
	}
}
