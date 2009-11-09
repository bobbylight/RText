/*
 * 11/05/2009
 *
 * NewToolDialog.java - A dialog used for editing external tools.
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
package org.fife.rtext;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.fife.ui.EscapableDialog;
import org.fife.ui.FSATextField;
import org.fife.ui.RButton;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.rtextfilechooser.RDirectoryChooser;
import org.fife.ui.rtextfilechooser.RTextFileChooser;


/**
 * A dialog that allows the user to create or edit an external tool.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class NewToolDialog extends EscapableDialog implements ActionListener {

	private JTabbedPane tabPane;
	private RTextFileChooser chooser;
	private RDirectoryChooser dirChooser;

	private FSATextField programField;
	private FSATextField dirField;

	private static final String MSG = "org.fife.rtext.NewToolDialog";


	/**
	 * Constructor.
	 *
	 * @param parent The parent window.
	 * @param rtext The parent application.
	 */
	public NewToolDialog(JDialog parent, RText rtext) {

		super(parent);
		ResourceBundle msg = ResourceBundle.getBundle(MSG);

		JPanel cp = new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(UIUtil.getEmpty5Border());

		tabPane = new JTabbedPane();
		cp.add(tabPane);

		Box basicPanel = Box.createVerticalBox();
		basicPanel.setBorder(UIUtil.getEmpty5Border());
		JLabel nameLabel = new JLabel(msg.getString("Name"));
		JTextField nameField = new JTextField(40);
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(nameLabel, BorderLayout.LINE_START);
		temp.add(nameField);
		basicPanel.add(temp);
		basicPanel.add(Box.createVerticalStrut(5));
		JLabel descLabel = new JLabel(msg.getString("Description"));
		JTextField descField = new JTextField(40);
		temp = new JPanel(new BorderLayout());
		temp.add(descLabel, BorderLayout.LINE_START);
		temp.add(descField);
		basicPanel.add(temp);
		basicPanel.add(Box.createVerticalStrut(5));

		JLabel programLabel = new JLabel(msg.getString("Program"));
		programField = new FSATextField();
		RButton programBrowseButton = new RButton(msg.getString("Browse"));
		programBrowseButton.setActionCommand("BrowseProgram");
		programBrowseButton.addActionListener(this);
		temp = new JPanel(new BorderLayout());
		temp.add(programLabel, BorderLayout.LINE_START);
		temp.add(programField);
		temp.add(programBrowseButton, BorderLayout.LINE_END);
		basicPanel.add(temp);
		basicPanel.add(Box.createVerticalStrut(5));
		JLabel dirLabel = new JLabel(msg.getString("Directory"));
		dirField = new FSATextField();
		dirField.setDirectoriesOnly(true);
		RButton dirBrowseButton = UIUtil.createTabbedPaneButton(msg.getString("Browse"));
		dirBrowseButton.setActionCommand("BrowseDir");
		dirBrowseButton.addActionListener(this);
		temp = new JPanel(new BorderLayout());
		temp.add(dirLabel, BorderLayout.LINE_START);
		temp.add(dirField);
		temp.add(dirBrowseButton, BorderLayout.LINE_END);
		basicPanel.add(temp);
		basicPanel.add(Box.createVerticalStrut(5));
		basicPanel.add(Box.createVerticalGlue());
		temp = new JPanel(new BorderLayout());
		temp.add(basicPanel, BorderLayout.NORTH);
		DefaultTableModel argModel = new DefaultTableModel();
		ModifiableTable argTable = new ModifiableTable(argModel);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.setBorder(BorderFactory.createTitledBorder(msg.getString("Arguments")));
		temp2.add(argTable);
		RButton varButton = UIUtil.createTabbedPaneButton(msg.getString("Variables"));
		JPanel temp3 = new JPanel(new BorderLayout());
		temp3.add(varButton, BorderLayout.LINE_START);
		temp2.add(temp3, BorderLayout.SOUTH);
		temp.add(temp2);
		tabPane.addTab(msg.getString("Tab.Main"), temp);

		JPanel envPanel = new JPanel(new BorderLayout());
		envPanel.setBorder(UIUtil.getEmpty5Border());
		ButtonGroup bg = new ButtonGroup();
		JRadioButton appendRB = new JRadioButton(msg.getString("AppendEnvVars"));
		bg.add(appendRB);
		JRadioButton replaceRB = new JRadioButton(msg.getString("ReplaceEnvVars"));
		bg.add(replaceRB);
		temp = new JPanel(new GridLayout(2,1, 5,5));
		temp.add(appendRB);
		temp.add(replaceRB);
		temp2 = new JPanel(new BorderLayout());
		temp2.add(temp, BorderLayout.LINE_START);
		envPanel.add(temp2, BorderLayout.NORTH);
		EnvVarsTableModel envModel = new EnvVarsTableModel(
								msg.getString("VariableName"),
								msg.getString("VariableValue"));
		ModifiableTable envTable = new ModifiableTable(envModel);
		envPanel.add(envTable);
		tabPane.addTab(msg.getString("Tab.Environment"), envPanel);

		JPanel buttonPanel = new JPanel();
		temp = new JPanel(new GridLayout(1,2, 5,5));
		RButton okButton = new RButton(msg.getString("OK"));
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		temp.add(okButton);
		RButton cancelButton = UIUtil.createTabbedPaneButton(msg.getString("Cancel"));
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		temp.add(cancelButton);
		buttonPanel.add(temp);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		setContentPane(cp);
		getRootPane().setDefaultButton(okButton);
		setTitle(msg.getString("Title"));
		setModal(false);
		pack();
		setLocationRelativeTo(rtext);

	}


	/**
	 * Called when an event occurs in this dialog.
	 *
	 * @param e The event.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if ("BrowseProgram".equals(command)) {
			if (chooser==null) {
				chooser = new RTextFileChooser(false);
			}
			int rc = chooser.showOpenDialog(this);
			if (rc==RTextFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				programField.setFileSystemAware(false);
				programField.setText(file.getAbsolutePath());
				programField.setFileSystemAware(true);
			}
		}

		else if ("BrowseDir".equals(command)) {
			if (dirChooser==null) {
				dirChooser = new RDirectoryChooser(this);
			}
			dirChooser.setVisible(true);
			String dir = dirChooser.getChosenDirectory();
			if (dir!=null) {
				dirField.setFileSystemAware(false);
				dirField.setText(dir);
				dirField.setFileSystemAware(true);
			}
		}

		else if ("OK".equals(command)) {
			
		}

		else if ("Cancel".equals(command)) {
			escapePressed();
		}

	}


	/**
	 * Table model used for the environment variable table.
	 */
	private class EnvVarsTableModel extends DefaultTableModel {

		public EnvVarsTableModel(String var, String value) {
			setColumnIdentifiers(new String[] { var, value });
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

	}


}