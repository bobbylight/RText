/*
 * ActionFactory - Manages all of RText's actions.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.actions;

import java.awt.ComponentOrientation;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.fife.rtext.RText;
import org.fife.rtext.RTextActionInfo;
import org.fife.rtext.RTextPreferences;
import org.fife.ui.app.StandardAction;
import org.fife.ui.app.GUIApplication.AboutAction;
import org.fife.ui.app.GUIApplication.HelpAction;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit;
import org.fife.ui.rtextarea.RTextAreaEditorKit;


/**
 * Creates all of the actions for RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ActionFactory implements RTextActionInfo {


	/**
	 * Private constructor to prevent instantiation.
	 */
	private ActionFactory() {
	}


	/**
	 * Installs RText's actions.
	 *
	 * @param rtext The application instance.
	 */
	public static void addActions(RText rtext, RTextPreferences prefs) {

		ResourceBundle msg = ResourceBundle.getBundle(
									"org.fife.rtext.actions.Actions");

		ClassLoader cl = ActionFactory.class.getClassLoader();
		String commonIconPath = "org/fife/rtext/graphics/common_icons/";

		try {
			rtext.setIconImage(ImageIO.read(cl.getResource(
						"org/fife/rtext/graphics/rtexticon.gif")));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		StandardAction a = new NewAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(NEW_ACTION));
		rtext.addAction(NEW_ACTION, a);

		a = new OpenAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(OPEN_ACTION));
		rtext.addAction(OPEN_ACTION, a);

		a = new OpenInNewWindowAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(OPEN_NEWWIN_ACTION));
		rtext.addAction(OPEN_NEWWIN_ACTION, a);

		a = new OpenRemoteAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(OPEN_REMOTE_ACTION));
		rtext.addAction(OPEN_REMOTE_ACTION, a);

		a = new SaveAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(SAVE_ACTION));
		rtext.addAction(SAVE_ACTION, a);

		a = new SaveAsAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(SAVE_AS_ACTION));
		rtext.addAction(SAVE_AS_ACTION, a);

		a = new SaveAsRemoteAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(SAVE_AS_REMOTE_ACTION));
		rtext.addAction(SAVE_AS_REMOTE_ACTION, a);

		a = new SaveAsWebPageAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(SAVE_WEBPAGE_ACTION));
		rtext.addAction(SAVE_WEBPAGE_ACTION, a);

		a = new SaveAllAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(SAVE_ALL_ACTION));
		rtext.addAction(SAVE_ALL_ACTION, a);

		a = new RText.ExitAction(rtext, msg, "ExitAction");
		a.setAccelerator(prefs.getAccelerator(RText.EXIT_ACTION_KEY));
		rtext.addAction(RText.EXIT_ACTION_KEY, a);

		String temp = msg.getString("CopyAsRtfAction");
		rtext.addAction(COPY_AS_RTF_ACTION, new RSyntaxTextAreaEditorKit.CopyAsRtfAction(temp,
			null,
			msg.getString("CopyAsRtfAction.ShortDesc"),
			new Integer(msg.getString("CopyAsRtfAction.Mnemonic").charAt(0)),
			prefs.getAccelerator(COPY_AS_RTF_ACTION)));

		temp = msg.getString("TimeAction");
		rtext.addAction(TIME_DATE_ACTION, new RTextAreaEditorKit.TimeDateAction(temp,
			new ImageIcon(cl.getResource(commonIconPath+"timedate16.gif")),
			msg.getString("TimeAction.ShortDesc"),
			new Integer(msg.getString("TimeAction.Mnemonic").charAt(0)),
			prefs.getAccelerator(TIME_DATE_ACTION)));

		a = new RText.ToggleToolBarAction(rtext, msg, "ToolBarAction");
		a.setAccelerator(prefs.getAccelerator(TOOL_BAR_ACTION));
		rtext.addAction(TOOL_BAR_ACTION, a);

		a = new RText.ToggleStatusBarAction(rtext, msg, "StatusBarAction");
		a.setAccelerator(prefs.getAccelerator(STATUS_BAR_ACTION));
		rtext.addAction(STATUS_BAR_ACTION, a);

		a = new LineNumberAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(LINE_NUMBER_ACTION));
		rtext.addAction(LINE_NUMBER_ACTION, a);

		a = new FilePropertiesAction(rtext, msg);
		a.setAccelerator(prefs.getAccelerator(FILE_PROPERTIES_ACTION));
		rtext.addAction(FILE_PROPERTIES_ACTION, a);

		a = new HelpAction(rtext, msg, "HelpAction");
		a.setAccelerator(prefs.getAccelerator(RText.HELP_ACTION_KEY));
		rtext.addAction(RText.HELP_ACTION_KEY, a);

		a = new AboutAction(rtext, msg, "AboutAction");
		a.setAccelerator(prefs.getAccelerator(RText.ABOUT_ACTION_KEY));
		rtext.addAction(RText.ABOUT_ACTION_KEY, a);

		a = new OptionsAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(OPTIONS_ACTION));
		rtext.addAction(OPTIONS_ACTION, a);

		a = new HomePageAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(HOME_PAGE_ACTION));
		rtext.addAction(HOME_PAGE_ACTION, a);

		a = new CheckForUpdatesAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(UPDATES_ACTION));
		rtext.addAction(UPDATES_ACTION, a);

		a = new CloseAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(CLOSE_ACTION));
		rtext.addAction(CLOSE_ACTION, a);

		a = new CloseAllAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(CLOSE_ALL_ACTION));
		rtext.addAction(CLOSE_ALL_ACTION, a);

		a = new FindAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(FIND_ACTION));
		rtext.addAction(FIND_ACTION, a);

		a = new FindNextAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(FIND_NEXT_ACTION));
		rtext.addAction(FIND_NEXT_ACTION, a);

		a = new ReplaceAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(REPLACE_ACTION));
		rtext.addAction(REPLACE_ACTION, a);

		a = new ReplaceNextAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(REPLACE_NEXT_ACTION));
		rtext.addAction(REPLACE_NEXT_ACTION, a);

		a = new ReplaceAllAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(REPLACE_ALL_ACTION));
		rtext.addAction(REPLACE_ALL_ACTION, a);

		a = new FindInFilesAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(FIND_IN_FILES_ACTION));
		rtext.addAction(FIND_IN_FILES_ACTION, a);

		a = new ReplaceInFilesAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(REPLACE_IN_FILES_ACTION));
		rtext.addAction(REPLACE_IN_FILES_ACTION, a);

		a = new PrintAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(PRINT_ACTION));
		rtext.addAction(PRINT_ACTION, a);

		a = new PrintPreviewAction(rtext, msg, null);
		a.setAccelerator(prefs.getAccelerator(PRINT_PREVIEW_ACTION));
		rtext.addAction(PRINT_PREVIEW_ACTION, a);

		a = new GoToAction(rtext, msg, new ImageIcon(cl.getResource(commonIconPath+"goto16.gif")));
		a.setAccelerator(prefs.getAccelerator(GOTO_ACTION));
		rtext.addAction(GOTO_ACTION, a);

		a = new TextAreaOrientationAction(rtext, msg, "LeftToRightAction", null,
				ComponentOrientation.LEFT_TO_RIGHT);
		a.setAccelerator(prefs.getAccelerator(LTR_ACTION));
		rtext.addAction(LTR_ACTION, a);

		a = new TextAreaOrientationAction(rtext, msg, "RightToLeftAction", null,
				ComponentOrientation.RIGHT_TO_LEFT);
		a.setAccelerator(prefs.getAccelerator(RTL_ACTION));
		rtext.addAction(RTL_ACTION, a);

		a = new MoveFocusLeftAction(rtext, msg);
		a.setAccelerator(prefs.getAccelerator(MOVE_FOCUS_LEFT_ACTION));
		rtext.addAction(MOVE_FOCUS_LEFT_ACTION, a);

		a = new MoveFocusRightAction(rtext, msg);
		a.setAccelerator(prefs.getAccelerator(MOVE_FOCUS_RIGHT_ACTION));
		rtext.addAction(MOVE_FOCUS_RIGHT_ACTION, a);

		a = new MoveFocusUpAction(rtext, msg);
		a.setAccelerator(prefs.getAccelerator(MOVE_FOCUS_UP_ACTION));
		rtext.addAction(MOVE_FOCUS_UP_ACTION, a);

		a = new MoveFocusDownAction(rtext, msg);
		a.setAccelerator(prefs.getAccelerator(MOVE_FOCUS_DOWN_ACTION));
		rtext.addAction(MOVE_FOCUS_DOWN_ACTION, a);

		a = new ViewSplitAction(rtext, msg, null, "SplitHorizontallyAction",
								VIEW_SPLIT_HORIZ_ACTION);
		a.setAccelerator(prefs.getAccelerator(VIEW_SPLIT_HORIZ_ACTION));
		rtext.addAction(VIEW_SPLIT_HORIZ_ACTION, a);

		a = new ViewSplitAction(rtext, msg, null, "SplitNoneAction",
								VIEW_SPLIT_NONE_ACTION);
		a.setAccelerator(prefs.getAccelerator(VIEW_SPLIT_NONE_ACTION));
		rtext.addAction(VIEW_SPLIT_NONE_ACTION, a);

		a = new ViewSplitAction(rtext, msg, null, "SplitVerticallyAction",
								VIEW_SPLIT_VERT_ACTION);
		a.setAccelerator(prefs.getAccelerator(VIEW_SPLIT_VERT_ACTION));
		rtext.addAction(VIEW_SPLIT_VERT_ACTION, a);

		a = new NextDocumentAction(rtext, msg, true);
		a.setAccelerator(prefs.getAccelerator(NEXT_DOCUMENT_ACTION));
		rtext.addAction(NEXT_DOCUMENT_ACTION, a);

		a = new NextDocumentAction(rtext, msg, false);
		a.setAccelerator(prefs.getAccelerator(PREVIOUS_DOCUMENT_ACTION));
		rtext.addAction(PREVIOUS_DOCUMENT_ACTION, a);

		a = new IncreaseFontSizeAction(rtext, msg);
		a.setAccelerator(prefs.getAccelerator(INC_FONT_SIZES_ACTION));
		rtext.addAction(INC_FONT_SIZES_ACTION, a);

		a = new DecreaseFontSizeAction(rtext, msg);
		a.setAccelerator(prefs.getAccelerator(DEC_FONT_SIZES_ACTION));
		rtext.addAction(DEC_FONT_SIZES_ACTION, a);

	}


}