/*
 * 12/14/2015
 *
 * Copyright (C) 2015 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.langsupport.typescript;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JTable;

import org.fife.rtext.AbstractParserNoticeWindow;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.rtext.plugins.langsupport.Plugin;
import org.fife.ui.ImageTranscodingUtil;
import org.fife.ui.RScrollPane;
import org.fife.ui.UIUtil;
import org.fife.ui.dockablewindows.DockableWindowScrollPane;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;


/**
 * A window that displays all errors in the most recent run of the TypeScript
 * compiler.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class TypeScriptNoticeWindow extends AbstractParserNoticeWindow {

	private final Plugin plugin;
	private final TypeScriptNoticeTableModel model;


	TypeScriptNoticeWindow(RText rtext, Plugin plugin) {

		super(rtext);
		this.plugin = plugin;
		ResourceBundle msg = plugin.getBundle();

		model = new TypeScriptNoticeTableModel(msg.getString("TypeScript.Message"));
		JTable table = createTable(model);
		UIUtil.removeTabbedPaneFocusTraversalKeyBindings(table);
		RScrollPane sp = new DockableWindowScrollPane(table);
		UIUtil.removeTabbedPaneFocusTraversalKeyBindings(sp);

		setLayout(new BorderLayout());
		add(sp);

		// active and position are set by caller, from TasksPrefs
		setDockableWindowName(msg.getString("TypeScript"));
		setDockableWindowTitle(msg.getString("TypeScript.BuildOutput"));

		try {
			InputStream in = getClass().getResourceAsStream(
				"/org/fife/rtext/plugins/langsupport/typescript.svg");
			setIcon(new ImageIcon(ImageTranscodingUtil.rasterize("ts", in, 16, 16)));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		applyComponentOrientation(rtext.getComponentOrientation());

	}


	/**
	 * Starts a thread to do the build based off the <code>tsconfig</code> file
	 * closest to the active editor's file.
	 */
	public void doBuild() {

		// Ensure we're visible
		if (!isActive()) {
			setActive(true);
		}

		this.focusInDockableWindowGroup(true);
		getRText().getMainView().getCurrentTextArea().requestFocusInWindow();
		model.update(null, null); // Clear prior results
		new BuildThread(plugin, this).start();

	}


	public void parseErrors(File rootDir, String stdout) {

		List<ParserNotice> errors = new ArrayList<>();

		Pattern error = Pattern.compile("([^\\(]+)\\((\\d+),(\\d+)\\): (.+)");

		String[] lines = stdout.split("\n");
		for (String line : lines) {
			Matcher m = error.matcher(line);
			if (m.matches()) {
				String fileFullPath = new File(rootDir, m.group(1)).
						getAbsolutePath();
				int lineNum = Integer.parseInt(m.group(2));
				String message = m.group(4);
				TypeScriptParserNotice notice = new TypeScriptParserNotice(
						null, message, lineNum);
				notice.setFileFullPath(fileFullPath);
				errors.add(notice);
			}
		}

		setTitleAfterBuilding(rootDir, errors);
		model.update(null, errors);

	}


	/**
	 * Sets the title for this window while a build is running.
	 *
	 * @param tsConfig The tsconfig file we're building from.
	 */
	void setTitleWhileBuilding(File tsConfig) {
		String msg = plugin.getBundle().getString("TypeScript.Building");
		String title = MessageFormat.format(msg, tsConfig.getAbsolutePath());
		setDockableWindowTitle(title);
	}


	/**
	 * Sets the title of this window after a build has completed.
	 *
	 * @param rootDir The root directory of the TypeScript project.
	 * @param notices The list of notices returned by the compiler.
	 */
	private void setTitleAfterBuilding(File rootDir,
			List<ParserNotice> notices) {

		String dateStr = new SimpleDateFormat().format(new Date());

		int errorCount = 0;
		int warningCount = 0;
		for (ParserNotice notice : notices) {
			switch (notice.getLevel()) {
				case ERROR:
					errorCount++;
					break;
				case WARNING:
					warningCount++;
					break;
				case INFO:
					// Do nothing
					break;
			}
		}

		String msg = plugin.getBundle().getString("TypeScript.BuildComplete");
		String title = MessageFormat.format(msg, dateStr, errorCount,
				warningCount, rootDir.getAbsolutePath());
		setDockableWindowTitle(title);

	}


	private class TypeScriptNoticeTableModel extends ParserNoticeTableModel {

		TypeScriptNoticeTableModel(String lastColHeader) {
			super(lastColHeader);
		}

		@Override
		protected void addNoticesImpl(RTextEditorPane textArea,
				List<ParserNotice> notices) {
			for (ParserNotice notice : notices) {
				TypeScriptParserNotice tsNotice =
						(TypeScriptParserNotice)notice;
				Object[] data = {	getIcon(), tsNotice.getFileFullPath(),
					notice.getLine(),
						notice.getMessage() };
				addRow(data);
			}
		}

	}


}
