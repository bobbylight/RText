package org.fife.rtext.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.fife.rtext.RText;
import org.fife.rtext.RTextActionInfo;


/**
 * Manages the tools available in an RText session.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ToolManager {

	private List tools;
	private JMenu toolsMenu;

//	private static final String MSG = "org.fife.rtext.tools.ToolManager";
//	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);

	private static final ToolManager INSTANCE = new ToolManager();


	/**
	 * Private constructor to prevent instantiation.
	 */
	private ToolManager() {

		tools = new ArrayList();

		toolsMenu = new JMenu("Tools");//msg.getString("Menu.Name"));
		toolsMenu.addSeparator();

	}


	/**
	 * Adds a tool.
	 *
	 * @param tool The tool to add.
	 * @see #removeTool(Tool)
	 */
	public void addTool(Tool tool) {
		tools.add(tool);
		Collections.sort(tools);
		refreshToolMenu();
	}


	/**
	 * Returns whether a tool with a given name is already defined.
	 *
	 * @param name The name of the tool.
	 * @return Whether a tool with that name is already defined.
	 */
	public boolean containsToolWithName(String name) {
		for (Iterator i=tools.iterator(); i.hasNext(); ) {
			Tool tool = (Tool)i.next();
			if (name.equals(tool.getName())) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Returns the singleton instance of the tool manager.
	 *
	 * @return The tool manager instance.
	 */
	public static ToolManager get() {
		return INSTANCE;
	}


	/**
	 * Returns the menu displaying the user tools.
	 *
	 * @return The tools menu.
	 */
	public JMenu getToolsMenu() {
		return toolsMenu;
	}


	/**
	 * Initializes the tools manager.  This must be called before it is used.
	 *
	 * @param rtext The parent application.
	 */
	public void init(RText rtext) {
		Action a = rtext.getAction(RTextActionInfo.NEW_TOOL_ACTION);
		toolsMenu.add(new JMenuItem(a));//createMenuItem(a));
	}


	private void refreshToolMenu() {

		while (toolsMenu.getItemCount()>2) {
			toolsMenu.remove(0);
		}

		if (tools.size()>0) {
			for (Iterator i=tools.iterator(); i.hasNext(); ) {
				Tool tool = (Tool)i.next();
				JMenuItem item = new JMenuItem(tool.getName());
				//item.setAccelerator(tool.getAccelerator());
				toolsMenu.add(item, toolsMenu.getComponentCount()-2);
			}
		}
		else {
			// TODO: Localize me!
			JMenuItem item = new JMenuItem("<No Tools Defined>");
			item.setEnabled(false);
			toolsMenu.add(item);
		}

	}


	/**
	 * Removes a tool.
	 *
	 * @param tool The tool to remove.
	 * @see #addTool(Tool)
	 */
	public void removeTool(Tool tool) {
		tools.remove(tool);
		refreshToolMenu();
	}


}