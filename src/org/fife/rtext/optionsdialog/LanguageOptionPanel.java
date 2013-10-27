/*
 * 05/25/2004
 *
 * LanguageOptionPanel.java - Option panel letting the user choose the
 * language they are most comfortable with.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.optionsdialog;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import org.fife.io.UnicodeReader;
import org.fife.rtext.EmptyIcon;
import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RScrollPane;
import org.fife.ui.RListSelectionModel;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;
import org.fife.ui.app.GUIApplication;


/**
 * Option panel letting the user choose the language he or she is most
 * comfortable with.
 *
 * @author Robert Futrell
 * @version 0.5
 */
class LanguageOptionPanel extends OptionsDialogPanel
								implements ListSelectionListener {

	private DefaultListModel listModel;
	private JList languageList;	// Contains all available languages.
	private Map<String, String> languageMap;
	private GUIApplication app;

	private static final String LANGUAGE_PROPERTY	= "language";

	private static final String ROOT_ELEMENT		= "RText-languages";
	private static final String LANGUAGE			= "language";
	private static final String NAME				= "name";
	private static final String ID					= "id";

	private static final EmptyIcon EMPTY_ICON		= new EmptyIcon(16, 11);

	private static final String FILE_NAME			= "localizations.xml";


	/**
	 * Constructor.
	 *
	 * @param app The owner of the options dialog in which this panel appears.
	 * @param msg The resource bundle to use.
	 */
	public LanguageOptionPanel(GUIApplication app, ResourceBundle msg) {

		super(msg.getString("OptLaName"));
		this.app = app;

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		JPanel languagePanel = new JPanel();
		languagePanel.setBorder(BorderFactory.createCompoundBorder(
							new OptionPanelBorder(
									msg.getString("OptLaLabel")),
									UIUtil.getEmpty5Border()));
		languagePanel.setLayout(new BorderLayout());
		JPanel temp = new JPanel(new BorderLayout());
		temp.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		SelectableLabel label = new SelectableLabel(msg.getString("OptLaDesc"));
		temp.add(label, BorderLayout.LINE_START);
		languagePanel.add(temp, BorderLayout.NORTH);

		listModel = new DefaultListModel();
		languageList = new JList(listModel);
		languageList.setCellRenderer(LanguageListCellRenderer.create());
		languageMap = new HashMap<String, String>(1);
		try {
			File file = new File(app.getInstallLocation(), FILE_NAME);
			getLocalizations(file);
		} catch (Exception e) {
			app.displayException(e);
		}

		languageList.setSelectionModel(new RListSelectionModel());
		languageList.addListSelectionListener(this);
		RScrollPane scrollPane = new RScrollPane(languageList);
		languagePanel.add(scrollPane);

		add(languagePanel);
		applyComponentOrientation(orientation);

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doApplyImpl(Frame owner) {
		RText rtext = (RText)owner;
		rtext.setLanguage(getSelectedLanguage());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		// They can't input invalid stuff on this options panel.
		return null;
	}


	/**
	 * Returns the icon to use for the specified localization.
	 *
	 * @param id The localization (such as <code>en</code> or
	 *        <code>zh_CN</code>).
	 * @return The icon.
	 */
	private Icon getIconFor(String id) {
		Icon icon = null;
		URL url = getClass().getClassLoader().getResource(
						"org/fife/rtext/graphics/flags/" + id + ".png");
		if (url!=null) {
			try {
				icon = new ImageIcon(ImageIO.read(url));
			} catch (IOException ioe) {
				app.displayException(ioe);
				icon = EMPTY_ICON;
			}
		}
		else {
			icon = EMPTY_ICON;
		}
		return icon;
	}


	/**
	 * Loads the languages to display as selectable.
	 *
	 * @param xmlFile The XML file from which to load.
	 * @throws IOException If an error occurs while parsing the file.
	 */
	private void getLocalizations(File xmlFile) throws IOException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		Document doc = null;
		try {
			db = dbf.newDocumentBuilder();
			//InputSource is = new InputSource(new FileReader(file));
			InputSource is = new InputSource(new UnicodeReader(
							new BufferedInputStream(
							new FileInputStream(xmlFile)), "UTF-8"));
			is.setEncoding("UTF-8");
			doc = db.parse(is);//db.parse(file);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("XML error:  Error parsing file");
		}

		// Traverse the XML tree.
		initializeFromXMLFile(doc);

	}


	/**
	 * Returns the selected language, as a <code>Locale</code> string value.
	 *
	 * @return The selected language; i.e., <code>en</code> or
	 *         <code>es</code>.
	 */
	public final String getSelectedLanguage() {
		IconTextInfo iti = (IconTextInfo)languageList.getSelectedValue();
		String language = iti.getText();
		String code = languageMap.get(language);
		if (code==null) {
			app.displayException(
				new InternalError("Couldn't find language code for " +
					"language: " + language));
			code = "en";
		}
		return code;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public JComponent getTopJComponent() {
		return languageList;
	}


	/**
	 * Used in parsing an XML document containing a macro.  This method
	 * initializes this macro with the data contained in the passed-in node.
	 *
	 * @param node The root node of the parsed XML document.
	 * @throws IOException If an error occurs while parsing the XML.
	 */
	private void initializeFromXMLFile(Node node) throws IOException {

		if (node==null)
			throw new IOException("XML error:  node==null!");

		int type = node.getNodeType();
		switch (type) {

			// The document node is ???
			case Node.DOCUMENT_NODE:
				initializeFromXMLFile(
							((Document)node).getDocumentElement());
				break;

			// Handle element nodes.
			case Node.ELEMENT_NODE:

				String nodeName = node.getNodeName();

				// Might be the "topmost" node.
				if (nodeName.equals(ROOT_ELEMENT)) {
					NodeList childNodes = node.getChildNodes();
					// We should at least have English.
					if (childNodes==null || childNodes.getLength()==0) {
						throw new IOException("XML error:  There must " +
							"be at least 1 language declared!");
					}
					int childCount = childNodes.getLength();
					for (int i=0; i<childCount; i++)
						initializeFromXMLFile(childNodes.item(i));
				}

				// Might be a language declaration.
				else if (nodeName.equals(LANGUAGE)) {
					// Shouldn't have any children.
					NodeList childNodes = node.getChildNodes();
					if (childNodes!=null && childNodes.getLength()>0) {
						throw new IOException("XML error:  language " +
							"tags shouldn't have children!");
					}
					NamedNodeMap attributes = node.getAttributes();
					if (attributes==null || attributes.getLength()!=2) {
						throw new IOException("XML error:  language " +
							"tags should have two attributes!");
					}
					String name = null;
					String id = null;
					for (int i=0; i<2; i++) {
						Node node2 = attributes.item(i);
						nodeName = node2.getNodeName();
						if (nodeName.equals(NAME))
							name = node2.getNodeValue();
						else if (nodeName.equals(ID))
							id = node2.getNodeValue();
						else
							throw new IOException("XML error: unknown " +
								"attribute: '" + nodeName + "'");
					}
					if (name==null || id==null) {
						throw new IOException("XML error: language " +
							"must have attributes 'name' and 'id'.");
					}
					Icon icon = getIconFor(id);
					IconTextInfo iti = new IconTextInfo(name, icon);
					listModel.addElement(iti);
					languageMap.put(name, id);
				}

				// Anything else is an error.
				else {
					throw new IOException("XML error:  Unknown element " +
						"node: " + nodeName);
				}

				break;

			// Whitespace nodes.
			case Node.TEXT_NODE:
				break;

			// An error occurred?
			default:
				throw new IOException("XML error:  Unknown node type: " +
					type);

		} // End of switch (type).

	}


	/**
	 * Sets the language selected in this Options panel.
	 *
	 * @param language The language to be selected, as a Locale string
	 *        constant (e.g., <code>en</code> or <code>es</code>).
	 */
	private void setSelectedLanguage(String language) {

		int count = listModel.size();
		for (int i=0; i<count; i++) {
			Object obj = listModel.get(i);
			String langName = ((IconTextInfo)obj).getText();
			String langValue = languageMap.get(langName);
			if (language.startsWith(langValue)) {
				languageList.setSelectedIndex(i);
				return;
			}
		}

		// If the passed-in language wasn't "recognized," default to
		// English.
		for (int i=0; i<count; i++) {
			Object obj = listModel.get(i);
			String langName = ((IconTextInfo)obj).getText();
			String langValue = languageMap.get(langName);
			if (langValue.equals("en")) {
				languageList.setSelectedIndex(i);
				return;
			}
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setValuesImpl(Frame owner) {
		setSelectedLanguage(((RText)owner).getLanguage());
	}


	/**
	 * Called when the user changes the language in the language list.
	 * Do not override.
	 */
	public void valueChanged(ListSelectionEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(LANGUAGE_PROPERTY,
						-1, languageList.getSelectedIndex());
	}


	/**
	 * Wrapper class for an icon and text; used in the language list to
	 * wrap the two into a single list element.
	 */
	static class IconTextInfo {

		private Icon icon;
		private String text;

		public IconTextInfo(String text, Icon icon) {
			this.text = text;
			this.icon = icon;
		}

		public Icon getIcon() {
			return icon;
		}

		public String getText() {
			return text;
		}

	}


}