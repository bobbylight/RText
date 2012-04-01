package org.fife.rtext.plugins.sourcebrowser.xml;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.fife.io.DocumentReader;
import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Generates source trees for XML files.  These trees update in real time with
 * edits in the text area.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class XmlSourceTreeGenerator extends DefaultHandler {

	private JTree tree;
	private XmlTreeNode root;
	private XmlTreeNode curElem;

	private RTextEditorPane textArea;
	private Document doc;
	private Locator locator;
	private Segment s;
	private XmlEditorListener listener;

	private Icon elemIcon;


	public XmlSourceTreeGenerator() {

		URL url = getClass().getResource("tag.png");
		if (url!=null) { // Always true
			elemIcon = new ImageIcon(url);
		}

		s = new Segment();
		listener = new XmlEditorListener();

	}


	public JTree constructSourceBrowserTree(RText rtext) {

		if (textArea!=null) {
			textArea.removeCaretListener(listener);
		}

		textArea = rtext.getMainView().getCurrentTextArea();
		textArea.addCaretListener(listener);
		doc = textArea.getDocument();
		curElem = root = new XmlTreeNode("Root");
		
		//long start = System.currentTimeMillis();
		try {
			XMLReader xr = createReader();
			if (xr==null) { // Couldn't create an XML reader.
				return null;
			}
			xr.setContentHandler(this);
			InputSource is = new InputSource(new DocumentReader(doc));
			//is.setEncoding("UTF-8");
			xr.parse(is);
		} catch (Exception e) {
			// Don't give an error; they likely just saved an incomplete XML
			// file
			// Fall through
		}
		//long time = System.currentTimeMillis() - start;
		//System.err.println("DEBUG: IconGroupLoader parsing: " + time + " ms");

		if (locator!=null) {
			try {
				root.offset = doc.createPosition(0);
				root.endOffset = doc.createPosition(doc.getLength());
			} catch (BadLocationException ble) {
				ble.printStackTrace();
			}
		}

		tree = new JTree(root);
		tree.setRootVisible(false);
		//UIUtil.expandAllNodes(tree);
		tree.setCellRenderer(new XmlTreeRenderer());
		tree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON1 &&
						e.getClickCount()==1) {
					TreePath path = tree.getLeadSelectionPath();
					if (path!=null) {
						XmlTreeNode node = (XmlTreeNode)path.getLastPathComponent();
						if (node.offset!=null) { // Should always be true
							int start = node.offset.getOffset();
							int len = node.name.length();
							textArea.setSelectionStart(start);
							textArea.setSelectionEnd(start+len);
							textArea.requestFocusInWindow();
						}
					}
				}
			}
		});

		return tree;

	}


	/**
	 * Creates the XML reader to use.  Note that in 1.4 JRE's, the reader
	 * class wasn't defined by default, but in 1.5+ it is.
	 *
	 * @return The XML reader to use.
	 */
	private XMLReader createReader() {
		XMLReader reader = null;
		try {
			reader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			// Happens in JRE 1.4.x; 1.5+ define the reader class properly
			try {
				reader = XMLReaderFactory.createXMLReader(
						"org.apache.crimson.parser.XMLReaderImpl");
			} catch (SAXException se) {
				//owner.displayException(se);
				se.printStackTrace();
			}
		}
		return reader;
	}


	/**
	 * Callback when an XML element ends.
	 */
	public void endElement(String uri, String localName, String qName) {

		if (locator!=null) {
			int line = locator.getLineNumber();
			if (line!=-1) {
				int offs = doc.getDefaultRootElement().
					getElement(line-1).getStartOffset();
				int col = locator.getColumnNumber();
				if (col!=-1) {
					offs += col - 1;
				}
				try {
					curElem.setEndOffset(doc.createPosition(offs));
				} catch (BadLocationException ble) {
					ble.printStackTrace();
				}
			}
		}

		curElem = (XmlTreeNode)curElem.getParent();

	}


	private int getTagStart(int end) {

		Element root = doc.getDefaultRootElement();
		int line = root.getElementIndex(end);
		Element elem = root.getElement(line);
		int start = elem.getStartOffset();
		int lastCharOffs = -1;

		try {
			while (line>=0) {
				doc.getText(start, end-start, s);
				for (int i=s.offset+s.count-1; i>=s.offset; i--) {
					char ch = s.array[i];
					if (ch=='<') {
						return lastCharOffs;
					}
					else if (Character.isLetterOrDigit(ch)) {
						//lastCharOffs = start + s.getIndex() - s.getBeginIndex();
						lastCharOffs = start + i - s.offset;
					}
				}
				if (--line>=0) {
					elem = root.getElement(line);
					start = elem.getStartOffset();
					end = elem.getEndOffset();
				}
			}
		} catch (BadLocationException ble) {
			ble.printStackTrace();
		}

		return -1;

	}


	public void setDocumentLocator(Locator l) {
		this.locator = l;
	}


	/**
	 * Callback when an XML element begins.
	 */
	public void startElement(String uri, String localName, String qName,
							Attributes attributes) {

		XmlTreeNode newElem = new XmlTreeNode(qName);
//		if (attributes.getLength()>0) {
//			String mainAttr = attributes.getLocalName(0) + "=" +
//								attributes.getValue(0);
//			for (int i=1; i<attributes.getLength(); i++) {
//				String name = attributes.getLocalName(i);
//				if ("id".equals(name) || "name".equals(name)) {
//					mainAttr = name + "=" + attributes.getValue(i);
//					break;
//				}
//			}
//			newElem.setMainAttr(mainAttr);
//		}
		if (locator!=null) {
			int line = locator.getLineNumber();
			if (line!=-1) {
				int offs = doc.getDefaultRootElement().
					getElement(line-1).getStartOffset();
				int col = locator.getColumnNumber();
				if (col!=-1) {
					offs += col - 1;
				}
				// "offs" is now the end of the tag.  Find the beginning of it.
				offs = getTagStart(offs);
				try {
					newElem.setStartOffset(doc.createPosition(offs));
				} catch (BadLocationException ble) {
					ble.printStackTrace();
				}
			}
		}

		curElem.add(newElem);
		curElem = newElem;

	}


	/**
	 * A node in the XML tree.
	 */
	private class XmlTreeNode extends DefaultMutableTreeNode {

		private String name;
		private String mainAttr;
		private Position offset;
		private Position endOffset;

		public XmlTreeNode(String name) {
			this.name = name;
		}

		public boolean containsOffset(int offs) {
			return offset!=null && endOffset!=null &&
					offs>=offset.getOffset() && offs<=endOffset.getOffset();
		}

		public boolean equals(Object o) {
			if (o instanceof XmlTreeNode) {
				XmlTreeNode node2 = (XmlTreeNode)o;
				if (name.equals(node2.name)) {
					// Only need to verify start offset, as elements can't overlap
					if (offset!=null /*&& endOffset!=null*/) {
						return offset.getOffset()==node2.offset.getOffset() /*&&
								endOffset.getOffset()==node2.endOffset.getOffset()*/;
					}
				}
			}
			return false;
		}

		public void selectInTree() {
			TreePath path = new TreePath(getPath());
			tree.setSelectionPath(path);
			tree.scrollPathToVisible(path);
		}

		public void setEndOffset(Position pos) {
			this.endOffset = pos;
		}

		/*
		public void setMainAttr(String attr) {
			this.mainAttr = attr;
		}
		*/

		public void setStartOffset(Position pos) {
			this.offset = pos;
		}

		public String toString() {
			String str = name;
			if (mainAttr!=null) {
				str = "<html>" + str + " <font color='#808080'>" + mainAttr;
			}
			return str;
		}

		public String toStringSelected() {
			String str = name;
			if (mainAttr!=null) {
				str += " " + mainAttr;
			}
			return str;
		}

	}


	/**
	 * Renders the XML tree.
	 */
	private class XmlTreeRenderer extends DefaultTreeCellRenderer {

	    public Component getTreeCellRendererComponent(JTree tree, Object value,
				  boolean sel,
				  boolean expanded,
				  boolean leaf, int row,
				  boolean focused) {
	    	super.getTreeCellRendererComponent(tree, value, sel, expanded,
	    										leaf, row, focused);
	    	if (sel) {
	    		setText(((XmlTreeNode)value).toStringSelected());
	    	}
	    	setIcon(elemIcon);
	    	return this;
	    }

	}


	/**
	 * Listens for events in the text area.
	 */
	private class XmlEditorListener implements CaretListener, ActionListener {

		private Timer timer;
		private int dot;

		public XmlEditorListener() {
			timer = new Timer(650, this);
			timer.setRepeats(false);
		}

		public void actionPerformed(ActionEvent e) {
			recursivelyCheck(root);
			//System.out.println("Here");
		}

		public void caretUpdate(CaretEvent e) {
			this.dot = e.getDot();
			timer.restart();
		}

		private boolean recursivelyCheck(XmlTreeNode node) {
			if (node.containsOffset(dot)) {
				for (int i=0; i<node.getChildCount(); i++) {
					XmlTreeNode child = (XmlTreeNode)node.getChildAt(i);
					if (recursivelyCheck(child)) {
						return true;
					}
				}
				// None of the children contain the offset, must this guy
				node.selectInTree();
				return true;
			}
			return false;
		}

	}


}