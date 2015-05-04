/*
 * 03/18/2010
 *
 * ContentAssistField.java - A text field with content-assist available.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.tools;

import javax.swing.JTextField;

import org.fife.rsta.ui.ContentAssistable;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.rsta.ui.search.AbstractSearchDialog;


/**
 * A text field with content assist available.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class ContentAssistField extends JTextField
										implements ContentAssistable {

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
	 * Returns whether auto-complete is enabled.
	 *
	 * @return Whether auto-complete is enabled.
	 * @see #setAutoCompleteEnabled(boolean)
	 */
	public boolean isAutoCompleteEnabled() {
		return enabled;
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