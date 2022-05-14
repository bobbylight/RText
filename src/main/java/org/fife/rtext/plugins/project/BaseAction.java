/*
 * 09/08/2012
 *
 * BaseAction.java - A base class for tree node actions.
 * Copyright (C) 2012 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.project;

import org.fife.ui.StandardAction;

import javax.swing.*;


/**
 * A base class for tree node actions.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class BaseAction extends StandardAction implements PopupContent{


	protected BaseAction(String keyRoot) {
		this(keyRoot, (Icon)null);
	}


	protected BaseAction(String keyRoot, Icon icon) {
		setName(Messages.getString(keyRoot));
		setMnemonic(Messages.getMnemonic(keyRoot + ".Mnemonic"));
		setIcon(icon);
	}

}
