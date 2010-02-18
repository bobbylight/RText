/*
 * 02/06/2010
 *
 * TasksOptionPanel.java - Option panel for the Tasks plugin.
 * Copyright (C) 2010 Robert Futrell
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
package org.fife.rtext.plugins.tasks;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.PlainDocument;

import org.fife.rtext.RText;
import org.fife.ui.EscapableDialog;
import org.fife.ui.PickyDocumentFilter;
import org.fife.ui.RButton;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.ModifiableTableChangeEvent;
import org.fife.ui.modifiabletable.ModifiableTableListener;
import org.fife.ui.modifiabletable.RowHandler;


/**
 * Options panel used to specify tasks options.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class TasksOptionPanel extends PluginOptionsDialogPanel
						implements ActionListener, ModifiableTableListener {

	private TasksPlugin plugin;
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
		setIcon(plugin.getPluginIcon());
		this.plugin = plugin;
		ComponentOrientation o = ComponentOrientation.getOrientation(getLocale());
		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(new OptionPanelBorder(
				plugin.getString("Options.TaskIdentifiers")));
		add(contentPane);

		model = new DefaultTableModel(0, 1);
		table = new ModifiableTable(model);
		table.addModifiableTableListener(this);
		table.getTable().setTableHeader(null);
		table.setRowHandler(new IdRowHandler());
		contentPane.add(table);

		RButton defaultsButton = new RButton(
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

		if ("RestoreDefaults".equals(command)) {

			String taskIds = plugin.getTaskIdentifiers();

			if (!taskIds.equals(TasksPrefs.DEFAULT_TASK_IDS)) {
				setDisplayedTaskIds(TasksPrefs.DEFAULT_TASK_IDS);
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}
			
		}

	}


	/**
	 * {@inheritDoc}
	 */
	protected void doApplyImpl(Frame owner) {

		StringBuffer sb = new StringBuffer();
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
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	public JComponent getTopJComponent() {
		return table;
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
	protected void setValuesImpl(Frame owner) {
		setDisplayedTaskIds(plugin.getTaskIdentifiers());
	}


	/**
	 * A document filter that only allows letters and '?' go through.
	 */
	private static class IdNameDocumentFilter extends PickyDocumentFilter {

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
		private RButton okButton;
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

			JPanel buttonPanel = new JPanel();
			temp = new JPanel(new GridLayout(1,2, 5,5));
			okButton = new RButton(plugin.getString("OK"));
			okButton.setActionCommand("OK");
			okButton.addActionListener(this);
			temp.add(okButton);
			RButton cancelButton = new RButton(plugin.getString("Cancel"));
			cancelButton.setActionCommand("Cancel");
			cancelButton.addActionListener(this);
			temp.add(cancelButton);
			buttonPanel.add(temp);
			cp.add(buttonPanel, BorderLayout.SOUTH);

			setTitle(plugin.getString("Options.TaskIdentifierTitle"));
			setModal(true);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			getRootPane().setDefaultButton(okButton);
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
	private class IdRowHandler implements RowHandler {

		public Object[] getNewRowInfo(Object[] oldData) {
			String oldValue = oldData==null ? null : (String)oldData[0];
			IdentifierDialog dlg = new IdentifierDialog(getOptionsDialog());
			dlg.setIdentifier(oldValue);
			dlg.setVisible(true);
			String input = dlg.getIdentifier();
			return input!=null ? new Object[] { input } : null;
		}

		public boolean shouldRemoveRow(int row) {
			return true; // Can remove any row
		}

		public void updateUI() {
			// Nothing to update.
		}
		
	}


}