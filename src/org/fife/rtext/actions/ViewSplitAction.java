/*
 * 12/10/2006
 *
 * ViewSplitAction.java - Action to split the editor view.
 * Copyright (C) 2006 Robert Futrell
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