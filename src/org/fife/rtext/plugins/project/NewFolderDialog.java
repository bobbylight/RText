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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.rtext.plugins.project.model.FolderFilterInfo;
import org.fife.rtext.plugins.project.model.FolderProjectEntry;
import org.fife.ui.EscapableDialog;
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
	private JButton backButton;
	private JButton okButton;
	private JButton cancelButton;
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
		this(parent, null);
	}


	/**
	 * Constructor.
	 *
	 * @param parent The window that owns this directory chooser.
	 * @param entry If non-<code>null</code>, this will be a dialog to edit
	 *        the entry, instead of create a new one.
	 */
	public NewFolderDialog(RText parent, FolderProjectEntry entry) {
		super(parent);
		init(parent);
		if (entry!=null) {
			setFolderProjectEntryInfo(entry);
		}
		pack();
		setLocationRelativeTo(parent);
	}


	private static final void addLeftAligned(Container parent, Component toAdd){
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(toAdd, BorderLayout.LINE_START);
		parent.add(temp);
	}


	private static final JButton createButton(String keyRoot) {
		JButton button = new JButton(Messages.getString(keyRoot));
		button.setMnemonic(Messages.getMnemonic(keyRoot + ".Mnemonic"));
		return button;
	}


	@Override
	public void escapePressed() {
		chosenDirectory = null;
		super.escapePressed();
	}


	/**
	 * Returns the directory chosen by the user.
	 *
	 * @return The chosen directory.  If the user canceled the dialog, then
	 *         <code>null</code> is returned.
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
		info.setHiddenFolderFilters(getDisallowedDirectories());
		info.setHiddenFileFilters(getDisallowedFileFilters());
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
		Container buttonPanel = UIUtil.createButtonFooter(temp);
		container.add(buttonPanel, BorderLayout.SOUTH);

		// Get ready to go!
		panelIndex = 0;
		layout.show(cp, PANELS[panelIndex]);
		setContentPane(container);
		setTitle(Messages.getString("FolderDialog.Title.Add"));
		applyComponentOrientation(orientation);
		getRootPane().setDefaultButton(okButton);
		setModal(true);

	}


	private void moveForwardOneStep() {
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


	private void setAllowedFileFilters(String[] filters) {
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
	private boolean setChosenDirectory(File dir) {
		return directoryTree.setSelectedFile(dir);
	}


	private void setDisallowedDirectories(String[] filters) {
		if (filters==null || filters.length==0) {
			outFolderField.setText(null);
		}
		outFolderField.setText(RTextUtilities.join(filters));
	}


	private void setDisallowedFileFilters(String[] filters) {
		if (filters==null || filters.length==0) {
			outFilterField.setText(null);
		}
		outFilterField.setText(RTextUtilities.join(filters));
	}


	/**
	 * Configures this dialog to edit an existing folder project entry,
	 * instead of creating a new one.
	 *
	 * @param entry The entry to initialize all fields with.
	 */
	private void setFolderProjectEntryInfo(FolderProjectEntry entry) {

		setChosenDirectory(entry.getFile());
		FolderFilterInfo info = entry.getFilterInfo();
		setAllowedFileFilters(info.getAllowedFileFilters());
		setDisallowedDirectories(info.getHiddenFolderFilters());
		setDisallowedFileFilters(info.getHiddenFileFilters());

		setTitle(Messages.getString("FolderDialog.Title.Edit"));

		// Folder panel is disabled, might as well skip it.
		directoryTree.setEnabled(false);
		setChosenDirectory(entry.getFile());
		moveForwardOneStep();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				inFilterField.selectAll();
				inFilterField.requestFocusInWindow();
			}
		});

		pack();

	}


	/**
	 * Listens for all events in this directory chooser.
	 */
	private class Listener implements ActionListener, TreeSelectionListener,
							PropertyChangeListener {

		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (okButton==source) {
				moveForwardOneStep();
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