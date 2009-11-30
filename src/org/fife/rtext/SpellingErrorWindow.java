/*
 * 10/17/2009
 *
 * SpellingErrorWindow.java - A dockable window that lists spelling errors in
 * currently open files.
 * Copyright (C) 2009 Robert Futrell
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

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTable;

import org.fife.ui.RScrollPane;
import org.fife.ui.dockablewindows.DockableWindowScrollPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rsyntaxtextarea.spell.SpellingParser;


/**
 * A window that displays the spelling errors for the currently active
 * text area.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SpellingErrorWindow extends AbstractParserNoticeWindow
							implements PropertyChangeListener {

	private SpellingTableModel model;


	public SpellingErrorWindow(RText rtext) {

		super(rtext);
		AbstractMainView  mainView = rtext.getMainView();
		mainView.addPropertyChangeListener(AbstractMainView.TEXT_AREA_ADDED_PROPERTY, this);
		mainView.addPropertyChangeListener(AbstractMainView.TEXT_AREA_REMOVED_PROPERTY, this);

		model = new SpellingTableModel(rtext.getString("SpellingErrorList.Word"));
		JTable table = createTable(model);
		RScrollPane sp = new DockableWindowScrollPane(table);

		setLayout(new BorderLayout());
		add(sp);

		setPosition(BOTTOM);
		setActive(true);
		setDockableWindowName(rtext.getString("SpellingErrorList.Spelling"));

		URL url = getClass().getResource("graphics/spellcheck.png");
		setIcon(new ImageIcon(url));

		// Start listening to any already-opened files.
		for (int i=0; i<mainView.getNumDocuments(); i++) {
			RTextEditorPane textArea = mainView.getRTextEditorPaneAt(i);
			List notices = textArea.getParserNotices();
			model.update(textArea, notices);
			textArea.addPropertyChangeListener(
						RSyntaxTextArea.PARSER_NOTICES_PROPERTY, this);
		}

	}


	public void propertyChange(PropertyChangeEvent e) {

		String prop = e.getPropertyName();

		if (RSyntaxTextArea.PARSER_NOTICES_PROPERTY.equals(prop)) {
			RTextEditorPane source = (RTextEditorPane)e.getSource();
			List notices = source.getParserNotices();//(List)e.getNewValue();
			model.update(source, notices);
		}

		if (AbstractMainView.TEXT_AREA_ADDED_PROPERTY.equals(prop)) {
			RTextEditorPane textArea = (RTextEditorPane)e.getNewValue();
			textArea.addPropertyChangeListener(
							RSyntaxTextArea.PARSER_NOTICES_PROPERTY, this);
		}

		else if (AbstractMainView.TEXT_AREA_REMOVED_PROPERTY.equals(prop)) {
			RTextEditorPane textArea = (RTextEditorPane)e.getNewValue();
			textArea.removePropertyChangeListener(
							RSyntaxTextArea.PARSER_NOTICES_PROPERTY, this);
		}

	}


	private class SpellingTableModel extends ParserNoticeTableModel {

		public SpellingTableModel(String lastColHeader) {
			super(lastColHeader);
		}

		protected void addNoticesImpl(RTextEditorPane textArea, List notices) {
			AbstractMainView view = getRText().getMainView();
			SpellingParser parser = view.getSpellingSupport().getSpellingParser();
			for (Iterator i=notices.iterator(); i.hasNext(); ) {
				ParserNotice notice = (ParserNotice)i.next();
				if (notice.getParser()==parser) {
					Object[] data = { getIcon(), textArea,
						// Integer.intValue(notice.getValue()+1) // TODO: 1.5
						new Integer(notice.getLine()+1),
						notice.getMessage() };
					addRow(data);
				}
			}
		}

	}


}