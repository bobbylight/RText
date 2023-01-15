/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;


import java.util.EventListener;

/**
 * Listens for events from the {@link EditorOptionsPreviewContext}.
 */
interface EditorOptionsPreviewContextListener extends EventListener {


	/**
	 * Called when properties in the editor preview context change.
	 *
	 * @param context The new values in the context.
	 */
	void editorOptionsPreviewContextChanged(EditorOptionsPreviewContext context);
}
