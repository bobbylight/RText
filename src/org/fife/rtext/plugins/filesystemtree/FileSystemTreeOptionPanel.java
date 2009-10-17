/*
 * 06/13/2005
 *
 * FileSystemTreeOptionPanel.java - Option panel for the FileSystemTree plugin.
 * Copyright (C) 2005 Robert Futrell
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
package org.fife.rtext.plugins.filesystemtree;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.fife.rtext.*;
import org.fife.ui.UIUtil;
import org.fife.ui.app.GUIApplicationConstants;
import org.fife.ui.app.GUIPlugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.Plugin;


/**
 * Option panel for the {@link FileSystemTree} plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FileSystemTreeOptionPanel extends PluginOptionsDialogPanel
					implements ItemListener, GUIApplicationConstants {

	private JComboBox locationCombo;

	private static final String LOCATION_PROPERTY = "LocationPanelProperty";


	/**
	 * Constructor.
	 */
	public FileSystemTreeOptionPanel(RText rtext, Plugin plugin) {

		super(plugin);
		ResourceBundle gpb = ResourceBundle.getBundle(
								"org/fife/ui/app/GUIPlugin");
		ResourceBundle fsvb = ResourceBundle.getBundle(
								FileSystemTreePlugin.BUNDLE_NAME);
		setName(fsvb.getString("Name"));

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		Border empty5Border = UIUtil.getEmpty5Border();
		setBorder(BorderFactory.createCompoundBorder(
				empty5Border,
				BorderFactory.createCompoundBorder(
					new OptionPanelBorder(fsvb.getString("OptionPanel.Title")),
					empty5Border)));
		setLayout(new BorderLayout());

		// A panel to contain everything that will go into our "top" area.
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		// A panel in which to select the file system tree placement.
		JPanel locationPanel = new JPanel();
		locationPanel.setLayout(new BoxLayout(locationPanel,
										BoxLayout.LINE_AXIS));
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
		locationPanel.add(locLabel);
		locationPanel.add(Box.createHorizontalStrut(5));
		locationPanel.add(locationCombo);
		locationPanel.add(Box.createHorizontalGlue());
		topPanel.add(locationPanel);

		// Put it all together!
		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(orientation);

	}


	/**
	 * Updates the file system tree plugin's parameters to reflect those in
	 * this options panel.
	 *
	 * @see #updateGUI
	 */
	protected void doApplyImpl(Frame owner) {
		FileSystemTreePlugin p = (FileSystemTreePlugin)getPlugin();
		p.setPosition(getFileSystemTreePlacement());
	}


	/**
	 * {@inheritDoc}
	 */
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		// They can't input invalid stuff on this options panel.
		return null;
	}


	/**
	 * Returns the selected placement for the file system tree.
	 *
	 * @return The selected placement.
	 * @see #setFileSystemPlacement
	 */
	public int getFileSystemTreePlacement() {
		return locationCombo.getSelectedIndex();
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
	 * Gets notified when the user selects an item in the location combo box.
	 *
	 * @param e The event.
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource()==locationCombo &&
				e.getStateChange()==ItemEvent.SELECTED) {
			hasUnsavedChanges = true;
			int placement = getFileSystemTreePlacement();
			firePropertyChange(LOCATION_PROPERTY, -1, placement);
		}
	}


	/**
	 * Sets the source browser placement displayed by this panel.
	 *
	 * @param placement The tab placement displayed; one of
	 *        <code>GUIApplication.LEFT</code>, <code>TOP</code>,
	 *        <code>RIGHT</code>, <code>BOTTOM</code> or <code>FLOATING</code>.
	 * @see #getFileSystemTreePlacement
	 */
	private void setFileSystemTreePlacement(int placement) {
		if (!GUIPlugin.isValidPosition(placement))
			placement = LEFT;
		locationCombo.setSelectedIndex(placement);
	}


	/**
	 * Updates this panel's displayed parameter values to reflect those of
	 * the file system tree plugin.
	 */
	protected void setValuesImpl(Frame frame) {
		FileSystemTreePlugin p = (FileSystemTreePlugin)getPlugin();
		setFileSystemTreePlacement(p.getPosition());
	}


}