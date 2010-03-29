/*
 * 03/18/2010
 *
 * ContentAssistField.java - A text field with content-assist available.
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
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
package org.fife.rtext.plugins.tools;

import javax.swing.JTextField;

import org.fife.rtext.ContentAssistable;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.search.AbstractSearchDialog;


/**
 * A text field with content assist available.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class ContentAssistField extends JTextField
										implements ContentAssistable{

	/**
	 * Whether content assist is enabled.
	 */
	private boolean enabled;

	/**
	 * The auto-completion instance for this text field.
	 */
	private AutoCompletion ac;

	/**
	 * Provides the completions for this text field.
	 */
	private CompletionProvider provider;


	/**
	 * Constructor.
	 */
	public ContentAssistField() {
	}


	/**
	 * Adds the completions known to this text field.
	 *
	 * @param provider The completion provider to add to.
	 */
	protected abstract void addCompletions(CompletionProvider provider);


	/**
	 * Lazily creates the AutoCompletion instance this text field uses.
	 *
	 * @return The auto-completion instance.
	 */
	private AutoCompletion getAutoCompletion() {
		if (ac==null) {
			ac = new AutoCompletion(getCompletionProvider());
		}
		return ac;
	}


	/**
	 * Creates the shared completion provider instance.
	 *
	 * @return The completion provider.
	 */
	protected synchronized CompletionProvider getCompletionProvider() {
		if (provider==null) {
			provider = new DefaultCompletionProvider();
			addCompletions(provider);
		}
		return provider;
	}


	/**
	 * Toggles whether regex auto-complete is enabled.  This method will fire
	 * a property change event of type
	 * {@link ContentAssistable#ASSISTANCE_IMAGE}.
	 * 
	 * @param enabled Whether regex auto complete should be enabled.
	 * @see #isAutoCompleteEnabled()
	 */
	public void setAutoCompleteEnabled(boolean enabled) {
		if (this.enabled!=enabled) {
			this.enabled = enabled;
			if (enabled) {
				AutoCompletion ac = getAutoCompletion();
				ac.install(this);
			}
			else {
				ac.uninstall();
			}
			String prop = ContentAssistable.ASSISTANCE_IMAGE;
			// Must take care how we fire the property event, as Swing
			// property change support won't fire a notice if old and new are
			// both non-null and old.equals(new).
			if (enabled) {
				firePropertyChange(prop, null,
								AbstractSearchDialog.getContentAssistImage());
			}
			else {
				firePropertyChange(prop, null, null);
			}
		}
	}


}