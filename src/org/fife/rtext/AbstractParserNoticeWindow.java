package org.fife.rtext;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;

import org.fife.ui.FileExplorerTableModel;
import org.fife.ui.dockablewindows.DockableWindow;


/**
 * Base class for dockable windows containing parser notices.
 *
 * @author Robert Futrell
 * @version 1.0
 */
abstract class AbstractParserNoticeWindow extends DockableWindow {

	private RText rtext;
	private JTable table;


	public AbstractParserNoticeWindow(RText rtext) {
		this.rtext = rtext;
	}


	protected JTable createTable(TableModel model) {

		table = new JTable();
		FileExplorerTableModel model2 = new FileExplorerTableModel(model,
													table.getTableHeader());

		model2.setColumnComparator(Integer.class, new Comparator() {	
			public int compare(Object o1, Object o2) {
				Integer int1 = (Integer)o1;
				Integer int2 = (Integer)o2;
				return int1.compareTo(int2);
			}
		});

		model2.setColumnComparator(RTextEditorPane.class, new Comparator() {	
			public int compare(Object o1, Object o2) {
				RTextEditorPane ta1 = (RTextEditorPane)o1;
				RTextEditorPane ta2 = (RTextEditorPane)o2;
				return ta1.getFileName().compareTo(ta2.getFileName());
			}
		});

		table.setDefaultRenderer(Icon.class, new IconTableCellRenderer());
		table.setDefaultRenderer(RTextEditorPane.class,
				new TextAreaTableCellRenderer());

		table.setModel(model2);

		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(32);
		tcm.getColumn(0).setWidth(32);
		tcm.getColumn(1).setPreferredWidth(200);
		tcm.getColumn(1).setWidth(200);
		tcm.getColumn(2).setPreferredWidth(48);
		tcm.getColumn(2).setWidth(48);
		tcm.getColumn(3).setPreferredWidth(800);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		table.addMouseListener(new TableMouseListener());

		return table;

	}


	/**
	 * Basic model for tables displaying parser notices.
	 */
	protected abstract class ParserNoticeTableModel extends DefaultTableModel {

		public ParserNoticeTableModel(String lastColHeader) {
			String[] colHeaders = new String[] { "", "File", "Line", lastColHeader, }; // TODO
			setColumnIdentifiers(colHeaders);
		}

		protected abstract void addNoticesImpl(RTextEditorPane textArea,
												List notices);

		public Class getColumnClass(int col) {
			Class clazz = null;
			switch (col) {
				case 0:
					clazz = Icon.class;
					break;
				case 1:
					clazz = RTextEditorPane.class;
					break;
				case 2:
					clazz = Integer.class;
					break;
				default:
					clazz = super.getColumnClass(col);
			}
			return clazz;
		}
			
		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void update(RTextEditorPane textArea, List notices) {
			//setRowCount(0);
			for (int i=0; i<getRowCount(); i++) {
				RTextEditorPane textArea2 = (RTextEditorPane)getValueAt(i, 1);
				if (textArea2==textArea) {
					removeRow(i);
					i--;
				}
			}
			if (notices!=null) {
				addNoticesImpl(textArea, notices);
			}
		}

	}


	/**
	 * A table cell renderer for icons.
	 */
	private static class IconTableCellRenderer extends DefaultTableCellRenderer{

		static final Border b = BorderFactory.createEmptyBorder(0, 5, 0, 5);

		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean selected, boolean focus, int row, int col) {
			super.getTableCellRendererComponent(table, value, selected, focus,
					row, col);
			setText(null);
			setIcon((Icon)value);
			setBorder(b);
			return this;
		}

	}


	/**
	 * Renders a text area in a table cell as just the file name.
	 */
	private static class TextAreaTableCellRenderer
					extends DefaultTableCellRenderer {

		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean selected, boolean focus, int row, int col) {
			super.getTableCellRendererComponent(table, value, selected, focus,
					row, col);
			RTextEditorPane textArea = (RTextEditorPane)value;
			setText(textArea.getFileName());
			return this;
		}

	}


	private class TableMouseListener extends MouseAdapter {

		public void mouseClicked(MouseEvent e) {

			if (e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==2) {
				int row = table.rowAtPoint(e.getPoint());
				if (row>-1) {
					RTextEditorPane textArea =
						(RTextEditorPane)table.getValueAt(row, 1);
					AbstractMainView mainView = rtext.getMainView();
					if (mainView.setSelectedTextArea(textArea)) {
						Integer i = (Integer)table.getValueAt(row, 2);
						int line = i.intValue();
						try {
							textArea.setCaretPosition(
								textArea.getLineStartOffset(line));
						} catch (BadLocationException ble) {
							UIManager.getLookAndFeel().
										provideErrorFeedback(textArea);
						}
						textArea.requestFocusInWindow();
					}
				}
			}

		}

	}


}