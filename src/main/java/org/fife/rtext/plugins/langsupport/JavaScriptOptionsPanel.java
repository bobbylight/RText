/*
 * 05/20/2010
 *
 * JavaOptionsPanel.java - Options for Java language support.
 * Copyright (C) 2010 Robert Futrell
 * https://fifesoft.com/rtext
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

	private final Listener listener;
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
	private final JButton rdButton;


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	JavaScriptOptionsPanel(RText app) {

		ResourceBundle msg = Plugin.MSG;
		setName(msg.getString("Options.JavaScript.Name"));
		listener = new Listener();
		setIcon(app.getIconGroup().getIcon("fileTypes/javascript"));
		app.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, e -> {
			setIcon(app.getIconGroup().getIcon("fileTypes/javascript"));
		});

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

		rdButton = new JButton(app.getString("RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(cp, rdButton, 5);

		cp.add(Box.createVerticalGlue());

		ComponentOrientation o = ComponentOrientation.
				getOrientation(getLocale());
		applyComponentOrientation(o);

		addChildPanel(new FoldingOnlyOptionsPanel(app,
						SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT, false));

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
		JLabel aaJavaKeysLabel = new JLabel(msg.getString("Options.JavaScript.AutoActivationJSKeys"));
		aaJavaKeysLabel.setEnabled(false);
		JTextField aaJavaKeysField = new JTextField(".", 10);
		aaJavaKeysField.setEnabled(false);
		JLabel aaDocKeysLabel = new JLabel(msg.getString("Options.JavaScript.AutoActionDocCommentKeys"));
		aaDocKeysLabel.setEnabled(false);
		JTextField aaDocKeysField = new JTextField("@{", 10);
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
		JCheckBox cb = new JCheckBox(Plugin.MSG.getString(key));
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
		JRadioButton rb = new JRadioButton(Plugin.MSG.getString(key));
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


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {

		OptionsPanelCheckResult result = null;

		if (jshintrcField.isEnabled()) {
			File jshintrc = jshintrcField.getSelectedFile();
			if (jshintrc!=null && !jshintrc.isFile()) {
				String msg = Plugin.MSG.getString(
						"Options.JavaScirpt.Error.JSHint");
				result = new OptionsPanelCheckResult(this, jshintrcField, msg);
			}
		}

		return result;
	}


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
				setDirty(true);
			}

			else if (paramAssistanceCB==source ||
					showDescWindowCB==source ||
					strictCB==source || e4xCB==source) {
				setDirty(true);
			}

			else if (rhinoRB==source) {
				setSyntaxCheckingEngine(JsErrorParser.RHINO);
				setDirty(true);
			}

			else if (jshintRB==source) {
				setSyntaxCheckingEngine(JsErrorParser.JSHINT);
				setDirty(true);
			}

			else if (autoActivateCB==source) {
				// Trick related components to toggle enabled states
				setAutoActivateCBSelected(autoActivateCB.isSelected());
				setDirty(true);
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
					setDirty(true);
				}

			}

		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			handleDocumentEvent();
		}

		private void handleDocumentEvent() {
			setDirty(true);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent();
		}

	}


}
