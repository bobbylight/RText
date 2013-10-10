/*
 * 11/14/2003
 *
 * StatusBar.java - The status bar used by RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.JLabel;

import org.fife.ui.StatusBarPanel;


/**
 * The status bar used by rtext.  Contains fields for:
 * <ul>
 *    <li>An informational/status message.
 *    <li>A line/column number.
 *    <li>An overwrite (insert) mode indicator.
 *    <li>A Caps Lock indicator.
 *    <li>A file "Read Only" mode indicator.
 * </ul>
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class StatusBar extends org.fife.ui.StatusBar
					implements PropertyChangeListener {

	private JLabel rowAndColumnIndicator;
	private JLabel overwriteModeIndicator;
	private JLabel capsLockIndicator;
	private JLabel readOnlyIndicator;

	private int row, column;

	private boolean rowColumnIndicatorVisible;

	private StatusBarPanel overwritePanel;
	private StatusBarPanel capsLockPanel;
	private StatusBarPanel readOnlyPanel;

	private String fileSaveSuccessfulText;
	private String openedFileText;

	// Hack: Sine row/column can change so frequently, we break apart
	// the row/column text in the status bar for speedy updating.
	private String rowColumnText1;
	private String rowColumnText2;
	private String rowColumnText3;
	// Buffer used for creating "row/column" message.  This is instantiated
	// once for performance.  Note that this assumes calls into this
	// StatusBar that update the row/column indicator are all from the
	// EDT thread.
	private StringBuilder rcBuf;


	/**
	 * Creates the status bar.
	 *
	 * @param defaultMessage The default status message for this status bar.
	 * @param showRowColumn If true, the row/column of the caret are displayed.
	 * @param newRow The initial value of the row that is displayed.
	 * @param newColumn The initial value fo the column that is displayed.
	 * @param overwriteModeEnabled If <code>true</code>, overwrite mode
	 *        indicator ("OVR") is enabled.
	 */
	public StatusBar(RText rtext, String defaultMessage, boolean showRowColumn,
				int newRow, int newColumn, boolean overwriteModeEnabled) {

		super(defaultMessage);

		ResourceBundle msg= ResourceBundle.getBundle(StatusBar.class.getName());

		// Initialize private variables.
		fileSaveSuccessfulText = msg.getString("FileSaveSuccessful");
		openedFileText = msg.getString("OpenedFile");
		initRowColumnTextStuff(msg);
		row = newRow;
		column = newColumn; // DON'T call setRowAndColumn() yet!
		rowAndColumnIndicator = new JLabel();
		rowColumnIndicatorVisible = !showRowColumn; // So next line works.
		setRowColumnIndicatorVisible(showRowColumn);
		readOnlyIndicator = createLabel(msg, "ReadOnlyIndicator");
		capsLockIndicator = createLabel(msg, "CapsLockIndicator");
		overwriteModeIndicator = createLabel(msg, "OverwriteModeIndicator");
		overwriteModeIndicator = createLabel(msg, "OverwriteModeIndicator");

		// Make the layout such that different items can be different sizes.
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;

		// Create a Read Only indicator.
		c.weightx = 0.0;
		readOnlyPanel = new StatusBarPanel(new BorderLayout(),
									readOnlyIndicator);
		readOnlyIndicator.setEnabled(false);
		addStatusBarComponent(readOnlyPanel, c);

		// Create a Caps lock indicator.
		c.weightx = 0.0;
		capsLockPanel = new StatusBarPanel(new BorderLayout(),
									capsLockIndicator);
		// On Mac OS X at least, the OS doesn't support getLockingKeyState().
		try {
			capsLockIndicator.setEnabled(Toolkit.getDefaultToolkit().
							getLockingKeyState(KeyEvent.VK_CAPS_LOCK));
		} catch (UnsupportedOperationException e) {
			capsLockIndicator.setText("-");
			setCapsLockIndicatorEnabled(false);
		}
		addStatusBarComponent(capsLockPanel, c);

		// Create and add a panel containing the overwrite/insert message.
		c.weightx = 0.0;
		overwritePanel = new StatusBarPanel(new BorderLayout(),
									overwriteModeIndicator);
		setOverwriteModeIndicatorEnabled(overwriteModeEnabled);
		addStatusBarComponent(overwritePanel, c);

		// Create and add a panel containing the row and column.
		c.weightx = 0.0;
		StatusBarPanel temp1 = new StatusBarPanel(new BorderLayout()) {
			@Override
			public Dimension getMinimumSize() {
				return new Dimension(50, super.getMinimumSize().height);
			}
			@Override
			public Dimension getPreferredSize() {
				Dimension preferredSize = super.getPreferredSize();
				return new Dimension(Math.max(50,preferredSize.width),
									preferredSize.height);
			}
		};
		temp1.add(rowAndColumnIndicator);
		addStatusBarComponent(temp1, c);

	}


	private static JLabel createLabel(ResourceBundle bundle, String key) {
		JLabel label = new JLabel(bundle.getString(key));
		label.setHorizontalAlignment(JLabel.CENTER);
		return label;
	}


	/**
	 * Decrements the value of column in row/column indicator.
	 */
	public void decColumn() {
		setRowAndColumn(row, Math.max(column-1, 0));
	}


	/**
	 * Decrements the value of row in row/column indicator.
	 */
	public void decRow() {
		setRowAndColumn(Math.max(row-1, 0), column);
	}


	/**
	 * Increments the value of column in row/column indicator.
	 */
	public void incColumn() {
		setRowAndColumn(row, column+1);
	}


	/**
	 * Increments the value of row in row/column indicator.
	 */
	public void incRow() {
		setRowAndColumn(row+1, column);
	}


	/**
	 * Dirty hack to avoid having to use MessageFormat.format() every
	 * time the caret's position changes.
	 *
	 * @param msg The resource bundle.
	 */
	private void initRowColumnTextStuff(ResourceBundle msg) {
		String rowColumnText = msg.getString("RowColumnIndicator");
		int rowPos = rowColumnText.indexOf("{0}");
		int colPos = rowColumnText.indexOf("{1}");
		int min = Math.min(rowPos, colPos);
		int max = Math.max(rowPos, colPos);
		rowColumnText1 = rowColumnText.substring(0, min);
		rowColumnText2 = rowColumnText.substring(min+3, max);
		rowColumnText3 = rowColumnText.substring(max+3);
		rcBuf = new StringBuilder();
	}


	/**
	 * Returns <code>true</code> if the caps lock indicator is enabled.
	 */
	public boolean isCapsLockIndicatorEnabled() {
		return capsLockIndicator.isEnabled();
	}


	/**
	 * Returns <code>true</code> if overwrite mode indicator is enabled.
	 */
	public boolean isOverwriteModeIndicatorEnabled() {
		return overwriteModeIndicator.isEnabled();
	}


	/**
	 * Returns <code>true</code> if the Read Only indicator is enabled.
	 */
	public boolean isReadOnlyIndicatorEnabled() {
		return readOnlyIndicator.isEnabled();
	}


	/**
	 * Returns whether or not the row/column indicator is visible.
	 */
	public boolean isRowColumnIndicatorVisible() {
		return rowColumnIndicatorVisible;
	}


	/**
	 * Called whenever a property changes on a component we're listening to.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String property = e.getPropertyName();

		// If a file was just saved...
		if (property.equals(RTextEditorPane.DIRTY_PROPERTY)) {
			boolean wasDirty = ((Boolean)e.getOldValue()).booleanValue();
			boolean isDirty = ((Boolean)e.getNewValue()).booleanValue();
			if (wasDirty && !isDirty)
				setStatusMessage(fileSaveSuccessfulText);
		}

		// If they opened a file...
		else if (property.equals(AbstractMainView.TEXT_AREA_ADDED_PROPERTY)) {
			RTextEditorPane textArea = (RTextEditorPane)e.getNewValue();
			setStatusMessage(MessageFormat.format(openedFileText,
									new Object[] { textArea.getFileName() }));
		}

		// If they saved a read-only file with a different filename (hence it
		// is now not read-only)...
		// NOTE:  We don't have the "else" below because we'll get both this
		// property change notification and a DIRTY_PROPERTY one.
		if (property.equals(RTextEditorPane.READ_ONLY_PROPERTY)) {
			boolean enabled = ((Boolean)e.getNewValue()).booleanValue();
			setReadOnlyIndicatorEnabled(enabled);
		}

	}


	/**
	 * Changes whether the caps lock indicator is enabled or disabled.  This
	 * should be called whenever the user presses CAPS LOCK, perhaps through a
	 * <code>KeyListener</code>.
	 *
	 * @param enabled If <code>true</code>, the caps indicator ("OVR") is
	 *        enabled; if <code>false</code>, it is disabled.
	 */
	public void setCapsLockIndicatorEnabled(boolean enabled) {
		capsLockIndicator.setEnabled(enabled);
	}


	/**
	 * Setter function for the column in row/column indicator.
	 *
	 * @param newColumn The column value to display for the caret.
	 */
	public void setColumn(int newColumn) {
		setRowAndColumn(row, newColumn);
	}


	/**
	 * Changes whether the overwrite indicator is enabled or disabled.
	 *
	 * @param enabled If <code>true</code>, the overwrite indicator ("OVR") is
	 *        enabled; if <code>false</code>, it is disabled.
	 */
	public void setOverwriteModeIndicatorEnabled(boolean enabled) {
		overwriteModeIndicator.setEnabled(enabled);
	}


	/**
	 * Changes whether the Read Only indicator is enabled or disabled.
	 *
	 * @param enabled If <code>true</code>, the read-only indicator is enabled;
	 *        if <code>false</code>, it is disabled.
	 */
	public void setReadOnlyIndicatorEnabled(boolean enabled) {
		readOnlyIndicator.setEnabled(enabled);
	}


	/**
	 * Setter function for the row in row/column indicator.
	 *
	 * @param newRow The row value to display for the caret.
	 */
	public void setRow(int newRow) {
		setRowAndColumn(newRow, column);
	}


	/**
	 * Setter function for row/column part of status bar.
	 *
	 * @param newRow The row value to display for the caret.
	 * @param newColumn The column value to display for the caret.
	 */
	public void setRowAndColumn(int newRow, int newColumn) {
		row = newRow;
		column = newColumn;
		updateRowColumnDisplay();
	}


	/**
	 * Enables or disables the row/column indicator.
	 *
	 * @param isVisible Whether or not the row/column indicator should be
	 *        visible.
	 */
	public void setRowColumnIndicatorVisible(boolean isVisible) {
		if (isVisible != rowColumnIndicatorVisible) {
			rowColumnIndicatorVisible = isVisible;
			if (isVisible)
				updateRowColumnDisplay();
			else
				rowAndColumnIndicator.setText("-");
		}
	}


	/**
	 * Updates the row/column indicator to reflect the current caret
	 * location, if it is enabled.
	 */
	private void updateRowColumnDisplay() {
		if (rowColumnIndicatorVisible) {
			//StringBuilder rcBuf = new StringBuilder(rowColumnText1);
			rcBuf.setLength(0);
			rcBuf.append(rowColumnText1).append(row).append(rowColumnText2);
			rcBuf.append(column).append(rowColumnText3);
			rowAndColumnIndicator.setText(rcBuf.toString());
		}
	}


}