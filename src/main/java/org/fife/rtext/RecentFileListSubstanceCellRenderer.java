/*
 * 12/20/2014
 *
 * Copyright (C) 2014 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.Component;
import java.io.File;
import javax.swing.JList;
import javax.swing.filechooser.FileSystemView;

import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.pushingpixels.substance.api.renderer.SubstanceDefaultListCellRenderer;


/**
 * The cell renderer to use for files in the "recent files" dialog when using
 * a Substance Look and Feel.  I love how Substance makes this stuff a pain in
 * the ass.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see RecentFileDialog
 * @see RecentFileListCellRenderer
 */
class RecentFileListSubstanceCellRenderer
		extends SubstanceDefaultListCellRenderer {

	private static final FileSystemView FILE_SYSTEM_VIEW = FileSystemView.
			getFileSystemView();


	@Override
	public Component getListCellRendererComponent(JList list, Object value,
							int index, boolean selected, boolean hasFocus) {

		super.getListCellRendererComponent(list, value, index, selected,
				hasFocus);

		FileLocation loc = (FileLocation)value;
		RecentFileListCellRenderer.setText(this, loc, selected);

		if (loc.isLocalAndExists()) {
			File file = new File(loc.getFileFullPath());
			setIcon(FILE_SYSTEM_VIEW.getSystemIcon(file));
		}

		return this;

	}


}
