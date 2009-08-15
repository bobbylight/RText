/*
 * 12/22/2004
 *
 * RTextActionInfo.java - Information on the actions owned by RText.
 * Copyright (C) 2004 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.rtext;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;


/**
 * An interface containing all information about RText's actions.<p>
 *
 * Note that these actions do NOT include those owned by
 * <code>AbstractMainView</code>.<p>
 *
 * Since RText extends <code>AbstractGUIApplication</code>, its
 * actions are kept in a hash map.  Thus, this interface contains the keys
 * for all actions, an array of those keys for looping, and the default
 * values for accelerators for all of the actions.<p>
 *
 * @author Robert Futrell
 * @version 0.1
 */
interface RTextActionInfo {

	// Constants specifying the available actions (for getAction).
	public static final String NEW_ACTION			= "newAction";
	public static final String OPEN_ACTION			= "openAction";
	public static final String OPEN_NEWWIN_ACTION	= "openNewWinAction";
	public static final String OPEN_REMOTE_ACTION	= "openRemoteAction";
	public static final String SAVE_ACTION			= "saveAction";
	public static final String SAVE_AS_ACTION		= "saveAsAction";
	public static final String SAVE_AS_REMOTE_ACTION	= "saveAsRemoteAction";
	public static final String SAVE_WEBPAGE_ACTION	= "saveWebPageAction";
	public static final String SAVE_ALL_ACTION		= "saveAllAction";
	public static final String COPY_AS_RTF_ACTION	= "copyAsRtf";
	public static final String TIME_DATE_ACTION		= "timeDateAction";
	public static final String LINE_NUMBER_ACTION	= "lineNumberAction";
	public static final String OPTIONS_ACTION		= "optionsAction";
	public static final String HOME_PAGE_ACTION		= "homePageAction";


	/**
	 * The names of all actions in an array.  Note that the order of these
	 * action names MUST be kept in-synch with the default accelerators
	 * array below.
	 */
	public static final String[] actionNames = {
		NEW_ACTION,
		OPEN_ACTION,
		OPEN_NEWWIN_ACTION,
		OPEN_REMOTE_ACTION,
		SAVE_ACTION,
		SAVE_AS_ACTION,
		SAVE_AS_REMOTE_ACTION,
		SAVE_WEBPAGE_ACTION,
		SAVE_ALL_ACTION,
		COPY_AS_RTF_ACTION,
		TIME_DATE_ACTION,
		LINE_NUMBER_ACTION,
		RText.HELP_ACTION_KEY,
		RText.ABOUT_ACTION_KEY,
		OPTIONS_ACTION,
		HOME_PAGE_ACTION,
	};

	static final int defaultModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	static final int shift = InputEvent.SHIFT_MASK;


	/**
	 * The default accelerator for all RText actions.  Note that this
	 * array MUST be kept in-synch with the actionNames array
	 * above.
	 */
	public static final KeyStroke[] defaultActionAccelerators = {
					KeyStroke.getKeyStroke(KeyEvent.VK_N, defaultModifier),
					KeyStroke.getKeyStroke(KeyEvent.VK_O, defaultModifier),
					KeyStroke.getKeyStroke(KeyEvent.VK_I, defaultModifier),
					null,
					KeyStroke.getKeyStroke(KeyEvent.VK_S, defaultModifier),
					null,
					null,
					null,
					null,
					KeyStroke.getKeyStroke(KeyEvent.VK_C, defaultModifier|shift),
					KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
					KeyStroke.getKeyStroke(KeyEvent.VK_1, defaultModifier),
					KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
					null,
					null,
					null,
			};

}