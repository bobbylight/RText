/*
 * 03/19/2005
 *
 * TabbedPaneViewTransferHandler.java - A transfer handler that can transfer
 * files between RTextTabbedPaneViews.
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
package org.fife.rtext;

import java.awt.Container;
import java.awt.datatransfer.*;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.fife.ui.TabbedPaneTransferHandler;


/**
 * Transfer handler for the tabbed pane view that adds the ability to drag and
 * drop files into the tabbed pane.<p>
 *
 * This class is here instead of simply using
 * <code>MainPanelTransferHandler</code> because we need to retain the ability
 * to drag-and-drop tabs to change their order.
 *
 * @author Robert Futrell
 * @version 0.1
 */
class TabbedPaneViewTransferHandler extends TabbedPaneTransferHandler {

	/**
	 * The tabbed pane view doing the sending or receiving.
	 */
	private AbstractMainView mainView;


	/**
	 * Constructor.
	 *
	 * @param mainView An instance of <code>RTextTabbedPaneView</code>.
	 */
	public TabbedPaneViewTransferHandler(AbstractMainView mainView) {
		this.mainView = mainView;
	}


	/**
	 * Overridden to include a check for the file flavor.
	 */
	public boolean canImport(JComponent c, DataFlavor[] flavors) {
		return MainPanelTransferHandler.hasFileFlavor(flavors) ||
				super.canImport(c, flavors);
	}


	/**
	 * Called when the drag-and-drop operation has just completed.  This
	 * creates a new tab for the "dragged" file(s) and places it in the
	 * destination <code>JTabbedPane</code>.
	 *
	 * @param c The component receiving the "drop" (the instance of
	 *        <code>JTabbedPane</code>).
	 * @param t The data being transfered (information about the file).
	 * @return Whether or not the import was successful.
	 */
	public boolean importData(JComponent c, Transferable t) {
		return MainPanelTransferHandler.
			importDataImpl(mainView, c, t) || super.importData(c, t);
	}


	/**
	 * Selects the specified tab in the specified tabbed pane.  This method
	 * is overridden so that the <code>RTextTabbedPaneView</code> does any
	 * necessary stuff when changing the tab.
	 *
	 * @param tabbedPane The tabbed pane.
	 * @param index The index of the tab to select.
	 */
	protected void selectTab(final JTabbedPane tabbedPane, final int index) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Container parent = tabbedPane.getParent();
				if (parent instanceof AbstractMainView) {
					((AbstractMainView)parent).setSelectedIndex(index);
				}
				else {
					// Should never happen.
					tabbedPane.setSelectedIndex(index);
				}
			}
		});
	}


}