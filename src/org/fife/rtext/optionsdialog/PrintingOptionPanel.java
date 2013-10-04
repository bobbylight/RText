/*
 * 02/27/2004
 *
 * PrintingOptionPanel.java - Options panel containing printing options.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.optionsdialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.*;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.FontSelector;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;


/**
 * Options panel for options dialog giving the printing options available.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class PrintingOptionPanel extends OptionsDialogPanel
							implements PropertyChangeListener {

	private FontSelector fontSelector;

	private JCheckBox headerCheckBox;	// If checked, the user wants a header on printed documents.
	private JCheckBox footerCheckBox;	// If checked, the user wants a footer on printed documents.
	//private boolean useHeader;		// Internal variable used to remember what the user clicked.
	//private boolean useFooter;		// Internal variable used to remember what the user clicked.

	private static final String FONT_PROPERTY	= "PrintingOptionPanel.font";


	/**
	 * Constructor.
	 *
	 * @param rtext The parent RText instance.
	 * @param msg The resource bundle to use.
	 */
	public PrintingOptionPanel(final RText rtext, final ResourceBundle msg) {

		super(msg.getString("OptPrName"));

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());

		Box printFontPanel = new Box(BoxLayout.LINE_AXIS);
		printFontPanel.setBorder(new OptionPanelBorder(msg.getString("OptPrFTitle")));
		printFontPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		fontSelector = new FontSelector();
		fontSelector.addPropertyChangeListener(FontSelector.FONT_PROPERTY, this);
		printFontPanel.add(fontSelector);
		printFontPanel.add(Box.createHorizontalGlue());

		Box temp = Box.createVerticalBox();
		headerCheckBox = new JCheckBox(msg.getString("OptPrPH"));
		footerCheckBox = new JCheckBox(msg.getString("OptPrPF"));
		headerCheckBox.setEnabled(false);
		footerCheckBox.setEnabled(false);
		temp.add(headerCheckBox);
		temp.add(footerCheckBox);
		Box printHeaderFooterPanel = createHorizontalBox();
		printHeaderFooterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		printHeaderFooterPanel.setBorder(new OptionPanelBorder(msg.getString("OptPrHFL")));
		printHeaderFooterPanel.add(temp);
		printHeaderFooterPanel.add(Box.createHorizontalGlue());

		temp = Box.createVerticalBox();
		temp.add(printFontPanel);
		temp.add(printHeaderFooterPanel);

		add(temp, BorderLayout.NORTH);
		applyComponentOrientation(orientation);
		
	}


	/**
	 * Applies the settings entered into this dialog on the specified
	 * application.
	 *
	 * @param owner The application.
	 */
	@Override
	protected void doApplyImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		mainView.setPrintFont(getPrintFont());			// Doesn't effect GUI.
		//mainView.setUsePrintHeader(pop.getUsePrintHeader());
		//mainView.setUsePrintFooter(pop.getUsePrintFooter());
	}


	/**
	 * Listens for actions in this panel.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String propertyName = e.getPropertyName();

		// If the user changed the print font...
		if (propertyName.equals(FontSelector.FONT_PROPERTY)) {
			hasUnsavedChanges = true;
			Font font = fontSelector.getDisplayedFont();
			firePropertyChange(FONT_PROPERTY, null, font);
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		// They can't input invalid stuff on this options panel.
		return null;
	}


	/**
	 * Returns the font the user selected for printing.
	 *
	 * @return A font to use for printing.
	 * @see #setPrintFont
	 */
	public Font getPrintFont() {
		return fontSelector.getDisplayedFont();
	}


	/**
	 * Returns the <code>JComponent</code> at the "top" of this Options
	 * panel.  This is the component that will receive focus if the user
	 * switches to this Options panel in the Options dialog.  As an added
	 * bonus, if this component is a <code>JTextComponent</code>, its
	 * text is selected for easy changing.
	 */
	@Override
	public JComponent getTopJComponent() {
		return fontSelector;
	}


	/**
	 * Sets the font currently being displayed as the font used for printing.
	 *
	 * @param printFont The font to display as the current font used for
	 *        printing.  If <code>null</code>, then a default is used.
	 * @see #getPrintFont
	 */
	private void setPrintFont(Font printFont) {
		if (printFont==null)
			printFont = new Font("Monospaced", Font.PLAIN, 9);
		fontSelector.setDisplayedFont(printFont, false);
	}


	/**
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are not handled.
	 *
	 * @param owner The parent application.
	 * @see #setValues(Frame)
	 */
	@Override
	protected void setValuesImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		setPrintFont(mainView.getPrintFont());
		//setPrintHeader(mainView.printHeader());
		//setPrintFooter(mainView.printFooter());
	}


}