/*
 * 01/12/2005
 *
 * TemplateOptionPanel.java - An option panel to manage RSyntaxTextArea's
 * templates.
 * Copyright (C) 2005 Robert Futrell
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
package org.fife.ui.rsyntaxtextarea;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

import org.fife.ui.*;
import org.fife.ui.modifiabletable.*;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.templates.CodeTemplate;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;
import org.fife.ui.rtextarea.RTextArea;


/**
 * An options panel that can be added to an
 * <code>org.fife.ui.OptionsDialog</code> to manage the templates
 * available to all instances of <code>RSyntaxTextArea</code>.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class TemplateOptionPanel extends OptionsDialogPanel {

	private ModifiableTable templateTable;
	private DefaultTableModel tableModel;
	private ResourceBundle msg;

	private static final String TEMPLATE_PROPERTY	= "template";


	/**
	 * Constructor.
	 */
	public TemplateOptionPanel() {

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		msg = ResourceBundle.getBundle(
					"org.fife.ui.rsyntaxtextarea.TemplateOptionPanel");
		setName(msg.getString("Title"));
		Listener listener = new Listener();

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(new OptionPanelBorder(msg.getString("Templates")));
		add(contentPane);

		tableModel = new DefaultTableModel(new Object[] {
					msg.getString("Template"), msg.getString("Expansion") }, 0);
		templateTable = new ModifiableTable(tableModel);
		JTable table = templateTable.getTable();
		table.getColumn(msg.getString("Expansion")).
							setCellRenderer(new ExpansionCellRenderer());
		table.setPreferredScrollableViewportSize(new Dimension(300, 300));
		templateTable.addModifiableTableListener(listener);

		contentPane.add(templateTable);
		applyComponentOrientation(orientation);

		// Must create and set row handler later, after this option panel has
		// been added to the parent Options dialog.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				RowHandler rowHandler = new TemplateDialog();
				templateTable.setRowHandler(rowHandler);
			}
		});

	}


	/**
	 * Applies the settings entered into this dialog on the specified
	 * application.
	 *
	 * @param owner The application.
	 */
	protected void doApplyImpl(Frame owner) {
		updateCodeTemplateManager();
	}


	/**
	 * Checks whether or not all input the user specified on this panel is
	 * valid.
	 *
	 * @return This method always returns <code>null</code> as the user cannot
	 *         mess up input in this panel.
	 */
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * Returns the <code>JComponent</code> at the "top" of this Options
	 * panel.  This is the component that will receive focus if the user
	 * switches to this Options panel in the Options dialog.  As an added
	 * bonus, if this component is a <code>JTextComponent</code>, its
	 * text is selected for easy changing.
	 */
	public JComponent getTopJComponent() {
		return templateTable.getTable();
	}


	/**
	 * Sets the table's contents to be the templates known by the specified
	 * template manager.
	 */
	public void setTemplates(CodeTemplateManager manager) {
		tableModel.setRowCount(0);
		if (manager!=null) {
			CodeTemplate[] templates = manager.getTemplates();
			int count = templates.length;
			for (int i=0; i<count; i++) {
				tableModel.addRow(new Object[] {
					new String(templates[i].getID()),
					// Deep copy.
					templates[i].clone()
				});
			}
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
		// Remove all old templates and load the new ones.
		CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();
		setTemplates(ctm);
	}


	/**
	 * Updates <code>RSyntaxTextArea</code>'s code template manager
	 * with the information entered here by the user.
	 */
	public void updateCodeTemplateManager() {

		CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();
		if (ctm!=null) {
			int count = tableModel.getRowCount();
			CodeTemplate[] templates = new CodeTemplate[count];
			for (int i=0; i<count; i++) {
				// Make deep copies in case they're clicking "Apply."
				CodeTemplate t = (CodeTemplate)tableModel.getValueAt(i, 1);
				templates[i] = (CodeTemplate)t.clone();
			}
			ctm.replaceTemplates(templates);
		}

	}


	/**
	 * Renders an "expansion" cell for the table.  This simply paints newlines
	 * as "\n" and tabs as "\t".
	 */
	private static class ExpansionCellRenderer extends DefaultTableCellRenderer{

		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected,
					boolean hasFocus, int row, int column) 
		{
			super.getTableCellRendererComponent(table, value, isSelected,
										hasFocus, row, column);
			StaticCodeTemplate template = (StaticCodeTemplate)value;
			String foo = template.getBeforeCaretText() +
						template.getAfterCaretText();
			this.setText(foo.replaceAll("\\n", "\\\\n").
							replaceAll("\\t", "\\\\t"));
			setComponentOrientation(table.getComponentOrientation());
			return this;
		}

	}


	/**
	 * Listens for events in this option panel.
	 */
	class Listener implements ModifiableTableListener {

		// A row was added, removed or modified in the template table.
		public void modifiableTableChanged(ModifiableTableChangeEvent e) {
			hasUnsavedChanges = true;
			TemplateOptionPanel.this.firePropertyChange(TEMPLATE_PROPERTY,
											null, templateTable);
		}

	}


	/**
	 * Dialog for modifying a template.
	 */
	class TemplateDialog extends EscapableDialog implements ActionListener,
									DocumentListener, RowHandler {

		private JTextField idField;
		private RTextArea bcTextArea;
		private RTextArea acTextArea;
		private RButton okButton;
		private RButton cancelButton;

		private char[] id;
		private String beforeCaret;
		private String afterCaret;

		public TemplateDialog() {

			super(getOptionsDialog());

			ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

			JPanel contentPane = new ResizableFrameContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.setBorder(UIUtil.getEmpty5Border());

			JLabel idLabel = UIUtil.createLabel(msg,
									"ID", "IDMnemonic");
			idField = new JTextField(20);
			Document doc = idField.getDocument();
			doc.addDocumentListener(this);
			if (doc instanceof AbstractDocument) {
				((AbstractDocument)doc).setDocumentFilter(
					new TemplateNameDocumentFilter());
			}
			idLabel.setLabelFor(idField);
			JPanel temp = new JPanel();
			temp.setLayout(new BoxLayout(temp, BoxLayout.LINE_AXIS));
			temp.add(idLabel);
			temp.add(Box.createHorizontalStrut(5));
			temp.add(idField);
			temp.add(Box.createHorizontalGlue());
			contentPane.add(temp, BorderLayout.NORTH);

			temp = new JPanel(new GridLayout(2,1, 5,5));
			temp.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
			JLabel label = UIUtil.createLabel(msg,
							"BeforeCaretText", "BeforeCaretMnemonic");
			bcTextArea = new RTextArea(4, 30);
			bcTextArea.setHighlightCurrentLine(false);
			label.setLabelFor(bcTextArea);
			RScrollPane sp = new RScrollPane(bcTextArea);
			JPanel temp2 = new JPanel(new BorderLayout());
			temp2.add(label, BorderLayout.NORTH);
			temp2.add(sp);
			temp.add(temp2);
			label = UIUtil.createLabel(msg, "AfterCaretText",
							"AfterCaretMnemonic");
			acTextArea = new RTextArea(4, 30);
			acTextArea.setHighlightCurrentLine(false);
			label.setLabelFor(acTextArea);
			sp = new RScrollPane(acTextArea);
			temp2 = new JPanel(new BorderLayout());
			temp2.add(label, BorderLayout.NORTH);
			temp2.add(sp);
			temp.add(temp2);
			contentPane.add(temp);

			temp = new JPanel();
			temp2 = new JPanel(new GridLayout(1,2, 5,5));
			okButton = UIUtil.createRButton(msg,
								"OK", "OKMnemonic");
			okButton.addActionListener(this);
			okButton.setEnabled(false);
			temp2.add(okButton);
			cancelButton = UIUtil.createRButton(msg,
								"Cancel", "CancelMnemonic");
			cancelButton.addActionListener(this);
			temp2.add(cancelButton);
			temp.add(temp2);
			contentPane.add(temp, BorderLayout.SOUTH);

			setContentPane(contentPane);
			setLocationRelativeTo(TemplateOptionPanel.this);
			setModal(true);
			getRootPane().setDefaultButton(okButton);
			applyComponentOrientation(orientation);
			pack();

		}

		public void actionPerformed(ActionEvent e) {
			this.setVisible(false);
			Object source = e.getSource();
			if (source==okButton) {
				id = idField.getText().toCharArray();
				beforeCaret = bcTextArea.getText();
				afterCaret = acTextArea.getText();
			}
			else if (source==cancelButton) {
				id = null;
				beforeCaret = afterCaret = null;
			}
		}

		public void changedUpdate(DocumentEvent e) {
		}

		protected void escapePressed() {
			id = null;
			beforeCaret = afterCaret = null;
			setVisible(false);
		}

		/**
		 * @return The after-caret text entered, or <code>null</code> if
		 *         the dialog was cancelled.
		 */
		public String getAfterCaretText() {
			return afterCaret;
		}

		/**
		 * @return The before-caret text entered, or <code>null</code> if
		 *         the dialog was cancelled.
		 */
		public String getBeforeCaretText() {
			return beforeCaret;
		}

		/**
		 * @return The id entered, or <code>null</code> if
		 *         the dialog was cancelled.
		 */
		public char[] getID() {
			return id;
		}

		public Object[] getNewRowInfo(Object[] oldData) {
			if (oldData==null) { // Adding a new row.
				setTitle(msg.getString("AddTemplateTitle"));
				setID(null);
				setBeforeCaretText(null);
				setAfterCaretText(null);
			}
			else { // Modifying a row.
				setTitle(msg.getString("ModifyTemplateTitle"));
				setID((String)oldData[0]);
				StaticCodeTemplate template = (StaticCodeTemplate)oldData[1];
				setBeforeCaretText(template.getBeforeCaretText());
				setAfterCaretText(template.getAfterCaretText());
			}
			setVisible(true);
			char[] id = getID();
			Object[] objs = null;
			if (id!=null) {
				String idString = new String(id);
				objs = new Object[] { idString,
						new StaticCodeTemplate(idString,
									getBeforeCaretText(),
									getAfterCaretText())
				};
			}
			return objs;
		}

		public void insertUpdate(DocumentEvent e) {
			if (!okButton.isEnabled())
				okButton.setEnabled(true);
		}

		public void removeUpdate(DocumentEvent e) {
			int length = idField.getDocument().getLength();
			if (length==0)
				okButton.setEnabled(false);
		}

		public void setAfterCaretText(String text) {
			acTextArea.setText(text);
		}

		public void setBeforeCaretText(String text) {
			bcTextArea.setText(text);
		}

		public void setID(String id) {
			idField.setText(id);
		}

		public boolean shouldRemoveRow(int row) {
			return true;
		}

		/**
		 * Not an override.  Implements <code>RowHandler#updateUI()</code>.
		 */
		public void updateUI() {
			SwingUtilities.updateComponentTreeUI(this);
		}

	}


	/**
	 * A document filter that only allows letters, numbers, and
	 * '_' to go through.
	 */
	private static class TemplateNameDocumentFilter extends PickyDocumentFilter{

		protected String cleanseImpl(String text) {
			int length = text.length();
			for (int i=0; i<length; i++) {
				if (!RSyntaxUtilities.
					isLetterOrDigit(text.charAt(i)) &&
						text.charAt(i)!='_') {
					text = text.substring(0,i) + text.substring(i+1);
					i--;
					length--;
				}
			}
			return text;
		}

	}


}