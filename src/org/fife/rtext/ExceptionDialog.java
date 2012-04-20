/*
 * 03/22/2005
 *
 * ExceptionDialog.java - A dialog for displaying program Exceptions to the
 * user.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.text.JTextComponent;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;


/**
 * The dialog displayed to the user when an <code>Exception</code> is
 * caught.  This class is here so we can syntax highlight the exception to
 * make things look a little nicer.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class ExceptionDialog extends org.fife.ui.app.ExceptionDialog {


	/**
	 * Constructor.
	 *
	 * @param owner The parent window.
	 * @param t The exception that was thrown.
	 */
	public ExceptionDialog(Frame owner, Throwable t) {
		super(owner, t);
	}


	/**
	 * Constructor.
	 *
	 * @param owner The parent dialog.
	 * @param t The exception that was thrown.
	 */
	public ExceptionDialog(Dialog owner, Throwable t) {
		super(owner, t);
	}


	/**
	 * Returns the text component to use to display the exception.  This
	 * method is overridden to return a text area with a syntax higlighted
	 * exception instead of a regular, black-and-white one.
	 *
	 * @return The text component.
	 */
	protected JTextComponent createTextComponent() {

		// We first make sure we're running on a Sun JVM.  This is because
		// the JRE spec does not specify the format of a stack trace, so we
		// cannot be sure it'll always look the way we expect.  I'm paranoid
		// that it'll be slightly different for different vendors (such as
		// IBM), so we'll only syntax highlight stack traces if we're positive
		// this is a Sun JRE.

		String vendor = System.getProperty("java.vm.vendor");
		if (vendor!=null && vendor.toLowerCase().indexOf("sun")>-1) {
			RSyntaxTextArea textArea = new RSyntaxTextArea() {
				private Dimension psv = new Dimension(500, 300);
				public Dimension getPreferredScrollableViewportSize() {
					return psv;
				}
			};
			textArea.restoreDefaultSyntaxScheme();
			//((RSyntaxDocument)textArea.getDocument()).
			//			setSyntaxStyle(new JavaExceptionTokenMaker());
			return textArea;
		}

		// If we're not positive this is a Sun JVM, return a standard text
		// area.
		return super.createTextComponent();

	}


}