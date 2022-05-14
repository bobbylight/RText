/*
 * 12/20/2014
 *
 * Copyright (C) 2014 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.filechooser.FileSystemView;

import org.fife.ui.UIUtil;
import org.fife.ui.rsyntaxtextarea.FileLocation;


/**
 * The cell renderer to use for files in the "recent files" dialog.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see RecentFileDialog
 */
class RecentFileListCellRenderer extends DefaultListCellRenderer {

	private static final FileSystemView FILE_SYSTEM_VIEW = FileSystemView.
			getFileSystemView();


	@Override
	public Component getListCellRendererComponent(JList list, Object value,
							int index, boolean selected, boolean hasFocus) {

		super.getListCellRendererComponent(list, value, index, selected,
				hasFocus);

		FileLocation loc = (FileLocation)value;
		setText(this, loc, selected);

		if (loc.isLocalAndExists()) {
			File file = new File(loc.getFileFullPath());
			setIcon(FILE_SYSTEM_VIEW.getSystemIcon(file));
		}

		return this;

	}


	/**
	 * Sets the text to display for a renderer.
	 *
	 * @param renderer The renderer.
	 * @param loc The file location being displayed.
	 */
	static void setText(DefaultListCellRenderer renderer, FileLocation loc,
			boolean selected) {
		String fileName = loc.getFileName();
		String fullPath = loc.getFileFullPath();
		String text = "<html>" + fileName + "<br>";
		if (!selected) {
			Color fg = renderer.getForeground();
			String color = UIUtil.isLightForeground(fg) ? "#c0c0c0" : "#808080";
			text += "<font color=\"" + color + "\">";
		}
		text += fullPath;
		if (!selected) {
			text += "</font>";
		}
		renderer.setText(text);
	}


}
