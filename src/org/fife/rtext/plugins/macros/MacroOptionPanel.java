/*
 * 07/23/2011
 *
 * MacroOptionPanel.java - Option panel for managing external macros.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.macros;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.macros.NewMacroDialog;
import org.fife.rtext.plugins.macros.Macro;
import org.fife.ui.KeyStrokeCellRenderer;
import org.fife.ui.UIUtil;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.modifiabletable.AbstractRowHandler;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.ModifiableTableChangeEvent;
import org.fife.ui.modifiabletable.ModifiableTableListener;
import org.fife.ui.rtextfilechooser.Utilities;


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

		List<Action> customButtons = new ArrayList<Action>();
		customButtons.add(new AddExampleMacrosAction(plugin));

		macroTable = new ModifiableTable(model, ModifiableTable.BOTTOM,
										ModifiableTable.ADD_REMOVE_MODIFY,
										customButtons);
		macroTable.addModifiableTableListener(this);
		macroTable.setRowHandler(new MacroTableRowHandler());
		JTable table = macroTable.getTable();
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(1).setCellRenderer(KeyStrokeCellRenderer.create());
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
	 * Copies a file.
	 *
	 * @param fromFile The source file.
	 * @param toFile The new file.
	 * @return Whether the operation was successful.
	 */
	private static final boolean copyFile(File fromFile, File toFile) {
		boolean success = false;
		try {
			Utilities.copyFile(fromFile, toFile);
			success = true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return success;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doApplyImpl(Frame owner) {

		// Clear previous macros, but remember what they were.  We'll determine
		// what macros were genuinely "removed" by the user, and delete their
		// corresponding scripts.
		MacroManager mm = MacroManager.get();
		SortedSet<Macro> oldMacros = mm.clearMacros();

		for (int i=0; i<model.getRowCount(); i++) {

			Macro macro = (Macro)model.getValueAt(i, 0);

			// If this is a macro template, create a copy of the file so the
			// user can't edit the original.
			MacroPlugin plugin = (MacroPlugin)getPlugin();
			File examplesDir = getExampleMacrosDir();
			File macroFile = new File(macro.getFile());
			if (examplesDir.equals(macroFile.getParentFile())) {
				// New macro name may have "_1", "_2", etc. on it, so be careful
				String newMacroName = macro.getName() + "." + 
						Utilities.getExtension(macroFile.getName());
				File newMacroFile = new File(plugin.getMacroDir(),newMacroName);
				if (copyFile(macroFile, newMacroFile)) {
					macro.setFile(newMacroFile.getAbsolutePath());
				}
			}

			mm.addMacro(macro);
			oldMacros.remove(macro); // This macro was "kept".

		}

		// Delete scripts for macros that were removed.  Keep macros in the
		// examples directory.  This should only occur if an error occurred
		// copying an example macro into the user's macros directory (see
		// above).
		File exampleDir = getExampleMacrosDir();
		for (Macro deleted : oldMacros) {
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
	@Override
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
	@Override
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
	@Override
	protected void setValuesImpl(Frame owner) {
		MacroManager tm = MacroManager.get();
		model.setRowCount(0);
		for (Iterator<Macro> i = tm.getMacroIterator(); i.hasNext(); ) {
			Macro macro = i.next();
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
	private class MacroTableRowHandler extends AbstractRowHandler {

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

	}


}