/*
 * 10/17/2009
 *
 * TaskWindow.java - A dockable window that lists tasks (todo's, fixme's, etc.)
 * in open files.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tasks;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTable;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.AbstractParserNoticeWindow;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.RScrollPane;
import org.fife.ui.dockablewindows.DockableWindowScrollPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.TaskTagParser;


/**
 * A window that displays text flagged in source code comments that have
 * been designated as "tasks."  Tasks are identified by configurable
 * identifiers, such as "<code>FIXME</code>", "<code>TODO</code>" and
 * "<code>HACK</code>".<p>
 *
 * Parsing for tasks is only done if the tasks window is visible.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class TaskWindow extends AbstractParserNoticeWindow
				implements PropertyChangeListener {

	private JTable table;
	private TaskNoticeTableModel model;
	private TaskTagParser taskParser;
	private boolean installed;


	public TaskWindow(RText rtext, String taskIdentifiers) {

		super(rtext);
		installed = false;

		model = new TaskNoticeTableModel(rtext.getString("TaskList.Task"));
		table = createTable(model);
		RTextUtilities.removeTabbedPaneFocusTraversalKeyBindings(table);
		RScrollPane sp = new DockableWindowScrollPane(table);
		RTextUtilities.removeTabbedPaneFocusTraversalKeyBindings(sp);

		setLayout(new BorderLayout());
		add(sp);

		// active and position are set by caller, from TasksPrefs
		setDockableWindowName(rtext.getString("TaskList.Tasks"));

		URL url = getClass().getResource("page_white_edit.png");
		setIcon(new ImageIcon(url));

		taskParser = new TaskTagParser();
		setTaskIdentifiers(taskIdentifiers);

		applyComponentOrientation(rtext.getComponentOrientation());

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
	 * Returns the identifiers scanned for to identify "tasks" (e.g.
	 * "<code>TODO</code>", "<code>FIXME</code>", "<code>IDEA</code>", etc.).
	 *
	 * @return The identifiers.  This will always return a value, even it task
	 *         parsing is disabled.  If there are no identifiers, an empty
	 *         string is returned.
	 * @see #setTaskIdentifiers(String)
	 */
	public String getTaskIdentifiers() {
		String pattern = taskParser.getTaskPattern();
		if (pattern!=null) {
			pattern = pattern.replaceAll("\\\\\\?", "?");
		}
		else {
			pattern = "";
		}
		return pattern;
	}


	/**
	 * @see #uninstallParser()
	 */
	private void installParser() {
		if (!installed) {
			RText rtext = getRText();
			AbstractMainView mainView = rtext.getMainView();
			mainView.addPropertyChangeListener(AbstractMainView.TEXT_AREA_ADDED_PROPERTY, this);
			mainView.addPropertyChangeListener(AbstractMainView.TEXT_AREA_REMOVED_PROPERTY, this);
			for (int i=0; i<mainView.getNumDocuments(); i++) {
				RTextEditorPane textArea = mainView.getRTextEditorPaneAt(i);
				addTaskParser(textArea);
			}
			installed = true;
		}
	}


	/**
	 * Returns whether a parser is the task parser.
	 *
	 * @param parser The parser to check.
	 * @return Whether the parser is the task parser.
	 */
	public boolean isTaskParser(Parser parser) {
		return parser==taskParser;
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
			List<ParserNotice> notices = source.getParserNotices();//(List)e.getNewValue();
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


	/**
	 * Overridden to disable the task parser when the task window isn't active
	 * (visible).
	 *
	 * @param active Whether the task window should be active.
	 */
	@Override
	public void setActive(boolean active) {
		if (active!=isActive()) {
			super.setActive(active);
			if (active && !installed) {
				installParser();
			}
			else if (!active && installed) {
				uninstallParser();
			}
		}
	}


	/**
	 * Sets the identifiers scanned for when locating tasks.
	 *
	 * @param identifiers The identifiers, separated by the '<code>|</code>'
	 *        character.
	 * @return Whether the task pattern was actually modified.  This will be
	 *         <code>false</code> if <code>identifiers</code> is the same as
	 *         the current value.
	 * @see #getTaskIdentifiers()
	 */
	public boolean setTaskIdentifiers(String identifiers) {
		if (!identifiers.equals(getTaskIdentifiers())) {
			identifiers = identifiers.replaceAll("\\?", "\\\\\\?");
			taskParser.setTaskPattern(identifiers);
			return true;
		}
		return false;
	}


	/**
	 * @see #installParser()
	 */
	private void uninstallParser() {
		if (installed) {
			RText rtext = getRText();
			AbstractMainView mainView = rtext.getMainView();
			mainView.removePropertyChangeListener(AbstractMainView.TEXT_AREA_ADDED_PROPERTY, this);
			mainView.removePropertyChangeListener(AbstractMainView.TEXT_AREA_REMOVED_PROPERTY, this);
			for (int i=0; i<mainView.getNumDocuments(); i++) {
				RTextEditorPane textArea = mainView.getRTextEditorPaneAt(i);
				removeTaskParser(textArea);
			}
			model.setRowCount(0);
			installed = false;
		}
	}


	private class TaskNoticeTableModel extends ParserNoticeTableModel {

		public TaskNoticeTableModel(String lastColHeader) {
			super(lastColHeader);
		}

		@Override
		protected void addNoticesImpl(RTextEditorPane textArea, List<ParserNotice> notices) {
			for (ParserNotice notice : notices) {
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