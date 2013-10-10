/*
 * 11/14/2003
 *
 * AboutDialog.java - Dialog that displays the program information for RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.*;

import org.fife.ui.RScrollPane;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.Plugin;


/**
 * The dialog that displays the program information about <code>RText</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class AboutDialog extends org.fife.ui.AboutDialog {

	private static final String MSG = "org.fife.rtext.AboutDialog";


	/**
	 * Creates a new <code>AboutDialog</code>.
	 *
	 * @param rtext The owner of this dialog.
	 */
	public AboutDialog(final RText rtext) {

		// Let it be known who the owner of this dialog is.
		super(rtext, rtext.getResourceBundle().getString("AboutDialogTitle"));

		ResourceBundle msg = ResourceBundle.getBundle(MSG);
		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());


		// Add a panel containing information about installed plugins.
		JPanel temp = UIUtil.newTabbedPanePanel(new BorderLayout());
		temp.setBorder(UIUtil.getEmpty5Border());
		JPanel panel = UIUtil.newTabbedPanePanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(new JLabel(msg.getString("Static.TableDescription")));
		panel.add(Box.createHorizontalGlue());
		temp.add(panel, BorderLayout.NORTH);
		JTable pluginTable = new JTable(createTableData(rtext),
					new String[] { msg.getString("Column.Plugin"),
								msg.getString("Column.Version"),
								msg.getString("Column.Author") }) {
			@Override
			public Dimension getPreferredScrollableViewportSize() {
				return new Dimension(50, 50); //Will be bigger.
			};
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
			/**
			 * Overridden to ensure the table completely fills the JViewport
			 * it is sitting in.  Note in Java 6 this could be taken care of
			 * by the method JTable#setFillsViewportHeight(boolean).
			 * 1.5: Change me to method call instead.
			 */
			@Override
			public boolean getScrollableTracksViewportHeight() {
				Component parent = getParent();
				return parent instanceof JViewport ?
					parent.getHeight()>getPreferredSize().height : false;
			}
		};
		UIUtil.fixJTableRendererOrientations(pluginTable);
		temp.add(new RScrollPane(pluginTable));
		temp.applyComponentOrientation(orientation);
		addPanel(msg.getString("Tab.Plugins"), temp);

		// Add a panel for libraries used by RText.
		addPanel(msg.getString("Tab.Libraries"), createLibrariesPanel(msg));

		pack();

	}


	@Override
	protected JPanel createAboutApplicationPanel() {

		// Create the picture.
		JPanel temp = UIUtil.newTabbedPanePanel(new BorderLayout());
		temp.setBorder(UIUtil.getEmpty5Border());
		ClassLoader cl = this.getClass().getClassLoader();
		ResourceBundle msg = ResourceBundle.getBundle(MSG);
		URL imageURL = cl.getResource("org/fife/rtext/graphics/" +
									msg.getString("Splash.Image"));
		ImageIcon icon = new ImageIcon(imageURL);
		JLabel label = new JLabel(icon);
		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		temp.add(label, BorderLayout.CENTER);

		// Add a panel containing the rest of the stuff.
		JPanel panel = UIUtil.newTabbedPanePanel();
		RText rtext = (RText)getOwner();
		InfoPane editor = new InfoPane(rtext);
//W3C_LENGTH_UNITS ="JEditorPane.w3cLengthUnits" in 1.5, but we're 1.4-compatible.
//editor.putClientProperty("JEditorPane.w3cLengthUnits", Boolean.TRUE);
panel.add(editor);

		temp.add(panel, BorderLayout.SOUTH);
		return temp;

	}


	private static final void appendLibrary(StringBuilder sb, String name,
			String url, String desc) {
		sb.append("<tr><td><b>").append(name).append("</b></td>");
		sb.append("<td><a href=\"").append(url).append("\">");
		sb.append(url).append("</a></td></tr>");
		sb.append("<tr><td colspan=\"2\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\u2022 ");
		sb.append(desc).append("</td></tr>");
		// Add an empty row, just for spacing.
		sb.append("<tr><td>&nbsp;</td><td>&nbsp;</td></tr>");
	}


	/**
	 * Creates a panel describing the libraries this application uses.
	 *
	 * @param msg The resource bundle.
	 * @return The panel.
	 */
	private static final JPanel createLibrariesPanel(ResourceBundle msg) {

		JPanel panel = UIUtil.newTabbedPanePanel(new BorderLayout());
		panel.setBorder(UIUtil.getEmpty5Border());

		StringBuilder sb = new StringBuilder("<html><table>");
		appendLibrary(sb, "RSyntaxTextArea:",
				"http://fifesoft.com/rsyntaxtextarea",
				msg.getString("Desc.RSyntaxTextArea"));
		appendLibrary(sb, "JTidy:", "http://jtidy.sourceforge.net/",
				msg.getString("Desc.JTidy"));
		appendLibrary(sb, "Jazzy:", "http://jazzy.sourceforge.net/",
				msg.getString("Desc.Jazzy"));
		appendLibrary(sb, "JGoodies:", "http://jgoodies.com",
				msg.getString("Desc.JGoodies"));
		appendLibrary(sb, "Insubstantial:",
				"https://github.com/Insubstantial/insubstantial",
				msg.getString("Desc.Substance"));
		appendLibrary(sb, "Groovy:",
				"http://groovy.codehaus.org/",
				msg.getString("Desc.Groovy"));
		sb.append("</table>");

		final SelectableLabel label = new SelectableLabel(sb.toString());
		label.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (HyperlinkEvent.EventType.ACTIVATED==e.getEventType()) {
					if (!UIUtil.browse(e.getDescription())) {
						UIManager.getLookAndFeel().provideErrorFeedback(label);
					}
				}
			}
		});
		panel.add(label);

		return panel;

	}


	/**
	 * Returns the array of strings for the "Plugins" tab on the About dialog.
	 *
	 * @param app The GUI Application containing plugins.
	 * @return The contents for the "Plugins" About tab.
	 */
	private static final String[][] createTableData(
			AbstractPluggableGUIApplication app) {

		Plugin[] plugins = app.getPlugins(); // Guaranteed non-null.
		int count = plugins.length;

		if (count>0) {
			String[][] strings = new String[count][3];
			for (int i=0; i<count; i++) {
				strings[i][0] = plugins[i].getPluginName();
				strings[i][1] = plugins[i].getPluginVersion();
				strings[i][2] = plugins[i].getPluginAuthor();
			}
			return strings;
		}

		// Shouldn't happen unless they remove standard plugin jars.
		ResourceBundle msg = ResourceBundle.getBundle(MSG);
		String text = msg.getString("Message.NoPluginsInstalled");
		String[][] strings = { { text, "", "" } };
		return strings;

	}


	private static class InfoPane extends SelectableLabel
									implements HyperlinkListener {

		private RText rtext;

		public InfoPane(RText rtext) {
			this.rtext = rtext;
			setText(getContentText());
			addHyperlinkListener(this);
		}

		public String getContentText() {
			String version = rtext==null ? "firstTime" :
											rtext.getVersionString();
			String text = "<html><body><center>" +
				"Version " + version + "<br>" +
				"Copyright (c) 2013 Robert Futrell<br>" +
				"<a href=\"http://rtext.fifesoft.com\">http://rtext.fifesoft.com</a>" +
				"</font></center></body></html>";
			return text;
		}

		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
				UIUtil.browse(e.getURL().toString());
			}
		}

		@Override
		public void updateUI() {
			super.updateUI();
			// Label font has been updated so we must update our HTML.
			setText(getContentText());
		}

	}


}