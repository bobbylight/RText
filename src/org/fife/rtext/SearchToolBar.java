/*
 * 11/01/2004
 *
 * SearchToolBar.java - Toolbar used by RText for quick searching.
 * Copyright (C) 2004 Robert Futrell
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

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonUI;

import org.fife.ui.rtextarea.SearchEngine;


/**
 * The toolbar used by RText for quick searching.
 *
 * @author Robert Futrell
 * @version 0.5
 */
class SearchToolBar extends JToolBar {

	private RText owner;

	private JTextField findField;
	private JButton findButton;
	private JButton findPrevButton;
	private JCheckBox matchCaseCheckBox;
	private JLabel infoLabel;
	private String textNotFound;


	/**
	 * Creates the tool bar.
	 *
	 * @param title The title of the toolbar.
	 * @param rtext The parent RText instance.
	 * @param mouseListener The status bar that listens for mouse-over events.
	 */
	public SearchToolBar(String title, final RText rtext,
					StatusBar mouseListener) {

		super(title);

		this.owner = rtext;
		ResourceBundle msg = ResourceBundle.getBundle(
								"org.fife.rtext.QuickSearchBar");

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		FindButtonListener findButtonListener = new FindButtonListener();
		FindFieldListener findFieldListener = new FindFieldListener();

		ClassLoader cl = this.getClass().getClassLoader();
		Icon icon = null;

		add(Box.createHorizontalStrut(5));

		FindButton hideButton = null;
		try {
			hideButton = new FindButton(new ImageIcon(
				cl.getResource("org/fife/rtext/graphics/close.gif")));
		} catch (Exception e) {
			hideButton = new FindButton("x");
		}
		hideButton.setBorderPainted(false);
		hideButton.setMargin(new Insets(0,0,0,0));
		try {
			icon = new ImageIcon(cl.getResource(
				"org/fife/rtext/graphics/close_rollover.gif"));
			hideButton.setRolloverEnabled(true);
			hideButton.setRolloverIcon(icon);
			hideButton.setPressedIcon(icon);
		} catch (Exception e) {
			e.printStackTrace(); // Never happens
		}
		hideButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				((RTextMenuBar)rtext.getJMenuBar()).
					setSearchToolbarMenuItemSelected(false);
			}
		});
		add(hideButton);

		add(Box.createHorizontalStrut(5));

		JLabel label = new JLabel(msg.getString("Find"));
		add(label);

		findField = new JTextField(20) {
			public Dimension getMinimumSize() {
				Dimension size = super.getMinimumSize();
				size.width = 200;
				return size;
			}
			public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				size.width = 200;
				return size;
			}
			public Dimension getMaximumSize() {
				Dimension max = super.getMaximumSize();
				max.width = 200;
				return max;
			}
		};
		// OS X-specific property (requires Java 5+).  Causes the text field
		// to be painted in OS X's "Search field" style.
		findField.putClientProperty("JTextField.variant", "search");
		findField.getAccessibleContext().setAccessibleDescription(
								msg.getString("FindFieldDesc"));
		findField.addMouseListener(mouseListener);
		findField.getDocument().addDocumentListener(findFieldListener);
		findField.addKeyListener(findFieldListener);
		findField.addFocusListener(findFieldListener);
		add(findField);

		add(Box.createHorizontalStrut(5));

		Icon nextIcon = null;
		Icon prevIcon = null;
		try {
			nextIcon = new ImageIcon(
				cl.getResource("org/fife/rtext/graphics/right.gif"));
			prevIcon = new ImageIcon(
				cl.getResource("org/fife/rtext/graphics/left.gif"));
			// Switch arrow directions for "Find Next" and "Prev" if RTL.
			if (!orientation.isLeftToRight()) {
				icon = nextIcon;
				nextIcon = prevIcon;
				prevIcon = icon;
			}
		} catch (Exception e) {
			rtext.displayException(e);
		}
		findButton = new FindButton(msg.getString("FindNext"), nextIcon);
		findButton.setActionCommand("FindNext");
		findButton.getAccessibleContext().setAccessibleDescription(
								msg.getString("DescFindNext"));
		findButton.addMouseListener(mouseListener);
		findButton.addActionListener(findButtonListener);
		findButton.setEnabled(false);
		add(findButton);

		add(Box.createHorizontalStrut(5));

		findPrevButton = new FindButton(msg.getString("FindPrev"), prevIcon);
		findPrevButton.getAccessibleContext().setAccessibleDescription(
								msg.getString("DescFindPrev"));
		findPrevButton.addMouseListener(mouseListener);
		findPrevButton.setActionCommand("FindPrevious");
		findPrevButton.addActionListener(findButtonListener);
		findPrevButton.setEnabled(false);
		add(findPrevButton);

		add(Box.createHorizontalStrut(5));

		matchCaseCheckBox = new JCheckBox(msg.getString("MatchCase"));
		add(matchCaseCheckBox);

		add(Box.createHorizontalStrut(15));

		infoLabel = new JLabel("");
		textNotFound = msg.getString("TextNotFound");
		add(infoLabel);

		// Get ready to go.
		setFloatable(false);
		applyComponentOrientation(orientation);

	}


	/**
	 * Makes the find field on this toolbar request focus.  If it is already
	 * focused, its text is selected.
	 */
	public void focusFindField() {
		if (findField.isFocusOwner()) {
			findField.selectAll();
		}
		else {
			findField.requestFocusInWindow();
		}
	}


	/**
	 * Special button used on QuickSearch toolbar.
	 */
	private static class FindButton extends JButton {

		public FindButton(String text) {
			super(text);
		}

		public FindButton(String text, Icon icon) {
			super(text, icon);
		}

		public FindButton(Icon icon) {
			super(icon);
		}

		public void setUI(ButtonUI ui) {
			super.setUI(new FindButtonUI());
		}

	}


	/**
	 * Listens for the user clicking on the Find button.
	 */
	class FindButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();
			boolean forward = true;
			if (actionCommand.equals("FindNext")) {
				forward = true;
			}
			else {//if (actionCommand.equals("FindPrevious")) {
				forward = false;
			}
			String text = findField.getText();
			RTextEditorPane textArea = owner.getMainView().currentTextArea;
			boolean found = SearchEngine.find(textArea, text, forward,
									matchCaseCheckBox.isSelected(),
									false, false);
			if (found) {
				infoLabel.setText("");
			}
			else {
				infoLabel.setForeground(Color.RED);
				infoLabel.setText(textNotFound);
				UIManager.getLookAndFeel().provideErrorFeedback(findField);
			}
			if (!findField.isFocusOwner()) {
				findField.grabFocus();
			}
		}

	}



	/**
	 * UI used by the buttons on this toolbar.
	 */
	private static class FindButtonUI extends BasicButtonUI {

		private Rectangle viewRect = new Rectangle(0,0,0,0);
		private Rectangle iconRect = new Rectangle(0,0,0,0);
		private Rectangle textRect = new Rectangle(0,0,0,0);
		private MouseInputHandler mouseInputHandler;
		private boolean isArmed;
		private boolean isMouseOver;
		private static boolean someButtonDepressed;

		protected void installListeners(AbstractButton b) {
			super.installListeners(b);
			mouseInputHandler = new MouseInputHandler(b);
			b.addMouseListener(mouseInputHandler);
		}

		protected boolean isArmed() {
			return isArmed;
		}

		protected boolean isMouseOver() {
			return isMouseOver;
		}

		public void paint(Graphics g, JComponent c)  {

			AbstractButton b = (AbstractButton) c;
			Font f = c.getFont();
			g.setFont(f);
			FontMetrics fm = b.getFontMetrics(b.getFont());
			Insets i = c.getInsets();
			viewRect.x = i.left;
			viewRect.y = i.top;
			viewRect.width = b.getWidth() - (i.right + viewRect.x);
			viewRect.height = b.getHeight() - (i.bottom + viewRect.y);
			textRect.x = textRect.y = textRect.width = textRect.height = 0;
			iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;

			// layout the text and icon
			String text = SwingUtilities.layoutCompoundLabel(
				c, fm, b.getText(), b.getIcon(), 
				b.getVerticalAlignment(), b.getHorizontalAlignment(),
				b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
				viewRect, iconRect, textRect, 
				b.getText() == null ? 0 : b.getIconTextGap());

			paintBackground(g, b);

			// Paint the Icon
			if(b.getIcon() != null)
				paintIcon(g,c,iconRect);

			if (text != null && !text.equals(""))
				paintText(g, b, textRect, text);

			if (b.isFocusPainted() && b.hasFocus()) {
				// paint UI specific focus
				paintFocus(g,b,viewRect,textRect,iconRect);
			}

		}

		public void paintBackground(Graphics g, AbstractButton b) {
			ButtonModel model = b.getModel();
			int width = b.getWidth() - 1;
			int height = b.getHeight() - 1;
			// This case is when the button isn't even enabled.
			if (!model.isEnabled()) {
				/* Do nothing. */
			}
			// This case is when the user has down-clicked, and the mouse is
			// still over the button.
			else if (model.isArmed()) {
				String text = b.getText();
				if (text!=null && !text.equals("")) {
					g.setColor(Color.BLACK);
					g.drawRect(0,0, width, height);
					textRect.translate(1,1);
				}
				iconRect.translate(1,1);
			}
			// This case is when the mouse is hovering over the button,
			// or they've left-clicked, but moved the mouse off the button.
			else if (isMouseOver() || isArmed()) {
				String text = b.getText();
				if (text!=null && !text.equals("")) {
					g.setColor(Color.BLACK);
					g.drawRect(0,0, width, height);
				}
			}
		}

		protected void setArmed(boolean armed) {
			isArmed = armed;
		}

		protected void setMouseOver(boolean over) {
			isMouseOver = over;
		}

		protected void uninstallListeners(AbstractButton b) {
			b.removeMouseListener(mouseInputHandler);
			super.uninstallListeners(b);
		}

		protected class MouseInputHandler extends MouseInputAdapter {

			private AbstractButton button;

			public MouseInputHandler(AbstractButton b) {
				button = b;
			}

			public void mousePressed(MouseEvent e) {
				someButtonDepressed = true;
				if (isMouseOver())
					setArmed(true);
			}

			public void mouseReleased(MouseEvent e) {
				someButtonDepressed = false;
				setArmed(false);
			}

			public void mouseEntered(MouseEvent e) {
				if (!someButtonDepressed) {
					setMouseOver(true);
					button.repaint();
				}
			}

			public void mouseExited(MouseEvent e) {
				setMouseOver(false);
				button.repaint();
			}

		}

	}


	/**
	 * Listens for the user typing into the search field.
	 */
	class FindFieldListener extends KeyAdapter
					implements DocumentListener, FocusListener {

		public void changedUpdate(DocumentEvent e) {
		}

		public void focusGained(FocusEvent e) {
			findField.selectAll();
		}

		public void focusLost(FocusEvent e) {
		}

		public void insertUpdate(DocumentEvent e) {
			if (!findButton.isEnabled()) {
				findButton.setEnabled(true);
				findPrevButton.setEnabled(true);
			}
		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_ESCAPE) {
				owner.getMainView().currentTextArea.
						requestFocusInWindow();
			}
		}

		public void keyTyped(KeyEvent e) {
			if (e.getKeyChar()=='\n') {
				int mod = e.getModifiers();
				if ((mod&InputEvent.CTRL_MASK)>0)
					findPrevButton.doClick(0);
				else
					findButton.doClick(0);
			}
		}

		public void removeUpdate(DocumentEvent e) {
			if (e.getDocument().getLength()==0) {
				findButton.setEnabled(false);
				findPrevButton.setEnabled(false);
			}
		}

	}


}