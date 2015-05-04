/*
 * 11/14/2003
 *
 * CurrentTextAreaEvent.java - Event fired when the current text area in RText
 * changes.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.util.EventObject;


/**
 * Event that notifies when a property of the current text area changes.
 *
 * @author Robert Futrell
 * @version 0.6
 */
public class CurrentTextAreaEvent extends EventObject {

	private static final int MIN_TYPE_VALUE			= 0;

	/**
	 * Fired when the currently active text area changes.
	 */
	public static final int TEXT_AREA_CHANGED		= 0;

	/**
	 * Fired when the current text area is made dirty or saved.
	 */
	public static final int IS_MODIFIED_CHANGED		= 1;

	/**
	 * Fired when the current text area's filename is changed.
	 */
	public static final int FILE_NAME_CHANGED		= 2;

	/**
	 * Fired when the current text area's syntax style changes.
	 */
	public static final int SYNTAX_STYLE_CNANGED		= 3;

	private static final int MAX_TYPE_VALUE			= 3;

	private int type;
	private Object oldValue;
	private Object newValue;


	/**
	 * Constructor.
	 *
	 * @param mainView The main view whose current text area (or a property
	 *        of it) changed.
	 * @param type The type of property that changed.
	 * @param oldValue The old value of the property.
	 * @param newValue The new value of the property.
	 */
	public CurrentTextAreaEvent(AbstractMainView mainView, int type,
							Object oldValue, Object newValue) {
		super(mainView);
		if (type<MIN_TYPE_VALUE || type>MAX_TYPE_VALUE)
			throw new IllegalArgumentException("Invalid type: " + type);
		this.type = type;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}


	/**
	 * Returns the new value of the current text area property.
	 *
	 * @return The new value.
	 */
	public Object getNewValue() {
		return newValue;
	}


	/**
	 * Returns the old value of the current text area property.
	 *
	 * @return The old value.
	 */
	public Object getOldValue() {
		return oldValue;
	}


	/**
	 * Returns the main view whose current text area (or a property of it)
	 * changed.
	 *
	 * @return The main view.
	 */
	public AbstractMainView getMainView() {
		return (AbstractMainView)getSource();
	}


	/**
	 * Returns the type of property that changed.  This allows you to know
	 * what type the old and new value objects are.
	 *
	 * @return The type of property that changed.
	 */
	public int getType() {
		return type;
	}


	/**
	 * Returns a string representation of this event.  Useful for debugging.
	 *
	 * @return A string representation of this event.
	 */
	@Override
	public String toString() {
		return "[CurrentTextAreaEvent: " +
			"type=" + getType() +
			//"; oldValue=" + getOldValue() +
			//"; newValue=" + getNewValue() +
			"]";
	}


}