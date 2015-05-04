/*
 * 02/06/2010
 *
 * TasksOptionPanel.java - Option panel for the Tasks plugin.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tasks;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.PlainDocument;

import org.fife.rtext.RText;
import org.fife.ui.EscapableDialog;
import org.fife.ui.PickyDocumentFilter;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.modifiabletable.AbstractRowHandler;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.ModifiableTableChangeEvent;
import org.fife.ui.modifiabletable.ModifiableTableListener;


/**
 * Options panel used to specify tasks options.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class TasksOptionPanel extends PluginOptionsDialogPanel
		implements ActionListener, ItemListener, ModifiableTableListener {

	private TasksPlugin plugin;
	private JCheckBox visibleCB;
	private JComboBox locationCombo;
	private DefaultTableModel model;
	private ModifiableTable table;

	private static final String PROPERTY			= "Property";


	/**
	 * Constructor.
	 *
	 * @param rtext The parent application.
	 * @param plugin The tasks plugin.
	 */
	public TasksOptionPanel(RText rtext, TasksPlugin plugin) {

		super(plugin.getPluginName(), plugin);

		ResourceBundle gpb = ResourceBundle.getBundle(
									"org.fife.ui.app.GUIPlugin");

		setIcon(plugin.getPluginIcon());
		this.plugin = plugin;
		ComponentOrientation o = ComponentOrientation.getOrientation(getLocale());
		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());

		// A panel to contain everything that will go into our "top" area.
		Box topPanel = Box.createVerticalBox();
		topPanel.setBorder(new OptionPanelBorder(
				plugin.getString("Options.TaskWindow")));

		// A check box toggling the plugin's visibility.
		visibleCB = new JCheckBox(gpb.getString("Visible"));
		visibleCB.addActionListener(this);
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
		locationCombo.addItemListener(this);
		JLabel locLabel = new JLabel(gpb.getString("Location.title"));
		locLabel.setLabelFor(locationCombo);
		locationPanel.add(locLabel);
		locationPanel.add(Box.createHorizontalStrut(5));
		locationPanel.add(locationCombo);
		locationPanel.add(Box.createHorizontalGlue());
		addLeftAligned(topPanel, locationPanel);
		add(topPanel, BorderLayout.NORTH);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(new OptionPanelBorder(
				plugin.getString("Options.TaskIdentifiers")));
		add(contentPane);

		model = new DefaultTableModel(0, 1);
		table = new ModifiableTable(model);
		table.addModifiableTableListener(this);
		table.getTable().setTableHeader(null);
		table.setRowHandler(new IdRowHandler());
		// Shrink default viewport size; default is too big
		JTable realTable = table.getTable();
		realTable.setPreferredScrollableViewportSize(new Dimension(300,200));
		contentPane.add(table);

		JButton defaultsButton = new JButton(
								plugin.getString("Options.RestoreDefaults"));
		defaultsButton.setActionCommand("RestoreDefaults");
		defaultsButton.addActionListener(this);
		JPanel temp = new JPanel(new BorderLayout());
		temp.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		temp.add(defaultsButton, BorderLayout.LINE_START);
		add(temp, BorderLayout.SOUTH);

		applyComponentOrientation(o);

	}


	/**
	 * Called when an event occurs in this panel.
	 *
	 * @param e The event.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (visibleCB==e.getSource()) {
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, null, null);
		}

		else if ("RestoreDefaults".equals(command)) {

			String taskIds = plugin.getTaskIdentifiers();

			if (!visibleCB.isSelected() ||
					locationCombo.getSelectedIndex()!=2 ||
					!taskIds.equals(TasksPrefs.DEFAULT_TASK_IDS)) {
				visibleCB.setSelected(true);
				locationCombo.setSelectedIndex(2);
				setDisplayedTaskIds(TasksPrefs.DEFAULT_TASK_IDS);
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}
			
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doApplyImpl(Frame owner) {

		plugin.setTaskWindowVisible(visibleCB.isSelected());
		plugin.setTaskWindowPosition(locationCombo.getSelectedIndex());

		StringBuilder sb = new StringBuilder();
		for (int i=0; i<model.getRowCount(); i++) {
			sb.append(model.getValueAt(i, 0));
			if (i<model.getRowCount()-1) {
				sb.append('|');
			}
		}

		plugin.setTaskIdentifiers(sb.toString());

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public JComponent getTopJComponent() {
		return table;
	}


	/**
	 * Gets notified when the user selects an item in the location combo box.
	 *
	 * @param e The event.
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource()==locationCombo &&
				e.getStateChange()==ItemEvent.SELECTED) {
			hasUnsavedChanges = true;
			int placement = locationCombo.getSelectedIndex();
			firePropertyChange(PROPERTY, -1, placement);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void modifiableTableChanged(ModifiableTableChangeEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(PROPERTY, null, null);
	}


	/**
	 * Sets the displayed task identifiers.
	 *
	 * @param ids The new task identifiers, separated by the '|' character.
	 *        This cannot be <code>null</code>.
	 */
	private void setDisplayedTaskIds(String identifiers) {
		model.setRowCount(0);
		if (identifiers!=null && identifiers.length()>0) {
			String[] ids = identifiers.split("\\|");
			for (int i=0; i<ids.length; i++) {
				model.addRow(new String[] { ids[i] });
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setValuesImpl(Frame owner) {
		visibleCB.setSelected(plugin.isTaskWindowVisible());
		locationCombo.setSelectedIndex(plugin.getTaskWindowPosition());
		setDisplayedTaskIds(plugin.getTaskIdentifiers());
	}


	/**
	 * A document filter that only allows letters and '?' go through.
	 */
	private static class IdNameDocumentFilter extends PickyDocumentFilter {

		@Override
		protected String cleanseImpl(String text) {
			int length = text.length();
			for (int i=0; i<length; i++) {
				char ch = text.charAt(i);
				if (!(Character.isLetter(ch) || ch=='?')) {
					text = text.substring(0,i) + text.substring(i+1);
					i--;
					length--;
				}
			}
			return text;
		}

	}


	/**
	 * Dialog that allows the user to add or edit an identifier.
	 */
	private class IdentifierDialog extends EscapableDialog
						implements ActionListener, DocumentListener {

		private JTextField idField;
		private JButton okButton;
		private boolean accepted;

		public IdentifierDialog(JDialog parent) {

			super(parent);
			ComponentOrientation o = parent.getComponentOrientation();

			JPanel cp = new ResizableFrameContentPane(new BorderLayout());
			cp.setBorder(UIUtil.getEmpty5Border());
			setContentPane(cp);

			Box box = Box.createVerticalBox();
			String text = plugin.getString("Options.TaskIdentifierDesc");
			SelectableLabel label = new SelectableLabel(text);
			box.add(label);
			box.add(Box.createVerticalStrut(5));

			JPanel temp = new JPanel(new BorderLayout());
			text = plugin.getString("Options.TaskIdentifierPrompt");
			JLabel label2 = new JLabel(text);
			if (o.isLeftToRight()) {
				label2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			}
			else {
				label2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			}
			temp.add(label2, BorderLayout.LINE_START);
			idField = new JTextField(30);
			PlainDocument doc = new PlainDocument();
			doc.setDocumentFilter(new IdNameDocumentFilter());
			idField.setDocument(doc);
			idField.getDocument().addDocumentListener(this);
			temp.add(idField);
			box.add(temp);
			box.add(Box.createVerticalGlue());
			cp.add(box, BorderLayout.NORTH);

			okButton = new JButton(plugin.getString("OK"));
			okButton.setActionCommand("OK");
			okButton.addActionListener(this);
			JButton cancelButton = new JButton(plugin.getString("Cancel"));
			cancelButton.setActionCommand("Cancel");
			cancelButton.addActionListener(this);
			Container buttons=UIUtil.createButtonFooter(okButton, cancelButton);
			cp.add(buttons, BorderLayout.SOUTH);

			setTitle(plugin.getString("Options.TaskIdentifierTitle"));
			setModal(true);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			getRootPane().setDefaultButton(okButton);
			applyComponentOrientation(o);
			pack();
			setLocationRelativeTo(parent);
			accepted = false;

			idField.requestFocusInWindow();

		}

		public void actionPerformed(ActionEvent e) {
			// Only called for OK and Cancel buttons
			String command = e.getActionCommand();
			if ("OK".equals(command)) {
				accepted = true;
			}
			escapePressed();
		}

		public void changedUpdate(DocumentEvent e) {
		}

		public String getIdentifier() {
			return accepted ? idField.getText() : null;
		}

		private void handleDocumentEvent(DocumentEvent e) {
			boolean empty = idField.getDocument().getLength()==0;
			okButton.setEnabled(!empty);
		}

		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		public void setIdentifier(String identifier) {
			idField.setText(identifier);
			idField.selectAll();
			handleDocumentEvent(null); // Force proper OK button state
		}

	}


	/**
	 * Row handler for task identifiers.
	 */
	private class IdRowHandler extends AbstractRowHandler {

		public Object[] getNewRowInfo(Object[] oldData) {
			String oldValue = oldData==null ? null : (String)oldData[0];
			IdentifierDialog dlg = new IdentifierDialog(getOptionsDialog());
			dlg.setIdentifier(oldValue);
			dlg.setVisible(true);
			String input = dlg.getIdentifier();
			return input!=null ? new Object[] { input } : null;
		}

	}


}