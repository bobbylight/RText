/*
 * 03/19/2004
 *
 * RTextSplitPaneView.java - A view for multiple RTextEditorPanes consisting
 * of a left-hand list pane listing all open documents and a cardlayout pane
 * on the right showing the currently open document.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.fife.ui.RListSelectionModel;
import org.fife.ui.RScrollPane;
import org.fife.ui.UIUtil;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowScrollPane;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rtextarea.RTextScrollPane;


/**
 * A view for multiple RTextEditorPanes consisting of a left-hand list pane
 * listing all open documents and a CardLayout pane on the right showing the
 * currently open document.
 *
 * @author Robert Futrell
 * @version 0.1
 */
class RTextSplitPaneView extends AbstractMainView
									implements ListSelectionListener {

	private DockableWindow listWindow;
	private JList documentList;
	private RScrollPane documentListScrollPane;
	private DefaultListModel listModel;
	private CardLayout layout;

	private List<Component> scrollPanes;	// The scroll panes passed in.

	private int selectedIndex;
	private int listWindowPosition;	// Location of file list window

	private static final String IMAGE_FILE = "graphics/page_white_stack.png";


	/**
	 * Creates a new <code>RTextSplitPaneView</code>.
	 *
	 * @param owner The <code>RText</code> that this view sits in.
	 * @param filesToOpen Array of strings representing files to open.  If
	 *        this parameter is null, a single file with a default name is
	 *        is opened.
	 * @param prefs A properties object used to initialize some fields on
	 *        this view.
	 */
	public RTextSplitPaneView(RText owner, String[] filesToOpen,
										RTextPreferences prefs) {

		setLayout(new GridLayout(1,1));	// So the split pane takes up the whole panel.

		// Create the document list.
		documentList = new JList();
		documentList.setBorder(UIUtil.getEmpty5Border());
		documentList.setCellRenderer(new DocumentListCellRenderer());
		listModel = new DefaultListModel();
		documentList.setModel(listModel);
		documentList.setSelectionModel(new RListSelectionModel());
		documentList.addListSelectionListener(this);
		documentList.addMouseListener(new ListListener());
		documentListScrollPane = new DockableWindowScrollPane(documentList);

		String name = "Files";
		listWindow = new DockableWindow(name, new BorderLayout());
		listWindow.setPrimaryComponent(documentList);
		URL res = getClass().getResource(IMAGE_FILE);
		listWindow.setIcon(new ImageIcon(res));
		listWindow.add(documentListScrollPane);
		listWindow.setPosition(DockableWindow.LEFT);
		listWindow.setActive(true);
		owner.addDockableWindow(listWindow);

		// Create the view panel.
		layout = new CardLayout();
		setLayout(layout);

		// These are initialized when documents are added.
		selectedIndex = -1;
		scrollPanes = new ArrayList<Component>();

		// Add transfer handler to listen for files being drag-and-dropped
		// into this main view.
		TransferHandler th = new MainPanelTransferHandler(this);
		documentList.setTransferHandler(th);

		// Set everything up.
		initialize(owner, filesToOpen, prefs);

	}


	/**
	 * Adds a text area to this view, and places a number beside documents
	 * opened multiple times.
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

		int numDocuments = getNumDocuments();
		listModel.addElement(new DocumentInfo(
						title, getIconFor((RTextScrollPane)component)));
		add(temp, new Integer(numDocuments).toString());
		scrollPanes.add(component);
		setSelectedIndex(numDocuments);		// Sets currentTextArea.
		numDocuments++;					// We just added a document.

		// Loop through all tabs (documents) except the last (the one just added).
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

		} // End of for (int i=0; i<numDocuments-1; i++).

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

		// Remove the document from this container.
		removeComponentAt(getSelectedIndex());

		// If there are open documents, make sure any duplicates are numbered correctly.
		// If there are no open documents, add a new empty one.
		if (getNumDocuments()>0)
			renumberDisplayNames();
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

		// Return JOptionPane.YES_OPTION or JOptionPane.NO_OPTION.
		return true;

	}
	

	/**
	 * Overridden to remove our file list dockable window.
	 */
	@Override
	public void dispose() {
		owner.removeDockableWindow(listWindow);
	}


	/**
	 * Returns the name being displayed for the document.  For example, in a
	 * tabbed pane, this could be the text on the tab for this document.
	 *
	 * @param index The index at which to find the name.  If the index is
	 *        invalid, <code>null</code> is returned.
	 * @return The name being displayed for this document.
	 */
	@Override
	public String getDocumentDisplayNameAt(int index) {
		if (index>=0 && index<getNumDocuments()) {
			return ((DocumentInfo)listModel.get(index)).text;
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
		return listWindowPosition;
	}


	/**
	 * Returns the number of documents open in this container.
	 *
	 * @return The number of open documents.
	 */
	@Override
	public int getNumDocuments() {
		return scrollPanes.size();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public RTextScrollPane getRTextScrollPaneAt(int index) {
		if (index<0 || index>=getNumDocuments())
			//throw new IndexOutOfBoundsException();
			return null;
		return (RTextScrollPane)scrollPanes.get(index);
	}


	/**
	 * Returns the currently active component.
	 *
	 * @return The component.
	 */
	@Override
	public Component getSelectedComponent() {
		return scrollPanes.get(getSelectedIndex());
	}


	/**
	 * Returns the currently selected document's index.  Note that this value is
	 * 1 greater than the corresponding scrollPane in scrollPanes, since the
	 * root of the JTree is index 0.
	 *
	 * @return The index of the currently selected document.
	 */
	@Override
	public int getSelectedIndex() {
		return selectedIndex;
	}


	/**
	 * Repaints the display names for open documents.
	 */
	@Override
	public void refreshDisplayNames() {
		documentList.repaint();
	}


	/**
	 * Removes a component from this container.
	 * NOTE:  This is HORRIBLY inefficient, as we remove all components from the
	 *        CardLayout, then add back all of the ones that weren't removed.  This
	 *        is done because of the way we select components to view (indexing).
	 * PENDING:  Improve efficiency.
	 */
	@Override
	protected void removeComponentAt(final int index) {
		int numDocuments = getNumDocuments();
		if (index>=0 && index<numDocuments) {
			scrollPanes.remove(index);		// Remove text area from array list.
			numDocuments--;				// We just removed one.
			Component[] comps = getComponents();
			removeAll();			// Remove all documents and ad remaining ones back.
			for (int i=0; i<numDocuments; i++) {
				RTextScrollPane sp = (RTextScrollPane)scrollPanes.get(i);
				boolean reAdded = false;
				for (int j=0; j<comps.length; j++) {
					RTextScrollPane sp2 = (RTextScrollPane)((JPanel)comps[j]).getComponent(0);
					if (sp==sp2) {
						add(comps[j], new Integer(i).toString());
						reAdded = true;
						break;
					}
				}
				if (!reAdded) {
					new Exception("Scroll pane " + i + " not re-added!").printStackTrace();
				}
			}
			int oldSelectedIndex = getSelectedIndex();	// selectedIndex becomes -1 from the statement below.
			listModel.remove(index);					// Remove document name from list.
			if (oldSelectedIndex==numDocuments)
				setSelectedIndex(oldSelectedIndex-1);	// If they were looking at the "last" document...
			else
				setSelectedIndex(oldSelectedIndex);	// If they were looking at any other document.
		}
	}


	/**
	 * Sets the name of the document displayed on the document's tree listing.
	 *
	 * @param index The index of the document whose name you want to change.
	 *        If this value is invalid, this method does nothing.
	 * @param displayName The name to display.
	 * @see #getDocumentDisplayNameAt
	 */
	@Override
	public void setDocumentDisplayNameAt(int index, String displayName) {
		if (index>=0 && index<getNumDocuments()) {
			DocumentInfo info = (DocumentInfo)listModel.get(index);
			info.text = displayName;
			// May need to reset icon if extension has changed.
			info.icon = getIconFor(getRTextScrollPaneAt(index));
			documentList.repaint(); // Needed for renderer to repaint.
		}
	}


	/**
	 * Changes the location of the document selection area of this component.
	 *
	 * @param location The location to use (<code>TOP</code>,
	 *        <code>LEFT</code>, <code>BOTTOM</code>, or <code>RIGHT</code>).
	 *        If this value is invalid, nothing happens.
	 */
	 @Override
	public void setDocumentSelectionPlacement(int location) {

		if (location==DOCUMENT_SELECT_TOP || location==DOCUMENT_SELECT_LEFT ||
			location==DOCUMENT_SELECT_BOTTOM || location==DOCUMENT_SELECT_RIGHT) {

			if (location!=listWindowPosition) {

				listWindowPosition = location;
				int pos = 0;
				switch (listWindowPosition) {

					case DOCUMENT_SELECT_TOP:
						pos = DockableWindow.TOP;
						break;

					case DOCUMENT_SELECT_BOTTOM:
						pos = DockableWindow.BOTTOM;
						break;

					case DOCUMENT_SELECT_LEFT:
						pos = DockableWindow.LEFT;
						break;

					case DOCUMENT_SELECT_RIGHT:
						pos = DockableWindow.RIGHT;
						break;

				}

				listWindow.setPosition(pos);
//				owner.pack();

			}

		}
	}


	/**
	 * Sets the currently active document.  This updates currentTextArea.
	 *
	 * @param index The index of the document to make the active document.  If
	 *        this value is invalid, nothing happens.
	 */
	@Override
	public void setSelectedIndex(int index) {
		// All we need to do is call the valueChanged() method below; it takes
		// care of everything for us.
		if (index>=0 && index<scrollPanes.size())
			documentList.setSelectedIndex(index);
	}


	/**
	 * Listens for the user to select values in the list.
	 */
	public void valueChanged(ListSelectionEvent e) {

		// Remove old listeners.
		RTextEditorPane current = getCurrentTextArea();
		if (current!=null) {
			current.removeCaretListener(owner);
		}

		// We must check for this in case they removed the last document.
		selectedIndex = documentList.getSelectedIndex();
		if (selectedIndex!=-1) {

			String key = new Integer(selectedIndex).toString();
			layout.show(this, key);
			current = getRTextEditorPaneAt(selectedIndex);
			setCurrentTextArea(current);

			if (current.isDirty())
				owner.setMessages(current.getFileFullPath() + "*", null);
			else
				owner.setMessages(current.getFileFullPath(), null);
			updateStatusBar();	// Updates read-only indicator and line/column.

			// Add back listeners.
			current.addCaretListener(owner);

			// Trick the parent RText into updating the row/column indicator.
			// We have to check mainView for null because this is called in
			// RText's constructor, before RText has a mainView.
			if (owner.getMainView()!=null)
				owner.caretUpdate(null); // Null because caretUpdate doesn't actually use the caret event.

			current.requestFocusInWindow();

			// Let any listeners know that the current document changed.
			firePropertyChange(CURRENT_DOCUMENT_PROPERTY, -1, selectedIndex);
			fireCurrentTextAreaEvent(CurrentTextAreaEvent.TEXT_AREA_CHANGED,
								null, current);

		}

	}


	/**
	 * Wrapper class for the text and icon displayed in the split pane.
	 */
	private static class DocumentInfo {

		public String text;
		public Icon icon;

		public DocumentInfo(String text, Icon icon) {
			this.text = text;
			this.icon = icon;
		}

	}


	/**
	 * Renders components in the list of open documents.
	 */
	private class DocumentListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list,
							Object value, int index,
							boolean isSelected, boolean cellHasFocus) {

			setComponentOrientation(list.getComponentOrientation());

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else {

				setBackground(list.getBackground());

				RTextEditorPane textArea = getRTextEditorPaneAt(index);
				if (textArea==null) // Happens in JRE 1.5.0, not in 1.4.x...
					setForeground(list.getForeground());
				else if (textArea.isDirty()==true && highlightModifiedDocumentDisplayNames())
					setForeground(getModifiedDocumentDisplayNamesColor());
				else
					setForeground(list.getForeground());
			}

			DocumentInfo info = (DocumentInfo)value;
			if (info!=null) {
				setText(info.text);
				setIcon(info.icon);
			}
			else {
				setText("");
			}

			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setBorder((cellHasFocus) ?
					UIManager.getBorder("List.focusCellHighlightBorder") :
					noFocusBorder);

			return this;

		}

	}


	/**
	 * Listens for mouse events in the document list.
	 */
	private class ListListener extends MouseAdapter {

		private JMenuItem createItem(String actionName) {
			JMenuItem item = new JMenuItem(owner.getAction(actionName));
			item.setToolTipText(null);
			return item;
		}

		private JPopupMenu createPopupMenu() {
			JPopupMenu popup = new JPopupMenu();
			popup.add(createItem(RText.SAVE_ACTION));
			popup.add(createItem(RText.SAVE_AS_ACTION));
			popup.add(createItem(RText.CLOSE_ACTION));
			popup.addSeparator();
			popup.add(createItem(RText.FILE_PROPERTIES_ACTION));
			return popup;
		}

		private void handleMouseEvent(MouseEvent e) {

			if (e.isPopupTrigger()) {

				Point p = e.getPoint();
				int index = documentList.locationToIndex(p);
				if (index>-1) { // Should always be true
					// Ensure the mouse is actually inside of the item
					Rectangle r = documentList.getCellBounds(index, index);
					if (r.contains(p)) {
						documentList.setSelectedIndex(index);
						JPopupMenu popup = createPopupMenu();
						popup.show(documentList, p.x, p.y);
					}
				}
			}

		}

		@Override
		public void mouseClicked(MouseEvent e) {
			handleMouseEvent(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			handleMouseEvent(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			handleMouseEvent(e);
		}

	}


}