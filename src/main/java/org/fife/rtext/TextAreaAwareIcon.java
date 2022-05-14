/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import org.fife.ui.rsyntaxtextarea.TextEditorPane;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * An icon capable of displaying informational "sub-icons" in the corners
 * of a main icon.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class TextAreaAwareIcon implements Icon, PropertyChangeListener {

	private Icon icon;
	private boolean paintModifiedMarker;

	TextAreaAwareIcon(TextEditorPane editorPane, Icon icon) {
		editorPane.addPropertyChangeListener(this);
		this.icon = icon;
	}

	@Override
	public int getIconHeight() {
		return icon.getIconHeight();
	}

	@Override
	public int getIconWidth() {
		return icon.getIconWidth();
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		icon.paintIcon(c, g, x, y);
		if (paintModifiedMarker) {
			g.setColor(Color.RED);
			g.fillRect(0, 0, getIconWidth(), getIconHeight());
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String propertyName = e.getPropertyName();
		if (TextEditorPane.DIRTY_PROPERTY.equals(propertyName)) {
			paintModifiedMarker = (Boolean)e.getNewValue();
		}
	}

}
