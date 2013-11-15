/*
 * 06/13/2005
 *
 * FileSystemTreePlugin.java - A plugin that displays a tree of files on the
 * local filesystem, allowing for easy opening of files.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.filesystemtree;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.fife.rtext.*;
import org.fife.ui.RScrollPane;
import org.fife.ui.WebLookAndFeelUtils;
import org.fife.ui.app.*;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowScrollPane;


/**
 * A panel displaying all files on the local file system, allowing for quick
 * and easy opening of files without opening the file chooser.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FileSystemTreePlugin extends GUIPlugin {

	private RText owner;
	private String name;
	private Tree tree;
	private FileSystemTreeOptionPanel optionPanel;
	private Icon pluginIcon;
	private ViewAction viewAction;
	private JToolBar dockableWindowTB;

	private JLabel dirLabel;
	private BackAction backAction;
	private ForwardAction forwardAction;
	private List<File> rootHistory;
	private int rootHistoryOffs;

	static final String BUNDLE_NAME			=
					"org/fife/rtext/plugins/filesystemtree/FileSystemTree";
	private static final String VERSION_STRING	= "2.5.2";

	private static final String VIEW_FST_ACTION	= "ViewFileSystemTreeAction";


	/**
	 * Creates a new <code>FileSystemTreePlugin</code>.
	 *
	 * @param app The RText instance.
	 */
	public FileSystemTreePlugin(AbstractPluggableGUIApplication app) {

		this.owner = (RText)app;

		URL url = this.getClass().getResource("filesystemtree.gif");
		if (url!=null)
			pluginIcon = new ImageIcon(url);

		ResourceBundle msg = ResourceBundle.getBundle(BUNDLE_NAME);
		this.name = msg.getString("Name");

		FileSystemTreePrefs prefs = loadPrefs();
		viewAction = new ViewAction(owner, msg);
		viewAction.setAccelerator(prefs.windowVisibilityAccelerator);

		DockableWindow wind = createDockableWindow(prefs);
		putDockableWindow(name, wind);

		rootHistory = new ArrayList<File>();
		rootHistory.add(null);
		rootHistoryOffs = 0;

	}


	/**
	 * Creates the single dockable window used by this plugin.
	 *
	 * @param prefs Preferences for this plugin.
	 * @return The dockable window.
	 */
	private DockableWindow createDockableWindow(FileSystemTreePrefs prefs) {

		DockableWindow wind = new DockableWindow(name, new BorderLayout()) {
			@Override
			public void updateUI() {
				super.updateUI();
				if (dockableWindowTB!=null) {
					WebLookAndFeelUtils.fixToolbar(dockableWindowTB);
				}
			}
		};

		dockableWindowTB = new JToolBar();
		dockableWindowTB.setFloatable(false);
		wind.add(dockableWindowTB, BorderLayout.NORTH);

		ResourceBundle msg = ResourceBundle.getBundle(BUNDLE_NAME);
		backAction = new BackAction(getRText(), msg);
		forwardAction = new ForwardAction(getRText(), msg);

//		tb.add(Box.createHorizontalStrut(3));
		dirLabel = new JLabel();
		// Allow label to be resized very small so it doesn't hog space
		dirLabel.setMinimumSize(new Dimension(8, 8));
		dockableWindowTB.add(dirLabel);
		dockableWindowTB.setMinimumSize(new Dimension(8, 8)); // ditto
		dockableWindowTB.setBorder(new BottomLineBorder(3));

		dockableWindowTB.add(Box.createHorizontalGlue());
		JButton b = new JButton(backAction);
		dockableWindowTB.add(b);
		b = new JButton(forwardAction);
		dockableWindowTB.add(b);
		WebLookAndFeelUtils.fixToolbar(dockableWindowTB);
		
		tree = new Tree(this);
		RTextUtilities.removeTabbedPaneFocusTraversalKeyBindings(tree);
		RScrollPane scrollPane = new DockableWindowScrollPane(tree);
		RTextUtilities.removeTabbedPaneFocusTraversalKeyBindings(scrollPane);
		wind.add(scrollPane);
		wind.setPrimaryComponent(tree);

		wind.setActive(prefs.active);
		wind.setPosition(prefs.position);
		wind.setIcon(getPluginIcon());

		ComponentOrientation o = ComponentOrientation.
									getOrientation(Locale.getDefault());
		wind.applyComponentOrientation(o);

		return wind;

	}


	/**
	 * Returns the options panel for this source browser.
	 *
	 * @return The options panel.
	 */
	public synchronized PluginOptionsDialogPanel getOptionsDialogPanel() {
		if (optionPanel==null) {
			optionPanel = new FileSystemTreeOptionPanel(owner, this);
		}
		return optionPanel;
	}


	/**
	 * Returns the author of the plugin.
	 *
	 * @return The plugin's author.
	 */
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	/**
	 * Returns the icon for this plugin.
	 *
	 * @return The icon for this plugin.
	 */
	public Icon getPluginIcon() {
		return pluginIcon;
	}


	/**
	 * Returns the name of this <code>GUIPlugin</code>.
	 *
	 * @return This plugin's name.
	 */
	public String getPluginName() {
		return name;
	}


	/**
	 * Returns the plugin version.
	 */
	public String getPluginVersion() {
		return VERSION_STRING;
	}


	/**
	 * Returns the file preferences for this plugin are saved in.
	 *
	 * @return The file.
	 */
	private static final File getPrefsFile() {
		return new File(RTextUtilities.getPreferencesDirectory(),
						"fileSystemTree.properties");
	}


	/**
	 * Returns the parent RText instance.
	 *
	 * @return The parent RText instance.
	 */
	RText getRText() {
		return owner;
	}


	/**
	 * Drills into a directory.
	 *
	 * @param dir The directory to drill into.
	 */
	public void goInto(File dir) {
		if (dir!=null && dir.isDirectory()) { // Should always be true
			try {

				tree.setRoot(dir);

				if (rootHistoryOffs==rootHistory.size()-1) {
					rootHistory.add(dir);
					rootHistoryOffs++;
				}
				else {
					rootHistory.add(++rootHistoryOffs, dir);
					int i = rootHistory.size() - 1;
					while (i>rootHistoryOffs) {
						rootHistory.remove(i);
						i--;
					}
				}

				dirLabel.setText(dir.getName());
				backAction.setEnabled(true);
				forwardAction.setEnabled(false);

			} catch (IllegalArgumentException iae) { // Just paranoid
				iae.printStackTrace();
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void install(AbstractPluggableGUIApplication app) {

		// Add a menu item to toggle the visibility of the dockable window
		owner.addAction(VIEW_FST_ACTION, viewAction);
		RTextMenuBar mb = (RTextMenuBar)owner.getJMenuBar();
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(viewAction);
		item.setSelected(getDockableWindow(getPluginName()).isActive());
		item.applyComponentOrientation(app.getComponentOrientation());
		JMenu viewMenu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		viewMenu.add(item);
		JPopupMenu popup = viewMenu.getPopupMenu();
		popup.pack();
		// Only needed for pre-1.6 support
		popup.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				item.setSelected(getDockableWindow(getPluginName()).isActive());
			}
		});

	}


	/**
	 * Loads saved preferences into the <code>prefs</code> member.  If this
	 * is the first time through, default values will be returned.
	 *
	 * @return The preferences.
	 */
	private FileSystemTreePrefs loadPrefs() {
		FileSystemTreePrefs prefs = new FileSystemTreePrefs();
		File prefsFile = getPrefsFile();
		if (prefsFile.isFile()) {
			try {
				prefs.load(prefsFile);
			} catch (IOException ioe) {
				getRText().displayException(ioe);
				// (Some) defaults will be used
			}
		}
		return prefs;
	}


	/**
	 * {@inheritDoc}
	 */
	public void savePreferences() {
		FileSystemTreePrefs prefs = new FileSystemTreePrefs();
		prefs.active = getDockableWindow(name).isActive();
		prefs.position = getDockableWindow(name).getPosition();
		prefs.windowVisibilityAccelerator = viewAction.getAccelerator();
		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			getRText().displayException(ioe);
		}
	}


	/**
	 * Called just before this <code>Plugin</code> is removed from an
	 * RText instance.  Here we uninstall any listeners we registered.
	 *
	 * @return Whether the uninstall went cleanly.
	 */
	public boolean uninstall() {
		return true;
	}


	/**
	 * Changes the tree view's root to the previous one.
	 */
	private class BackAction extends StandardAction {

		public BackAction(RText app, ResourceBundle msg) {
			super(app, msg, "Action.Back");
			setName(null); // We're only a toolbar icon
			setIcon("arrow_left.png");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			if (rootHistoryOffs>0) {
				Object obj = rootHistory.get(--rootHistoryOffs);
				if (obj instanceof File) {
					File dir = (File)obj;
					if (!dir.isDirectory()) {
						UIManager.getLookAndFeel().provideErrorFeedback(null);
						rootHistory.clear();
						rootHistory.add(null);
						rootHistoryOffs = 0;
						obj = null; // Default to showing the file system roots
					}
				}
				tree.setRoot((File)obj);
				dirLabel.setText(obj instanceof File ?
									((File)obj).getName() : null);
				setEnabled(rootHistoryOffs>0);
				forwardAction.setEnabled(rootHistoryOffs<rootHistory.size()-1);
			}
		}

	}


	/**
	 * Changes the tree view's root to the next one (assuming they've gone
	 * back at least once before).
	 */
	private class ForwardAction extends StandardAction {

		public ForwardAction(RText app, ResourceBundle msg) {
			super(app, msg, "Action.Forward");
			setName(null); // We're only a toolbar icon
			setIcon("arrow_right.png");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			if (rootHistoryOffs<rootHistory.size()-1) {
				Object obj = rootHistory.get(++rootHistoryOffs);
				if (obj instanceof File) {
					File dir = (File)obj;
					if (!dir.isDirectory()) {
						UIManager.getLookAndFeel().provideErrorFeedback(null);
						rootHistory.clear();
						rootHistory.add(null);
						rootHistoryOffs = 0;
						obj = null; // Default to showing the file system roots
					}
				}
				tree.setRoot((File)obj);
				dirLabel.setText(obj instanceof File ?
						((File)obj).getName() : null);
				backAction.setEnabled(rootHistoryOffs>0);
				setEnabled(rootHistoryOffs<rootHistory.size()-1);
			}
		}

	}


	/**
	 * Toggles the visibility of this file system tree.
	 */
	private class ViewAction extends StandardAction {

		public ViewAction(RText app, ResourceBundle msg) {
			super(app, msg, "MenuItem.View");
		}

		public void actionPerformed(ActionEvent e) {
			DockableWindow wind = getDockableWindow(getPluginName());
			wind.setActive(!wind.isActive());
		}

	}


}