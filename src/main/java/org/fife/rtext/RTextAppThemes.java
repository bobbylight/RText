/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;


import org.fife.ui.app.AppTheme;
import org.fife.ui.app.themes.FlatDarkTheme;
import org.fife.ui.app.themes.FlatLightTheme;
import org.fife.ui.app.themes.NativeTheme;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Application themes available in RText.
 */
final class RTextAppThemes {

	/**
	 * The color to use when rendering the name of modified documents in tabs when the current Look
	 * and Feel is light.
	 */
	public static final Color LIGHT_MODIFIED_DOCUMENT_NAME_COLOR = Color.RED;

	/**
	 * The color to use when rendering the name of modified documents in tabs when the current Look
	 * and Feel is dark.
	 */
	public static final Color DARK_MODIFIED_DOCUMENT_NAME_COLOR = new Color(255, 128, 128);


	/**
	 * Private constructor to prevent instantiation.
	 */
	private RTextAppThemes() {
		// Do nothing - comment for Sonar
	}


	static List<AppTheme> get() {

		List<AppTheme> themes = new ArrayList<>();

		NativeTheme nativeTheme = new NativeTheme();
		nativeTheme.addExtraUiDefault("rtext.editorTheme", "/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml");
		nativeTheme.addExtraUiDefault("rtext.iconGroupName", "Eclipse Icons");
		nativeTheme.addExtraUiDefault("rtext.labelErrorForeground", LIGHT_MODIFIED_DOCUMENT_NAME_COLOR);
		themes.add(nativeTheme);

		FlatDarkTheme flatDarkTheme = new FlatDarkTheme();
		flatDarkTheme.addExtraUiDefault("rtext.editorTheme", "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml");
		flatDarkTheme.addExtraUiDefault("rtext.iconGroupName", "IntelliJ Icons (Dark)");
		flatDarkTheme.addExtraUiDefault("rtext.labelErrorForeground", DARK_MODIFIED_DOCUMENT_NAME_COLOR);
		themes.add(flatDarkTheme);

		FlatLightTheme flatLightTheme = new FlatLightTheme();
		flatLightTheme.addExtraUiDefault("rtext.editorTheme", "/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml");
		flatLightTheme.addExtraUiDefault("rtext.iconGroupName", "IntelliJ Icons (Light)");
		flatLightTheme.addExtraUiDefault("rtext.labelErrorForeground", LIGHT_MODIFIED_DOCUMENT_NAME_COLOR);
		themes.add(flatLightTheme);

		return themes;
	}
}
