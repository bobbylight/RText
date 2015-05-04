/*
 * 08/28/2012
 *
 * RenameDialog.java - Dialog for renaming workspace tree nodes.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.tree.NameChecker;


/**
 * A dialog for renaming nodes in the workspace outline tree.  Note that
 * although we extend {@link AbstractEnterFileNameDialog}, we are not always
 * renaming a file.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RenameDialog extends AbstractEnterFileNameDialog {


	/**
	 * Constructor.
	 *
	 * @param owner The rtext window that owns this dialog.
	 * @param isForFile Whether this dialog is for a regular file as opposed to
	 *        a directory.
	 * @param type The type of node being renamed.
	 * @param checker The name checking callback.
	 */
	public RenameDialog(RText owner, boolean isForFile, String type,
			NameChecker checker) {
		super(owner, isForFile, checker);
		setTitle(Messages.getString("RenameDialog.Title", type));
	}


	@Override
	protected void addDescPanel() {
	}


}