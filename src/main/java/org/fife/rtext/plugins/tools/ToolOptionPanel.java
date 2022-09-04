/*
 * 11/05/2009
 *
 * ToolOptionPanel.java - Option panel for managing external tools.
 * Copyright (C) 2009 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import java.awt.*;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.fife.rtext.AbstractConsoleTextAreaOptionPanel;
import org.fife.ui.KeyStrokeCellRenderer;
import org.fife.ui.UIUtil;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowConstants;
import org.fife.ui.modifiabletable.AbstractRowHandler;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.ModifiableTableChangeEvent;
import org.fife.ui.modifiabletable.ModifiableTableListener;


/**
 * Options panel for managing external tools.
 *
 * @author Robert Futrell
 * @version 1.1
 */
class ToolOptionPanel extends AbstractConsoleTextAreaOptionPanel<ToolPlugin>
			implements ModifiableTableListener {

	static final String MSG = "org.fife.rtext.plugins.tools.OptionPanel";

	private DefaultTableModel model;

	static final String TITLE_KEY = "Title";


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin.
	 */
	ToolOptionPanel(ToolPlugin plugin) {

		super(plugin);
		ResourceBundle msg = ResourceBundle.getBundle(MSG);
		setName(msg.getString(TITLE_KEY));

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());
		Box topPanel = Box.createVerticalBox();

		topPanel.add(createGeneralPanel());
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		topPanel.add(createToolsPanel());
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		addRestoreDefaultsButton(topPanel);

		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(orientation);

	}


	/**
	 * Creates a row of data for our table based on a tool.
	 *
	 * @param tool The tool.
	 * @return The row of data for the table model.
	 */
	private static Object[] createRowData(Tool tool) {
		return new Object[] {
			new ToolWrapper(tool),
			KeyStroke.getKeyStroke(tool.getAccelerator()),
			tool.getDescription()
		};
	}


	private Container createToolsPanel() {

		ResourceBundle msg = ResourceBundle.getBundle(MSG);

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new OptionPanelBorder(msg.getString("Tools")));
		add(panel);

		Box topPanel = Box.createVerticalBox();
		panel.add(topPanel, BorderLayout.NORTH);

		model = new DefaultTableModel(new String[] {
			msg.getString("TableHeader.Tool"),
			msg.getString("TableHeader.Shortcut"),
			msg.getString("TableHeader.Description") }, 0);

		ModifiableTable toolTable = new ModifiableTable(model,
			ModifiableTable.BOTTOM, ModifiableTable.ADD_REMOVE_MODIFY);
		toolTable.addModifiableTableListener(this);
		toolTable.setRowHandler(new ToolTableRowHandler());
		JTable table = toolTable.getTable();
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(1).setCellRenderer(KeyStrokeCellRenderer.create());
		table.setPreferredScrollableViewportSize(new Dimension(300,180));
		panel.add(toolTable);

		return panel;

	}


	@Override
	protected void doApplyImpl(Frame owner) {

		ToolPlugin p = getPlugin();
		ToolDockableWindow window = p.getDockableWindow();
		window.setActive(visibleCB.isSelected());
		window.setPosition(locationCombo.getSelectedIndex());

		ToolManager tm = ToolManager.get();
		tm.clearTools();
		for (int i=0; i<model.getRowCount(); i++) {
			Tool tool = ((ToolWrapper)model.getValueAt(i, 0)).tool;
			tm.addTool(tool);
		}

	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	@Override
	public void modifiableTableChanged(ModifiableTableChangeEvent e) {
		setDirty(true);
	}


	/**
	 * Sets the tool output panel placement placement displayed by this panel.
	 *
	 * @param placement The placement displayed; one of
	 *        <code>DockableWindowConstants.LEFT</code>, <code>TOP</code>,
	 *        <code>RIGHT</code>, <code>BOTTOM</code> or <code>FLOATING</code>.
	 */
	private void setToolOutputPanelPlacement(int placement) {
		if (!DockableWindow.isValidPosition(placement))
			placement = DockableWindowConstants.LEFT;
		locationCombo.setSelectedIndex(placement);
	}


	@Override
	protected void setValuesImpl(Frame owner) {

		ToolPlugin p = getPlugin();
		ToolDockableWindow window = p.getDockableWindow();
		visibleCB.setSelected(window.isActive());
		setToolOutputPanelPlacement(window.getPosition());

		ToolManager tm = ToolManager.get();
		model.setRowCount(0);
		for (Iterator<Tool> i=tm.getToolIterator(); i.hasNext();) {
			Tool tool = i.next();
			model.addRow(createRowData(tool));
		}

	}


	/**
	 * Wrapper to renderer a Tool nicely in a table.
	 */
	private static class ToolWrapper {

		private final Tool tool;

		ToolWrapper(Tool tool) {
			this.tool = tool;
		}

		@Override
		public String toString() {
			return tool.getName();
		}

	}


	/**
	 * Handles modification of tool table values.
	 */
	private class ToolTableRowHandler extends AbstractRowHandler {

		@Override
		public Object[] getNewRowInfo(Object[] oldData) {
			NewToolDialog toolDialog = new NewToolDialog(getOptionsDialog());
			Tool old;
			if (oldData!=null) {
				old = ((ToolWrapper)oldData[0]).tool;
				toolDialog.setTool(old);
			}
			toolDialog.setLocationRelativeTo(ToolOptionPanel.this);
			toolDialog.setVisible(true);
			Tool tool = toolDialog.getTool();
			if (tool!=null) {
				return createRowData(tool);
			}
			return null;
		}

	}


}
