/*
 * 06/13/2009
 *
 * AssistanceIconPanel.java - A panel that sits alongside a text component,
 * that can display assistance icons for that component.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;


/**
 * A panel meant to be displayed alongside a text component, that can display
 * assistance icons for that text component.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class AssistanceIconPanel extends DecorativeIconPanel
						implements FocusListener, PropertyChangeListener {

	/**
	 * The tooltip text for the light bulb icon.  It is assumed that access
	 * to this field is single-threaded (on the EDT).
	 */
	private static String ASSISTANCE_AVAILABLE;


	/**
	 * Constructor.
	 *
	 * @param comp The component to listen to.  This can be <code>null</code>
	 *        to create a "filler" icon panel for alignment purposes.  
	 */
	public AssistanceIconPanel(JComponent comp) {

		// null can be passed to make a "filler" icon panel for alignment
		// purposes.
		if (comp!=null) {

			if (comp instanceof JComboBox) {
				JComboBox combo = (JComboBox)comp;
				Component c = combo.getEditor().getEditorComponent();
				if (c instanceof JTextComponent) { // Always true
					JTextComponent tc = (JTextComponent)c;
					tc.addFocusListener(this);
				}
			}
			else { // Usually a JTextComponent
				comp.addFocusListener(this);
			}

			comp.addPropertyChangeListener(
				ContentAssistable.ASSISTANCE_IMAGE, this);

		}

	}


	/**
	 * Called when the combo box or text component gains focus.
	 *
	 * @param e The focus event.
	 */
	public void focusGained(FocusEvent e) {
		setShowIcon(true);
	}


	/**
	 * Called when the combo box or text component loses focus.
	 *
	 * @param e The focus event.
	 */
	public void focusLost(FocusEvent e) {
		setShowIcon(false);
	}


	/**
	 * Returns the "Content Assist Available" tooltip text for the light bulb
	 * icon.  It is assumed that this method is only called on the EDT.
	 *
	 * @return The text.
	 */
	static String getAssistanceAvailableText() {
		if (ASSISTANCE_AVAILABLE==null) {
			ResourceBundle msg = ResourceBundle.getBundle(
											"org.fife.ui.search.Search");
			ASSISTANCE_AVAILABLE = msg.getString("ContentAssistAvailable");
		}
		return ASSISTANCE_AVAILABLE;
	}


	/**
	 * Called when the property
	 * {@link ContentAssistable#ASSISTANCE_IMAGE} is fired by the component
	 * we are listening to.
	 *
	 * @param e The change event.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		Image img = (Image)e.getNewValue();
		setAssistanceEnabled(img);
	}


	/**
	 * A hook for applications to initialize this panel, if the component
	 * we're listening to already has content assist enabled.
	 *
	 * @param img The image to display, or <code>null</code> if content assist
	 *        is not currently available.
	 */
	public void setAssistanceEnabled(Image img) {
		if (img==null && getIcon()!=EMPTY_ICON) {
			setIcon(EMPTY_ICON);
			setToolTipText(null);
		}
		else {
			setIcon(new ImageIcon(img));
			setToolTipText(getAssistanceAvailableText());
		}
	}


}