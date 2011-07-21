/*
 * 07/20/2011
 *
 * JspOptionsPanel.java - Options for JSP language support.
 * Copyright (C) 2010 Robert Futrell
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
package org.fife.rtext.plugins.langsupport;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.border.Border;

import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;



/**
 * Options panel for JSP code completion.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class JspOptionsPanel extends OptionsDialogPanel {


	/**
	 * Constructor.
	 */
	public JspOptionsPanel() {

		ResourceBundle msg = Plugin.msg;
		setName(msg.getString("Options.Jsp.Name"));
//		listener = new Listener();
		setIcon(new ImageIcon(getClass().getResource("page_white_code_red.png")));

		ComponentOrientation o = ComponentOrientation.
											getOrientation(getLocale());

		setLayout(new BorderLayout());
		Border empty5Border = UIUtil.getEmpty5Border();
		setBorder(empty5Border);

		Box cp = Box.createVerticalBox();
		cp.setBorder(null);
		add(cp, BorderLayout.NORTH);

		Box box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(msg.
				getString("Options.General.Section.General")));
		cp.add(box);
		cp.add(Box.createVerticalStrut(5));

		// TODO: Add more stuff

		cp.add(Box.createVerticalGlue());

		applyComponentOrientation(o);

	}


	protected void doApplyImpl(Frame owner) {
		// TODO Auto-generated method stub

	}


	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		// TODO Auto-generated method stub
		return null;
	}


	public JComponent getTopJComponent() {
		// TODO Auto-generated method stub
		return null;
	}


	protected void setValuesImpl(Frame owner) {
		// TODO Auto-generated method stub

	}


}