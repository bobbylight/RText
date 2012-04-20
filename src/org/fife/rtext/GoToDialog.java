/*
 * 11/14/2003
 *
 * GoToDialog.java - A dialog allowing you to skip to a specific line number
 * in a document in RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import org.fife.ui.EscapableDialog;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;


/**
 * A "Go To" dialog allowing you to go to a specific line number in a document
 * in RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class GoToDialog extends EscapableDialog {

	private JButton okButton;
	private JButton cancelButton;
	private JTextField lineNumberField;
	private int maxLineNumberAllowed;	// Number of lines in the document.
	private int lineNumber;			// The line to go to, or -1 for Cancel.

	private RText owner;


	/**
	 * Creates a new <code>GoToDialog</code>.
	 *
	 * @param owner The rtext window that owns this dialog.
	 * @param actionListener The component that listens for GoTo events.
	 */
	public GoToDialog(RText owner, AbstractMainView actionListener) {

		// Let it be known who the owner of this dialog is.
		super(owner);
		this.owner = owner;

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
		ResourceBundle bundle = owner.getResourceBundle();

		lineNumber = 1;
		maxLineNumberAllowed = 1; // Empty document has 1 line.
		GoToListener listener = new GoToListener();

		// Set the main content pane for the "GoTo" dialog.
		JPanel contentPane =new ResizableFrameContentPane(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		setContentPane(contentPane);

		// Make a panel containing the "Line Number" edit box.
		JPanel enterLineNumberPane = new JPanel();
		BoxLayout box=new BoxLayout(enterLineNumberPane, BoxLayout.LINE_AXIS);
		enterLineNumberPane.setLayout(box);
		lineNumberField = new JTextField(16);
		lineNumberField.setText(""+lineNumber);
		AbstractDocument doc = (AbstractDocument)lineNumberField.getDocument();
		doc.addDocumentListener(listener);
		doc.setDocumentFilter(new NumberDocumentFilter());
		enterLineNumberPane.add(new JLabel(bundle.getString("LineNumber")));
		enterLineNumberPane.add(lineNumberField);

		// Make a panel containing the OK and Cancel buttons.
		JPanel buttonPanel = new JPanel(new GridLayout(1,2, 5,5));
		okButton = UIUtil.createRButton(bundle, "OKButtonLabel", "OKButtonMnemonic");
		okButton.setActionCommand("OK");
		okButton.addActionListener(listener);
		cancelButton = UIUtil.createRButton(bundle, "Cancel", "CancelMnemonic");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(listener);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		// Put everything into a neat little package.
		contentPane.add(enterLineNumberPane, BorderLayout.NORTH);
		JPanel temp = new JPanel();
		temp.add(buttonPanel);
		contentPane.add(temp, BorderLayout.SOUTH);
		JRootPane rootPane = getRootPane();
		rootPane.setDefaultButton(okButton);
		setTitle(bundle.getString("GotoDialogTitle"));
		setModal(true);
		applyComponentOrientation(orientation);
		pack();
		setLocationRelativeTo(owner);

	}


	/**
	 * Called when they've clicked OK or pressed Enter; check the line number
	 * they entered for validity and if it's okay, close this dialog.  If it
	 * isn't okay, display an error message.
	 */
	private void attemptToGetGoToLine() {

		try {

			lineNumber = Integer.parseInt(lineNumberField.getText());

			if (lineNumber<1 || lineNumber>maxLineNumberAllowed)
				throw new NumberFormatException();

			// If we have a valid line number, close the dialog!
			setVisible(false);

		} catch (NumberFormatException nfe) {
			ResourceBundle msg = owner.getResourceBundle();
			JOptionPane.showMessageDialog(this,
				msg.getString("LineNumberRange")+maxLineNumberAllowed+".",
				msg.getString("ErrorDialogTitle"),
				JOptionPane.ERROR_MESSAGE);
			return;
		}

	}


	/**
	 * Called when the user clicks Cancel or hits the Escape key.  This
	 * hides the dialog.
	 */
	protected void escapePressed() {
		lineNumber = -1;
		super.escapePressed();
	}


	/**
	 * Gets the line number the user entered to go to.
	 *
	 * @return The line number the user decided to go to, or <code>-1</code>
	 *         if the dialog was canceled.
	 */
	public int getLineNumber() {
		return lineNumber;
	}


	/**
	 * Sets the maximum line number for them to enter.
	 *
	 * @param max The new maximum line number value.
	 */
	public void setMaxLineNumberAllowed(int max) {
		this.maxLineNumberAllowed = max;
	}


	/**
	 * Overrides <code>JDialog</code>'s <code>setVisible</code> method; decides
	 * whether or not buttons are enabled if the user is enabling the dialog.
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			lineNumber = -1;
			okButton.setEnabled(lineNumberField.getDocument().getLength()>0);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					lineNumberField.requestFocusInWindow();
					lineNumberField.selectAll();
				}
			});
		}
		super.setVisible(visible);
	}


	/**
	 * Listens for events in this dialog.
	 */
	private class GoToListener implements ActionListener, DocumentListener {

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("OK"))
				attemptToGetGoToLine();
			else if (command.equals("Cancel"))
				escapePressed();
		}

		public void changedUpdate(DocumentEvent e) {
		}

		public void insertUpdate(DocumentEvent e) {
			okButton.setEnabled(lineNumberField.getDocument().getLength()>0);
		}

		public void removeUpdate(DocumentEvent e) {
			okButton.setEnabled(lineNumberField.getDocument().getLength()>0);
		}

	}


}