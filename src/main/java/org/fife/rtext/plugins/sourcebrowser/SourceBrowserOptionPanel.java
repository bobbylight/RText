/*
 * 06/14/2004
 *
 * SourceBrowserOptionPanel.java - Option panel for the Source Browser
 * component.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.sourcebrowser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.fife.rtext.*;
import org.fife.ui.FSATextField;
import org.fife.ui.Hyperlink;
import org.fife.ui.RFileChooser;
import org.fife.ui.UIUtil;
import org.fife.ui.app.GUIApplicationConstants;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.Plugin;
import org.fife.ui.dockablewindows.DockableWindow;


/**
 * Option panel for the Source Browser component.  This component requires
 * ctags to be installed on the system.<p>
 * 
 * The source browser will provide a list of all
 * functions/variables/classes/etc. declared in a source file, allowing the
 * programmer quick access to them.  See the documentation for
 * <code>org.fife.rtext.plugins.sourcebrowser.SourceBrowser</code> for more
 * information.
 *
 * @author Robert Futrell
 * @version 1.2
 */
class SourceBrowserOptionPanel extends PluginOptionsDialogPanel
						implements ActionListener, DocumentListener,
								ItemListener, GUIApplicationConstants {

	private JCheckBox visibleCB;
	private JComboBox locationCombo;
	private JLabel ctagsExecutableLocationLabel;
	private JRadioButton exubCtagsRB;
	private JRadioButton standardCtagsRB;
	private JRadioButton lastSelectedCtagsRB;
	private FSATextField ctagsExecutableTextField;
	private JButton exeBrowseButton;
	private JCheckBox htmlToolTipCheckBox;

	private RFileChooser exeFileChooser;

	private static final String CTAGS_LOCATION_PROPERTY = "CTagsLocation";
	private static final String CTAGS_TYPE_PROPERTY = "CTagsType";
	private static final String HTML_TOOLTIPS_PROPERTY = "HTMLToolTips";
	private static final String PROPERTY				= "Property";
	private static final String SB_LOCATION_PROPERTY = "SBLoc";

	private static final String CTAGS_HOME_PAGE	= "http://ctags.sourceforge.net";


	/**
	 * Constructor.
	 */
	public SourceBrowserOptionPanel(RText rtext, Plugin plugin) {

		super(plugin);
		ResourceBundle gpb = ResourceBundle.getBundle(
								"org/fife/ui/app/GUIPlugin");
		ResourceBundle sbb = ResourceBundle.getBundle(
								SourceBrowserPlugin.BUNDLE_NAME);
		setName(sbb.getString("Name"));

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
		boolean ltr = orientation.isLeftToRight();

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		// A panel to contain everything that will go into our "top" area.
		Box topPanel = Box.createVerticalBox();
		topPanel.setBorder(
				new OptionPanelBorder(sbb.getString("OptionPanel.Title")));

		// A check box toggling the plugin's visibility.
		JPanel temp = new JPanel(new BorderLayout());
		visibleCB = new JCheckBox(gpb.getString("Visible"));
		visibleCB.addActionListener(this);
		temp.add(visibleCB, BorderLayout.LINE_START);
		topPanel.add(temp);
		topPanel.add(Box.createVerticalStrut(5));

		// A combo in which to select the file system tree placement.
		temp = new JPanel(new SpringLayout());
		locationCombo = new JComboBox();
		UIUtil.fixComboOrientation(locationCombo);
		locationCombo.addItem(gpb.getString("Location.top"));
		locationCombo.addItem(gpb.getString("Location.left"));
		locationCombo.addItem(gpb.getString("Location.bottom"));
		locationCombo.addItem(gpb.getString("Location.right"));
		locationCombo.addItem(gpb.getString("Location.floating"));
		locationCombo.addItemListener(this);
		JLabel locLabel = new JLabel(gpb.getString("Location.title"));
		locLabel.setLabelFor(locationCombo);

		// A field in which to enter the CTags executable location.
		ctagsExecutableLocationLabel = new JLabel(
					sbb.getString("OptionPanel.Label.ExecutablePath"));
		ctagsExecutableTextField = new FSATextField();
		ctagsExecutableTextField.setColumns(30);
		ctagsExecutableTextField.getDocument().addDocumentListener(this);
		ctagsExecutableLocationLabel.setLabelFor(ctagsExecutableTextField);
		exeBrowseButton = new JButton(sbb.getString(
									"OptionPanel.Button.Browse"));
		exeBrowseButton.setActionCommand("Browse");
		exeBrowseButton.addActionListener(this);

		// A panel to select whether the ctags binary is "Exuberant."
		JLabel label = new JLabel(sbb.getString("OptionPanel.Label.CtagsType"));
		label.setVerticalAlignment(JLabel.TOP);
		JPanel typePanel = new JPanel(new BorderLayout());
		ButtonGroup bg = new ButtonGroup();
		exubCtagsRB = UIUtil.newRadio(sbb, "OptionPanel.CtagsType.Exuberant", bg, this, true);
		Container temp2 = new JPanel(new BorderLayout());
		temp2.add(exubCtagsRB, BorderLayout.LINE_START);
		typePanel.add(temp2, BorderLayout.NORTH);
		standardCtagsRB = UIUtil.newRadio(sbb, "OptionPanel.CtagsType.Standard", bg, this);
		temp2 = new JPanel(new BorderLayout());
		temp2.add(standardCtagsRB, BorderLayout.LINE_START);
		typePanel.add(temp2, BorderLayout.SOUTH);
		temp2 = new JPanel(new BorderLayout());
		temp2.add(typePanel, BorderLayout.LINE_START);
		
		Dimension filler = new Dimension(1,1);
		if (ltr) {
			temp.add(locLabel);
			temp.add(locationCombo);
			// The "filler" rigid area below MUST have finite preferred width.
			temp.add(Box.createRigidArea(filler));
			temp.add(ctagsExecutableLocationLabel);
			temp.add(ctagsExecutableTextField);
			temp.add(exeBrowseButton);
			temp.add(label);
			temp.add(temp2);
			temp.add(Box.createRigidArea(filler)); // MUST have finite pw.
		}
		else {
			// The "filler" rigid area below MUST have finite preferred width.
			temp.add(Box.createRigidArea(filler));
			temp.add(locationCombo);
			temp.add(locLabel);
			temp.add(exeBrowseButton);
			temp.add(ctagsExecutableTextField);
			temp.add(ctagsExecutableLocationLabel);
			temp.add(Box.createRigidArea(filler)); // MUST have finite pw.
			temp.add(temp2);
			temp.add(label);
		}

		UIUtil.makeSpringCompactGrid(temp, 3,3, 5,5, 5,5);
		topPanel.add(temp);
		topPanel.add(Box.createVerticalStrut(5));

		// A panel for the "HTML tooltips" checkbox.
		JPanel toolTipPanel = new JPanel(new BorderLayout());
		htmlToolTipCheckBox = new JCheckBox(sbb.getString(
									"OptionPanel.HTMLToolTips"));
		htmlToolTipCheckBox.setActionCommand("HTMLToolTips");
		htmlToolTipCheckBox.addActionListener(this);
		toolTipPanel.add(htmlToolTipCheckBox, BorderLayout.LINE_START);
		topPanel.add(toolTipPanel);

		// A link to the Exuberant Ctags project.
		topPanel.add(Box.createVerticalStrut(20));
		temp = new JPanel(new BorderLayout());
		String text = sbb.getString("OptionPanel.ExuberantDesc");
		int linkPos = text.indexOf("{0}");
		if (linkPos>-1) { // Always true
			String part1 = text.substring(0, linkPos);
			String part3 = text.substring(linkPos+3);
			temp2 = createHorizontalBox();
			temp2.add(new JLabel(part1));
			Hyperlink link = new Hyperlink(
				sbb.getString("OptionPanel.ExuberantHomePage"),
				CTAGS_HOME_PAGE);
			temp2.add(link);
			temp2.add(new JLabel(part3));
			temp.add(temp2, BorderLayout.LINE_START);
		}
		else { // Should never be true.
			label = new JLabel(text);
			temp.add(label, BorderLayout.LINE_START);
		}
		topPanel.add(temp);

		// Put it all together!
		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(orientation);

	}


	/**
	 * Listens for actions being performed in this panel.
	 */
	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();
		String command = e.getActionCommand();

		if (visibleCB==source) {
			hasUnsavedChanges = true;
			boolean visible = visibleCB.isSelected();
			firePropertyChange(PROPERTY, !visible, visible);
		}

		else if ("Browse".equals(command)) {
			if (exeFileChooser==null) {
				exeFileChooser = new RFileChooser();
				//exeFileChooser.setFileFilter(new ExecutableFileFilter());
				exeFileChooser.applyComponentOrientation(
										getComponentOrientation());
			}
			int returnVal = exeFileChooser.showOpenDialog(this);
			// If they selected a file and clicked "OK", open the flie!
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				ctagsExecutableTextField.setFileSystemAware(false);
				ctagsExecutableTextField.setText(
					exeFileChooser.getSelectedFile().getAbsolutePath());
				ctagsExecutableTextField.setFileSystemAware(true);
			}
		}

		else if (exubCtagsRB==source) {
			if (lastSelectedCtagsRB!=exubCtagsRB) {
				lastSelectedCtagsRB = exubCtagsRB;
				hasUnsavedChanges = true;
				String value = SourceBrowserPlugin.CTAGS_TYPE_EXUBERANT;
				firePropertyChange(CTAGS_TYPE_PROPERTY, null, value);
			}
		}

		else if ("HTMLToolTips".equals(command)) {
			hasUnsavedChanges = true;
			boolean value = htmlToolTipCheckBox.isSelected();
			firePropertyChange(HTML_TOOLTIPS_PROPERTY, !value, value);
		}

		else if (standardCtagsRB==source) {
			if (lastSelectedCtagsRB!=standardCtagsRB) {
				lastSelectedCtagsRB = standardCtagsRB;
				hasUnsavedChanges = true;
				String value = SourceBrowserPlugin.CTAGS_TYPE_STANDARD;
				firePropertyChange(CTAGS_TYPE_PROPERTY, null, value);
			}
		}

	}


	/**
	 * Called when a property of a document of a text field in this panel
	 * gets changed (never happens).
	 */
	public void changedUpdate(DocumentEvent e) {
	}


	/**
	 * Updates the source browser plugin's parameters to reflect those in
	 * this options panel.
	 */
	@Override
	protected void doApplyImpl(Frame frame) {
		SourceBrowserPlugin p = (SourceBrowserPlugin)getPlugin();
		DockableWindow wind = p.getDockableWindow(p.getPluginName());
		wind.setActive(visibleCB.isSelected());
		wind.setPosition(locationCombo.getSelectedIndex());//getSourceBrowserPlacement());
		p.setCTagsExecutableLocation(ctagsExecutableTextField.getText());
		p.setCTagsType(getCTagsType());
		p.setUseHTMLToolTips(getUseHTMLToolTips());
	}


	/**
	 * Called when a text field in this panel gets updated.
	 */
	private void doDocumentUpdated(DocumentEvent e) {

		hasUnsavedChanges = true;

		// Fire the property change so the "Apply" button gets enabled.
		// We must check documents as DocumentEvent does not have getSource().
		Document modifiedDocument = e.getDocument();
		if (modifiedDocument==ctagsExecutableTextField.getDocument()) {
			firePropertyChange(CTAGS_LOCATION_PROPERTY, null,
								ctagsExecutableTextField.getText());
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
	 * Returns a string representation of the type of Ctags specified by
	 * the user.
	 *
	 * @return The type of Ctags specified.
	 * @see #setCTagsType(String)
	 * @see SourceBrowserPlugin#CTAGS_TYPE_EXUBERANT
	 * @see SourceBrowserPlugin#CTAGS_TYPE_STANDARD
	 */
	public String getCTagsType() {
		return exubCtagsRB.isSelected() ?
			SourceBrowserPlugin.CTAGS_TYPE_EXUBERANT :
			SourceBrowserPlugin.CTAGS_TYPE_STANDARD;
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
		return locationCombo;
	}


	/**
	 * Returns whether HTML tooltips should be used by the source browser.
	 *
	 * @return Whether HTML tooltips should be used.
	 * @see #setUseHTMLToolTips
	 */
	public boolean getUseHTMLToolTips() {
		return htmlToolTipCheckBox.isSelected();
	}


	/**
	 * Called whenever text is inserted into the ctags executable text field.
	 */
	public void insertUpdate(DocumentEvent e) {
		doDocumentUpdated(e);
	}


	/**
	 * Gets notified when the user selects an item in the location combo box.
	 *
	 * @param e The event.
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource()==locationCombo &&
				e.getStateChange()==ItemEvent.SELECTED) {
			hasUnsavedChanges = true;
			int placement = locationCombo.getSelectedIndex();
			firePropertyChange(SB_LOCATION_PROPERTY, -1, placement);
		}
	}


	/**
	 * Called when a text field in this panel gets updated.
	 */
	public void removeUpdate(DocumentEvent e) {
		doDocumentUpdated(e);
	}


	/**
	 * Sets the path to the ctags executable displayed by this options panel.
	 *
	 * @param path The path to display.
	 * @see #getCTagsExecutableLocation
	 */
	private void setCTagsExecutableLocation(String path) {
		ctagsExecutableTextField.setFileSystemAware(false);
		ctagsExecutableTextField.setText(path);
		ctagsExecutableTextField.setFileSystemAware(true);
	}


	/**
	 * Sets the source browser placement displayed by this panel.
	 *
	 * @param placement The tab placement displayed; one of
	 *        <code>GUIApplication.LEFT</code>, <code>TOP</code>,
	 *        <code>RIGHT</code>, <code>BOTTOM</code> or <code>FLOATING</code>.
	 * @see #getSourceBrowserPlacement
	 */
	private void setSourceBrowserPlacement(int placement) {
		if (!DockableWindow.isValidPosition(placement))
			placement = LEFT;
		locationCombo.setSelectedIndex(placement);
	}


	/**
	 * Sets thetype of Ctags specified by the user.
	 *
	 * @return The type of Ctags specified.  If this is <code>null</code> or
	 *         invalid, {@link SourceBrowserPlugin#CTAGS_TYPE_EXUBERANT} is
	 *         used.
	 * @see #getCTagsType()
	 * @see SourceBrowserPlugin#CTAGS_TYPE_EXUBERANT
	 * @see SourceBrowserPlugin#CTAGS_TYPE_STANDARD
	 */
	public void setCTagsType(String type) {
		if (SourceBrowserPlugin.CTAGS_TYPE_EXUBERANT.equals(type)) {
			exubCtagsRB.setSelected(true);
			lastSelectedCtagsRB = exubCtagsRB;
		}
		else if (SourceBrowserPlugin.CTAGS_TYPE_STANDARD.equals(type)) {
			standardCtagsRB.setSelected(true);
			lastSelectedCtagsRB = standardCtagsRB;
		}
		else { // Invalid type specified
			exubCtagsRB.setSelected(true);
			lastSelectedCtagsRB = exubCtagsRB;
		}
	}


	/**
	 * Sets whether the "HTML tooltips" checkbox is selected.
	 *
	 * @param use Whether or not the "HTML tooltips" checkbox is selected.
	 * @see #getUseHTMLToolTips
	 */
	public void setUseHTMLToolTips(boolean use) {
		htmlToolTipCheckBox.setSelected(use);
	}


	/**
	 * Updates this panel's displayed parameter values to reflect those of
	 * the source browser plugin.
	 */
	@Override
	protected void setValuesImpl(Frame frame) {
		SourceBrowserPlugin p = (SourceBrowserPlugin)getPlugin();
		DockableWindow wind = p.getDockableWindow(p.getPluginName());
		visibleCB.setSelected(wind.isActive());
		setSourceBrowserPlacement(wind.getPosition());
		setCTagsExecutableLocation(p.getCTagsExecutableLocation());
		setCTagsType(p.getCTagsType());
		setUseHTMLToolTips(p.getUseHTMLToolTips());
	}


}