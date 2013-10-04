/*
 * 11/14/2003
 *
 * BackgroundDialog.java - Dialog allowing you to change the background
 * of RTextAreas.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.fife.ui.ImagePreviewPane;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.RFileChooser;
import org.fife.ui.UIUtil;
import org.fife.ui.rtextfilechooser.filters.ImageFileFilter;


/**
 * Dialog allowing the user to select the background to use for an
 * <code>RTextArea</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class BackgroundDialog extends JDialog implements ActionListener {

	private JRadioButton colorButton;
	private JRadioButton imageButton;
	private JButton colorBrowseButton;
	private JButton imageBrowseButton;
	private JButton okButton;
	private JButton cancelButton;
	private ColorOrImageIcon colorOrImageIcon;

	private Color currentColor;
	private String currentImageFileName;
	private JTextField imageFileNameField;
	private boolean viewingColors;

	private ResourceBundle msg;

	private JFileChooser imageChooser;

	private static final int IMAGE_DIM				= 120;


	/**
	 * Creates and initializes a <code>BackgroundDialog</code>.
	 *
	 * @param owner The owner of this dialog.
	 */
	public BackgroundDialog(Dialog owner) {

		// Call parent's constructor and set the dialog's title.
		super(owner);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		msg = ResourceBundle.getBundle(
							"org.fife.ui.rtextarea.BackgroundDialog");
		setTitle(msg.getString("BackgroundDialogTitle"));

		Border empty5Border = UIUtil.getEmpty5Border();

		// Initialize the image chooser.
		imageChooser = new RFileChooser();
		ImagePreviewPane imagePreviewPane = new ImagePreviewPane();
		imageChooser.setAccessory(imagePreviewPane);
		imageChooser.addPropertyChangeListener(imagePreviewPane);
		imageChooser.setFileFilter(new ImageFileFilter());
		imageChooser.applyComponentOrientation(orientation);

		// Create a panel showing a "background preview."
		Box previewPanel = Box.createVerticalBox();
		previewPanel.setBorder(BorderFactory.createTitledBorder(
									msg.getString("PreviewLabel")));
		colorOrImageIcon = new ColorOrImageIcon();
		JLabel tempLabel = new JLabel();
		tempLabel.setIcon(colorOrImageIcon);
		previewPanel.add(tempLabel);

		// Create a panel allowing you to go to the color picker or image
		// selector to change the background.
		JPanel changePanel = new JPanel(new BorderLayout());
		changePanel.setBorder(empty5Border);

		JPanel temp = new JPanel(new BorderLayout());
		changePanel.add(temp, BorderLayout.NORTH);

		// Need temp3 so radio buttons & buttons stay right-aligned in RTL
		// locales.  It's unnecessary for LTR locales.
		JPanel temp2 = new JPanel(new SpringLayout());
		JPanel temp3 = new JPanel(new BorderLayout());
		temp3.add(temp2, BorderLayout.LINE_START);
		temp.add(temp3, BorderLayout.NORTH);

		ButtonGroup bg = new ButtonGroup();

		colorButton = UIUtil.newRadio(msg, "Color", bg, this, true);
		colorButton.setActionCommand("ColorRadioButton");

		colorBrowseButton = new JButton(msg.getString("BrowseColors"));
		colorBrowseButton.setActionCommand("BrowseColors");
		colorBrowseButton.addActionListener(this);
		colorBrowseButton.setEnabled(true);

		imageButton = UIUtil.newRadio(msg, "Image", bg, this);
		imageButton.setActionCommand("ImageRadioButton");

		imageBrowseButton = new JButton(msg.getString("BrowseImages"));
		imageBrowseButton.setActionCommand("BrowseImages");
		imageBrowseButton.addActionListener(this);
		imageBrowseButton.setEnabled(false);

		if (orientation.isLeftToRight()) { // SpringLayout
			temp2.add(colorButton);
			temp2.add(colorBrowseButton);
			temp2.add(imageButton);
			temp2.add(imageBrowseButton);
		}
		else {
			temp2.add(colorBrowseButton);
			temp2.add(colorButton);
			temp2.add(imageBrowseButton);
			temp2.add(imageButton);
		}

		UIUtil.makeSpringCompactGrid(temp2, 2,2,	//rows, cols
								0,0,		//initX, initY
								6, 6);	//xPad, yPad

		imageFileNameField = new JTextField(30);
		imageFileNameField.setEditable(false);
		temp.add(imageFileNameField, BorderLayout.SOUTH);

		// Create a panel combining the first two.
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		topPanel.add(previewPanel, BorderLayout.LINE_START);
		topPanel.add(changePanel);

		// Create a panel for the OK and Cancel buttons.
		okButton = UIUtil.newButton(msg, "OK", "OKMnemonic");
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		cancelButton = UIUtil.newButton(msg, "Cancel", "CancelMnemonic");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		Container buttons = UIUtil.createButtonFooter(okButton, cancelButton);

		// Arrange the dialog!
		JPanel cp = new ResizableFrameContentPane(new BorderLayout());
		cp.setBorder(empty5Border);
		cp.add(topPanel, BorderLayout.NORTH);
		temp = new JPanel(new BorderLayout());
		temp.add(new JSeparator(), BorderLayout.NORTH);
		temp.add(buttons, BorderLayout.SOUTH);
		cp.add(temp, BorderLayout.SOUTH);
		setContentPane(cp);

		// Get everything ready to go.
		getRootPane().setDefaultButton(okButton);
		setResizable(true);
		setModal(true);	// So the user can't go back to the editor.
		setLocationRelativeTo(owner);
		applyComponentOrientation(orientation);
		pack();

	}


	// Callback for when the user clicks a button, etc.
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals("ColorRadioButton")) {
			colorBrowseButton.setEnabled(true);
			imageBrowseButton.setEnabled(false);
			viewingColors = true;
			colorOrImageIcon.setColor(currentColor);
			okButton.setEnabled(true);
			String colorString = currentColor.toString();
			imageFileNameField.setText(colorString.substring(colorString.indexOf('[')));
			this.repaint();
		}

		else if (command.equals("ImageRadioButton")) {
			colorBrowseButton.setEnabled(false);
			imageBrowseButton.setEnabled(true);
			viewingColors = false;
			colorOrImageIcon.setColor(null);
			if (currentImageFileName!=null) {
				imageFileNameField.setText(currentImageFileName);
				okButton.setEnabled(true);
			}
			else {
				imageFileNameField.setText(msg.getString("NoImageLabel"));
				okButton.setEnabled(false);
			}
			this.repaint();
		}

		else if (command.equals("BrowseColors")) {
			Color tempColor = JColorChooser.showDialog(this,
							msg.getString("BGColorChooserTitle"),
							currentColor);
			if (tempColor != null) {
				currentColor = tempColor;
				colorOrImageIcon.setColor(currentColor);
				String colorString = currentColor.toString();
				imageFileNameField.setText(colorString.substring(colorString.indexOf('[')));
				this.repaint();
				//currentImageFileName = null;
			}
		}

		else if (command.equals("BrowseImages")) {

			int returnVal = imageChooser.showOpenDialog(this);

			// If they selected a file and clicked "OK", open the flie!
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				currentImageFileName = imageChooser.getSelectedFile().getAbsolutePath();
				Image previewImage = getPreviewImage(currentImageFileName);
				if (previewImage==null) {
					JOptionPane.showMessageDialog(this,
							msg.getString("ImageFileError") + "\n'" +
										currentImageFileName + "'",
							msg.getString("ErrorDialogTitle"),
							JOptionPane.ERROR_MESSAGE);
					currentImageFileName = null;
					okButton.setEnabled(false);
					imageFileNameField.setText(msg.getString("NoImageLabel"));
					colorOrImageIcon.setImage(null);
					repaint();
				}
				else {
					colorOrImageIcon.setImage(previewImage);
					imageFileNameField.setText(currentImageFileName);
					okButton.setEnabled(true);
					this.repaint();
				}
			}

		}

		else if (command.equals("OK")) {
			this.setVisible(false);
		}

		else if (command.equals("Cancel")) {
			currentColor = null;
			currentImageFileName = null;
			this.setVisible(false);
		}

	}


	/**
	 * Returns the background the user chose - either a
	 * <code>java.awt.Color</code> or a <code>java.awt.Image</code>,
	 * or <code>null</code> if they hit cancel.
	 *
	 * @return The background, as an <code>Object</code>.
	 */
	public Object getChosenBackground() {
		if (viewingColors) {
			return currentColor;
		}
		else if (currentImageFileName != null) {
			return getImageFromFile(currentImageFileName);
		}
		return null;
	}


	/**
	 * Returns the full path to the image the user chose, or <code>null</code>
	 * if the user selected a color or hit cancel.
	 *
	 * @return The full path to the image the user chose for the background,
	 *         or <code>null</code>.
	 */
	public String getCurrentImageFileName() {
		if (viewingColors)
			return null;
		return currentImageFileName;	// Should be null if they hit cancel.
	}


	/**
	 * Returns an image from a file in a safe fashion.
	 *
	 * @param fileName The file from which to get the image (must be .jpg,
	 *        .png, .gif or .bmp).
	 * @return The image contained in the file, or <code>null</code> if the
	 *         image file was invalid.
	 */
	private Image getImageFromFile(String fileName) {

		Image image = null;
		try {
			image = ImageIO.read(new File(fileName));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
						msg.getString("ImageFileIOError") + "\nFile: " +
											fileName + " -\n" + e,
						msg.getString("ErrorDialogTitle"),
						JOptionPane.ERROR_MESSAGE);
		}
		return image;

	}


	/**
	 * Returns a scaled version of the specified image, for a "preview"
	 * display.
	 *
	 * @param fileName The name of an image file.
	 * @return A scaled version of the image, or <code>null</code> if an
	 *         IO error occurs.
	 */
	private Image getPreviewImage(String fileName) {

		Image image = null;

		File file = new File(fileName);
		if (file.isFile()) {
			try {
				image = ImageIO.read(file);
				//image = new ImageIcon(fileName).getImage();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		if (image!=null) {
			// NOTE: Do NOT use image.getScaledInstance()!  Not only is
			// this method considered old and shouldn't be used (but it
			// isn't deprecated as of Java 6!), and slower than the
			// alternative resizing method, but it also has a bug which
			// sometimes throws an exception when using the
			// Image.SCALE_SMOOTH hint.  So use a different method to
			// get a scaled version.
			// See Java bugs:
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4937376
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6196792
			//image = image.getScaledInstance(IMAGE_DIM,IMAGE_DIM, Image.SCALE_SMOOTH);
			BufferedImage bi =  new BufferedImage(image.getWidth(this),
										image.getHeight(this),
										BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bi.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2d.drawImage(image, 0,0, IMAGE_DIM,IMAGE_DIM, null);
			g2d.dispose();
			image = bi;
		}

		return image;

	}


	/**
	 * Sets the currently-selected background in this dialog.  This should be
	 * called just before calling <code>setVisible(true)</code>.
	 *
	 * @param defaultBackground Should be either a <code>java.awt.Color</code>
	 *        instance or a <code>java.awt.Image</code> instance containing an
	 *        image to use as the initial background choice.
	 * @param imageFileName Full path to the file containing
	 *        <code>defaultBackground</code>, if it is an image.  If it is a
	 *        <code>java.awt.Color</code>, this parameter should be
	 *        <code>null</code>.
	 */
	public void initializeData(Object defaultBackground, String imageFileName) {

		if (defaultBackground instanceof Color) {
			currentColor = (Color)defaultBackground;
			colorOrImageIcon.setColor(currentColor);
			colorOrImageIcon.setImage(null);
			currentImageFileName = null;
			String colorString = currentColor.toString();
			imageFileNameField.setText(colorString.substring(colorString.indexOf('[')));
			colorButton.setSelected(true);	// In case they selected "Image", then canceled last time.
			colorBrowseButton.setEnabled(true);
			imageBrowseButton.setEnabled(false);
			okButton.setEnabled(true); 		// In case they selected "Image", then canceled last time.
			viewingColors = true;
		}
		else if (defaultBackground instanceof Image) {
			currentImageFileName = imageFileName;// We know that the image is currently displayed.
			Image previewImage = getPreviewImage(currentImageFileName);
			colorOrImageIcon.setColor(null);
			colorOrImageIcon.setImage(previewImage);
			imageFileNameField.setText(currentImageFileName);
			currentColor = Color.WHITE;
			imageButton.setSelected(true);	// In case they selected "Color", then canceled last time.
			colorBrowseButton.setEnabled(false);
			imageBrowseButton.setEnabled(true);
			viewingColors = false;
		}
		else {
			JOptionPane.showMessageDialog(this,
					msg.getString("BGBadObjectMessage") + "\n" + defaultBackground,
					msg.getString("ErrorDialogTitle"),
					JOptionPane.ERROR_MESSAGE);
			currentImageFileName = null;
			imageFileNameField.setText(msg.getString("NoImageLabel"));
			currentColor = Color.WHITE;
			colorBrowseButton.setEnabled(true);
			imageBrowseButton.setEnabled(false);
			viewingColors = true;
		}

	}


	/**
	 * Updates the UI used by this dialog.  This is overridden to update the
	 * image file chooser.  Note that this is NOT overriding any
	 * <code>JDialog#updateUI</code> method, as none exists.  This method is
	 * specific to <code>BackgroundDialog</code>.
	 */
	public void updateUI() {
		if (imageChooser != null) {
			SwingUtilities.updateComponentTreeUI(imageChooser);
		}
	}


	/**
	 * An icon capable of displaying either an image or a color rectangle.
	 * If a color is set, then a color rectangle is painted.  Otherwise, the
	 * image is painted.
	 */
	private static class ColorOrImageIcon extends ImageIcon {

		private Color color = Color.RED;

		public Color getColor() {
			return color;
		}

		@Override
		public int getIconHeight() {
			return IMAGE_DIM;
		}

		@Override
		public int getIconWidth() {
			return IMAGE_DIM;
		}

		// Overridden to prevent "null" images (via setImage(null)) from
		// throwing an exception.
		@Override
		protected void loadImage(Image image) {
			if (image!=null) {
				super.loadImage(image);
			}
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			if (getColor()!=null) {
				Color old = g.getColor();
				g.setColor(getColor());
				g.fillRect(x,y, getIconWidth(), getIconHeight());
				g.setColor(old);
			}
			else {
				// In 1.4.2, calling super.paintIcon() with a null image
				// threw an NPE.  In 1.5+, Javadoc for this method
				// explicitly states that null images cause nothing to be
				// drawn.  But since we support 1.4.x, we check for null
				// images here.
				if (getImage()!=null) {
					super.paintIcon(c, g, x,y);
				}
			}
		}

		public void setColor(Color color) {
			this.color = color;
		}

	}

}