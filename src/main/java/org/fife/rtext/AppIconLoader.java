package org.fife.rtext;

import org.fife.ui.ImageTranscodingUtil;
import org.fife.ui.UIUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Loads icons for the application.  Picks "good" icons based on whether the
 * current Look and Feel is light or dark.<p>
 *
 * This class currently isn't used extensively, but in general, we should
 * move things to it to foster the use of SVG icons.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public final class AppIconLoader {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private AppIconLoader() {
		// Do nothing (comment for Sonar)
	}


	public static Icon getIcon(String darkResource, String lightResource, int w, int h) {

		Icon icon = null;

		if (UIUtil.isDarkLookAndFeel()) {
			icon = getIconImpl("graphics/dark/" + darkResource, w, h);
		}

		if (icon == null) {
			icon = getIconImpl("graphics/light/" + lightResource, w, h);
		}

		return icon;
	}


	private static Icon getIconImpl(String resource, int w, int h) {
		if (resource.endsWith(".svg")) {
			return getSvgIcon(resource, w, h);
		}
		return getRegularIcon(resource, w, h);
	}


	private static Icon getRegularIcon(String resource, int w, int h) {

		try (InputStream in = AppIconLoader.class.getResourceAsStream(resource)) {
			return new ImageIcon(ImageIO.read(in));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return null;
	}


	private static Icon getSvgIcon(String resource, int w, int h) {

		String svgName = resource.substring(resource.lastIndexOf('/') + 1);

		try (InputStream in = AppIconLoader.class.getResourceAsStream(resource)) {
			return new ImageIcon(ImageTranscodingUtil.rasterize(svgName, in, w, h));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return null;
	}
}
