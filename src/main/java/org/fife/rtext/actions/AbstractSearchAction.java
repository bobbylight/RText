/*
 * Copyright (C) 2003 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import org.fife.ui.rtextarea.SearchContext;

import javax.swing.*;
import java.awt.event.ActionEvent;


/**
 * A base class for actions that perform search operations.
 *
 * @author Robert Futrell
 * @version 1,0
 */
public interface AbstractSearchAction extends Action {


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param context The search context to use.  The context shared between
	 *        all of the find/replace dialogs will be used if this is
	 *        {@code null}.  This is here as a means for callers to override
	 *        what search is performed to be different from what is in the
	 *        current search dialog's UI (e.g. if the user uses a keyboard
	 *        shortcut to search backwards).
	 * @see #actionPerformed(ActionEvent)
	 */
	void actionPerformed(SearchContext context);
}
