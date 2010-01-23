/*
 * 11/05/2009
 *
 * ToolOptionPanel.java - Option panel for managing external tools.
 * Copyright (C) 2009 Robert Futrell
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
package org.fife.rtext.plugins.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.fife.rtext.plugins.tools.NewToolDialog;
import org.fife.rtext.plugins.tools.Tool;
import org.fife.ui.UIUtil;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.ModifiableTableChangeEvent;
import org.fife.ui.modifiabletable.ModifiableTableListener;
import org.fife.ui.modifiabletable.RowHandler;


/**
 * Options panel for managing external tools.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ToolOptionPanel extends PluginOptionsDialogPanel
						implements ModifiableTableListener {

	private static final String MSG = "org.fife.rtext.plugins.tools.OptionPanel";

	private ToolTableModel model;
	private ModifiableTable toolTable;

	private static final String PROPERTY		= "property";


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin.
	 */
	public ToolOptionPanel(ToolPlugin plugin) {

		super(plugin);

		ResourceBundle msg = ResourceBundle.getBundle(MSG);
		setName(msg.getString("Title"));

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		JPanel cp = new JPanel(new BorderLayout());
		cp.setBorder(new OptionPanelBorder(msg.getString("Tools")));
		add(cp);

		model = new ToolTableModel(new String[] {
				msg.getString("TableHeader.Tool"),
				msg.getString("TableHeader.Shortcut"),
				msg.getString("TableHeader.Description")
		});

		ToolManager tm = ToolManager.get();
		for (Iterator i=tm.getToolIterator(); i.hasNext(); ) {
			Tool tool = (Tool)i.next();
			model.addRow(new Object[] { tool,
								KeyStroke.getKeyStroke(tool.getAccelerator()),
								tool.getDescription() });
		}

		toolTable = new ModifiableTable(model, ModifiableTable.BOTTOM,
										ModifiableTable.ADD_REMOVE_MODIFY);
		toolTable.addModifiableTableListener(this);
		toolTable.setRowHandler(new ToolTableRowHandler());
		JTable table = toolTable.getTable();
		table.getColumnModel().getColumn(0).setCellRenderer(new ToolCellRenderer());
		table.setPreferredScrollableViewportSize(new Dimension(300,300));
		cp.add(toolTable);

		applyComponentOrientation(orientation);

	}


	protected void doApplyImpl(Frame owner) {
		// TODO Auto-generated method stub

	}


	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	public JComponent getTopJComponent() {
		return toolTable;
	}


	/**
	 * {@inheritDoc}
	 */
	public void modifiableTableChanged(ModifiableTableChangeEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(PROPERTY, null, new Integer(e.getRow()));
	}


	protected void setValuesImpl(Frame owner) {
		// TODO Auto-generated method stub

	}


	/**
	 * Renderer for tools in the JTable.
	 */
	private static class ToolCellRenderer extends DefaultTableCellRenderer {

		public Component getTableCellRendererComponent(JTable table,
								Object value, boolean isSelected,
								boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
										 hasFocus, row, column);
			setText(((Tool)value).getName());
			setComponentOrientation(table.getComponentOrientation());
			return this;
		}

	}


	/**
	 * Table data for the tool table.
	 */
	private static class ToolTableModel extends DefaultTableModel {

		public ToolTableModel(String[] headers) {
			super(headers, 0);
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}

	}


	/**
	 * Handles modification of tool table values.
	 */
	private class ToolTableRowHandler implements RowHandler {

		private NewToolDialog toolDialog;

		public Object[] getNewRowInfo(Object[] oldData) {
			if (toolDialog==null) {
				toolDialog = new NewToolDialog(getOptionsDialog());
			}
			Tool old = null;
			if (oldData!=null) {
				old = (Tool)oldData[0];
				toolDialog.setTool(old);
			}
			toolDialog.setLocationRelativeTo(ToolOptionPanel.this);
			toolDialog.setVisible(true);
			Tool tool = toolDialog.getTool();
			if (tool!=null) {
				return new Object[] { tool,
						KeyStroke.getKeyStroke(tool.getAccelerator()),
						tool.getDescription() };
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
			if (toolDialog!=null) {
				SwingUtilities.updateComponentTreeUI(toolDialog);
			}
		}

	}


}