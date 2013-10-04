/*
 * 11/14/2003
 *
 * RTextTabbedPaneView.java - A JTabbedPane containing multiple
 * text documents.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Stroke;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.TabbedPaneUI;

import org.fife.ui.DrawDnDIndicatorTabbedPane;
import org.fife.ui.rtextarea.RTextScrollPane;


/**
 * An extension of <code>JTabbedPane</code> that contains documents being
 * edited by rtext.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class RTextTabbedPaneView extends AbstractMainView implements ChangeListener {

	private TabbedPane tabbedPane;
	private boolean inCloseCurrentDocument;
	private int rightClickIndex;

	private final TabbedPaneViewTransferHandler transferHandler =
								new TabbedPaneViewTransferHandler(this);

	private static final String POPUP_MSG = "org.fife.rtext.TabbedPaneViewPopupMenu";


	/**
	 * Creates a new <code>RTextTabbedPaneView</code>.
	 *
	 * @param owner The <code>RText</code> that this tabbed pane sits in.
	 * @param filesToOpen Array of strings representing files to open.  If
	 *        this parameter is null, a single file with a default name is
	 *        opened.
	 * @param properties A properties object used to initialize some fields on
	 *        this tabbed pane.
	 */
	public RTextTabbedPaneView(RText owner, String[] filesToOpen,
							RTextPreferences properties) {

		setLayout(new GridLayout(1,1));
		tabbedPane = new TabbedPane();
		tabbedPane.addChangeListener(this);
		add(tabbedPane);

		// Set everything up.
		initialize(owner, filesToOpen, properties);

		// Set up our drag-and-drop handler.
		tabbedPane.setTransferHandler(transferHandler);
		try {
			tabbedPane.getDropTarget().addDropTargetListener(transferHandler);
		} catch (java.util.TooManyListenersException tmle) {
			owner.displayException(tmle);
		}
		TabDragListener tdl = new TabDragListener();
		tabbedPane.addMouseListener(tdl);
		tabbedPane.addMouseMotionListener(tdl);

		rightClickIndex = -1;

	}


	/**
	 * Adds a change listener.
	 *
	 * @param listener The change listener.
	 */
	public void addChangeListener(ChangeListener listener) {
		tabbedPane.addChangeListener(listener);
	}


	/**
	 * Adds a container listener.
	 *
	 * @param listener The container listener.
	 */
	@Override
	public void addContainerListener(ContainerListener listener) {
		tabbedPane.addContainerListener(listener);
	}


	/**
	 * Adds a tab to the tabbed pane, and places a number beside documents
	 * opened multiple times.
	 *
	 * @param title The "display name" to use on the tab of the document.
	 * @param component The scroll pane containing the text editor to add.
	 * @param fileFullPath The path to the file this editor contains.
	 */
	@Override
	protected void addTextAreaImpl(String title, Component component,
							String fileFullPath) {

		// "Physically" add the tab.
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(component);
		RTextScrollPane sp = (RTextScrollPane)component;
		RTextEditorPane textArea = (RTextEditorPane)sp.getTextArea();
		temp.add(createErrorStrip(textArea), BorderLayout.LINE_END);
		tabbedPane.addTab(title, getIconFor(sp), temp);

		// Loop through all tabs (documents) except the last (the one just added).
		int tabCount = getNumDocuments();
		for (int i=0; i<tabCount-1; i++) {

			// If any of them is the same physical file as the just added one, do the numbering.
			if (getRTextEditorPaneAt(i).getFileFullPath().equals(fileFullPath)) {
				int count = 0;
				for (int j=i; j<tabCount; j++) {
					RTextEditorPane pane = getRTextEditorPaneAt(j);
					if (pane.getFileFullPath().equals(fileFullPath)) {
						String newTitle = title + " (" + (++count) + ")";
						if (pane.isDirty())
							newTitle = newTitle + "*";
						try {
							setDocumentDisplayNameAt(j, newTitle);
						} catch (Exception e) {
							owner.displayException(e);
						}
					}
				}
				break;
			}

		}

		// Do any extra stuff.
		// This updates currentTextArea and shifts focus too.
		setSelectedIndex(tabCount-1);
		if (getCurrentTextArea().isDirty())
			owner.setMessages(fileFullPath + "*", "Opened document '" + fileFullPath + "'");
		else
			owner.setMessages(fileFullPath, "Opened document '" + fileFullPath + "'");
		
		// RText's listeners will be updated by stateChanged() for all
		// addTextAreaImpl() calls.
		
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

inCloseCurrentDocument = true;
		RTextEditorPane oldTextArea = getCurrentTextArea();

		// Remove listeners from current text area IFF stateChanged() won't do it
		// (i.e., we're removing any document except the "rightmost" document).
//		boolean removingLastDocument = (getSelectedIndex()==getNumDocuments()-1);
//		if (removingLastDocument==false) {
			oldTextArea.removeCaretListener(owner);
//		}

		// Remove the document from this tabbed pane.
		removeComponentAt(getSelectedIndex());

		// If there are open documents, make sure any duplicates are numbered
		// correctly.  If there are no open documents, add a new empty one.
		if (getNumDocuments()>0)
			renumberDisplayNames();
		else
			addNewEmptyUntitledFile();

		// Request focus in the window for the new currentTextArea.
		// Note that this may also be done by stateChanged() but we need to
		// do it here too because code below relies on currentTextArea being
		// up-to-date.
		oldTextArea = getCurrentTextArea();
		setCurrentTextArea(getRTextEditorPaneAt(getSelectedIndex()));
		final RTextEditorPane currentTextArea = getCurrentTextArea();
		// MUST be done by SwingUtilities.invokeLater(), I think because
		// currentTextArea is not yet visible on this line of code, so
		// calling requestFocusInWindow() now would do nothing.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// NOTE: This null check here is silly, but we have to do
				// it because: Before this runnable runs, we are sure that
				// at least 1 document is open in RText.  However, when this
				// Runnable runs, there is a case where there are no
				// documents open: If one empty untitled document is open,
				// and the user tries to open a file from the file history
				// that no longer exists.  In that case the current document
				// will have been closed to be replaced with the new one,
				// but the "does not exist, create it?" dialog pops up
				// while no documents are in the tabbed pane, which causes
				// currentTextArea to be null here.
				if (currentTextArea!=null) {
					currentTextArea.requestFocusInWindow();
				}
			}
		});

		// Update RText's listeners IFF the active document number doesn't
		// change (i.e., they closed any document except the "rightmost" one).
		// Closing the "rightmost" document means stateChanged() will handle
		// the listeners.
