/*
 * 11/14/2003
 *
 * AboutDialog.java - Dialog that displays the program information for RText.
 * Copyright (C) 2003 Robert Futrell
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
package org.fife.rtext;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.*;

import org.fife.ui.RScrollPane;
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
		JPanel temp = UIUtil.createTabbedPanePanel();
		temp.setLayout(new BorderLayout());
		temp.setBorder(UIUtil.getEmpty5Border());
		JPanel panel = UIUtil.createTabbedPanePanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(new JLabel(msg.getString("Static.TableDescription")));
		panel.add(Box.createHorizontalGlue());
		temp.add(panel, BorderLayout.NORTH);
		JTable pluginTable = new JTable(createTableData(rtext),
					new String[] { msg.getString("Column.Plugin"),
								msg.getString("Column.Version"),
								msg.getString("Column.Author") }) {
				public Dimension getPreferredScrollableViewportSize() {
					return new Dimension(50, 50); //Will be bigger.
				};
				public boolean isCellEditable(int row, int column) {
					return false;
				}
		};
		UIUtil.fixJTableRendererOrientations(pluginTable);
		temp.add(new RScrollPane(pluginTable));
		temp.applyComponentOrientation(orientation);
		addPanel(msg.getString("Tab.Plugins"), temp);

		pack();

	}


	protected JPanel createAboutApplicationPanel() {

		// Create the picture.
		JPanel temp = UIUtil.createTabbedPanePanel();
		temp.setLayout(new BorderLayout());
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
		JPanel panel = UIUtil.createTabbedPanePanel();
		RText rtext = (RText)getOwner();
		InfoPane editor = new InfoPane(rtext);
//W3C_LENGTH_UNITS ="JEditorPane.w3cLengthUnits" in 1.5, but we're 1.4-compatible.
//editor.putClientProperty("JEditorPane.w3cLengthUnits", Boolean.TRUE);
panel.add(editor);

		temp.add(panel, BorderLayout.SOUTH);
		return temp;

	}


	/**
	 * Returns the array of strings for the "Plugins" tab on the About dialog.
	 *
	 * @param app The GUI Application containing plugins.
	 * @return The contents for the "Plugins" About tab.
	 */
	private String[][] createTableData(AbstractPluggableGUIApplication app) {

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


	private static class InfoPane extends JEditorPane
									implements HyperlinkListener {

		private RText rtext;

		public InfoPane(RText rtext) {
			setEditable(false);
			setContentType("text/html");
			this.rtext = rtext;
			setText(getContentText());
			addHyperlinkListener(this);
		}

		public String getContentText() {
			Font font = UIManager.getFont("Label.font");
			String fontName = font.getFamily();
			int size = font.getSize();
			String version = rtext==null ? "firstTime" :
							rtext.getVersionString();
			String text = "<html><body><center>" +
							"<font style=\"font-family: " + fontName +
							",verdana,arial,helvetica; font-size: "
							+ size + "pt; \">" +
				"Version " + version + "<br>" +
				"Copyright (c) 2003-2009 Robert Futrell<br>" +
				"<a href=\"http://rtext.fifesoft.com\">http://rtext.fifesoft.com</a>" +
				"</font></center></body></html>";
			return text;
		}

		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
				UIUtil.browse(e.getURL().toString());
			}
		}

		public void updateUI() {
			super.updateUI();
			// Override the values for these properties in the new UI.
			setBorder(null);
			setOpaque(false);
			setBackground(new Color(0, 0, 0, 0)); // Needed for Nimbus
			// Label font has been updated so we must update our HTML.
			setText(getContentText());
		}

	}


}