/*
 * 05/27/2010
 *
 * PhpOptionsPanel.java - Options for PHP language support.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
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
	private final JCheckBox enabledCB;
	private final JCheckBox showDescWindowCB;
	private final JCheckBox autoAddClosingTagsCB;
	private final JCheckBox autoActivateCB;
	private final JLabel aaDelayLabel;
	private final JTextField aaDelayField;
	private final JCheckBox foldingEnabledCB;
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

		enabledCB = createCB("Options.General.EnableCodeCompletion");
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

		autoAddClosingTagsCB = createCB("Options.Html.AutoAddClosingTags");
		addLeftAligned(box, autoAddClosingTagsCB, 5);

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
		JLabel aaHtmlKeysLabel = new JLabel(msg.getString("Options.Php.AutoActivationHtmlKeys"));
		aaHtmlKeysLabel.setEnabled(false);
		JTextField aaHtmlKeysField = new JTextField("<", 10);
		aaHtmlKeysField.setEnabled(false);
		if (o.isLeftToRight()) {
			temp.add(aaDelayLabel);		temp.add(aaDelayField);
			temp.add(aaHtmlKeysLabel);		temp.add(aaHtmlKeysField);
		}
		else {
			temp.add(aaDelayField);		temp.add(aaDelayLabel);
			temp.add(aaHtmlKeysField);		temp.add(aaHtmlKeysLabel);
		}
		UIUtil.makeSpringCompactGrid(temp, 2,2, 0,0, 5,5);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(temp, BorderLayout.LINE_START);
		box2.add(temp2);

		box2.add(Box.createVerticalGlue());

		cp.add(Box.createVerticalStrut(5));
		box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(msg.
				getString("Options.General.Section.Folding")));
		cp.add(box);
		cp.add(Box.createVerticalStrut(5));

		foldingEnabledCB = createCB("Options.General.EnableCodeFolding");
		addLeftAligned(box, foldingEnabledCB, 5);

		cp.add(Box.createVerticalStrut(5));
		rdButton = new JButton(msg.getString("Options.General.RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(cp, rdButton, 5);

		cp.add(Box.createVerticalGlue());

		applyComponentOrientation(o);

	}


	private JCheckBox createCB(String key) {
		if (key.indexOf('.')==-1) {
			key = "Options.Php." + key;
		}
		JCheckBox cb = new JCheckBox(Plugin.MSG.getString(key));
		cb.addActionListener(listener);
		return cb;
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
		//aaHtmlKeysLabel.setEnabled(selected);
		//aaHtmlKeysField.setEnabled(selected);
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
	private class Listener implements ActionListener, DocumentListener {

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