//		if (removingLastDocument==false) {
			currentTextArea.addCaretListener(owner);
//		}

inCloseCurrentDocument = false;

		// Let any listeners know that the current document changed.
		firePropertyChange(CURRENT_DOCUMENT_PROPERTY, -1, getSelectedIndex());
		fireCurrentTextAreaEvent(CurrentTextAreaEvent.TEXT_AREA_CHANGED,
							oldTextArea, currentTextArea);

		// Update the RText's status bar.
		updateStatusBar();

		// Update RText's title and the status bar message.
		if (currentTextArea.isDirty())
			owner.setMessages(currentTextArea.getFileFullPath() + "*", msg.getString("Ready"));
		else
			owner.setMessages(currentTextArea.getFileFullPath(), msg.getString("Ready"));

		return true;

	}


	/**
	 * Returns the name being displayed for the specified document.
	 *
	 * @param index The index at which to find the name.  If the index is
	 *        invalid, <code>null</code> is returned.
	 * @return The name being displayed for this document.
	 */
	@Override
	public String getDocumentDisplayNameAt(int index) {
		if (index>=0 && index<tabbedPane.getTabCount()) {
			return tabbedPane.getTitleAt(index);
		}
		return null;
	}


	/**
	 * Returns the location of the document selection area of this component.
	 *
	 * @return The location of the document selection area.
	 */
	@Override
	public int getDocumentSelectionPlacement() {
		return tabbedPane.getTabPlacement();
	}


	/**
	 * Returns the number of documents open in this container.
	 *
	 * @return The number of open documents.
	 */
	@Override
	public int getNumDocuments() {
		return tabbedPane.getTabCount();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public RTextScrollPane getRTextScrollPaneAt(int index) {
		if (index<0 || index>=getNumDocuments())
			//throw new IndexOutOfBoundsException();
			return null;
		JPanel temp = (JPanel)tabbedPane.getComponentAt(index);
		return (RTextScrollPane)temp.getComponent(0);
	}


	/**
	 * Returns the currently active component.
	 *
	 * @return The component.
	 */
	@Override
	public Component getSelectedComponent() {
		return tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
	}


	/**
	 * Returns the currently selected document's index.
	 *
	 * @return The index of the currently selected document.
	 */
	@Override
	public int getSelectedIndex() {
		return tabbedPane.getSelectedIndex();
	}


	/**
	 * Repaints the display names for open documents.
	 */
	@Override
	public void refreshDisplayNames() {

		Color defaultForeground = UIManager.getColor("tabbedpane.foreground");
		Color modifiedColor = getModifiedDocumentDisplayNamesColor();
		int numDocuments = getNumDocuments();

		if (highlightModifiedDocumentDisplayNames()==true) {
			for (int i=0; i<numDocuments; i++) {
				if (getRTextEditorPaneAt(i).isDirty()==true) {
					tabbedPane.setForegroundAt(i, modifiedColor);
				}
				else {
					tabbedPane.setForegroundAt(i, defaultForeground);
				}
			}
		}
		else {
			for (int i=0; i<numDocuments; i++)
				tabbedPane.setForegroundAt(i, defaultForeground);
		}

	}


	/**
	 * Removes a change listener from this tabbed pane.
	 *
	 * @param listener The listener to remove.
	 */
	public void removeChangeListener(ChangeListener listener) {
		tabbedPane.removeChangeListener(listener);
	}


	/**
	 * Removes a component from this container.  Note that this method does
	 * not update currentTextArea, you must do that yourself.
	 */
	@Override
	protected void removeComponentAt(int index) {
		if (index>=0 && index<getNumDocuments()) {
			tabbedPane.removeTabAt(index);
			//currentTextArea = getRTextEditorPaneAt(getSelectedIndex());
		}
	}


	/**
	 * Removes a container listener from this tabbed pane.
	 *
	 * @param listener The listener to remove.
	 */
	@Override
	public void removeContainerListener(ContainerListener listener) {
		tabbedPane.removeContainerListener(listener);
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
			tabbedPane.setTitleAt(index, displayName);
			// Hack-of-a-way to tell if this document is modified.
			if (displayName.charAt(displayName.length()-1)=='*') {
				if (highlightModifiedDocumentDisplayNames()==true) {
					tabbedPane.setForegroundAt(index,
							getModifiedDocumentDisplayNamesColor());
				}
				else {
					tabbedPane.setForegroundAt(index,
										tabbedPane.getForeground());
				}
			}
			// Just set it to regular color (this may/may not be unnecessary...).
			else {
				tabbedPane.setForegroundAt(index, tabbedPane.getForeground());
			}
		}
		// May need to reset icon if extension has changed.
		tabbedPane.setIconAt(index, getIconFor(getRTextScrollPaneAt(index)));
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
			tabbedPane.setTabPlacement(location);
	}


	/**
	 * Sets the currently active document.  This updates currentTextArea.
	 *
	 * @param index The index of the document to make the active document.
	 *        If this value is invalid, nothing happens.
	 */
	@Override
	public void setSelectedIndex(int index) {
		if (index>=0 && index<getNumDocuments()) {
			tabbedPane.setSelectedIndex(index);
			setCurrentTextArea(getRTextEditorPaneAt(index));
			updateStatusBar();
			getCurrentTextArea().requestFocusInWindow();
		}
	}


	public void stateChanged(ChangeEvent e) {

		// Skip this stuff if we're called from closeCurrentDocument(), as
		// that method does this stuff to; let's not do it twice (listeners
		// would get two CURRENT_DOCUMENT_PROPERTY events).
		if (inCloseCurrentDocument)
			return;

		// TODO: Factor the code below and the similar code in
		// closeCurrentDocument() into a common method.

		// Remove the RText listeners associated with the current
		// currentTextArea.
		RTextEditorPane oldTextArea = getCurrentTextArea();
		if (oldTextArea!=null) {
			oldTextArea.removeCaretListener(owner);
		}

		// The new currentTextArea will only be null when we're closing the
		// only open document.  Even then, after this a new document will be
		// opened and this method will be re-called.
		setCurrentTextArea(getRTextEditorPaneAt(getSelectedIndex()));
		final RTextEditorPane currentTextArea = getCurrentTextArea();
		if (currentTextArea!=null) {

			if (currentTextArea.isDirty())
				owner.setMessages(currentTextArea.getFileFullPath() + "*", null);
			else
				owner.setMessages(currentTextArea.getFileFullPath(), null);
			updateStatusBar(); // Update read-only and line/col. indicators.

			// Give the current text area focus.  We have to do this in
			// a Runnable as during this stateChanged() call, the text area's
			// panel hasn't actually been made visible yet, and that must
			// have happened for requestFocusInWindow to work.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					currentTextArea.requestFocusInWindow();
				}
			});

			// Update RText actions associated with the current currentTextArea.
			currentTextArea.addCaretListener(owner);
			
			// Trick the parent RText into updating the row/column indicator.
			// We have to check mainView for null because this is called in
			// RText's constructor, before RText has a mainView.
			if (owner.getMainView()!=null)
				// Null because caretUpdate doesn't actually use the caret event.
				owner.caretUpdate(null);

			// Let any listeners know that the current document changed.
			firePropertyChange(CURRENT_DOCUMENT_PROPERTY, -1, getSelectedIndex());
			fireCurrentTextAreaEvent(CurrentTextAreaEvent.TEXT_AREA_CHANGED,
								null, currentTextArea);

		}

	}


	/**
	 * The actual tabbed pane.
	 */
	class TabbedPane extends JTabbedPane
							implements DrawDnDIndicatorTabbedPane {

		private int x,y, width,height;
		private Stroke wideStroke;
		private JPopupMenu popup;
		private TabbedPaneCloseAction closeAction;

		public TabbedPane() {
			x = y = -1;
			enableEvents(AWTEvent.MOUSE_EVENT_MASK);
			ToolTipManager.sharedInstance().registerComponent(this);
		}

		public void clearDnDIndicatorRect() {
			x = y = -1;
			repaint();
		}

		protected JPopupMenu getTabPopupMenu() {
			if (popup==null) {
				ResourceBundle msg = ResourceBundle.getBundle(POPUP_MSG);
				popup = new JPopupMenu();
				String title = msg.getString("Close");
				closeAction = new TabbedPaneCloseAction(title);
				JMenuItem item = new JMenuItem(closeAction);
				popup.add(item);
				title = msg.getString("CloseOthers");
				item = new JMenuItem(
							new TabbedPaneCloseOthersAction(title));
				popup.add(item);
				item = new JMenuItem(owner.getAction(RText.CLOSE_ALL_ACTION));
				item.setToolTipText(null);
				popup.add(item);
				popup.add(new JPopupMenu.Separator());
				title = msg.getString("CopyPathToClipboard");
				item = new JMenuItem(
					new TabbedPaneCopyPathAction(title));
				popup.add(item);
			}
			return popup;
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			TabbedPaneUI ui = getUI();
			if (ui != null) {
				int index = ui.tabForCoordinate(this, e.getX(), e.getY());
				if (index!=-1) {
					return getRTextEditorPaneAt(index).getFileFullPath();
				}
			}
			return super.getToolTipText(e);
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			if (x!=-1) {
				// Since this must be the EDT we can lazily create.
				if (wideStroke==null)
					wideStroke = new BasicStroke(2.0f);
				Graphics2D g2d = (Graphics2D)g;
				Stroke temp = g2d.getStroke();
				g2d.setStroke(wideStroke);
				g2d.setColor(SystemColor.controlDkShadow);
				g2d.drawRect(x,y, width,height);
				g2d.setStroke(temp);
			}
		}

		@Override
		protected void processMouseEvent(MouseEvent e) {
			// NOTE: We don't allow RMB clicks that aren't popup triggers
			// to go into super.processMouseEvent() in the off-chance that
			// a popup trigger is mouse release.  By default, tabbed panes
			// would then use the mouse "press" of the RMB to select the
			// armed tab, which we don't want.
			if (SwingUtilities.isRightMouseButton(e)) {
				if (e.isPopupTrigger()) {
					int x = e.getX();
					int y = e.getY();
					int index = indexAtLocation(x, y);
					if (index!=-1) {
						rightClickIndex = index;
						JPopupMenu popup = getTabPopupMenu();
						String name = RTextTabbedPaneView.this.
									getDocumentDisplayNameAt(index);
						closeAction.setDocumentName(name);
						popup.show(this, x, y);
					}
				}
			}
			else if (SwingUtilities.isMiddleMouseButton(e)) {
				if (e.getID()==MouseEvent.MOUSE_CLICKED &&
						e.getClickCount()==1 && !e.isPopupTrigger()) {
					int x = e.getX();
					int y = e.getY();
					int index = indexAtLocation(x, y);
					if (index!=-1) {
						RTextTabbedPaneView.this.setSelectedIndex(index);
						RTextTabbedPaneView.this.closeCurrentDocument();
					}
				}
			}
			else {
				super.processMouseEvent(e);
			}
		}

		public void setDnDIndicatorRect(int x, int y, int width, int height) {
			this.x = x; this.y = y;
			this.width = width; this.height = height;
			repaint();
		}

		@Override
		public void updateUI() {
			super.updateUI();
			if (popup!=null) {
				SwingUtilities.updateComponentTreeUI(popup);
			}
		}

	}


	/**
	 * Action that closes the tab last right-clicked (e.g. the popup menu
	 * was displayed for it) in the tabbed pane.
	 */
	private class TabbedPaneCloseAction extends AbstractAction {

		private String template;

		public TabbedPaneCloseAction(String template) {
			this.template = template;
		}

		public void actionPerformed(ActionEvent e) {
			if (rightClickIndex>-1) {
				RTextTabbedPaneView.this.setSelectedIndex(rightClickIndex);
				RTextTabbedPaneView.this.closeCurrentDocument();
			}
		}

		public void setDocumentName(String name) {
			String text = MessageFormat.format(template,
										new Object[] { name });
			putValue(AbstractAction.NAME, text);
		}

	}


	/**
	 * Action that closes all tabs except the one last right-clicked (e.g.
	 * the popup menu was displayed for it) in the tabbed pane.
	 */
	private class TabbedPaneCloseOthersAction extends AbstractAction {

		public TabbedPaneCloseOthersAction(String text) {
			putValue(AbstractAction.NAME, text);
		}

		public void actionPerformed(ActionEvent e) {
			if (rightClickIndex>-1) {
				RTextTabbedPaneView.this.closeAllDocumentsExcept(
												rightClickIndex);
			}
		}

	}


	/**
	 * Action that copies the full path to the right-clicked tab to the
	 * clipboard.
	 */
	private class TabbedPaneCopyPathAction extends AbstractAction {

		public TabbedPaneCopyPathAction(String text) {
			putValue(AbstractAction.NAME, text);
		}

		public void actionPerformed(ActionEvent e) {
			if (rightClickIndex>-1) {
				RTextEditorPane textArea = RTextTabbedPaneView.this.
								getRTextEditorPaneAt(rightClickIndex);
				String path = textArea.getFileFullPath();
				Clipboard c = Toolkit.getDefaultToolkit().
											getSystemClipboard();
				c.setContents(new StringSelection(path), null);
			}
		}

	}


	/**
	 * Listens for the user drag-and-dropping tabs in this tabbed
	 * pane.
	 */
	private class TabDragListener extends MouseInputAdapter {

		private int tab;
		private JComponent draggedTab;
		MouseEvent firstMouseEvent;

		@Override
		public void mouseDragged(MouseEvent e) {
			if (draggedTab==null)
				return;
			if (firstMouseEvent != null) {
				e.consume();
				int action = javax.swing.TransferHandler.MOVE;
				int dx = Math.abs(e.getX() - firstMouseEvent.getX());
				int dy = Math.abs(e.getY() - firstMouseEvent.getY());
				//Arbitrarily define a 5-pixel shift as the
				//official beginning of a drag.
				if (dx > 5 || dy > 5) {
					//This is a drag, not a click.
					//Tell the transfer handler to initiate the drag.
					transferHandler.exportAsDrag(tabbedPane,
											firstMouseEvent, action);
					firstMouseEvent = null;
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			tab = tabbedPane.indexAtLocation(e.getX(), e.getY());
			if (tab>-1) {
				draggedTab = (JComponent)tabbedPane.getComponentAt(tab);
				firstMouseEvent = e;
				e.consume();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			draggedTab = null;
			firstMouseEvent = null;
		}

	}


}