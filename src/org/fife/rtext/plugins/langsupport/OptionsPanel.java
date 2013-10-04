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
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import org.fife.rsta.ac.java.JavaCellRenderer;
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
public class OptionsPanel extends PluginOptionsDialogPanel {

	/**
	 * ID used to identify this option panel, so others can attach to it.
	 */
	public static final String OPTION_PANEL_ID = "LanguageSupportOptionPanel";

	private JCheckBox altColorCB;
	private RColorSwatchesButton altColorButton;
	private JButton rdButton;

	private static final Color DEFAULT_ALT_ROW_COLOR	= new Color(0xf4f4f4);
	private static final String PROPERTY				= "Property";


	public OptionsPanel(Plugin plugin) {

		super(plugin);
		setId(OPTION_PANEL_ID);
		URL url = getClass().getResource("comment.png");
		setIcon(new ImageIcon(url));

		ResourceBundle msg = Plugin.msg;
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

		altColorCB = new JCheckBox(msg.getString("Options.Main.AlternateColor"));
		altColorCB.addActionListener(listener);
		altColorButton = new RColorSwatchesButton();
		altColorButton.addActionListener(listener);
		Box temp2 = createHorizontalBox();
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
		addChildPanel(new COptionsPanel());
		addChildPanel(new CPlusPlusOptionsPanel());
		addChildPanel(new CSharpOptionsPanel());
		addChildPanel(new CssOptionsPanel());
		addChildPanel(new ClojureOptionsPanel());
		addChildPanel(new GroovyOptionsPanel());
		addChildPanel(new HtmlOptionsPanel());
		addChildPanel(new JavaOptionsPanel());
		addChildPanel(new JavaScriptOptionsPanel());
		addChildPanel(new JsonOptionsPanel());
		addChildPanel(new JspOptionsPanel());
		addChildPanel(new LatexOptionsPanel());
		addChildPanel(new MxmlOptionsPanel());
		addChildPanel(new NsisOptionsPanel());
		addChildPanel(new PerlOptionsPanel());
		addChildPanel(new PhpOptionsPanel());
		addChildPanel(new ScalaOptionsPanel());
		addChildPanel(new ShellOptionsPanel());
		addChildPanel(new XmlOptionsPanel());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doApplyImpl(Frame owner) {
		Color c = altColorCB.isSelected() ? altColorButton.getColor() : null;
		// All cell renderers except Java's are CompletionCellRenderers.
		CompletionCellRenderer.setAlternateBackground(c);
		JavaCellRenderer.setAlternateBackground(c);
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
		return altColorCB;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setValuesImpl(Frame owner) {

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

		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (altColorCB==source) {
				altColorButton.setEnabled(altColorCB.isSelected());
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (altColorButton==source) {
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (rdButton==source) {

				if (altColorCB.isSelected() || !DEFAULT_ALT_ROW_COLOR.
						equals(altColorButton.getColor())) {
					altColorCB.setSelected(false);
					altColorButton.setEnabled(false);
					altColorButton.setColor(DEFAULT_ALT_ROW_COLOR);
					hasUnsavedChanges = true;
					firePropertyChange(PROPERTY, null, null);
				}

			}

		}

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