/*
 * 10/17/2009
 *
 * SpellingErrorWindow.java - A dockable window that lists spelling errors in
 * currently open files.
 * Copyright (C) 2009 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.*;

import org.fife.ui.RScrollPane;
import org.fife.ui.UIUtil;
import org.fife.ui.dockablewindows.DockableWindowConstants;
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


	SpellingErrorWindow(RText rtext) {

		super(rtext);
		AbstractMainView  mainView = rtext.getMainView();
		mainView.addPropertyChangeListener(AbstractMainView.TEXT_AREA_ADDED_PROPERTY, this);
		mainView.addPropertyChangeListener(AbstractMainView.TEXT_AREA_REMOVED_PROPERTY, this);

		model = new SpellingTableModel(rtext.getString("SpellingErrorList.Word"));
		JTable table = createTable(model);
		UIUtil.removeTabbedPaneFocusTraversalKeyBindings(table);
		RScrollPane sp = new DockableWindowScrollPane(table);
		UIUtil.removeTabbedPaneFocusTraversalKeyBindings(sp);

		setLayout(new BorderLayout());
		add(sp);

		setPosition(DockableWindowConstants.BOTTOM);
		setActive(true);
		setDockableWindowName(rtext.getString("SpellingErrorList.Spelling"));

		setIcon(getIconForAppTheme());
		rtext.addPropertyChangeListener(RText.ICON_STYLE_PROPERTY, this);

		// Start listening to any already-opened files.
		for (int i=0; i<mainView.getNumDocuments(); i++) {
			RTextEditorPane textArea = mainView.getRTextEditorPaneAt(i);
			List<ParserNotice> notices = textArea.getParserNotices();
			model.update(textArea, notices);
			textArea.addPropertyChangeListener(
						RSyntaxTextArea.PARSER_NOTICES_PROPERTY, this);
		}

	}


	private Icon getIconForAppTheme() {
		return getRText().getIconGroup().getIcon("spellcheck");
	}


	@Override
	public void propertyChange(PropertyChangeEvent e) {

		String prop = e.getPropertyName();

		if (RSyntaxTextArea.PARSER_NOTICES_PROPERTY.equals(prop)) {
			RTextEditorPane source = (RTextEditorPane)e.getSource();
			List<ParserNotice> notices = source.getParserNotices();//(List)e.getNewValue();
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

		else if (RText.ICON_STYLE_PROPERTY.equals(prop)) {
			setIcon(getIconForAppTheme());
		}

	}


	private class SpellingTableModel extends ParserNoticeTableModel {

		SpellingTableModel(String lastColHeader) {
			super(lastColHeader);
		}

		@Override
		protected void addNoticesImpl(RTextEditorPane textArea,
				List<ParserNotice> notices) {
			AbstractMainView view = getRText().getMainView();
			SpellingParser parser = view.getSpellingSupport().getSpellingParser();
			for (ParserNotice notice : notices) {
				if (notice.getParser()==parser) {
					Object[] data = { getIcon(), textArea,
						notice.getLine() + 1,
						notice.getMessage() };
					addRow(data);
				}
			}
		}

	}


}
