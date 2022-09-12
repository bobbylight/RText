/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.FontSelector;
import org.fife.ui.UIUtil;
import org.fife.ui.rtextarea.RTextArea;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Options panel for the editor's font and tab/space-related options.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FontAndTabsOptionPanel extends AbstractTextAreaOptionPanel
		implements DocumentListener, PropertyChangeListener {

	/**
	 * ID used to identify this option panel.
	 */
	public static final String OPTION_PANEL_ID = "FontAndTabsOptionPanel";

	private FontSelector fontSelector;

	private JLabel tabSizeLabel;
	private JTextField tabSizeField;
	private int tabSize;
	private JCheckBox emulateTabsCheckBox;

	private JCheckBox visibleWhitespaceCheckBox;
	private JCheckBox visibleEOLCheckBox;
	private JCheckBox showTabLinesCheckBox;


	/**
	 * Constructor.
	 */
	public FontAndTabsOptionPanel() {

		setId(OPTION_PANEL_ID);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setName(MSG.getString("Title.Font"));

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		// We'll add everything to this panel, then add this panel so that
		// stuff stays at the "top."
		Box topPanel = Box.createVerticalBox();

		topPanel.add(createFontPanel());
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		topPanel.add(createTabPanel());
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		topPanel.add(createOtherPanel());
		topPanel.add(Box.createVerticalStrut(SECTION_VERTICAL_SPACING));

		// Create a panel containing the preview and "Restore Defaults"
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(new PreviewPanel(MSG, 9, 40));
		bottomPanel.add(createRestoreDefaultsPanel(), BorderLayout.SOUTH);
		topPanel.add(bottomPanel);

		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(orientation);

	}


	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if ("EmulateTabsCheckBox".equals(command)) {
			setDirty(true);
		}

		else if ("VisibleWhitespace".equals(command)) {
			setDirty(true);
		}

		else if ("VisibleEOL".equals(command)) {
			setDirty(true);
		}

		else if ("ShowIndentGuide".equals(command)) {
			setDirty(true);
		}

		else {
			super.actionPerformed(e);
		}
	}


	/**
	 * This doesn't get called but is here because this class implements
	 * <code>DocumentListener</code>.
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {
	}


	private JCheckBox createCheckBox(String key) {
		JCheckBox cb = new JCheckBox(MSG.getString(key));
		cb.setActionCommand(key);
		cb.addActionListener(this);
		return cb;
	}


	private JPanel createFontPanel() {

		JPanel fontPanel = new JPanel(new BorderLayout());
		fontPanel.setBorder(new OptionPanelBorder(MSG.getString("Font")));

		fontSelector = new FontSelector();
		fontSelector.setColorSelectable(true);
		fontSelector.addPropertyChangeListener(FontSelector.FONT_PROPERTY, this);
		fontSelector.addPropertyChangeListener(FontSelector.FONT_COLOR_PROPERTY, this);
		fontPanel.add(fontSelector);

		return fontPanel;
	}


	private JComponent createOtherPanel() {

		Box otherPanel = Box.createVerticalBox();
		otherPanel.setBorder(new OptionPanelBorder(getString("OptOtherTitle")));

		visibleWhitespaceCheckBox = createCheckBox("VisibleWhitespace");
		addLeftAligned(otherPanel, visibleWhitespaceCheckBox, COMPONENT_VERTICAL_SPACING);

		visibleEOLCheckBox = createCheckBox("VisibleEOL");
		addLeftAligned(otherPanel, visibleEOLCheckBox, COMPONENT_VERTICAL_SPACING);

		showTabLinesCheckBox = new JCheckBox(MSG.getString("ShowIndentGuide"));
		showTabLinesCheckBox.setActionCommand("ShowIndentGuide");
		showTabLinesCheckBox.addActionListener(this);
		Box box = createHorizontalBox();
		box.add(showTabLinesCheckBox);
		box.add(Box.createHorizontalGlue());
		addLeftAligned(otherPanel, box);

		return otherPanel;
	}


	private JComponent createTabPanel() {

		Box tabPanel = Box.createVerticalBox();
		tabPanel.setBorder(new OptionPanelBorder(MSG.getString("Tabs")));

		Box inputPanel = createHorizontalBox();
		tabSizeLabel = new JLabel(MSG.getString("TabSize"));
		tabSizeField = new JTextField();
		tabSizeField.getDocument().addDocumentListener(this);
		Dimension size = new Dimension(40,tabSizeField.getPreferredSize().height);
		tabSizeField.setMaximumSize(size);
		tabSizeField.setPreferredSize(size);
		inputPanel.add(tabSizeLabel);
		inputPanel.add(Box.createHorizontalStrut(5));
		inputPanel.add(tabSizeField);
		inputPanel.add(Box.createHorizontalGlue());
		addLeftAligned(tabPanel, inputPanel, COMPONENT_VERTICAL_SPACING);

		emulateTabsCheckBox = new JCheckBox(MSG.getString("EmulateTabs"));
		emulateTabsCheckBox.setActionCommand("EmulateTabsCheckBox");
		emulateTabsCheckBox.addActionListener(this);
		addLeftAligned(tabPanel, emulateTabsCheckBox);

		tabPanel.add(Box.createVerticalGlue());
		return tabPanel;
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

		mainView.setTextAreaForeground(fontSelector.getFontColor());
		mainView.setTextAreaFont(fontSelector.getDisplayedFont(), fontSelector.getUnderline());
		rtext.setRowColumnIndicatorVisible(!mainView.getLineWrap());
		mainView.setTabSize(getTabSize());				// Doesn't update if unnecessary.
		mainView.setTabsEmulated(emulateTabsCheckBox.isSelected());		// Doesn't update if unnecessary.
		mainView.setWhitespaceVisible(visibleWhitespaceCheckBox.isSelected()); // (RSyntaxTextArea) doesn't update if unnecessary.
		mainView.setShowEOLMarkers(visibleEOLCheckBox.isSelected());
		mainView.setShowTabLines(showTabLinesCheckBox.isSelected());

	}


	/**
	 * Called when a text field in this panel gets updated.
	 */
	private void doDocumentUpdated(DocumentEvent e) {
		setDirty(true);
	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {

		// Ensure the tab size specified is valid.
		int temp;
		try {
			temp = Integer.parseInt(tabSizeField.getText());
			if (temp<0) throw new NumberFormatException();
		} catch (NumberFormatException nfe) {
			OptionsPanelCheckResult res = new OptionsPanelCheckResult(this);
			res.errorMessage = "Invalid number format for tab size;\nPlease input a tab size greater than zero.";
			res.component = tabSizeField;
			// Hack; without this, tabSize is still valid, so if they hit Cancel
			// then brought the Options dialog back up, the invalid text would
			// still be there.
			tabSize = -1;
			return res;
		}
		tabSize = temp;	// Store the value the user will get.

		// If that went okay then the entire panel is okay.
		return null;

	}


	/**
	 * Returns the tab size selected by the user.
	 *
	 * @return The tab size selected by the user.
	 */
	public int getTabSize() {
		return tabSize;
	}


	@Override
	public JComponent getTopJComponent() {
		return tabSizeField;
	}


	@Override
	protected void handleRestoreDefaults() {

		EditorOptionsPreviewContext editorContext = EditorOptionsPreviewContext.get();

		// This panel's defaults are based on the current theme.
		RText app = (RText)getOptionsDialog().getParent();
		Theme rstaTheme = editorContext.getEditorTheme(app);

		// Note we're a little cheap here and go with RSTA's default font rather
		// than look for fonts in themes.  This is OK since we don't actually
		// set fonts in any of the default themes.
		Font defaultFont = RTextArea.getDefaultFont();
		Color defaultForeground = rstaTheme.scheme.getStyle(TokenTypes.IDENTIFIER).foreground;
		int defaultTabSize = RTextArea.getDefaultTabSize();

		if (!fontSelector.getDisplayedFont().equals(defaultFont) ||
			!fontSelector.getFontColor().equals(defaultForeground) ||
			fontSelector.getUnderline() ||
			getTabSize()!=defaultTabSize ||
			emulateTabsCheckBox.isSelected() ||
			visibleWhitespaceCheckBox.isSelected() ||
			visibleEOLCheckBox.isSelected() ||
			showTabLinesCheckBox.isSelected()) {
			fontSelector.setDisplayedFont(defaultFont, false);
			fontSelector.setFontColor(defaultForeground);
			setTabSize(defaultTabSize);
			emulateTabsCheckBox.setSelected(false);
			visibleWhitespaceCheckBox.setSelected(false);
			visibleEOLCheckBox.setSelected(false);
			showTabLinesCheckBox.setSelected(false);
			setDirty(true);
		}
	}


	/**
	 * Called when a text field in this panel gets updated.
	 */
	@Override
	public void insertUpdate(DocumentEvent e) {
		doDocumentUpdated(e);
	}


	/**
	 * Called when a property changes in an object we're listening to.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		// We need to forward this on to the options dialog, whatever
		// it is, so that the "Apply" button gets updated.
		setDirty(true);
	}


	/**
	 * Called when a text field in this panel gets updated.
	 */
	@Override
	public void removeUpdate(DocumentEvent e) {
		doDocumentUpdated(e);
	}


	/**
	 * Sets the tab size currently being displayed.
	 *
	 * @param tabSize The tab size to display.
	 */
	private void setTabSize(int tabSize) {
		if (this.tabSize!=tabSize && tabSize>0) {
			this.tabSize = tabSize;
		}
		// We do this not in the if-condition above because the user could
		// have typed in a bad value, then hit "Cancel" previously, and we
		// need to clear this out.
		tabSizeField.setText(Integer.toString(tabSize));
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

		// Iniitialize the shared preview context if this is the first pass.
		// Otherwise we'll get NPE's
		EditorOptionsPreviewContext previewContext = EditorOptionsPreviewContext.get();
		if (previewContext.getSyntaxScheme() == null) {
			previewContext.initialize(rtext);
		}

		AbstractMainView mainView = rtext.getMainView();
		fontSelector.setFontColor(mainView.getTextAreaForeground());
		fontSelector.setDisplayedFont(mainView.getTextAreaFont(), mainView.getTextAreaUnderline());
		setTabSize(mainView.getTabSize());
		emulateTabsCheckBox.setSelected(mainView.areTabsEmulated());
		visibleWhitespaceCheckBox.setSelected(mainView.isWhitespaceVisible());
		visibleEOLCheckBox.setSelected(mainView.getShowEOLMarkers());
		showTabLinesCheckBox.setSelected(mainView.getShowTabLines());

		syncEditorOptionsPreviewContext();
	}


	@Override
	protected void syncEditorOptionsPreviewContext() {
		EditorOptionsPreviewContext context = EditorOptionsPreviewContext.get();
		context.getSyntaxScheme().changeBaseFont(context.getFont(), fontSelector.getDisplayedFont());
		context.setFont(fontSelector.getDisplayedFont());
		context.setFontColor(fontSelector.getFontColor());
		context.setTabSize(getTabSize());
		context.setEmulateTabs(emulateTabsCheckBox.isSelected());
		context.setShowWhitespace(visibleWhitespaceCheckBox.isSelected());
		context.setShowEolMarkers(visibleEOLCheckBox.isSelected());
		context.setShowIndentGuides(showTabLinesCheckBox.isSelected());

		context.possiblyFireChangeEventAndReset();
	}


}
