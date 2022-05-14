/*
 * 10/29/2009
 *
 * XmlOptionPanel.java - Options for XML language assistance.
 * Copyright (C) 2009 Robert Futrell
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
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.xml.XmlLanguageSupport;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.modes.XMLTokenMaker;


/**
 * Options panel for XML language assistance.  Some of the options here
 * actually don't correspond to options made available in
 * <code>RSTALanguageSupport</code>, but rather options built into RSTA proper;
 * however, to keep the options dialog organized consistently all
 * language-specific options are under the umbrella of this plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class XmlOptionsPanel extends OptionsDialogPanel implements ActionListener {

	private final JCheckBox codeCompletionEnabledCB;
	private final JCheckBox autoCompleteClosingTagsCB;
	private final JCheckBox foldingEnabledCB;
	private final JButton rdButton;


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	XmlOptionsPanel(RText app) {

		ResourceBundle msg = Plugin.MSG;
		setName(msg.getString("Options.Xml.Name"));
		setIcon(app.getIconGroup().getIcon("fileTypes/xml"));
		app.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, e -> {
			setIcon(app.getIconGroup().getIcon("fileTypes/xml"));
		});

		ComponentOrientation o = ComponentOrientation.
										getOrientation(getLocale());
		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		Box cp = Box.createVerticalBox();
		cp.setBorder(null);
		add(cp, BorderLayout.NORTH);

		Box box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(msg.
				getString("Options.General.Section.General")));
		cp.add(box);
		cp.add(Box.createVerticalStrut(5));

		codeCompletionEnabledCB = createCB("Options.Xml.EnableCodeCompletion");
		addLeftAligned(box, codeCompletionEnabledCB, 5);

		autoCompleteClosingTagsCB = createCB("CompleteClosingTags");
		addLeftAligned(box, autoCompleteClosingTagsCB, 5);

		box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(msg.
				getString("Options.General.Section.Folding")));
		cp.add(box);
		cp.add(Box.createVerticalStrut(5));

		foldingEnabledCB = createCB("Options.General.EnableCodeFolding");
		addLeftAligned(box, foldingEnabledCB, 5);

		cp.add(Box.createVerticalStrut(5));
		rdButton = new JButton(app.getString("RestoreDefaults"));
		rdButton.addActionListener(this);
		addLeftAligned(cp, rdButton, 5);

		cp.add(Box.createVerticalGlue());
		applyComponentOrientation(o);

	}


	@Override
	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();

		if (autoCompleteClosingTagsCB==source || codeCompletionEnabledCB==source ||
				foldingEnabledCB==source) {
			setDirty(true);
		}

		else if (rdButton==source &&
				!autoCompleteClosingTagsCB.isSelected() ||
				!codeCompletionEnabledCB.isSelected() ||
				!foldingEnabledCB.isSelected()) {
			autoCompleteClosingTagsCB.setSelected(true);
			codeCompletionEnabledCB.setSelected(true);
			foldingEnabledCB.setSelected(true);
			setDirty(true);
		}

	}


	private JCheckBox createCB(String key) {
		if (key.indexOf('.')==-1) {
			key = "Options.Xml." + key;
		}
		JCheckBox cb = new JCheckBox(Plugin.MSG.getString(key));
		cb.addActionListener(this);
		return cb;
	}


	@Override
	protected void doApplyImpl(Frame owner) {

		boolean closeClosingTags = autoCompleteClosingTagsCB.isSelected();
		XMLTokenMaker.setCompleteCloseTags(closeClosingTags);

		final String language = SyntaxConstants.SYNTAX_STYLE_XML;
		RText rtext = (RText)owner;
		AbstractMainView view = rtext.getMainView();
		view.setCodeFoldingEnabledFor(language, foldingEnabledCB.isSelected());

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		XmlLanguageSupport xls = (XmlLanguageSupport)lsf.getSupportFor(language);
		xls.setShowSyntaxErrors(codeCompletionEnabledCB.isSelected());

	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	@Override
	public JComponent getTopJComponent() {
		return autoCompleteClosingTagsCB;
	}


	@Override
	protected void setValuesImpl(Frame owner) {

		boolean close = XMLTokenMaker.getCompleteCloseMarkupTags();
		autoCompleteClosingTagsCB.setSelected(close);

		final String language = SyntaxConstants.SYNTAX_STYLE_XML;
		RText rtext = (RText)owner;
		AbstractMainView view = rtext.getMainView();
		foldingEnabledCB.setSelected(view.isCodeFoldingEnabledFor(language));

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		XmlLanguageSupport xls = (XmlLanguageSupport)lsf.getSupportFor(language);
		codeCompletionEnabledCB.setSelected(xls.getShowSyntaxErrors());

	}


}
