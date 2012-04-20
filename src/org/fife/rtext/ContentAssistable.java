/*
 * 01/12/2009
 *
 * ContentAssistable.java - A component, such as text field, that supports
 * content assist.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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