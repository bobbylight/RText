/*
 * 03/19/2005
 *
 * MainPanelTransferHandler.java - Transfer handler for RText's main views
 * capable of loading files from drag-and-drop.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.io.File;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.TransferHandler;


/**
 * A transfer handler for RText's main view capable of receiving files
 * from drag-and-drop.<p>
 *
 * Note that this class is only used by the split pane view and MDI view, not
 * by the tabbed pane view.  This is because the tabbed pane view has its own
 * transfer handler subclassed from a different transfer handler allowing the
 * drag-and-drop of tabs from one location to another.  Methods such as
 * <code>canImport</code> and <code>importData</code> will return these
 * child components and not the main view, so we need to know what it is
 * ahead of time.
 *
 * @author Robert Futrell
 * @version 0.1
 */
class MainPanelTransferHandler extends TransferHandler {

	/**
	 * The tabbed pane view doing the sending or receiving.
	 */
	private AbstractMainView mainView;

	/**
	 * Data flavor for importing files.
	 */
	private static final DataFlavor fileFlavor = DataFlavor.javaFileListFlavor;



	/**
	 * Constructor.<p>
	 *
	 * You must pass in the main view that will receive the files because this
	 * transfer handler is often registered on child components of the actual
	 * main view component (such as the "file list" of the split pane view or
	 * the "desktop pane" of the MDI view).  
	 *
	 * @param mainView The main view.
	 */
	public MainPanelTransferHandler(AbstractMainView mainView) {
		this.mainView = mainView;
	}



	/**
	 * Ensures that the data being imported can be read as a list of files.
	 */
	@Override
	public boolean canImport(JComponent c, DataFlavor[] flavors) {
		return hasFileFlavor(flavors);
	}


	/**
	 * Says that we cannot move or copy data from this main view, only add
	 * to it.
	 *
	 * @param c This parameter is ignored.
	 * @return <code>TransferHandler.NONE</code>, as we can only add data, not
	 *         move it or copy it.
	 */
	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.NONE;
	}

	
	/**
	 * Does the flavor list have the file flavor?
	 *
	 * @param flavors The flavors in which to check for the file flavor.
	 * @return Whether <code>flavors</code> contains the file flavor.
	 */
	public static boolean hasFileFlavor(DataFlavor[] flavors) {
		for (int i=0; i<flavors.length; i++) {
			if (fileFlavor.equals(flavors[i]))
				return true;
		}
		return false;
	}


	/**
	 * Called when the drag-and-drop operation has just completed.  This
	 * creates a new tab for the "dragged" file(s) and places it in the
	 * destination <code>AbstractMainView</code>.
	 *
	 * @param c The component receiving the "drop".
	 * @param t The data being transfered (information about the file).
	 * @return Whether or not the import was successful.
	 */
	@Override
	public boolean importData(JComponent c, Transferable t) {
		return MainPanelTransferHandler.importDataImpl(mainView, c, t);
	}


	/**
	 * Does the dirty work of importing file data.  This method is static so
	 * that it can be called by <code>TabbedPaneViewTransferHandler</code>,
	 * which unfortunately must derive from a different superclass.
	 *
	 * @param mainView The main view receiving the data.
	 * @param c The component receiving the "drop".
	 * @param t The data being transfered (information about the file).
	 * @return Whether or not the import was successful.
	 */
	public static boolean importDataImpl(AbstractMainView mainView,
								JComponent c, Transferable t) {

		if (hasFileFlavor(t.getTransferDataFlavors())) {
			try {
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>)t.getTransferData(fileFlavor);
				int count = files==null ? 0 : files.size();
				for (int i=0; i<count; i++) {
					File file = files.get(i);
					// "null" encoding means check for Unicode first.
					mainView.openFile(file.getAbsolutePath(), null);
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}


}