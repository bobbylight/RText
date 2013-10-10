/*
 * 11/05/2009
 *
 * ToolOptionPanel.java - Option panel for managing external tools.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.fife.rtext.plugins.tools.NewToolDialog;
import org.fife.rtext.plugins.tools.Tool;
import org.fife.ui.KeyStrokeCellRenderer;
import org.fife.ui.UIUtil;
import org.fife.ui.app.GUIApplicationConstants;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.modifiabletable.AbstractRowHandler;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.ModifiableTableChangeEvent;
import org.fife.ui.modifiabletable.ModifiableTableListener;


/**
 * Options panel for managing external tools.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ToolOptionPanel extends PluginOptionsDialogPanel
			implements ModifiableTableListener, GUIApplicationConstants {

	static final String MSG = "org.fife.rtext.plugins.tools.OptionPanel";

	private Listener listener;
	private JCheckBox visibleCB;
	private JComboBox locationCombo;
	private DefaultTableModel model;
	private ModifiableTable toolTable;

	private static final String PROPERTY		= "property";
	static final String TITLE_KEY				= "Title";


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin.
	 */
	public ToolOptionPanel(ToolPlugin plugin) {

		super(plugin);
		listener = new Listener();

		ResourceBundle msg = ResourceBundle.getBundle(MSG);
		setName(msg.getString(TITLE_KEY));
		ResourceBundle gpb = ResourceBundle.getBundle(
				"org/fife/ui/app/GUIPlugin");

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		JPanel cp = new JPanel(new BorderLayout());
		cp.setBorder(new OptionPanelBorder(msg.getString("Tools")));
		add(cp);

		Box topPanel = Box.createVerticalBox();
		cp.add(topPanel, BorderLayout.NORTH);

		// A check box toggling the plugin's visibility.
		visibleCB = new JCheckBox(gpb.getString("Visible"));
		visibleCB.addActionListener(listener);
		addLeftAligned(topPanel, visibleCB, 5);

		// A combo in which to select the dockable window's placement.
		Box locationPanel = createHorizontalBox();
		locationCombo = new JComboBox();
		UIUtil.fixComboOrientation(locationCombo);
		locationCombo.addItem(gpb.getString("Location.top"));
		locationCombo.addItem(gpb.getString("Location.left"));
		locationCombo.addItem(gpb.getString("Location.bottom"));
		locationCombo.addItem(gpb.getString("Location.right"));
		locationCombo.addItem(gpb.getString("Location.floating"));
		locationCombo.addItemListener(listener);
		JLabel locLabel = new JLabel(gpb.getString("Location.title"));
		locLabel.setLabelFor(locationCombo);
		locationPanel.add(locLabel);
		locationPanel.add(Box.createHorizontalStrut(5));
		locationPanel.add(locationCombo);
		locationPanel.add(Box.createHorizontalGlue());
		addLeftAligned(topPanel, locationPanel, 20);
		topPanel.add(Box.createVerticalGlue());

		model = new DefaultTableModel(new String[] {
				msg.getString("TableHeader.Tool"),
				msg.getString("TableHeader.Shortcut"),
				msg.getString("TableHeader.Description") }, 0);

		toolTable = new ModifiableTable(model, ModifiableTable.BOTTOM,
										ModifiableTable.ADD_REMOVE_MODIFY);
		toolTable.addModifiableTableListener(this);
		toolTable.setRowHandler(new ToolTableRowHandler());
		JTable table = toolTable.getTable();
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(1).setCellRenderer(KeyStrokeCellRenderer.create());
		table.setPreferredScrollableViewportSize(new Dimension(300,300));
		cp.add(toolTable);

		applyComponentOrientation(orientation);

	}


	/**
	 * Creates a row of data for our table based on a tool.
	 *
	 * @param tool The tool.
	 * @return The row of data for the table model.
	 */
	private static final Object[] createRowData(Tool tool) {
		return new Object[] {
			new ToolWrapper(tool),
			KeyStroke.getKeyStroke(tool.getAccelerator()),
			tool.getDescription()
		};
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doApplyImpl(Frame owner) {

		ToolPlugin p = (ToolPlugin)getPlugin();
		DockableWindow wind = p.getDockableWindow();
		wind.setActive(visibleCB.isSelected());
		wind.setPosition(getToolOutputPanelPlacement());

		ToolManager tm = ToolManager.get();
		tm.clearTools();
		for (int i=0; i<model.getRowCount(); i++) {
			Tool tool = ((ToolWrapper)model.getValueAt(i, 0)).tool;
			tm.addTool(tool);
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
	 * Returns the selected placement for the tool output panel.
	 *
	 * @return The selected placement.
	 * @see #setToolOutputPanelPlacement(int)
	 */
	public int getToolOutputPanelPlacement() {
		return locationCombo.getSelectedIndex();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
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


	/**
	 * Sets the tool output panel placement placement displayed by this panel.
	 *
	 * @param placement The placement displayed; one of
	 *        <code>GUIApplication.LEFT</code>, <code>TOP</code>,
	 *        <code>RIGHT</code>, <code>BOTTOM</code> or <code>FLOATING</code>.
	 * @see #getToolOutputPanelPlacement()
	 */
	private void setToolOutputPanelPlacement(int placement) {
		if (!DockableWindow.isValidPosition(placement))
			placement = LEFT;
		locationCombo.setSelectedIndex(placement);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setValuesImpl(Frame owner) {

		ToolPlugin p = (ToolPlugin)getPlugin();
		DockableWindow wind = p.getDockableWindow();
		visibleCB.setSelected(wind.isActive());
		setToolOutputPanelPlacement(wind.getPosition());

		ToolManager tm = ToolManager.get();
		model.setRowCount(0);
		for (Iterator<Tool> i=tm.getToolIterator(); i.hasNext(); ) {
			Tool tool = i.next();
			model.addRow(createRowData(tool));
		}

	}


	/**
	 * Listens for events in this panel.
	 */
	private class Listener implements ActionListener, ItemListener {

		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (source==visibleCB) {
				hasUnsavedChanges = true;
				boolean visible = visibleCB.isSelected();
				firePropertyChange(PROPERTY, !visible, visible);
			}

		}

		public void itemStateChanged(ItemEvent e) {
			if (e.getSource()==locationCombo &&
					e.getStateChange()==ItemEvent.SELECTED) {
				hasUnsavedChanges = true;
				int placement = getToolOutputPanelPlacement();
				firePropertyChange(PROPERTY, -1, placement);
			}
		}

	}


	/**
	 * Wrapper to renderer a Tool nicely in a table.  Needed since Substance
	 * requires inheritance for renderers, so we can avoid a hard dependency
	 * on any Substance classes.  What a pain in the ass.
	 */
	private static class ToolWrapper {

		private Tool tool;

		public ToolWrapper(Tool tool) {
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

		public Object[] getNewRowInfo(Object[] oldData) {
			NewToolDialog toolDialog = new NewToolDialog(getOptionsDialog());
			Tool old = null;
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