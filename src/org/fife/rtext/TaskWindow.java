/*
 * 10/17/2009
 *
 * TaskWindow.java - A dockable window that lists tasks (todo's, fixme's, etc.)
 * in open files.
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
import org.fife.ui.rsyntaxtextarea.parser.TaskTagParser;


/**
 * A window that displays the "todo" and "fixme" items in all open files.
 * Parsing for tasks is only done if this window is visible.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class TaskWindow extends AbstractParserNoticeWindow
				implements PropertyChangeListener {

	private JTable table;
	private TaskNoticeTableModel model;
	private TaskTagParser taskParser;


	public TaskWindow(RText rtext) {

		super(rtext);

		model = new TaskNoticeTableModel(rtext.getString("TaskList.Task"));
		table = createTable(model);
		RScrollPane sp = new DockableWindowScrollPane(table);

		setLayout(new BorderLayout());
		add(sp);

		setPosition(BOTTOM);
		setActive(true);
		setDockableWindowName(rtext.getString("TaskList.Tasks"));

		URL url = getClass().getResource("graphics/page_white_edit.png");
		setIcon(new ImageIcon(url));

		taskParser = new TaskTagParser();

	}

	/**
	 * Overridden to add parsing listeners when this window is visible.
	 */
	public void addNotify() {

		super.addNotify();

		RText rtext = getRText();
		AbstractMainView mainView = rtext.getMainView();
		mainView.addPropertyChangeListener(AbstractMainView.TEXT_AREA_ADDED_PROPERTY, this);
		mainView.addPropertyChangeListener(AbstractMainView.TEXT_AREA_REMOVED_PROPERTY, this);
		for (int i=0; i<mainView.getNumDocuments(); i++) {
			RTextEditorPane textArea = mainView.getRTextEditorPaneAt(i);
			addTaskParser(textArea);
		}

	}


	/**
	 * Adds listeners to start parsing a text area for tasks.
	 *
	 * @param textArea The text area to start parsing for tasks.
	 * @see #removeTaskParser(RTextEditorPane)
	 */
	private void addTaskParser(RTextEditorPane textArea) {
		textArea.addPropertyChangeListener(
				RSyntaxTextArea.PARSER_NOTICES_PROPERTY, this);
		textArea.addParser(taskParser);
	}


	/**
	 * Notified when a text area is parsed, or when a text area is added or
	 * removed (so listeners can be added/removed as appropriate).
	 *
	 * @param e The event.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String prop = e.getPropertyName();

		// A text area has been re-parsed for tasks.
		if (RSyntaxTextArea.PARSER_NOTICES_PROPERTY.equals(prop)) {
			RTextEditorPane source = (RTextEditorPane)e.getSource();
			List notices = source.getParserNotices();//(List)e.getNewValue();
			model.update(source, notices);
		}

		if (AbstractMainView.TEXT_AREA_ADDED_PROPERTY.equals(prop)) {
			RTextEditorPane textArea = (RTextEditorPane)e.getNewValue();
			addTaskParser(textArea);
		}

		else if (AbstractMainView.TEXT_AREA_REMOVED_PROPERTY.equals(prop)) {
			RTextEditorPane textArea = (RTextEditorPane)e.getNewValue();
			textArea.removeParser(taskParser);
			textArea.removePropertyChangeListener(
							RSyntaxTextArea.PARSER_NOTICES_PROPERTY, this);
		}

	}


	/**
	 * Overridden to remove all parsing listeners when this window is not
	 * visible.
	 */
	public void removeNotify() {

		super.removeNotify();

		RText rtext = getRText();
		AbstractMainView mainView = rtext.getMainView();
		mainView.removePropertyChangeListener(AbstractMainView.TEXT_AREA_ADDED_PROPERTY, this);
		mainView.removePropertyChangeListener(AbstractMainView.TEXT_AREA_REMOVED_PROPERTY, this);
		for (int i=0; i<mainView.getNumDocuments(); i++) {
			RTextEditorPane textArea = mainView.getRTextEditorPaneAt(i);
			removeTaskParser(textArea);
		}
		model.setRowCount(0);

	}


	/**
	 * Removes the listeners that parse a text area for tasks.
	 *
	 * @param textArea The text area to stop parsing for tasks.
	 * @see #addTaskParser(RTextEditorPane)
	 */
	private void removeTaskParser(RTextEditorPane textArea) {
		textArea.removePropertyChangeListener(
				RSyntaxTextArea.PARSER_NOTICES_PROPERTY, this);
		textArea.removeParser(taskParser);
	}


	private class TaskNoticeTableModel extends ParserNoticeTableModel {

		public TaskNoticeTableModel(String lastColHeader) {
			super(lastColHeader);
		}

		protected void addNoticesImpl(RTextEditorPane textArea, List notices) {
			for (Iterator i=notices.iterator(); i.hasNext(); ) {
				ParserNotice notice = (ParserNotice)i.next();
				if (notice.getParser()==taskParser) {
					Object[] data = {	getIcon(), textArea,
							// Integer.intValue(notice.getValue()+1) // TODO: 1.5
							new Integer(notice.getLine()+1),
							notice.getMessage() };
					addRow(data);
				}
			}
		}
		
	}


}