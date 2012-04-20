/*
 * 01/13/2010
 *
 * GetKeyStrokeDialog.java - A dialog that allows the user to edit a
 * key stroke.
 * Copyright (C) 2010 Robert Futrell
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.fife.ui.RButton;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;


/**
 * A dialog for editing a key stroke.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class GetKeyStrokeDialog extends JDialog implements ActionListener {

	private KeyStroke stroke;
	private KeyStrokeField textField;
	private boolean canceled;


	/**
	 * Constructor.
	 *
	 * @param rtext The parent application.
	 * @param initial The initial key stroke to display.
	 */
	public GetKeyStrokeDialog(RText rtext, KeyStroke initial) {
		super(rtext, rtext.getResourceBundle().getString("KeyStrokeDialogTitle"));
		ComponentOrientation orientation = ComponentOrientation.
								getOrientation(getLocale());
		ResourceBundle msg = rtext.getResourceBundle();
		JPanel contentPane = new JPanel();
		contentPane.setBorder(UIUtil.getEmpty5Border());
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		JPanel temp = new JPanel(new BorderLayout());
		JLabel prompt = new JLabel(msg.getString("KeyStrokePrompt"));
		temp.add(prompt, BorderLayout.LINE_START);
		contentPane.add(temp);
		contentPane.add(Box.createVerticalStrut(8));
		JLabel charLabel = new JLabel(msg.getString("KeyStrokeKey"));
		textField = new KeyStrokeField();
		charLabel.setLabelFor(textField);
		temp = new JPanel(new BorderLayout());
		temp.add(charLabel, BorderLayout.LINE_START);
		temp.add(textField);
		contentPane.add(temp);
		temp = new JPanel(new GridLayout(1,2, 5,5));
		RButton ok = new RButton(msg.getString("OKButtonLabel"));
		ok.setActionCommand("OK");
		ok.addActionListener(this);
		temp.add(ok);
		RButton cancel = new RButton(msg.getString("Cancel"));
		cancel.setActionCommand("Cancel");
		cancel.addActionListener(this);
		temp.add(cancel);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(temp);
		JPanel realCP = new ResizableFrameContentPane(new BorderLayout());
		realCP.add(contentPane, BorderLayout.NORTH);
		realCP.add(buttonPanel, BorderLayout.SOUTH);
		setContentPane(realCP);
		setKeyStroke(initial);
		setModal(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		applyComponentOrientation(orientation);
		pack();
	}


	/**
	 * Called when an event occurs in this dialog.
	 *
	 * @param e The event.
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("OK")) {
			stroke = textField.getKeyStroke();
			canceled = false;
			setVisible(false);
		}
		else if (command.equals("Cancel")) {
			stroke = null;
			setVisible(false);
		}
	}


	/**
	 * Whether the dialog was canceled.
	 *
	 * @return Whether the dialog was canceled.
	 */
	public boolean getCancelled() {
		return canceled;
	}


	/**
	 * Returns the key stroke the user entered.
	 *
	 * @return The key stroke, or <code>null</code> if the user canceled the
	 *         dialog.
	 * @see #setKeyStroke(KeyStroke)
	 */
	public KeyStroke getKeyStroke() {
		return stroke;
	}

	/**
	 * Sets the key stroke displayed in this dialog.
	 *
	 * @param stroke The key stroke.
	 * @see #getKeyStroke()
	 */
	public void setKeyStroke(KeyStroke stroke) {
		this.stroke = stroke;
		textField.setText(RTextUtilities.getPrettyStringFor(stroke));
	}


	/**
	 * {@inheritDoc}
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			canceled = true; // Default to canceled.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					textField.requestFocusInWindow();
					textField.selectAll();
				}
			});
		}
		super.setVisible(visible);
	}


}