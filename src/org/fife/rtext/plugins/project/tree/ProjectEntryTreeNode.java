package org.fife.rtext.plugins.project.tree;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.DocumentFilter;

import org.fife.rtext.NumberDocumentFilter;
import org.fife.rtext.RText;
import org.fife.rtext.plugins.project.Messages;
import org.fife.rtext.plugins.project.ProjectEntry;
import org.fife.rtext.plugins.project.ProjectPlugin;


/**
 * The tree node used for project entries.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ProjectEntryTreeNode extends AbstractWorkspaceTreeNode {

	private ProjectEntry entry;
	private Icon icon;


	public ProjectEntryTreeNode(ProjectPlugin plugin, ProjectEntry entry) {
		super(plugin);
		this.entry = entry;
		icon = FileSystemView.getFileSystemView().getSystemIcon(entry.getFile());
	}


	public List getPopupActions() {
		List actions = new ArrayList();
		actions.add(new RenameAction());
		actions.add(null);
		actions.add(new PropertiesAction());
		return actions;
	}


	public Icon getIcon() {
		return icon;
	}


	protected void handleProperties() {
		JOptionPane.showMessageDialog(null, "Properties of the item!");
	}


	protected void handleRename() {
		RText rtext = plugin.getRText();
		boolean directory = entry.getFile().isDirectory();
		String key = "ProjectPlugin." + (directory ? "Directory" : "File");
		String type = Messages.getString(key);
		DocumentFilter filter = new NumberDocumentFilter();
		RenameDialog dialog = new RenameDialog(rtext, type, filter);
		dialog.setName(entry.getFile().getName());
		dialog.setVisible(true);
	}


	public String toString() {
		return entry.getFile().getName();
	}
	

}