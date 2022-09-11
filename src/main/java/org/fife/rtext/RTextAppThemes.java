/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;


import org.fife.ui.app.AppTheme;
import org.fife.ui.app.console.AbstractConsoleTextArea;
import org.fife.ui.app.themes.FlatDarkTheme;
import org.fife.ui.app.themes.FlatLightTheme;
import org.fife.ui.app.themes.NativeTheme;
import org.fife.ui.rsyntaxtextarea.Theme;

import java.awt.*;
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
		nativeTheme.addExtraUiDefault("rtext.console.prompt", AbstractConsoleTextArea.DEFAULT_LIGHT_PROMPT_FG);
		nativeTheme.addExtraUiDefault("rtext.console.stdout", AbstractConsoleTextArea.DEFAULT_LIGHT_STDOUT_FG);
		nativeTheme.addExtraUiDefault("rtext.console.stderr", AbstractConsoleTextArea.DEFAULT_LIGHT_STDERR_FG);
		nativeTheme.addExtraUiDefault("rtext.console.result", AbstractConsoleTextArea.DEFAULT_LIGHT_RESULT_FG);
		themes.add(nativeTheme);

		FlatDarkTheme flatDarkTheme = new FlatDarkTheme();
		flatDarkTheme.setHyperlinkForeground(new Color(0x589df6));
		flatDarkTheme.addExtraUiDefault("rtext.editorTheme", "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml");
		flatDarkTheme.addExtraUiDefault("rtext.iconGroupName", "IntelliJ Icons (Dark)");
		flatDarkTheme.addExtraUiDefault("rtext.labelErrorForeground", DARK_MODIFIED_DOCUMENT_NAME_COLOR);
		flatDarkTheme.addExtraUiDefault("rtext.listAltRowColor", darkListAltRowColor);
		flatDarkTheme.addExtraUiDefault("rtext.console.prompt", AbstractConsoleTextArea.DEFAULT_DARK_PROMPT_FG);
		flatDarkTheme.addExtraUiDefault("rtext.console.stdout", AbstractConsoleTextArea.DEFAULT_DARK_STDOUT_FG);
		flatDarkTheme.addExtraUiDefault("rtext.console.stderr", AbstractConsoleTextArea.DEFAULT_DARK_STDERR_FG);
		flatDarkTheme.addExtraUiDefault("rtext.console.result", AbstractConsoleTextArea.DEFAULT_DARK_RESULT_FG);
		themes.add(flatDarkTheme);

		FlatLightTheme flatLightTheme = new FlatLightTheme();
		flatLightTheme.setHyperlinkForeground(Color.BLUE);
		flatLightTheme.addExtraUiDefault("rtext.editorTheme", "/org/fife/ui/rsyntaxtextarea/themes/default-alt.xml");
		flatLightTheme.addExtraUiDefault("rtext.iconGroupName", "IntelliJ Icons (Light)");
		flatLightTheme.addExtraUiDefault("rtext.labelErrorForeground", LIGHT_MODIFIED_DOCUMENT_NAME_COLOR);
		flatLightTheme.addExtraUiDefault("rtext.listAltRowColor", lightListAltRowColor);
		flatLightTheme.addExtraUiDefault("rtext.console.prompt", AbstractConsoleTextArea.DEFAULT_LIGHT_PROMPT_FG);
		flatLightTheme.addExtraUiDefault("rtext.console.stdout", AbstractConsoleTextArea.DEFAULT_LIGHT_STDOUT_FG);
		flatLightTheme.addExtraUiDefault("rtext.console.stderr", AbstractConsoleTextArea.DEFAULT_LIGHT_STDERR_FG);
		flatLightTheme.addExtraUiDefault("rtext.console.result", AbstractConsoleTextArea.DEFAULT_LIGHT_RESULT_FG);
		themes.add(flatLightTheme);

		return themes;
	}


	/**
	 * Returns the RSTA editor theme from an application theme.
	 *
	 * @param theme The application theme.
	 * @param baseFont The base font to use for the theme. If this is {@code null},
	 *        the default RSTA font will be used.
	 * @return The RSTA editor theme.
	 * @throws IOException If an IO error occurs.
	 */
	public static Theme getRstaTheme(AppTheme theme, Font baseFont) throws IOException {
		String rstaThemeName = (String)theme.getExtraUiDefaults().get("rtext.editorTheme");
		return Theme.load(RTextAppThemes.class.getResourceAsStream(rstaThemeName), baseFont);
	}
}
