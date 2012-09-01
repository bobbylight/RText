package org.fife.rtext.plugins.project.tree;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DocumentFilter;

import org.fife.rtext.NumberDocumentFilter;
import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.Project;
import org.fife.rtext.plugins.project.ProjectPlugin;


/**
 * Tree node for projects.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ProjectTreeNode extends AbstractWorkspaceTreeNode {

	private Project project;
	private static Icon icon;


	public ProjectTreeNode(ProjectPlugin plugin, Project project) {
		super(plugin);
		this.project = project;
	}


	public List getPopupActions() {
		List actions = new ArrayList();
		actions.add(new RenameAction());
		actions.add(null);
		actions.add(new PropertiesAction());
		return actions;
	}


	/**
	 * {@inheritDoc}
	 */
	public Icon getIcon() {
		return icon;
	}


	protected void handleProperties() {
		JOptionPane.showMessageDialog(null, "Properties of the item!");
	}


	protected void handleRename() {
		RText rtext = plugin.getRText();
		String type = Messages.getString("ProjectPlugin.Project");
		DocumentFilter filter = new NumberDocumentFilter();
		RenameDialog dialog = new RenameDialog(rtext, type, filter);
		dialog.setName(project.getName());
		dialog.setVisible(true);
	}


	public String toString() {
		return project.getName();
	}


	static {
		icon = new ImageIcon(ProjectTreeNode.class.
				getResource("application.png"));
	}


}