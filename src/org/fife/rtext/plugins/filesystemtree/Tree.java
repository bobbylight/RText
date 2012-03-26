package org.fife.rtext.plugins.filesystemtree;

import java.awt.Cursor;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.*;

import org.fife.rtext.*;
import org.fife.ui.app.StandardAction;
import org.fife.ui.rtextfilechooser.FileSystemTree;


/**
 * The extended file system tree used by this plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class Tree extends FileSystemTree {

	private FileSystemTreePlugin plugin;
	private OpenAction openAction;
	private OpenAction openInNewWindowAction;
	private GoIntoAction goIntoAction;
	private PropertiesAction propertiesAction;

	private static final String MSG =
			"org.fife.rtext.plugins.filesystemtree.PopupMenu";


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin.
	 */
	public Tree(FileSystemTreePlugin plugin) {

		this.plugin = plugin;
		Listener listener = new Listener();
		addMouseListener(listener);
		addPropertyChangeListener(listener);

		// Make the Enter key trigger opening a file on the JTree.
		InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap actionMap = getActionMap();
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OnEnter");
		actionMap.put("OnEnter", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				doOpenFile();
			}
		});

		// Add a needed extra bit of space at the top.
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(3, 0, 0, 0),
				getBorder()));

	}


	/**
	 * {@inheritDoc}
	 */
	protected void configurePopupMenuActions() {

		super.configurePopupMenuActions();
		File selected = getSelectedFile();

		boolean enable = selected!=null && selected.isFile();
		openAction.setEnabled(enable);
		openInNewWindowAction.setEnabled(enable);
		propertiesAction.setEnabled(enable);

		goIntoAction.setEnabled(selected!=null && selected.isDirectory());

	}


	/**
	 * Creates the popup menu for this file system tree.  Subclasses can
	 * override this method if they wish to add more menu items to the
	 * popup menu.
	 *
	 * @return The popup menu for this file system tree.
	 */
	protected JPopupMenu createPopupMenu() {

		JPopupMenu popup = super.createPopupMenu();

		ResourceBundle msg = ResourceBundle.getBundle(MSG);
		openAction = new OpenAction(msg.getString("Open"), false);
		popup.insert(new JMenuItem(openAction), 0);
		openInNewWindowAction = new OpenAction(
						msg.getString("OpenInNewWindow"), true);
		popup.insert(new JMenuItem(openInNewWindowAction), 1);

		goIntoAction = new GoIntoAction(plugin.getRText(), msg);
		popup.insert(new JMenuItem(goIntoAction), 4);
		popup.insert(new JPopupMenu.Separator(), 5);

		popup.addSeparator();
		propertiesAction = new PropertiesAction(msg.getString("Action.Properties"));
		popup.add(new JMenuItem(propertiesAction));

		// Re-do this to set orientation for new menu items.
		popup.applyComponentOrientation(getComponentOrientation());
		return popup;

	}


	/**
	 * If a file is selected in the file system tree, it is opened in RText.
	 */
	private void doOpenFile() {
		File file = getSelectedFile();
		if (file!=null) {
			// We'll make sure the file exists and is a regular file
			// (as opposed to a directory) before attempting to open it.
			if (file.isFile()) {
				AbstractMainView mainView = plugin.getRText().getMainView();
				// null encoding means check for Unicode first, and
				// if it isn't, use system default encoding.
				mainView.openFile(file.getAbsolutePath(), null);
			}
		}
	}


	/**
	 * Makes this tree drill down into the selected folder.
	 */
	private class GoIntoAction extends StandardAction {

		public GoIntoAction(RText app, ResourceBundle msg) {
			super(app, msg, "Action.GoInto");
		}

		public void actionPerformed(ActionEvent e) {
			File file = getSelectedFile();
			if (file!=null && file.isDirectory()) {
				plugin.goInto(file);
			}
			else { // Should never happen
				UIManager.getLookAndFeel().provideErrorFeedback(Tree.this);
			}
		}

	}


	/**
	 * Listens for events in this tree.
	 */
	private class Listener extends MouseAdapter implements
									PropertyChangeListener {

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount()==2) {
				doOpenFile();
			}
		}

		public void propertyChange(PropertyChangeEvent e) {
			String name = e.getPropertyName();
			if (name.equals(FileSystemTree.WILL_EXPAND_PROPERTY)) {
				plugin.getRText().setCursor(Cursor.
							getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
			else if (name.equals(FileSystemTree.EXPANDED_PROPERTY)) {
				plugin.getRText().setCursor(Cursor.
							getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}

	}


	/**
	 * Action that opens the currently selected file.
	 */
	private class OpenAction extends AbstractAction {

		private boolean newWindow;

		public OpenAction(String name, boolean newWindow) {
			putValue(NAME, name);
			this.newWindow = newWindow;
		}

		public void actionPerformed(ActionEvent e) {
			if (newWindow) {
				// Create a new RText window.
				RText r= new RText(null, (RTextPreferences)RTextPreferences.
						generatePreferences(plugin.getRText()));
				StoreKeeper.addRTextInstance(r);
				String file = getSelectedFileName();
				// Open the new RText's file chooser.  Do this in an
				// invokeLater() call as RText's constructor leaves some
				// stuff to do via invokeLater() as well, and we must wait
				// for this stuff to complete before we can continue (e.g.
				// RText's "working directory" must be set).
				SwingUtilities.invokeLater(new OpenInNewWindowRunnable(
										r, file));
			}
			else {
				doOpenFile();
			}
		}

	}


	/**
	 * Adds an old text file to an RText instance.
	 */
	private class OpenInNewWindowRunnable implements Runnable {

		private RText rtext;
		private String file;

		public OpenInNewWindowRunnable(RText rtext, String file) {
			this.rtext = rtext;
			this.file = file;
		}

		public void run() {
			AbstractMainView mainView = rtext.getMainView();
			mainView.openFile(file, null);
		}

	}


	/**
	 * Displays a dialog about the selected file.
	 */
	private class PropertiesAction extends AbstractAction {

		public PropertiesAction(String name) {
			putValue(NAME, name);
			int alt = InputEvent.ALT_MASK;
			putValue(ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, alt));
		}

		public void actionPerformed(ActionEvent e) {
			File file = getSelectedFile();
			if (file.isFile()) {
				// TODO: Extract class sharing code with TextFilePropertiesDialog
				// and display it!
			}
			else { // File was deleted from under us?
				UIManager.getLookAndFeel().provideErrorFeedback(Tree.this);
			}
		}

	}


}