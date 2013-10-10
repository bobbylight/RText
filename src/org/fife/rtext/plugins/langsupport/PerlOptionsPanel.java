/*
 * 05/20/2010
 *
 * PerlOptionsPanel.java - Options for Perl language support.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import org.fife.rsta.ac.IOUtil;
import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.perl.PerlLanguageSupport;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.ui.EscapableDialog;
import org.fife.ui.FSATextField;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;
import org.fife.ui.modifiabletable.AbstractRowHandler;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.ModifiableTableChangeEvent;
import org.fife.ui.modifiabletable.ModifiableTableListener;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextfilechooser.RDirectoryChooser;


/**
 * The options panel for Perl-specific language support options.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class PerlOptionsPanel extends OptionsDialogPanel {

	private Listener listener;
	private JCheckBox enabledCB;
	private JCheckBox paramAssistanceCB;
	private JCheckBox showDescWindowCB;
	private JCheckBox useParensCB;
	private JCheckBox compileCB;
	private JCheckBox foldingEnabledCB;
	private JLabel installLocLabel;
	private FSATextField installLocField;
	private JButton installBrowseButton;
	private JCheckBox warningsCB;
	private JCheckBox taintModeCB;
	private JCheckBox overridePerl5LibCB;
	private ModifiableTable perl5Table;
	private JButton rdButton;

	private static final String PERL5LIB		= "PERL5LIB";
	private static final String PROPERTY		= "Property";


	/**
	 * Constructor.
	 */
	public PerlOptionsPanel() {

		ResourceBundle msg = Plugin.msg;
		setName(msg.getString("Options.Perl.Name"));
		listener = new Listener();
		setIcon(new ImageIcon(RText.class.getResource("graphics/file_icons/epic.gif")));

		ComponentOrientation o = ComponentOrientation.
											getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());

		Box cp = Box.createVerticalBox();
		cp.setBorder(null);
		add(cp, BorderLayout.NORTH);

		Box box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(msg.
				getString("Options.General.Section.Folding")));
		cp.add(box);
		cp.add(Box.createVerticalStrut(5));

		foldingEnabledCB = createCB("Options.General.EnableCodeFolding");
		addLeftAligned(box, foldingEnabledCB);

		box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(msg.
				getString("Options.Perl.Section.CodeCompletion")));
		cp.add(box);
		cp.add(Box.createVerticalStrut(5));

		enabledCB = createCB("Options.General.EnableCodeCompletion");
		addLeftAligned(box, enabledCB, 3);

		Box box2 = Box.createVerticalBox();
		if (o.isLeftToRight()) {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		}
		else {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
		}
		box.add(box2);

		showDescWindowCB = createCB("Options.General.ShowDescWindow");
		addLeftAligned(box2, showDescWindowCB, 3);

		paramAssistanceCB = createCB("Options.General.ParameterAssistance");
		addLeftAligned(box2, paramAssistanceCB, 3);

		useParensCB = createCB("UseParensInCodeCompletion");
		addLeftAligned(box2, useParensCB);

		box2.add(Box.createVerticalGlue());

		box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(msg.
				getString("Options.Perl.Section.SyntaxChecking")));
		cp.add(box);

		compileCB = createCB("Options.General.UnderlineErrors");
		addLeftAligned(box, compileCB, 3);

		box2 = Box.createVerticalBox();
		if (o.isLeftToRight()) {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		}
		else {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
		}
		box.add(box2);

		installLocLabel = new JLabel(msg.getString("Options.Perl.InstallLoc"));
		installLocField = new FSATextField(true, "");
		installLocField.getDocument().addDocumentListener(listener);
		installLocLabel.setLabelFor(installLocField);
		installBrowseButton = new JButton(msg.getString("Browse"));
		installBrowseButton.addActionListener(listener);
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(installLocLabel, BorderLayout.LINE_START);
		temp.add(installLocField);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(installBrowseButton);
		temp.add(temp2, BorderLayout.LINE_END);
		if (o.isLeftToRight()) {
			installLocLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			temp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		}
		else {
			installLocLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			temp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		}
		box2.add(temp);
		box2.add(Box.createVerticalStrut(3));
		warningsCB = createCB("Warnings");
		taintModeCB = createCB("TaintMode");
		Box box3 = createHorizontalBox();
		box3.add(warningsCB);
		box3.add(Box.createHorizontalStrut(40));
		box3.add(taintModeCB);
		box3.add(Box.createHorizontalGlue());
		addLeftAligned(box2, box3);
		box2.add(Box.createVerticalGlue());

		box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(msg.
				getString("Options.Perl.Section.Perl5Lib")));
		cp.add(box);
		overridePerl5LibCB = createCB("OverridePerl5Lib");
		addLeftAligned(box, overridePerl5LibCB, 5);
		DefaultTableModel model = new DefaultTableModel(0, 1);
		perl5Table = new ModifiableTable(model, ModifiableTable.BOTTOM,
										ModifiableTable.ALL_BUTTONS);
		perl5Table.addModifiableTableListener(listener);
		perl5Table.setRowHandler(new Perl5LibTableRowHandler());
		perl5Table.getTable().setTableHeader(null);
		Dimension s = perl5Table.getTable().getPreferredScrollableViewportSize();
		s.height = 110; // JTable default is 400!
		perl5Table.getTable().setPreferredScrollableViewportSize(s);
		box.add(perl5Table);

		cp.add(Box.createVerticalStrut(10));

		rdButton = new JButton(msg.getString("Options.General.RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(cp, rdButton);

//		cp.add(Box.createVerticalGlue());

		applyComponentOrientation(o);

	}


	private JCheckBox createCB(String key) {
		if (key.indexOf('.')==-1) {
			key = "Options.Perl." + key;
		}
		JCheckBox cb = new JCheckBox(Plugin.msg.getString(key));
		cb.addActionListener(listener);
		return cb;
	}


	/**
	 * Creates a path separator-separated string to use for the
	 * <code>PERL5LIB</code> environment variable, based on the values entered
	 * into the table.
	 *
	 * @return The new <code>PERL5LIB</code> value.
	 */
	private String createPerl5LibFromTable() {
		StringBuilder sb = new StringBuilder();
		int rowCount = perl5Table.getTable().getRowCount();
		for (int row=0; row<rowCount; row++) {
			sb.append(perl5Table.getTable().getValueAt(row, 0));
			if (row<rowCount-1) {
				sb.append(File.pathSeparatorChar);
			}
		}
		return sb.toString();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doApplyImpl(Frame owner) {

		RText app = (RText)owner;
		AbstractMainView view = app.getMainView();

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		final String language = SyntaxConstants.SYNTAX_STYLE_PERL;
		LanguageSupport ls = lsf.getSupportFor(language);
		PerlLanguageSupport pls = (PerlLanguageSupport)ls;

		// Code folding options
		view.setCodeFoldingEnabledFor(language, foldingEnabledCB.isSelected());

		// Options dealing with code completion.
		pls.setAutoCompleteEnabled(enabledCB.isSelected());
		pls.setParameterAssistanceEnabled(paramAssistanceCB.isSelected());
		pls.setShowDescWindow(showDescWindowCB.isSelected());
		pls.setUseParensWithFunctions(useParensCB.isSelected());

		// Options dealing with runtime syntax checking.
		boolean old = pls.isParsingEnabled();
		pls.setParsingEnabled(compileCB.isSelected());
		boolean reunderline = old!=pls.isParsingEnabled();
		File oldLoc = PerlLanguageSupport.getPerlInstallLocation();
		File newLoc = installLocField.getText().length()>0 ?
				new File(installLocField.getText()) : null;
		PerlLanguageSupport.setPerlInstallLocation(newLoc);
		reunderline |= (oldLoc==null && newLoc!=null) ||
						(oldLoc!=null && !oldLoc.equals(newLoc));
		old = pls.getWarningsEnabled();
		pls.setWarningsEnabled(warningsCB.isSelected());
		reunderline |= old!=pls.getWarningsEnabled();
		old = pls.isTaintModeEnabled();
		pls.setTaintModeEnabled(taintModeCB.isSelected());
		reunderline |= old!=pls.isTaintModeEnabled();

		// Overriding the PERL5LIB environment variable.
		if (overridePerl5LibCB.isSelected()) {
			String perl5Lib = createPerl5LibFromTable();
			if (!perl5Lib.equals(pls.getPerl5LibOverride())) {
				pls.setPerl5LibOverride(perl5Lib);
				reunderline = true;
			}
		}
		else {
			if (pls.getPerl5LibOverride()!=null) {
				pls.setPerl5LibOverride(null); // => use default
				reunderline = true;
			}
		}

		// Something in the way we compile the Perl code changed
		if (reunderline) {
			for (int i=0; i<view.getNumDocuments(); i++) {
				RTextEditorPane textArea = view.getRTextEditorPaneAt(i);
				textArea.forceReparsing(pls.getParser(textArea));
			}
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {

		OptionsPanelCheckResult res = null;

		String text = installLocField.getText();
		if (text.length()>0) { // Empty field == okay
			File file = new File(text);
			if (!file.isDirectory()) {
				res = new OptionsPanelCheckResult(this, installLocField, 
					Plugin.msg.getString("Options.Perl.Error.InvalidPerlHome"));
			}
		}

		return res;

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public JComponent getTopJComponent() {
		return foldingEnabledCB;
	}


	private void setCompileCBSelected(boolean selected) {
		compileCB.setSelected(selected);
		installLocLabel.setEnabled(selected);
		installLocField.setEnabled(selected);
		installBrowseButton.setEnabled(selected);
		warningsCB.setEnabled(selected);
		taintModeCB.setEnabled(selected);
	}


	private void setEnabledCBSelected(boolean selected) {
		enabledCB.setSelected(selected);
		paramAssistanceCB.setEnabled(selected);
		showDescWindowCB.setEnabled(selected);
		useParensCB.setEnabled(selected);
	}


	/**
	 * Populates the <code>PERL5LIB</code> override table with the value
	 * specified.
	 *
	 * @param contents The new contents.  If this is <code>null</code>, the
	 *        table will be empty.  Otherwise, this will be split on the
	 *        system-specific path separator, and the resulting tokens will be
	 *        added to the table.
	 */
	private void setPerl5TableContents(String contents) {

		DefaultTableModel model = (DefaultTableModel)perl5Table.getTable().
																getModel();
		model.setRowCount(0);

		if (contents!=null) {
			String[] items = contents.split(";");
			for (int i=0; i<items.length; i++) {
				model.addRow(new Object[] { items[i] });
			}
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setValuesImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		final String language = SyntaxConstants.SYNTAX_STYLE_PERL;
		LanguageSupport ls=lsf.getSupportFor(language);
		PerlLanguageSupport pls = (PerlLanguageSupport)ls;

		// Code folding options
		AbstractMainView view = ((RText)owner).getMainView();
		foldingEnabledCB.setSelected(view.isCodeFoldingEnabledFor(language));

		// Options dealing with code completion
		setEnabledCBSelected(pls.isAutoCompleteEnabled());
		paramAssistanceCB.setSelected(pls.isParameterAssistanceEnabled());
		showDescWindowCB.setSelected(pls.getShowDescWindow());
		useParensCB.setSelected(pls.getUseParensWithFunctions());

		// Options dealing with runtime syntax checking
		setCompileCBSelected(pls.isParsingEnabled());
		installLocField.setFileSystemAware(false);
		File installLoc = PerlLanguageSupport.getPerlInstallLocation();
		String path = installLoc==null ? null : installLoc.getAbsolutePath();
		installLocField.setText(path);
		installLocField.setFileSystemAware(true);
		warningsCB.setSelected(pls.getWarningsEnabled());
		taintModeCB.setSelected(pls.isTaintModeEnabled());

		String override = pls.getPerl5LibOverride();
		if (override==null) {
			overridePerl5LibCB.setSelected(false);
			perl5Table.setEnabled(false);
			setPerl5TableContents(IOUtil.getEnvSafely(PERL5LIB));
		}
		else {
			overridePerl5LibCB.setSelected(true);
			perl5Table.setEnabled(true);
			setPerl5TableContents(override);
		}

	}


	/**
	 * Listens for events in this options panel.
	 */
	private class Listener implements ActionListener, DocumentListener,
							ModifiableTableListener {

		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (enabledCB==source) {
				// Trick related components to toggle enabled states
				setEnabledCBSelected(enabledCB.isSelected());
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (paramAssistanceCB==source ||
					showDescWindowCB==source ||
					useParensCB==source ||
					warningsCB==source ||
					taintModeCB==source) {
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (compileCB==source) {
				// Trick related components to toggle enabled states
				setCompileCBSelected(compileCB.isSelected());
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (installBrowseButton==source) {
				RDirectoryChooser chooser = new RDirectoryChooser(
														getOptionsDialog());
				chooser.setVisible(true);
				String dir = chooser.getChosenDirectory();
				if (dir!=null && !dir.equals(installLocField.getText())) {
					installLocField.setFileSystemAware(false);
					installLocField.setText(dir);
					installLocField.setFileSystemAware(true);
					hasUnsavedChanges = true;
					firePropertyChange(PROPERTY, null, null);
				}
			}

			else if (overridePerl5LibCB==source) {
				boolean enabled = overridePerl5LibCB.isSelected();
				perl5Table.setEnabled(enabled);
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (rdButton==source) {

				File locFile = PerlLanguageSupport.
											getDefaultPerlInstallLocation();
				String origInstallLoc = locFile==null ? "" :
										locFile.getAbsolutePath();
				String installFieldText = installLocField.getText();
				boolean installLocFieldModified =
					!origInstallLoc.equals(installFieldText);

				if (!foldingEnabledCB.isSelected() ||
						!compileCB.isSelected() ||
						!enabledCB.isSelected() ||
						installLocFieldModified ||
						!paramAssistanceCB.isSelected() ||
						!showDescWindowCB.isSelected() ||
						taintModeCB.isSelected() ||
						!useParensCB.isSelected() ||
						!warningsCB.isSelected() ||
						overridePerl5LibCB.isSelected()) {
					foldingEnabledCB.setSelected(true);
					setCompileCBSelected(true);
					setEnabledCBSelected(true);
					installLocField.setFileSystemAware(false);
					installLocField.setText(origInstallLoc);
					installLocField.setFileSystemAware(true);
					paramAssistanceCB.setSelected(true);
					showDescWindowCB.setSelected(true);
					taintModeCB.setSelected(false);
					useParensCB.setSelected(true);
					warningsCB.setSelected(true);
					overridePerl5LibCB.setSelected(false);
					perl5Table.setEnabled(false);
					setPerl5TableContents(IOUtil.getEnvSafely(PERL5LIB));
					hasUnsavedChanges = true;
					firePropertyChange(PROPERTY, null, null);
				}

			}

		}

		public void changedUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		private void handleDocumentEvent(DocumentEvent e) {
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, null, null);
		}

		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		public void modifiableTableChanged(ModifiableTableChangeEvent e) {
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, null, null);
		}

		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

	}


	/**
	 * The row handler for the PERL5LIB table.
	 */
	private class Perl5LibTableRowHandler extends AbstractRowHandler {

		public Object[] getNewRowInfo(Object[] oldData) {
			String oldValue = oldData==null ? null : (String)oldData[0];
			Perl5ItemDialog dialog = new Perl5ItemDialog(getOptionsDialog());
			dialog.setPath(oldValue);
			int rc = dialog.showDialog();
			if (rc==Perl5ItemDialog.OK) {
				String newValue = dialog.getPath();
				return new String[] { newValue };
			}
			return null;
		}

	}


	/**
	 * The dialog that allows the user to add or modify an item to go into
	 * the PERL5LIB environment variable.
	 */
	private static class Perl5ItemDialog extends EscapableDialog
				implements ActionListener, DocumentListener {

		static final int OK		= 0;
		static final int CANCEL	= 1;

		private JTextField field;
		private JButton okButton;
		private JButton cancelButton;
		private int rc;

		public Perl5ItemDialog(JDialog owner) {

			super(owner);
			ComponentOrientation orientation = ComponentOrientation.
										getOrientation(getLocale());
			JPanel contentPane = new ResizableFrameContentPane(
											new BorderLayout());
			contentPane.setBorder(UIUtil.getEmpty5Border());

			// Panel containing main stuff.
			Box topPanel = Box.createVerticalBox();
			JPanel temp = new JPanel(new BorderLayout());
			temp.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
			JLabel label = UIUtil.newLabel(Plugin.msg,
								"Options.Perl.Perl5LibItem");
			JPanel temp2 = new JPanel(new BorderLayout());
			temp2.add(label);
			if (orientation.isLeftToRight()) { // Space between label and text field.
				temp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
			}
			else {
				temp2.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
			}
			temp.add(temp2, BorderLayout.LINE_START);
			field = new JTextField(40);
			field.getDocument().addDocumentListener(this);
			label.setLabelFor(field);
			temp.add(field);
			JButton browseButton = new JButton(Plugin.msg.getString("Browse"));
			browseButton.setActionCommand("Browse");
			browseButton.addActionListener(this);
			temp2 = new JPanel(new BorderLayout());
			temp2.add(browseButton);
			if (orientation.isLeftToRight()) { // Space between text field and button.
				temp2.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
			}
			else {
				temp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
			}
			temp.add(temp2, BorderLayout.LINE_END);
			topPanel.add(temp);
			contentPane.add(topPanel, BorderLayout.NORTH);

			// Panel containing buttons for the bottom.
			okButton = new JButton(Plugin.msg.getString("Options.General.OK"));
			okButton.addActionListener(this);
			cancelButton = new JButton(Plugin.msg.
										getString("Options.General.Cancel"));
			cancelButton.addActionListener(this);
			Container buttons=UIUtil.createButtonFooter(okButton, cancelButton);
			contentPane.add(buttons, BorderLayout.SOUTH);

			// Get ready to go.
			setTitle(Plugin.msg.
					getString("Options.Perl.Perl5LibItem.DialogTitle"));
			setContentPane(contentPane);
			getRootPane().setDefaultButton(okButton);
			setModal(true);
			applyComponentOrientation(orientation);
			pack();

		}

		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source==okButton) {
				rc = OK;
				escapePressed();
			}
			else if (source==cancelButton) {
				escapePressed();
			}
			else {
				String command = e.getActionCommand();
				if ("Browse".equals(command)) {
					RDirectoryChooser chooser =
						new RDirectoryChooser((JDialog)getOwner());
					chooser.setChosenDirectory(new File(getPath()));
					chooser.setVisible(true);
					String chosenDir = chooser.getChosenDirectory();
					if (chosenDir!=null) {
						field.setText(chosenDir);
					}
				}
			}
		}

		public void changedUpdate(DocumentEvent e) {
		}

		public String getPath() {
			return field.getText();
		}

		public void insertUpdate(DocumentEvent e) {
			okButton.setEnabled(true);
		}

		public void removeUpdate(DocumentEvent e) {
			okButton.setEnabled(field.getDocument().getLength()>0);
		}

		public void setPath(String path) {
			field.setText(path);
		}

		public int showDialog() {
			rc = CANCEL; // Set here in case they "X" the dialog out.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					field.requestFocusInWindow();
					field.selectAll();
				}
			});
			setLocationRelativeTo(getOwner());
			okButton.setEnabled(false);
			setVisible(true);
			return rc;
		}

	}


}