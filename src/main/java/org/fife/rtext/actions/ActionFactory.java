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
import org.fife.rtext.RTextPrefs;
import org.fife.ui.app.AbstractGUIApplication;
import org.fife.ui.app.AppAction;
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
public final class ActionFactory implements RTextActionInfo {


	/**
	 * Private constructor to prevent instantiation.
	 */
	private ActionFactory() {
	}


	/**
	 * Installs RText's actions.
	 *
	 * @param rtext The application instance.
	 * @param prefs The application's preferences.
	 */
	public static void addActions(RText rtext, RTextPrefs prefs) {

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

		AppAction<RText> a = new NewAction(rtext, msg, null);
		rtext.addAction(NEW_ACTION, a);

		a = new OpenAction(rtext, msg, null);
		rtext.addAction(OPEN_ACTION, a);

		a = new OpenInNewWindowAction(rtext, msg, null);
		rtext.addAction(OPEN_NEWWIN_ACTION, a);

		a = new OpenRecentAction(rtext, msg, null);
		rtext.addAction(OPEN_RECENT_ACTION, a);

		a = new OpenRemoteAction(rtext, msg, null);
		rtext.addAction(OPEN_REMOTE_ACTION, a);

		a = new SaveAction(rtext, msg, null);
		rtext.addAction(SAVE_ACTION, a);

		a = new SaveAsAction(rtext, msg, null);
		rtext.addAction(SAVE_AS_ACTION, a);

		a = new SaveAsRemoteAction(rtext, msg, null);
		rtext.addAction(SAVE_AS_REMOTE_ACTION, a);

		a = new SaveAsWebPageAction(rtext, msg, null);
		rtext.addAction(SAVE_WEBPAGE_ACTION, a);

		a = new SaveAllAction(rtext, msg, null);
		rtext.addAction(SAVE_ALL_ACTION, a);

		a = new RText.ExitAction<RText>(rtext, msg, "ExitAction");
		rtext.addAction(RText.EXIT_ACTION_KEY, a);

		String temp = msg.getString("CopyAsRtfAction");
		rtext.addAction(COPY_AS_RTF_ACTION, new RSyntaxTextAreaEditorKit.CopyAsRtfAction(temp,
			null,
			msg.getString("CopyAsRtfAction.ShortDesc"),
			(int)msg.getString("CopyAsRtfAction.Mnemonic").charAt(0),
			null));

		temp = msg.getString("TimeAction");
		rtext.addAction(TIME_DATE_ACTION, new RTextAreaEditorKit.TimeDateAction(temp,
			new ImageIcon(cl.getResource(commonIconPath+"timedate16.gif")),
			msg.getString("TimeAction.ShortDesc"),
			(int)msg.getString("TimeAction.Mnemonic").charAt(0),
			null));

		a = new RText.ToggleToolBarAction<RText>(rtext, msg, "ToolBarAction");
		rtext.addAction(TOOL_BAR_ACTION, a);

		a = new RText.ToggleStatusBarAction<RText>(rtext, msg, "StatusBarAction");
		rtext.addAction(STATUS_BAR_ACTION, a);

		a = new LineNumberAction(rtext, msg, null);
		rtext.addAction(LINE_NUMBER_ACTION, a);

		a = new FilePropertiesAction(rtext, msg);
		rtext.addAction(FILE_PROPERTIES_ACTION, a);

		a = new HelpAction<RText>(rtext, msg, "HelpAction");
		rtext.addAction(RText.HELP_ACTION_KEY, a);

		a = new AboutAction<RText>(rtext, msg, "AboutAction");
		rtext.addAction(RText.ABOUT_ACTION_KEY, a);

		a = new AbstractGUIApplication.OptionsAction<RText>(rtext, msg, "OptionsAction");
		rtext.addAction(OPTIONS_ACTION, a);

		a = new HomePageAction(rtext, msg, null);
		rtext.addAction(HOME_PAGE_ACTION, a);

		a = new CheckForUpdatesAction(rtext, msg, null);
		rtext.addAction(UPDATES_ACTION, a);

		a = new CloseAction(rtext, msg, null);
		rtext.addAction(CLOSE_ACTION, a);

		a = new CloseAllAction(rtext, msg, null);
		rtext.addAction(CLOSE_ALL_ACTION, a);

		a = new FindAction(rtext, msg, null);
		rtext.addAction(FIND_ACTION, a);

		a = new FindNextAction(rtext, msg, null);
		rtext.addAction(FIND_NEXT_ACTION, a);

		a = new ReplaceAction(rtext, msg, null);
		rtext.addAction(REPLACE_ACTION, a);

		a = new ReplaceNextAction(rtext, msg, null);
		rtext.addAction(REPLACE_NEXT_ACTION, a);

		a = new ReplaceAllAction(rtext, msg, null);
		rtext.addAction(REPLACE_ALL_ACTION, a);

		a = new FindInFilesAction(rtext, msg, null);
		rtext.addAction(FIND_IN_FILES_ACTION, a);

		a = new ReplaceInFilesAction(rtext, msg, null);
		rtext.addAction(REPLACE_IN_FILES_ACTION, a);

		a = new PrintAction(rtext, msg, null);
		rtext.addAction(PRINT_ACTION, a);

		a = new PrintPreviewAction(rtext, msg, null);
		rtext.addAction(PRINT_PREVIEW_ACTION, a);

		a = new GoToAction(rtext, msg, new ImageIcon(cl.getResource(commonIconPath+"goto16.gif")));
		rtext.addAction(GOTO_ACTION, a);

		a = new TextAreaOrientationAction(rtext, msg, "LeftToRightAction", null,
				ComponentOrientation.LEFT_TO_RIGHT);
		rtext.addAction(LTR_ACTION, a);

		a = new TextAreaOrientationAction(rtext, msg, "RightToLeftAction", null,
				ComponentOrientation.RIGHT_TO_LEFT);
		rtext.addAction(RTL_ACTION, a);

		a = new MoveFocusLeftAction(rtext, msg);
		rtext.addAction(MOVE_FOCUS_LEFT_ACTION, a);

		a = new MoveFocusRightAction(rtext, msg);
		rtext.addAction(MOVE_FOCUS_RIGHT_ACTION, a);

		a = new MoveFocusUpAction(rtext, msg);
		rtext.addAction(MOVE_FOCUS_UP_ACTION, a);

		a = new MoveFocusDownAction(rtext, msg);
		rtext.addAction(MOVE_FOCUS_DOWN_ACTION, a);

		a = new ViewSplitAction(rtext, msg, null, "SplitHorizontallyAction",
								VIEW_SPLIT_HORIZ_ACTION);
		rtext.addAction(VIEW_SPLIT_HORIZ_ACTION, a);

		a = new ViewSplitAction(rtext, msg, null, "SplitNoneAction",
								VIEW_SPLIT_NONE_ACTION);
		rtext.addAction(VIEW_SPLIT_NONE_ACTION, a);

		a = new ViewSplitAction(rtext, msg, null, "SplitVerticallyAction",
								VIEW_SPLIT_VERT_ACTION);
		rtext.addAction(VIEW_SPLIT_VERT_ACTION, a);

		a = new NextDocumentAction(rtext, msg, true);
		rtext.addAction(NEXT_DOCUMENT_ACTION, a);

		a = new NextDocumentAction(rtext, msg, false);
		rtext.addAction(PREVIOUS_DOCUMENT_ACTION, a);

		a = new IncreaseFontSizeAction(rtext, msg);
		rtext.addAction(INC_FONT_SIZES_ACTION, a);

		a = new DecreaseFontSizeAction(rtext, msg);
		rtext.addAction(DEC_FONT_SIZES_ACTION, a);

	}


}
