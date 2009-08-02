/*
 * 11/14/2003
 *
 * CurrentTextAreaListener.java - Listens for the current text area changing
 * in RText.
 * Copyright (C) 2003 Robert Futrell
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

import java.util.EventListener;


/**
 * A listener interested in knowing about changes to the current text area.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public interface CurrentTextAreaListener extends EventListener {


	/**
	 * Called when a property of the current text area (or the current text
	 * area itself) is modified.
	 *
	 * @param e The event.
	 */
	public void currentTextAreaPropertyChanged(CurrentTextAreaEvent e);


}