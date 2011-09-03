/*
 * 05/20/2010
 *
 * JavaOptionsPanel.java - Options for Java language support.
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
package org.fife.rtext.plugins.langsupport;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.java.JarInfo;
import org.fife.rsta.ac.java.JarManager;
import org.fife.rsta.ac.java.JavaLanguageSupport;
import org.fife.rtext.NumberDocumentFilter;
import org.fife.ui.EscapableDialog;
import org.fife.ui.FSATextField;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RButton;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.ModifiableTableChangeEvent;
import org.fife.ui.modifiabletable.ModifiableTableListener;
import org.fife.ui.modifiabletable.RowHandler;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextfilechooser.RDirectoryChooser;


/**
 * The options panel for Java-specific language support options.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class JavaOptionsPanel extends OptionsDialogPanel {

	private Listener listener;
	private JCheckBox enabledCB;
	private JCheckBox paramAssistanceCB;
	private JCheckBox showDescWindowCB;
	private ModifiableTable bpt;
	private DefaultTableModel model;
	private JarRowHandler rowHandler;
	private RButton addJREButton;
	private JCheckBox buildPathModsCB;
	private JCheckBox autoActivateCB;
	private JLabel aaDelayLabel;
	private JTextField aaDelayField;
	private JLabel aaJavaKeysLabel;
	private JTextField aaJavaKeysField;
	private JLabel aaDocKeysLabel;
	private JTextField aaDocKeysField;
	private RButton rdButton;

	private static final String PROPERTY		= "Property";


	/**
	 * Constructor.
	 */
	public JavaOptionsPanel() {

		ResourceBundle msg = Plugin.msg;
		setName(msg.getString("Options.Java.Name"));
		listener = new Listener();
		setIcon(new ImageIcon(getClass().getResource("cup.png")));

		ComponentOrientation o = ComponentOrientation.
											getOrientation(getLocale());

		setLayout(new BorderLayout());
		Border empty5Border = UIUtil.getEmpty5Border();
		setBorder(empty5Border);

		Box cp = Box.createVerticalBox();
		cp.setBorder(null);
		add(cp, BorderLayout.NORTH);

		Box box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(msg.
				getString("Options.General.Section.General")));
		cp.add(box);
		cp.add(Box.createVerticalStrut(5));

		enabledCB = createCB("Options.Java.EnableCodeCompletion");
		addLeftAligned(box, enabledCB, 5);

		Box box2 = Box.createVerticalBox();
		if (o.isLeftToRight()) {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		}
		else {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
		}
		box.add(box2);

		showDescWindowCB = createCB("Options.General.ShowDescWindow");
		addLeftAligned(box2, showDescWindowCB, 5);

		paramAssistanceCB = createCB("Options.General.ParameterAssistance");
		addLeftAligned(box2, paramAssistanceCB, 5);

		box2.add(Box.createVerticalGlue());

		box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(
				msg.getString("Options.General.AutoActivation")));
		cp.add(box);

		autoActivateCB = createCB("Options.General.EnableAutoActivation");
		addLeftAligned(box, autoActivateCB, 5);

		box2 = Box.createVerticalBox();
		if (o.isLeftToRight()) {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		}
		else {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
		}
		box.add(box2);

		SpringLayout sl = new SpringLayout();
		JPanel temp = new JPanel(sl);
		aaDelayLabel = new JLabel(msg.getString("Options.General.AutoActivationDelay"));
		aaDelayField = new JTextField(10);
		AbstractDocument doc = (AbstractDocument)aaDelayField.getDocument();
		doc.setDocumentFilter(new NumberDocumentFilter());
		doc.addDocumentListener(listener);
		aaJavaKeysLabel = new JLabel(msg.getString("Options.Java.AutoActivationJavaKeys"));
		aaJavaKeysLabel.setEnabled(false);
		aaJavaKeysField = new JTextField(".", 10);
		aaJavaKeysField.setEnabled(false);
		aaDocKeysLabel = new JLabel(msg.getString("Options.Java.AutoActionDocCommentKeys"));
		aaDocKeysLabel.setEnabled(false);
		aaDocKeysField = new JTextField("@", 10);
		aaDocKeysField.setEnabled(false);
		Dimension d = new Dimension(5, 5);
		Dimension spacer = new Dimension(30, 5);
		if (o.isLeftToRight()) {
			temp.add(aaDelayLabel);				temp.add(aaDelayField);
			temp.add(Box.createRigidArea(spacer));
			temp.add(aaJavaKeysLabel);			temp.add(aaJavaKeysField);
			temp.add(Box.createRigidArea(d));	temp.add(Box.createRigidArea(d));
			temp.add(Box.createRigidArea(spacer));
			temp.add(aaDocKeysLabel);			temp.add(aaDocKeysField);
		}
		else {
			temp.add(aaDelayField);			temp.add(aaDelayLabel);
			temp.add(Box.createRigidArea(spacer));
			temp.add(aaJavaKeysField);		temp.add(aaJavaKeysLabel);
			temp.add(Box.createRigidArea(d));	temp.add(Box.createRigidArea(d));
			temp.add(Box.createRigidArea(spacer));
			temp.add(aaDocKeysField);		temp.add(aaDocKeysLabel);
		}
		UIUtil.makeSpringCompactGrid(temp, 2,5, 0,0, 5,5);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(temp, BorderLayout.LINE_START);
		box2.add(temp2);

		box2.add(Box.createVerticalGlue());

		box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(
				msg.getString("Options.Java.BuildPath")));
		cp.add(box);

		model = new DefaultTableModel(0, 2);
		String[] colNames = { msg.getString("Options.Java.JarFile"),
							msg.getString("Options.Java.SourceLocation") };
		bpt = new ModifiableTable(model, colNames,
					ModifiableTable.BOTTOM, ModifiableTable.ADD_REMOVE_MODIFY);
		bpt.addModifiableTableListener(listener);
		bpt.getTable().setPreferredScrollableViewportSize(
													new Dimension(50, 16*8));
		rowHandler = new JarRowHandler();
		bpt.setRowHandler(rowHandler);
		box.add(bpt);
		box.add(Box.createVerticalStrut(5));

		addJREButton = new RButton(msg.getString("Options.Java.AddJRE"));
		addJREButton.addActionListener(listener);
		addLeftAligned(box, addJREButton, 5);

		buildPathModsCB = createCB("CheckForBuildPathMods");
		addLeftAligned(box, buildPathModsCB, 5);
		box.add(Box.createVerticalGlue());

		cp.add(Box.createVerticalStrut(5));
		rdButton = new RButton(msg.getString("Options.General.RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(cp, rdButton, 5);

		cp.add(Box.createVerticalGlue());

		applyComponentOrientation(o);

	}


//	private void addLeftAligned(Box to, Component c) {
//		JPanel panel = new JPanel(new BorderLayout());
//		panel.add(c, BorderLayout.LINE_START);
//		to.add(panel);
//		to.add(Box.createVerticalStrut(5));
//	}


	private JCheckBox createCB(String key) {
		if (key.indexOf('.')==-1) {
			key = "Options.Java." + key;
		}
		JCheckBox cb = new JCheckBox(Plugin.msg.getString(key));
		cb.addActionListener(listener);
		return cb;
	}


	/**
	 * {@inheritDoc}
	 */
	protected void doApplyImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport ls=lsf.getSupportFor(SyntaxConstants.SYNTAX_STYLE_JAVA);
		JavaLanguageSupport jls = (JavaLanguageSupport)ls;
		JarManager jarMan = jls.getJarManager();

		// Options dealing with code completion.
		jls.setAutoCompleteEnabled(enabledCB.isSelected());
		jls.setParameterAssistanceEnabled(paramAssistanceCB.isSelected());
		jls.setShowDescWindow(showDescWindowCB.isSelected());
		JarManager.setCheckModifiedDatestamps(buildPathModsCB.isSelected());

		// Options dealing with auto-activation.
		jls.setAutoActivationEnabled(autoActivateCB.isSelected());
		int delay = 300;
		String temp = aaDelayField.getText();
		if (temp.length()>0) {
			try {
				delay = Integer.parseInt(aaDelayField.getText());
			} catch (NumberFormatException nfe) { // Never happens
				nfe.printStackTrace();
			}
		}
		jls.setAutoActivationDelay(delay);
		// TODO: Trigger keys for Java and Javadoc?

		// Options dealing with the build path.
		// TODO: This is very inefficient!  This will always create new
		// JarReaders for all jars, even if it isn't necessary.  This will
		// cause a pause when ctrl+spacing for the first time.
		jarMan.clearJars();
		for (int i=0; i<model.getRowCount(); i++) {
			File jar = (File)model.getValueAt(i, 0);
			JarInfo info = new JarInfo(jar);
			String source = (String)model.getValueAt(i, 1);
			if (source!=null && source.length()>0) {
				info.setSourceLocation(new File(source));
			}
			try {
				jarMan.addJar(info);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

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
		return enabledCB;
	}


	/**
	 * Populates the table model for the "build path" table.
	 *
	 * @param jarMan The shared jar manager.
	 */
	private void populateTableModel(JarManager jarMan) {

		model.setRowCount(0);

		List jars = jarMan.getJars();
		for (Iterator i=jars.iterator(); i.hasNext(); ) { 
			JarInfo info = (JarInfo)i.next();
			File jar = info.getJarFile();
			File sourceFile = info.getSourceLocation();
			String source = sourceFile!=null ? sourceFile.getAbsolutePath() : null;
			model.addRow(new Object[] { jar, source });
		}

	}


	private void setAutoActivateCBSelected(boolean selected) {
		autoActivateCB.setSelected(selected);
		aaDelayLabel.setEnabled(selected);
		aaDelayField.setEnabled(selected);
		//aaJavaKeysLabel.setEnabled(selected);
		//aaJavaKeysField.setEnabled(selected);
		//aaDocKeysLabel.setEnabled(selected);
		//aaDocKeysField.setEnabled(selected);
	}


	private void setEnabledCBSelected(boolean selected) {
		enabledCB.setSelected(selected);
		paramAssistanceCB.setEnabled(selected);
		showDescWindowCB.setEnabled(selected);
		buildPathModsCB.setEnabled(selected);
	}


	/**
	 * {@inheritDoc}
	 */
	protected void setValuesImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport ls=lsf.getSupportFor(SyntaxConstants.SYNTAX_STYLE_JAVA);
		JavaLanguageSupport jls = (JavaLanguageSupport)ls;

		// Options dealing with code completion
		setEnabledCBSelected(jls.isAutoCompleteEnabled());
		paramAssistanceCB.setSelected(jls.isParameterAssistanceEnabled());
		showDescWindowCB.setSelected(jls.getShowDescWindow());
		buildPathModsCB.setSelected(JarManager.getCheckModifiedDatestamps());

		// Options dealing with auto-activation
		setAutoActivateCBSelected(jls.isAutoActivationEnabled());
		aaDelayField.setText(Integer.toString(jls.getAutoActivationDelay()));
		// TODO: Trigger keys for Java and Javadoc?

		// Options dealing with the build path.
		populateTableModel(jls.getJarManager());

	}


	/**
	 * Handler for editing jars/source attachments in the table.
	 */
	private class JarRowHandler implements RowHandler {

		public Object[] getNewRowInfo(Object[] old) {
			RowHandlerDialog rhd = new RowHandlerDialog(getOptionsDialog(),
														old);
			rhd.setLocationRelativeTo(getOptionsDialog());
			rhd.setVisible(true);
			return rhd.newRowInfo;
		}

		public boolean shouldRemoveRow(int arg0) {
			return true;
		}

		public void updateUI() {
			// Nothing to do
		}

	}


	private static class RowHandlerDialog extends EscapableDialog
							implements ActionListener {

		private FSATextField jarField;
		private FSATextField sourceField;
		private RButton okButton;
		private RButton cancelButton;
		private Object[] newRowInfo;

		private RowHandlerDialog(JDialog parent, Object[] old) {

			super(parent);
			JPanel cp = new ResizableFrameContentPane(new BorderLayout());
			cp.setBorder(UIUtil.getEmpty5Border());

			JPanel topPanel = new JPanel(new SpringLayout());

			JLabel jarLabel = new JLabel(getString("Jar"));
			jarField = new FSATextField(40);
			if (old!=null) {
				jarField.setText(((File)old[0]).getAbsolutePath());
			}
			jarLabel.setLabelFor(jarField);

			JLabel sourceLabel = new JLabel(getString("Source"));
			sourceField = new FSATextField(40);
			if (old!=null) {
				sourceField.setText((String)old[1]);
			}
			sourceLabel.setLabelFor(sourceField);

			if (getComponentOrientation().isLeftToRight()) {
				topPanel.add(jarLabel);     topPanel.add(jarField);
				topPanel.add(sourceLabel);  topPanel.add(sourceField);
			}
			else {
				topPanel.add(jarField);     topPanel.add(jarLabel);
				topPanel.add(sourceField);  topPanel.add(sourceLabel);
			}
			UIUtil.makeSpringCompactGrid(topPanel, 2, 2, 5, 5, 5, 5);
			cp.add(topPanel, BorderLayout.NORTH);

			JPanel buttonPanel = new JPanel();
			JPanel temp = new JPanel(new GridLayout(1,2, 5,5));
			okButton = new RButton(Plugin.msg.getString("Options.General.OK"));
			okButton.addActionListener(this);
			temp.add(okButton);
			cancelButton = new RButton(Plugin.msg.
								getString("Options.General.Cancel"));
			cancelButton.addActionListener(this);
			temp.add(cancelButton);
			buttonPanel.add(temp);
			cp.add(buttonPanel, BorderLayout.SOUTH);

			setContentPane(cp);
			getRootPane().setDefaultButton(okButton);
			setTitle(getString("Title"));
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setModal(true);
			pack();

		}

		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (okButton==source) {
				newRowInfo = new Object[] {
						new File(jarField.getText()),
						sourceField.getText() };
				escapePressed();
			}

			else if (cancelButton==source) {
				escapePressed();
			}

		}

		private String getString(String keySuffix) {
			String key = "Options.Java.BuildPathDialog." + keySuffix;
			return Plugin.msg.getString(key);
		}

	}


	/**
	 * Listens for events in this options panel.
	 */
	private class Listener implements ActionListener, DocumentListener,
								ModifiableTableListener {

		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (enabledCB==source) {
				// Trick related components to toggle enabled states
				setEnabledCBSelected(enabledCB.isSelected());
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (paramAssistanceCB==source ||
					showDescWindowCB==source ||
					buildPathModsCB==source) {
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (autoActivateCB==source) {
				// Trick related components to toggle enabled states
				setAutoActivateCBSelected(autoActivateCB.isSelected());
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (addJREButton==source) {
				RDirectoryChooser chooser = new RDirectoryChooser(
														getOptionsDialog());
				chooser.setVisible(true);
				String dir = chooser.getChosenDirectory();
				if (dir!=null) {
					JarInfo info = JarInfo.getJREJarInfo(new File(dir));
					if (info!=null) {
						String src = null;
						if (info.getSourceLocation()!=null) {
							src = info.getSourceLocation().getAbsolutePath();
						}
						model.addRow(new Object[] { info.getJarFile(), src });
						hasUnsavedChanges = true;
						firePropertyChange(PROPERTY, null, null);
					}
				}
			}

			else if (rdButton==source) {

				JarInfo jreInfo = JarInfo.getMainJREJarInfo();

				int rowCount = model.getRowCount();
				boolean jreFieldModified = rowCount!=1 ||
					!((File)model.getValueAt(0, 0)).equals(
												jreInfo.getJarFile());

				if (enabledCB.isSelected() ||
						jreFieldModified ||
						!paramAssistanceCB.isSelected() ||
						!showDescWindowCB.isSelected() ||
						!buildPathModsCB.isSelected() ||
						autoActivateCB.isSelected() ||
						!"300".equals(aaDelayField.getText())) {
					setEnabledCBSelected(false);
					paramAssistanceCB.setSelected(true);
					showDescWindowCB.setSelected(true);
					buildPathModsCB.setSelected(true);
					setAutoActivateCBSelected(false);
					aaDelayField.setText("300");
					model.setRowCount(0);
					String src = jreInfo.getSourceLocation()!=null ?
							jreInfo.getSourceLocation().getAbsolutePath() :
								null;
					model.addRow(new Object[] {
							jreInfo.getJarFile(),
							src });
					hasUnsavedChanges = true;
					firePropertyChange(PROPERTY, null, null);
				}

			}

		}

		public void changedUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		private void handleDocumentEvent(DocumentEvent e) {
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, null, null);
		}

		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		public void modifiableTableChanged(ModifiableTableChangeEvent e) {
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, null, null);
		}

		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

	}


}