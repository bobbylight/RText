/*
 * 02/10/2006
 *
 * EditorPaneCreateDelegate.java - Creates instances of RTExtEditorPane.
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
package org.fife.rtext;

import java.io.IOException;

import org.fife.ui.rsyntaxtextarea.FileLocation;


/**
 * An implementation of this interface is capable of creating
 * <code>RTextEditorPane</code>s.  Classes can implement this interface and
 * be passed into RText's main view when creating a new text area to
 * create that text area with special features, etc.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface EditorPaneCreatorDelegate {


	/**
	 * Returns an editor pane to use in RText.<p>
	 *
	 * Note that this method does not have to actually open the file,
	 * configure syntax highlighting, etc.  All of that is done by RText.
	 * This method merely has to set the four properties set to it (and
	 * do any special configuration/add any features RText won't do).
	 *
	 * @param owner The parent RText instance.
	 * @param lineWrap whether or not line wrap is enabled.
	 * @param textMode Either <code>RTextEditorPane.INSERT_MODE</code> or
	 *        <code>RTextEditorPane.OVERWRITE_MODE</code>.
	 * @param loc Location of the file being opened.
	 * @param encoding The encoding of the file.
	 * @throws IOException If an IO error occurs reading the file to load.
	 */
	public RTextEditorPane createRTextEditorPane(RText owner,
					boolean lineWrap, int textMode,
					FileLocation loc, String encoding) throws IOException;


}