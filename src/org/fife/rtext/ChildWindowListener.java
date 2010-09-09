/*
 * 07/15/2009
 *
 * ChildWindowListener.java - Listens for events in child windows.
 * Copyright (C) 2009 Robert Futrell
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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.lang.reflect.Method;


/**
 * Listens for events in child windows of RText, and toggles the opacity of
 * those child windows if desired.  This is done via reflection, since the
 * features we're using were added in Java 6 but RText supports 1.4+.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ChildWindowListener extends ComponentAdapter
							implements WindowFocusListener {

	/**
	 * Constant indicating that the child windows should never be
	 * translucent.
	 */
	public static final int TRANSLUCENT_NEVER					= 0;

	/**
	 * Indicates that the child windows should be translucent when they
	 * are not focused.
	 */
	public static final int TRANSLUCENT_WHEN_NOT_FOCUSED		= 1;

	/**
	 * Indicates that the child windows should be translucent when they
	 * are overlapping the main application window.
	 */
	public static final int TRANSLUCENT_WHEN_OVERLAPPING_APP	= 2;

	/**
	 * Indicates that the child windows should always be translucent.
	 */
	public static final int TRANSLUCENT_ALWAYS					= 3;

	/**
	 * The parent application.
	 */
	private RText app;

	/**
	 * When the child windows should be made translucent.
	 */
	private int translucencyRule;

	/**
	 * The class that handles window transparency in 6u10.
	 */
	private static final String CLASS_NAME = "com.sun.awt.AWTUtilities";


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	public ChildWindowListener(RText app) {
		this.app = app;
	}


	/**
	 * Called when one of the child windows is moved.
	 */
	public void componentMoved(ComponentEvent e) {
		if (translucencyRule==TRANSLUCENT_WHEN_OVERLAPPING_APP) {
			Window w = (Window)e.getComponent();
			if (!w.isShowing()) {
				// Resized evidently called (as a result of
				// setLocationRelativeTo() ?) before window is shown.
				return;
			}
			else if (w==app) {
				// Main application window - might change overlapping with
				// children
				refreshTranslucencies();
			}
			else { // One of Find or Replace dialog
				refreshTranslucency(w);
			}
		}
	}


	/**
	 * Called when one of the child windows is resized.
	 */
	public void componentShown(ComponentEvent e) {
		// Need to do this always, in case the translucency rule changed since
		// the window was last visible.
		Window w = (Window)e.getComponent();
		if (w!=app) {
			refreshTranslucency(w);
		}
	}



	/**
	 * Called when one of the child windows is resized.
	 */
	public void componentResized(ComponentEvent e) {
		if (translucencyRule==TRANSLUCENT_WHEN_OVERLAPPING_APP) {
			Window w = (Window)e.getComponent();
			if (!w.isShowing()) {
				// Resized evidently called (as a result of pack() ?) before
				// window is shown.
				return;
			}
			else if (w==app) {
				// Main application window - might change overlapping with
				// children
				refreshTranslucencies();
			}
			else { // One of Find or Replace dialog
				refreshTranslucency(w);
			}
		}
	}


	/**
	 * Returns the opacity of a window.
	 *
	 * @param w The window.
	 * @return The opacity of the window.
	 */
	private float getOpacity(Window w) {
		float opacity = 1;
		try {
			Class clazz = Class.forName(CLASS_NAME);
			Method m = clazz.getDeclaredMethod("getWindowOpacity",
								new Class[] { Window.class });
			opacity = ((Float)m.invoke(null, new Object[] { w })).floatValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return opacity;
	}


	/**
	 * Refreshes the opacity of a window based on the current properties
	 * selected by the user.
	 *
	 * @param window The window to refresh.
	 */
	private void refreshTranslucency(Window window) {

		// The user has turned off this feature.
		if (!app.isSearchWindowOpacityEnabled()) {
			setTranslucent(window, false);
			return;
		}

		switch (translucencyRule) {

			case TRANSLUCENT_ALWAYS:
				setTranslucent(window, true);
				break;

			case TRANSLUCENT_NEVER:
				setTranslucent(window, false);
				break;

			case TRANSLUCENT_WHEN_NOT_FOCUSED:
				setTranslucent(window, !window.isFocused());
				break;

			case TRANSLUCENT_WHEN_OVERLAPPING_APP:
				// TODO: Convert p1 and p2 appropriately for multi-monitors.
				Point p1 = window.getLocationOnScreen();
				Rectangle bounds1 = window.getBounds();
				bounds1.setLocation(p1);
				Point p2 = app.getLocationOnScreen();
				Rectangle bounds2 = app.getBounds();
				bounds2.setLocation(p2);
				setTranslucent(window, bounds2.intersects(bounds1));
				break;

		}

	}


	/**
	 * Refreshes the opacity of all windows being listened to.
	 */
	public void refreshTranslucencies() {
		AbstractMainView view = app.getMainView();
		// A window must be showing for its bounds to be queried.
		if (view.findDialog!=null && view.findDialog.isShowing()) {
			refreshTranslucency(view.findDialog);
		}
		if (view.replaceDialog!=null && view.replaceDialog.isShowing()) {
			refreshTranslucency(view.replaceDialog);
		}
	}


	public void setTranslucencyRule(int rule) {
		if (rule>=0 && rule<=3 && rule!=translucencyRule) {
			this.translucencyRule = rule;
			if (rule==TRANSLUCENT_WHEN_OVERLAPPING_APP) {
				app.addComponentListener(this);
			}
			else { // OK if not actually added
				app.removeComponentListener(this);
			}
			refreshTranslucencies();
		}
	}


	/**
	 * Sets whether a specific window is translucent.
	 *
	 * @param w The window.
	 * @param translucent Whether that window is translucent.
	 */
	private void setTranslucent(Window w, boolean translucent) {

		float curOpacity = getOpacity(w);
		float newOpacity = translucent ? app.getSearchWindowOpacity() : 1;

		if (curOpacity!=newOpacity) {
			try {
				Class clazz = Class.forName(CLASS_NAME);
				Method m = clazz.getDeclaredMethod("setWindowOpacity",
									new Class[] { Window.class, float.class });
				m.invoke(null, new Object[] { w, new Float(newOpacity) });
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}


	/**
	 * Called when a window gains focus.
	 *
	 * @param e The event.
	 */
	public void windowGainedFocus(WindowEvent e) {
		if (translucencyRule==TRANSLUCENT_WHEN_NOT_FOCUSED) {
			refreshTranslucency(e.getWindow());
		}
	}


	/**
	 * Called when a window loses focus.
	 *
	 * @param e The event.
	 */
	public void windowLostFocus(WindowEvent e) {
		if (translucencyRule==TRANSLUCENT_WHEN_NOT_FOCUSED) {
			refreshTranslucency(e.getWindow());
		}
	}


}