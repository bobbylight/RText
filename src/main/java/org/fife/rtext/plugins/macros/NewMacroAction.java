/*
 * 07/23/2011
 *
 * NewMacroAction.java - Action that creates a new macro.
 * Copyright (C) 2011 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.macros;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ResourceBundle;

import org.fife.io.IOUtil;
import org.fife.rtext.RText;
import org.fife.ui.app.AppAction;
import org.fife.ui.app.themes.FlatLightTheme;
import org.fife.ui.app.themes.NativeTheme;
import org.fife.util.MacOSUtil;

import javax.swing.*;


/**
 * Action that creates a new macro.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class NewMacroAction extends AppAction<RText> {

	/**
	 * The parent plugin.
	 */
	private final MacroPlugin plugin;


	/**
	 * Constructor.
	 *
	 * @param plugin The parent plugin.
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 */
	NewMacroAction(MacroPlugin plugin, RText owner, ResourceBundle msg) {
		super(owner, msg, "NewMacroAction");
		this.plugin = plugin;
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		RText owner = getApplication();
		NewMacroDialog nmd = new NewMacroDialog(plugin, owner);
		nmd.setVisible(true);

		Macro macro = nmd.getMacro();
		if (macro!=null) {

			File file = new File(macro.getFile());
			if (!file.isFile()) { // Should always be true
				createInitialContentByExtension(file);
			}

			getApplication().openFile(file);
			MacroManager.get().addMacro(macro);

		}

	}


	/**
	 * Creates a new file with initial content for a macro, based on the
	 * file's extension.
	 *
	 * @param file The file's extension.
	 */
	private void createInitialContentByExtension(File file) {

		try {
			PrintWriter w = new PrintWriter(new BufferedWriter(
						new FileWriter(file)));
			String fileName = file.getName();
			String ext = fileName.substring(fileName.lastIndexOf('.')+1);
			String content = getInitialContentImpl(ext);
			w.println(content);
			w.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}


	private String getInitialContentImpl(String ext) throws IOException {
		InputStream in = getClass().getResourceAsStream(ext + ".template.txt");
		return IOUtil.readFully(in);
	}


	void restoreDefaultIcon() {

		String themeId = getApplication().getTheme().getId();
		if (MacOSUtil.isMacOs()) {
			String menuItemThemeId = NativeTheme.ID.equals(themeId) ?
				themeId : FlatLightTheme.ID;
			setIcon(plugin.getIcon(menuItemThemeId));
			setRolloverIcon((Icon)null);
			return;
		}

		setIcon(plugin.getIcon(themeId));
		Icon rolloverIcon = FlatLightTheme.ID.equals(themeId) ?
			plugin.getIcon("white") : null;
		setRolloverIcon(rolloverIcon);
	}
}
