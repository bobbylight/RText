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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.js.JavaScriptLanguageSupport;
import org.fife.rsta.ac.js.JsErrorParser;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.NumberDocumentFilter;
import org.fife.rtext.RText;
import org.fife.ui.FSATextField;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * The options panel for Java-specific language support options.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class JavaScriptOptionsPanel extends OptionsDialogPanel {

	private Listener listener;
	private JCheckBox enabledCB;
	private JCheckBox paramAssistanceCB;
	private JCheckBox showDescWindowCB;
	private JCheckBox autoActivateCB;
	private JRadioButton rhinoRB;
	private JCheckBox strictCB;
	private JCheckBox e4xCB;
	private JRadioButton jshintRB;
	private SelectableLabel jsHintDescLabel;
	private JLabel jshintrcLabel;
	private FSATextField jshintrcField;
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
	public JavaScriptOptionsPanel() {

		ResourceBundle msg = Plugin.msg;
		setName(msg.getString("Options.JavaScript.Name"));
		listener = new Listener();
		setIcon(new ImageIcon(RText.class.getResource("graphics/file_icons/script_code.png")));

		setLayout(new BorderLayout());
		Border empty5Border = UIUtil.getEmpty5Border();
		setBorder(empty5Border);

		Box cp = Box.createVerticalBox();
		cp.setBorder(null);
		add(cp, BorderLayout.NORTH);

		cp.add(createGeneralPanel(msg));
		cp.add(Box.createVerticalStrut(5));

		cp.add(createSyntaxCheckingEnginePanel(msg));
		cp.add(Box.createVerticalStrut(5));

		cp.add(createAutoActivationPanel(msg));
		cp.add(Box.createVerticalStrut(5));

		rdButton = new JButton(msg.getString("Options.General.RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(cp, rdButton, 5);

		cp.add(Box.createVerticalGlue());

		ComponentOrientation o = ComponentOrientation.
				getOrientation(getLocale());
		applyComponentOrientation(o);

		addChildPanel(new FoldingOnlyOptionsPanel(null,
						SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT));

	}


	/**
	 * Returns the panel containing auto-activation options.
	 *
	 * @param msg The plugin resource bundle.
	 * @return The panel.
	 */
	private Box createAutoActivationPanel(ResourceBundle msg) {

		ComponentOrientation o = ComponentOrientation.
						getOrientation(getLocale());

		Box box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(
				msg.getString("Options.General.AutoActivation")));

		autoActivateCB = createCB("Options.General.EnableAutoActivation");
		addLeftAligned(box, autoActivateCB, 5);

		SpringLayout sl = new SpringLayout();
		JPanel temp = new JPanel(sl);
		aaDelayLabel = new JLabel(msg.getString("Options.General.AutoActivationDelay"));
		aaDelayField = new JTextField(10);
		AbstractDocument doc = (AbstractDocument)aaDelayField.getDocument();
		doc.setDocumentFilter(new NumberDocumentFilter());
		doc.addDocumentListener(listener);
		aaJavaKeysLabel = new JLabel(msg.getString("Options.JavaScript.AutoActivationJSKeys"));
		aaJavaKeysLabel.setEnabled(false);
		aaJavaKeysField = new JTextField(".", 10);
		aaJavaKeysField.setEnabled(false);
		aaDocKeysLabel = new JLabel(msg.getString("Options.JavaScript.AutoActionDocCommentKeys"));
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
		addLeftAligned(box, temp2, 20);

		return box;

	}


	private JCheckBox createCB(String key) {
		if (key.indexOf('.')==-1) {
			key = "Options.JavaScript." + key;
		}
		JCheckBox cb = new JCheckBox(Plugin.msg.getString(key));
		cb.addActionListener(listener);
		return cb;
	}


	/**
	 * Returns the panel containing general JS options.
	 *
	 * @param msg The plugin resource bundle.
	 * @return The panel.
	 */
	private Box createGeneralPanel(ResourceBundle msg) {

		Box box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(msg.
				getString("Options.General.Section.General")));

		enabledCB = createCB("Options.JavaScript.EnableCodeCompletion");
		addLeftAligned(box, enabledCB, 5);

		showDescWindowCB = createCB("Options.General.ShowDescWindow");
		addLeftAligned(box, showDescWindowCB, 5, 20);

		paramAssistanceCB = createCB("Options.General.ParameterAssistance");
		addLeftAligned(box, paramAssistanceCB, 5, 20);

		return box;

	}


	private JRadioButton createRB(String key) {
		if (key.indexOf('.')==-1) {
			key = "Options.JavaScript." + key;
		}
		JRadioButton rb = new JRadioButton(Plugin.msg.getString(key));
		rb.addActionListener(listener);
		return rb;
	}


	/**
	 * Returns the panel containing options pertaining to the error checking
	 * library to use.
	 *
	 * @param msg The plugin resource bundle.
	 * @return The panel.
	 */
	private Box createSyntaxCheckingEnginePanel(ResourceBundle msg) {

		Box box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(
				msg.getString("Options.JavaScript.SyntaxCheckingEngine")));
		ButtonGroup syntaxEngineBG = new ButtonGroup();
		rhinoRB = createRB("Rhino");
		syntaxEngineBG.add(rhinoRB);
		addLeftAligned(box, rhinoRB, 5);

		strictCB = createCB("Strict");
		addLeftAligned(box, strictCB, 5, 20);

		e4xCB = createCB("E4x");
		addLeftAligned(box, e4xCB, 5, 20);

		jshintRB = createRB("JSHint");
		syntaxEngineBG.add(jshintRB);
		addLeftAligned(box, jshintRB, 5);

		jsHintDescLabel = new SelectableLabel(
				msg.getString("Options.JavaScript.JSHint.Desc"));
		addLeftAligned(box, jsHintDescLabel, 5, 20);

		jshintrcLabel = UIUtil.newLabel(msg,
				"Options.JavaScript.JSHint.JSHintrc");
		jshintrcField = new FSATextField(40);
		jshintrcField.getDocument().addDocumentListener(listener);
		jshintrcLabel.setLabelFor(jshintrcField);
		JPanel temp = new JPanel(new BorderLayout(5, 0));
		temp.add(jshintrcLabel, BorderLayout.LINE_START);
		temp.add(jshintrcField);
		addLeftAligned(box, temp, 5, 20);

		return box;

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doApplyImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport ls = lsf.getSupportFor(
				SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		JavaScriptLanguageSupport jls = (JavaScriptLanguageSupport)ls;

		// Options dealing with code completion.
		jls.setAutoCompleteEnabled(enabledCB.isSelected());
		jls.setParameterAssistanceEnabled(paramAssistanceCB.isSelected());
		jls.setShowDescWindow(showDescWindowCB.isSelected());

		// Options dealing with syntax checking.
		boolean reparse = false;
		reparse |= jls.setErrorParser(rhinoRB.isSelected() ?
				JsErrorParser.RHINO : JsErrorParser.JSHINT);
		reparse |= jls.setDefaultJsHintRCFile(jshintrcField.getSelectedFile()); // jshint
		reparse |= jls.setStrictMode(strictCB.isSelected()); // rhino
		reparse |= jls.setXmlAvailable(e4xCB.isSelected()); // rhino

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
		// TODO: Trigger keys for JS and JSDoc?

		// Some option related to syntax errors changed.
		if (reparse) {
			RText rtext = (RText)owner;
			AbstractMainView mainView = rtext.getMainView();
			for (int i=0; i<mainView.getNumDocuments(); i++) {
				RSyntaxTextArea textArea = mainView.getRTextEditorPaneAt(i);
				textArea.forceReparsing(jls.getParser(textArea));
			}
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {

		OptionsPanelCheckResult result = null;

		if (jshintrcField.isEnabled()) {
			File jshintrc = jshintrcField.getSelectedFile();
			if (jshintrc!=null && !jshintrc.isFile()) {
				String msg = Plugin.msg.getString(
						"Options.JavaScirpt.Error.JSHint");
				result = new OptionsPanelCheckResult(this, jshintrcField, msg);
			}
		}

		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public JComponent getTopJComponent() {
		return enabledCB;
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
		//strictCB.setEnabled(selected);
		//e4xCB.setEnabled(selected);
	}


	/**
	 * Sets what syntax checking engine is selected to use.  This
	 * enables/disables other fields as appropriate.
	 *
	 * @param errorParser The new syntax checking engine.
	 */
	private void setSyntaxCheckingEngine(JsErrorParser errorParser) {

		boolean jshintFieldsEnabled = true;
		boolean rhinoFieldsEnabled = true;
		if (errorParser==JsErrorParser.JSHINT) {
			rhinoFieldsEnabled = false;
		}
		else { // Null or RHINO
			jshintFieldsEnabled = false;
		}

		rhinoRB.setSelected(rhinoFieldsEnabled);
		strictCB.setEnabled(rhinoFieldsEnabled);
		e4xCB.setEnabled(rhinoFieldsEnabled);
		jshintRB.setSelected(jshintFieldsEnabled);
		jsHintDescLabel.setEnabled(jshintFieldsEnabled);
		jshintrcLabel.setEnabled(jshintFieldsEnabled);
		jshintrcField.setEnabled(jshintFieldsEnabled);

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setValuesImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport ls=lsf.getSupportFor(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		JavaScriptLanguageSupport jls = (JavaScriptLanguageSupport)ls;

		// Options dealing with code completion
		setEnabledCBSelected(jls.isAutoCompleteEnabled());
		paramAssistanceCB.setSelected(jls.isParameterAssistanceEnabled());
		showDescWindowCB.setSelected(jls.getShowDescWindow());
		strictCB.setSelected(jls.isStrictMode());
		e4xCB.setSelected(jls.isXmlAvailable());

		// Options dealing with syntax checking.
		setSyntaxCheckingEngine(jls.getErrorParser());
		File jshintrcFile = jls.getDefaultJsHintRCFile();
		String jshint = jshintrcFile==null ? null : jshintrcFile.getAbsolutePath();
		jshintrcField.setText(jshint); // jshint
		strictCB.setSelected(jls.isStrictMode()); // rhino
		e4xCB.setSelected(jls.isXmlAvailable()); // rhino

		// Options dealing with auto-activation
		setAutoActivateCBSelected(jls.isAutoActivationEnabled());
		aaDelayField.setText(Integer.toString(jls.getAutoActivationDelay()));
		// TODO: Trigger keys for JS and JSDoc?

	}


	/**
	 * Listens for events in this options panel.
	 */
	private class Listener implements ActionListener, DocumentListener {

		@Override
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
					strictCB==source || e4xCB==source) {
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (rhinoRB==source) {
				setSyntaxCheckingEngine(JsErrorParser.RHINO);
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (jshintRB==source) {
				setSyntaxCheckingEngine(JsErrorParser.JSHINT);
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (autoActivateCB==source) {
				// Trick related components to toggle enabled states
				setAutoActivateCBSelected(autoActivateCB.isSelected());
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (rdButton==source) {

				if (!enabledCB.isSelected() ||
						!paramAssistanceCB.isSelected() ||
						!showDescWindowCB.isSelected() ||
						!rhinoRB.isSelected() ||
						strictCB.isSelected() ||
						e4xCB.isSelected() ||
						jshintrcField.getText().length()>0 ||
						!autoActivateCB.isSelected() ||
						!"300".equals(aaDelayField.getText())) {
					setEnabledCBSelected(true);
					paramAssistanceCB.setSelected(true);
					showDescWindowCB.setSelected(true);
					setSyntaxCheckingEngine(JsErrorParser.RHINO);
					strictCB.setSelected(false);
					e4xCB.setSelected(false);
					jshintrcField.setText(null);
					setAutoActivateCBSelected(true);
					aaDelayField.setText("300");
					hasUnsavedChanges = true;
					firePropertyChange(PROPERTY, null, null);
				}

			}

		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		private void handleDocumentEvent(DocumentEvent e) {
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, null, null);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

	}


}