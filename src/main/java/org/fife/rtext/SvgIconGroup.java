/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext;

import org.fife.ui.ImageTranscodingUtil;
import org.fife.ui.app.AbstractGUIApplication;
import org.fife.ui.rtextarea.IconGroup;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * An icon group consisting of SVG icons.
 */
public class SvgIconGroup extends IconGroup {

	private String jarFile;

	public SvgIconGroup(AbstractGUIApplication<?> owner, String name, String jarFile) {
		super(name, "", null, "svg", jarFile);
		this.jarFile = owner.getInstallLocation() + '/' + jarFile;
	}

	@Override
	protected Icon getIconImpl(String iconFullPath) {
		try (InputStream svg = new URL("jar:file:///" +
			jarFile + "!/" + iconFullPath).openStream()) {
			//System.err.println("***** " + url.toString());
			BufferedImage image = ImageTranscodingUtil.rasterize(
				iconFullPath, svg, 16, 16);
			return new ImageIcon(image);
		} catch (IOException ioe) {
			// If any one icon's not there, we just don't display it in the UI
			int lastDot = iconFullPath.lastIndexOf('.');
			String pngIconName = iconFullPath.substring(0, lastDot) + ".png";
			return super.getIconImpl(pngIconName);
		}
	}
}
