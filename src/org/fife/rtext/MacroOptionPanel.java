/*
 * 10/17/2004
 *
 * MacroOptionPanel.java - Option panel letting the user manage their
 * recorded macros.
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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RButton;
import org.fife.ui.RListSelectionModel;
import org.fife.ui.RScrollPane;
import org.fife.ui.UIUtil;


/**
 * Option panel letting the user manage their recorded macros.
 *
 * @author Robert Futrell
 * @version 0.1
 */
class MacroOptionPanel extends OptionsDialogPanel
						implements ActionListener, ListSelectionListener {

	private JList macroList;
	private DefaultListModel listModel;
	private RButton removeButton;


	/**
	 * Constructor.
	 *
	 * @param rtext The owner of the options dialog in which this panel
	 *        appears.
	 * @param msg The resource bundle to use.
	 */
	public MacroOptionPanel(final RText rtext, final ResourceBundle msg) {

		super(msg.getString("OptMaName"));

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		JPanel cp = new JPanel(new BorderLayout());
		cp.setBorder(new OptionPanelBorder(msg.getString("OptMaTit")));
		add(cp);

		listModel = new DefaultListModel();
		macroList = new JList(listModel);
		macroList.setSelectionModel(new RListSelectionModel());
		macroList.addListSelectionListener(this);
		macroList.setVisibleRowCount(16);
		RScrollPane scrollPane = new RScrollPane(macroList);
		cp.add(scrollPane);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		removeButton = new RButton(msg.getString("RemoveButtonLabel"));
		removeButton.setActionCommand("Remove");
		removeButton.addActionListener(this);
		buttonPanel.add(removeButton, BorderLayout.LINE_START);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		applyComponentOrientation(orientation);

	}


	/**
	 * Listens for actions on this panel.
	 *
	 * @param e The action that occurred.
	 */
	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		if ("Remove".equals(actionCommand)) {
			ListData selection = (ListData)macroList.getSelectedValue();
			if (selection==null)
				throw new InternalError("Macro list had no selection!");
			File macroFile = selection.getFile();
			if (!macroFile.delete()) {
				// TODO: Localize me
				String text = "Could not delete macro file:\n" +
								macroFile.getAbsolutePath();
				JOptionPane.showMessageDialog(this, text);
			}
			int index = macroList.getSelectedIndex();
			listModel.remove(index);
			macroList.setSelectedIndex(
								Math.min(index, listModel.getSize()-1));
			removeButton.setEnabled(macroList.getSelectedIndex()>-1);
		}

	}


	/**
	 * Applies the settings entered into this dialog on the specified
	 * application.
	 *
	 * @param owner The application.
	 */
	protected void doApplyImpl(Frame owner) {
	}


	/**
	 * {@inheritDoc}
	 */
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		// They can't input invalid stuff on this options panel.
		return null;
	}


	/**
	 * Returns the <code>JComponent</code> at the "top" of this Options
	 * panel.  This is the component that will receive focus if the user
	 * switches to this Options panel in the Options dialog.  As an added
	 * bonus, if this component is a <code>JTextComponent</code>, its
	 * text is selected for easy changing.
	 *
	 * @return The top <code>JComponent</code>.
	 */
	public JComponent getTopJComponent() {
		return macroList;
	}


	/**
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are not handled.
	 *
	 * @param owner The parent application.
	 * @see #setValues(Frame)
	 */
	protected void setValuesImpl(Frame owner) {

		// Get the (guaranteed non-null) macro files array.
		File[] macroFiles = RTextUtilities.getSavedMacroFiles();
		int count = macroFiles.length;

		// Initialize the macro list.
		listModel.removeAllElements();
		if (count>0) {
			for (int i=0; i<count; i++)
				listModel.addElement(new ListData(macroFiles[i]));
			macroList.setSelectedIndex(0);
		}

		// Enable/disable the "Remove" button appropriately.
		removeButton.setEnabled(count>0);

	}


	/**
	 * Called when the user changes the language in the language list.
	 * Do not override.
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()==false) {
			int firstIndex = e.getFirstIndex();
			int lastIndex = e.getLastIndex();
			if (firstIndex==lastIndex && lastIndex==-1)
				macroList.setSelectedIndex(-1);
		}
	}


	/**
	 * Wrapper class for the data in the macro list.
	 */
	private static class ListData {

		private File file;

		public ListData(File file) {
			this.file = file;
		}

		public File getFile() {
			return file;
		}

		public String toString() {
			return RTextUtilities.getMacroName(file);
		}

	}


}