/*
 * 01/12/2009
 *
 * ContentAssistable.java - A component, such as text field, that supports
 * content assist.
 * Copyright (C) 2009 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
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


/**
 * A component (such as a text field) that supports content assist.
 * Implementations will fire a property change event of type
 * {@link #ASSISTANCE_IMAGE} when content assist is enabled or disabled.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface ContentAssistable {

	/**
	 * Property event fired when the image to use when the component is focused
	 * changes.  This will either be <code>null</code> for "no image," or
	 * a <code>java.awt.Image</code>.
	 */
	public static final String ASSISTANCE_IMAGE	= "AssistanceImage";


}