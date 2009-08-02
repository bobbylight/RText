/*
 * 12/08/2004
 *
 * TextFilePropertiesDialog.java - Dialog allowing you to view/edit a
 * text file's properties.
 * Copyright (C) 2004 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.ui.rsyntaxtextarea;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.fife.ui.EscapableDialog;
import org.fife.ui.RButton;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.SpecialValueComboBox;
import org.fife.ui.UIUtil;


/**
 * A dialog that displays the properties of an individual text file being
 * edited by a {@link org.fife.ui.rsyntaxtextarea.TextEditorPane}.  Some
 * properties can be modified directly from this dialog.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class TextFilePropertiesDialog extends EscapableDialog
								implements ActionListener {

	private static final long serialVersionUID = 1L;

	public static final String TERM_CR		= "\r";
	public static final String TERM_LF		= "\n";
	public static final String TERM_CRLF	= "\r\n";
	public static final String TERM_SYSTEM	= System.getProperty("line.separator");

	private SpecialValueComboBox terminatorCombo;
	private JComboBox encodingCombo;

	private RButton okButton;

	private TextEditorPane textArea;


	/**
	 * Constructor.
	 *
	 * @param parent The main application window.
	 * @param textArea The text area on which to report.
	 */
	public TextFilePropertiesDialog(Frame parent, TextEditorPane textArea) {

		super(parent);
		this.textArea = textArea;

		ComponentOrientation o = ComponentOrientation.
									getOrientation(getLocale());

		ResourceBundle msg = ResourceBundle.getBundle(
						"org.fife.ui.rsyntaxtextarea.TextFilePropertiesDialog");

		setTitle(msg.getString("Title") + textArea.getFileName());

		JPanel contentPane = new ResizableFrameContentPane(new BorderLayout());
		contentPane.setBorder(UIUtil.getEmpty5Border());

		// Where we actually add our content.
		JPanel content2 = new JPanel();
		content2.setLayout(new SpringLayout());
		contentPane.add(content2, BorderLayout.NORTH);

		JLabel filePathLabel = new JLabel(msg.getString("Path"));
		JTextField filePathField = new JTextField(30);
		filePathField.setText(textArea.getFileFullPath());
		filePathField.setEditable(false);
		filePathLabel.setLabelFor(filePathField);

		JLabel linesLabel = new JLabel(msg.getString("Lines"));
		JLabel linesCountLabel = new JLabel("" + textArea.getLineCount());

		JLabel charsLabel = new JLabel(msg.getString("Characters"));
		JLabel charsCountLabel = new JLabel("" +
								textArea.getDocument().getLength());

		JLabel terminatorLabel = UIUtil.createLabel(msg,
							"LineTerminator", "LineTerminatorMnemonic");
		terminatorCombo = new SpecialValueComboBox();
		UIUtil.fixComboOrientation(terminatorCombo);
		terminatorCombo.addSpecialItem(msg.getString("SysDef"), TERM_SYSTEM);
		terminatorCombo.addSpecialItem(msg.getString("CR"),     TERM_CR);
		terminatorCombo.addSpecialItem(msg.getString("LF"),     TERM_LF);
		terminatorCombo.addSpecialItem(msg.getString("CRLF"),   TERM_CRLF);
		terminatorCombo.setSelectedSpecialItem((String)textArea.
											getLineSeparator());
		terminatorCombo.setActionCommand("TerminatorComboBox");
		terminatorCombo.addActionListener(this);
		terminatorLabel.setLabelFor(terminatorCombo);

		JLabel encodingLabel = UIUtil.createLabel(msg,
								"Encoding", "EncodingMnemonic");
		encodingCombo = new JComboBox();
		UIUtil.fixComboOrientation(encodingCombo);

		// Populate the combo box with all available encodings.
		Map availcs = Charset.availableCharsets();
		Set keys = availcs.keySet();
		for (Iterator i=keys.iterator(); i.hasNext(); )
			encodingCombo.addItem(i.next());
		setEncoding(textArea.getEncoding());
		encodingCombo.setActionCommand("encodingCombo");
		encodingCombo.addActionListener(this);
		encodingLabel.setLabelFor(encodingCombo);

		JLabel sizeLabel = new JLabel(msg.getString("FileSize"));
		File file = new File(textArea.getFileFullPath());
		String size = "";
		if (file.exists() && !file.isDirectory()) {
			size = Long.toString(file.length());
		}
		JLabel sizeLabel2 = new JLabel(size);
		
		long temp = textArea.getLastSaveOrLoadTime();
		String modifiedString;
		if (temp<=0) { // 0 or -1, can be either
			modifiedString = "";
		}
		else {
			Date modifiedDate = new Date(temp);
			SimpleDateFormat rob = new SimpleDateFormat("hh:mm a  EEE, MMM d, yyyy");
			modifiedString = rob.format(modifiedDate);
		}
		JLabel modifiedLabel = new JLabel(msg.getString("LastModified"));
		JLabel modified = new JLabel(modifiedString);

		if (o.isLeftToRight()) {
			content2.add(filePathLabel);     content2.add(filePathField);
			content2.add(linesLabel);        content2.add(linesCountLabel);
			content2.add(charsLabel);        content2.add(charsCountLabel);
			content2.add(terminatorLabel);   content2.add(terminatorCombo);
			content2.add(encodingLabel);     content2.add(encodingCombo);
			content2.add(sizeLabel);         content2.add(sizeLabel2);
			content2.add(modifiedLabel);     content2.add(modified);
		}
		else {
			content2.add(filePathField);     content2.add(filePathLabel);
			content2.add(linesCountLabel);   content2.add(linesLabel);
			content2.add(charsCountLabel);   content2.add(charsLabel);
			content2.add(terminatorCombo);   content2.add(terminatorLabel);
			content2.add(encodingCombo);     content2.add(encodingLabel);
			content2.add(sizeLabel2);        content2.add(sizeLabel);
			content2.add(modified);          content2.add(modifiedLabel);
		}

		UIUtil.makeSpringCompactGrid(content2,
									7,2,		// rows,cols,
									0,0,		// initial-x, initial-y,
									5,5);	// x-spacing, y-spacing.

		// Make a panel for OK and cancel buttons.
		JPanel bottomPanel = new JPanel();
		JPanel buttonPanel = new JPanel(new GridLayout(1,2, 5,0));
		okButton = UIUtil.createRButton(msg, "OK", "OKMnemonic");
		okButton.setActionCommand("OKButton");
		okButton.addActionListener(this);
		okButton.setEnabled(false);
		buttonPanel.add(okButton);
		RButton cancelButton = UIUtil.createRButton(msg,
									"Cancel", "CancelMnemonic");
		cancelButton.setActionCommand("CancelButton");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		bottomPanel.add(buttonPanel);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);

		setContentPane(contentPane);
		setModal(true);
		applyComponentOrientation(o);
		pack();
		setLocationRelativeTo(parent);

	}


	/**
	 * Listens for actions in this dialog.
	 *
	 * @param e The action event.
	 */
	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		if (actionCommand.equals("TerminatorComboBox")) {
			okButton.setEnabled(true);
		}

		else if (actionCommand.equals("encodingCombo")) {
			okButton.setEnabled(true);
		}

		else if (actionCommand.equals("OKButton")) {
			String terminator = terminatorCombo.getSelectedSpecialItem();
			if (terminator!=null) {
				String old = (String)textArea.getLineSeparator();
				if (!terminator.equals(old)) {
					textArea.setLineSeparator(terminator);
				}
			}
			String encoding = (String)encodingCombo.getSelectedItem();
			if (encoding!=null) {
				textArea.setEncoding(encoding);
			}
			setVisible(false);
		}

		else if (actionCommand.equals("CancelButton")) {
			escapePressed();
		}

	}


	/**
	 * Sets the encoding selected by this dialog.
	 *
	 * @param encoding The desired encoding.  If this value is invalid or not
	 *        supported by this OS, <code>US-ASCII</code> is used.
	 * @see #getEncoding
	 */
	private void setEncoding(String encoding) {

		Charset cs1 = Charset.forName(encoding);

		int count = encodingCombo.getItemCount();
		for (int i=0; i<count; i++) {
			String item = (String)encodingCombo.getItemAt(i);
			Charset cs2 = Charset.forName(item);
			if (cs1.equals(cs2)) {
				encodingCombo.setSelectedIndex(i);
				return;
			}
		}

		// Encoding not found: select default.
		cs1 = Charset.forName("US-ASCII");
		for (int i=0; i<count; i++) {
			String item = (String)encodingCombo.getItemAt(i);
			Charset cs2 = Charset.forName(item);
			if (cs1.equals(cs2)) {
				encodingCombo.setSelectedIndex(i);
				return;
			}
		}

	}


}