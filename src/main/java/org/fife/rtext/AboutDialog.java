package org.fife.rtext;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.FontUIResource;

import org.fife.ui.*;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextfilechooser.Utilities;

class AboutDialog extends EscapableDialog {

	private static final long serialVersionUID = 1L;

	private RText parent;
	private Listener listener;
	private SelectableLabel memoryField;

	private static final String MSG = "org.fife.rtext.AboutDialog";


	AboutDialog(RText parent) {

		super(parent);
		this.parent = parent;
		listener = new Listener();
		//ResourceBundle msg = parent.getResourceBundle();
		ComponentOrientation o = parent.getComponentOrientation();

		ResourceBundle msg = ResourceBundle.getBundle(MSG);

		JPanel cp = new ResizableFrameContentPane(new BorderLayout());

		Box box = Box.createVerticalBox();

		JPanel top = new JPanel(new BorderLayout(15, 0));
		top.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		Color topBG = UIManager.getColor("TextField.background");
		top.setOpaque(true);
		top.setBackground(topBG);
		top.setBorder(new TopBorder());

		String imageName = "/org/fife/rtext/graphics/app_icons/seagull-" +
			(UIUtil.isLightForeground(new JLabel().getForeground()) ? "dark.svg" : "light.svg");

		try (InputStream in = getClass().getResourceAsStream(imageName)) {
			Image image = ImageTranscodingUtil.rasterize(imageName, in, 64, 64);
			JLabel linkLabel = new JLabel(new ImageIcon(image));
			top.add(linkLabel, BorderLayout.LINE_START);
		} catch (IOException ioe) {
			parent.displayException(ioe);
		}

		JPanel topText = new JPanel(new BorderLayout());
		topText.setOpaque(false);
		top.add(topText);

		// Don't use a Box, as some JVM's won't have the resulting component
		// honor its opaque property.
		JPanel box2 = new JPanel();
		box2.setOpaque(false);
		box2.setLayout(new BoxLayout(box2, BoxLayout.Y_AXIS));
		topText.add(box2, BorderLayout.NORTH);

		JLabel label = new JLabel(msg.getString("About.Header"));
		label.setOpaque(true);
		label.setBackground(topBG);
		Font labelFont = label.getFont();
		label.setFont(labelFont.deriveFont(Font.BOLD, 20));
		addLeftAligned(label, box2);
		box2.add(Box.createVerticalStrut(5));

		Date buildDate = parent.getBuildDate();
		String dateStr = buildDate == null ? "unknown" :
			DateFormat.getDateTimeInstance().format(buildDate);
		String desc = getString(msg, "About.MainDesc", parent.getVersionString(), dateStr);
		SelectableLabel textArea = new SelectableLabel(desc);
		textArea.addHyperlinkListener(listener);
		box2.add(textArea);
		box2.add(Box.createVerticalGlue());

		box.add(top);
		box.add(Box.createVerticalStrut(5));

		JPanel temp = new JPanel(new SpringLayout());
		SelectableLabel javaField = new SelectableLabel(System.getProperty("java.home"));
		memoryField = new SelectableLabel();
		JLabel javaLabel = UIUtil.newLabel(msg, "About.JavaHome", javaField);
		JLabel memoryLabel = UIUtil.newLabel(msg, "About.Memory", memoryField);

		if (o.isLeftToRight()) {
			temp.add(javaLabel);        temp.add(javaField);
			temp.add(memoryLabel);      temp.add(memoryField);
		}
		else {
			temp.add(javaField);        temp.add(javaLabel);
			temp.add(memoryField);      temp.add(memoryLabel);
		}
		UIUtil.makeSpringCompactGrid(temp, 2, 2, 5,5, 15,5);
		box.add(temp);

		box.add(Box.createVerticalGlue());

		cp.add(box, BorderLayout.NORTH);

		JButton okButton = UIUtil.newButton(msg, "Button.OK");
		okButton.addActionListener(e -> setVisible(false));
		JPanel buttons = (JPanel)UIUtil.createButtonFooter(okButton);
		buttons.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(5, 8, 5, 8),
			buttons.getBorder()));
		cp.add(buttons, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(okButton);
		setTitle(parent.getString("AboutDialogTitle"));
		setContentPane(cp);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		pack();

	}


	private JPanel addLeftAligned(Component toAdd, Container addTo) {
		JPanel temp = new JPanel(new BorderLayout());
		temp.setOpaque(false); // For ones on white background.
		temp.add(toAdd, BorderLayout.LINE_START);
		addTo.add(temp);
		return temp;
	}


	private String getMemoryInfo() {
		long curMemory = Runtime.getRuntime().totalMemory() -
			Runtime.getRuntime().freeMemory();
		return Utilities.getFileSizeStringFor(curMemory, false);
	}


	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.width = Math.max(d.width, 600); // Looks better with a little width.
		return d;
	}


	private static String getString(ResourceBundle msg, String key, Object... params) {
		String value = msg.getString(key);
		return params.length > 0 ? MessageFormat.format(value, params) : value;
	}

	public void setVisible(boolean visible) {
		if (visible) {
			memoryField.setText(getMemoryInfo());
		}
		super.setVisible(visible);
	}


	/**
	 * Dialog showing used libraries and credits.
	 */
	private class CreditsDialog extends EscapableDialog {

		private static final long serialVersionUID = 1L;

		CreditsDialog() {

			super(AboutDialog.this);
			JPanel cp = new ResizableFrameContentPane(new BorderLayout());
			cp.setBorder(UIUtil.getEmpty5Border());
			ResourceBundle msg = ResourceBundle.getBundle(MSG);

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
			appendLibrary(sb, "Substance:",
				"https://github.com/kirill-grouchnikov/radiance/",
				msg.getString("Desc.Substance"));
			appendLibrary(sb, "Groovy:",
				"http://groovy.codehaus.org/",
				msg.getString("Desc.Groovy"));
			sb.append("</table>");

			SelectableLabel label = new SelectableLabel(sb.toString());
			label.addHyperlinkListener(listener);
			cp.add(label);

			JButton okButton = UIUtil.newButton(msg, "Button.OK");
			okButton.addActionListener(e -> setVisible(false));
			Container buttons = UIUtil.createButtonFooter(okButton);
			cp.add(buttons, BorderLayout.SOUTH);

			setContentPane(cp);
			setTitle(msg.getString("Dialog.Credits.Title"));
			setModal(true);
			pack();
			setLocationRelativeTo(AboutDialog.this);

		}

		private void appendLibrary(StringBuilder sb, String name,
										  String url, String desc) {
			sb.append("<tr><td><b>").append(name).append("</b></td>");
			sb.append("<td><a href=\"").append(url).append("\">");
			sb.append(url).append("</a></td></tr>");
			sb.append("<tr><td colspan=\"2\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\u2022 ");
			sb.append(desc).append("</td></tr>");
			// Add an empty row, just for spacing.
			sb.append("<tr><td>&nbsp;</td><td>&nbsp;</td></tr>");
		}
	}


	/**
	 * Dialog showing the license for this application.
	 */
	private class LicenseDialog extends EscapableDialog {

		private static final long serialVersionUID = 1L;

		LicenseDialog() {

			super(AboutDialog.this);
			JPanel cp = new ResizableFrameContentPane(new BorderLayout());
			cp.setBorder(UIUtil.getEmpty5Border());
			ResourceBundle msg = ResourceBundle.getBundle(MSG);

			JTextArea textArea = new JTextArea(25, 80);
            Font font = RTextArea.getDefaultFont();
            if (font instanceof FontUIResource) { // Substance!  argh!!!
                font = new Font(font.getFamily(), font.getStyle(), font.getSize());
            }
            textArea.setFont(font);
			loadLicense(textArea);
			textArea.setEditable(false);
			RScrollPane sp = new RScrollPane(textArea);
			cp.add(sp);

			JButton okButton = UIUtil.newButton(msg, "Button.OK");
			okButton.addActionListener(e -> setVisible(false));
			Container buttons = UIUtil.createButtonFooter(okButton);
			cp.add(buttons, BorderLayout.SOUTH);

			setContentPane(cp);
			setTitle(msg.getString("Dialog.License.Title"));
			setModal(true);
			pack();
			setLocationRelativeTo(AboutDialog.this);

		}

		private void loadLicense(JTextArea textArea) {
			File file = new File(parent.getInstallLocation(), "License.txt");
			try {
				BufferedReader r = new BufferedReader(new FileReader(file));
				textArea.read(r, null);
				r.close();
			} catch (IOException ioe) {
				textArea.setText(ioe.getMessage());
			}
		}

	}


	/**
	 * Listens for events in this dialog.
	 */
	private class Listener implements HyperlinkListener {

		private void handleLocalLink(URL url) {

			String str = url.toString();
			String command = str.substring(str.lastIndexOf('/')+1);

			if ("libraries".equals(command)) {
				new CreditsDialog().setVisible(true);
			}
			else if ("license".equals(command)) {
				new LicenseDialog().setVisible(true);
			}

		}


		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
				URL url = e.getURL();
				if ("file".equals(url.getProtocol())) {
					handleLocalLink(url);
					return;
				}
				if (!UIUtil.browse(url.toString())) {
					UIManager.getLookAndFeel().provideErrorFeedback(AboutDialog.this);
				}
			}
		}

	}


	/**
	 * The border of the "top section" of the About dialog.
	 */
	private static class TopBorder extends AbstractBorder {

		private static final long serialVersionUID = 1L;

		public Insets getBorderInsets(Component c) {
			return getBorderInsets(c, new Insets(0, 0, 0, 0));
		}

		public Insets getBorderInsets(Component c, Insets insets) {
			insets.top = insets.left = insets.right = 5;
			insets.bottom = 6;
			return insets;
		}

		public void paintBorder(Component c, Graphics g, int x, int y,
								int width, int height) {
			Color color = UIManager.getColor("controlShadow");
			if (color==null) {
				color = SystemColor.controlShadow;
			}
			g.setColor(color);
			g.drawLine(x,y+height-1, x+width,y+height-1);
		}

	}

}
