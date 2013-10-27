/*
 * 04/16/2004
 *
 * RTextMDIView.java - A multi-document interface implementation.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rtextarea.RTextScrollPane;


/**
 * An implementation of a multi-document interface.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class RTextMDIView extends AbstractMainView implements InternalFrameListener {

	private static final int CASCADE_X_INCREMENT		= 30;
	private static final int CASCADE_Y_INCREMENT		= 30;

	private static int openFrameCount = 0;

	private JDesktopPane desktopPane;
	private java.util.List<InternalFrame> frames;

	private JPopupMenu popupMenu;

	// Whether documentList should be in the top, left, bottom, or right pane.
	private int documentSelectionPlacement;


	/**
	 * Creates a new <code>RTextMDIView</code>.
	 *
	 * @param owner The <code>RText</code> that this view sits in.
	 * @param filesToOpen Array of strings representing files to open.  If
	 *        this parameter is null, a single file with a default name is
	 *        opened.
	 * @param properties A properties object used to initialize some fields on
	 *        this view.
	 */
	public RTextMDIView(RText owner, String[] filesToOpen,
									RTextPreferences properties) {

		frames = new ArrayList<InternalFrame>(5);

		setLayout(new GridLayout(1,1));
		desktopPane = new JDesktopPane();
		desktopPane.setBackground(Color.GRAY);
		desktopPane.addMouseListener(new MDIMouseListener());
		add(desktopPane);

		// Add transfer handler to listen for files being drag-and-dropped
		// into this main view.
		TransferHandler th = new MainPanelTransferHandler(this);
		desktopPane.setTransferHandler(th);

		// Set everything up.
		initialize(owner, filesToOpen, properties);

	}


	/**
	 * Adds a new document to the desktop pane and places a number beside
	 * documents opened multiple times.
	 *
	 * @param title The "display name" for the document.
	 * @param component The scroll pane containing the text editor to add.
	 * @param fileFullPath The full path to the document being added.
	 */
	@Override
	protected void addTextAreaImpl(String title, Component component,
							String fileFullPath) {

		JPanel temp = new JPanel(new BorderLayout());
		temp.add(component);
		RTextScrollPane sp = (RTextScrollPane)component;
		RTextEditorPane textArea = (RTextEditorPane)sp.getTextArea();
		ErrorStrip es = createErrorStrip(textArea);
		temp.add(es, BorderLayout.LINE_END);

		// "Physically" add the frame.
		InternalFrame frame = new InternalFrame(title, temp);
		frame.setVisible(true);	// Necessary.
		frame.addInternalFrameListener(this);
		desktopPane.add(frame);
		frames.add(frame);

		// Loop through all tabs (documents) except the last (the one just added).
		int numDocuments = getNumDocuments();
		for (int i=0; i<numDocuments-1; i++) {

			// If any of them is the same physical file as the just added one, do the numbering.
			if (getRTextEditorPaneAt(i).getFileFullPath().equals(fileFullPath)) {
				int count = 0;
				for (int j=i; j<numDocuments; j++) {
					RTextEditorPane pane = getRTextEditorPaneAt(j);
					if (pane.getFileFullPath().equals(fileFullPath)) {
						String newTitle = title + " (" + (++count) + ")";
						if (pane.isDirty())
							newTitle = newTitle + "*";
						try {
							setDocumentDisplayNameAt(j, newTitle);
						} catch (Exception e) { System.err.println("Exception: " + e); }
					}
				}
				break;
			}

		}

		// Do any extra stuff.
		// This updates currentTextArea and shifts focus too.
		setSelectedIndex(numDocuments-1);
		if (getCurrentTextArea().isDirty())
			owner.setMessages(fileFullPath + "*", "Opened document '" + fileFullPath + "'");
		else
			owner.setMessages(fileFullPath, "Opened document '" + fileFullPath + "'");
		
		// RText's listeners will be updated by stateChanged() for all
		// addTextAreaImpl() calls.
		
	}


	/**
	 * Cascades the windows in this MDI view.
	 */
	public void cascadeWindows() {

		int x = 0;
		int y = 0;
		int numRows = 0;
		Dimension desktopSize = desktopPane.getSize();

		int size = frames.size();
		int selectedIndex = getSelectedIndex();
		for (int i=0; i<size; i++) {
			if (i==selectedIndex)
				continue;
			JInternalFrame frame = frames.get(i);
			frame.setBounds(x,y, 300,300);
			frame.toFront();
			x += CASCADE_X_INCREMENT;
			y += CASCADE_Y_INCREMENT;
			if (y + 300 >= desktopSize.height) {
				x = (CASCADE_X_INCREMENT*2) * ++numRows + CASCADE_X_INCREMENT/2;
				y = 0;
			}
		}

		// Make it so current text area is "last" to be cascaded by swapping
		// its place with the last frame's place.
		JInternalFrame currentFrame = frames.get(selectedIndex);
		currentFrame.setBounds(x,y, 300,300);
		currentFrame.toFront();

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected synchronized boolean closeCurrentDocumentImpl() {

		ResourceBundle msg = owner.getResourceBundle();

		// Return code for if the user is prompted to save; returns yes for
		// closeAllDocuments().
		int rc = promptToSaveBeforeClosingIfDirty();
		if (rc==JOptionPane.CANCEL_OPTION) {
			return false;
		}

		// Remove the document from this tabbed pane.
		removeComponentAt(getSelectedIndex());

		// If there are open documents, make sure any duplicates are numbered
		// correctly. If there are no open documents, add a new empty one.
		if (getNumDocuments()>0) {
			renumberDisplayNames();
			JInternalFrame frame = frames.get(0);
			desktopPane.setSelectedFrame(frame);
			try {
				frame.setSelected(true); // Updates currentTextArea.
			} catch (PropertyVetoException e) {
				e.printStackTrace(); // Never happens.
			}
			frame.toFront();
		}
		else
			addNewEmptyUntitledFile();

		// Update the RText's status bar.
		updateStatusBar();

		// Update RText's title and the status bar message.
		RTextEditorPane editor = getCurrentTextArea();
		if (editor.isDirty())
			owner.setMessages(editor.getFileFullPath() + "*", msg.getString("Ready"));
		else
			owner.setMessages(editor.getFileFullPath(), msg.getString("Ready"));

		return true;

	}


	/**
	 * Creates the popup menu for the desktop pane.
	 */
	protected void createPopupMenu() {

		popupMenu = new JPopupMenu();

		JMenuItem menuItem = new JMenuItem(new AbstractAction() {
								public void actionPerformed(java.awt.event.ActionEvent e) {
									tileWindowsVertically();
								}
							});
		menuItem.setText("Tile Vertically");
		popupMenu.add(menuItem);

		menuItem = new JMenuItem(new AbstractAction() {
								public void actionPerformed(java.awt.event.ActionEvent e) {
									tileWindowsHorizontally();
								}
							});
		menuItem.setText("Tile Horizontally");
		popupMenu.add(menuItem);

		menuItem = new JMenuItem(new AbstractAction() {
								public void actionPerformed(java.awt.event.ActionEvent e) {
									cascadeWindows();
								}
							});
		menuItem.setText("Cascade");
		popupMenu.add(menuItem);

	}


	/**
	 * Returns the name being displayed for the document.  For example, in a tabbed
	 * pane, this could be the text on the tab for this document.
	 *
	 * @param index The index at which to find the name.  If the index is invalid,
	 *        <code>null</code> is returned.
	 * @return The name being displayed for this document.
	 */
	@Override
	public String getDocumentDisplayNameAt(int index) {
		if (index>=0 && index<getNumDocuments()) {
			return ((JInternalFrame)frames.get(index)).getTitle();
		}
		return null;
	}


	/**
	 * Returns the location of the document selection area of this component.
	 * Note that this value currently has no effect on an an instance of
	 * <code>RTextMDIView</code>.
	 *
	 * @return The location of the document selection area.
	 */
	@Override
	public int getDocumentSelectionPlacement() {
		return documentSelectionPlacement;
	}


	/**
	 * Returns the number of documents open in this container.
	 *
	 * @return The number of open documents.
	 */
	@Override
	public int getNumDocuments() {
		return frames.size();
	}


	/**
	 * Returns the preferred size of this MDI view.
	 *
	 * @return The preferred size of this view.
	 */
	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = new Dimension(300,300); // Default value.
		int numDocuments = frames.size();
		for (int i=0; i<numDocuments; i++) {
			Dimension framePreferredSize = ((JInternalFrame)frames.get(i)).getPreferredSize();
			preferredSize.width = Math.max(framePreferredSize.width, preferredSize.width);
			preferredSize.height = Math.max(framePreferredSize.height, preferredSize.height);
		}
		return preferredSize;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public RTextScrollPane getRTextScrollPaneAt(int index) {
		if (index<0 || index>=getNumDocuments())
			//throw new IndexOutOfBoundsException();
			return null;
		JPanel temp = (JPanel)((JInternalFrame)frames.get(index)).
											getContentPane().getComponent(0);
		return (RTextScrollPane)temp.getComponent(0);
	}


	/**
	 * Returns the currently active component.
	 *
	 * @return The component.
	 */
	@Override
	public Component getSelectedComponent() {
		return desktopPane.getSelectedFrame();
	}


	/**
	 * Returns the currently selected document's index.
	 *
	 * @return The index of the currently selected document.
	 */
	@Override
	public int getSelectedIndex() {
		return frames.indexOf(desktopPane.getSelectedFrame());
	}


	/**
	 * Called when an internal frame is activated.
	 */
	public void internalFrameActivated(InternalFrameEvent e) {

		RTextEditorPane current = getRTextEditorPaneAt(getSelectedIndex());
		setCurrentTextArea(current);

		// Update RText's title bar and status bar.
		String title = current.getFileFullPath()+(current.isDirty() ? "*" : "");
		owner.setMessages(title, null);
		updateStatusBar();	// Updates read-only indicator and line/column.
		// currentTextArea.requestFocusInWindow();

		current.addCaretListener(owner);

		// Trick the parent RText into updating the row/column indicator.
		// Null because caretUpdate doesn't actually use the caret event.
		owner.caretUpdate(null);

		// Let any listeners know that the current document changed.
		firePropertyChange(CURRENT_DOCUMENT_PROPERTY, -1, getSelectedIndex());
		fireCurrentTextAreaEvent(CurrentTextAreaEvent.TEXT_AREA_CHANGED,
										null, current);

	}


	/**
	 * Called when an internal frame is closed.
	 */
	public void internalFrameClosed(InternalFrameEvent e) {
	}


	/**
	 * Called when an internal frame is closing.
	 */
	public void internalFrameClosing(InternalFrameEvent e) {

		// We must ensure that the frame closing is the selected frame, because
		// the user can click the "x" button of an internal frame that isn't the
		// currently active frame.
		setSelectedIndex(frames.indexOf(e.getInternalFrame()));

		owner.getAction(RText.CLOSE_ACTION).actionPerformed(null);

	}


	/**
	 * Called when an internal frame is deactivated.
	 */
	public void internalFrameDeactivated(InternalFrameEvent e) {
		getCurrentTextArea().removeCaretListener(owner);
	}


	/**
	 * Called when an internal frame is de-iconified.
	 */
	public void internalFrameDeiconified(InternalFrameEvent e) {
	}


	/**
	 * Called when an internal frame is iconified.
	 */
	public void internalFrameIconified(InternalFrameEvent e) {
	}


	/**
	 * Called when an internal frame is opened.
	 */
	public void internalFrameOpened(InternalFrameEvent e) {
	}


	/**
	 * Repaints the display names for open documents.
	 */
	@Override
	public void refreshDisplayNames() {
		// Can't change MDI window color in JDesktopPane?
	}


	/**
	 * Removes a component from this container.  Note that this method does not
	 * update currentTextArea, you must do that yourself.
	 */
	@Override
	protected void removeComponentAt(int index) {
		if (index>=0 && index<getNumDocuments()) {
			((JInternalFrame)frames.get(index)).dispose();
			frames.remove(index);
			//tabbedPane.removeTabAt(index);
		}
	}


	/**
	 * Sets the name of the document displayed on the document's tab.
	 *
	 * @param index The index of the document whose name you want to change.
	 *        If this value is invalid, this method does nothing.
	 * @param displayName The name to display.
	 * @see #getDocumentDisplayNameAt
	 */
	@Override
	public void setDocumentDisplayNameAt(int index, String displayName) {
		if (index>=0 && index<getNumDocuments()) {
			((JInternalFrame)frames.get(index)).setTitle(displayName);
		}
	}


	/**
	 * Changes the location of the document selection area of this component.
	 *
	 * @param location The location to use; (<code>TOP</code>,
	 *        <code>LEFT</code>, <code>BOTTOM</code>, or <code>RIGHT</code>.
	 *        If this value is invalid, nothing happens.
	 */
	 @Override
	public void setDocumentSelectionPlacement(int location) {
		if (location==DOCUMENT_SELECT_TOP || location==DOCUMENT_SELECT_LEFT ||
			location==DOCUMENT_SELECT_BOTTOM || location==DOCUMENT_SELECT_RIGHT)
			documentSelectionPlacement = location;
	}


	/**
	 * Sets the currently active document.  This updates currentTextArea.
	 *
	 * @param index The index of the document to make the active document.  If this
	 *        value is invalid, nothing happens.
	 */
	@Override
	public void setSelectedIndex(int index) {
		if (index>=0 && index<getNumDocuments()) {
			JInternalFrame frame = frames.get(index);
			try {
				frame.setSelected(true); // Updates currentTextArea via internalFrameActivated.
			} catch (PropertyVetoException e) { }
			//frame.toFront();
			desktopPane.setSelectedFrame(frame);
		}
	}


	/**
	 * Tiles all open internal frames horizontally.
	 */
	public void tileWindowsHorizontally() {

		int numFrames = frames.size();
		int numToResize = 0;

		for (int i=0; i<numFrames; i++) {

			JInternalFrame frame = frames.get(i);

			if (frame.isVisible() && !frame.isIcon()) {

				// Make sure any frames that can't be resized don't
				// cover all our stuff up.
				if (!frame.isResizable()) {
					try {
						frame.setMaximum(false);
					} catch (PropertyVetoException e) { }
				}

				else
					numToResize++;

			} // End of if (frame.isVisible() && !frame.isIcon()).

		} // End of for (int i=0; i<numFrames; i++).

		if (numToResize>0) {

			Rectangle desktopBounds = desktopPane.getBounds();
			int desktopWidth = desktopBounds.width;
			int desktopHeight = desktopBounds.height;
			int fHeight = desktopHeight / numToResize;
			int yPos = 0;

			for (int i=0; i<numFrames; i++) {

				JInternalFrame frame = frames.get(i);

				if (frame.isVisible() && frame.isResizable() && !frame.isIcon()) {
					frame.setSize(desktopWidth, fHeight);
					frame.setLocation(0, yPos);
					yPos += fHeight;
				}

			} // End of for (int i=0; i<numFrames; i++).

		} // End of if (numToResize>0).

	}


	/**
	 * Tiles all open internal frames horizontally.
	 */
	public void tileWindowsVertically() {

		int numFrames = frames.size();
		int numToResize = 0;

		for (int i=0; i<numFrames; i++) {

			JInternalFrame frame = frames.get(i);

			if (frame.isVisible() && !frame.isIcon()) {

				// Make sure any frames that can't be resized don't
				// cover all our stuff up.
				if (!frame.isResizable()) {
					try {
						frame.setMaximum(false);
					} catch (PropertyVetoException e) { }
				}

				else
					numToResize++;

			} // End of if (frame.isVisible() && !frame.isIcon()).

		} // End of for (int i=0; i<numFrames; i++).

		if (numToResize>0) {

			Rectangle desktopBounds = desktopPane.getBounds();
			int desktopWidth = desktopBounds.width;
			int desktopHeight = desktopBounds.height;
			int fWidth = desktopWidth / numToResize;
			int xPos = 0;

			for (int i=0; i<numFrames; i++) {

				JInternalFrame frame = frames.get(i);

				if (frame.isVisible() && frame.isResizable() && !frame.isIcon()) {
					frame.setBounds(xPos,0, fWidth,desktopHeight);
					xPos += fWidth;
				}

			} // End of for (int i=0; i<numFrames; i++).

		} // End of if (numToResize>0).

	}


	/**
	 * Overridden so we can update the right-click popup menu.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		if (popupMenu!=null)
			SwingUtilities.updateComponentTreeUI(popupMenu);
	}


	/**
	 * The internal frames used.
	 */
	class InternalFrame extends JInternalFrame {

		private static final int xOffset = 30;
		private static final int yOffset = 30;

		public InternalFrame(String title, Component component) {
			super(title, true, true, true, true);
			RTextScrollPane sp = (RTextScrollPane)((JPanel)component).
													getComponent(0);
			this.setFrameIcon(getIconFor(sp));
			Container contentPane = getContentPane();
			contentPane.setLayout(new GridLayout(1,1));
			contentPane.add(component);
			++openFrameCount;
			if (openFrameCount>8)
				openFrameCount = 0;
			setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
			pack();
			setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
		}

	}


	/**
	 * Listens for mouse events in the desktop pane.
	 */
	class MDIMouseListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				if (popupMenu==null)
					createPopupMenu();
				popupMenu.show(desktopPane, e.getX(), e.getY());
			}
		}

	}


}