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
import java.awt.ComponentOrientation;
import java.awt.Dimension;
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
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableModel;

import org.fife.ui.EscapableDialog;
import org.fife.ui.FSATextField;
import org.fife.ui.RButton;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.RowHandler;
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
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	/**
	 * Constructor.
	 *
	 * @param parent The parent window.
	 * @param rtext The parent application.
	 */
	public NewToolDialog(JDialog parent, RText rtext) {

		super(parent);
		ComponentOrientation o = parent.getComponentOrientation();

		JPanel cp = new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(UIUtil.getEmpty5Border());

		tabPane = new JTabbedPane();
		cp.add(tabPane);

		JPanel springPanel = new JPanel(new SpringLayout());
		springPanel.setBorder(UIUtil.getEmpty5Border());
		JLabel nameLabel = new JLabel(msg.getString("Name"));
		JTextField nameField = new JTextField(40);
		JLabel descLabel = new JLabel(msg.getString("Description"));
		JTextField descField = new JTextField(40);
		JLabel programLabel = new JLabel(msg.getString("Program"));
		programField = new FSATextField();
		RButton programBrowseButton = new RButton(msg.getString("Browse"));
		programBrowseButton.setActionCommand("BrowseProgram");
		programBrowseButton.addActionListener(this);
		JLabel dirLabel = new JLabel(msg.getString("Directory"));
		dirField = new FSATextField();
		dirField.setDirectoriesOnly(true);
		RButton dirBrowseButton = UIUtil.createTabbedPaneButton(msg.getString("Browse"));
		dirBrowseButton.setActionCommand("BrowseDir");
		dirBrowseButton.addActionListener(this);

		Dimension dim = new Dimension(1, 1); // MUST have finite width!
		if (o.isLeftToRight()) {
			springPanel.add(nameLabel);		springPanel.add(nameField); springPanel.add(Box.createRigidArea(dim));
			springPanel.add(descLabel);		springPanel.add(descField); springPanel.add(Box.createRigidArea(dim));
			springPanel.add(programLabel);	springPanel.add(programField); springPanel.add(programBrowseButton);
			springPanel.add(dirLabel);		springPanel.add(dirField); springPanel.add(dirBrowseButton);
		}
		else {
			springPanel.add(Box.createRigidArea(dim));	springPanel.add(nameField);		springPanel.add(nameLabel);
			springPanel.add(Box.createRigidArea(dim));	springPanel.add(descField);		springPanel.add(descLabel);
			springPanel.add(programBrowseButton);		springPanel.add(programField);	springPanel.add(programLabel);
			springPanel.add(dirBrowseButton);			springPanel.add(dirField);		springPanel.add(dirLabel);
		}

		UIUtil.makeSpringCompactGrid(springPanel, 4, 3, 5, 5, 5, 5);
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(springPanel, BorderLayout.NORTH);

		DefaultTableModel argModel = new DefaultTableModel();
		ModifiableTable argTable = new ModifiableTable(argModel);
		argTable.setRowHandler(new ArgTableRowHandler());
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.setBorder(BorderFactory.createTitledBorder(msg.getString("Arguments")));
		temp2.add(argTable);
		temp.add(temp2);
		tabPane.addTab(msg.getString("Tab.Main"), temp);

		JPanel envPanel = new JPanel(new BorderLayout());
		envPanel.setBorder(UIUtil.getEmpty5Border());
		ButtonGroup bg = new ButtonGroup();
		JRadioButton appendRB = new JRadioButton(msg.getString("AppendEnvVars"));
		bg.add(appendRB);
		JRadioButton replaceRB = new JRadioButton(msg.getString("ReplaceEnvVars"));
		bg.add(replaceRB);
		temp = new JPanel(new GridLayout(2,1, 5,0));
		temp.add(appendRB);
		temp.add(replaceRB);
		temp2 = new JPanel(new BorderLayout());
		temp2.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		temp2.add(temp, BorderLayout.LINE_START);
		envPanel.add(temp2, BorderLayout.NORTH);
		EnvVarsTableModel envModel = new EnvVarsTableModel(
								msg.getString("VariableName"),
								msg.getString("VariableValue"));
		ModifiableTable envTable = new ModifiableTable(envModel);
		envTable.setRowHandler(new EnvVarTableRowHandler());
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
		setModal(true);
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
	 * The dialog that allows the user to add or modify a command line
	 * argument.
	 */
	private class ArgDialog extends EscapableDialog implements ActionListener {

		private JTextField argField;
		private String arg;

		public ArgDialog(JDialog parent) {

			super(parent);

			JPanel cp = new ResizableFrameContentPane(new BorderLayout());
			cp.setBorder(UIUtil.getEmpty5Border());

			JPanel temp = new JPanel(new BorderLayout());
			JLabel argLabel = new JLabel(msg.getString("ArgumentDialog.Argument"));
			temp.add(argLabel, BorderLayout.LINE_START);
			argField = new JTextField(20);
			JPanel temp2 = new JPanel(new BorderLayout());
			temp2.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
			temp2.add(argField);
			temp.add(temp2);
			RButton varButton = new RButton(msg.getString("Variables"));
			temp.add(varButton, BorderLayout.LINE_END);
			cp.add(temp, BorderLayout.NORTH);

			temp = new JPanel();
			JPanel buttonPanel = new JPanel(new GridLayout(1,2, 5,5));
			RButton okButton = new RButton(msg.getString("OK"));
			okButton.setActionCommand("OK");
			okButton.addActionListener(this);
			buttonPanel.add(okButton);
			RButton cancelButton = new RButton(msg.getString("Cancel"));
			cancelButton.setActionCommand("Cancel");
			cancelButton.addActionListener(this);
			buttonPanel.add(cancelButton);
			temp.add(buttonPanel);
			cp.add(temp, BorderLayout.SOUTH);

			setTitle(msg.getString("ArgumentDialog.Title"));
			getRootPane().setDefaultButton(okButton);
			setContentPane(cp);
			pack();
			setLocationRelativeTo(parent);

		}

		public void actionPerformed(ActionEvent e) {

			String command = e.getActionCommand();

			if ("OK".equals(command)) {
				arg = argField.getText();
				setVisible(false);
			}

			else if ("Cancel".equals(command)) {
				escapePressed();
			}

		}

		public void setArg(String arg) {
			argField.setText(arg);
			argField.selectAll();
		}

	}


	/**
	 * Row handler for the command line arguments table.
	 */
	private class ArgTableRowHandler implements RowHandler {

		public Object[] getNewRowInfo(Object[] oldData) {
			ArgDialog dialog = new ArgDialog(NewToolDialog.this);
			if (oldData!=null) {
				dialog.setArg((String)oldData[0]);
			}
			dialog.setModal(true);
			dialog.setVisible(true);
			return dialog.arg==null ? null : new String[] { dialog.arg };
		}

		public boolean shouldRemoveRow(int row) {
			return true;
		}

		public void updateUI() {
		}

	}


	/**
	 * The dialog that allows the user to add or modify an environment variable.
	 */
	private class EnvVarDialog extends EscapableDialog
								implements ActionListener {

		private JTextField nameField;
		private JTextField valueField;
		private boolean escaped;

		public EnvVarDialog(JDialog parent) {

			super(parent);
			escaped = true;

			JPanel cp = new ResizableFrameContentPane(new BorderLayout());
			cp.setBorder(UIUtil.getEmpty5Border());

			JLabel nameLabel = new JLabel(msg.getString("EnvVarDialog.Name"));
			nameField = new JTextField(20);
			JLabel valueLabel = new JLabel(msg.getString("EnvVarDialog.Value"));
			valueField = new JTextField(20);
			RButton varButton = new RButton(msg.getString("Variables"));

			JPanel temp = new JPanel(new SpringLayout());
			Dimension dim = new Dimension(1, 1); // MUST have finite width!
			if (getComponentOrientation().isLeftToRight()) {
				temp.add(nameLabel); temp.add(nameField); temp.add(Box.createRigidArea(dim));
				temp.add(valueLabel); temp.add(valueField); temp.add(varButton);
			}
			else {
				temp.add(Box.createRigidArea(dim)); temp.add(nameField); temp.add(nameLabel);
				temp.add(varButton); temp.add(valueField); temp.add(valueLabel);
			}
			UIUtil.makeSpringCompactGrid(temp, 2, 3, 5, 5, 5, 5);
			cp.add(temp, BorderLayout.NORTH);

			temp = new JPanel();
			JPanel buttonPanel = new JPanel(new GridLayout(1,2, 5,5));
			RButton okButton = new RButton(msg.getString("OK"));
			okButton.setActionCommand("OK");
			okButton.addActionListener(this);
			buttonPanel.add(okButton);
			RButton cancelButton = new RButton(msg.getString("Cancel"));
			cancelButton.setActionCommand("Cancel");
			cancelButton.addActionListener(this);
			buttonPanel.add(cancelButton);
			temp.add(buttonPanel);
			cp.add(temp, BorderLayout.SOUTH);

			setTitle(msg.getString("EnvVarDialog.Title"));
			getRootPane().setDefaultButton(okButton);
			setContentPane(cp);
			pack();
			setLocationRelativeTo(parent);

		}

		public void actionPerformed(ActionEvent e) {

			String command = e.getActionCommand();

			if ("OK".equals(command)) {
				escaped = false;
				setVisible(false);
			}

			else if ("Cancel".equals(command)) {
				escapePressed();
			}

		}

		public String[] getReturnValue() {
			return escaped ? null :
				new String[] { nameField.getText(), valueField.getText() };
		}

		public void setData(String name, String value) {
			nameField.setText(name);
			valueField.setText(value);
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


	/**
	 * Row handler for the environment variable table.
	 */
	private class EnvVarTableRowHandler implements RowHandler {

		public Object[] getNewRowInfo(Object[] oldData) {
			EnvVarDialog dialog = new EnvVarDialog(NewToolDialog.this);
			if (oldData!=null) {
				dialog.setData((String)oldData[0], (String)oldData[1]);
			}
			dialog.setModal(true);
			dialog.setVisible(true);
			return dialog.getReturnValue();
		}

		public boolean shouldRemoveRow(int row) {
			return true;
		}

		public void updateUI() {
		}

	}


}