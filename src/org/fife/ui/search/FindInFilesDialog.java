/*
 * 11/26/2003
 *
 * FindInFilesDialog.java - A dialog that allows you to search for text
 * in all files in a directory.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.search;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;

import org.fife.rsta.ui.AssistanceIconPanel;
import org.fife.rsta.ui.RComboBoxModel;
import org.fife.rsta.ui.search.AbstractSearchDialog;
import org.fife.rsta.ui.search.FindReplaceButtonsEnableResult;
import org.fife.ui.FSATextField;
import org.fife.ui.RScrollPane;
import org.fife.ui.StatusBar;
import org.fife.ui.UIUtil;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextfilechooser.RDirectoryChooser;


/**
 * A dialog allowing the user to search for a text string in all files in a
 * directory, so they don't have to do the files one at a time.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class FindInFilesDialog extends AbstractSearchDialog {

	// Text fields in which the user enters parameters that are not
	// defined in AbstractSearchDialog.
	protected JComboBox inFilesComboBox;
	protected FSATextField inFolderTextField;

	protected JCheckBox subfoldersCheckBox;

	protected JButton findButton;
	private JButton browseButton;

	private JRadioButton matchingLinesRadioButton;
	private JRadioButton fileCountsOnlyRadioButton;

	protected JCheckBox verboseCheckBox;

	private StatusBar statusBar;

	private ResultsComponent resultsComponent;

	// This helps us work around the "bug" where JComboBox eats the first
	// Enter press.
	private String lastSearchString;
	private String lastInFilesString;

	// The listener list for FindInFilesEvents.
	private EventListenerList eventListenerList;

	private FindInFilesThread workerThread;
	private FindInFilesDocumentListener docListener;

	// Some strings cached from our resources for efficiency.
	private String defaultStatusText;
	private String searchingCompleteString;

	private static final String MSG = "org.fife.ui.search.Search";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	/**
	 * Creates a new <code>FindInFilesDialog</code>.
	 *
	 * @param owner The main window that owns this dialog.
	 */
	public FindInFilesDialog(Frame owner) {

		super(owner);
		this.setTitle(getString2("FindInFilesDialogTitle"));

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		// These listeners will be used by all text fields.
		docListener = new FindInFilesDocumentListener();
		FindInFilesFocusAdapter focusAdapter = new FindInFilesFocusAdapter();
		FindInFilesKeyListener keyListener = new FindInFilesKeyListener();

		// Set the main content pane for the "Find in Files" dialog.
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);

		// Make a "Find what" combo box.
		JTextComponent textField = getTextComponent(findTextCombo);
		textField.addFocusListener(focusAdapter);
		textField.addKeyListener(keyListener);
		textField.getDocument().addDocumentListener(docListener);

		// Make an "In files" combo box.
		inFilesComboBox = new JComboBox(new RComboBoxModel());
		inFilesComboBox.setEditable(true);
		textField = getTextComponent(inFilesComboBox);
		textField.addFocusListener(focusAdapter);
		textField.addKeyListener(keyListener);
		textField.getDocument().addDocumentListener(docListener);

		// Make an "In folder" text field.
		// NOTE:  Change the line below for "directories only", but
		// SLOW NFS paths because of File.isDirectory()...
		inFolderTextField = new FSATextField();//true);
		inFolderTextField.setText(System.getProperty("user.home"));
		inFolderTextField.addFocusListener(focusAdapter);
		inFolderTextField.getDocument().addDocumentListener(docListener);

		// Make a panel containing the edit boxes and their associated labels.
		JPanel inputPanel = createInputPanel();

		// Make a "Conditions" panel.
		Box conditionsPanel = Box.createVerticalBox();
		conditionsPanel.setBorder(createTitledBorder(getString2("Conditions")));
		conditionsPanel.add(caseCheckBox);
		conditionsPanel.add(wholeWordCheckBox);
		conditionsPanel.add(regexCheckBox);

		// Make a "Report detail" panel.
		Box detailEtcPanel = createDetailsPanel();

		// Make a panel containing the "Conditions" and "detailEtc" panels.
		Box bottomLeftPanel = new Box(BoxLayout.LINE_AXIS);
		bottomLeftPanel.add(conditionsPanel);
		bottomLeftPanel.add(Box.createHorizontalStrut(10));
		bottomLeftPanel.add(detailEtcPanel);
		bottomLeftPanel.add(Box.createHorizontalGlue());

		// Make a panel containing all of the text fields, and the bottom left panel.
		Box leftPanel = Box.createVerticalBox();
		leftPanel.add(inputPanel);
		leftPanel.add(bottomLeftPanel);

		// Make a panel containing the buttons.
		JPanel rightPanel2 = new JPanel(new GridLayout(3,1, 5,5));
		findButton = UIUtil.newButton(getBundle(), "Find");
		findButton.setActionCommand("FindInFiles");
		findButton.addActionListener(this);
		browseButton = UIUtil.newButton(msg, "Browse");
		browseButton.setActionCommand("Browse");
		browseButton.addActionListener(this);
		cancelButton = UIUtil.newButton(msg, "Close");
		cancelButton.setActionCommand("Close");
		cancelButton.addActionListener(this);
		rightPanel2.add(findButton);
		rightPanel2.add(browseButton);
		rightPanel2.add(cancelButton);
		JPanel rightPanel = new JPanel(new BorderLayout());
		if (orientation.isLeftToRight()) {
			rightPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		}
		else {
			rightPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		}
		rightPanel.add(rightPanel2, BorderLayout.NORTH);

		// Combine leftPanel and rightPanel.
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(leftPanel);
		topPanel.add(rightPanel, BorderLayout.LINE_END);

		// Make a panel containing a "Verbose output" check box.
		Box extraOptionsPanel = createExtraOptionsPanel();

		// Make the "results" panel.
		JPanel resultsPanel = new JPanel(new GridLayout(1,1, 3,3));
		Border empty5Border = UIUtil.getEmpty5Border();
		resultsPanel.setBorder(BorderFactory.createCompoundBorder(
			empty5Border,
			BorderFactory.createCompoundBorder(
				createTitledBorder(getString2("Results")),
				BorderFactory.createEmptyBorder(3,3,3,3)
			)));
		resultsComponent = createResultsComponent();
		JScrollPane resultsScrollPane = new RScrollPane((JComponent)resultsComponent);
		resultsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		resultsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		resultsPanel.add(resultsScrollPane);

		// Make the "status bar."
		statusBar = new org.fife.ui.StatusBar();

		// Initialize some variables.
		eventListenerList = new EventListenerList();
		defaultStatusText = getString2("DefaultStatusText");
		searchingCompleteString = getString2("SearchingComplete");

		// Put everything together.
		setStatusText(defaultStatusText);
		Box temp = Box.createVerticalBox();
		temp.setBorder(empty5Border);
		temp.add(topPanel);
		temp.add(Box.createVerticalStrut(5));
		if (extraOptionsPanel!=null) {
			temp.add(extraOptionsPanel);
		}
		contentPane.add(temp, BorderLayout.NORTH);
		contentPane.add(resultsPanel);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(findButton);
		setModal(false);
		applyComponentOrientation(orientation);
		pack();
		setLocationRelativeTo(owner);

	}


	/**
	 * Called whenever the user does something.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		// If the user selects the "Find" button...
		if ("FindInFiles".equals(command)) {

			// Add the "Find What" item to the combo box's list.  Then, if
			// they just searched for an item that's already in the list
			// other than the first, move it to the first position.
			String item = getTextComponent(findTextCombo).getText();
			findTextCombo.addItem(item); // Ensures item is at index 0.
			context.setSearchFor(getSearchString());

			// Add the "In Files" item to the combo box's list.  Then, if
			// they just searched for an item that's already in the list
			// other than the first, move it to the first position.
			item = getTextComponent(inFilesComboBox).getText();
			inFilesComboBox.addItem(item); // Ensures item is at index 0.

			// Actually perform the search.
			doFindInFiles();

		}


		// If the user selects the "Browse..." button...
		else if ("Browse".equals(command)) {
			RDirectoryChooser chooser = new RDirectoryChooser(this);
			String dirName = inFolderTextField.getText().trim();
			if (dirName.length()>0) {
				File dir = new File(dirName);
				chooser.setChosenDirectory(dir);
			}
			chooser.setVisible(true);
			String directory = chooser.getChosenDirectory();
			if (directory!=null) {
				inFolderTextField.setFileSystemAware(false);
				inFolderTextField.setText(directory);
				inFolderTextField.setFileSystemAware(true);
			}
		}

		// If the user selects the Close/Stop button...
		else if ("Close".equals(command)) {
			FindInFilesThread workerThread = getWorkerThread();
			if (workerThread!=null) { // Search going on => stop search.
				workerThread.interrupt();
				setSearching(false);
			}
			else { // No search => close the dialog.
				this.setVisible(false);
			}
		}

		else if ("Subfolders".equals(command)) {
			boolean search = subfoldersCheckBox.isSelected();
			((FindInFilesSearchContext)context).setSearchSubfolders(search);
		}

		else if ("Verbose".equals(command)) {
			boolean verbose = verboseCheckBox.isSelected();
			((FindInFilesSearchContext)context).setVerbose(verbose);
		}

		// The superclass might care about this action.
		else {
			super.actionPerformed(e);
		}

	}


	/**
	 * Adds information on a match (or verbose search information) to the
	 * search table.<p>
	 *
	 * We assume this method is being called by {@link FindInFilesThread},
	 * not the EDT, so the match data is added via
	 * <code>SwingUtilities.invokeLater</code>.  Match data should never be
	 * gathered on the EDT since it is a potentially long process to gather it.
	 *
	 * @param matchData Data about the found text.
	 */
	void addMatchData(final MatchData matchData) {
		final String dirName = inFolderTextField.getText();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				getResultsComponent().addMatchData(matchData, dirName);
			}
		});
	}


	/**
	 * Adds a find-in-files listener to this find in files dialog.
	 *
	 * @param listener The listener to add.
	 * @see  #removeFindInFilesListener
	 */
	public void addFindInFilesListener(FindInFilesListener listener) {
		eventListenerList.add(FindInFilesListener.class, listener);
	}


	/**
	 * Adds a file filter to the "In files:" combo box.
	 *
	 * @param filter The filter to add.
	 */
	public void addInFilesComboBoxFilter(String filter) {
		inFilesComboBox.addItem(filter);
	}


	/**
	 * Clears the search results table.  This method can be called from
	 * threads other than the EDT.
	 */
	void clearSearchResults() {
		if (SwingUtilities.isEventDispatchThread()) {
			getResultsComponent().clear();
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() { getResultsComponent().clear(); }
			});
		}
	}


	/**
	 * Creates the panel containing "Report Detail" options.
	 *
	 * @return The panel.
	 */
	protected Box createDetailsPanel() {

		Box detailPanel = Box.createVerticalBox();
		detailPanel.setBorder(createTitledBorder(getString2("ReportDetail")));
		matchingLinesRadioButton = new JRadioButton(getString2("MatchingLines"));
		matchingLinesRadioButton.setMnemonic((int)getString2("MatchingLinesMnemonic").charAt(0));
		matchingLinesRadioButton.setSelected(true);
		detailPanel.add(matchingLinesRadioButton);
		fileCountsOnlyRadioButton = new JRadioButton(getString2("FileCounts"));
		fileCountsOnlyRadioButton.setMnemonic((int)getString2("FileCountsMnemonic").charAt(0));
		ButtonGroup bg = new ButtonGroup();
		bg.add(matchingLinesRadioButton);
		bg.add(fileCountsOnlyRadioButton);
		detailPanel.add(fileCountsOnlyRadioButton);

		// Make a panel containing the "Report detail" panel and some check boxes.
		Box panel = Box.createVerticalBox();
		subfoldersCheckBox = new JCheckBox(getString2("SearchSubfolders"));
		subfoldersCheckBox.setMnemonic((int)getString2("SearchSubfoldersMnemonic").charAt(0));
		subfoldersCheckBox.setActionCommand("Subfolders");
		subfoldersCheckBox.addActionListener(this);
		panel.add(detailPanel);
		panel.add(subfoldersCheckBox);

		return panel;

	}


	/**
	 * Returns a panel containing any extra options, such as a "verbose"
	 * output option.
	 *
	 * @return The panel, or <code>null</code> if there are no extra
	 *         options.
	 */
	protected Box createExtraOptionsPanel() {
		Box temp = new Box(BoxLayout.LINE_AXIS);
		verboseCheckBox = new JCheckBox(getString2("Verbose"));
		verboseCheckBox.setActionCommand("Verbose");
		verboseCheckBox.addActionListener(this);
		verboseCheckBox.setMnemonic((int)getString2("VerboseMnemonic").charAt(0));
		temp.add(verboseCheckBox);
		temp.add(Box.createHorizontalGlue());
		return temp;
	}


	/**
	 * Creates and returns the panel containing input fields and their
	 * labels.
	 *
	 * @return The panel.
	 */
	protected JPanel createInputPanel() {

		JPanel inputPanel = new JPanel(new SpringLayout());

		// Make labels to go with the combo boxes/text fields.
		JLabel findLabel = UIUtil.newLabel(getBundle(), "FindWhat", findTextCombo);
		JLabel inLabel = new JLabel(getString2("InFiles"));
		inLabel.setLabelFor(inFilesComboBox);
		inLabel.setDisplayedMnemonic((int)getString2("InFilesMnemonic").charAt(0));
		JLabel dirLabel = new JLabel(getString2("InDirectory"));
		dirLabel.setLabelFor(inFolderTextField);
		dirLabel.setDisplayedMnemonic((int)getString2("InDirectoryMnemonic").charAt(0));

		JPanel temp = new JPanel(new BorderLayout());
		temp.add(findTextCombo);
		AssistanceIconPanel aip = new AssistanceIconPanel(findTextCombo);
		temp.add(aip, BorderLayout.LINE_START);

		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(inFilesComboBox);
		temp2.add(Box.createHorizontalStrut(AssistanceIconPanel.WIDTH), BorderLayout.LINE_START);

		JPanel temp3 = new JPanel(new BorderLayout());
		temp3.add(inFolderTextField);
		temp3.add(Box.createHorizontalStrut(AssistanceIconPanel.WIDTH), BorderLayout.LINE_START);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		// Make a panel of the edit fields and add it to inputPanel.
		if (orientation.isLeftToRight()) {
			inputPanel.add(findLabel);
			inputPanel.add(temp);
			inputPanel.add(inLabel);
			inputPanel.add(temp2);
			inputPanel.add(dirLabel);
			inputPanel.add(temp3);
		}
		else {
			inputPanel.add(temp);
			inputPanel.add(findLabel);
			inputPanel.add(temp2);
			inputPanel.add(inLabel);
			inputPanel.add(temp3);
			inputPanel.add(dirLabel);
		}
		UIUtil.makeSpringCompactGrid(inputPanel,
									3,2,		// rows,cols,
									0,0,		// initial-x, initial-y,
									5,5);	// x-spacing, y-spacing.

		return inputPanel;

	}


	/**
	 * Creates and returns the component used to display search
	 * results.
	 *
	 * @return The component.
	 */
	protected ResultsComponent createResultsComponent() {
		FindInFilesTable table = new FindInFilesTable();
		table.addMouseListener(new FindInFilesDialogMouseListener(table));
		return table;
	}


	/**
	 * Overridden to return the "find in files"-specific search context.
	 *
	 * @return The search context.
	 */
	@Override
	protected SearchContext createDefaultSearchContext() {
		return new FindInFilesSearchContext();
	}


	/**
	 * Returns the thread that will do the searching.
	 *
	 * @param directory The directory to search in.
	 * @return The thread.
	 */
	protected FindInFilesThread createWorkerThread(File directory) {
		return new FindInFilesThread(this, directory);
	}


	/**
	 * This function actually performs a search through the given directory.
	 */
	private void doFindInFiles() {

		// First, ensure that the directory they selected actually exists.
		String dirPath = inFolderTextField.getText();
		final File directory = new File(dirPath);
		if (!directory.isDirectory()) {
			JOptionPane.showMessageDialog(this,
						getString2("ErrorDirNotExist") + dirPath,
						getString2("ErrorDialogTitle"),
						JOptionPane.ERROR_MESSAGE);
			inFolderTextField.selectAll();
			inFolderTextField.requestFocusInWindow();
			return;
		}

		// Next, if we're doing a regex search, ensure we have a valid
		// regex to search for.
		if (regexCheckBox.isSelected()) {
			try {
				Pattern.compile(getSearchString());
			} catch (Exception e) {
				// Doesn't usually happen; should be caught earlier.
				String text = e.getMessage();
				if (text==null) {
					text = e.toString();
				}
				JOptionPane.showMessageDialog(this,
					"Invalid regular expression:\n" + text +
					"\nPlease check your regular expression search string.",
					getString2("ErrorDialogTitle"),
					JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		// Show the hourglass cursor, as we may have a wait ahead of us.
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Disable the buttons so the user doesn't think they can use them
		// while we're searching.
		setSearching(true);

		// Start searching!
		setWorkerThread(createWorkerThread(directory));
		getWorkerThread().start();

	}


	/**
	 * Notifies all find-in-files listeners of a find-in-files event in this
	 * dialog.
	 *
	 * @param e The event to notify all listeners about.
	 */
	protected void fireFindInFilesEvent(FindInFilesEvent e) {

		// Guaranteed to return a non-null array
		Object[] listeners = eventListenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==FindInFilesListener.class) {
				((FindInFilesListener)listeners[i+1]).
									findInFilesFileSelected(e);
			}
		}

	}


	/**
	 * Returns localized text specific to Find in Files/Replace in Files.
	 * Localized text for general Find and Replace dialogs can be obtained
	 * via <code>getString()</code>.
	 *
	 * @param key The key for the text.
	 * @return The localized text.
	 */
	String getString2(String key) {
		return msg.getString(key);
	}


	/**
	 * Returns the text editor component for the specified combo box.
	 *
	 * @param combo The combo box.
	 * @return The text component.
	 */
	protected static final JTextComponent getTextComponent(JComboBox combo) {
		return org.fife.rsta.ui.UIUtil.getTextComponent(combo);
	}


	/**
	 * Returns whether to check subfolders.
	 *
	 * @return Whether or not to check subfolders.
	 * @see #getMatchCase
	 * @see #getMatchWholeWord
	 * @see #getUseRegEx
	 */
	boolean getCheckSubfolders() {
		return subfoldersCheckBox.isSelected();
	}


	/**
	 * Returns whether the user wants verbose output about their search.
	 *
	 * @return Whether the user wants verbose output about their search.
	 */
	boolean getDoVerboseOutput() {
		return verboseCheckBox.isSelected();
	}


	/**
	 * Returns the contents of the "In Files:" combo box.
	 *
	 * @return The contents.
	 */
	String getInFilesComboBoxContents() {
		return (String)inFilesComboBox.getSelectedItem();
	}


	/**
	 * Returns the length of the text in a text component.
	 *
	 * @param c The text component.
	 * @return The number of characters in that text component.
	 */
	protected static final int getLength(JTextComponent c) {
		return c.getDocument().getLength();
	}


	/**
	 * Returns whether matches should be case-sensitive.
	 *
	 * @return Whether or not matches should be case-sensitive.
	 * @see #getCheckSubfolders
	 * @see #getMatchWholeWord
	 * @see #getUseRegEx
	 */
	boolean getMatchCase() {
		return caseCheckBox.isSelected();
	}


	/**
	 * Returns whether matches should be whole word.
	 *
	 * @return Whether or not matches should be whole word.
	 * @see #getCheckSubfolders
	 * @see #getMatchCase
	 * @see #getUseRegEx
	 */
	boolean getMatchWholeWord() {
		return wholeWordCheckBox.isSelected();
	}


	/**
	 * Returns the component used to display results.
	 *
	 * @return The component.
	 */
	protected ResultsComponent getResultsComponent() {
		return resultsComponent;
	}


	/**
	 * Returns whether each line that matched the search criteria should be
	 * shown (as opposed to just a match count for each file).
	 *
	 * @return Whether or not each matched line should be shown.
	 */
	boolean getShowMatchingLines() {
		return matchingLinesRadioButton.isSelected();
	}


	/**
	 * Returns whether regular expressions should be used in searches.
	 *
	 * @return Whether or not regular expressions should be used in searches.
	 * @see #getCheckSubfolders
	 * @see #getMatchCase
	 * @see #getMatchWholeWord
	 */
	boolean getUseRegEx() {
		return regexCheckBox.isSelected();
	}


	/**
	 * Synchronizes access to our "worker" thread.
	 *
	 * @return The thread that is currently searching, or <code>null</code> if
	 *         no searching is going on.
	 */
	protected synchronized FindInFilesThread getWorkerThread() {
		return workerThread;
	}


	/**
	 * Returns whether any action-related buttons (Find Next, Replace, etc.)
	 * should be enabled.  Subclasses can call this method when the "Find What"
	 * or "Replace With" text fields are modified.  They can then
	 * enable/disable any components as appropriate.
	 *
	 * @return Whether the buttons should be enabled.
	 */
	@Override
	protected FindReplaceButtonsEnableResult handleToggleButtons() {

		FindReplaceButtonsEnableResult er = super.handleToggleButtons();
		boolean enable = er.getEnable();
		findButton.setEnabled(enable && isEverythingFilledIn());
		JTextComponent tc = getTextComponent(findTextCombo);
		tc.setForeground(enable ?
					UIManager.getColor("TextField.foreground") : Color.RED);

		String tooltip = er.getError();
		String status = defaultStatusText;
		if (tooltip!=null) {
			status = tooltip;
			if (status.indexOf('\n')>-1) {
				status = status.substring(0, status.indexOf('\n'));
			}
		}
		setStatusText(status);

		if (tooltip!=null && tooltip.indexOf('\n')>-1) {
			tooltip = tooltip.replaceFirst("\\\n", "</b><br><pre>");
			tooltip = "<html><b>" + tooltip;
		}
		tc.setToolTipText(tooltip); // Always set, even if null

		return er;

	}


	/**
	 * Returns whether everything in the UI that needs to be filled in for
	 * a search to be performed is filled in.
	 *
	 * @return Whether everything is filled in.
	 */
	protected boolean isEverythingFilledIn() {
		return getWorkerThread()==null && 
				getLength(getTextComponent(findTextCombo))>0 &&
				getLength(getTextComponent(inFilesComboBox))>0 &&
				getLength(inFolderTextField)>0;
	}


	/**
	 * Overridden to initialize UI elements specific to this subclass.
	 */
	@Override
	protected void refreshUIFromContext() {
		super.refreshUIFromContext();
		if (this.caseCheckBox==null) {
			return; // First time through, UI not realized yet
		}
		FindInFilesSearchContext fifsc = (FindInFilesSearchContext)context;
		subfoldersCheckBox.setSelected(fifsc.getSearchSubfolders());
		verboseCheckBox.setSelected(fifsc.getVerbose());
	}


	/**
	 * Removes a find-in-files listener to this find in files dialog.
	 *
	 * @param listener The listener to remove
	 * @see  #addFindInFilesListener
	 */
	public void removeFindInFilesListener(FindInFilesListener listener) {
		eventListenerList.remove(FindInFilesListener.class, listener);
	}


	/**
	 * Called by the searching thread when searching was terminated early for
	 * some reason.
	 *
	 * @param message A message describing why searching was terminated.
	 */
	void searchCompleted(String message) {
		setStatusText(message);
		searchCompleted(-1);
	}


	/**
	 * Called by the searching thread when searching has completed.
	 *
	 * @param time The time in milliseconds the search took.
	 */
	void searchCompleted(final long time) {

		SwingUtilities.invokeLater(new Runnable() { public void run() {

			setWorkerThread(null);

			// Return the cursor to the regular one.
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			// Re-enable the buttons since the user can do stuff again.
			setSearching(false);

			// If searching completed normally (e.g., wasn't terminated).
			if (time!=-1) {

				// Make the status bar indicate that searching completed.
				String temp = MessageFormat.format(searchingCompleteString,
								new Object[] { ""+(time/1000.0f) });
				setStatusText(temp);

				// Update the results list and notify the user if the
				// message wasn't found at all.
				if (getResultsComponent().getRowCount()==0) {
					String searchString = (String)findTextCombo.
												getSelectedItem();
					JOptionPane.showMessageDialog(FindInFilesDialog.this,
						getString2("SearchStringNotFound") +
												searchString + "'.",
						getString2("InfoDialogTitle"),
						JOptionPane.INFORMATION_MESSAGE);
				}

			}

			getResultsComponent().prettyUp();

		}});

	}


	/**
	 * Enables or disables widgets in the dialog as appropriate.
	 *
	 * @param searching Whether searching is starting.
	 */
	protected void setSearching(boolean searching) {
		boolean enabled = !searching;
		findButton.setEnabled(enabled);
		browseButton.setEnabled(enabled);
		if (searching) {
			cancelButton.setText(getString2("Stop"));
			cancelButton.setMnemonic((int)getString2("Stop.Mnemonic").charAt(0));
		}
		else {
			cancelButton.setText(getString2("Close"));
			cancelButton.setMnemonic((int)getString2("Close.Mnemonic").charAt(0));
		}
		findTextCombo.setEnabled(enabled);
		inFilesComboBox.setEnabled(enabled);
		inFolderTextField.setEnabled(enabled);
	}


	/**
	 * Sets the text in the status bar.
	 *
	 * @param text The text to display.
	 */
	public void setStatusText(final String text) {
		// Check whether dialog is visible in case search table is
		// docked on another window.
		if (isVisible()) {
			if (SwingUtilities.isEventDispatchThread()) {
				statusBar.setStatusMessage(text);
			}
			else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						statusBar.setStatusMessage(text);
					}
				});
			}
		}
	}


	/**
	 * Displays or hides the Find in Files dialog.  Note that you should use
	 * this method and not <code>show()</code> to properly display this dialog.
	 *
	 * @param visible Whether the dialog should be displayed or hidden.
	 */
	@Override
	public void setVisible(boolean visible) {

		refreshUIFromContext();
		super.setVisible(visible);

		// If they're making the dialog visible, make sure the status text
		// is "Ready" and not something left over from the last time the
		// dialog was visible.
		if (visible==true) {
			setStatusText(defaultStatusText);
		}

		// Give the "Find" text field focus.
		if (SwingUtilities.isEventDispatchThread()) {
			handleToggleButtons();
			findTextCombo.requestFocusInWindow();
			JTextComponent editor = getTextComponent(findTextCombo);
			editor.selectAll();
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					handleToggleButtons();
					findTextCombo.requestFocusInWindow();
					JTextComponent editor = getTextComponent(findTextCombo);
					editor.selectAll();
				}
			});
		}

	}


	/**
	 * Synchronizes access to our "worker" thread.
	 *
	 * @param workerThread The new worker thread.
	 * @see #getWorkerThread
	 */
	private synchronized void setWorkerThread(FindInFilesThread thread) {
		this.workerThread = thread;
	}


	/**
	 * Called whenever the user changes the Look and Feel, etc.
	 * This is overridden so we can reinstate the listeners that are evidently
	 * lost on the JTextField portion of our combo box.
	*/
	public void updateUI() {

		// Create listeners for the combo boxes.
		FindInFilesFocusAdapter focusAdapter = new FindInFilesFocusAdapter();
		FindInFilesKeyListener keyListener = new FindInFilesKeyListener();

		// Fix the Find What combo box's listeners.
		JTextComponent textField = getTextComponent(findTextCombo);
		textField.addFocusListener(focusAdapter);
		textField.addKeyListener(keyListener);
		textField.getDocument().addDocumentListener(docListener);

		// Fix the In Files combo box's listeners.
		textField = getTextComponent(inFilesComboBox);
		textField.addFocusListener(focusAdapter);
		textField.addKeyListener(keyListener);
		textField.getDocument().addDocumentListener(docListener);

		// Fix the In Folders combo box's listeners.
		inFolderTextField.addFocusListener(focusAdapter);
		inFolderTextField.getDocument().addDocumentListener(docListener);

	}


	/**
	 * Listens for changes in the "Find what" text field.
	 */
	private class FindInFilesDocumentListener implements DocumentListener {

		public void insertUpdate(DocumentEvent e) {
			handleToggleButtons();
		}

		public void removeUpdate(DocumentEvent e) {
			handleToggleButtons();
		}

		public void changedUpdate(DocumentEvent e) {
		}

	}


	/**
	 * Listens for the text field gaining focus.
	 */
	protected class FindInFilesFocusAdapter extends FocusAdapter {

		@Override
		public void focusGained(FocusEvent e) {

			Component component = e.getComponent();
			((JTextField)component).selectAll();

			// Remember what it originally was, in case they tabbed out.
			if (component==getTextComponent(findTextCombo))
				lastSearchString = (String)findTextCombo.getSelectedItem();
			else if (component==getTextComponent(inFilesComboBox))
				lastInFilesString = (String)inFilesComboBox.getSelectedItem();

		}

	}


	/**
	 * Listens for the user to double-click on the results JList.  This class
	 * is what sends out <code>FindInFilesEvent</code>s.
	 */
	class FindInFilesDialogMouseListener extends MouseAdapter {

		ResultsComponent comp;

		FindInFilesDialogMouseListener(ResultsComponent comp) {
			this.comp = comp;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==2) {
				int row = comp.getSelectedRow();
				if (row==-1)
					return;
				MatchData data = comp.getMatchDataForRow(row);
				String fileName = data.getFileName();
				// Might be a directory if Verbose is enabled.
				if (!(new File(fileName).isFile())) {
					UIManager.getLookAndFeel().provideErrorFeedback(null);
					return;
				}
				String lineStr = data.getLineNumber();
				int line = -1;
				if (!FindInFilesThread.NO_LINE_NUMBER.equals(lineStr)) {
					// Should be in format "3" or "5-7".
					if (lineStr.indexOf('-')>-1) {
						lineStr = lineStr.substring(0, lineStr.indexOf('-'));
					}
					try {
						line = Integer.parseInt(lineStr);
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
				fireFindInFilesEvent(new FindInFilesEvent(
							FindInFilesDialog.this, fileName, line));
			}
		}

	}


	/**
	 * Listens for key presses in the Find In Files dialog.
	 */
	private class FindInFilesKeyListener extends KeyAdapter {

		@Override
		public void keyReleased(KeyEvent e) {

			// This is an ugly hack to get around JComboBox's
			// insistence on eating the first Enter keypress
			// it receives when it has focus and its selected item
			// has changed since the last time it lost focus.
			if (e.getKeyCode()==KeyEvent.VK_ENTER && isPreJava6JRE()) {

				Object source = e.getSource();

				// If they pressed Enter in the 'Find What' combo box.
				if (source==getTextComponent(findTextCombo)) {
					String inFilesString = (String)inFilesComboBox.getSelectedItem();
					lastInFilesString = inFilesString;	// Just in case it changed too.
					String searchString = (String)findTextCombo.getSelectedItem();
					if (!searchString.equals(lastSearchString)) {
						findButton.doClick(0);
						lastSearchString = searchString;
						getTextComponent(findTextCombo).selectAll();
					}
				}

				// If they pressed enter in the 'In Files' combo box.
				else if (source==getTextComponent(inFilesComboBox)) {
					String searchString = (String)findTextCombo.getSelectedItem();
					lastSearchString = searchString;	// Just in case it changed too.
					String inFilesString = (String)inFilesComboBox.getSelectedItem();
					if (!inFilesString.equals(lastInFilesString)) {
						findButton.doClick(0);
						lastInFilesString = inFilesString;
						getTextComponent(inFilesComboBox).selectAll();
					}
				}

			}

		}

	}


}