/*
 * 06/14/2004
 *
 * SourceBrowserOptionPanel.java - Option panel for the Source Browser
 * component.
 * Copyright (C) 2004 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This file is a part of RText.
 *
 * RText is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
import org.fife.ui.RButton;
import org.fife.ui.RFileChooser;
import org.fife.ui.UIUtil;
import org.fife.ui.app.GUIApplicationConstants;
import org.fife.ui.app.GUIPlugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.Plugin;


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

	private JComboBox locationCombo;

	private JLabel ctagsExecutableLocationLabel;
	private JRadioButton exubCtagsRB;
	private JRadioButton standardCtagsRB;
	private JRadioButton lastSelectedCtagsRB;
	private FSATextField ctagsExecutableTextField;
	private RButton exeBrowseButton;
	private JCheckBox htmlToolTipCheckBox;

	private RFileChooser exeFileChooser;

	private static final String CTAGS_LOCATION_PROPERTY = "CTagsLocation";
	private static final String CTAGS_TYPE_PROPERTY = "CTagsType";
	private static final String HTML_TOOLTIPS_PROPERTY = "HTMLToolTips";
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
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setBorder(
				new OptionPanelBorder(sbb.getString("OptionPanel.Title")));

		// A combo in which to select the file system tree placement.
		JPanel temp = new JPanel(new SpringLayout());
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
		// Postpone discovering the parent window since this panel hasn't
		// been added to the dialog yet.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ctagsExecutableTextField.discoverParentWindow();
			}
		});
		ctagsExecutableTextField.getDocument().addDocumentListener(this);
		ctagsExecutableLocationLabel.setLabelFor(ctagsExecutableTextField);
		exeBrowseButton = new RButton(sbb.getString(
									"OptionPanel.Button.Browse"));
		exeBrowseButton.setActionCommand("Browse");
		exeBrowseButton.addActionListener(this);

		// A panel to select whether the ctags binary is "Exuberant."
		JLabel label = new JLabel(sbb.getString("OptionPanel.Label.CtagsType"));
		label.setVerticalAlignment(JLabel.TOP);
		JPanel typePanel = new JPanel(new BorderLayout());
		exubCtagsRB = new JRadioButton(
					sbb.getString("OptionPanel.CtagsType.Exuberant"));
		exubCtagsRB.setActionCommand("ExuberantCtags");
		exubCtagsRB.addActionListener(this);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(exubCtagsRB, BorderLayout.LINE_START);
		typePanel.add(temp2, BorderLayout.NORTH);
		standardCtagsRB = new JRadioButton(
					sbb.getString("OptionPanel.CtagsType.Standard"));
		standardCtagsRB.setActionCommand("StandardCtags");
		standardCtagsRB.addActionListener(this);
		temp2 = new JPanel(new BorderLayout());
		temp2.add(standardCtagsRB, BorderLayout.LINE_START);
		typePanel.add(temp2, BorderLayout.SOUTH);
		temp2 = new JPanel(new BorderLayout());
		temp2.add(typePanel, BorderLayout.LINE_START);
		ButtonGroup bg = new ButtonGroup();
		bg.add(exubCtagsRB);
		bg.add(standardCtagsRB);
		exubCtagsRB.setSelected(true);
		
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
			temp2 = new JPanel();
			temp2.setLayout(new BoxLayout(temp2, BoxLayout.LINE_AXIS));
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

		String actionCommand = e.getActionCommand();

		if (actionCommand.equals("Browse")) {
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

		else if (actionCommand.equals("ExuberantCtags")) {
			if (lastSelectedCtagsRB!=exubCtagsRB) {
				lastSelectedCtagsRB = exubCtagsRB;
				hasUnsavedChanges = true;
				String value = SourceBrowserPlugin.CTAGS_TYPE_EXUBERANT;
				firePropertyChange(CTAGS_TYPE_PROPERTY, null, value);
			}
		}

		else if (actionCommand.equals("HTMLToolTips")) {
			hasUnsavedChanges = true;
			boolean value = htmlToolTipCheckBox.isSelected();
			firePropertyChange(HTML_TOOLTIPS_PROPERTY, !value, value);
		}

		else if (actionCommand.equals("StandardCtags")) {
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
	protected void doApplyImpl(Frame frame) {
		SourceBrowserPlugin p = (SourceBrowserPlugin)getPlugin();
		p.setPosition(locationCombo.getSelectedIndex());//getSourceBrowserPlacement());
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
	 * Checks whether or not all input the user specified on this panel is
	 * valid.  This should be overridden to check, for example, whether
	 * text fields have valid values, etc.  This method will be called
	 * whenever the user clicks "OK" or "Apply" on the options dialog to
	 * ensure all input is valid.  If it isn't, the component with invalid
	 * data will be given focus and the user will be prompted to fix it.<br>
	 * 
	 *
	 * @return <code>null</code> if the panel has all valid inputs, or an
	 *         <code>OptionsPanelCheckResult</code> if an input was invalid.
	 *         This component is the one that had the error and will be
	 *         given focus, and the string is an error message that will be
	 *         displayed.
	 */
	public OptionsPanelCheckResult ensureValidInputs() {
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
		if (!GUIPlugin.isValidPosition(placement))
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
	protected void setValuesImpl(Frame frame) {
		SourceBrowserPlugin p = (SourceBrowserPlugin)getPlugin();
		setSourceBrowserPlacement(p.getPosition());
		setCTagsExecutableLocation(p.getCTagsExecutableLocation());
		setCTagsType(p.getCTagsType());
		setUseHTMLToolTips(p.getUseHTMLToolTips());
	}


}