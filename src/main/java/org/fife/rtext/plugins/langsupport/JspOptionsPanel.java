/*
 * 07/20/2011
 *
 * JspOptionsPanel.java - Options for JSP language support.
 * Copyright (C) 2010 Robert Futrell
 * https://fifesoft.com/rtext
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
import javax.swing.border.Border;

import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.jsp.JspLanguageSupport;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel for JSP code completion.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class JspOptionsPanel extends OptionsDialogPanel {

	private final Listener listener;
	private final JCheckBox enabledCB;
	private final JCheckBox autoAddClosingTagsCB;
	private final JCheckBox foldingEnabledCB;
	private final JButton rdButton;


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	JspOptionsPanel(RText app) {

		ResourceBundle msg = Plugin.MSG;
		setName(msg.getString("Options.Jsp.Name"));
		listener = new Listener();
		setIcon(app.getIconGroup().getIcon("fileTypes/jsp"));
		app.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, e -> {
			setIcon(app.getIconGroup().getIcon("fileTypes/jsp"));
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
		addLeftAligned(box, enabledCB);
		cp.add(Box.createVerticalStrut(5));

		autoAddClosingTagsCB = createCB("Options.Html.AutoAddClosingTags");
		addLeftAligned(box, autoAddClosingTagsCB, 5);
		cp.add(Box.createVerticalStrut(5));

		foldingEnabledCB = createCB("Options.General.EnableCodeFolding");
		addLeftAligned(box, foldingEnabledCB, 5);
		cp.add(Box.createVerticalStrut(5));

		rdButton = new JButton(app.getString("RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(cp, rdButton);

		cp.add(Box.createVerticalGlue());

		applyComponentOrientation(o);

	}


	private JCheckBox createCB(String key) {
		if (key.indexOf('.')==-1) {
			key = "Options.JSP." + key;
		}
		JCheckBox cb = new JCheckBox(Plugin.MSG.getString(key));
		cb.addActionListener(listener);
		return cb;
	}


	@Override
	protected void doApplyImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		JspLanguageSupport ls = (JspLanguageSupport)lsf.getSupportFor(
				SyntaxConstants.SYNTAX_STYLE_JSP);

		// HTML-specific options
		ls.setAutoAddClosingTags(autoAddClosingTagsCB.isSelected());

		// Options dealing with code completion.
		ls.setAutoCompleteEnabled(enabledCB.isSelected());

		// Options dealing with code folding.
		RText rtext = (RText)owner;
		AbstractMainView view = rtext.getMainView();
		boolean folding = foldingEnabledCB.isSelected();
		view.setCodeFoldingEnabledFor(SyntaxConstants.SYNTAX_STYLE_JSP, folding);

	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	@Override
	public JComponent getTopJComponent() {
		return enabledCB;
	}


	private void setEnabledCBSelected(boolean selected) {
		enabledCB.setSelected(selected);
		// TODO: Toggle enabled state of all other components.
	}


	@Override
	protected void setValuesImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		JspLanguageSupport ls = (JspLanguageSupport)lsf.getSupportFor(
				SyntaxConstants.SYNTAX_STYLE_JSP);

		// HTML-specific options
		autoAddClosingTagsCB.setSelected(ls.getAutoAddClosingTags());

		// Options dealing with code completion
		setEnabledCBSelected(ls.isAutoCompleteEnabled());

		// Options related to code folding.
		RText rtext = (RText)owner;
		AbstractMainView view = rtext.getMainView();
		boolean folding = view.isCodeFoldingEnabledFor(SyntaxConstants.SYNTAX_STYLE_JSP);
		foldingEnabledCB.setSelected(folding);

	}


	/**
	 * Listens for events in this options panel.
	 */
	private class Listener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (enabledCB==source) {
				// Trick related components to toggle enabled states
				setEnabledCBSelected(enabledCB.isSelected());
				setDirty(true);
			}

			else if (autoAddClosingTagsCB==source) {
				setDirty(true);
			}

			else if (foldingEnabledCB==source) {
				setDirty(true);
			}

			else if (rdButton==source) {
				if (!enabledCB.isSelected() ||
						!autoAddClosingTagsCB.isSelected() ||
						!foldingEnabledCB.isSelected()) {
					enabledCB.setSelected(true);
					autoAddClosingTagsCB.setSelected(true);
					foldingEnabledCB.setSelected(true);
					setDirty(true);
				}
			}

		}

	}


}
