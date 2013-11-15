/*
 * 09/16/2005
 *
 * HeapIndicatorPlugin.java - Status bar panel showing the current JVM heap.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.heapindicator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.fife.rtext.RTextUtilities;
import org.fife.ui.SubstanceUtils;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.StatusBarPlugin;


/**
 * A status bar component displaying the current JVM heap.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class HeapIndicatorPlugin extends StatusBarPlugin {

	private AbstractPluggableGUIApplication app;
	private HeapIcon heapIcon;
	private Timer timer;
	private TimerEvent timerEvent;
	private long usedMem;
	private long totalMem;
	private ResourceBundle msg;
	private HeapIndicatorOptionPanel optionPanel;
	private Icon pluginIcon;

	private boolean useSystemColors;
	private Color iconForeground;
	private Color iconBorderColor;

	private static Object[] objArray;

	private static final String BUNDLE_NAME		=
					"org.fife.rtext.plugins.heapindicator.HeapIndicator";
	private static final String VERSION		= "2.5.2";


	/**
	 * Constructor.
	 *
	 * @param app The GUI application.
	 */
	public HeapIndicatorPlugin(AbstractPluggableGUIApplication app) {

		msg = ResourceBundle.getBundle(BUNDLE_NAME);

		HeapIndicatorPrefs prefs = loadPrefs();

		this.app = app;
		heapIcon = new HeapIcon(this);
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(2,2,4,2));
		add(new JLabel(heapIcon));

		setUseSystemColors(prefs.useSystemColors);
		setIconForeground(prefs.iconForeground);
		setIconBorderColor(prefs.iconBorderColor);
		setVisible(prefs.visible);

		getData();
		setRefreshInterval(prefs.refreshInterval); // Must be called!

		ToolTipManager.sharedInstance().registerComponent(this);

		try {
			URL res = getClass().getResource("indicator.png");
			pluginIcon = new ImageIcon(ImageIO.read(res));
		} catch (IOException ioe) { // Never happens
			app.displayException(ioe);
		}

	}


	protected static final long bytesToKb(long bytes) {
		return bytes / 1024L;
	}


	/**
	 * Returns this plugin's resource bundle.
	 *
	 * @return The resource bundle.
	 */
	ResourceBundle getBundle() {
		return msg;
	}


	/**
	 * Updates heap memory information.
	 */
	protected void getData() {
		totalMem = Runtime.getRuntime().totalMemory();
		usedMem =  totalMem - Runtime.getRuntime().freeMemory();
	}


	public Color getIconBorderColor() {

		Color c = iconBorderColor;

		if (getUseSystemColors()) {
			if (SubstanceUtils.isSubstanceInstalled()) {
				try {
					c = SubstanceUtils.getSubstanceColor(
										SubstanceUtils.ULTRA_DARK_COLOR);
				} catch (Exception e) {
					c = UIManager.getColor("Label.foreground");
					e.printStackTrace();
				}
			}
			else {
				c = UIManager.getColor("Label.foreground");
			}
		}

		return c;

	}


	public Color getIconForeground() {

		Color c = iconForeground;

		if (getUseSystemColors()) {
			if (SubstanceUtils.isSubstanceInstalled()) {
				try {
					c = SubstanceUtils.getSubstanceColor(
										SubstanceUtils.MID_COLOR);
				} catch (Exception e) {
					c = UIManager.getColor("ProgressBar.foreground");
					e.printStackTrace();
				}
			}
			else {
				c = UIManager.getColor("ProgressBar.foreground");
			}
		}

		return c;

	}


	/**
	 * Returns an options panel for use in an Options dialog.  This panel
	 * should contain all options pertaining to this plugin.
	 *
	 * @return The options panel.
	 */
	public PluginOptionsDialogPanel getOptionsDialogPanel() {
		if (optionPanel==null)
			optionPanel = new HeapIndicatorOptionPanel(app, this);
		return optionPanel;
	}


	/**
	 * Returns the two long values in an <code>Object</code> array.
	 *
	 * @param num The first long.
	 * @param denom The second long.
	 * @return The two longs in an <code>Object</code> array.
	 */
	private static final Object[] getParams(long num, long denom) {
		if (objArray==null)
			objArray = new Object[2];
		objArray[0] = new Long(num);
		objArray[1] = new Long(denom);
		return objArray;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	/**
	 * {@inheritDoc}
	 */
	public Icon getPluginIcon() {
		return pluginIcon;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getPluginName() {
		return msg.getString("Plugin.Name");
	}


	/**
	 * {@inheritDoc}
	 */
	public String getPluginVersion() {
		return VERSION;
	}


	/**
	 * Returns the file preferences for this plugin are saved in.
	 *
	 * @return The file.
	 */
	private static final File getPrefsFile() {
		return new File(RTextUtilities.getPreferencesDirectory(),
						"heapIndicator.properties");
	}


	/**
	 * Returns the refresh interval of the heap indicator.
	 *
	 * @return The refresh interval, in milliseconds.
	 * @see #setRefreshInterval
	 */
	public int getRefreshInterval() {
		return timer==null ? -1 : timer.getDelay();
	}


	/**
	 * Returns the text to display for the tooltip.
	 *
	 * @return The tooltip text.
	 */
	@Override
	public String getToolTipText() {
		long num = bytesToKb(getUsedMemory());
		long denom = bytesToKb(getTotalMemory());
		String toolTip = msg.getString("Plugin.ToolTip.text");
		toolTip = MessageFormat.format(toolTip, getParams(num, denom));
		return toolTip;
	}


	/**
	 * Returns the total amount of memory available to the JVM.
	 *
	 * @return The total memory available to the JVM, in bytes.
	 * @see #getUsedMemory
	 */
	public long getTotalMemory() {
		return totalMem;
	}


	/**
	 * Returns the amount of memory currently being used by the JVM.
	 *
	 * @return The memory being used by the JVM, in bytes.
	 * @see #getTotalMemory
	 */
	public long getUsedMemory() {
		return usedMem;
	}


	/**
	 * Returns whether or not system colors are used when painting the
	 * heap indicator.
	 *
	 * @return Whether or not to use system colors.
	 * @see #setUseSystemColors
	 */
	public boolean getUseSystemColors() {
		return useSystemColors;
	}


	/**
	 * Called just after a plugin is added to a GUI application.  If this is
	 * a <code>GUIPlugin</code>, it has already been added visually.  Plugins
	 * should use this method to register any listeners to the GUI application
	 * and do any other necessary setup.
	 *
	 * @param app The application to which this plugin was just added.
	 * @see #uninstall
	 */
	public void install(AbstractPluggableGUIApplication app) {
	}


	protected void installTimer(int interval) {
		if (timer==null) {
			timerEvent = new TimerEvent();
			timer = new Timer(interval, timerEvent);
		}
		else {
			timer.stop();
			timer.setDelay(interval);
		}
		timer.start();
	}


	/**
	 * Loads saved preferences into the <code>prefs</code> member.  If this
	 * is the first time through, default values will be returned.
	 *
	 * @return The preferences.
	 */
	private HeapIndicatorPrefs loadPrefs() {
		HeapIndicatorPrefs prefs = new HeapIndicatorPrefs();
		File prefsFile = getPrefsFile();
		if (prefsFile.isFile()) {
			try {
				prefs.load(prefsFile);
			} catch (IOException ioe) {
				app.displayException(ioe);
				// (Some) defaults will be used
			}
		}
		return prefs;
	}


	@Override
	protected void processMouseEvent(MouseEvent e) {
		switch (e.getID()) {
			case MouseEvent.MOUSE_CLICKED:
				if (e.getClickCount()==2) {
					long oldMem = getUsedMemory();
					Runtime.getRuntime().gc();
					getData();
					long newMem = getUsedMemory();
					long difference = oldMem - newMem;
					String text = msg.getString(
									"Plugin.PopupDialog.GC.text");
					text = MessageFormat.format(text,
								new Object[] {
									new Long(bytesToKb(difference))
								}
					);
					JOptionPane.showMessageDialog(app, text,
							msg.getString("Plugin.PopupDialog.GC.title"),
							JOptionPane.INFORMATION_MESSAGE);
				}
				break;
			default:
		}
		super.processMouseEvent(e);
	}


	/**
	 * {@inheritDoc}
	 */
	public void savePreferences() {
		HeapIndicatorPrefs prefs = new HeapIndicatorPrefs();
		prefs.visible         = isVisible();
		prefs.refreshInterval = getRefreshInterval();
		prefs.useSystemColors = getUseSystemColors();
		prefs.iconForeground  = getIconForeground();
		prefs.iconBorderColor = getIconBorderColor();
		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			app.displayException(ioe);
		}
	}


	public void setIconBorderColor(Color iconBorderColor) {
		this.iconBorderColor = iconBorderColor;
		repaint();
	}


	public void setIconForeground(Color iconForeground) {
		this.iconForeground = iconForeground;
		repaint();
	}


	/**
	 * Sets the refresh interval for the heap indicator.
	 *
	 * @param interval The new refresh interval, in milliseconds.
	 * @see #getRefreshInterval
	 */
	public void setRefreshInterval(int interval) {
		if (interval<=0 || interval==getRefreshInterval())
			return;
		installTimer(interval);
	}


	/**
	 * Sets whether or not to use system colors when painting the heap
	 * indicator.
	 *
	 * @param useSystemColors Whether or not to use system colors.
	 * @see #getUseSystemColors
	 */
	public void setUseSystemColors(boolean useSystemColors) {
		if (useSystemColors!=getUseSystemColors()) {
			this.useSystemColors = useSystemColors;
			repaint();
		}
	}


	@Override
	public void setVisible(boolean visible) {
		if (visible)
			installTimer(getRefreshInterval());
		else
			uninstallTimer();
		super.setVisible(visible);
	}


	/**
	 * Called just before this <code>Plugin</code> is removed from an
	 * <code>GUIApplication</code>.  This gives the plugin a chance to clean
	 * up any loose ends (kill any threads, close any files, remove listeners,
	 * etc.).
	 *
	 * @return Whether the uninstall went cleanly.
	 * @see #install
	 */
	public boolean uninstall() {
		uninstallTimer();
		return true;
	}


	protected void uninstallTimer() {
		if (timer!=null) {
			timer.stop();
			timer.removeActionListener(timerEvent);
			timerEvent = null;	// May help GC.
			timer = null;		// May help GC.
		}
	}


	/**
	 * Timer event that gets fired.  This refreshes the GC icon.
	 */
	private class TimerEvent implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			getData();
			repaint();
		}

	}


}