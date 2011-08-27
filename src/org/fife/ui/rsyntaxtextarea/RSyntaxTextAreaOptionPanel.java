/*
 * 02/26/2004
 *
 * RSyntaxTextAreaOptionsPanel.java - An options panel that can be
 * added to org.fife.ui.OptionsDialog for an RSyntaxTextArea.
 * Copyright (C) 2004 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://rtext.fifesoft.com
 *
 * This file is a part of RText.
 *
 * RText is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * RText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.ui.rsyntaxtextarea;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.*;

import org.fife.ui.RListSelectionModel;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rtextarea.RTextAreaOptionPanel;


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

	private JPanel syntaxPanel;

	private JList syntaxList;
	private DefaultListModel syntaxListModel;
	private FontSelector fontSelector;
	private RColorSwatchesButton foregroundButton;
	private JCheckBox fgCheckBox;
	private JCheckBox bgCheckBox;
	private RColorSwatchesButton backgroundButton;
	private JPanel previewPanel;
	private JLabel stylePreviewLabel;

	private SyntaxScheme colorScheme;

	private Box bracketMatchingPanel;
	private JCheckBox bracketMatchCheckBox;
	private JLabel bmBGColorLabel;
	private RColorSwatchesButton bmBGColorButton;
	private JLabel bmBorderColorLabel;
	private RColorSwatchesButton bmBorderColorButton;

	private JCheckBox visibleWhitespaceCheckBox;
	private JCheckBox visibleEOLCheckBox;

//	private JCheckBox autoIndentCheckBox;
	private JCheckBox remWhitespaceLinesCheckBox;
	private JCheckBox autoInsertClosingCurlyCheckBox;
	private JCheckBox showTabLinesCheckBox;
	private RColorSwatchesButton tabLineColorButton;

	private JCheckBox aaCheckBox;
	private JCheckBox fractionalMetricsCheckBox;

	private boolean isSettingStyle;

	private static final String BRACKET_MATCHING_PROPERTY		= "RSTAOpts.bracketMatchCheckBox";
	private static final String BRACKET_MATCHING_BG_PROPERTY	= "RSTAOpts.bracketMatchBackgroundColor";
	private static final String BM_BORDER_PROPERTY			= "RSTAOpts.bracketMatchBorderColor";
	private static final String DEFAULTS_RESTORED			= "RSTAOpts.defaultsRestored";
	private static final String FRACTIONAL_METRICS_PROPERTY	= "RSTAOpts.fractionalFontMetrics";
	private static final String SHOW_TAB_LINES_PROPERTY			= "RSTAOpts.showTabLines";
	private static final String SMOOTH_TEXT_PROPERTY			= "RSTAOpts.smoothText";
	private static final String SYNTAX_COLOR_PROPERTY			= "RSTAOpts.syntaxColor";
	private static final String SYNTAX_FONT_PROPERTY			= "RSTAOpts.syntaxFont";
	private static final String TAB_LINES_COLOR_PROPERTY		= "RSTAOpts.tabLinesColor";
	private static final String UNKNOWN_PROPERTY				= "RSTAOpts.unknown";
	private static final String VISIBLE_EOL_PROPERTY			= "RSTAOps.visibleEOL";
	private static final String VISIBLE_WHITESPACE_PROPERTY	= "RSTAOpts.visibleWhitespace";


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

		// Syntax panel contains all of the "syntax highlighting" stuff.
		syntaxPanel = new JPanel(new BorderLayout());
		syntaxPanel.setBorder(new OptionPanelBorder(
								msg.getString("SyntaxHighlighting")));

		// Add the token style selection panel to the right.
		syntaxListModel = new DefaultListModel();
		syntaxList = new JList(syntaxListModel);
		syntaxList.setSelectionModel(new RListSelectionModel());
		syntaxList.addListSelectionListener(this);
		syntaxList.setVisibleRowCount(14);
		syntaxListModel.addElement(msg.getString("Style.Comment.EndOfLine"));
		syntaxListModel.addElement(msg.getString("Style.Comment.Multiline"));
		syntaxListModel.addElement(msg.getString("Style.Comment.Documentation"));
		syntaxListModel.addElement(msg.getString("Style.ReservedWord"));
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
		syntaxListModel.addElement("<html><b>"+msg.getString("Style.Identifier.PlainText"));
		syntaxListModel.addElement(msg.getString("Style.Whitespace"));
		syntaxListModel.addElement(msg.getString("Style.Separator"));
		syntaxListModel.addElement(msg.getString("Style.Operator"));
		syntaxListModel.addElement(msg.getString("Style.Preprocessor"));
		syntaxListModel.addElement(msg.getString("Style.MarkupTag.Delimiter"));
		syntaxListModel.addElement(msg.getString("Style.MarkupTag.TagName"));
		syntaxListModel.addElement(msg.getString("Style.MarkupTag.Attribute"));
		syntaxListModel.addElement(msg.getString("Style.Error.Identifier"));
		syntaxListModel.addElement(msg.getString("Style.Error.Number"));
		syntaxListModel.addElement(msg.getString("Style.Error.String"));
		syntaxListModel.addElement(msg.getString("Style.Error.Char"));
		syntaxPanel.add(new RScrollPane(syntaxList), BorderLayout.WEST);

		// Create a panel for other syntax style properties.
		Box propertiesPanel = Box.createVerticalBox();

		// Create a panel for selecting the font.
		fontSelector = new FontSelector(true);
		fontSelector.setUnderlineSelectable(true);
		fontSelector.addPropertyChangeListener(FontSelector.FONT_PROPERTY, this);
		fontSelector.addPropertyChangeListener(FontSelector.ENABLED_PROPERTY, this);
		propertiesPanel.add(fontSelector);
		propertiesPanel.add(Box.createVerticalStrut(8));

		// Add the foreground and background buttons to the properties panel.
		Box temp = Box.createHorizontalBox();
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
		propertiesPanel.add(Box.createHorizontalStrut(8));
		temp = Box.createHorizontalBox();
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
		propertiesPanel.add(Box.createHorizontalStrut(8));

		// Add the properties panel and a "preview" panel to the main
		// syntax panel.
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		temp2.add(propertiesPanel, BorderLayout.NORTH);
		previewPanel = new JPanel(new BorderLayout());
		JLabel previewLabel = new JLabel(msg.getString("Preview"));
		previewPanel.add(previewLabel, BorderLayout.NORTH);
		stylePreviewLabel = new JLabel("Fake", JLabel.CENTER);
		stylePreviewLabel.setBorder(
							BorderFactory.createLineBorder(Color.BLACK));
		stylePreviewLabel.setOpaque(true);
		previewPanel.add(stylePreviewLabel);
		temp2.add(previewPanel);
		syntaxPanel.add(temp2);

		// Add the syntax panel to us.
		contentPane.add(syntaxPanel);

		// Now create a panel containing all checkbox properties in the
		// bottom of this panel.
		temp = Box.createVerticalBox();
		temp.setBorder(new OptionPanelBorder(msg.getString("Advanced")));

		bracketMatchingPanel = Box.createHorizontalBox();
		bracketMatchCheckBox = new JCheckBox(msg.getString("HighlightMB"));
		bracketMatchCheckBox.setActionCommand("BracketMatchCheckBox");
		bracketMatchCheckBox.addActionListener(this);
		bmBGColorLabel = new JLabel(msg.getString("BackgroundFill"));
		bmBGColorButton = new RColorSwatchesButton(Color.BLACK, 50,15);
		bmBGColorButton.addPropertyChangeListener(this);
		bmBorderColorLabel = new JLabel(msg.getString("Border"));
		bmBorderColorButton = new RColorSwatchesButton(Color.BLACK, 50,15);
		bmBorderColorButton.addPropertyChangeListener(this);
		bracketMatchingPanel.add(bracketMatchCheckBox);
		bracketMatchingPanel.add(bmBGColorLabel);
		bracketMatchingPanel.add(bmBGColorButton);
		bracketMatchingPanel.add(Box.createHorizontalStrut(5));
		bracketMatchingPanel.add(bmBorderColorLabel);
		bracketMatchingPanel.add(bmBorderColorButton);
		bracketMatchingPanel.add(Box.createHorizontalGlue());
		temp.add(bracketMatchingPanel);

		temp.add(Box.createVerticalStrut(3));

		showTabLinesCheckBox = new JCheckBox(msg.getString("ShowIndentGuide"));
		showTabLinesCheckBox.setActionCommand("ShowIndentGuide");
		showTabLinesCheckBox.addActionListener(this);
		Box box = Box.createHorizontalBox();
		box.add(showTabLinesCheckBox);
		box.add(Box.createHorizontalStrut(5));
		tabLineColorButton = new RColorSwatchesButton(Color.black, 50,15);
		tabLineColorButton.addPropertyChangeListener(this);
		box.add(tabLineColorButton);
		box.add(Box.createHorizontalGlue());
		addLeftAligned(temp, box);

		temp.add(Box.createVerticalStrut(3));

		visibleWhitespaceCheckBox = new JCheckBox(msg.getString("VisibleWhitespace"));
		visibleWhitespaceCheckBox.setActionCommand("VisibleWhitespace");
		visibleWhitespaceCheckBox.addActionListener(this);
		addLeftAligned(temp, visibleWhitespaceCheckBox);

		temp.add(Box.createVerticalStrut(3));

		visibleEOLCheckBox = new JCheckBox(msg.getString("VisibleEOL"));
		visibleEOLCheckBox.setActionCommand("VisibleEOL");
		visibleEOLCheckBox.addActionListener(this);
		addLeftAligned(temp, visibleEOLCheckBox);

		autoInsertClosingCurlyCheckBox = new JCheckBox(msg.getString("AutoCloseCurlys"));
		autoInsertClosingCurlyCheckBox.setActionCommand("AutoCloseCurlys");
		autoInsertClosingCurlyCheckBox.addActionListener(this);
		addLeftAligned(temp, autoInsertClosingCurlyCheckBox);

		temp.add(Box.createVerticalStrut(3));

/*
		autoIndentCheckBox = new JCheckBox(msg.getString("AutoIndent"));
		autoIndentCheckBox.setActionCommand("AutoIndent");
		autoIndentCheckBox.addActionListener(this);
		addLeftAligned(temp, autoIndentCheckBox);

		temp.add(Box.createVerticalStrut(3));
*/
		remWhitespaceLinesCheckBox = new JCheckBox(msg.getString("RemWhitespaceLines"));
		remWhitespaceLinesCheckBox.setActionCommand("RemWhitespaceLines");
		remWhitespaceLinesCheckBox.addActionListener(this);
		addLeftAligned(temp, remWhitespaceLinesCheckBox);

		temp.add(Box.createVerticalStrut(3));


		aaCheckBox = new JCheckBox(msg.getString("SmoothText"));
		aaCheckBox.setActionCommand("aaCB");
		aaCheckBox.addActionListener(this);
		addLeftAligned(temp, aaCheckBox);

		temp.add(Box.createVerticalStrut(3));

		fractionalMetricsCheckBox = new JCheckBox(msg.getString("FracFM"));
		fractionalMetricsCheckBox.setActionCommand("FracFM");
		fractionalMetricsCheckBox.addActionListener(this);
		addLeftAligned(temp, fractionalMetricsCheckBox);

		temp.add(Box.createVerticalStrut(3));

		Box rdPanel = Box.createHorizontalBox();
		RButton rdButton = new RButton(msg.getString("RestoreDefaults"));
		rdButton.setActionCommand("RestoreDefaults");
		rdButton.addActionListener(this);
		rdPanel.add(rdButton);
		rdPanel.add(Box.createHorizontalGlue());

		// Create a panel containing all "Advanced" stuff.
		JPanel advancedPanel = new JPanel(new BorderLayout());
		advancedPanel.add(temp);
		advancedPanel.add(rdPanel, BorderLayout.SOUTH);

		contentPane.add(advancedPanel);
		contentPane.add(Box.createVerticalGlue());
		//add(advancedPanel, BorderLayout.SOUTH);
		applyComponentOrientation(orientation);

		// Refresh preview and possibly displayed font if user changed
		// relevant stuff in the (parent) text area option panel.
		addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if (syntaxList==null) return;
				int index = syntaxList.getSelectedIndex();
				if (index==-1) return;
				index = indexToStyle(index); // To get index into schemes.
				RTextAreaOptionPanel parent = (RTextAreaOptionPanel)getParentPanel();
				Style style = colorScheme.styles[index];
				Font f = style.font!=null ? style.font : parent.getTextAreaFont();
				boolean u = style.font!=null ? style.underline : parent.getUnderline();
				Color fg = style.foreground!=null ? style.foreground :
									parent.getTextAreaForeground();
				Color bg = style.background;
				if (bg==null) {
					Object obj = parent.getBackgroundObject();
					bg = (obj instanceof Color) ? (Color)obj : Color.WHITE;
				}
				stylePreviewLabel.setFont(f);
				stylePreviewLabel.setForeground(fg);
				stylePreviewLabel.setBackground(bg);
				stylePreviewLabel.setText(getPreviewText(u));
				if (style.font==null) {//!fontSelector.isToggledOn()) {
					fontSelector.setDisplayedFont(parent.getTextAreaFont(),
											parent.getUnderline());
				}
			}
		});

	}


	/**
	 * Listens for actions in this panel.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		// If the user clicked the "foreground" check box.
		if ("fgCheckBox".equals(command) && !isSettingStyle) {
			boolean selected = fgCheckBox.isSelected();
			foregroundButton.setEnabled(selected);
			stylePreviewLabel.setForeground(selected ?
					foregroundButton.getColor() : Color.BLACK);
			// Update our color scheme.
			int i = syntaxList.getSelectedIndex();
			colorScheme.styles[indexToStyle(i)].foreground =
						selected ? foregroundButton.getColor() : null;
			hasUnsavedChanges = true;
			firePropertyChange(SYNTAX_COLOR_PROPERTY, null, null);
		}

		// If the user clicked the "background" check box.
		else if ("bgCheckBox".equals(command) && !isSettingStyle) {
			boolean selected = bgCheckBox.isSelected();
			backgroundButton.setEnabled(selected);
			stylePreviewLabel.setBackground(selected ?
					backgroundButton.getColor() : Color.WHITE);
			// Update our color scheme.
			int i = syntaxList.getSelectedIndex();
			colorScheme.styles[indexToStyle(i)].background =
						selected ? backgroundButton.getColor() : null;
			hasUnsavedChanges = true;
			firePropertyChange(SYNTAX_COLOR_PROPERTY, null, null);
		}

		// If the user clicked the "restore defaults" button.
		else if ("RestoreDefaults".equals(command)) {

			SyntaxScheme currentScheme = getSyntaxScheme();
			SyntaxScheme defaultScheme = new SyntaxScheme(true);
			Color defaultBMBGColor = RSyntaxTextArea.getDefaultBracketMatchBGColor();
			Color defaultBMBorderColor = RSyntaxTextArea.getDefaultBracketMatchBorderColor();
			boolean defaultAA = File.separatorChar=='\\';

			if ( !currentScheme.equals(defaultScheme) ||
				!bracketMatchCheckBox.isSelected() ||
				!bmBGColorButton.getColor().equals(defaultBMBGColor) ||
				!bmBorderColorButton.getColor().equals(defaultBMBorderColor) ||
				isWhitespaceVisible() ||
				visibleEOLCheckBox.isSelected() ||
				showTabLinesCheckBox.isSelected() ||
				!Color.gray.equals(tabLineColorButton.getColor()) ||
				autoInsertClosingCurlyCheckBox.isSelected() ||
				remWhitespaceLinesCheckBox.isSelected() ||
				aaCheckBox.isSelected()!=defaultAA ||
				fractionalMetricsCheckBox.isSelected())
			{
				setSyntaxScheme(defaultScheme);
				setBracketMatchCheckboxSelected(true);
				setBracketMatchBGColor(defaultBMBGColor);
				bmBorderColorButton.setColor(defaultBMBorderColor);
				setWhitespaceVisible(false);
				visibleEOLCheckBox.setSelected(false);
				setTabLinesEnabled(false);
				tabLineColorButton.setColor(Color.gray);
				autoInsertClosingCurlyCheckBox.setSelected(false);
				remWhitespaceLinesCheckBox.setSelected(false);
				aaCheckBox.setSelected(defaultAA);
				fractionalMetricsCheckBox.setSelected(false);
				hasUnsavedChanges = true;
				firePropertyChange(DEFAULTS_RESTORED, null, null);
				// Force a repaint of the preview panel.
				valueChanged(null);
			}

		}

		// Toggle whether or not the bracket-matching buttons are enabled.
		else if ("BracketMatchCheckBox".equals(command)) {
			boolean selected = bracketMatchCheckBox.isSelected();
			bmBGColorButton.setEnabled(selected);
			bmBorderColorButton.setEnabled(selected);
			hasUnsavedChanges = true;
			firePropertyChange(BRACKET_MATCHING_PROPERTY, !selected, selected);
		}

		// Toggle whether whitespace is visible.
		else if ("VisibleWhitespace".equals(command)) {
			boolean visible = visibleWhitespaceCheckBox.isSelected();
			hasUnsavedChanges = true;
			firePropertyChange(VISIBLE_WHITESPACE_PROPERTY, !visible, visible);
		}

		else if ("VisibleEOL".equals(command)) {
			boolean visible = visibleEOLCheckBox.isSelected();
			hasUnsavedChanges = true;
			firePropertyChange(VISIBLE_EOL_PROPERTY, !visible, visible);
		}

		else if ("ShowIndentGuide".equals(command)) {
			boolean show = showTabLinesCheckBox.isSelected();
			tabLineColorButton.setEnabled(show);
			hasUnsavedChanges = true;
			firePropertyChange(SHOW_TAB_LINES_PROPERTY, !show, show);
		}

		// Toggle auto-indent.
		else if ("AutoIndent".equals(command)) {
		}

		// Toggle remove whitespace-only lines.
		else if ("RemWhitespaceLines".equals(command)) {
			boolean visible = remWhitespaceLinesCheckBox.isSelected();
			hasUnsavedChanges = true;
			firePropertyChange(VISIBLE_EOL_PROPERTY, !visible, visible);
		}

		else if ("AutoCloseCurlys".equals(command)) {
			boolean visible = autoInsertClosingCurlyCheckBox.isSelected();
			hasUnsavedChanges = true;
			firePropertyChange(VISIBLE_EOL_PROPERTY, !visible, visible);
		}

		// The checkbox to toggle anti-aliasing in text editors.
		else if ("aaCB".equals(command)) {
			boolean selected = aaCheckBox.isSelected();
			hasUnsavedChanges = true;
			firePropertyChange(SMOOTH_TEXT_PROPERTY, !selected, selected);
		}

		// Toggle the fractional font metrics property.
		else if ("FracFM".equals(command)) {
			boolean frac = fractionalMetricsCheckBox.isSelected();
			hasUnsavedChanges = true;
			firePropertyChange(FRACTIONAL_METRICS_PROPERTY, !frac, frac);
		}

	}


	/**
	 * Applies the settings entered into this dialog on the specified
	 * application.
	 *
	 * @param owner The application.
	 */
	protected void doApplyImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		rtext.setSyntaxScheme(getSyntaxScheme()); // Doesn't update if it doesn't have to.
		boolean bmEnabled = isBracketMatchCheckboxSelected();
		mainView.setBracketMatchingEnabled(bmEnabled);	// Doesn't update if it doesn't have to.
		mainView.setMatchedBracketBGColor(getBracketMatchBGColor()); // Doesn't update if it doesn't have to.
		mainView.setMatchedBracketBorderColor(bmBorderColorButton.getColor()); // Doesn't update if it doesn't have to.
		mainView.setRememberWhitespaceLines(!remWhitespaceLinesCheckBox.isSelected()); // Doesn't update if it doesn't have to.
		mainView.setAutoInsertClosingCurlys(autoInsertClosingCurlyCheckBox.isSelected()); // Doesn't update if it doesn't have to.
		mainView.setWhitespaceVisible(isWhitespaceVisible()); // (RSyntaxTextArea) doesn't update if not necessary.
		mainView.setShowEOLMarkers(visibleEOLCheckBox.isSelected());
		mainView.setShowTabLines(showTabLinesCheckBox.isSelected());
		mainView.setTabLinesColor(tabLineColorButton.getColor());
		mainView.setAntiAliasEnabled(aaCheckBox.isSelected());
		mainView.setFractionalFontMetricsEnabled(fractionalMetricsCheckBox.isSelected()); // Doesn't update if not necessary.
	}


	/**
	 * Checks whether or not all input the user specified on this panel is
	 * valid.
	 *
	 * @return This method always returns <code>null</code> as the user cannot
	 *         mess up input in this panel.
	 */
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * Returns the color the user chose for the background of a matched
	 * bracket.
	 *
	 * @return The color the user chose.
	 * @see #setBracketMatchBGColor
	 */
	public Color getBracketMatchBGColor() {
		return bmBGColorButton.getColor();
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
	 * Returns the <code>JComponent</code> at the "top" of this Options
	 * panel.  This is the component that will receive focus if the user
	 * switches to this Options panel in the Options dialog.  As an added
	 * bonus, if this component is a <code>JTextComponent</code>, its
	 * text is selected for easy changing.
	 */
	public JComponent getTopJComponent() {
		return syntaxList;
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
	private int indexToStyle(int index) {
		return index + 1;
	}


	/**
	 * Returns whether or not the bracketMatch checkbox is selected
	 *
	 * @return Whether or not the checkbox is selected.
	 * @see #setBracketMatchCheckboxSelected
	 */
	public boolean isBracketMatchCheckboxSelected() {
		return bracketMatchCheckBox.isSelected();
	}


	/**
	 * Returns whether the user decided whitespace should be visible.
	 *
	 * @return Whether or not the user wants whitespace to be visible in the
	 *         text area(s).
	 * @see #setWhitespaceVisible
	 */
	public boolean isWhitespaceVisible() {
		return visibleWhitespaceCheckBox.isSelected();
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

		// FontSelectors fire properties when users interactively update
		// the font or its properties.
		if (source==fontSelector && stylePreviewLabel!=null &&
				colorScheme!=null) {

			int i = syntaxList.getSelectedIndex();
			Style style = colorScheme.styles[indexToStyle(i)];
			style.fontMetrics = null;

			Font f = null;
			boolean underline = false;
			if (fontSelector.isToggledOn()) {
				f = fontSelector.getDisplayedFont();
				underline = fontSelector.getUnderline();
				style.font = f;
				style.underline = underline;
			}
			else {
				RTextAreaOptionPanel parent = (RTextAreaOptionPanel)getParentPanel();
				f = parent.getTextAreaFont();
				//underline = parent.getUnderline();
				style.font = null;
				style.underline = false;
			}

			stylePreviewLabel.setFont(f);
			hasUnsavedChanges = true;
			stylePreviewLabel.setText(getPreviewText(style.underline));
			firePropertyChange(SYNTAX_FONT_PROPERTY, e.getOldValue(),
							e.getNewValue());

		}

		else if (source==foregroundButton && stylePreviewLabel!=null &&
				colorScheme!=null) {
			Color fg = null;
			if (foregroundButton.isEnabled()) {
				fg = foregroundButton.getColor();
			}
			else {
				RTextAreaOptionPanel parent = (RTextAreaOptionPanel)getParentPanel();
				fg = parent.getTextAreaForeground();
			}
			stylePreviewLabel.setForeground(fg);
			// Method valueChanged() will cause this method to get fired,
			// so we must make sure we're not in it.
			if (!isSettingStyle) {
				hasUnsavedChanges = true;
				// Update our color scheme.
				int i = syntaxList.getSelectedIndex();
				colorScheme.styles[indexToStyle(i)].foreground = fg;
				firePropertyChange(SYNTAX_COLOR_PROPERTY, e.getOldValue(),
												e.getNewValue());
			}
		}

		else if (source==backgroundButton && stylePreviewLabel!=null &&
				colorScheme!=null) {
			Color bg = null;
			if (backgroundButton.isEnabled()) {
				bg = backgroundButton.getColor();
			}
			else {
				RTextAreaOptionPanel parent = (RTextAreaOptionPanel)getParentPanel();
				Object bgObj = parent.getBackgroundObject();
				bg = bgObj instanceof Color ? (Color)bgObj : Color.WHITE;
			}
			stylePreviewLabel.setBackground(bg);
			// Method valueChanged() will cause this method to get fired,
			// so we must make sure we're not in it.
			if (!isSettingStyle) {
				hasUnsavedChanges = true;
				// Update our color scheme.
				int i = syntaxList.getSelectedIndex();
				colorScheme.styles[indexToStyle(i)].background = bg;
				firePropertyChange(SYNTAX_COLOR_PROPERTY, e.getOldValue(),
												e.getNewValue());
			}
		}

		else if (source==bmBGColorButton) {
			hasUnsavedChanges = true;
			firePropertyChange(BRACKET_MATCHING_BG_PROPERTY,
							e.getOldValue(), e.getNewValue());
		}

		else if (source==bmBorderColorButton) {
			hasUnsavedChanges = true;
			firePropertyChange(BM_BORDER_PROPERTY,
							e.getOldValue(), e.getNewValue());
		}

		else if (source==tabLineColorButton) {
			hasUnsavedChanges = true;
			firePropertyChange(TAB_LINES_COLOR_PROPERTY,
					e.getOldValue(), e.getNewValue());
		}

		else {
			hasUnsavedChanges = true;
			firePropertyChange(UNKNOWN_PROPERTY,
							e.getOldValue(), e.getNewValue());
		}
		
	}


	/**
	 * Sets the color to use for the background of a matched bracket.
	 *
	 * @param color The color to use.
	 * @see #getBracketMatchBGColor
	 */
	public void setBracketMatchBGColor(Color color) {
		bmBGColorButton.setColor(color);
	}


	/**
	 * Sets whether or not the bracket match color checkbox is selected.
	 *
	 * @param selected Whether or not the checkbox is selected.
	 * @see #isBracketMatchCheckboxSelected
	 */
	public void setBracketMatchCheckboxSelected(boolean selected) {
		bracketMatchCheckBox.setSelected(selected);
		bmBGColorButton.setEnabled(selected);
		bmBorderColorButton.setEnabled(selected);
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
		// We must do manually call valueChanged() below as, if the selected
		// index was already 0, the above call won't trigger the valueChanged
		// callback.  If it isn't triggered, then the syntaxPreviewPanel and
		// color buttons won't be updated...
		valueChanged(null);
	}


	public void setTabLinesEnabled(boolean enabled) {
		showTabLinesCheckBox.setSelected(enabled);
		tabLineColorButton.setEnabled(enabled);
	}


	/**
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are not handled.
	 *
	 * @param owner The parent application.
	 * @see #setValues(Frame)
	 */
	protected void setValuesImpl(Frame owner) {
		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		setSyntaxScheme(rtext.getSyntaxScheme());
		boolean bmEnabled = mainView.isBracketMatchingEnabled();
		setBracketMatchCheckboxSelected(bmEnabled);
		setBracketMatchBGColor(mainView.getMatchedBracketBGColor());
		bmBorderColorButton.setColor(mainView.getMatchedBracketBorderColor());
		remWhitespaceLinesCheckBox.setSelected(!mainView.getRememberWhitespaceLines());
		autoInsertClosingCurlyCheckBox.setSelected(mainView.getAutoInsertClosingCurlys());
		setWhitespaceVisible(mainView.isWhitespaceVisible());
		visibleEOLCheckBox.setSelected(mainView.getShowEOLMarkers());
		setTabLinesEnabled(mainView.getShowTabLines());
		tabLineColorButton.setColor(mainView.getTabLinesColor());
		aaCheckBox.setSelected(mainView.isAntiAliasEnabled());
		fractionalMetricsCheckBox.setSelected(mainView.isFractionalFontMetricsEnabled());
	}


	/**
	 * Sets whether the "Visible whitespace" checkbox is selected.
	 *
	 * @param visible Whether the "visible whitespace" checkbox should be
	 *        selected.
	 * @see #isWhitespaceVisible
	 */
	public void setWhitespaceVisible(boolean visible) {
		visibleWhitespaceCheckBox.setSelected(visible);
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

		RTextAreaOptionPanel parent = (RTextAreaOptionPanel)getParentPanel();
		Font font = null;
		boolean underline = false;
		Style style = colorScheme.styles[index];
		if (style.font!=null) {
			font = style.font;
			underline = style.underline;
		}
		else {
			font = parent.getTextAreaFont();
			underline = parent.getUnderline();
		}

		// Update our color style.
		fontSelector.setToggledOn(style.font!=null);
		fontSelector.setDisplayedFont(font, underline);

		// Refresh text preview in case size only was changed.
		stylePreviewLabel.setFont(font);
		// Refresh text preview in case underline was toggled.
		stylePreviewLabel.setText(getPreviewText(style.underline));

		boolean notNull = style.foreground!=null;
		fgCheckBox.setSelected(notNull);
		foregroundButton.setEnabled(notNull);
		foregroundButton.setColor(notNull ? style.foreground : 
									parent.getTextAreaForeground());
		notNull = style.background!=null;
		bgCheckBox.setSelected(notNull);
		backgroundButton.setEnabled(notNull);
		if (style.background!=null) {
			backgroundButton.setColor(style.background);
		}
		else {
			Object parentBG = parent.getBackgroundObject();
			if (parentBG instanceof Color) {
				backgroundButton.setColor((Color)parentBG);
			}
			else {
				backgroundButton.setColor(Color.WHITE);
			}
		}

		// Do these manually since inSettingStyle prevents them from
		// happening via a propertyChange.
		stylePreviewLabel.setForeground(foregroundButton.getColor());
		stylePreviewLabel.setBackground(backgroundButton.getColor());
		isSettingStyle = false;

	}


}