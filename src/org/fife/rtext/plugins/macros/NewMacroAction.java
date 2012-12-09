/*
 * 07/23/2011
 *
 * NewMacroAction.java - Action that creates a new macro.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
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
import org.fife.ui.app.StandardAction;


/**
 * Action that creates a new macro.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class NewMacroAction extends StandardAction {

	/**
	 * The parent plugin.
	 */
	private MacroPlugin plugin;


	/**
	 * Constructor.
	 *
	 * @param plugin The parent plugin.
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 */
	public NewMacroAction(MacroPlugin plugin, RText owner, ResourceBundle msg) {
		super(owner, msg, "NewMacroAction");
		setIcon("cog_add.png");
		this.plugin = plugin;
	}


	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent e) {

		RText owner = (RText)getApplication();
		NewMacroDialog nmd = new NewMacroDialog(plugin, owner);
		nmd.setVisible(true);

		Macro macro = nmd.getMacro();
		if (macro!=null) {

			File file = new File(macro.getFile());
			if (!file.isFile()) { // Should always be true
				createInitialContentByExtension(file);
			}

			((RText)getApplication()).openFile(file.getAbsolutePath());
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


}