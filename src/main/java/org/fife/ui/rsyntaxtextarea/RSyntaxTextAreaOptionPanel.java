/*
 * 02/26/2004
 *
 * RSyntaxTextAreaOptionsPanel.java - An options panel that can be
 * added to org.fife.ui.OptionsDialog for an RSyntaxTextArea.
 * Copyright (C) 2004 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.*;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.*;


/**
 * An options panel that can be added to an
 * <code>org.fife.ui.OptionsDialog</code> to display options appropriate
 * for an <code>RSyntaxTextArea</code>.  This panel allows the user to
 * customize the fonts and colors used in the editor.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RSyntaxTextAreaOptionPanel extends AbstractTextAreaOptionPanel
					implements PropertyChangeListener, ListSelectionListener,
								EditorOptionsPreviewContextListener {

	/**
	 * ID used to identify this option panel.
	 */
	public static final String OPTION_PANEL_ID = "RTextAreaOptionPanel";

	private JTextField mainBackgroundField;
	private JButton mainBackgroundButton;
	private BackgroundDialog backgroundDialog;
	private Object background;
	private String bgImageFileName; // null if background is a color.

	private JList<String> syntaxList;
	private FontSelector fontSelector;
	private RColorSwatchesButton foregroundButton;
	private JCheckBox fgCheckBox;
	private JCheckBox bgCheckBox;
	private RColorSwatchesButton backgroundButton;

	private SyntaxScheme colorScheme;

	private boolean isSettingStyle;


	/**
	 * Constructor.
	 */
	public RSyntaxTextAreaOptionPanel() {

		setId(OPTION_PANEL_ID);
		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setName(MSG.getString("Title.SyntaxHighlighting"));

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());
		Box cp = Box.createVerticalBox();
		add(cp, BorderLayout.NORTH);

		cp.add(createOverridePanel());
		cp.add(Box.createVerticalStrut(5));

		cp.add(createBackgroundPanel(orientation));
		cp.add(Box.createVerticalStrut(5));

		cp.add(createSyntaxPanel(orientation));
		cp.add(Box.createVerticalStrut(5));

		// Create a panel containing the preview and "Restore Defaults"
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(new PreviewPanel(MSG, 9, 40));
		bottomPanel.add(createRestoreDefaultsPanel(), BorderLayout.SOUTH);

		cp.add(bottomPanel);
		applyComponentOrientation(orientation);
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if ("BackgroundButton".equals(command)) {
			if (backgroundDialog==null) {
				backgroundDialog = new BackgroundDialog(getOptionsDialog());
			}
			backgroundDialog.initializeData(background, bgImageFileName);
			backgroundDialog.setVisible(true);
			Object newBG = backgroundDialog.getChosenBackground();
			// Non-null newBG means user hit OK, not Cancel.
			if (newBG!=null && !newBG.equals(background)) {
				setBackgroundObject(newBG);
				setBackgroundImageFileName(backgroundDialog.
										getCurrentImageFileName());
				setDirty(true);
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
			setDirty(true);
		}

		// If the user clicked the "background" check box.
		else if ("bgCheckBox".equals(command) && !isSettingStyle) {
			boolean selected = bgCheckBox.isSelected();
			backgroundButton.setEnabled(selected);
			// Update our color scheme.
			int i = syntaxList.getSelectedIndex();
			colorScheme.getStyle(indexToStyle(i)).background =
						selected ? backgroundButton.getColor() : null;
			setDirty(true);
		}

		else {
			super.actionPerformed(e);
		}
	}


	private JComponent createBackgroundPanel(ComponentOrientation orientation) {

		JPanel springPanel = new JPanel(new SpringLayout());
		springPanel.setBorder(new OptionPanelBorder(MSG.getString("Background")));

		JLabel bgLabel = new JLabel(MSG.getString("Background"));
		mainBackgroundField = new JTextField(20);
		mainBackgroundField.setEditable(false);
		mainBackgroundButton = new JButton(MSG.getString("Change"));
		mainBackgroundButton.setActionCommand("BackgroundButton");
		mainBackgroundButton.addActionListener(this);
		bgLabel.setLabelFor(mainBackgroundButton);

		Box bgRestPanel = createHorizontalBox();
		bgRestPanel.add(mainBackgroundField);
		bgRestPanel.add(Box.createHorizontalStrut(5));
		bgRestPanel.add(mainBackgroundButton);

		UIUtil.addLabelValuePairs(springPanel, orientation,
			bgLabel, bgRestPanel);
		UIUtil.makeSpringCompactGrid(springPanel, 1, 2, 0, 0, 5, 5);
		return springPanel;
	}


	private JPanel createSyntaxPanel(ComponentOrientation orientation) {

		JPanel syntaxPanel = new JPanel(new BorderLayout());
		syntaxPanel.setBorder(BorderFactory.createCompoundBorder(
			new OptionPanelBorder(MSG.getString("FontsAndColors")),
			BorderFactory.createEmptyBorder(5, 0, 0, 0)));

		// Add the token style selection panel to the right.
		DefaultListModel<String> syntaxListModel = new DefaultListModel<>();
		syntaxList = new JList<>(syntaxListModel);
		syntaxList.setSelectionModel(new RListSelectionModel());
		syntaxList.addListSelectionListener(this);
		syntaxList.setVisibleRowCount(8);
		syntaxListModel.addElement(MSG.getString("Style.Comment.EndOfLine"));
		syntaxListModel.addElement(MSG.getString("Style.Comment.Multiline"));
		syntaxListModel.addElement(MSG.getString("Style.Comment.Documentation"));
		syntaxListModel.addElement(MSG.getString("Style.Comment.Keyword"));
		syntaxListModel.addElement(MSG.getString("Style.Comment.Markup"));
		syntaxListModel.addElement(MSG.getString("Style.ReservedWord"));
		syntaxListModel.addElement(MSG.getString("Style.ReservedWord2"));
		syntaxListModel.addElement(MSG.getString("Style.Function"));
		syntaxListModel.addElement(MSG.getString("Style.Literal.Boolean"));
		syntaxListModel.addElement(MSG.getString("Style.Literal.Integer"));
		syntaxListModel.addElement(MSG.getString("Style.Literal.Float"));
		syntaxListModel.addElement(MSG.getString("Style.Literal.Hex"));
		syntaxListModel.addElement(MSG.getString("Style.Literal.String"));
		syntaxListModel.addElement(MSG.getString("Style.Literal.Char"));
		syntaxListModel.addElement(MSG.getString("Style.Literal.Backquote"));
		syntaxListModel.addElement(MSG.getString("Style.DataType"));
		syntaxListModel.addElement(MSG.getString("Style.Variable"));
		syntaxListModel.addElement(MSG.getString("Style.RegularExpression"));
		syntaxListModel.addElement(MSG.getString("Style.Annotation"));
		syntaxListModel.addElement("<html><b>"+ MSG.getString("Style.Identifier.PlainText"));
		syntaxListModel.addElement(MSG.getString("Style.Whitespace"));
		syntaxListModel.addElement(MSG.getString("Style.Separator"));
		syntaxListModel.addElement(MSG.getString("Style.Operator"));
		syntaxListModel.addElement(MSG.getString("Style.Preprocessor"));
		syntaxListModel.addElement(MSG.getString("Style.MarkupTag.Delimiter"));
		syntaxListModel.addElement(MSG.getString("Style.MarkupTag.TagName"));
		syntaxListModel.addElement(MSG.getString("Style.MarkupTag.Attribute"));
		syntaxListModel.addElement(MSG.getString("Style.MarkupTag.AttributeValue"));
		syntaxListModel.addElement(MSG.getString("Style.MarkupTag.Comment"));
		syntaxListModel.addElement(MSG.getString("Style.MarkupTag.DTD"));
		syntaxListModel.addElement(MSG.getString("Style.MarkupTag.ProcessingInstruction"));
		syntaxListModel.addElement(MSG.getString("Style.MarkupTag.CDataDelimiter"));
		syntaxListModel.addElement(MSG.getString("Style.MarkupTag.CData"));
		syntaxListModel.addElement(MSG.getString("Style.MarkupTag.EntityReference"));
		syntaxListModel.addElement(MSG.getString("Style.Error.Identifier"));
		syntaxListModel.addElement(MSG.getString("Style.Error.Number"));
		syntaxListModel.addElement(MSG.getString("Style.Error.String"));
		syntaxListModel.addElement(MSG.getString("Style.Error.Char"));
		syntaxPanel.add(new RScrollPane(syntaxList), BorderLayout.LINE_START);

		// Create a panel for other syntax style properties.
		Box propertiesPanel = Box.createVerticalBox();

		// Create a panel for selecting the font.
		fontSelector = new FontSelector(FontSelector.CHECK_BOX);
		fontSelector.setUnderlineSelectable(true);
		fontSelector.addPropertyChangeListener(FontSelector.FONT_PROPERTY, this);
		fontSelector.addPropertyChangeListener(FontSelector.ENABLED_PROPERTY, this);
		fontSelector.putClientProperty(UIUtil.PROPERTY_ALWAYS_IGNORE, Boolean.TRUE);

		// Just to keep it right-aligned with stuff above...
		Box temp = createHorizontalBox();
		temp.add(fontSelector);
		temp.add(Box.createHorizontalStrut(5));
		propertiesPanel.add(temp);
		propertiesPanel.add(Box.createVerticalStrut(8));

		// Add the foreground and background buttons to the properties panel.
		temp = createHorizontalBox();
		fgCheckBox = new JCheckBox(MSG.getString("Foreground"));
		fgCheckBox.setActionCommand("fgCheckBox");
		fgCheckBox.addActionListener(this);
		foregroundButton = new RColorSwatchesButton(Color.BLACK);
		foregroundButton.addPropertyChangeListener(
			RColorButton.COLOR_CHANGED_PROPERTY, this);
		foregroundButton.putClientProperty(UIUtil.PROPERTY_ALWAYS_IGNORE, Boolean.TRUE);
		temp.add(fgCheckBox);
		temp.add(Box.createHorizontalStrut(5));
		temp.add(foregroundButton);
		temp.add(Box.createHorizontalGlue());
		addLeftAligned(propertiesPanel, temp, 8);
		temp = createHorizontalBox();
		bgCheckBox = new JCheckBox(MSG.getString("Background"));
		bgCheckBox.setActionCommand("bgCheckBox");
		bgCheckBox.addActionListener(this);
		backgroundButton = new RColorSwatchesButton(Color.BLACK);
		backgroundButton.addPropertyChangeListener(
			RColorButton.COLOR_CHANGED_PROPERTY, this);
		backgroundButton.putClientProperty(UIUtil.PROPERTY_ALWAYS_IGNORE, Boolean.TRUE);
		temp.add(bgCheckBox);
		temp.add(Box.createHorizontalStrut(5));
		temp.add(backgroundButton);
		temp.add(Box.createHorizontalGlue());
		addLeftAligned(propertiesPanel, temp);
		propertiesPanel.add(Box.createVerticalGlue());

		JPanel temp2 = new JPanel(new BorderLayout());
		if (orientation.isLeftToRight()) {
			temp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		}
		else {
			temp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		}
		temp2.add(propertiesPanel, BorderLayout.NORTH);
		syntaxPanel.add(temp2);

		return syntaxPanel;
	}


	@Override
	protected void doApplyImpl(Frame owner) {

		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		mainView.setOverrideEditorStyles(overrideCheckBox.isSelected());

		if (overrideCheckBox.isSelected()) {
			mainView.setBackgroundObject(getBackgroundObject());
			mainView.setBackgroundImageFileName(getBackgroundImageFileName());
			rtext.setSyntaxScheme(colorScheme); // Doesn't update if it doesn't have to.
		}
		else {
			Theme editorTheme = EditorOptionsPreviewContext.get().getEditorTheme(rtext);
			mainView.setBackgroundObject(editorTheme.bgColor);
			mainView.setBackgroundImageFileName(null);
			rtext.setSyntaxScheme(editorTheme.scheme); // Doesn't update if it doesn't have to.
		}
	}


	@Override
	public void editorOptionsPreviewContextChanged(EditorOptionsPreviewContext context) {

		// If the tracked syntax scheme changed (i.e. the default font changed), we
		// must update this panel as well
		SyntaxScheme newScheme = context.getSyntaxScheme();
		if (!newScheme.equals(colorScheme)) {
			setSyntaxScheme(newScheme);
			setDirty(true);
		}

		super.editorOptionsPreviewContextChanged(context);
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


	@Override
	protected void handleRestoreDefaults() {

		// This panel's defaults are based on the current theme.
		RText app = (RText)getOptionsDialog().getParent();
		EditorOptionsPreviewContext editorContext = EditorOptionsPreviewContext.get();
		Theme rstaTheme = editorContext.getEditorTheme(app);

		Color defaultBackground = rstaTheme.bgColor;
		SyntaxScheme currentScheme = colorScheme;
		SyntaxScheme defaultScheme = rstaTheme.scheme;

		if (overrideCheckBox.isSelected() ||
			!defaultBackground.equals(background) ||
			!currentScheme.equals(defaultScheme)) {
			overrideCheckBox.setSelected(false);
			setBackgroundObject(defaultBackground);
			setSyntaxScheme(defaultScheme);
			setDirty(true);
			// Force a repaint of the preview panel.
			valueChanged(null);
			// Some custom components have child components that enable/disable
			// on their own criteria, so we're explicit here since it's the most
			// straightforward way to ensure the UI is disabled as expected
			setComponentsEnabled(false);
		}

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
	private static int indexToStyle(int index) {
		return index + 1;
	}


	/**
	 * Called when a property changes in an object we're listening to.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {

		if (isSettingStyle)
			return;

		Object source = e.getSource();

		// We need to forward this on to the options dialog, whatever
		// it is, so that the "Apply" button gets updated.

		// FontSelectors fire properties when users interactively update
		// the font or its properties.
		if (source==fontSelector && colorScheme!=null) {

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

			setDirty(true);

		}

		else if (source==foregroundButton && colorScheme!=null) {
			Color fg = null;
			if (foregroundButton.isEnabled()) {
				fg = foregroundButton.getColor();
			}
			// Method valueChanged() will cause this method to get fired,
			// so we must make sure we're not in it.
			if (!isSettingStyle) {
				// Update our color scheme.
				int i = syntaxList.getSelectedIndex();
				colorScheme.getStyle(indexToStyle(i)).foreground = fg;
				setDirty(true);
			}
		}

		else if (source==backgroundButton && colorScheme!=null) {
			Color bg;
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
				setDirty(true);
			}
		}

		else {
			setDirty(true);
		}

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
			sampleBG = UIUtil.getTranslucentImage(parent,
					(Image)sampleBG,
					parent.getMainView().getBackgroundImageAlpha());
		}
	}


	@Override
	protected void setComponentsEnabled(boolean enabled, Component... ignore) {

		super.setComponentsEnabled(enabled, ignore);

		// These components ignore the global change above since they have additional
		// conditions to check to determine whether they are enabled
		fontSelector.setEnabled(enabled);
		foregroundButton.setEnabled(enabled && fgCheckBox.isSelected());
		backgroundButton.setEnabled(enabled && bgCheckBox.isSelected());
	}


	/**
	 * Sets the syntax highlighting color scheme being displayed in this
	 * panel.
	 *
	 * @param ss The syntax highlighting color scheme to display.
	 */
	public void setSyntaxScheme(SyntaxScheme ss) {
		colorScheme = (SyntaxScheme)ss.clone();
		syntaxList.setSelectedIndex(0);
		// We must do manually call valueChanged() below as, if the selected
		// index was already 0, the above call won't trigger the valueChanged
		// callback.  If it isn't triggered, then the syntaxPreviewPanel and
		// color buttons won't be updated...
		valueChanged(null);
	}


	@Override
	protected void setValuesImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		setBackgroundObject(mainView.getBackgroundObject());
		setBackgroundImageFileName(mainView.getBackgroundImageFileName());
		setSyntaxScheme(rtext.getSyntaxScheme());

		// Do this after initializing all values above
		overrideCheckBox.setSelected(mainView.getOverrideEditorStyles());
		setComponentsEnabled(overrideCheckBox.isSelected());

		syncEditorOptionsPreviewContext();
	}


	@Override
	protected void syncEditorOptionsPreviewContext() {
		EditorOptionsPreviewContext context = EditorOptionsPreviewContext.get();
		context.setOverrideEditorTheme(overrideCheckBox.isSelected());
		context.setBackgroundObject(getBackgroundObject());
		context.setSyntaxScheme((SyntaxScheme)colorScheme.clone());
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
	@Override
	public void valueChanged(ListSelectionEvent e) {

		int index = syntaxList.getSelectedIndex();
		index = indexToStyle(index); // To get index into schemes.
		isSettingStyle = true;

		Font font;
		boolean underline;
		Style style = colorScheme.getStyle(index);
		if (style.font!=null) {
			font = style.font;
			underline = style.underline;
		}
		else {
			font = EditorOptionsPreviewContext.get().getFont();
			underline = false; // Default font can't be set to underline
		}

		// Update our color style.
		fontSelector.setToggledOn(style.font!=null);
		fontSelector.setDisplayedFont(font, underline);

		boolean notNull = style.foreground!=null;
		fgCheckBox.setSelected(notNull);
		foregroundButton.setEnabled(notNull);
		foregroundButton.setColor(notNull ? style.foreground :
			EditorOptionsPreviewContext.get().getFontColor());
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
