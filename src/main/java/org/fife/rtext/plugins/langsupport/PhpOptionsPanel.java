/*
 * 05/27/2010
 *
 * PhpOptionsPanel.java - Options for PHP language support.
 * Copyright (C) 2010 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.php.PhpLanguageSupport;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.NumberDocumentFilter;
import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel for PHP language support.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class PhpOptionsPanel extends OptionsDialogPanel {

	private final Listener listener;
	private JCheckBox enabledCB;
	private JCheckBox showDescWindowCB;
	private JCheckBox autoAddClosingTagsCB;
	private JCheckBox autoActivateCB;
	private JLabel aaDelayLabel;
	private JTextField aaDelayField;
	private JCheckBox foldingEnabledCB;
	private final JButton rdButton;


	/**
	 * Constructor.
	 *
	 * @param app  The parent application.
	 */
	PhpOptionsPanel(RText app) {

		ResourceBundle msg = Plugin.MSG;
		setName(msg.getString("Options.Php.Name"));
		listener = new Listener();
		setIcon(app.getIconGroup().getIcon("fileTypes/php"));
		app.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, e -> {
			setIcon(app.getIconGroup().getIcon("fileTypes/php"));
		});

		ComponentOrientation o = ComponentOrientation.
											getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());

		Box topPanel = Box.createVerticalBox();

		topPanel.add(createGeneralPanel(msg));
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		topPanel.add(createAutoActivationPanel(msg, o));
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		topPanel.add(createFoldingPanel(msg));
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		rdButton = new JButton(app.getString("RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(topPanel, rdButton);

		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(o);

	}


	private Box createAutoActivationPanel(ResourceBundle msg,
										  ComponentOrientation o) {

		Box aaPanel = Box.createVerticalBox();
		aaPanel.setBorder(new OptionPanelBorder(
			msg.getString("Options.General.AutoActivation")));

		autoActivateCB = createCB("Options.General.EnableAutoActivation");
		addLeftAligned(aaPanel, autoActivateCB, COMPONENT_VERTICAL_SPACING);

		SpringLayout sl = new SpringLayout();
		JPanel temp = new JPanel(sl);
		aaDelayLabel = new JLabel(msg.getString("Options.General.AutoActivationDelay"));
		aaDelayField = new JTextField(10);
		AbstractDocument doc = (AbstractDocument)aaDelayField.getDocument();
		doc.setDocumentFilter(new NumberDocumentFilter());
		doc.addDocumentListener(listener);
		JLabel aaHtmlKeysLabel = new JLabel(msg.getString("Options.Php.AutoActivationHtmlKeys"));
		aaHtmlKeysLabel.setEnabled(false);
		JTextField aaHtmlKeysField = new JTextField("<", 10);
		aaHtmlKeysField.setEnabled(false);
		UIUtil.addLabelValuePairs(temp, o,
			aaDelayLabel, aaDelayField,
			aaHtmlKeysLabel, aaHtmlKeysField);
		UIUtil.makeSpringCompactGrid(temp, 2,2, 0,0, 5,5);
		addLeftAligned(aaPanel, temp, 0, 20);

		return aaPanel;
	}


	private JCheckBox createCB(String key) {
		if (key.indexOf('.')==-1) {
			key = "Options.Php." + key;
		}
		JCheckBox cb = new JCheckBox(Plugin.MSG.getString(key));
		cb.addActionListener(listener);
		return cb;
	}


	private Box createFoldingPanel(ResourceBundle msg) {

		Box foldingPanel = Box.createVerticalBox();
		foldingPanel.setBorder(new OptionPanelBorder(msg.
			getString("Options.General.Section.Folding")));

		foldingEnabledCB = createCB("Options.General.EnableCodeFolding");
		addLeftAligned(foldingPanel, foldingEnabledCB);

		return foldingPanel;
	}


	private Box createGeneralPanel(ResourceBundle msg) {

		Box generalPanel = Box.createVerticalBox();
		generalPanel.setBorder(new OptionPanelBorder(msg.
			getString("Options.General.Section.General")));

		enabledCB = createCB("Options.General.EnableCodeCompletion");
		addLeftAligned(generalPanel, enabledCB, COMPONENT_VERTICAL_SPACING);

		showDescWindowCB = createCB("Options.General.ShowDescWindow");
		addLeftAligned(generalPanel, showDescWindowCB, COMPONENT_VERTICAL_SPACING, 20);

		autoAddClosingTagsCB = createCB("Options.Html.AutoAddClosingTags");
		addLeftAligned(generalPanel, autoAddClosingTagsCB);

		return generalPanel;
	}


	@Override
	protected void doApplyImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport ls=lsf.getSupportFor(SyntaxConstants.SYNTAX_STYLE_PHP);

		// HTML-specific options
		PhpLanguageSupport phpls = (PhpLanguageSupport)ls;
		phpls.setAutoAddClosingTags(autoAddClosingTagsCB.isSelected());

		// Options dealing with code completion.
		ls.setAutoCompleteEnabled(enabledCB.isSelected());
		ls.setShowDescWindow(showDescWindowCB.isSelected());

		// Options dealing with auto-activation.
		ls.setAutoActivationEnabled(autoActivateCB.isSelected());
		int delay = 300;
		String temp = aaDelayField.getText();
		if (temp.length()>0) {
			try {
				delay = Integer.parseInt(aaDelayField.getText());
			} catch (NumberFormatException nfe) { // Never happens
				nfe.printStackTrace();
			}
		}
		ls.setAutoActivationDelay(delay);

		// Options dealing with code folding.
		RText rtext = (RText)owner;
		AbstractMainView view = rtext.getMainView();
		boolean folding = foldingEnabledCB.isSelected();
		view.setCodeFoldingEnabledFor(SyntaxConstants.SYNTAX_STYLE_PHP, folding);

	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	@Override
	public JComponent getTopJComponent() {
		return enabledCB;
	}


	private void setAutoActivateCBSelected(boolean selected) {
		autoActivateCB.setSelected(selected);
		aaDelayLabel.setEnabled(selected);
		aaDelayField.setEnabled(selected);
	}


	private void setEnabledCBSelected(boolean selected) {
		enabledCB.setSelected(selected);
		showDescWindowCB.setEnabled(selected);
	}


	@Override
	protected void setValuesImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport ls=lsf.getSupportFor(SyntaxConstants.SYNTAX_STYLE_PHP);

		// HTML-specific options
		PhpLanguageSupport phpls = (PhpLanguageSupport)ls;
		autoAddClosingTagsCB.setSelected(phpls.getAutoAddClosingTags());

		// Options dealing with code completion
		setEnabledCBSelected(ls.isAutoCompleteEnabled());
		showDescWindowCB.setSelected(ls.getShowDescWindow());

		// Options dealing with auto-activation
		setAutoActivateCBSelected(ls.isAutoActivationEnabled());
		aaDelayField.setText(Integer.toString(ls.getAutoActivationDelay()));

		// Options related to code folding.
		RText rtext = (RText)owner;
		AbstractMainView view = rtext.getMainView();
		boolean folding = view.isCodeFoldingEnabledFor(SyntaxConstants.SYNTAX_STYLE_PHP);
		foldingEnabledCB.setSelected(folding);

	}


	/**
	 * Listens for events in this options panel.
	 */
	private final class Listener implements ActionListener, DocumentListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (enabledCB==source) {
				// Trick related components to toggle enabled states
				setEnabledCBSelected(enabledCB.isSelected());
				setDirty(true);
			}

			else if (autoActivateCB==source) {
				setAutoActivateCBSelected(autoActivateCB.isSelected());
				setDirty(true);
			}

			else if (autoAddClosingTagsCB==source) {
				setDirty(true);
			}

			else if (foldingEnabledCB==source ||
					showDescWindowCB==source) {
				setDirty(true);
			}

			else if (rdButton==source) {
				if (!enabledCB.isSelected() ||
						!autoAddClosingTagsCB.isSelected() ||
						!showDescWindowCB.isSelected() ||
						!autoActivateCB.isSelected() ||
						!"300".equals(aaDelayField.getText()) ||
						!foldingEnabledCB.isSelected()) {
					setEnabledCBSelected(true);
					autoAddClosingTagsCB.setSelected(true);
					showDescWindowCB.setSelected(true);
					setAutoActivateCBSelected(true);
					aaDelayField.setText("300");
					foldingEnabledCB.setSelected(true);
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
