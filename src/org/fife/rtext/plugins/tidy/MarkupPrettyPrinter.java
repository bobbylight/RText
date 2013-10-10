/*
 * 02/15/2013
 *
 * MarkupPrettyPrinter - Pretty-prints XML and HTML.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tidy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.fife.io.DocumentReader;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.w3c.tidy.Tidy;
import org.w3c.tidy.TidyMessage;
import org.w3c.tidy.TidyMessageListener;


/**
 * Pretty printer for HTML and XML.  Currently simply delegates to
 * <a href="http://jtidy.sourceforge.net">JTidy</a>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class MarkupPrettyPrinter implements PrettyPrinter, TidyMessageListener {

	private RText rtext;
	private Plugin plugin;
	private int result;
	private StringBuilder output;


	MarkupPrettyPrinter(RText rtext, Plugin plugin) {
		this.rtext = rtext;
		this.plugin = plugin;
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


	public PrettyPrintResult prettyPrint(String text) {

		RTextEditorPane textArea = rtext.getMainView().getCurrentTextArea();
		String style = textArea.getSyntaxEditingStyle();
		boolean xml = SyntaxConstants.SYNTAX_STYLE_XML.equals(style);
		DocumentReader dr = new DocumentReader(textArea.getDocument());
		StringWriter sr = new StringWriter();

		result = RESULT_OK;
		output = new StringBuilder();
		Tidy tidy = new Tidy();
		int spaces;

		if (xml) {
			XmlOptions opts = plugin.getXmlOptions();
			tidy.setXmlTags(true);
			tidy.setXmlOut(true);
			tidy.setXmlPi(opts.getAddXmlDeclaration());
			tidy.setEscapeCdata(false);
			tidy.setIndentCdata(false);
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
		text = sr.toString();
		if (spaces==-1) {
			text = replaceLeadingSpacesWithTabs(text);
		}

		try {
			sr.close();
			dr.close();
		} catch (IOException ioe) { // Never happens
			ioe.printStackTrace();
		}

		String message = output.toString();
		return new PrettyPrintResult(result, text, message);

	}


	/**
	 * Returns a copy of a string where leading spaces for lines are replaced
	 * with leading tabs.
	 *
	 * @param text The text.
	 * @return The text, with leading spaces replaced with leading tabs.
	 */
	private static final String replaceLeadingSpacesWithTabs(String text) {

		StringBuilder sb = new StringBuilder();

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