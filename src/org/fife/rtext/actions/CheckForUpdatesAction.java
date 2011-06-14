/*
 * CheckForUpdatesAction - Action that checks whether there are updates to
 * RText.
 * Copyright (C) 2011 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.Icon;

import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;


/**
 * An action that checks a simple web service for the latest RText version.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class CheckForUpdatesAction extends StandardAction {

	/**
	 * The URL to contact to see if there is a newer RText release.
	 */
	private static final String CHECK_URL = "http://fifesoft.com/rtext/latest.properties";


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public CheckForUpdatesAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "CheckForUpdates");
		setIcon(icon);
	}



	public void actionPerformed(ActionEvent e) {
		try {
			URL url = new URL(CHECK_URL);
			InputStream in = (InputStream)url.getContent();
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line=r.readLine())!=null) {
				System.out.println(line);
			}
			r.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}


}