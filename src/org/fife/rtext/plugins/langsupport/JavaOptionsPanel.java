/*
 * 05/20/2010
 *
 * JavaOptionsPanel.java - Options for Java language support.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import org.fife.rsta.ac.java.buildpath.DirSourceLocation;
import org.fife.rsta.ac.java.buildpath.JarLibraryInfo;
import org.fife.rsta.ac.java.buildpath.LibraryInfo;
import org.fife.rsta.ac.java.buildpath.SourceLocation;
import org.fife.rsta.ac.java.buildpath.ZipSourceLocation;
import org.fife.rsta.ac.java.JarManager;
import org.fife.rsta.ac.java.JavaLanguageSupport;
import org.fife.rtext.NumberDocumentFilter;
import org.fife.rtext.RText;
import org.fife.ui.EscapableDialog;
import org.fife.ui.FSATextField;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;
import org.fife.ui.modifiabletable.AbstractRowHandler;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.ModifiableTableChangeEvent;
import org.fife.ui.modifiabletable.ModifiableTableListener;
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
	private JButton addJREButton;
	private JCheckBox buildPathModsCB;
	private JCheckBox autoActivateCB;
	private JLabel aaDelayLabel;
	private JTextField aaDelayField;
	private JLabel aaJavaKeysLabel;
	private JTextField aaJavaKeysField;
	private JLabel aaDocKeysLabel;
	private JTextField aaDocKeysField;
	private JButton rdButton;

	private static final String PROPERTY		= "Property";


	/**
	 * Constructor.
	 */
	public JavaOptionsPanel() {

		ResourceBundle msg = Plugin.msg;
		setName(msg.getString("Options.Java.Name"));
		listener = new Listener();
		setIcon(new ImageIcon(RText.class.getResource("graphics/file_icons/java.png")));

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
		addLeftAligned(box, enabledCB, 3);

		Box box2 = Box.createVerticalBox();
		if (o.isLeftToRight()) {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		}
		else {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
		}
		box.add(box2);

		showDescWindowCB = createCB("Options.General.ShowDescWindow");
		addLeftAligned(box2, showDescWindowCB, 3);

		paramAssistanceCB = createCB("Options.General.ParameterAssistance");
		addLeftAligned(box2, paramAssistanceCB);

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
		aaDocKeysField = new JTextField("@{", 10);
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

		addJREButton = new JButton(msg.getString("Options.Java.AddJRE"));
		addJREButton.addActionListener(listener);
		addLeftAligned(box, addJREButton, 5);

		buildPathModsCB = createCB("CheckForBuildPathMods");
		addLeftAligned(box, buildPathModsCB, 5);
		box.add(Box.createVerticalGlue());

		cp.add(Box.createVerticalStrut(5));
		rdButton = new JButton(msg.getString("Options.General.RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(cp, rdButton, 5);

		cp.add(Box.createVerticalGlue());

		applyComponentOrientation(o);

		addChildPanel(new FoldingOnlyOptionsPanel(null,
							SyntaxConstants.SYNTAX_STYLE_JAVA));

	}


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
	@Override
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
		jarMan.clearClassFileSources();
		for (int i=0; i<model.getRowCount(); i++) {
			File jar = (File)model.getValueAt(i, 0);
			LibraryInfo info = new JarLibraryInfo(jar);
			String source = (String)model.getValueAt(i, 1);
			if (source!=null && source.length()>0) {
				File loc = new File(source);
				SourceLocation sourceLoc = loc.isFile() ?
						(SourceLocation)new ZipSourceLocation(loc) :
							new DirSourceLocation(loc);
				info.setSourceLocation(sourceLoc);
			}
			try {
				jarMan.addClassFileSource(info);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

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
		return enabledCB;
	}


	/**
	 * Populates the table model for the "build path" table.
	 *
	 * @param jarMan The shared jar manager.
	 */
	private void populateTableModel(JarManager jarMan) {

		model.setRowCount(0);

		List<LibraryInfo> jars = jarMan.getClassFileSources();
		for (LibraryInfo li : jars) {
			JarLibraryInfo info = (JarLibraryInfo)li;
			File jar = info.getJarFile();
			SourceLocation sourceLoc = info.getSourceLocation();
			String source = sourceLoc!=null ? sourceLoc.getLocationAsString() : null;
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
	@Override
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
	private class JarRowHandler extends AbstractRowHandler {

		public Object[] getNewRowInfo(Object[] old) {
			RowHandlerDialog rhd = new RowHandlerDialog(getOptionsDialog(),old);
			rhd.setLocationRelativeTo(getOptionsDialog());
			rhd.setVisible(true);
			return rhd.newRowInfo;
		}

	}


	private static class RowHandlerDialog extends EscapableDialog
							implements ActionListener {

		private FSATextField jarField;
		private FSATextField sourceField;
		private JButton okButton;
		private JButton cancelButton;
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

			okButton = new JButton(Plugin.msg.getString("Options.General.OK"));
			okButton.addActionListener(this);
			cancelButton = new JButton(Plugin.msg.
								getString("Options.General.Cancel"));
			cancelButton.addActionListener(this);
			Container buttons=UIUtil.createButtonFooter(okButton, cancelButton);
			cp.add(buttons, BorderLayout.SOUTH);

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

		private static final String getString(String keySuffix) {
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
					LibraryInfo info = LibraryInfo.getJreJarInfo(new File(dir));
					if (info!=null) {
						String src = null;
						if (info.getSourceLocation()!=null) {
							src = info.getSourceLocation().getLocationAsString();
						}
						File rtJar = new File(info.getLocationAsString());
						model.addRow(new Object[] { rtJar, src });
						hasUnsavedChanges = true;
						firePropertyChange(PROPERTY, null, null);
					}
				}
			}

			else if (rdButton==source) {

				JarLibraryInfo jreInfo = (JarLibraryInfo)LibraryInfo.
														getMainJreJarInfo();

				int rowCount = model.getRowCount();
				boolean jreFieldModified = rowCount!=1 ||
					!((File)model.getValueAt(0, 0)).equals(
												jreInfo.getJarFile());

				if (enabledCB.isSelected() ||
						jreFieldModified ||
						!paramAssistanceCB.isSelected() ||
						!showDescWindowCB.isSelected() ||
						!buildPathModsCB.isSelected() ||
						!autoActivateCB.isSelected() ||
						!"300".equals(aaDelayField.getText())) {
					setEnabledCBSelected(false);
					paramAssistanceCB.setSelected(true);
					showDescWindowCB.setSelected(true);
					buildPathModsCB.setSelected(true);
					setAutoActivateCBSelected(true);
					aaDelayField.setText("300");
					model.setRowCount(0);
					String src = jreInfo.getSourceLocation()!=null ?
							jreInfo.getSourceLocation().getLocationAsString() :
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