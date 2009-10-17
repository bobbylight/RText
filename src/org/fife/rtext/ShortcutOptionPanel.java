/*
 * 08/21/2004
 *
 * ShortcutOptionPanel.java - Option panel letting the user configure
 * shortcuts in RText.
 * Copyright (C) 2004 Robert Futrell
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
package org.fife.rtext;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.table.*;

import org.fife.ui.*;
import org.fife.ui.modifiabletable.*;


/**
 * Option panel letting the user configure shortcut keys for RText.
 *
 * @author Robert Futrell
 * @version 0.2
 */
class ShortcutOptionPanel extends OptionsDialogPanel
					implements ActionListener, ModifiableTableListener {

	private ModifiableTable shortcutTable;
	private DefaultTableModel model;
	private RText rtext;
	private Action[] masterActionList;

	private static final String SHORTCUT_PROPERTY	= "SOP.shortcut";


	/**
	 * Constructor.
	 *
	 * @param rtext The owner of the options dialog in which this panel
	 *        appears.
	 * @param msg The resource bundle to use.
	 */
	public ShortcutOptionPanel(final RText rtext, final ResourceBundle msg) {

		super(msg.getString("OptSCName"));
		this.rtext = rtext;

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(
					new OptionPanelBorder(msg.getString("OptSCLabel")));
		add(contentPane);

		model = new ShortcutTableModel(
				msg.getString("OptSCCol1"), msg.getString("OptSCCol2"));
		shortcutTable = new ModifiableTable(model, ModifiableTable.BOTTOM,
										ModifiableTable.MODIFY);
		shortcutTable.addModifiableTableListener(this);
		shortcutTable.setRowHandler(new ShortcutTableRowHandler());
		JTable table = shortcutTable.getTable();
		table.getColumn(msg.getString("OptSCCol2")).setCellRenderer(
									new ShortcutCellRenderer());
		table.setPreferredScrollableViewportSize(new Dimension(300,300));
		contentPane.add(shortcutTable);

		RButton defButton = new RButton(msg.getString("RestoreDefaults"));
		defButton.setActionCommand("RestoreDefaults");
		defButton.addActionListener(this);
		JPanel temp = new JPanel(new BorderLayout());
		temp.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		temp.add(defButton, BorderLayout.LINE_START);
		add(temp, BorderLayout.SOUTH);
		applyComponentOrientation(orientation);

	}


	/**
	 * Listens for actions in this panel.
	 */
	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		if (actionCommand.equals("RestoreDefaults")) {
			rtext.restoreDefaultAccelerators(); // Does mainView too.
			setActions(rtext.getActions(), rtext.getMainView().getActions());
		}

	}


	/**
	 * Applies the settings entered into this dialog on the specified
	 * application.
	 *
	 * @param owner The application.
	 */
	protected void doApplyImpl(Frame owner) {

		Action[] actions = getActions();
		int actionsLength = actions.length;
		Action[] realActions = rtext.getActions();
		int j;

		for (int k=0; k<realActions.length; k++) {
			String name = (String)realActions[k].getValue(Action.NAME);
			for (j=0; j<actionsLength; j++) {
				String name2 = (String)actions[j].getValue(Action.NAME);
				if (name.equals(name2)) {
					realActions[k].putValue(Action.ACCELERATOR_KEY, actions[j].getValue(Action.ACCELERATOR_KEY));
					break;
				}
			}
			if (j==actionsLength)
				System.err.println("err0r!!!!");
		}

		realActions = rtext.getMainView().getActions();
		for (int k=0; k<realActions.length; k++) {
			String name = (String)realActions[k].getValue(Action.NAME);
			for (j=0; j<actionsLength; j++) {
				String name2 = (String)actions[j].getValue(Action.NAME);
				if (name.equals(name2)) {
					realActions[k].putValue(Action.ACCELERATOR_KEY, actions[j].getValue(Action.ACCELERATOR_KEY));
					break;
				}
			}
			if (j==actionsLength)
				System.err.println("err0r!!!!");
		}

		// HORRIBLE workaround for Java Bug ID 5026829 (JMenuItems,
		// among other Swing components, don't update themselves
		// when the Actions on which they were created have their
		// properties changed).
		rtext.menuItemAcceleratorWorkaround();

	}


	/**
	 * {@inheritDoc}
	 */
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * Returns a pretty string value for a KeyStroke, suitable for display as
	 * the keystroke's value in a GUI.
	 *
	 * @param keyStroke The keystroke.
	 * @return The string value of the keystroke.
	 */
	public static String getPrettyStringFor(KeyStroke keyStroke) {

		if (keyStroke==null)
			return "";

		String string = KeyEvent.getKeyModifiersText(keyStroke.getModifiers());
		if (string!=null && string.length()>0)
			string += "+";
		int keyCode = keyStroke.getKeyCode();
		if (keyCode!=KeyEvent.VK_SHIFT && keyCode!=KeyEvent.VK_CONTROL &&
			keyCode!=KeyEvent.VK_ALT && keyCode!=KeyEvent.VK_META)
			string += KeyEvent.getKeyText(keyCode);
		return  string;

	}


	/**
	 * Returns the actions and their shortcuts defined by the user.
	 *
	 * @return The actions and their shortcuts.
	 */
	public Action[] getActions() {
		updateMasterActionList();
		return masterActionList;
	}


	/**
	 * Returns the <code>JComponent</code> at the "top" of this Options
	 * panel.
	 */
	public JComponent getTopJComponent() {
		return shortcutTable.getTable();
	}


	/**
	 * Called whenever the extension/color mapping table is changed.
	 *
	 * @param e An event describing the change.
	 */
	public void modifiableTableChanged(ModifiableTableChangeEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(SHORTCUT_PROPERTY, null, new Integer(e.getRow()));
	}


	/**
	 * Sets the actions to display as configurable.
	 *
	 * @param rtextActions The actions owned by RText.
	 * @param mainViewActions The actions owned by the main view.
	 */
	private void setActions(Action[] rtextActions, Action[] mainViewActions) {

		int numRTextActions = rtextActions.length;
		int numMainViewActions = mainViewActions.length;
		masterActionList = new Action[numRTextActions + numMainViewActions];

		for (int i=0; i<numRTextActions; i++) {
			masterActionList[i] = rtextActions[i];
		}
		for (int i=0; i<numMainViewActions; i++) {
			masterActionList[numRTextActions+i] = mainViewActions[i];
		}

		Arrays.sort(masterActionList, new Comparator() {
			public int compare(Object o1, Object o2) {
				String name1 = (String)((Action)o1).getValue(Action.NAME);
				String name2 = (String)((Action)o2).getValue(Action.NAME);
				if (name1==null) {
					if (name2==null)
						return 0;
					return -1;
				}
				if (name2==null) // name1!=null && name2==null.
					return 1;
				return name1.compareTo(name2);
			}
			public boolean equals(Object o2) {
				return o2==this;
			}
		});

		for (int i=0; i<masterActionList.length; i++) {
			model.setValueAt(masterActionList[i].getValue(Action.NAME), i,0);
			model.setValueAt(masterActionList[i].getValue(Action.ACCELERATOR_KEY), i,1);
		}

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
		setActions(rtext.getActions(), mainView.getActions());
	}


	/**
	 * Synchronizes the master action list with the values entered in the
	 * table.
	 */
	private void updateMasterActionList() {
		int num = masterActionList.length;
		for (int i=0; i<num; i++) {
			masterActionList[i].putValue(Action.ACCELERATOR_KEY,
									model.getValueAt(i,1));
		}	
	}


	/**
	 * Dialog returning a shortcut.
	 */
	class GetKeyStrokeDialog extends JDialog implements ActionListener {

		private KeyStroke stroke;
		private JTextField textField;
		private boolean canceled;

		GetKeyStrokeDialog(KeyStroke initial) {
			super(rtext, rtext.getResourceBundle().getString("KeyStrokeDialogTitle"));
			ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
			ResourceBundle msg = rtext.getResourceBundle();
			JPanel contentPane = new JPanel();
			contentPane.setBorder(UIUtil.getEmpty5Border());
			contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			JPanel temp = new JPanel(new BorderLayout());
			JLabel prompt = new JLabel(msg.getString("KeyStrokePrompt"));
			temp.add(prompt, BorderLayout.LINE_START);
			contentPane.add(temp);
			contentPane.add(Box.createVerticalStrut(8));
			JLabel charLabel = new JLabel(msg.getString("KeyStrokeKey"));
			textField = new JTextField(20) {
				protected void processKeyEvent(KeyEvent e) {
					int keyCode = e.getKeyCode();
					if (e.getID()==KeyEvent.KEY_PRESSED &&
						keyCode!=KeyEvent.VK_ENTER &&
						keyCode!=KeyEvent.VK_BACK_SPACE) {
						int modifiers = e.getModifiers();
						stroke = KeyStroke.getKeyStroke(
								keyCode, modifiers);
						setText(getPrettyStringFor(stroke));
						return;
					}
					else if (keyCode==KeyEvent.VK_BACK_SPACE) {
						stroke = null; // Not necessary; sanity check.
						setText(null);
					}
				}
			};
			charLabel.setLabelFor(textField);
			temp = new JPanel(new BorderLayout());
			temp.add(charLabel, BorderLayout.LINE_START);
			temp.add(textField);
			contentPane.add(temp);
			temp = new JPanel(new GridLayout(1,2, 5,5));
			RButton ok = new RButton(msg.getString("OKButtonLabel"));
			ok.setActionCommand("OK");
			ok.addActionListener(this);
			temp.add(ok);
			RButton cancel = new RButton(msg.getString("Cancel"));
			cancel.setActionCommand("Cancel");
			cancel.addActionListener(this);
			temp.add(cancel);
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(temp);
			JPanel realCP = new ResizableFrameContentPane(new BorderLayout());
			realCP.add(contentPane, BorderLayout.NORTH);
			realCP.add(buttonPanel, BorderLayout.SOUTH);
			setContentPane(realCP);
			setKeyStroke(initial);
			setModal(true);
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			applyComponentOrientation(orientation);
			pack();
		}

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("OK")) {
				canceled = false;
				setVisible(false);
			}
			else if (command.equals("Cancel")) {
				stroke = null;
				setVisible(false);
			}
		}

		public boolean getCancelled() {
			return canceled;
		}

		public KeyStroke getKeyStroke() {
			return stroke;
		}

		public void setKeyStroke(KeyStroke stroke) {
			this.stroke = stroke;
			textField.setText(getPrettyStringFor(stroke));
		}

		public void setVisible(boolean visible) {
			if (visible) {
				canceled = true; // Default to cancelled.
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						textField.requestFocusInWindow();
						textField.selectAll();
					}
				});
			}
			super.setVisible(visible);
		}

	}


	/**
	 * Handles modification of shortcut table values.
	 */
	private class ShortcutTableRowHandler implements RowHandler {

		private GetKeyStrokeDialog ksDialog;

		public Object[] getNewRowInfo(Object[] oldData) {
			KeyStroke keyStroke = (KeyStroke)oldData[1];
			String action = (String)oldData[0];
			if (ksDialog==null)
				ksDialog = new GetKeyStrokeDialog(null);
			ksDialog.setKeyStroke(keyStroke);
			ksDialog.setLocationRelativeTo(ShortcutOptionPanel.this);
			ksDialog.setVisible(true);
			if (!ksDialog.getCancelled()) {
				KeyStroke temp = ksDialog.getKeyStroke();
				if ((temp==null && keyStroke!=null) ||
						(temp!=null && !temp.equals(keyStroke))) {
					return new Object[] { action, temp };
				}
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
			if (ksDialog!=null) {
				SwingUtilities.updateComponentTreeUI(ksDialog);
			}
		}

	}


	/**
	 * Renderer for shortcuts in the JTable.
	 */
	static class ShortcutCellRenderer extends DefaultTableCellRenderer {

		public Component getTableCellRendererComponent(JTable table,
								Object value, boolean isSelected,
								boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
										 hasFocus, row, column);
			setText(getPrettyStringFor((KeyStroke)value));
			setComponentOrientation(table.getComponentOrientation());
			return this;
		}

	}


	/**
	 * Table data for the "Shortcut" table.
	 */
	static class ShortcutTableModel extends DefaultTableModel {

		public ShortcutTableModel(String fileTypeHead, String filterHead) {
			super(new Object[] { fileTypeHead, filterHead },
				RText.actionNames.length + AbstractMainView.NUM_ACTIONS);
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}

	}


}