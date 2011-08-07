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
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.util.Iterator;
import java.util.SortedSet;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.fife.rtext.RTextUtilities;
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
		cp.setBorder(new OptionPanelBorder(plugin.getString("Plugin.Name")));
		add(cp);

		model = new DefaultTableModel(new String[] {
				plugin.getString("Options.TableHeader.Macro"),
				plugin.getString("Options.TableHeader.Shortcut"),
				plugin.getString("Options.TableHeader.Description") }, 0);

		macroTable = new ModifiableTable(model, ModifiableTable.BOTTOM,
										ModifiableTable.ADD_REMOVE_MODIFY);
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
	 * {@inheritDoc}
	 */
	protected void doApplyImpl(Frame owner) {

		// Clear previous macros, but remember what they were.  We'll determine
		// what macros were genuinely "removed" by the user, and delete their
		// corresponding scripts.
		MacroManager tm = MacroManager.get();
		SortedSet oldMacros = tm.clearMacros();

		for (int i=0; i<model.getRowCount(); i++) {
			Macro macro = (Macro)model.getValueAt(i, 0);
			tm.addMacro(macro);
			oldMacros.remove(macro); // This macro was "kept".
		}

		// Delete scripts for macros that were removed.
		for (Iterator i=oldMacros.iterator(); i.hasNext(); ) {
			Macro deleted = (Macro)i.next();
			System.out.println("Deleting macro: " + deleted);
			new File(deleted.getFile()).delete();
		}

	}


	/**
	 * {@inheritDoc}
	 */
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
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
			model.addRow(new Object[] { macro.clone(),
								KeyStroke.getKeyStroke(macro.getAccelerator()),
								macro.getDesc() });
		}
	}


	/**
	 * Renderer for KeyStrokes in the JTable.
	 */
	private static class KeyStrokeCellRenderer extends DefaultTableCellRenderer{

		public Component getTableCellRendererComponent(JTable table,
								Object value, boolean isSelected,
								boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
										 hasFocus, row, column);
			KeyStroke ks = (KeyStroke)value;
			setText(RTextUtilities.getPrettyStringFor(ks));
			setComponentOrientation(table.getComponentOrientation());
			return this;
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