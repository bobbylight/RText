/*
 * 03/01/2004
 *
 * FileFilterOptionPanel.java - Option panel letting the user configure
 * what extensions get opened with what syntax highlighting scheme.
 * Copyright (C) 2004 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
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
package org.fife.rtext;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RButton;
import org.fife.ui.UIUtil;
import org.fife.ui.modifiabletable.*;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Option panel letting the user configure what files get opened with what
 * syntax highlighting scheme.
 *
 * @author Robert Futrell
 * @version 0.2
 */
class FileFilterOptionPanel extends OptionsDialogPanel
					implements ActionListener, ModifiableTableListener {

	private ModifiableTable filterTable;
	private FilterTableModel model;
	private JCheckBox guessTypeCB;
	private RText rtext;

	private static final String DEFAULTS_RESTORED	= "defaultsRestored";


	/**
	 * Constructor.
	 *
	 * @param rtext The RText instance.
	 * @param msg The resource bundle to use.
	 */
	public FileFilterOptionPanel(final RText rtext, final ResourceBundle msg) {

		super(msg.getString("OptFFName"));
		this.rtext = rtext;

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(
					new OptionPanelBorder(msg.getString("OptFFLabel")));
		add(contentPane);

		JLabel desc = new JLabel(msg.getString("OptFFDesc"));
		JPanel temp = new JPanel(new BorderLayout());
		temp.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		temp.add(desc, BorderLayout.LINE_START);
		contentPane.add(temp, BorderLayout.NORTH);
		model = new FilterTableModel(msg.getString("OptFFCol1"), msg.getString("OptFFCol2"));
		filterTable = new ModifiableTable(model, ModifiableTable.BOTTOM, ModifiableTable.MODIFY);
		filterTable.setRowHandler(new FileFilterRowHandler());
		filterTable.addModifiableTableListener(this);
		JTable table = filterTable.getTable();
		table.setPreferredScrollableViewportSize(new Dimension(300,300));
		contentPane.add(filterTable);

		JPanel bottomPanel = new JPanel(new BorderLayout());

		guessTypeCB = new JCheckBox(msg.getString("GuessContentType"));
		guessTypeCB.setActionCommand("GuessContentType");
		guessTypeCB.addActionListener(this);
		temp = new JPanel(new BorderLayout());
		temp.add(guessTypeCB, BorderLayout.LINE_START);
		temp.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		bottomPanel.add(temp, BorderLayout.NORTH);
		
		RButton defaultsButton = new RButton(msg.getString("RestoreDefaults"));
		defaultsButton.setActionCommand("RestoreDefaults");
		defaultsButton.addActionListener(this);
		temp = new JPanel(new BorderLayout());
		temp.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		temp.add(defaultsButton, BorderLayout.LINE_START);
		bottomPanel.add(temp, BorderLayout.SOUTH);

		add(bottomPanel, BorderLayout.SOUTH);
		applyComponentOrientation(orientation);

	}


	/**
	 * Listens for actions in this panel.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if ("RestoreDefaults".equals(command)) {
			// Empty constructor returns defaults.
			SyntaxFilters defaultFilters = new SyntaxFilters();
			boolean changed = setSyntaxFilters(defaultFilters);
			if (changed || !guessTypeCB.isSelected()) {
				guessTypeCB.setSelected(true);
				hasUnsavedChanges = true;
				firePropertyChange(DEFAULTS_RESTORED,
								Boolean.FALSE,Boolean.TRUE);
			}
		}

		else if ("GuessContentType".equals(command)) {
			hasUnsavedChanges = true;
			firePropertyChange(DEFAULTS_RESTORED, false, true);
		}

	}


	/**
	 * Applies the settings entered into this dialog on the specified
	 * application.
	 *
	 * @param owner The application.
	 */
	protected void doApplyImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		mainView.setSyntaxFilters(getSyntaxFilters());
		mainView.setGuessFileContentType(guessTypeCB.isSelected());
	}


	/**
	 * {@inheritDoc}
	 */
	protected OptionsPanelCheckResult ensureValidInputsImpl() {

		// Make sure each filter value in the table is valid.
		int rowCount = model.getRowCount();
		for (int i=0; i<rowCount; i++) {
			String filters = (String)model.getValueAt(i, 1);
			if (SyntaxFilters.isValidFileFilterString(filters)==false) {
				String temp = rtext.getString("InvalidFFString", filters);
				return new OptionsPanelCheckResult(this, filterTable, temp);
			}
		}

		// Otherwise, the input was okay.
		return null;

	}


	/**
	 * Returns the syntax file filters accepted by the user.
	 *
	 * @return The syntax file filters.
	 */
	public SyntaxFilters getSyntaxFilters() {
		SyntaxFilters filters = new SyntaxFilters();
		for (int i=0; i<model.styles.length; i++) {
			filters.setFiltersForSyntaxStyle(model.styles[i],
									(String)model.getValueAt(i,1));
		}
		return filters;
	}


	/**
	 * Returns the <code>JComponent</code> at the "top" of this Options
	 * panel.  This is the component that will receive focus if the user
	 * switches to this Options panel in the Options dialog.  As an added
	 * bonus, if this component is a <code>JTextComponent</code>, its
	 * text is selected for easy changing.
	 */
	public JComponent getTopJComponent() {
		return filterTable;
	}


	/**
	 * Called whenever the extension/color mapping table is changed.
	 *
	 * @param e An event describing the change.
	 */
	public void modifiableTableChanged(ModifiableTableChangeEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange("fileFilterChanged", null,
						new Integer(e.getRow()));
	}


	/**
	 * Sets the syntax filters displayed for each file type.
	 *
	 * @param syntaxFilters the filters for each type.
	 * @return Whether any of the values actually changed.
	 * @see SyntaxFilters
	 */
	public boolean setSyntaxFilters(SyntaxFilters syntaxFilters) {

		String filterString;
		boolean changed = false;

		for (int i=0; i<model.styles.length; i++) {
			String style = model.styles[i];
			filterString = syntaxFilters.getFilterString(style);
			String old = (String)model.getValueAt(i, 1);
			if (!filterString.equals(old)) {
				model.setValueAt(filterString, i,1);
				changed = true;
			}
		}

		return changed;

	}


	/**
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are not handled.
	 *
	 * @param owner The parent application.
	 * @see #setValues(Frame)
	 */
	protected void setValuesImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		setSyntaxFilters(mainView.getSyntaxFilters());
		guessTypeCB.setSelected(mainView.getGuessFileContentType());
	}


	/**
	 * Table data for the "File filter" table.
	 */
	static class FilterTableModel extends DefaultTableModel
							implements SyntaxConstants {

		public String[] columnNames;
		public String[] styles;

		public FilterTableModel(String fileTypeHeader, String filterHeader) {
			super(new Object[] { fileTypeHeader, filterHeader }, 0);
			styles = new String[25];
			addRow(new Object[] { "Assembler (x86)",	null }); styles[0]  = SYNTAX_STYLE_ASSEMBLER_X86;
			addRow(new Object[] { "C",					null }); styles[1]  = SYNTAX_STYLE_C;
			addRow(new Object[] { "C++",				null }); styles[2]  = SYNTAX_STYLE_CPLUSPLUS;
			addRow(new Object[] { "C#",					null }); styles[3]  = SYNTAX_STYLE_CSHARP;
			addRow(new Object[] { "CSS",				null }); styles[4]  = SYNTAX_STYLE_CSS;
			addRow(new Object[] { "Delphi",				null }); styles[5]  = SYNTAX_STYLE_DELPHI;
			addRow(new Object[] { "Fortran",			null }); styles[6]  = SYNTAX_STYLE_FORTRAN;
			addRow(new Object[] { "Groovy",				null }); styles[7]  = SYNTAX_STYLE_GROOVY;
			addRow(new Object[] { "HTML",				null }); styles[8]  = SYNTAX_STYLE_HTML;
			addRow(new Object[] { "Java",				null }); styles[9]  = SYNTAX_STYLE_JAVA;
			addRow(new Object[] { "JavaScript",			null }); styles[10] = SYNTAX_STYLE_JAVASCRIPT;
			addRow(new Object[] { "JSP",				null }); styles[11] = SYNTAX_STYLE_JSP;
			addRow(new Object[] { "Lisp",				null }); styles[12] = SYNTAX_STYLE_LISP;
			addRow(new Object[] { "Lua",				null }); styles[13] = SYNTAX_STYLE_LUA;
			addRow(new Object[] { "Make",				null }); styles[14] = SYNTAX_STYLE_MAKEFILE;
			addRow(new Object[] { "Perl",				null }); styles[15] = SYNTAX_STYLE_PERL;
			addRow(new Object[] { "PHP",				null }); styles[16] = SYNTAX_STYLE_PHP;
			addRow(new Object[] { "Properties files",	null }); styles[17] = SYNTAX_STYLE_PROPERTIES_FILE;
			addRow(new Object[] { "Python",				null }); styles[18] = SYNTAX_STYLE_PYTHON;
			addRow(new Object[] { "Ruby",				null }); styles[19] = SYNTAX_STYLE_RUBY;
			addRow(new Object[] { "SAS",				null }); styles[20] = SYNTAX_STYLE_SAS;
			addRow(new Object[] { "Scala",				null }); styles[21] = SYNTAX_STYLE_SCALA;
			addRow(new Object[] { "SQL",				null }); styles[22] = SYNTAX_STYLE_SQL;
			addRow(new Object[] { "Tcl",				null }); styles[23] = SYNTAX_STYLE_TCL;
			addRow(new Object[] { "UNIX shell scripts",	null }); styles[24] = SYNTAX_STYLE_UNIX_SHELL;
			addRow(new Object[] { "Windows Batch",		null }); styles[25] = SYNTAX_STYLE_WINDOWS_BATCH;
			addRow(new Object[] { "XML",				null }); styles[26] = SYNTAX_STYLE_XML;
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}

	}


	/**
	 * Handles the addition, removal, and modifying of rows in
	 * the file filter table.
	 */
	private class FileFilterRowHandler implements RowHandler {

		public Object[] getNewRowInfo(Object[] oldData) {
			String oldValue = (String)oldData[1];
			String filterType = (String)oldData[0];
			String input;
			while (true) {
				String prompt = rtext.getString("FileFilterPrompt", filterType);
				input = JOptionPane.showInputDialog(
					FileFilterOptionPanel.this, prompt, oldValue);
				if (input!=null) {
					if (!SyntaxFilters.isValidFileFilterString(input)) {
						String temp = rtext.getString("InvalidFFString",
												input);
						JOptionPane.showMessageDialog(
									FileFilterOptionPanel.this, temp,
									rtext.getString("ErrorDialogTitle"),
									JOptionPane.ERROR_MESSAGE);
						continue;
					}
				}
				break;
			} // End of while (true).
			if (input!=null) {
				return new Object[] { oldData[0], input };
			}
			return null;
		}

		public boolean shouldRemoveRow(int row) {
			return false; // Cannot remove any rows.
		}

		/**
		 * Not an override.  Implements <code>RowHandler#updateUI()</code>.
		 */
		public void updateUI() {
			// Nothing to update.
		}

	}


}