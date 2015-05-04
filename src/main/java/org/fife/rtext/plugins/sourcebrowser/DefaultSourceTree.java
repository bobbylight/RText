/*
 * 11/10/2010
 *
 * DefaultSourceTree.java - A tree that displays the structure of code by
 * parsing Exuberant ctags output.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.sourcebrowser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.fife.ctags.TagEntry;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.rtext.plugins.sourcebrowser.SourceBrowserPlugin.ExtendedTagEntry;
import org.fife.ui.RTreeSelectionModel;
import org.fife.ui.UIUtil;
import org.fife.ui.app.StandardAction;
import org.fife.ui.rtextarea.SearchEngine;


/**
 * The default source tree used by the source browser.<p>
 * This class has a lot of functionality copied from RSTALanguageSupport's
 * <code>AbstractSourceTree</code> class, but is unfortunately here because
 * we don't want this plugin to depend on that library.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DefaultSourceTree extends JTree {

	private SourceBrowserPlugin plugin;
	private RText owner;
	private Listener listener;
	private TreeCellRenderer treeRenderer;
	private DefaultTreeModel treeModel;
	private int mouseX;
	private int mouseY;
	private boolean ignoreTreeSelections;
	private boolean sorted;

	private Icon fileIcon;
	private String lineFoundText;
	private String cantFindLineText;


	public DefaultSourceTree(SourceBrowserPlugin plugin, RText owner) {

		this.plugin = plugin;
		this.owner = owner;
		listener = new Listener();

		setToggleClickCount(1);
		treeRenderer = SourceTreeCellRenderer.createTreeCellRenderer(this);
		setCellRenderer(treeRenderer);
		setSelectionModel(new RTreeSelectionModel());
		addTreeSelectionListener(listener);
		treeModel = new DefaultTreeModel(null);
		setModel(treeModel);
		ToolTipManager.sharedInstance().registerComponent(this);
		addMouseMotionListener(listener);
		addMouseListener(listener);

		ResourceBundle msg = plugin.getBundle();
		this.lineFoundText = msg.getString("StatusBarMsg.FoundLine");
		this.cantFindLineText = msg.getString("StatusBarMsg.CantFindLine");

	}


	/**
	 * Expands the appropriate tree nodes after the source has been re-parsed.
	 */
	private void expandInitialNodes() {
		UIUtil.expandAllNodes(this);
	}


	/**
	 * Returns the icon to use for "root" nodes of source trees (typically
	 * representing the type of source file).
	 *
	 * @return The icon to use.
	 * @see #setRootIcon(Icon)
	 */
	public Icon getRootIcon() {
		return fileIcon;
	}


	/**
	 * Returns the text being displayed for the specified row in the ctags
	 * tree.
	 *
	 * @param row The for for which to return text.
	 * @return The text.
	 */
	private String getTagTextForRow(int row) {
		TreePath path = getPathForRow(row);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.
											getLastPathComponent();
		return node.toString();
	}


	/**
	 * Returns whether the contents of this tree are sorted.
	 *
	 * @return Whether the contents of this tree are sorted.
	 * @see #setSorted(boolean)
	 */
	public boolean isSorted() {
		return sorted;
	}


	/**
	 * Decides whether or not to display the popup menu.
	 *
	 * @param e The mouse event to check.
	 */
	private void maybeShowPopup(MouseEvent e) {

		if (e.isPopupTrigger()) {

			JPopupMenu popup = new JPopupMenu();

			mouseX = e.getX();
			mouseY = e.getY();
			int row = getClosestRowForLocation(mouseX, mouseY);

			// The tree may not be visible (we're editing a plain text file)
			if (row>-1) {

				DefaultMutableTreeNode node =
					(DefaultMutableTreeNode)getPathForRow(row).
										getLastPathComponent();

				// If we're clicking on a node representing a tag...
				String tagText = node.toString();
				if (treeModel.isLeaf(node) &&
						tagText!=null &&
						tagText.indexOf("(0)")==-1 &&
						!tagText.startsWith("<")) {

					ignoreTreeSelections = true;
					// Don't bother selecting if it's a category
					setSelectionRow(row);
					ignoreTreeSelections = false;

					// Add an "Insert tag" menu item.
					ResourceBundle msg = plugin.getBundle();
					JMenuItem item = new JMenuItem(msg.getString("InsertTag") + tagText);
					item.setMnemonic(
							msg.getString("InsertTagMnemonic").charAt(0));
					item.setActionCommand("InsertAtCaret");
					item.addActionListener(listener);
					popup.add(item);

					// Add an "Jump to tag" menu item.
					item = new JMenuItem(msg.getString("JumpToTag") + tagText);
					item.setMnemonic(
							msg.getString("JumpToTagMnemonic").charAt(0));
					item.setActionCommand("JumpToTag");
					item.addActionListener(listener);
					popup.add(item);

				}

			}

			if (popup.getComponentCount()>0) {
				popup.addSeparator();
			}

			JMenuItem item = new JMenuItem(new ConfigureAction());
			popup.add(item);

			popup.show(this, mouseX, mouseY);

		}

	}


	/**
	 * Refreshes what children are visible in the tree.  This should be called
	 * manually when updating a source tree with a new root, and is also called
	 * internally on filtering and sorting.
	 */
	public void refresh() {
		Object root = getModel().getRoot();
		if (root instanceof SourceTreeNode) {
			SourceTreeNode node = (SourceTreeNode)root;
			node.setSorted(isSorted());
			node.refresh();
			((DefaultTreeModel)getModel()).reload();
			plugin.ensureSourceTreeSortedProperly();
			expandInitialNodes();
		}
	}


	/**
	 * Sets the root of this tree, and updates the expanded state of any nodes.
	 *
	 * @param newRoot The new root.
	 */
	void setRoot(SourceTreeNode newRoot) {
		treeModel.setRoot(newRoot);
		refresh();
	}


	/**
	 * Sets the icon to use for "root" nodes of source trees (typically
	 * representing the type of source file).
	 *
	 * @param icon The icon to use.
	 * @see #getRootIcon()
	 */
	public void setRootIcon(Icon icon) {
		fileIcon = icon;
	}


	/**
	 * Toggles whether the leaf nodes in this source tree are sorted.
	 *
	 * @param sorted Whether the tree nodes are sorted.
	 * @see #isSorted()
	 */
	public void setSorted(boolean sorted) {
		if (this.sorted!=sorted) {
			this.sorted = sorted;
			Object root = getModel().getRoot();
			if (root instanceof SourceTreeNode) {
				((SourceTreeNode)root).setSorted(sorted);
			}
			((DefaultTreeModel)getModel()).reload();
			expandInitialNodes();
		}
	}


	@Override
	public void updateUI() {
		super.updateUI();
		treeRenderer = SourceTreeCellRenderer.createTreeCellRenderer(this);
		setCellRenderer(treeRenderer); // So it picks up new LnF's colors??
	}


	/**
	 * Displays the options for this plugin in the Options dialog.
	 */
	private class ConfigureAction extends StandardAction {

		public ConfigureAction() {
			super(owner, plugin.getBundle(), "MenuItem.Configure");
		}

		public void actionPerformed(ActionEvent e) {
			plugin.showOptions();
		}

	}


	/**
	 * Listens for events in this tree.
	 */
	private class Listener extends MouseAdapter implements ActionListener,
						MouseMotionListener, TreeSelectionListener {

		/**
		 * Listens for actions in this component.
		 */
		public void actionPerformed(ActionEvent e) {

			String command = e.getActionCommand();
			int row = getClosestRowForLocation(mouseX, mouseY);

			// Go to the tag's location in the current file.
			if (command.equals("JumpToTag")) {
				setSelectionRow(row);
			}

			// Insert the tag at the current caret position.
			else if (command.equals("InsertAtCaret")) {
				RTextEditorPane editor = owner.getMainView().getCurrentTextArea();
				editor.replaceSelection(getTagTextForRow(row));
				editor.requestFocusInWindow();
			}

		}


		/**
		 * Called when the user clicks in the ctags tree.
		 */
		@Override
		public void mouseClicked(MouseEvent e) {

			// Double-clicking on "<ctags executable not configured" brings up
			// the options dialog.
			if (e.getClickCount()==2 && e.getButton()==MouseEvent.BUTTON1) {
				TreePath path = getSelectionPath();
				if (path!=null) {
					DefaultMutableTreeNode node =
						(DefaultMutableTreeNode)path.getLastPathComponent();
					String strVal = node.toString();
					if (strVal!=null && strVal.startsWith("<")) {
						plugin.showOptions();
						return;
					}
				}
			}

			maybeShowPopup(e);

		}


		/**
		 * Called when the mouse is dragged in the ctags tree.
		 */
		public void mouseDragged(MouseEvent e) {
			mouseMovedImpl(e);
		}


		/**
		 * Called when the mouse enters the ctags tree.
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
		}


		/**
		 * Called when the mouse exits the ctags tree.
		 */
		@Override
		public void mouseExited(MouseEvent e) {
		}


		/**
		 * Called when the mouse is moved in the ctags tree.
		 */
		public void mouseMoved(MouseEvent e) {
			mouseMovedImpl(e);
		}


		/**
		 * Called when the mouse moves/is dragged over the source browser tree.
		 * This method sets the tooltip displayed for the source browser tree.
		 */
		private void mouseMovedImpl(MouseEvent e) {

			// Get the item the mouse is pointing at.  It is possible that
			// They are pointing at no item; in that case, just quit now.
			int row = getRowForLocation(e.getX(), e.getY());
			if (row==-1)
				return;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
								getPathForRow(row).getLastPathComponent();
			if (node==null)
				return;
			Object object = node.getUserObject();
			String text = null;

			// If it's a high-level node (e.g. "Methods (4)").
			if (object instanceof String) {
				text = (String)object;
			}

			// If it's a low-level node (function, variable, etc.).  Note
			// that this should always be a TagEntry, but we're just being
			// safe by checking here.
			else if (object instanceof TagEntry) {
				ExtendedTagEntry entry = (ExtendedTagEntry)object;
				text = mouseMovedTagEntry(entry);
			}

			// Set the tooltip text.
			if (treeRenderer instanceof JComponent) {
				((JComponent)treeRenderer).setToolTipText(text);
			}

		}


		private String mouseMovedTagEntry(ExtendedTagEntry entry) {

			String text = null;

			// If we've already generated the (probably HTML) tool tip text,
			// use it.
			if (entry.cachedToolTipText!=null) {
				text = entry.cachedToolTipText;
			}

			// Create the tool tip text (HTML, or just text if only a line
			// number was found (such as #defines for C/C++).
			else {

				text = entry.getPlainTextPattern();

				// If we have a pattern, try to create an HTML tool tip.
				if (text!=null) {

					// To trim off the "regular expression" parts.
					text = text.substring(2, text.length()-2);

					if (plugin.getUseHTMLToolTips()) {
						// FIXME: Fix me to look line by line (as it's
						// guaranteed to be just a a line!).
						RTextEditorPane textArea = owner.getMainView().
													getCurrentTextArea();
						int pos = SearchEngine.getNextMatchPos(text,
								textArea.getText(), true, true, false);
						if (pos>-1) {
							try {
								int line = textArea.getLineOfOffset(pos);
								text = plugin.getHTMLForLine(line);
								entry.cachedToolTipText = text;
							} catch (BadLocationException ble) {
								owner.displayException(ble);
							}
						}
					}

				}

				// If all we have is a line number, use the tag's element's
				// name as the tooltip.
				else {
					text = entry.name.replaceAll("\t", " ");
				}

			}

			return text;

		}


		/**
		 * Called when the user downclicks in the ctags tree.
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}


		/**
		 * Called when the user up-clicks in the ctags tree.
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}


		/**
		 * Called when the user clicks on a tree node.  This method attempts to
		 * find the declaration of the selected item in the current text area, and
		 * if it is found (which it should be, unless it was removed, it is
		 * selected.
		 *
		 * @param e The tree selection event.
		 */
		public void valueChanged(TreeSelectionEvent e) {
			if (ignoreTreeSelections) {
				return;
			}
			TreePath path = getSelectionPath();
			// Must check for null, otherwise we get an exception on shutdown.
			if (path!=null) {
				Object object = ((DefaultMutableTreeNode)path.
									getLastPathComponent()).getUserObject();
				if (object instanceof TagEntry) {
					TagEntry entry = (TagEntry)object;
					RTextEditorPane editor=owner.getMainView().getCurrentTextArea();
					String pattern = entry.getPlainTextPattern();
					if (pattern!=null) {
						// FIXME: Fix me to look line by line (as it's
						// guaranteed to be just a a line!).
						int pos = SearchEngine.getNextMatchPos(pattern,
									editor.getText(), true, true, false);
						if (pos>-1) {
							editor.setCaretPosition(pos);
							editor.moveCaretPosition(pos+pattern.length());
							owner.setMessages(null, lineFoundText);
						}
						else {
							UIManager.getLookAndFeel().provideErrorFeedback(
														DefaultSourceTree.this);
							owner.setMessages(null, cantFindLineText);
						}

					}
					else { // Must use line number (used by C macros, for instance).
						Element map = editor.getDocument().getDefaultRootElement();
						Element line = map.getElement((int)entry.lineNumber-1);
						editor.setCaretPosition(line.getStartOffset());
						editor.moveCaretPosition(line.getEndOffset()-1);
					}
					editor.requestFocusInWindow();	// So we can see the highlighted line.
				}
			}
		}

	}


}