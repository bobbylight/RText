/*
 * 11/14/2003
 *
 * PrintAction.java - Action to print the current document in RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.ui.app.StandardAction;


/**
 * Action used by an <code>AbstractMainView</code> to print the current
 * document.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class PrintAction extends StandardAction {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public PrintAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "PrintAction");
		setIcon(icon);
	}


	public void actionPerformed(ActionEvent e) {

		RText owner = (RText)getApplication();
		RTextEditorPane textArea = owner.getMainView().getCurrentTextArea();

		DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
		attributeSet.add(new JobName(textArea.getFileFullPath(), null));
		attributeSet.add(new Copies(1));
		attributeSet.add(Sides.ONE_SIDED);
		attributeSet.add(OrientationRequested.PORTRAIT);
		PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, null);//attributeSet);
		if (services==null || services.length==0) {
			ResourceBundle msg = owner.getResourceBundle();
			JOptionPane.showMessageDialog(owner,
				msg.getString("ErrorNoPrintServices"),
				msg.getString("ErrorDialogTitle"),
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
		//DocFlavor[] flavors = defaultService.getSupportedDocFlavors();
		PrintService chosenService = ServiceUI.printDialog(null, 200, 200,
									services, defaultService, flavor,
									attributeSet);
		if (chosenService != null) {

			DocPrintJob job = chosenService.createPrintJob();
			Doc myDoc = new SimpleDoc(textArea, flavor, null);//attributeSet);

			try {
				job.print(myDoc, attributeSet);
			} catch (PrintException pe) {
				ResourceBundle msg = owner.getResourceBundle();
				JOptionPane.showMessageDialog(owner,
					msg.getString("ErrorDialogPrintText") + pe + ".",
					msg.getString("ErrorDialogTitle"),
					JOptionPane.ERROR_MESSAGE);
			}

		}

	}


}