/*
 * 02/26/2004
 *
 * RSyntaxTextAreaOptionsPanel.java - An options panel that can be
 * added to org.fife.ui.OptionsDialog for an RSyntaxTextArea.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.*;

import org.fife.ui.FontSelector;
import org.fife.ui.RListSelectionModel;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.*;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rtextarea.RTextArea;


/**
 * An options panel that can be added to an
 * <code>org.fife.ui.OptionsDialog</code> to display options appropriate
 * for an <code>RSyntaxTextArea</code>.  This panel allows the user to
 * change the following properties of the text area:
 * <ul>
 *    <li>Syntax highlighting colors</li>
 *    <li>Bracket matching and color</li>
 *    <li>Toggle whitespace (spaces and tabs) visibility</li>
 *    <li>Toggle anti-aliasing and fractional font metrics</li>
 * </ul>
 * It also gives the user a button to restore the default color scheme.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class RSyntaxTextAreaOptionPanel extends OptionsDialogPanel
					implements ActionListener, PropertyChangeListener,
								ListSelectionListener{

	private FontSelector mainFontSelector;
	private JTextField mainBackgroundField;
	private JButton mainBackgroundButton;
	private BackgroundDialog backgroundDialog;
	private Object background;
	private String bgImageFileName; // null if background is a color.

	private JPanel syntaxPanel;

	private JList syntaxList;
	private DefaultListModel syntaxListModel;
	private FontSelector fontSelector;
	private RColorSwatchesButton foregroundButton;
	private JCheckBox fgCheckBox;
	private JCheckBox bgCheckBox;
	private RColorSwatchesButton backgroundButton;

	private JComboBox sampleCombo;
	private RSyntaxTextArea sampleArea;
	private SyntaxScheme colorScheme;

	private boolean isSettingStyle;

	private static final String DEFAULTS_RESTORED			= "RSTAOpts.defaultsRestored";
	private static final String SYNTAX_COLOR_PROPERTY			= "RSTAOpts.syntaxColor";
	private static final String SYNTAX_FONT_PROPERTY			= "RSTAOpts.syntaxFont";
	private static final String UNKNOWN_PROPERTY				= "RSTAOpts.unknown";

	private static final String[] SAMPLES = {
		"previewJava.txt", "previewPerl.txt", "previewXml.txt",
	};

	private static final String[] SAMPLE_STYLES = {
		SyntaxConstants.SYNTAX_STYLE_JAVA, SyntaxConstants.SYNTAX_STYLE_PERL,
		SyntaxConstants.SYNTAX_STYLE_XML,
	};

	/**
	 * Constructor.
	 */
	public RSyntaxTextAreaOptionPanel() {

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		ResourceBundle msg = ResourceBundle.getBundle(
							"org.fife.ui.rsyntaxtextarea.OptionPanel");

		setName(msg.getString("Title"));

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());
		Box contentPane = Box.createVerticalBox();
		add(contentPane, BorderLayout.NORTH);

		SpringLayout sl = new SpringLayout();
		JPanel springPanel = new JPanel(sl);
		springPanel.setBorder(new OptionPanelBorder(msg.getString("Font")));
		mainFontSelector = new FontSelector(FontSelector.NOT_LABELED);
		mainFontSelector.setColorSelectable(true);
		mainFontSelector.addPropertyChangeListener(FontSelector.FONT_PROPERTY, this);
		mainFontSelector.addPropertyChangeListener(FontSelector.FONT_COLOR_PROPERTY, this);
		JLabel mfsLabel = new JLabel(msg.getString("Font"));
		mfsLabel.setLabelFor(mainFontSelector);
		JLabel bgLabel = new JLabel(msg.getString("Background"));
		mainBackgroundField = new JTextField(20);
		mainBackgroundField.setEditable(false);
		mainBackgroundButton = new JButton(msg.getString("Change"));
		mainBackgroundButton.setActionCommand("BackgroundButton");
		mainBackgroundButton.addActionListener(this);
		bgLabel.setLabelFor(mainBackgroundButton);
		Box bgRestPanel = createHorizontalBox();
		bgRestPanel.add(mainBackgroundField);
		bgRestPanel.add(Box.createHorizontalStrut(5));
		bgRestPanel.add(mainBackgroundButton);
		if (orientation.isLeftToRight()) {
			springPanel.add(mfsLabel); springPanel.add(mainFontSelector);
			springPanel.add(bgLabel);  springPanel.add(bgRestPanel);
		}
		else {
			springPanel.add(mainFontSelector); springPanel.add(mfsLabel);
			springPanel.add(bgRestPanel);  springPanel.add(bgLabel);
		}
		UIUtil.makeSpringCompactGrid(springPanel, 2, 2, 0, 0, 5, 5);
		contentPane.add(springPanel);
		contentPane.add(Box.createVerticalStrut(5));

		// Syntax panel contains all of the "syntax highlighting" stuff.
		syntaxPanel = new JPanel(new BorderLayout());
		syntaxPanel.setBorder(new OptionPanelBorder(
								msg.getString("FontsAndColors")));

		// Add the token style selection panel to the right.
		syntaxListModel = new DefaultListModel();
		syntaxList = new JList(syntaxListModel);
		syntaxList.setSelectionModel(new RListSelectionModel());
		syntaxList.addListSelectionListener(this);
		syntaxList.setVisibleRowCount(10);
		syntaxListModel.addElement(msg.getString("Style.Comment.EndOfLine"));
		syntaxListModel.addElement(msg.getString("Style.Comment.Multiline"));
		syntaxListModel.addElement(msg.getString("Style.Comment.Documentation"));
		syntaxListModel.addElement(msg.getString("Style.Comment.Keyword"));
		syntaxListModel.addElement(msg.getString("Style.Comment.Markup"));
		syntaxListModel.addElement(msg.getString("Style.ReservedWord"));
		syntaxListModel.addElement(msg.getString("Style.ReservedWord2"));
		syntaxListModel.addElement(msg.getString("Style.Function"));
		syntaxListModel.addElement(msg.getString("Style.Literal.Boolean"));
		syntaxListModel.addElement(msg.getString("Style.Literal.Integer"));
		syntaxListModel.addElement(msg.getString("Style.Literal.Float"));
		syntaxListModel.addElement(msg.getString("Style.Literal.Hex"));
		syntaxListModel.addElement(msg.getString("Style.Literal.String"));
		syntaxListModel.addElement(msg.getString("Style.Literal.Char"));
		syntaxListModel.addElement(msg.getString("Style.Literal.Backquote"));
		syntaxListModel.addElement(msg.getString("Style.DataType"));
		syntaxListModel.addElement(msg.getString("Style.Variable"));
		syntaxListModel.addElement(msg.getString("Style.RegularExpression"));
		syntaxListModel.addElement(msg.getString("Style.Annotation"));
		syntaxListModel.addElement("<html><b>"+msg.getString("Style.Identifier.PlainText"));
		syntaxListModel.addElement(msg.getString("Style.Whitespace"));
		syntaxListModel.addElement(msg.getString("Style.Separator"));
		syntaxListModel.addElement(msg.getString("Style.Operator"));
		syntaxListModel.addElement(msg.getString("Style.Preprocessor"));
		syntaxListModel.addElement(msg.getString("Style.MarkupTag.Delimiter"));
		syntaxListModel.addElement(msg.getString("Style.MarkupTag.TagName"));
		syntaxListModel.addElement(msg.getString("Style.MarkupTag.Attribute"));
		syntaxListModel.addElement(msg.getString("Style.MarkupTag.AttributeValue"));
		syntaxListModel.addElement(msg.getString("Style.MarkupTag.Comment"));
		syntaxListModel.addElement(msg.getString("Style.MarkupTag.DTD"));
		syntaxListModel.addElement(msg.getString("Style.MarkupTag.ProcessingInstruction"));
		syntaxListModel.addElement(msg.getString("Style.MarkupTag.CDataDelimiter"));
		syntaxListModel.addElement(msg.getString("Style.MarkupTag.CData"));
		syntaxListModel.addElement(msg.getString("Style.MarkupTag.EntityReference"));
		syntaxListModel.addElement(msg.getString("Style.Error.Identifier"));
		syntaxListModel.addElement(msg.getString("Style.Error.Number"));
		syntaxListModel.addElement(msg.getString("Style.Error.String"));
		syntaxListModel.addElement(msg.getString("Style.Error.Char"));
		syntaxPanel.add(new RScrollPane(syntaxList), BorderLayout.LINE_START);

		// Create a panel for other syntax style properties.
		Box propertiesPanel = Box.createVerticalBox();

		// Create a panel for selecting the font.
		fontSelector = new FontSelector(FontSelector.CHECK_BOX);
		fontSelector.setUnderlineSelectable(true);
		fontSelector.addPropertyChangeListener(FontSelector.FONT_PROPERTY, this);
		fontSelector.addPropertyChangeListener(FontSelector.ENABLED_PROPERTY, this);
		 // Just to keep it right-aligned with stuff above...
		Box temp = createHorizontalBox();
		temp.add(fontSelector);
		temp.add(Box.createHorizontalStrut(5));
		propertiesPanel.add(temp);
		propertiesPanel.add(Box.createVerticalStrut(8));

		// Add the foreground and background buttons to the properties panel.
		temp = createHorizontalBox();
		temp.setAlignmentX(Component.LEFT_ALIGNMENT);
		fgCheckBox = new JCheckBox(msg.getString("Foreground"));
		fgCheckBox.setActionCommand("fgCheckBox");
		fgCheckBox.addActionListener(this);
		foregroundButton = new RColorSwatchesButton(Color.BLACK);
		foregroundButton.addPropertyChangeListener(
						RColorButton.COLOR_CHANGED_PROPERTY, this);
		temp.add(fgCheckBox);
		temp.add(foregroundButton);
		temp.add(Box.createHorizontalGlue());
		propertiesPanel.add(temp);
		propertiesPanel.add(Box.createVerticalStrut(8));
		temp = createHorizontalBox();
		temp.setAlignmentX(Component.LEFT_ALIGNMENT);
		bgCheckBox = new JCheckBox(msg.getString("Background"));
		bgCheckBox.setActionCommand("bgCheckBox");
		bgCheckBox.addActionListener(this);
		backgroundButton = new RColorSwatchesButton(Color.BLACK);
		backgroundButton.addPropertyChangeListener(
						RColorButton.COLOR_CHANGED_PROPERTY, this);
		temp.add(bgCheckBox);
		temp.add(backgroundButton);
		temp.add(Box.createHorizontalGlue());
		propertiesPanel.add(temp);
		propertiesPanel.add(Box.createVerticalGlue());

		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(propertiesPanel, BorderLayout.NORTH);
		syntaxPanel.add(temp2);

		// Add the syntax panel to us.
		contentPane.add(syntaxPanel);

		// Now create a panel containing all checkbox properties in the
		// bottom of this panel.
		temp = Box.createVerticalBox();
		temp.setBorder(new OptionPanelBorder(msg.getString("Preview")));
		Box horizBox = createHorizontalBox();
		horizBox.add(new JLabel(msg.getString("SampleTextLabel")));
		horizBox.add(Box.createHorizontalStrut(5));
		final String[] samples = { "Java", "Perl", "XML", };
		sampleCombo = new JComboBox(samples);
		sampleCombo.setEditable(false);
		sampleCombo.addActionListener(this);
		horizBox.add(sampleCombo);
		horizBox.add(Box.createHorizontalGlue());
		addLeftAligned(temp, horizBox, 3);
		sampleArea = createSampleTextArea();
		temp.add(new RScrollPane(sampleArea));
		temp.add(Box.createVerticalStrut(3));

		Box rdPanel = createHorizontalBox();
		JButton rdButton = new JButton(msg.getString("RestoreDefaults"));
		rdButton.setActionCommand("RestoreDefaults");
		rdButton.addActionListener(this);
		rdPanel.add(rdButton);
		rdPanel.add(Box.createHorizontalGlue());

		// Create a panel containing all "Advanced" stuff.
		JPanel advancedPanel = new JPanel(new BorderLayout());
		advancedPanel.add(temp);
		advancedPanel.add(rdPanel, BorderLayout.SOUTH);

		contentPane.add(advancedPanel);
		//contentPane.add(Box.createVerticalGlue());
		applyComponentOrientation(orientation);

	}


	/**
	 * Listens for actions in this panel.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals("BackgroundButton")) {
			if (backgroundDialog==null) {
				backgroundDialog = new BackgroundDialog(getOptionsDialog());
			}
			backgroundDialog.initializeData(background, bgImageFileName);
			backgroundDialog.setVisible(true);
			Object newBG = backgroundDialog.getChosenBackground();
			// Non-null newBG means user hit OK, not Cancel.
			if (newBG!=null && !newBG.equals(background)) {
				Object oldBG = background;
				setBackgroundObject(newBG);
				setBackgroundImageFileName(backgroundDialog.
										getCurrentImageFileName());
				refreshSyntaxHighlightingSection();
				hasUnsavedChanges = true;
				firePropertyChange(UNKNOWN_PROPERTY, oldBG, newBG);
			}
		}

		// If the user clicked the "foreground" check box.
		else if ("fgCheckBox".equals(command) && !isSettingStyle) {
			boolean selected = fgCheckBox.isSelected();
			foregroundButton.setEnabled(selected);
			// Update our color scheme.
			int i = syntaxList.getSelectedIndex();
			colorScheme.getStyle(indexToStyle(i)).foreground =
						selected ? foregroundButton.getColor() : null;
			refreshSyntaxHighlightingSection();
			hasUnsavedChanges = true;
			firePropertyChange(SYNTAX_COLOR_PROPERTY, null, null);
		}

		// If the user clicked the "background" check box.
		else if ("bgCheckBox".equals(command) && !isSettingStyle) {
			boolean selected = bgCheckBox.isSelected();
			backgroundButton.setEnabled(selected);
			// Update our color scheme.
			int i = syntaxList.getSelectedIndex();
			colorScheme.getStyle(indexToStyle(i)).background =
						selected ? backgroundButton.getColor() : null;
			refreshSyntaxHighlightingSection();
			hasUnsavedChanges = true;
			firePropertyChange(SYNTAX_COLOR_PROPERTY, null, null);
		}

		// Changing the canned sample text.
		else if (sampleCombo==e.getSource()) {
			refreshDisplayedSample();
		}

		// If the user clicked the "restore defaults" button.
		else if ("RestoreDefaults".equals(command)) {

			Font defaultFont = RTextArea.getDefaultFont();
			Color defaultForeground = RTextArea.getDefaultForeground();
			SyntaxScheme currentScheme = getSyntaxScheme();
			SyntaxScheme defaultScheme = new SyntaxScheme(true);

			if (!getTextAreaFont().equals(defaultFont) ||
				getUnderline()==true ||
				!getTextAreaForeground().equals(defaultForeground) ||
				!Color.WHITE.equals(background) ||
				!currentScheme.equals(defaultScheme))
			{
				setBackgroundObject(Color.WHITE);
				setTextAreaFont(defaultFont, false);
				setTextAreaForeground(defaultForeground);
				setSyntaxScheme(defaultScheme);
				refreshSyntaxHighlightingSection();
				hasUnsavedChanges = true;
				firePropertyChange(DEFAULTS_RESTORED, null, null);
				// Force a repaint of the preview panel.
				valueChanged(null);
			}

		}

	}


	/**
	 * Creates the text area used to display sample syntax highlighting.
	 *
	 * @return The text area.
	 */
	private static final RSyntaxTextArea createSampleTextArea() {
		RSyntaxTextArea textArea = new RSyntaxTextArea(10, 40);
		textArea.setHighlightCurrentLine(false);
		textArea.setAntiAliasingEnabled(true);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		textArea.setPopupMenu(null); // Disable the popup menu.
		return textArea;
	}


	/**
	 * Applies the settings entered into this dialog on the specified
	 * application.
	 *
	 * @param owner The application.
	 */
	@Override
	protected void doApplyImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		mainView.setTextAreaForeground(getTextAreaForeground());
		mainView.setTextAreaFont(getTextAreaFont(), getUnderline());
		mainView.setBackgroundObject(getBackgroundObject());
		mainView.setBackgroundImageFileName(getBackgroundImageFileName());
		rtext.setSyntaxScheme(getSyntaxScheme()); // Doesn't update if it doesn't have to.
	}


	/**
	 * Checks whether or not all input the user specified on this panel is
	 * valid.
	 *
	 * @return This method always returns <code>null</code> as the user cannot
	 *         mess up input in this panel.
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * Returns the name of the file containing the chosen background image.
	 * If the user selected a color for the background, this method returns
	 * <code>null</code>.
	 *
	 * @return The name of the file containing the chosen background image.
	 * @see #getBackgroundObject()
	 */
	public String getBackgroundImageFileName() {
		return bgImageFileName;
	}


	/**
	 * Returns the background object (a color or an image) selected by the
	 * user.
	 *
	 * @return The background object.
	 * @see #getBackgroundImageFileName()
	 */
	public Object getBackgroundObject() {
		return background;
	}


	/**
	 * Returns the text to use in the preview label.
	 *
	 * @param underline Whether the preview text should be underlined.
	 * @return The text the label should display.
	 */
	protected static String getPreviewText(boolean underline) {
		return underline ? "<html><u>Hello, world!</u>" :
							"Hello, world!";
	}


	/**
	 * Returns the syntax highlighting color scheme the user chose.
	 *
	 * @return The syntax highlighting color scheme.
	 * @see #setSyntaxScheme
	 */
	public SyntaxScheme getSyntaxScheme() {
		return colorScheme;
	}


	/**
	 * Returns the font to use in text areas.
	 *
	 * @return The font to use.
	 */
	public Font getTextAreaFont() {
		return mainFontSelector.getDisplayedFont();
	}


	/**
	 * Returns the text area's foreground color.
	 *
	 * @return The foreground color of the text area.
	 */
	public Color getTextAreaForeground() {
		return mainFontSelector.getFontColor();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public JComponent getTopJComponent() {
		return mainFontSelector;
	}


	/**
	 * Returns whether the text area's font should be underlined.
	 *
	 * @return Whether the text areas should underline their font.
	 * @see #getFont()
	 */
	public boolean getUnderline() {
		return mainFontSelector.getUnderline();
	}


	/**
	 * Converts an index in the list to a <code>Style</code> index in a
	 * <code>SyntaxScheme</code>.  This is necessary because the list does
	 * not display an item for <code>Style</code> index 0 (token type
	 * <code>NULL</code>).
	 *
	 * @param index An index into the <code>JList</code>.
	 * @return The corresponding index into a <code>SyntaxScheme</code>.
	 */
	private static final int indexToStyle(int index) {
		return index + 1;
	}


	/**
	 * Called when a property changes in an object we're listening to.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		if (isSettingStyle)
			return;

		Object source = e.getSource();

		// We need to forward this on to the options dialog, whatever
		// it is, so that the "Apply" button gets updated.

		if (source==mainFontSelector) {
			sampleArea.setFont(mainFontSelector.getDisplayedFont());
			sampleArea.setForeground(mainFontSelector.getFontColor());
			refreshSyntaxHighlightingSection();
			valueChanged(null); // Just to refresh fontSelector.  TODO: refactor
			hasUnsavedChanges = true;
			firePropertyChange(UNKNOWN_PROPERTY, false, true);
		}

		// FontSelectors fire properties when users interactively update
		// the font or its properties.
		else if (source==fontSelector && colorScheme!=null) {

			int i = syntaxList.getSelectedIndex();
			Style style = colorScheme.getStyle(indexToStyle(i));
			style.fontMetrics = null;

			if (fontSelector.isToggledOn()) {
				style.font = fontSelector.getDisplayedFont();
				style.underline = fontSelector.getUnderline();
			}
			else {
				//underline = parent.getUnderline();
				style.font = null;
				style.underline = false;
			}

			refreshSyntaxHighlightingSection();
			hasUnsavedChanges = true;
			firePropertyChange(SYNTAX_FONT_PROPERTY, e.getOldValue(),
							e.getNewValue());

		}

		else if (source==foregroundButton && colorScheme!=null) {
			Color fg = null;
			if (foregroundButton.isEnabled()) {
				fg = foregroundButton.getColor();
			}
			else {
				fg = getTextAreaForeground();
			}
			// Method valueChanged() will cause this method to get fired,
			// so we must make sure we're not in it.
			if (!isSettingStyle) {
				// Update our color scheme.
				int i = syntaxList.getSelectedIndex();
				colorScheme.getStyle(indexToStyle(i)).foreground = fg;
				refreshSyntaxHighlightingSection();
				hasUnsavedChanges = true;
				firePropertyChange(SYNTAX_COLOR_PROPERTY, e.getOldValue(),
												e.getNewValue());
			}
		}

		else if (source==backgroundButton && colorScheme!=null) {
			Color bg = null;
			if (backgroundButton.isEnabled()) {
				bg = backgroundButton.getColor();
			}
			else {
				Object bgObj = getBackgroundObject();
				bg = bgObj instanceof Color ? (Color)bgObj : Color.WHITE;
			}
			// Method valueChanged() will cause this method to get fired,
			// so we must make sure we're not in it.
			if (!isSettingStyle) {
				// Update our color scheme.
				int i = syntaxList.getSelectedIndex();
				colorScheme.getStyle(indexToStyle(i)).background = bg;
				refreshSyntaxHighlightingSection();
				hasUnsavedChanges = true;
				firePropertyChange(SYNTAX_COLOR_PROPERTY, e.getOldValue(),
												e.getNewValue());
			}
		}

		else {
			refreshSyntaxHighlightingSection();
			hasUnsavedChanges = true;
			firePropertyChange(UNKNOWN_PROPERTY,
							e.getOldValue(), e.getNewValue());
		}
		
	}


	/**
	 * Refreshes the displayed sample text.
	 */
	private void refreshDisplayedSample() {
		int index = sampleCombo.getSelectedIndex();
		if (index<0 || index>SAMPLES.length) {
			index = 0;
		}
		InputStream in = getClass().getResourceAsStream(SAMPLES[index]);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		try {
			sampleArea.read(br, null);
			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		sampleArea.setCaretPosition(0);
		sampleArea.setSyntaxEditingStyle(SAMPLE_STYLES[index]);
		sampleArea.discardAllEdits();
	}


	/**
	 * Refreshes the "Syntax Highlighting" section.  Should be called whenever
	 * higher-level text area properties (font, background, etc.) are modified.
	 */
	private void refreshSyntaxHighlightingSection() {
		sampleArea.repaint();
	}


	/**
	 * Sets the name of the file containing the background image.  If the
	 * initial background object is a color, you should pass <code>null</code>
	 * to this method.
	 *
	 * @param name The name of the file containing the background image.
	 * @see #getBackgroundImageFileName
	 * @see #setBackgroundObject
	 */
	private void setBackgroundImageFileName(String name) {
		bgImageFileName = name;
		if (bgImageFileName!=null)
			mainBackgroundField.setText(bgImageFileName);
	}


	/**
	 * Sets the background object displayed in this options panel.
	 *
	 * @param background The background object.
	 * @see #getBackgroundObject
	 */
	private void setBackgroundObject(Object background) {
		if (background instanceof Color) {
			String s = background.toString();
			mainBackgroundField.setText(s.substring(s.indexOf('[')));
		}
		else if (background instanceof Image) {
			// backgroundField taken care of by setBackgroundImageFileName.
		}
		else {
			throw new IllegalArgumentException("Background must be either " +
				"a Color or an Image");
		}
		this.background = background;
		Object sampleBG = background;
		if (sampleBG instanceof Image) { // Lighten as RText will
			RText parent = (RText)getOptionsDialog().getParent();
			sampleBG = RTextUtilities.getTranslucentImage(parent,
					(Image)sampleBG,
					parent.getMainView().getBackgroundImageAlpha());
		}
		sampleArea.setBackgroundObject(sampleBG);
		refreshSyntaxHighlightingSection();
	}


	/**
	 * Sets the syntax highlighting color scheme being displayed in this
	 * panel.
	 *
	 * @param ss The syntax highlighting color scheme to display.
	 * @see #getSyntaxScheme
	 */
	public void setSyntaxScheme(SyntaxScheme ss) {
		colorScheme = (SyntaxScheme)ss.clone();
		syntaxList.setSelectedIndex(0);
		sampleArea.setSyntaxScheme(colorScheme);
		// We must do manually call valueChanged() below as, if the selected
		// index was already 0, the above call won't trigger the valueChanged
		// callback.  If it isn't triggered, then the syntaxPreviewPanel and
		// color buttons won't be updated...
		valueChanged(null);
	}


	/**
	 * Sets the font/underline status to display for text areas.
	 *
	 * @param font The font.
	 * @param underline Whether the font is underlined.
	 * @see #getTextAreaFont()
	 * @see #getUnderline()
	 */
	private void setTextAreaFont(Font font, boolean underline) {
		mainFontSelector.setDisplayedFont(font, underline);
		sampleArea.setFont(font);
		refreshDisplayedSample();
	}


	/**
	 * Sets the foreground color for text areas.
	 *
	 * @param fg The new foreground color.
	 * @see #getTextAreaForeground()
	 */
	private void setTextAreaForeground(Color fg) {
		mainFontSelector.setFontColor(fg);
		sampleArea.setForeground(fg);
		refreshDisplayedSample();
	}


	/**
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are not handled.
	 *
	 * @param owner The parent application.
	 * @see #setValues(Frame)
	 */
	@Override
	protected void setValuesImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		setTextAreaForeground(mainView.getTextAreaForeground());
		setTextAreaFont(mainView.getTextAreaFont(), mainView.getTextAreaUnderline());
		setBackgroundObject(mainView.getBackgroundObject());
		setBackgroundImageFileName(mainView.getBackgroundImageFileName());
		setSyntaxScheme(rtext.getSyntaxScheme());
		if (sampleArea.getDocument().getLength()==0) { // First time through
			refreshDisplayedSample();
		}
	}


	/**
	 * Overridden to ensure the background dialog is updated as well.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		if (backgroundDialog!=null) {
			SwingUtilities.updateComponentTreeUI(backgroundDialog); // Updates dialog.
			backgroundDialog.updateUI(); // Updates image file chooser.
		}
	}


	/**
	 * Called when the user changes the selection in the style list.
	 *
	 * @param e The list selection event.
	 */
	public void valueChanged(ListSelectionEvent e) {

		int index = syntaxList.getSelectedIndex();
		index = indexToStyle(index); // To get index into schemes.
		isSettingStyle = true;

		Font font = null;
		boolean underline = false;
		Style style = colorScheme.getStyle(index);
		if (style.font!=null) {
			font = style.font;
			underline = style.underline;
		}
		else {
			font = getTextAreaFont();
			underline = getUnderline();
		}

		// Update our color style.
		fontSelector.setToggledOn(style.font!=null);
		fontSelector.setDisplayedFont(font, underline);

		boolean notNull = style.foreground!=null;
		fgCheckBox.setSelected(notNull);
		foregroundButton.setEnabled(notNull);
		foregroundButton.setColor(notNull ? style.foreground : 
									getTextAreaForeground());
		notNull = style.background!=null;
		bgCheckBox.setSelected(notNull);
		backgroundButton.setEnabled(notNull);
		if (style.background!=null) {
			backgroundButton.setColor(style.background);
		}
		else {
			Object parentBG = getBackgroundObject();
			if (parentBG instanceof Color) {
				backgroundButton.setColor((Color)parentBG);
			}
			else {
				backgroundButton.setColor(Color.WHITE);
			}
		}

		isSettingStyle = false;

	}


}