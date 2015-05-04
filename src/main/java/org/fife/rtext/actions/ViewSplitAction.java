/*
 * 12/10/2006
 *
 * ViewSplitAction.java - Action to split the editor view.
 * Copyright (C) 2006 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import org.fife.rtext.RText;
import org.fife.rtext.RTextActionInfo;
import org.fife.ui.app.StandardAction;


/**
 * Action that splits the editor view either vertically or horizontally.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ViewSplitAction extends StandardAction {

	//private String splitType;


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 * @param nameKey The localization key for the name.
	 * @param splitType One of
	 *        {@link RTextActionInfo#VIEW_SPLIT_HORIZ_ACTION},
	 *        {@link RTextActionInfo#VIEW_SPLIT_NONE_ACTION}, or
	 *        {@link RTextActionInfo#VIEW_SPLIT_VERT_ACTION}.
	 */
	public ViewSplitAction(RText owner, ResourceBundle msg, Icon icon,
							String nameKey, String splitType) {
		super(owner, msg, nameKey);
		setIcon(icon);
		//this.splitType = splitType;
	}


	public void actionPerformed(ActionEvent e) {
		// TODO
		//owner.setEditorSplitType(splitType);
		//owner.getMainView().setEditorSplitType(splitType);
	}


}