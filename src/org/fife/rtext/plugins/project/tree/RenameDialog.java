package org.fife.rtext.plugins.project.tree;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;

import org.fife.rsta.ui.EscapableDialog;
import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.Messages;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;


/**
 * A dialog for renaming nodes in the workspace outline tree.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RenameDialog extends EscapableDialog{

	private JButton okButton;
	private JButton cancelButton;
	private JTextField nameField;

	private RText owner;


	/**
	 * Constructor.
	 *
	 * @param owner The rtext window that owns this dialog.
	 * @param type The type of node being renamed.
	 */
	public RenameDialog(RText owner, String type, DocumentFilter filter) {

		super(owner);
		this.owner = owner;
		Listener listener = new Listener();

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
		ResourceBundle bundle = owner.getResourceBundle();

		JPanel cp =new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		setContentPane(cp);

		// A panel containing the main content.
		String key = "RenameDialog.Field.Label";
		JLabel label = new JLabel(Messages.getString(key));
		label.setDisplayedMnemonic(Messages.getString(key + ".Mnemonic").charAt(0));
		nameField = new JTextField(40);
		((AbstractDocument)nameField.getDocument()).setDocumentFilter(filter);
		label.setLabelFor(nameField);
		Box box = new Box(BoxLayout.LINE_AXIS);
		box.add(label);
		box.add(Box.createHorizontalStrut(5));
		box.add(nameField);
		box.add(Box.createHorizontalGlue());

		// Make a panel containing the OK and Cancel buttons.
		JPanel buttonPanel = new JPanel(new GridLayout(1,2, 5,5));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
		okButton = UIUtil.createRButton(bundle, "OKButtonLabel", "OKButtonMnemonic");
		okButton.setActionCommand("OK");
		okButton.addActionListener(listener);
		cancelButton = UIUtil.createRButton(bundle, "Cancel", "CancelMnemonic");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(listener);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		// Put everything into a neat little package.
		cp.add(box, BorderLayout.NORTH);
		JPanel temp = new JPanel();
		temp.add(buttonPanel);
		cp.add(temp, BorderLayout.SOUTH);
		JRootPane rootPane = getRootPane();
		rootPane.setDefaultButton(okButton);
		setTitle(Messages.getString("RenameDialog.Title", type));
		setModal(true);
		applyComponentOrientation(orientation);
		pack();
		setLocationRelativeTo(owner);

	}


	/**
	 * Returns the name selected.
	 *
	 * @return The name selected, or <code>null</code> if the dialog was
	 *         cancelled.
	 * @see #setName(String)
	 */
	public String getName() {
		String name = nameField.getText();
		return name.length()>0 ? name : null;
	}


	/**
	 * Sets the name displayed in this dialog.
	 *
	 * @param name The name to display.
	 * @see #getName()
	 */
	public void setName(String name) {
		nameField.setText(name);
		nameField.requestFocusInWindow();
		nameField.selectAll();
	}


	/**
	 * Listens for events in this dialog.
	 */
	private class Listener implements ActionListener, DocumentListener {

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("OK")) {
				escapePressed();
				nameField.setText(null); // So user gets back nothing
			}
			else if (command.equals("Cancel")) {
				escapePressed();
			}
		}

		public void changedUpdate(DocumentEvent e) {
		}

		public void insertUpdate(DocumentEvent e) {
			okButton.setEnabled(nameField.getDocument().getLength()>0);
		}

		public void removeUpdate(DocumentEvent e) {
			okButton.setEnabled(nameField.getDocument().getLength()>0);
		}

	}


}