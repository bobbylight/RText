/*
 * 09/12/2012
 *
 * NewFolderDialog.java - Adds a new physical folder to a project.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.plugins.project.model.FolderFilterInfo;
import org.fife.ui.EscapableDialog;
import org.fife.ui.RButton;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.RScrollPane;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;
import org.fife.ui.rtextfilechooser.DirectoryTree;


/**
 * Lets the user select a folder to add to a project.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class NewFolderDialog extends EscapableDialog {

	private SelectableLabel descLabel;
	private RButton backButton;
	private RButton okButton;
	private RButton cancelButton;
	private JPanel cp;
	private CardLayout layout;
	private int panelIndex;
	private DirectoryTree directoryTree;

	private JTextField inFilterField;
	private JTextField outFilterField;
	private JTextField outFolderField;
	
	private String chosenDirectory;

	private static final String[] PANELS = { "SelectDir", "EnterFilters" };


	/**
	 * Constructor.
	 *
	 * @param parent The window that owns this directory chooser.
	 */
	public NewFolderDialog(RText parent) {
		super(parent);
		init(parent);
	}


	private void addLeftAligned(Container parent, Component toAdd) {
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(toAdd, BorderLayout.LINE_START);
		parent.add(temp);
	}


	private RButton createButton(String keyRoot) {
		RButton button = new RButton(Messages.getString(keyRoot));
		button.setMnemonic(Messages.getMnemonic(keyRoot + ".Mnemonic"));
		return button;
	}


	public void escapePressed() {
		chosenDirectory = null;
		super.escapePressed();
	}


	/**
	 * Returns the directory chosen by the user.
	 *
	 * @return The chosen directory.  If the user canceled the dialog, then
	 *         <code>null</code> is returned.
	 * @see #setChosenDirectory(File)
	 */
	public String getChosenDirectory() {
		return chosenDirectory;
	}


	private String[] getAllowedFileFilters() {
		String text = inFilterField.getText().trim();
		if (text.length()==0 || (text.length()==1 && text.charAt(0)=='*')) {
			return null;
		}
		return text.split("\\s*,\\s*");
	}


	private String[] getDisallowedDirectories() {
		String text = outFolderField.getText().trim();
		if (text.length()==0) {
			return null;
		}
		return text.split("\\s*,\\s*");
	}


	private String[] getDisallowedFileFilters() {
		String text = outFilterField.getText().trim();
		if (text.length()==0) {
			return null;
		}
		return text.split("\\s*,\\s*");
	}


	/**
	 * Returns the filtering info selected in this dialog.
	 *
	 * @return The filtering information selected, or <code>null</code> if
	 *         the directory was cancelled.
	 */
	public FolderFilterInfo getFilterInfo() {
		if (chosenDirectory==null) {
			return null;
		}
		FolderFilterInfo info = new FolderFilterInfo();
		info.setAllowedFileFilters(getAllowedFileFilters());
		info.setDisallowedDirectoryFilters(getDisallowedDirectories());
		info.setDisallowedFileFilters(getDisallowedFileFilters());
		return info;
	}


	/**
	 * Initializes this directory chooser.
	 *
	 * @param parent The window that owns this directory chooser.
	 */
	private void init(Window parent) {

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		Listener listener = new Listener();

		// The "choose a directory" panel.
		layout = new CardLayout();
		cp = new JPanel(layout);

		// Add a panel with the directory tree.
		JPanel treePanel = new JPanel(new GridLayout(1,1));
		directoryTree = new DirectoryTree();
		directoryTree.getSelectionModel().addTreeSelectionListener(listener);
		directoryTree.addPropertyChangeListener(listener);
		RScrollPane scrollPane = new RScrollPane(directoryTree);
		scrollPane.setHorizontalScrollBarPolicy(
							RScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(
							RScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		treePanel.add(scrollPane);
		cp.add(treePanel);
		layout.addLayoutComponent(treePanel, PANELS[0]);

		// The "enter filters" panel.
		Box filterPanel = Box.createVerticalBox();
		JLabel inFilterLabel = new JLabel(Messages.getString("FolderDialog.InFilter"));
		inFilterLabel.setDisplayedMnemonic(Messages.getMnemonic("FolderDialog.InFilter.Mnemonic"));
		addLeftAligned(filterPanel, inFilterLabel);
		inFilterField = new JTextField("*", 20);
		filterPanel.add(inFilterField);
		inFilterLabel.setLabelFor(inFilterField);
		filterPanel.add(Box.createVerticalStrut(20));
		JLabel outFilterLabel = new JLabel(Messages.getString("FolderDialog.OutFilter"));
		outFilterLabel.setDisplayedMnemonic(Messages.getMnemonic("FolderDialog.OutFilter.Mnemonic"));
		addLeftAligned(filterPanel, outFilterLabel);
		outFilterField = new JTextField(20);
		filterPanel.add(outFilterField);
		outFilterLabel.setLabelFor(outFilterField);
		filterPanel.add(Box.createVerticalStrut(20));
		JLabel outFolderLabel = new JLabel(Messages.getString("FolderDialog.OutFolders"));
		outFolderLabel.setDisplayedMnemonic(Messages.getMnemonic("FolderDialog.OutFolders.Mnemonic"));
		addLeftAligned(filterPanel, outFolderLabel);
		outFolderField = new JTextField("CVS, .svn, .hg, .git", 20);
		filterPanel.add(outFolderField);
		outFolderLabel.setLabelFor(outFolderField);
		filterPanel.add(Box.createVerticalGlue());
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(filterPanel, BorderLayout.NORTH);
		cp.add(temp);
		layout.addLayoutComponent(temp, PANELS[1]);

		// Add a panel with the OK and Cancel buttons.
		JPanel container = new ResizableFrameContentPane(new BorderLayout());
		container.setBorder(UIUtil.getEmpty5Border());
		container.add(cp);
		descLabel= new SelectableLabel(Messages.getString(
				"FolderDialog.FolderSelect.Desc"));
		descLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		container.add(descLabel, BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel(new BorderLayout());
		temp = new JPanel(new GridLayout(1,3, 5,0));
		temp.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		backButton = createButton("Button.Back");
		backButton.setEnabled(false);
		backButton.addActionListener(listener);
		temp.add(backButton);
		okButton = createButton("Button.Next");
		okButton.setEnabled(false);
		okButton.addActionListener(listener);
		temp.add(okButton);
		cancelButton = createButton("Button.Cancel");
		cancelButton.addActionListener(listener);
		temp.add(cancelButton);
		buttonPanel.add(temp, BorderLayout.LINE_END);
		container.add(buttonPanel, BorderLayout.SOUTH);

		// Get ready to go!
		panelIndex = 0;
		layout.show(cp, PANELS[panelIndex]);
		setContentPane(container);
		setTitle(Messages.getString("FolderDialog.Title"));
		applyComponentOrientation(orientation);
		getRootPane().setDefaultButton(okButton);
		pack();
		setModal(true);
		setLocationRelativeTo(parent);

	}


	public void setAllowedFileFilters(String[] filters) {
		if (filters==null || filters.length==0) {
			inFilterField.setText(null);
		}
		inFilterField.setText(RTextUtilities.join(filters));
	}


	/**
	 * Selects the specified directory, if it exists.  Otherwise, the
	 * selection is cleared.
	 *
	 * @param dir The directory to select.
	 * @return Whether the directory exists and was selected.
	 * @see #getChosenDirectory()
	 */
	public boolean setChosenDirectory(File dir) {
		return directoryTree.setSelectedFile(dir);
	}


	public void setDisallowedDirectories(String[] filters) {
		if (filters==null || filters.length==0) {
			outFolderField.setText(null);
		}
		outFolderField.setText(RTextUtilities.join(filters));
	}


	public void setDisallowedFileFilters(String[] filters) {
		if (filters==null || filters.length==0) {
			outFilterField.setText(null);
		}
		outFilterField.setText(RTextUtilities.join(filters));
	}


	/**
	 * Listens for all events in this directory chooser.
	 */
	private class Listener implements ActionListener, TreeSelectionListener,
							PropertyChangeListener {

		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (okButton==source) {
				backButton.setEnabled(true);
				if (panelIndex==PANELS.length-1) {
					chosenDirectory = directoryTree.getSelectedFileName();
					setVisible(false);
				}
				else {
					layout.show(cp, PANELS[++panelIndex]);
					if (panelIndex==PANELS.length-1) {
						okButton.setText(Messages.getString("Button.Finish"));
						okButton.setMnemonic(Messages.getMnemonic("Button.Finish.Mnemonic"));
						descLabel.setText(Messages.getString("FolderDialog.Filters.Desc"));
					}
				}
			}
			else if (backButton==source) {
				layout.show(cp, PANELS[--panelIndex]);
				backButton.setEnabled(panelIndex>0);
				okButton.setText(Messages.getString("Button.Next"));
				okButton.setMnemonic(Messages.getMnemonic("Button.Next.Mnemonic"));
				descLabel.setText(Messages.getString("FolderDialog.FolderSelect.Desc"));
			}
			else if (cancelButton==source) {
				escapePressed();
			}
		}

		public void propertyChange(PropertyChangeEvent e) {
			String property = e.getPropertyName();
			if (property.equals(DirectoryTree.WILL_EXPAND_PROPERTY)) {
				NewFolderDialog.this.setCursor(
					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
			else if (property.equals(DirectoryTree.EXPANDED_PROPERTY)) {
				NewFolderDialog.this.setCursor(
					Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}

		public void valueChanged(TreeSelectionEvent e) {
			okButton.setEnabled(e.getNewLeadSelectionPath()!=null);
		}

	}


}