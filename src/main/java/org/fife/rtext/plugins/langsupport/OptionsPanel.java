/*
 * 05/20/2010
 *
 * OptionsPanel.java - The main options panel for Java language support.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import org.fife.rsta.ac.java.JavaCellRenderer;
import org.fife.rtext.RText;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.autocomplete.CompletionCellRenderer;


/**
 * The main options dialog panel for language options.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class OptionsPanel extends PluginOptionsDialogPanel<Plugin> {

	/**
	 * ID used to identify this option panel, so others can attach to it.
	 */
	private static final String OPTION_PANEL_ID = "LanguageSupportOptionPanel";

	private final JComboBox<Integer> codeFoldingThresholdCB;
	private final JCheckBox altColorCB;
	private final RColorSwatchesButton altColorButton;
	private final JButton rdButton;

	private static final Color DEFAULT_ALT_ROW_COLOR	= new Color(0xf4f4f4);


	OptionsPanel(Plugin plugin) {

		super(plugin);
		setId(OPTION_PANEL_ID);
		RText app = plugin.getApplication();
		setIcon(app.getIconGroup().getIcon("comment"));
		app.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, e -> {
			app.getIconGroup().getIcon("comment");
		});

		ResourceBundle msg = Plugin.MSG;
		setName(msg.getString("Name"));
		Listener listener = new Listener();

		ComponentOrientation o = ComponentOrientation.
											getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		Box cp = Box.createVerticalBox();
		add(cp, BorderLayout.NORTH);

		SelectableLabel label = new SelectableLabel();
		label.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
		label.setText(msg.getString("Options.Main.Label"));
		label.addHyperlinkListener(listener);
		cp.add(label);
		cp.add(Box.createVerticalStrut(5));

		Box temp = Box.createVerticalBox();
		temp.setBorder(new OptionPanelBorder(msg.getString("Options.Main.Section")));

		codeFoldingThresholdCB = new JComboBox<>(new Integer[] { 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 });
		codeFoldingThresholdCB.addActionListener(listener);
		Box temp2 = createHorizontalBox();
		temp2.add(UIUtil.newLabel(msg, "Options.Main.CodeFoldingThreshold", codeFoldingThresholdCB));
		temp2.add(Box.createHorizontalStrut(5));
		temp2.add(codeFoldingThresholdCB);
		temp2.add(Box.createHorizontalGlue());
		addLeftAligned(temp, temp2);

		altColorCB = new JCheckBox(msg.getString("Options.Main.AlternateColor"));
		altColorCB.addActionListener(listener);
		altColorButton = new RColorSwatchesButton();
		altColorButton.addActionListener(listener);
		temp2 = createHorizontalBox();
		temp2.add(altColorCB);
		temp2.add(Box.createHorizontalStrut(5));
		temp2.add(altColorButton);
		temp2.add(Box.createHorizontalGlue());
		temp.add(temp2);
		cp.add(temp);
		cp.add(Box.createVerticalStrut(10));

		rdButton = new JButton(msg.getString("Options.General.RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(cp, rdButton, 5);

		cp.add(Box.createVerticalGlue());

		applyComponentOrientation(o);

		// Language-specific child panels
		addChildPanel(new COptionsPanel(app));
		addChildPanel(new CPlusPlusOptionsPanel(app));
		addChildPanel(new CSharpOptionsPanel(app));
		addChildPanel(new CssOptionsPanel(app));
		addChildPanel(new ClojureOptionsPanel(app));
		addChildPanel(new DOptionsPanel(app));
		addChildPanel(new DartOptionsPanel(app));
		addChildPanel(new GoOptionsPanel(app));
		addChildPanel(new GroovyOptionsPanel(app));
		addChildPanel(new HtmlOptionsPanel(app));
		addChildPanel(new JavaOptionsPanel(app));
		addChildPanel(new JavaScriptOptionsPanel(app));
		addChildPanel(new JSHintOptionsPanel(app));
		addChildPanel(new JsonOptionsPanel(app));
		addChildPanel(new JspOptionsPanel(app));
		addChildPanel(new KotlinOptionsPanel(app));
		addChildPanel(new LatexOptionsPanel(app));
		addChildPanel(new LessOptionsPanel(app));
		addChildPanel(new MxmlOptionsPanel(app));
		addChildPanel(new NsisOptionsPanel(app));
		addChildPanel(new PerlOptionsPanel(app));
		addChildPanel(new PhpOptionsPanel(app));
		addChildPanel(new PythonOptionsPanel(app));
		addChildPanel(new ScalaOptionsPanel(app));
		addChildPanel(new ShellOptionsPanel(app));
		addChildPanel(new TypeScriptOptionsPanel(app));
		addChildPanel(new XmlOptionsPanel(app));
	}


	@Override
	protected void doApplyImpl(Frame owner) {

		RText rtext = (RText)owner;
		rtext.getMainView().setMaxFileSizeForCodeFolding(
			(Integer)codeFoldingThresholdCB.getSelectedItem());

		Color c = altColorCB.isSelected() ? altColorButton.getColor() : null;
		// All cell renderers except Java's are CompletionCellRenderers.
		CompletionCellRenderer.setAlternateBackground(c);
		JavaCellRenderer.setAlternateBackground(c);
	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	@Override
	public JComponent getTopJComponent() {
		return altColorCB;
	}


	@Override
	protected void setValuesImpl(Frame owner) {

		RText app = (RText)owner;
		codeFoldingThresholdCB.setSelectedItem(app.getMainView().getMaxFileSizeForCodeFolding());

		Color altColor = CompletionCellRenderer.getAlternateBackground();
		if (altColor==null) {
			altColorCB.setSelected(false);
			altColorButton.setColor(DEFAULT_ALT_ROW_COLOR);
			altColorButton.setEnabled(false);
		}
		else {
			altColorCB.setSelected(true);
			altColorButton.setColor(altColor);
		}

	}


	/**
	 * Listens for events in this panel.
	 */
	private class Listener implements ActionListener, HyperlinkListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			int defaultCodeFoldingThreshold = 12;
			Object source = e.getSource();

			if (codeFoldingThresholdCB == source) {
				setDirty(true);
			}

			else if (altColorCB==source) {
				altColorButton.setEnabled(altColorCB.isSelected());
				setDirty(true);
			}

			else if (altColorButton==source) {
				setDirty(true);
			}

			else if (rdButton==source) {

				if (!Objects.equals(defaultCodeFoldingThreshold,
							codeFoldingThresholdCB.getSelectedItem()) ||
						altColorCB.isSelected() ||
						!DEFAULT_ALT_ROW_COLOR.equals(altColorButton.getColor())) {
					codeFoldingThresholdCB.setSelectedItem(defaultCodeFoldingThreshold);
					altColorCB.setSelected(false);
					altColorButton.setEnabled(false);
					altColorButton.setColor(DEFAULT_ALT_ROW_COLOR);
					setDirty(true);
				}

			}

		}

		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType()==EventType.ACTIVATED) {
				if (!UIUtil.browse(e.getURL().toExternalForm())) {
					UIManager.getLookAndFeel().provideErrorFeedback(
													OptionsPanel.this);
				}
			}
		}

	}


}
