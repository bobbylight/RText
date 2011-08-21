/*
 * 07/23/2011
 *
 * MacroOptionPanel.java - Option panel for managing external macros.
 * Copyright (C) 2011 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This file is a part of RText.
 *
 * RText is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.rtext.plugins.macros;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.fife.rtext.KeyStrokeCellRenderer;
import org.fife.rtext.RText;
import org.fife.rtext.plugins.macros.NewMacroDialog;
import org.fife.rtext.plugins.macros.Macro;
import org.fife.ui.UIUtil;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.ModifiableTableChangeEvent;
import org.fife.ui.modifiabletable.ModifiableTableListener;
import org.fife.ui.modifiabletable.RowHandler;


/**
 * Options panel for managing external macros.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class MacroOptionPanel extends PluginOptionsDialogPanel
						implements ModifiableTableListener {

	private DefaultTableModel model;
	private ModifiableTable macroTable;

	private static final String PROPERTY		= "property";
	static final String TITLE_KEY				= "Plugin.Name";


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin.
	 */
	public MacroOptionPanel(MacroPlugin plugin) {

		super(plugin);

		setName(plugin.getString(TITLE_KEY));

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		JPanel cp = new JPanel(new BorderLayout());
		cp.setBorder(new OptionPanelBorder(plugin.getString("Options.Section.MacroList")));
		add(cp);

		model = new DefaultTableModel(new String[] {
				plugin.getString("Options.TableHeader.Macro"),
				plugin.getString("Options.TableHeader.Shortcut"),
				plugin.getString("Options.TableHeader.Description") }, 0);

		List customButtons = new ArrayList();
		customButtons.add(new AddExampleMacrosAction(plugin));

		macroTable = new ModifiableTable(model, ModifiableTable.BOTTOM,
										ModifiableTable.ADD_REMOVE_MODIFY,
										customButtons);
		macroTable.addModifiableTableListener(this);
		macroTable.setRowHandler(new MacroTableRowHandler());
		JTable table = macroTable.getTable();
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(1).setCellRenderer(new KeyStrokeCellRenderer());
		table.setPreferredScrollableViewportSize(new Dimension(300,300));
		cp.add(macroTable);

		applyComponentOrientation(orientation);

	}


	/**
	 * Adds a row to the table in this options panel for a macro.
	 *
	 * @param macro The macro to add to the table.
	 */
	private void addTableRowForMacro(Macro macro) {
		model.addRow(new Object[] { macro.clone(),
				KeyStroke.getKeyStroke(macro.getAccelerator()),
				macro.getDesc() });
	}


	/**
	 * {@inheritDoc}
	 */
	protected void doApplyImpl(Frame owner) {

		// Clear previous macros, but remember what they were.  We'll determine
		// what macros were genuinely "removed" by the user, and delete their
		// corresponding scripts.
		MacroManager mm = MacroManager.get();
		SortedSet oldMacros = mm.clearMacros();

		for (int i=0; i<model.getRowCount(); i++) {
			Macro macro = (Macro)model.getValueAt(i, 0);
			mm.addMacro(macro);
			oldMacros.remove(macro); // This macro was "kept".
		}

		// Delete scripts for macros that were removed.
		File exampleDir = getExampleMacrosDir();
		for (Iterator i=oldMacros.iterator(); i.hasNext(); ) {
			Macro deleted = (Macro)i.next();
			File file = new File(deleted.getFile());
			File parentDir = file.getParentFile();
			if (parentDir!=null && parentDir.equals(exampleDir)) {
				System.out.println("NOT deleting macro: " + deleted +
						" (example macro)");
			}
			else {
				System.out.println("Deleting macro: " + deleted);
				file.delete();
			}
		}

	}


	/**
	 * {@inheritDoc}
	 */
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * Returns the directory that RText's example macros are stored in.
	 *
	 * @return The directory.
	 */
	private File getExampleMacrosDir() {
		RText app = ((MacroPlugin)getPlugin()).getRText();
		String installDir = app.getInstallLocation();
		return new File(installDir, "exampleMacros");
	}


	/**
	 * Returns whether the table in this option panel contains a macro
	 * with a given name.
	 *
	 * @param name The name to check for.
	 * @return Whether the table contains a macro with that name.
	 */
	private boolean getTableContainsMacroNamed(String name) {
		for (int i=0; i<model.getRowCount(); i++) {
			Macro macro = (Macro)model.getValueAt(i, 0);
			if (macro.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * {@inheritDoc}
	 */
	public JComponent getTopJComponent() {
		return macroTable;
	}


	/**
	 * {@inheritDoc}
	 */
	public void modifiableTableChanged(ModifiableTableChangeEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(PROPERTY, null, new Integer(e.getRow()));
	}


	/**
	 * {@inheritDoc}
	 */
	protected void setValuesImpl(Frame owner) {
		MacroManager tm = MacroManager.get();
		model.setRowCount(0);
		for (Iterator i=tm.getMacroIterator(); i.hasNext(); ) {
			Macro macro = (Macro)i.next();
			addTableRowForMacro(macro);
		}
	}


	/**
	 * Adds the example macros that ship with RText.
	 */
	private class AddExampleMacrosAction extends AbstractAction {

		public AddExampleMacrosAction(MacroPlugin plugin) {
			putValue(NAME, plugin.getString("Options.Button.AddExampleMacros"));
		}

		public void actionPerformed(ActionEvent e) {
			File exampleMacrosDir = getExampleMacrosDir();
			if (exampleMacrosDir.isDirectory()) {
				File[] files = exampleMacrosDir.listFiles();
				int fileCount = files==null ? 0 : files.length;
				for (int i=0; i<fileCount; i++) {
					addMacro(files[i]);
				}
			}
			else {
				UIManager.getLookAndFeel().provideErrorFeedback(null);
			}
		}

		private void addMacro(File file) {

			String name = file.getName();
			int dot = name.lastIndexOf('.');
			if (dot>-1) {

				Macro macro = null;

				String extension = name.substring(dot+1);
				if ("js".equalsIgnoreCase(extension) ||
						"groovy".equalsIgnoreCase(extension)) {
					String macroName = name.substring(0, dot);
					if (getTableContainsMacroNamed(macroName)) {
						int count = 1;
						while (getTableContainsMacroNamed(macroName + "_" + count)) {
							count++;
						}
						macroName += "_" + count;
					}
					macro = new Macro();
					macro.setName(macroName);
					macro.setFile(file.getAbsolutePath());
				}

				if (macro!=null) {
					addTableRowForMacro(macro);
				}

			}

		}

	}


	/**
	 * Handles modification of macro table values.
	 */
	private class MacroTableRowHandler implements RowHandler {

		public Object[] getNewRowInfo(Object[] oldData) {
			NewMacroDialog macroDialog = new NewMacroDialog(
					(MacroPlugin)getPlugin(), getOptionsDialog());
			Macro old = null;
			if (oldData!=null) {
				old = (Macro)oldData[0];
				macroDialog.setMacro(old);
			}
			macroDialog.setLocationRelativeTo(MacroOptionPanel.this);
			macroDialog.setVisible(true);
			Macro macro = macroDialog.getMacro();
			if (macro!=null) {
				return new Object[] { macro,
						KeyStroke.getKeyStroke(macro.getAccelerator()),
						macro.getDesc() };
			}
			return null;
		}

		public boolean shouldRemoveRow(int row) {
			return true;
		}

		/**
		 * Not an override.  Implements <code>RowHandler#updateUI()</code>.
		 */
		public void updateUI() {
		}

	}


}