/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rsyntaxtextarea;

import org.fife.rtext.AbstractMainView;
import org.fife.rtext.RText;
import org.fife.ui.RColorButton;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Option panel for text area highlight-related options (mark all,
 * mark occurrences, etc.).
 *
 * @author Robert Futrell
 * @version 0.6
 */
public class HighlightsOptionPanel extends AbstractTextAreaOptionPanel
		implements ChangeListener, PropertyChangeListener {

	private RColorSwatchesButton markAllColorButton;
	private JCheckBox enableMOCheckBox;
	private RColorSwatchesButton moColorButton;
	private JCheckBox secLangCB;
	private JLabel[] secLangLabels;
	private RColorSwatchesButton[] secLangButtons;

	private static final int SEC_LANG_COUNT		= 3;

	private static final Color[] DEFAULT_SECONDARY_LANGUAGE_COLORS = {
		new Color(0xfff0cc),
		new Color(0xdafeda),
		new Color(0xffe0f0),
	};


	/**
	 * Constructor.
	 */
	public HighlightsOptionPanel() {

		ComponentOrientation o = ComponentOrientation.
									getOrientation(getLocale());

		setName(MSG.getString("Title.Highlights"));

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		// We'll add everything to this panel, then add this panel so that
		// stuff stays at the "top."
		Box topPanel = Box.createVerticalBox();

		topPanel.add(createOverridePanel());
		topPanel.add(Box.createVerticalStrut(5));

		topPanel.add(createHighlightsPanel());
		topPanel.add(Box.createVerticalStrut(5));

		topPanel.add(createSecondaryLanguagesPanel(o));
		topPanel.add(Box.createVerticalStrut(5));

		// Create a panel containing the preview and "Restore Defaults"
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(new PreviewPanel(MSG, 9, 40));
		bottomPanel.add(createRestoreDefaultsPanel(), BorderLayout.SOUTH);
		topPanel.add(bottomPanel);

		add(topPanel, BorderLayout.NORTH);
		applyComponentOrientation(o);

	}


	/**
	 * Listens for actions in this panel.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		Object source = e.getSource();

		if ("MarkOccurrences".equals(command)) {
			boolean selected = enableMOCheckBox.isSelected();
			moColorButton.setEnabled(selected);
			setDirty(true);
		}

		else if (secLangCB==source) {
			boolean selected = ((JCheckBox)source).isSelected();
			setHighlightSecondaryLanguages(selected);
			setDirty(true);
		}

		else {
			handleRestoreDefaults();
		}
	}


	/**
	 * Creates a panel containing the "mark occurrences"-related options.
	 *
	 * @return The panel.
	 */
	private Box createHighlightsPanel() {

		Box p = Box.createVerticalBox();
		p.setBorder(new OptionPanelBorder(MSG.getString("Section.Highlights")));


		markAllColorButton = new RColorSwatchesButton();
		markAllColorButton.addPropertyChangeListener(
			RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		JLabel markAllLabel = new JLabel(MSG.getString("MarkAllColor"));
		markAllLabel.setLabelFor(markAllColorButton);
		Box box = createHorizontalBox();
		box.add(markAllLabel);
		box.add(Box.createHorizontalStrut(5));
		box.add(markAllColorButton);
		box.add(Box.createHorizontalGlue());
		p.add(box);
		p.add(Box.createVerticalStrut(3));

		enableMOCheckBox = new JCheckBox(
			MSG.getString("EnableMarkOccurrences"));
		enableMOCheckBox.setActionCommand("MarkOccurrences");
		enableMOCheckBox.addActionListener(this);

		moColorButton = new RColorSwatchesButton();
		moColorButton.addPropertyChangeListener(
			RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);

		box = createHorizontalBox();
		box.add(enableMOCheckBox);
		box.add(Box.createHorizontalStrut(5));
		box.add(moColorButton);
		box.add(Box.createHorizontalGlue());
		p.add(box);

		return p;

	}


	/**
	 * Creates a panel containing the secondary language-related options.<p>
	 * These should really be elsewhere, but we're getting short on space in
	 * other text editor-related panels.
	 *
	 * @param o The component orientation.
	 * @return The panel.
	 */
	private Box createSecondaryLanguagesPanel(ComponentOrientation o) {

		Box p = Box.createVerticalBox();
		p.setBorder(new OptionPanelBorder(MSG.getString("SecondaryLanguages")));

		secLangCB =  new JCheckBox(MSG.getString("HighlightSecondaryLanguages"));
		secLangCB.addActionListener(this);
		addLeftAligned(p, secLangCB);

		secLangLabels = new JLabel[SEC_LANG_COUNT];
		secLangButtons = new RColorSwatchesButton[SEC_LANG_COUNT];
		for (int i=0; i<SEC_LANG_COUNT; i++) {
			secLangButtons[i] = new RColorSwatchesButton();
			secLangButtons[i].addPropertyChangeListener(
					RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
			secLangLabels[i] = new JLabel(MSG.getString(
					"HighlightSecondaryLanguages.Color" + (i+1)));
			secLangLabels[i].setLabelFor(secLangButtons[i]);
		}

		JPanel temp = new JPanel(new SpringLayout());
		if (o.isLeftToRight()) {
			temp.add(secLangLabels[0]);  temp.add(secLangButtons[0]);
			temp.add(Box.createVerticalStrut(20));
			temp.add(secLangLabels[1]);  temp.add(secLangButtons[1]);
			temp.add(Box.createVerticalStrut(20));
			temp.add(secLangLabels[2]);  temp.add(secLangButtons[2]);
		}
		else {
			temp.add(secLangButtons[0]); temp.add(secLangLabels[0]);
			temp.add(Box.createVerticalStrut(20));
			temp.add(secLangButtons[1]);  temp.add(secLangLabels[1]);
			temp.add(Box.createVerticalStrut(20));
			temp.add(secLangButtons[2]);  temp.add(secLangLabels[2]);
		}
		UIUtil.makeSpringCompactGrid(temp, 1,8, 0,0, 5,5);
		addLeftAligned(p, temp, 0, 20);

		return p;

	}


	@Override
	protected void doApplyImpl(Frame owner) {

		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		mainView.setOverrideEditorStyles(overrideCheckBox.isSelected());

		if (overrideCheckBox.isSelected()) {

			mainView.setMarkAllHighlightColor(getMarkAllHighlightColor());
			mainView.setMarkOccurrences(enableMOCheckBox.isSelected());
			mainView.setMarkOccurrencesColor(moColorButton.getColor());

			mainView.setHighlightSecondaryLanguages(secLangCB.isSelected());
			for (int i = 0; i < SEC_LANG_COUNT; i++) {
				mainView.setSecondaryLanguageColor(i, secLangButtons[i].getColor());
			}
		}
		else {

			Theme editorTheme = EditorOptionsPreviewContext.get().getEditorTheme(rtext);
			mainView.setMarkAllHighlightColor(editorTheme.markAllHighlightColor);
			mainView.setMarkOccurrences(true);
			mainView.setMarkOccurrencesColor(editorTheme.markOccurrencesColor);

			mainView.setHighlightSecondaryLanguages(secLangCB.isSelected());
			for (int i = 0; i < SEC_LANG_COUNT; i++) {
				mainView.setSecondaryLanguageColor(i, editorTheme.secondaryLanguages[i]);
			}
		}
	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * Returns the color displayed, as a <code>Color</code> and not a
	 * <code>ColorUIResource</code>, to prevent LookAndFeel changes from
	 * overriding them when they are installed.
	 *
	 * @param button The button.
	 * @return The color displayed by the button.
	 */
	private static Color getColor(RColorSwatchesButton button) {
		return new Color(button.getColor().getRGB());
	}


	/**
	 * Returns the color selected by the user for "mark all".
	 *
	 * @return The color.
	 */
	public Color getMarkAllHighlightColor() {
		return markAllColorButton.getColor();
	}


	@Override
	protected void handleRestoreDefaults() {

		// This panel's defaults are based on the current theme.
		RText app = (RText)getOptionsDialog().getParent();
		EditorOptionsPreviewContext editorContext = EditorOptionsPreviewContext.get();
		Theme rstaTheme = editorContext.getEditorTheme(app);

		Color defaultMarkAllColor = rstaTheme.markAllHighlightColor;
		Color defaultMarkOccurrencesColor = rstaTheme.markOccurrencesColor;
		Color[] defaultSecLangColor = new Color[SEC_LANG_COUNT];
		for (int i = 0; i < defaultSecLangColor.length; i++) {
			if (rstaTheme.secondaryLanguages != null && rstaTheme.secondaryLanguages.length > i) {
				defaultSecLangColor[i] = rstaTheme.secondaryLanguages[i];
			}
			if (defaultSecLangColor[i] == null) {
				defaultSecLangColor[i] = DEFAULT_SECONDARY_LANGUAGE_COLORS[i];
			}
		}

		if (overrideCheckBox.isSelected() ||
			!getMarkAllHighlightColor().equals(defaultMarkAllColor) ||
			!enableMOCheckBox.isSelected() ||
			!moColorButton.getColor().equals(defaultMarkOccurrencesColor) ||
			secLangCB.isSelected() ||
			!defaultSecLangColor[0].equals(secLangButtons[0].getColor()) ||
			!defaultSecLangColor[1].equals(secLangButtons[1].getColor()) ||
			!defaultSecLangColor[2].equals(secLangButtons[2].getColor())) {

			overrideCheckBox.setSelected(false);
			setMarkAllHighlightColor(defaultMarkAllColor);
			enableMOCheckBox.setSelected(true);
			moColorButton.setEnabled(true);
			moColorButton.setColor(defaultMarkOccurrencesColor);
			setHighlightSecondaryLanguages(false);
			for (int i=0; i<SEC_LANG_COUNT; i++) {
				secLangButtons[i].setColor(defaultSecLangColor[i]);
			}
			setDirty(true);
		}

	}


	/**
	 * Called when a property changes in an object we're listening to.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		// We need to forward this on to the options dialog, whatever
		// it is, so that the "Apply" button gets updated.
		if (RColorButton.COLOR_CHANGED_PROPERTY.equals(e.getPropertyName())) {
			setDirty(true);
		}
	}


	@Override
	protected void setComponentsEnabled(boolean enabled, Component... ignore) {

		super.setComponentsEnabled(enabled, ignore);

		// These components ignore the global change above since they have additional
		// conditions to check to determine whether they are enabled
		moColorButton.setEnabled(enabled && enableMOCheckBox.isSelected());
		boolean secondaryLanguagesConfigurable = enabled && secLangCB.isSelected();
		for (int i=0; i<SEC_LANG_COUNT; i++) {
			secLangLabels[i].setEnabled(secondaryLanguagesConfigurable);
			secLangButtons[i].setEnabled(secondaryLanguagesConfigurable);
		}
	}


	private void setHighlightSecondaryLanguages(boolean highlight) {
		secLangCB.setSelected(highlight);
		for (int i=0; i<SEC_LANG_COUNT; i++) {
			secLangLabels[i].setEnabled(highlight);
			secLangButtons[i].setEnabled(highlight);
		}
	}


	/**
	 * Sets the color selected for "mark all".
	 *
	 * @param color The color to have selected.
	 * @see #getMarkAllHighlightColor()
	 */
	private void setMarkAllHighlightColor(Color color) {
		if (color!=null)
			markAllColorButton.setColor(color);
	}


	@Override
	protected void setValuesImpl(Frame owner) {

		RText rtext = (RText)owner;
		AbstractMainView mainView = rtext.getMainView();
		setMarkAllHighlightColor(mainView.getMarkAllHighlightColor());
		enableMOCheckBox.setSelected(mainView.getMarkOccurrences());
		moColorButton.setEnabled(enableMOCheckBox.isSelected());
		moColorButton.setColor(mainView.getMarkOccurrencesColor());

		setHighlightSecondaryLanguages(mainView.getHighlightSecondaryLanguages());
		for (int i=0; i<SEC_LANG_COUNT; i++) {
			secLangButtons[i].setColor(mainView.getSecondaryLanguageColor(i));
		}

		// Do this after initializing all values above
		overrideCheckBox.setSelected(mainView.getOverrideEditorStyles());
		setComponentsEnabled(overrideCheckBox.isSelected());

		syncEditorOptionsPreviewContext();
	}


	/**
	 * Called when the user changes the caret blink rate spinner value.
	 *
	 * @param e The change event.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		setDirty(true);
	}


	@Override
	protected void syncEditorOptionsPreviewContext() {

		EditorOptionsPreviewContext context = EditorOptionsPreviewContext.get();
		context.setOverrideEditorTheme(overrideCheckBox.isSelected());

		// "Highlights" section
		context.setMarkAllHighlightColor(getMarkAllHighlightColor());
		context.setMarkOccurrences(enableMOCheckBox.isSelected());
		context.setMarkOccurrencesColor(moColorButton.getColor());

		// "Secondary Languages" section
		context.setHighlightSecondaryLanguages(secLangCB.isSelected());
		for (int i = 0; i < SEC_LANG_COUNT; i++) {
			context.setSecondaryLanguageBackground(i, secLangButtons[i].getColor());
		}
	}

}
