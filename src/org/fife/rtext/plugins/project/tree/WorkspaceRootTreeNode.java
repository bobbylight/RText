package org.fife.rtext.plugins.project.tree;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.text.DocumentFilter;

import org.fife.rtext.NumberDocumentFilter;
import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectPlugin;
import org.fife.rtext.plugins.project.Workspace;


/**
 * Tree node for the root of a workspace.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class WorkspaceRootTreeNode extends AbstractWorkspaceTreeNode {

	private Workspace workspace;
	private Icon icon;


	public WorkspaceRootTreeNode(ProjectPlugin plugin, Workspace workspace) {
		super(plugin);
		this.workspace = workspace;
		icon = new ImageIcon(getClass().getResource("application_double.png"));
	}


	/**
	 * {@inheritDoc}
	 */
	public Icon getIcon() {
		return icon;
	}


	public List getPopupActions() {
		List actions = new ArrayList();
		actions.add(new RenameAction());
		actions.add(null);
		actions.add(new PropertiesAction());
		return actions;
	}


	protected void handleProperties() {
		JOptionPane.showMessageDialog(null, "Properties of the item!");
	}


	protected void handleRename() {
		RText rtext = plugin.getRText();
		String type = Messages.getString("ProjectPlugin.Workspace");
		DocumentFilter filter = new NumberDocumentFilter();
		RenameDialog dialog = new RenameDialog(rtext, type, filter);
		dialog.setName(workspace.getName());
		dialog.setVisible(true);
	}


	public String toString() {
		return workspace.getName();
	}


}