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

import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * Listens for events in child windows of RText.<p>
 *
 * This class is not currently used.
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
	private int whenTranslucent;

	private Map/*<Window, Boolean>*/ translucentMap;

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
		translucentMap = new HashMap();
	}


	public void componentMoved(ComponentEvent e) {
		System.out.println("Moved!");
		Window w = (Window)e.getComponent();
		Rectangle bounds = w.getBounds();
		if (bounds.intersects(app.getBounds())) {
			setTranslucent(w, true);
		}
		else {
			setTranslucent(w, false);
		}
	}


	/**
	 * Returns whether translucency is supported by this JVM.
	 *
	 * @return Whether translucency is supported.
	 */
	public static boolean isTranslucencySupported() {

		boolean supported = false;

//		try {
//			Class enumClazz = Class.forName("com.sun.awt.AWTUtilities$Translucency");
//			Field[] fields = enumClazz.getDeclaredFields();
//			for (int i=0; i<fields.length; i++) {
//				System.out.println(fields[i].getName());
//				if ("TRANSLUCENT".equals(fields[i].getName())) {
//System.out.println("yay");
//					supported = true;
//				}
//			}
//		} catch (RuntimeException re) {
//			throw re;
//		} catch (Exception e) {
//			supported = false; // FindBugs - non-empty catch block
//		}

		return supported;

	}


	private void setTranslucent(Window w, boolean translucent) {
		try {
			Class clazz = Class.forName(CLASS_NAME);
			Method m = clazz.getDeclaredMethod("setWindowOpacity",
								new Class[] { Window.class, float.class });
			float val = translucent ? app.getSearchWindowOpacity():1.0f;
			m.invoke(null, new Object[] { w, new Float(val) });
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * Called when a window gains focus.
	 *
	 * @param e The event.
	 */
	public void windowGainedFocus(WindowEvent e) {
		setTranslucent(e.getWindow(), false);
	}


	/**
	 * Called when a window loses focus.
	 *
	 * @param e The event.
	 */
	public void windowLostFocus(WindowEvent e) {
		setTranslucent(e.getWindow(), true);
	}


}