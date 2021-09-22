/*
 * 12/19/2014
 *
 * RecentFileDialog.java - Allows the user to quickly open a recently-opened
 * file.
 * Copyright (C) 2014 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.EscapableDialog;
import org.fife.ui.RListSelectionModel;
import org.fife.ui.RScrollPane;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;


/**
 * A dialog allowing the user to quickly open a file they have recently opened.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RecentFileDialog extends EscapableDialog {

	private RText rtext;
	private List<FileLocation> files;

	private JList<FileLocation> list;
	private DefaultListModel<FileLocation> model;
	private JTextField filterField;
	private JButton okButton;

	/**
	 * The maximum width of this dialog.
	 */
	private static final int MAX_WIDTH = 800;


	/**
	 * Constructor.
	 *
	 * @param parent The parent application.
	 */
	public RecentFileDialog(RText parent) {

		super(parent, parent.getString("Dialog.RecentFiles.Title"), true);
		this.rtext = parent;
		files = rtext.getRecentFiles();

		createUI();

		pack();
		setLocationRelativeTo(rtext);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

	}


	/**
	 * Creates the content of this dialog.
	 */
	private void createUI() {

		Listener listener = new Listener();

		JPanel cp = new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(UIUtil.getEmpty5Border());
		setContentPane(cp);

		filterField = new JTextField();
		filterField.getDocument().addDocumentListener(listener);
		filterField.addKeyListener(listener);
		cp.add(filterField, BorderLayout.NORTH);

		okButton = new JButton(rtext.getString("OKButtonLabel"));
		okButton.setActionCommand("OK");
		okButton.addActionListener(listener);
		JButton cancelButton = new JButton(rtext.getString("Cancel"));
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(listener);

		Container buttons = UIUtil.createButtonFooter(okButton, cancelButton);
		cp.add(buttons, BorderLayout.SOUTH);

		model = new DefaultListModel<>();
		list = new JList<>(model);
		setFilter(null); // Do initial population.
		list.addMouseListener(listener);
		list.setCellRenderer(new RecentFileListCellRenderer());
		list.setSelectionModel(new RListSelectionModel());
		RScrollPane sp = new RScrollPane(list);
		cp.add(sp);

		getRootPane().setDefaultButton(okButton);

	}


	/**
	 * Overridden to limit this dialog's width.
	 */
	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		if (size != null) {
			size.width = Math.min(MAX_WIDTH, size.width);
		}
		return size;
	}


	/**
	 * Returns whether the given file's name matches a pattern.
	 *
	 * @param loc The file to check.
	 * @param pattern The pattern for the file name.
	 * @return Whether the file's name matched the pattern.
	 */
	private static boolean matches(FileLocation loc, Pattern pattern) {
		if (pattern == null) {
			return true;
		}
		String fileName = loc.getFileName();
		return pattern.matcher(fileName).find();
	}


	/**
	 * Opens the file selected in the file list.
	 */
	private void openSelectedFile() {
		FileLocation loc = list.getSelectedValue();
		if (loc != null) {
			if (loc.isLocalAndExists()) {
				rtext.openFile(new File(loc.getFileFullPath()));
			}
			else {
				// TODO: Support opening remote FileLocations once
				// RSyntaxTextArea bug #94 is fixed.  Probably have to modify
				// RemoteFileChooser to be pre-filled in but prompt just for
				// password.
				UIManager.getLookAndFeel().provideErrorFeedback(list);
			}
			escapePressed();
		}
		else {
			UIManager.getLookAndFeel().provideErrorFeedback(list);
		}
	}


	/**
	 * Selects the next row in the file list.
	 *
	 * @see #selectPreviousVisibleRow()
	 */
	private void selectNextVisibleRow() {
		if (model.size() > 0) {
			int index = list.getSelectedIndex();
			index = (index + 1) % model.size();
			list.setSelectedIndex(index);
			list.ensureIndexIsVisible(index);
		}
	}


	/**
	 * Selects the previous row in the file list.
	 *
	 * @see #selectNextVisibleRow()
	 */
	private void selectPreviousVisibleRow() {
		if (model.size() > 0) {
			int index = list.getSelectedIndex();
			index--;
			if (index < 0) {
				index = model.size() - 1;
			}
			list.setSelectedIndex(index);
			list.ensureIndexIsVisible(index);
		}
	}


	/**
	 * Sets the filter for the file list.
	 *
	 * @param filter The new filter.
	 */
	private void setFilter(String filter) {

		model.clear();

		Pattern pattern = filter==null || filter.isEmpty() ? null :
			RSyntaxUtilities.wildcardToPattern("^" + filter, false, false);

		for (FileLocation loc : files) {
			if (matches(loc, pattern)) {
				model.addElement(loc);
			}
		}

		boolean modelNotEmpty = model.size() > 0;
		if (modelNotEmpty) {
			list.setSelectedIndex(0);
			list.ensureIndexIsVisible(0);
		}
		okButton.setEnabled(modelNotEmpty);

	}


	/**
	 * Toggles whether this dialog is visible.
	 *
	 * @param visible Whether this dialog should be visible.
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			if (model.size() > 0) {
				// Evidently list must be displayable for this to work?
				list.setSelectedIndex(0);
			}
			super.setVisible(true);
			filterField.requestFocusInWindow();
		}
		else {
			super.setVisible(false);
		}
	}


	/**
	 * Listens for events in this dialog.
	 */
	private class Listener extends MouseAdapter implements ActionListener,
			DocumentListener, KeyListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			String command = e.getActionCommand();

			if ("OK".equals(command)) {
				openSelectedFile();
			}

			else if ("Cancel".equals(command)) {
				escapePressed();
			}

		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			handleDocumentEvent();
		}

		private void handleDocumentEvent() {
			setFilter(filterField.getText());
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent();
		}

		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_DOWN -> selectNextVisibleRow();
				case KeyEvent.VK_UP -> selectPreviousVisibleRow();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount()==2) {
				openSelectedFile();
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent();
		}

	}


}
