/*
 * 03/22/2010
 *
 * TidyAction.java - Action that "tidies" source code.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tidy;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.swing.JOptionPane;

import org.fife.io.DocumentReader;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.StandardAction;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.w3c.tidy.Tidy;
import org.w3c.tidy.TidyMessage;
import org.w3c.tidy.TidyMessageListener;


/**
 * Action for tidying source code.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class TidyAction extends StandardAction implements TidyMessageListener {

	/**
	 * The tidying plugin.
	 */
	private Plugin plugin;

	/**
	 * Any output from the tidy operation.  This may contain warnings, errors,
	 * a summary of the operation, etc., depending on the parser.
	 */
	private StringBuffer output;

	/**
	 * The result of the tidy operation.  0 => OK, 1 => warnings, 2 => errors.
	 */
	private int result;

	/**
	 * Specifies that there were no issues formatting the source.
	 */
	private static final int RESULT_OK				= 0;

	/**
	 * Specifies that there were warnings formatting the source.
	 */
	private static final int RESULT_WARNINGS		= 1;

	/**
	 * Specifies that there were errors formatting the source.
	 */
	private static final int RESULT_ERRORS			= 2;


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 * @param plugin The tidying plugin.
	 */
	public TidyAction(AbstractPluggableGUIApplication app, Plugin plugin) {
		super(app, Plugin.msg, "Action.Tidy");
		this.plugin = plugin;
	}


	/**
	 * Callback for this action.
	 *
	 * @param e The event.
	 */
	public void actionPerformed(ActionEvent e) {

		RText app = (RText)getApplication();
		RTextEditorPane textArea = app.getMainView().getCurrentTextArea();
		String style = textArea.getSyntaxEditingStyle();
		boolean xml = SyntaxConstants.SYNTAX_STYLE_XML.equals(style);
		DocumentReader dr = new DocumentReader(textArea.getDocument());
		StringWriter sr = new StringWriter();

		result = RESULT_OK;
		output = new StringBuffer();
		int icon = JOptionPane.INFORMATION_MESSAGE;
		Tidy tidy = new Tidy();
		int spaces;

		if (xml) {
			XmlOptions opts = plugin.getXmlOptions();
			tidy.setXmlTags(true);
			tidy.setXmlOut(true);
			tidy.setXmlPi(opts.getAddXmlDeclaration());
			spaces = opts.getSpaceCount();
			tidy.setSpaces(spaces<0 ? 1 : spaces); // Convert to tabs later
		}

		else {
			HtmlOptions opts = plugin.getHtmlOptions();
			tidy.setDocType("omit"); // Don't add DOCTYPE
			tidy.setDropEmptyParas(opts.getDropEmptyParas());
			tidy.setHideEndTags(opts.getHideOptionalEndTags());
			tidy.setLogicalEmphasis(opts.getLogicalEmphasis());
			tidy.setMakeClean(opts.getMakeClean()); // Replace presentational tags
			spaces = opts.getSpaceCount();
			tidy.setSpaces(spaces<0 ? 1 : spaces); // Convert to tabs later
			tidy.setUpperCaseTags(opts.getUpperCaseTagNames());
			tidy.setUpperCaseAttrs(opts.getUpperCaseAttrNames());
			tidy.setWraplen(opts.getWrapLength());
		}

		// tidy sometimes returns "errors" without sending a
		// TidyMessage.Level.ERROR message, so as a "workaround" we'll force
		// output in that case.
		tidy.setForceOutput(true);
		tidy.setSmartIndent(true);
		tidy.setMessageListener(this);
		tidy.setTidyMark(false); // No meta tag saying "made by JTidy"
		tidy.parse(dr, sr);

		// If they want to indent with tabs, convert leading spaces
		String text = sr.toString();
		if (spaces==-1) {
			text = replaceLeadingSpacesWithTabs(text);
		}

		switch (result) {
			case RESULT_WARNINGS:
				icon = JOptionPane.WARNING_MESSAGE;
				// Fall through
			case RESULT_OK:
				textArea.beginAtomicEdit();
				try {
					textArea.setText(text);
				} finally { // Treat clear and set as 1 operation (!)
					textArea.endAtomicEdit();
				}
				break;
			case RESULT_ERRORS:
				icon = JOptionPane.ERROR_MESSAGE;
				break;
		}
		String title = Plugin.msg.getString("Dialog.Result.Title");
		JOptionPane.showMessageDialog(app, output.toString(), title, icon);

		try {
			sr.close();
			dr.close();
		} catch (IOException ioe) { // Never happens
			ioe.printStackTrace();
		}

	}


	/**
	 * Called when a message is received from the tidy parser.
	 *
	 * @param msg The message.
	 */
	public void messageReceived(TidyMessage msg) {

		// Always remember the "worst" thing that has happened.
		TidyMessage.Level level = msg.getLevel();
		if (level==TidyMessage.Level.ERROR) {
			result = RESULT_ERRORS;
		}
		else if (level==TidyMessage.Level.WARNING && result!=RESULT_ERRORS) {
			result = RESULT_WARNINGS;
		}
		output.append(msg.getMessage()).append('\n');

	}


	/**
	 * Returns a copy of a string where leading spaces for lines are replaced
	 * with leading tabs.
	 *
	 * @param text The text.
	 * @return The text, with leading spaces replaced with leading tabs.
	 */
	private String replaceLeadingSpacesWithTabs(String text) {

		StringBuffer sb = new StringBuffer();

		BufferedReader r = new BufferedReader(new StringReader(text));
		String line = null;

		try {
			while ((line=r.readLine())!=null) {
				int i = 0;
				while (i<line.length() && line.charAt(i)==' ') {
					sb.append('\t');
					i++;
				}
				sb.append(line.substring(i)).append('\n');
			}
			r.close();
		} catch (IOException ioe) { // Never happens; exceptions aren't thrown
			ioe.printStackTrace();
		}

		return sb.toString();

	}


}