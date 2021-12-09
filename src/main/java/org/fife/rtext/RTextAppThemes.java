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
import org.fife.ui.rsyntaxtextarea.Theme;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Application themes available in RText.
 */
public final class RTextAppThemes {

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

		Color lightListAltRowColor = new Color(0xf4f4f4);
		Color darkListAltRowColor = new Color(60, 63, 65);

		List<AppTheme> themes = new ArrayList<>();

		NativeTheme nativeTheme = new NativeTheme();
		nativeTheme.setHyperlinkForeground(Color.BLUE);
		nativeTheme.addExtraUiDefault("rtext.editorTheme", "/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml");
		nativeTheme.addExtraUiDefault("rtext.iconGroupName", "Eclipse Icons");
		nativeTheme.addExtraUiDefault("rtext.labelErrorForeground", LIGHT_MODIFIED_DOCUMENT_NAME_COLOR);
		nativeTheme.addExtraUiDefault("rtext.listAltRowColor", lightListAltRowColor);
		themes.add(nativeTheme);

		FlatDarkTheme flatDarkTheme = new FlatDarkTheme();
		flatDarkTheme.setHyperlinkForeground(new Color(0x589df6));
		flatDarkTheme.addExtraUiDefault("rtext.editorTheme", "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml");
		flatDarkTheme.addExtraUiDefault("rtext.iconGroupName", "IntelliJ Icons (Dark)");
		flatDarkTheme.addExtraUiDefault("rtext.labelErrorForeground", DARK_MODIFIED_DOCUMENT_NAME_COLOR);
		flatDarkTheme.addExtraUiDefault("rtext.listAltRowColor", darkListAltRowColor);
		themes.add(flatDarkTheme);

		FlatLightTheme flatLightTheme = new FlatLightTheme();
		flatLightTheme.setHyperlinkForeground(Color.BLUE);
		flatLightTheme.addExtraUiDefault("rtext.editorTheme", "/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml");
		flatLightTheme.addExtraUiDefault("rtext.iconGroupName", "IntelliJ Icons (Light)");
		flatLightTheme.addExtraUiDefault("rtext.labelErrorForeground", LIGHT_MODIFIED_DOCUMENT_NAME_COLOR);
		flatLightTheme.addExtraUiDefault("rtext.listAltRowColor", lightListAltRowColor);
		themes.add(flatLightTheme);

		return themes;
	}


	/**
	 * Returns the RSTA editor theme from an application theme.
	 *
	 * @param theme The application theme.
	 * @return The RSTA editor theme.
	 * @throws java.io.IOException If an IO error occurs.
	 */
	public static Theme getRstaTheme(AppTheme theme) throws IOException {

		String rstaThemeName = (String)theme.getExtraUiDefaults().get("rtext.editorTheme");

		return Theme.load(RTextAppThemes.class.getResourceAsStream(rstaThemeName));
	}
}
