/*
 * 11/05/2009
 *
 * NewToolDialog.java - A dialog used for editing external tools.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

import org.fife.rsta.ui.AssistanceIconPanel;
import org.fife.rsta.ui.search.AbstractSearchDialog;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.EscapableDialog;
import org.fife.ui.FSATextField;
import org.fife.ui.KeyStrokeField;
import org.fife.ui.MenuButton;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.modifiabletable.AbstractRowHandler;
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

	private JTextField nameField;
	private JTextField descField;
	private FSATextField programField;
	private FSATextField dirField;
	private KeyStrokeField shortcutField;
	private DefaultTableModel argModel;
	private DefaultTableModel envModel;
	private JRadioButton appendRB;
	private JRadioButton replaceRB;

	private Tool tool;

	/**
	 * The original name of the tool being edited, or <code>null</code> if
	 * the user is creating a new tool.
	 */
	private String origName;

	private static final String MSG = "org.fife.rtext.plugins.tools.NewToolDialog";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	/**
	 * Constructor.
	 *
	 * @param parent The parent window.
	 */
	public NewToolDialog(JDialog parent) {
		super(parent);
		createGUI();
	}


	/**
	 * Constructor.
	 *
	 * @param parent The parent window.
	 */
	public NewToolDialog(JFrame parent) {
		super(parent);
		createGUI();
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
			tool = checkInputs();
			if (tool!=null) {
				escapePressed();
			}
		}

		else if ("Cancel".equals(command)) {
			escapePressed();
		}

	}


	/**
	 * Adds a completion for a tool-related variable.
	 *
	 * @param p The completion provider to add to.
	 * @param key The key for the localized tool variable.
	 */
	private static final void addToolVarCompletion(DefaultCompletionProvider p,
			String key) {
		String temp = msg.getString(key);
		int split = temp.indexOf(" - ");
		if (split>-1) { // Always true
			String input = temp.substring(0, split);
			String desc = temp.substring(split+3);
			BasicCompletion comp = new BasicCompletion(p, input, desc);
			p.addCompletion(comp);
		}
		else {
			System.err.println("Warning - split not found for: " + key);
		}
	}


	/**
	 * Returns the tool definition input by the user.
	 *
	 * @return The tool, or <code>null</code> if there was a problem
	 *         with their input.  In the latter case, a notice has been
	 *         displayed to the user about the problem.
	 */
	private Tool checkInputs() {

		// The name of the tool
		String name = nameField.getText();
		if (!isValidName(name)) {
			showError(nameField, "Error.InvalidName", name);
			return null;
		}
		if (!name.equals(origName) &&
				ToolManager.get().containsToolWithName(name)) {
			showError(nameField, "Error.ToolAlreadyExists", name);
			return null;
		}

		// A description of the tool
		String desc = descField.getText();

		// The program to launch
		String program = programField.getText().trim();
		if (program.length()==0) {
			showError(programField, "Error.NoProgramSpecified", null);
			return null;
		}

		// The directory to run in
		String dir = dirField.getText().trim();
		if (dir.length()==0) {
			dir = System.getProperty("user.dir");
		}

		KeyStroke accelerator = shortcutField.getKeyStroke();
		
		// If we get here, all the parameters are valid, so create the tool!
		Tool tool = new Tool(name, desc);
		tool.setProgram(program);
		tool.setDirectory(dir);
		if (accelerator!=null) {
			tool.setAccelerator(accelerator.toString());
		}

		for (int i=0; i<argModel.getRowCount(); i++) {
			String arg = (String)argModel.getValueAt(i, 0);
			tool.addArg(arg);
		}

		tool.setAppendEnvironmentVars(appendRB.isSelected());

		for (int i=0; i<envModel.getRowCount(); i++) {
			String varName = (String)envModel.getValueAt(i, 0);
			String varValue = (String)envModel.getValueAt(i, 1);
			tool.putEnvVar(varName, varValue);
		}

		return tool;

	}


	private void createGUI() {

		Container parent = getParent();
		ComponentOrientation o = parent.getComponentOrientation();

		JPanel cp = new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(UIUtil.getEmpty5Border());

		tabPane = new JTabbedPane();
		cp.add(tabPane);

		JPanel springPanel = new JPanel(new SpringLayout());
		springPanel.setBorder(UIUtil.getEmpty5Border());
		JLabel nameLabel = new JLabel(msg.getString("Name"));
		nameField = new JTextField(40);
		JPanel nameFieldPanel = RTextUtilities.createAssistancePanel(nameField, null);
		JLabel descLabel = new JLabel(msg.getString("Description"));
		descField = new JTextField(40);
		JPanel descFieldPanel = RTextUtilities.createAssistancePanel(descField, null);
		JLabel programLabel = new JLabel(msg.getString("Program"));
		programField = new FSATextField();
		JPanel programFieldPanel = RTextUtilities.createAssistancePanel(programField, null);
		JButton programBrowseButton = new JButton(msg.getString("Browse"));
		programBrowseButton.setActionCommand("BrowseProgram");
		programBrowseButton.addActionListener(this);
		JLabel dirLabel = new JLabel(msg.getString("Directory"));
		dirField = new FSATextField();
DefaultCompletionProvider provider = new DefaultCompletionProvider();
provider.addCompletion(new BasicCompletion(provider, "${file_dir}", "Directory of the current file"));
AutoCompletion ac = new AutoCompletion(provider);
ac.setAutoCompleteSingleChoices(false);
ac.install(dirField);
		dirField.setDirectoriesOnly(true);
		AssistanceIconPanel aip = new AssistanceIconPanel(dirField);
		aip.setAssistanceEnabled(AbstractSearchDialog.getContentAssistImage());
		JPanel dirFieldPanel = RTextUtilities.createAssistancePanel(dirField, aip);
		JButton dirBrowseButton = UIUtil.newTabbedPaneButton(msg.getString("Browse"));
		dirBrowseButton.setActionCommand("BrowseDir");
		dirBrowseButton.addActionListener(this);
		JLabel shortcutLabel = new JLabel(msg.getString("Shortcut"));
		shortcutField = new KeyStrokeField();
		JPanel shortcutFieldPanel = RTextUtilities.createAssistancePanel(shortcutField, null);

		Dimension dim = new Dimension(1, 1); // MUST have finite width!
		if (o.isLeftToRight()) {
			springPanel.add(nameLabel);		springPanel.add(nameFieldPanel);     springPanel.add(Box.createRigidArea(dim));
			springPanel.add(descLabel);		springPanel.add(descFieldPanel);     springPanel.add(Box.createRigidArea(dim));
			springPanel.add(programLabel);	springPanel.add(programFieldPanel);  springPanel.add(programBrowseButton);
			springPanel.add(dirLabel);		springPanel.add(dirFieldPanel);      springPanel.add(dirBrowseButton);
			springPanel.add(shortcutLabel); springPanel.add(shortcutFieldPanel); springPanel.add(Box.createRigidArea(dim));
		}
		else {
			springPanel.add(Box.createRigidArea(dim));	springPanel.add(nameFieldPanel);     springPanel.add(nameLabel);
			springPanel.add(Box.createRigidArea(dim));	springPanel.add(descFieldPanel);     springPanel.add(descLabel);
			springPanel.add(programBrowseButton);		springPanel.add(programFieldPanel);  springPanel.add(programLabel);
			springPanel.add(dirBrowseButton);			springPanel.add(dirFieldPanel);      springPanel.add(dirLabel);
			springPanel.add(Box.createRigidArea(dim));  springPanel.add(shortcutFieldPanel); springPanel.add(shortcutLabel);
		}

		UIUtil.makeSpringCompactGrid(springPanel, 5, 3, 5, 5, 5, 5);
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(springPanel, BorderLayout.NORTH);

		argModel = new DefaultTableModel(0, 1);
		ModifiableTable argTable = new ModifiableTable(argModel,
				BorderLayout.SOUTH, ModifiableTable.ALL_BUTTONS);
		argTable.getTable().setTableHeader(null);
		Dimension s = argTable.getTable().getPreferredScrollableViewportSize();
		s.height = 200; // JTable default is 400!
		argTable.getTable().setPreferredScrollableViewportSize(s);
		argTable.setRowHandler(new ArgTableRowHandler());
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.setBorder(BorderFactory.createTitledBorder(msg.getString("CommandLineArgs")));
		temp2.add(argTable);
		temp.add(temp2);
		tabPane.addTab(msg.getString("Tab.Main"), temp);

		JPanel envPanel = new JPanel(new BorderLayout());
		envPanel.setBorder(UIUtil.getEmpty5Border());
		ButtonGroup bg = new ButtonGroup();
		appendRB = UIUtil.newRadio(msg, "AppendEnvVars", bg, null, true);
		replaceRB = UIUtil.newRadio(msg, "ReplaceEnvVars", bg);
		temp = new JPanel(new GridLayout(2,1, 5,0));
		temp.add(appendRB);
		temp.add(replaceRB);
		temp2 = new JPanel(new BorderLayout());
		temp2.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		temp2.add(temp, BorderLayout.LINE_START);
		envPanel.add(temp2, BorderLayout.NORTH);
		envModel = new DefaultTableModel(
				new Object[] { msg.getString("VariableName"),
								msg.getString("VariableValue") }, 0);
		ModifiableTable envTable = new ModifiableTable(envModel,
				BorderLayout.SOUTH, ModifiableTable.ALL_BUTTONS);
		s = envTable.getTable().getPreferredScrollableViewportSize();
		s.height = 200; // JTable default is 400!
		envTable.getTable().setPreferredScrollableViewportSize(s);
		envTable.setRowHandler(new EnvVarTableRowHandler());
		envPanel.add(envTable);
		tabPane.addTab(msg.getString("Tab.Environment"), envPanel);

		JButton okButton = new JButton(msg.getString("OK"));
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		JButton cancelButton = UIUtil.newTabbedPaneButton(msg.getString("Cancel"));
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		Container buttonPanel=UIUtil.createButtonFooter(okButton, cancelButton);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		setContentPane(cp);
		getRootPane().setDefaultButton(okButton);
		setTitle(msg.getString("Title"));
		setModal(true);
		applyComponentOrientation(o);
		pack();
		setLocationRelativeTo(parent);

	}


	/**
	 * Creates a completion provider for tool variable completions.
	 *
	 * @return The completion provider.
	 */
	private static final CompletionProvider createToolVarCompletionProvider() {
		DefaultCompletionProvider p = new DefaultCompletionProvider();
		addToolVarCompletion(p, "Variable.FileName");
		addToolVarCompletion(p, "Variable.FileNameNoExt");
		addToolVarCompletion(p, "Variable.FileDir");
		addToolVarCompletion(p, "Variable.FileFullPath");
		return p;
	}


	/**
	 * Returns the tool created by the user.
	 *
	 * @return The tool, or <code>null</code> if the user canceled
	 *         the dialog.
	 * @see #setTool(Tool)
	 */
	public Tool getTool() {
		return tool;
	}


	/**
	 * Returns whether an identifier is a valid tool name.
	 *
	 * @param name The identifier to check.
	 * @return Whether the identifier is a valid tool name.
	 */
	private static final boolean isValidName(String name) {

		boolean valid = false;

		if (name.length()>0) {
			valid = true;
			for (int i=0; i<name.length(); i++) {
				char ch = name.charAt(i);
				if (!Character.isJavaIdentifierPart(ch)) {
					valid = false;
					break;
				}
			}
		}

		return valid;

	}


	/**
	 * Sets the tool being displayed by this dialog.
	 *
	 * @param tool The tool.
	 * @see #getTool()
	 */
	public void setTool(Tool tool) {

		origName = tool.getName();
		nameField.setText(origName);
		descField.setText(tool.getDescription());
		programField.setText(tool.getProgram());
		dirField.setText(tool.getDirectory());
		shortcutField.setKeyStroke(KeyStroke.getKeyStroke(
										tool.getAccelerator()));

		argModel.setRowCount(0);
		String[] args = tool.getArgs();
		for (int i=0; i<args.length; i++) {
			argModel.addRow(new String[] { args[i] });
		}

		if (tool.getAppendEnvironmentVars()) {
			appendRB.setSelected(true);
		}
		else {
			replaceRB.setSelected(true);
		}

		envModel.setRowCount(0);
		Map<String, String> envVars = tool.getEnvVars();
		for (Map.Entry<String, String> entry : envVars.entrySet()) {
			envModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
		}

	}


	/**
	 * Overridden to ensure that the first tab is always the selected one.
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			tabPane.setSelectedIndex(0);
			nameField.selectAll();
			nameField.requestFocusInWindow();
		}
		super.setVisible(visible);
	}


	/**
	 * Displays an error message.
	 *
	 * @param comp The component with invalid input.
	 * @param key A key into the resource bundle for the error.
	 * @param param An optional parameter for the localized error.  This may
	 *        be <code>null</code>.
	 */
	private void showError(JComponent comp, String key, String param) {
		String desc = msg.getString(key);
		if (param!=null) {
			desc = MessageFormat.format(desc, new Object[] { param });
		}
		String title = msg.getString("Error.Title");
		JOptionPane.showMessageDialog(this, desc, title,
									JOptionPane.ERROR_MESSAGE);
		comp.requestFocusInWindow();
		if (comp instanceof JTextComponent) {
			((JTextComponent) comp).selectAll();
		}
	}


	/**
	 * The dialog that allows the user to add or modify a command line
	 * argument.
	 */
	private class ArgDialog extends EscapableDialog implements ActionListener,
									DocumentListener {

		private JTextField argField;
		private JButton okButton;
		private String arg;

		public ArgDialog(JDialog parent) {

			super(parent);
			ComponentOrientation o = parent.getComponentOrientation();

			JPanel cp = new ResizableFrameContentPane(new BorderLayout());
			cp.setBorder(UIUtil.getEmpty5Border());

			JPanel temp = new JPanel(new BorderLayout());
			JLabel argLabel = new JLabel(msg.getString("ArgumentDialog.Argument"));
			temp.add(argLabel, BorderLayout.LINE_START);
			argField = new JTextField(20);
			CompletionProvider provider = createToolVarCompletionProvider();
			AutoCompletion ac = new AutoCompletion(provider);
			ac.setAutoCompleteSingleChoices(false);
			ac.install(argField);
			AssistanceIconPanel aip = new AssistanceIconPanel(argField);
			aip.setAssistanceEnabled(AbstractSearchDialog.getContentAssistImage());
			JPanel argFieldPanel = RTextUtilities.createAssistancePanel(argField, aip);
			argField.getDocument().addDocumentListener(this);
			JPanel temp2 = new JPanel(new BorderLayout());
			temp2.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
			temp2.add(argFieldPanel);
			temp.add(temp2);
			VariableButton varButton = new VariableButton(argField);
			temp.add(varButton, BorderLayout.LINE_END);

			// Add OptionPane icon if one is defined.
			Icon icon = UIManager.getIcon("OptionPane.questionIcon");
			if (icon!=null) {
				temp2 = new JPanel(new BorderLayout());
				JLabel iconLabel = new JLabel(icon);
				iconLabel.setBorder(o.isLeftToRight() ?
						BorderFactory.createEmptyBorder(0, 0, 0, 8) :
							BorderFactory.createEmptyBorder(0, 8, 0, 0));
				temp2.add(iconLabel, BorderLayout.LINE_START);
				JPanel temp3 = new JPanel(new BorderLayout());
				temp3.add(temp, BorderLayout.NORTH);
				temp2.add(temp3);
				temp = temp2;
			}
			cp.add(temp, BorderLayout.NORTH);

			okButton = new JButton(msg.getString("OK"));
			okButton.setEnabled(false);
			okButton.setActionCommand("OK");
			okButton.addActionListener(this);
			JButton cancelButton = new JButton(msg.getString("Cancel"));
			cancelButton.setActionCommand("Cancel");
			cancelButton.addActionListener(this);
			Container buttons=UIUtil.createButtonFooter(okButton, cancelButton);
			cp.add(buttons, BorderLayout.SOUTH);

			setTitle(msg.getString("ArgumentDialog.Title"));
			getRootPane().setDefaultButton(okButton);
			setContentPane(cp);
			applyComponentOrientation(o);
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

		public void changedUpdate(DocumentEvent e) {
		}

		public void insertUpdate(DocumentEvent e) {
			okButton.setEnabled(true);
		}

		public void removeUpdate(DocumentEvent e) {
			okButton.setEnabled(e.getDocument().getLength()>0);
		}

		public void setArg(String arg) {
			argField.setText(arg);
			argField.selectAll();
			okButton.setEnabled(arg!=null && arg.length()>0);
		}

	}


	/**
	 * Row handler for the command line arguments table.
	 */
	private class ArgTableRowHandler extends AbstractRowHandler {

		public Object[] getNewRowInfo(Object[] oldData) {
			ArgDialog dialog = new ArgDialog(NewToolDialog.this);
			if (oldData!=null) {
				dialog.setArg((String)oldData[0]);
			}
			dialog.setModal(true);
			dialog.setVisible(true);
			return dialog.arg==null ? null : new String[] { dialog.arg };
		}

	}


	/**
	 * The dialog that allows the user to add or modify an environment variable.
	 */
	private class EnvVarDialog extends EscapableDialog
								implements DocumentListener, ActionListener {

		private JTextField nameField;
		private JTextField valueField;
		private JButton okButton;
		private boolean escaped;

		public EnvVarDialog(JDialog parent) {

			super(parent);
			ComponentOrientation o = parent.getComponentOrientation();
			escaped = true;

			JPanel cp = new ResizableFrameContentPane(new BorderLayout());
			cp.setBorder(UIUtil.getEmpty5Border());

			JLabel nameLabel = new JLabel(msg.getString("EnvVarDialog.Name"));
			nameField = new JTextField(20);
			nameField.getDocument().addDocumentListener(this);
			JPanel nameFieldPanel = RTextUtilities.createAssistancePanel(nameField, null);
			JLabel valueLabel = new JLabel(msg.getString("EnvVarDialog.Value"));
			valueField = new JTextField(20);
			CompletionProvider provider = createToolVarCompletionProvider();
			AutoCompletion ac = new AutoCompletion(provider);
			ac.setAutoCompleteSingleChoices(false);
			ac.install(valueField);
			AssistanceIconPanel aip = new AssistanceIconPanel(valueField);
			aip.setAssistanceEnabled(AbstractSearchDialog.getContentAssistImage());
			JPanel valueFieldPanel = RTextUtilities.createAssistancePanel(valueField, aip);
			VariableButton varButton = new VariableButton(valueField);

			JPanel temp = new JPanel(new SpringLayout());
			Dimension dim = new Dimension(1, 1); // MUST have finite width!
			if (parent.getComponentOrientation().isLeftToRight()) {
				temp.add(nameLabel); temp.add(nameFieldPanel); temp.add(Box.createRigidArea(dim));
				temp.add(valueLabel); temp.add(valueFieldPanel); temp.add(varButton);
			}
			else {
				temp.add(Box.createRigidArea(dim)); temp.add(nameFieldPanel); temp.add(nameLabel);
				temp.add(varButton); temp.add(valueFieldPanel); temp.add(valueLabel);
			}
			UIUtil.makeSpringCompactGrid(temp, 2, 3, 5, 5, 5, 5);

			// Add OptionPane icon if one is defined.
			Icon icon = UIManager.getIcon("OptionPane.questionIcon");
			if (icon!=null) {
				JPanel temp2 = new JPanel(new BorderLayout());
				JLabel iconLabel = new JLabel(icon);
				iconLabel.setBorder(o.isLeftToRight() ?
						BorderFactory.createEmptyBorder(0, 0, 0, 8) :
							BorderFactory.createEmptyBorder(0, 8, 0, 0));
				JPanel temp3 = new JPanel(new BorderLayout());
				temp3.add(iconLabel, BorderLayout.NORTH);
				temp2.add(temp3, BorderLayout.LINE_START);
				temp3 = new JPanel(new BorderLayout());
				temp3.add(temp, BorderLayout.NORTH);
				temp2.add(temp3);
				temp = temp2;
			}
			cp.add(temp, BorderLayout.NORTH);

			okButton = new JButton(msg.getString("OK"));
			okButton.setEnabled(false);
			okButton.setActionCommand("OK");
			okButton.addActionListener(this);
			JButton cancelButton = new JButton(msg.getString("Cancel"));
			cancelButton.setActionCommand("Cancel");
			cancelButton.addActionListener(this);
			Container buttons=UIUtil.createButtonFooter(okButton, cancelButton);
			cp.add(buttons, BorderLayout.SOUTH);

			setTitle(msg.getString("EnvVarDialog.Title"));
			getRootPane().setDefaultButton(okButton);
			setContentPane(cp);
			applyComponentOrientation(o);
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

		public void changedUpdate(DocumentEvent e) {
		}

		public String[] getReturnValue() {
			return escaped ? null :
				new String[] { nameField.getText(), valueField.getText() };
		}

		public void insertUpdate(DocumentEvent e) {
			okButton.setEnabled(true);
		}

		public void removeUpdate(DocumentEvent e) {
			okButton.setEnabled(e.getDocument().getLength()>0);
		}

		public void setData(String name, String value) {
			nameField.setText(name);
			valueField.setText(value);
			okButton.setEnabled(name!=null && name.length()>0);
		}

	}


	/**
	 * Row handler for the environment variable table.
	 */
	private class EnvVarTableRowHandler extends AbstractRowHandler {

		public Object[] getNewRowInfo(Object[] oldData) {
			EnvVarDialog dialog = new EnvVarDialog(NewToolDialog.this);
			if (oldData!=null) {
				dialog.setData((String)oldData[0], (String)oldData[1]);
			}
			dialog.setModal(true);
			dialog.setVisible(true);
			return dialog.getReturnValue();
		}

	}


	/**
	 * An action that inserts text into a text field.
	 */
	private static class VariableAction extends AbstractAction {

		private JTextField field;
		private String replacement;

		public VariableAction(String nameKey, String replacement,
								JTextField field) {
			putValue(NAME, msg.getString(nameKey));
			this.field = field;
			this.replacement = replacement;
		}

		public void actionPerformed(ActionEvent e) {
			field.replaceSelection(replacement);
			field.requestFocusInWindow();
		}

	}


	/**
	 * A button with a popup menu allowing the insertion of variables into
	 * a text field.
	 */
	private static class VariableButton extends MenuButton {

		public VariableButton(JTextField field) {
			super(null);
			setText(msg.getString("Variables"));
			setHorizontalTextPosition(SwingConstants.LEADING);
			addMenuItem(new VariableAction("Variable.FileName", "${file_name}", field));
			addMenuItem(new VariableAction("Variable.FileNameNoExt", "${file_name_no_ext}", field));
			addMenuItem(new VariableAction("Variable.FileDir", "${file_dir}", field));
			addMenuItem(new VariableAction("Variable.FileFullPath", "${file_full_path}", field));
		}

		public void addMenuItem(VariableAction a) {
			addMenuItem(new JMenuItem(a));
		}

	}


}