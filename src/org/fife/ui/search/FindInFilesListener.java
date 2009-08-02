/*
 * 08/11/2004
 *
 * FindInFilesListener.java - Listens for events from a FindInFilesDialog.
 * Copyright (C) 2004 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
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
package org.fife.ui.search;

import java.util.EventListener;


/**
 * An interface for objects that wish to be notified of files being selected
 * in a <code>FindInFilesDialog.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public interface FindInFilesListener extends EventListener {


	/**
	 * Called when the user selects a file in a listened-to find-in-files dialog.
	 */
	public void findInFilesFileSelected(FindInFilesEvent e);


}