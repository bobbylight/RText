/*
 * 03/25/2004
 *
 * UIOptionPanel.java - Option panel for overall user interface
 * (non-textarea) related options.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.optionsdialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.SpecialValueComboBox;
import org.fife.ui.StatusBar;
import org.fife.ui.UIUtil;
import org.fife.ui.app.ExtendedLookAndFeelInfo;
import org.fife.ui.rtextarea.IconGroup;


/**
 * Option panel for user-interface specific options (that don't have to
 * do with the text area).
 *
 * @author Robert Futrell
 * @version 1.0
 */
class UIOptionPanel extends OptionsDialogPanel implements ActionListener,
										PropertyChangeListener {

	private int mainViewStyle;
	private int documentSelectionPlacement;
	private int statusBarStyle;

	private JPanel springPanel, springPanel2;
	private JComboBox viewCombo;
	private JComboBox docSelCombo;
	private SpecialValueComboBox lnfCombo;
	private SpecialValueComboBox imageLnFCombo;
	private JComboBox statusBarCombo;

	private JCheckBox highlightModifiedCheckBox;
	private RColorSwatchesButton hmColorButton;

	private JCheckBox showHostNameCheckBox;


	/**
	 * Constructor.
	 */
	public UIOptionPanel(final RText rtext, final ResourceBundle msg) {

		super(msg.getString("OptUIName"));

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		// Set up our border and layout.
		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		// Create a panel to put everything in.  We'll add this panel to our
		// "North" so it doesn't stretch when the user resizes the dialog.
		Box everything = Box.createVerticalBox();

		viewCombo = new JComboBox();
		UIUtil.fixComboOrientation(viewCombo);
		viewCombo.setActionCommand("ViewComboBox");
		viewCombo.addActionListener(this);
		viewCombo.addItem(msg.getString("OptUITV"));
		viewCombo.addItem(msg.getString("OptUISPV"));
		viewCombo.addItem(msg.getString("OptUIMDIV"));

		docSelCombo = new JComboBox();
		UIUtil.fixComboOrientation(docSelCombo);
		docSelCombo.setActionCommand("DocSelCombo");
		docSelCombo.addActionListener(this);
		docSelCombo.addItem(msg.getString("OptUITop"));
		docSelCombo.addItem(msg.getString("OptUILeft"));
		docSelCombo.addItem(msg.getString("OptUIBottom"));
		docSelCombo.addItem(msg.getString("OptUIRight"));

		// Add a panel for the "Layout" stuff.
		springPanel = new JPanel(new SpringLayout());
		JPanel temp = new JPanel(new BorderLayout());
		temp.setBorder(new OptionPanelBorder(msg.getString("OptUILT")));
		if (orientation.isLeftToRight()) {
			springPanel.add(new JLabel(msg.getString("OptUIViewT")));
			springPanel.add(viewCombo);
			springPanel.add(new JLabel(msg.getString("OptUIDSPT")));
			springPanel.add(docSelCombo);
		}
		else {
			springPanel.add(viewCombo);
			springPanel.add(new JLabel(msg.getString("OptUIViewT")));
			springPanel.add(docSelCombo);
			springPanel.add(new JLabel(msg.getString("OptUIDSPT")));
		}
		temp.add(springPanel, BorderLayout.LINE_START);
		UIUtil.makeSpringCompactGrid(springPanel,
									2,2,		// rows,cols,
									0,0,		// initial-x, initial-y,
									5,5);	// x-spacing, y-spacing.
		everything.add(temp);

		lnfCombo = createLookAndFeelComboBox(rtext);
		lnfCombo.setActionCommand("LookAndFeelComboBox");
		lnfCombo.addActionListener(this);

		imageLnFCombo = new SpecialValueComboBox();
		UIUtil.fixComboOrientation(imageLnFCombo);
		imageLnFCombo.setActionCommand("IconComboBox");
		imageLnFCombo.addActionListener(this);
		Collection<IconGroup> iconGroups = rtext.getIconGroupMap().values();
		for (IconGroup group : iconGroups) {
			imageLnFCombo.addSpecialItem(group.getName(), group.getName());
		}

		statusBarCombo = new JComboBox();
		UIUtil.fixComboOrientation(statusBarCombo);
		statusBarCombo.setActionCommand("StatusBarComboBox");
		statusBarCombo.addActionListener(this);
		statusBarCombo.addItem(msg.getString("OptUIW95A"));
		statusBarCombo.addItem(msg.getString("OptUIWXPA"));

		// Add a panel for the "Appearance" stuff.
		springPanel2 = new JPanel(new SpringLayout());
		temp = new JPanel(new BorderLayout());
		temp.setBorder(new OptionPanelBorder(msg.getString("OptUIAT")));
		if (orientation.isLeftToRight()) {
			springPanel2.add(new JLabel(msg.getString("OptUILnFT")));
			springPanel2.add(lnfCombo);
			springPanel2.add(new JLabel(msg.getString("OptUIIAT")));
			springPanel2.add(imageLnFCombo);
			springPanel2.add(new JLabel(msg.getString("OptUISBT")));
			springPanel2.add(statusBarCombo);
		}
		else {
			springPanel2.add(lnfCombo);
			springPanel2.add(new JLabel(msg.getString("OptUILnFT")));
			springPanel2.add(imageLnFCombo);
			springPanel2.add(new JLabel(msg.getString("OptUIIAT")));
			springPanel2.add(statusBarCombo);
			springPanel2.add(new JLabel(msg.getString("OptUISBT")));
		}
		temp.add(springPanel2, BorderLayout.LINE_START);
		UIUtil.makeSpringCompactGrid(springPanel2,
									3,2,		// rows,cols,
									0,0,		// initial-x, initial-y,
									5,5);	// x-spacing, y-spacing.
		everything.add(temp);

		// Add a panel for the "modified filenames" color button.
		Container miscPanel = createHorizontalBox();
		highlightModifiedCheckBox = new JCheckBox(msg.getString("OptUIHMDN"));
		highlightModifiedCheckBox.setActionCommand("HighlightModifiedCheckBox");
		highlightModifiedCheckBox.addActionListener(this);
		hmColorButton = new RColorSwatchesButton(Color.RED);
		hmColorButton.addPropertyChangeListener(this);
		miscPanel.add(highlightModifiedCheckBox);
		miscPanel.add(hmColorButton);
		miscPanel.add(Box.createHorizontalGlue());
		everything.add(miscPanel);

		// A panel for the "Show hostname" check box.
		miscPanel = new JPanel(new BorderLayout());
		showHostNameCheckBox = new JCheckBox(msg.getString("OptUIShowHostName"));
		showHostNameCheckBox.setActionCommand("ShowHostNameCB");
		showHostNameCheckBox.addActionListener(this);
		miscPanel.add(showHostNameCheckBox, BorderLayout.LINE_START);
		everything.add(miscPanel);

		// Add everything "to the north" so the spacing between stuff doesn't
		// change then the user stretches the dialog.
		add(everything, BorderLayout.NORTH);
		applyComponentOrientation(orientation);

	}


	/**
	 * Listens for actions being performed in this panel.
	 */
	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		if (actionCommand.equals("ViewComboBox")) {
			hasUnsavedChanges = true;
			int old = mainViewStyle;
			mainViewStyle = viewCombo.getSelectedIndex();
			firePropertyChange("UIOptionPanel.mainViewStyle", old, mainViewStyle);
		}

		else if (actionCommand.equals("DocSelCombo")) {
			hasUnsavedChanges = true;
			int old = documentSelectionPlacement;
			documentSelectionPlacement = docSelCombo.getSelectedIndex() +
										JTabbedPane.TOP;
			firePropertyChange("UIOptionPanel.documentSelectionPlacement",
								old, documentSelectionPlacement);
		}

		else if (actionCommand.equals("LookAndFeelComboBox")) {
			hasUnsavedChanges = true;
			String newLnF = lnfCombo.getSelectedSpecialItem();
			firePropertyChange("UIOptionPanel.lookAndFeel", null, newLnF);
		}

		else if (actionCommand.equals("IconComboBox")) {
			hasUnsavedChanges = true;
			String name = imageLnFCombo.getSelectedSpecialItem();
			firePropertyChange("UIOptionPanel.iconStyle", null, name);
		}

		else if (actionCommand.equals("StatusBarComboBox")) {
			hasUnsavedChanges = true;
			int old = statusBarStyle;
			statusBarStyle = statusBarCombo.getSelectedIndex();
			firePropertyChange("UIOptionPanel.statusBarStyle", old, statusBarStyle);
		}

		else if (actionCommand.equals("HighlightModifiedCheckBox")) {
			boolean highlight = highlightModifiedDocumentDisplayNames();
			hmColorButton.setEnabled(highlight);
			hasUnsavedChanges = true;
			firePropertyChange("UIOptionPanel.highlightModified", !highlight, highlight);
		}

		else if (actionCommand.equals("ShowHostNameCB")) {
			boolean show = getShowHostName();
			hasUnsavedChanges = true;
			firePropertyChange("UIOptionPanel.showHostName", !show, show);
		}

	}


	/**
	 * Creates and returns a special value combo box containing all available
	 * Look and Feels.
	 *
	 * @param rtext The parent RText instance.
	 * @return The combo box.
	 */
	private static final SpecialValueComboBox createLookAndFeelComboBox(
			RText rtext) {

		SpecialValueComboBox combo = new SpecialValueComboBox();
		UIUtil.fixComboOrientation(combo);
		boolean osIsWindows = rtext.getOS()==RText.OS_WINDOWS;

		// Get all Look and Feels, with the system default listed first.
		LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
		for (int i=1; i<infos.length; i++) {
			String clazzName = infos[i].getClassName();
			if (clazzName.equals(UIManager.getSystemLookAndFeelClassName())) {
				LookAndFeelInfo temp = infos[0];
				infos[0] = infos[i];
				infos[i] = temp;
				break;
			}
		}

		for (int i=0; i<infos.length; i++) {
			// NOTE: It would be nice if we could check the
			// LookAndFeel.isSupportedLookAndFeel() method, but that would
			// require loading each LnF class, which we're trying to avoid.
			// We'll assume Windows supports all LnFs, and any other OS does
			// NOT support a standard Look with "Windows" in the name.
			String name = infos[i].getName();
			if (osIsWindows || !(name.toLowerCase().indexOf("windows")>-1)) {
				combo.addSpecialItem(name, infos[i].getClassName());
			}
		}

		// Add the Office Looks if we're on Windows.
		if (osIsWindows) {
			combo.addSpecialItem("MS Office XP",
				"org.fife.plaf.OfficeXP.OfficeXPLookAndFeel");
			combo.addSpecialItem("MS Office 2003",
				"org.fife.plaf.Office2003.Office2003LookAndFeel");
			combo.addSpecialItem("Visual Studio 2005",
				"org.fife.plaf.VisualStudio2005.VisualStudio2005LookAndFeel");
		}

		// Add any 3rd party Look and Feels in the lnfs subdirectory.
		ExtendedLookAndFeelInfo[] info = rtext.get3rdPartyLookAndFeelInfo();
		if (info!=null && info.length>0) {
			for (int i=0; i<info.length; i++) {
				combo.addSpecialItem(info[i].getName(),info[i].getClassName());
			}
		}

		return combo;

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
		mainView.setDocumentSelectionPlacement(getDocumentSelectionPlacement());	// Doesn't update if it doesn't have to.
		RTextUtilities.setLookAndFeel(rtext, getLookAndFeelClassName()); // Doesn't update if...
		rtext.setIconGroupByName(getIconGroupName());		// Doesn't update if it doesn't have to.
		mainView.setHighlightModifiedDocumentDisplayNames(highlightModifiedDocumentDisplayNames());
		mainView.setModifiedDocumentDisplayNamesColor(getModifiedDocumentDisplayNamesColor());
		rtext.setMainViewStyle(getMainViewStyle());			// Doesn't update if it doesn't have to.
		rtext.getStatusBar().setStyle(getStatusBarStyle());
		rtext.setShowHostName(getShowHostName());	// Doesn't update if doesn't have to.
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
	 * Returns the document selection placement the user chose.
	 *
	 * @return The document selection placement.
	 * @see #setDocumentSelectionPlacement
	 */
	public int getDocumentSelectionPlacement() {
		return documentSelectionPlacement;
	}


	/**
	 * Returns the icon style the user chose.
	 *
	 * @return The icon style.
	 * @see #setIconGroupByName
	 */
	public String getIconGroupName() {
		return imageLnFCombo.getSelectedSpecialItem();
	}


	/**
	 * Returns the selected Look and Feel.
	 *
	 * @return The look and feel.
	 * @see #setLookAndFeelByClassName
	 */
	public String getLookAndFeelClassName() {
		return lnfCombo.getSelectedSpecialItem();
	}


	/**
	 * Returns the main view style the user chose.
	 *
	 * @return The main view style, either <code>RText.TABBED_VIEW</code> or
	 *         <code>RText.SPLIT_PANE_VIEW</code>.
	 * @see #setMainViewStyle
	 */
	public int getMainViewStyle() {
		return mainViewStyle;
	}


	/**
	 * Gets the color the user chose to highlight modified documents'
	 * display names.
	 *
	 * @return color The color chosen.
	 * @see #setModifiedDocumentDisplayNamesColor
	 * @see #highlightModifiedDocumentDisplayNames
	 * @see #setHighlightModifiedDocumentDisplayNames
	 */
	public Color getModifiedDocumentDisplayNamesColor() {
		return hmColorButton.getColor();
	}


	/**
	 * Returns whether the user wants the host name displayed in the title
	 * bar.
	 *
	 * @return Whether to show the host name.
	 * @see #setShowHostName(String)
	 */
	public boolean getShowHostName() {
		return showHostNameCheckBox.isSelected();
	}


	/**
	 * Returns the status bar style selected by the user.
	 *
	 * @return The status bar style selected.
	 * @see #setStatusBarStyle
	 */
	public int getStatusBarStyle() {
		return statusBarStyle;
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
		return viewCombo;
	}


	/**
	 * Returns whether the user chose to highlight documents' display names
	 * with a different color.
	 *
	 * @return Whether or not the user chose to highlight documents' display
	 *         names with a different color.
	 *
	 * @see #setHighlightModifiedDocumentDisplayNames
	 * @see #getModifiedDocumentDisplayNamesColor
	 * @see #setModifiedDocumentDisplayNamesColor
	 */
	public boolean highlightModifiedDocumentDisplayNames() {
		return highlightModifiedCheckBox.isSelected();
	}


	/**
	 * Called whenever a property change occurs in this panel.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String propName = e.getPropertyName();

		if (propName.equals(RColorSwatchesButton.COLOR_CHANGED_PROPERTY)) {
			hasUnsavedChanges = true;
			firePropertyChange("UIOptionPanel." + propName,
								e.getOldValue(), e.getNewValue());
		}

	}


	/**
	 * Sets the tab placement displayed by this panel.
	 *
	 * @param documentSelectionPlacement The tab placement displayed; one of
	 *        <code>JTabbedPane.LEFT</code>, <code>TOP</code>,
	 *        <code>RIGHT</code>, or <code>BOTTOM</code>.
	 * @see #getDocumentSelectionPlacement
	 */
	public void setDocumentSelectionPlacement(int documentSelectionPlacement) {

		if (documentSelectionPlacement!=JTabbedPane.LEFT &&
				documentSelectionPlacement!=JTabbedPane.RIGHT &&
				documentSelectionPlacement!=JTabbedPane.TOP &&
				documentSelectionPlacement!=JTabbedPane.BOTTOM) {
			documentSelectionPlacement = JTabbedPane.TOP;
		}

		if (this.documentSelectionPlacement!=documentSelectionPlacement) {
			this.documentSelectionPlacement = documentSelectionPlacement;
			docSelCombo.setSelectedIndex(documentSelectionPlacement -
									JTabbedPane.TOP);
		}

	}


	/**
	 * Sets whether the "highlight modified documents' display names"
	 * checkbox is checked.
	 *
	 * @param highlight Whether or not the checkbox is to be checked.
	 * @see #highlightModifiedDocumentDisplayNames
	 * @see #getModifiedDocumentDisplayNamesColor
	 * @see #setModifiedDocumentDisplayNamesColor
	 */
	public void setHighlightModifiedDocumentDisplayNames(boolean highlight) {
		highlightModifiedCheckBox.setSelected(highlight);
		hmColorButton.setEnabled(highlight);
	}


	/**
	 * Sets the icon style displayed by this panel.
	 *
	 * @param name The name of the icon group.
	 * @see #getIconGroupName
	 */
	public void setIconGroupByName(String name) {
		int count = imageLnFCombo.getItemCount();
		for (int i=0; i<count; i++) {
			String specialValue = imageLnFCombo.getSpecialItemAt(i);
			if (specialValue.equals(name)) {
				imageLnFCombo.setSelectedIndex(i);
				return;
			}
		}
		imageLnFCombo.setSelectedIndex(0); // Default value???
	}


	/**
	 * Sets the look and feel displayed in this panel.
	 *
	 * @param name The class name for the Look.
	 * @see #getLookAndFeelClassName
	 */
	public void setLookAndFeelByClassName(String name) {
		int count = lnfCombo.getItemCount();
		for (int i=0; i<count; i++) {
			String specialValue = lnfCombo.getSpecialItemAt(i);
			if (specialValue.equals(name)) {
				lnfCombo.setSelectedIndex(i);
				return;
			}
		}
		lnfCombo.setSelectedIndex(0); // Default value???
	}


	/**
	 * Sets the main view style displayed by this panel.
	 *
	 * @param viewStyle The style displayed; either
	 *        <code>RText.TABBED_VIEW</code> or
	 *        <code>RText.SPLIT_PANE_VIEW</code>.
	 * @see #getMainViewStyle
	 */
	public void setMainViewStyle(final int viewStyle) {
		if (viewStyle==RText.TABBED_VIEW || viewStyle==RText.SPLIT_PANE_VIEW ||
			viewStyle==RText.MDI_VIEW)
			mainViewStyle = viewStyle;
		else
			mainViewStyle = RText.TABBED_VIEW;
		viewCombo.setSelectedIndex(mainViewStyle);
	}


	/**
	 * Sets the color for the button used for selecting the color used
	 * to highlight modified documents' display names.
	 *
	 * @param color The color to use.
	 * @throws NullPointerException If <code>color</code> is <code>null</code>.
	 * @see #getModifiedDocumentDisplayNamesColor
	 * @see #highlightModifiedDocumentDisplayNames
	 * @see #setHighlightModifiedDocumentDisplayNames
	 */
	public void setModifiedDocumentDisplayNamesColor(Color color) {
		if (color==null)
			throw new NullPointerException();
		hmColorButton.setColor(color);
	}


	/**
	 * Sets the "show hostname" checkbox's state.
	 *
	 * @param show Whether the checkbox should be enabled.
	 * @see #getShowHostName()
	 */
	public void setShowHostName(boolean show) {
		showHostNameCheckBox.setSelected(show);
	}


	/**
	 * Sets the status bar style selected.
	 *
	 * @param style The status bar style.
	 * @see #getStatusBarStyle
	 */
	public void setStatusBarStyle(int style) {
		if (style!=StatusBar.WINDOWS_98_STYLE &&
				style!=StatusBar.WINDOWS_XP_STYLE)
			style = StatusBar.WINDOWS_XP_STYLE;
		statusBarStyle = style;
		statusBarCombo.setSelectedIndex(statusBarStyle);
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
		setDocumentSelectionPlacement(mainView.getDocumentSelectionPlacement());
		setLookAndFeelByClassName(UIManager.getLookAndFeel().getClass().getName());
		setIconGroupByName(rtext.getIconGroup().getName());
		setMainViewStyle(rtext.getMainViewStyle());
		setHighlightModifiedDocumentDisplayNames(mainView.highlightModifiedDocumentDisplayNames());
		setStatusBarStyle(rtext.getStatusBar().getStyle());
		setModifiedDocumentDisplayNamesColor(mainView.getModifiedDocumentDisplayNamesColor());
		setShowHostName(rtext.getShowHostName());
	}


	@Override
	public void updateUI() {
		super.updateUI();
		if (springPanel!=null) {
			UIUtil.makeSpringCompactGrid(springPanel,
								2,2,		// rows,cols,
								0,0,		// initial-x, initial-y,
								5,5);	// x-spacing, y-spacing.
		}
		if (springPanel2!=null) {
			UIUtil.makeSpringCompactGrid(springPanel2,
								3,2,		// rows,cols,
								0,0,		// initial-x, initial-y,
								5,5);	// x-spacing, y-spacing.
		}
	}

}