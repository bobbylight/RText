/*
 * 03/30/2012
 *
 * SourceTreeNode.java - The tree node used in DefaultSourceTrees.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.rtext.plugins.sourcebrowser;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.fife.ui.autocomplete.Util;


/**
 * Base class for tree nodes in an <code>DefaultSourceTree</code>.  They can
 * be sorted and filtered based on user input.<p>
 * This class is a clone of the identical class in
 * <code>RSTALanguageSupport</code>, but unfortunately exists to prevent
 * a dependency on that library in this plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SourceTreeNode extends DefaultMutableTreeNode {

	private boolean sortable;
	private boolean sorted;
	private String prefix;
	private Vector<Object> visibleChildren;
	private int sortPriority;


	public SourceTreeNode(Object userObject) {
		this(userObject, false);
	}


	public SourceTreeNode(Object userObject, boolean sorted) {
		super(userObject);
		visibleChildren = new Vector<Object>();
		setSortable(true);
		setSorted(sorted);
	}


	@Override
	public void add(MutableTreeNode child) {
		//super.add(child);
		if(child!=null && child.getParent()==this) {
			insert(child, super.getChildCount() - 1);
		}
		else {
			insert(child, super.getChildCount());
		}
		if (sortable && sorted) {
			refreshVisibleChildren(); // TODO: Find index and add for performance
		}
	}


	@Override
	public Enumeration<?> children() {
		return visibleChildren.elements();
	}


	public int compareTo(Object obj) {
		int res = -1;
		if (obj instanceof SourceTreeNode) {
			SourceTreeNode stn2 = (SourceTreeNode)obj;
			res = getSortPriority() - stn2.getSortPriority();
			if (res==0 && ((SourceTreeNode)getParent()).isSorted()) {
				res = toString().compareToIgnoreCase(stn2.toString());
			}
		}
		return res;
	}


	/**
	 * Returns a comparator used to sort the child nodes of this node.
	 * The default implementation sorts alphabetically.  Subclasses may want
	 * to override to return a comparator that groups by type of node and sorts
	 * by group, etc.
	 *
	 * @return A comparator.
	 */
	// We can't be more specific with our type as Swing's not genericized
	public Comparator<Object> createComparator() {
		return new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				SourceTreeNode stn1 = (SourceTreeNode)o1;
				SourceTreeNode stn2 = (SourceTreeNode)o2;
				return stn1.compareTo(stn2);
			}
		};
	}


	/**
	 * Filters the children of this tree node based on the specified prefix.
	 *
	 * @param prefix The prefix.  If this is <code>null</code>, all possible
	 *        children are shown.  This should be all lower case.
	 */
	protected void filter(String prefix) {
		this.prefix = prefix;
		refreshVisibleChildren();
		for (int i=0; i<super.getChildCount(); i++) {
			Object child = children.get(i);
			if (child instanceof SourceTreeNode) {
				((SourceTreeNode)child).filter(prefix);
			}
		}
	}


	@Override
	public TreeNode getChildAfter(TreeNode child) {
		if (child==null) {
			throw new IllegalArgumentException("child cannot be null");
		}
		int index = getIndex(child);
		if (index==-1) {
			throw new IllegalArgumentException("child node not contained");
		}
		return index<getChildCount()-1 ? getChildAt(index+1) : null;
	}


	@Override
	public TreeNode getChildAt(int index) {
		return (TreeNode)visibleChildren.get(index);
	}


	@Override
	public TreeNode getChildBefore(TreeNode child) {
		if (child==null) {
			throw new IllegalArgumentException("child cannot be null");
		}
		int index = getIndex(child);
		if (index==-1) {
			throw new IllegalArgumentException("child node not contained");
		}
		return index> 0 ? getChildAt(index - 1) : null;
	}


	@Override
	public int getChildCount() {
		return visibleChildren.size();
	}


	@Override
	public int getIndex(TreeNode child) {
		if (child==null) {
			throw new IllegalArgumentException("child cannot be null");
		}
		for (int i=0; i<visibleChildren.size(); i++) {
			TreeNode node = (TreeNode)visibleChildren.get(i);
			if (node.equals(child)) {
				return i;
			}
		}
		return -1;
	}


	/**
	 * Returns the relative priority of this node against others when being
	 * sorted (lower is higher priority).
	 *
	 * @return The relative priority.
	 * @see #setSortPriority(int)
	 */
	public int getSortPriority() {
		return sortPriority;
	}


    /**
	 * Returns whether this particular node's children can be sorted.
	 *
	 * @return Whether this node's children can be sorted.
	 * @see #setSortable(boolean)
	 */
	public boolean isSortable() {
		return sortable;
	}


	/**
	 * Returns whether this node is sorted.
	 *
	 * @return Whether this node is sorted.
	 */
	public boolean isSorted() {
		return sorted;
	}


	protected void refresh() {
		refreshVisibleChildren();
		for (int i=0; i<getChildCount(); i++) {
			TreeNode child = getChildAt(i);
			if (child instanceof SourceTreeNode) {
				((SourceTreeNode)child).refresh();
			}
		}
	}


	/**
	 * Refreshes what children are visible in the tree.
	 */
	@SuppressWarnings("unchecked")
	private void refreshVisibleChildren() {
		visibleChildren.clear();
		if (children!=null) {
			visibleChildren.addAll(children);
			if (sortable && sorted) {
				Collections.sort(visibleChildren, createComparator());
			}
			if (prefix!=null) {
				Iterator<Object> i = visibleChildren.iterator();
				while (i.hasNext()) {
					TreeNode node = (TreeNode)i.next();
					if (node.isLeaf()) {
						String text = node.toString();
						text = Util.stripHtml(text);
						if (!text.toLowerCase().startsWith(prefix)) {
							//System.out.println("Removing tree node: " + text);
							i.remove();
						}
					}
				}
			}
		}
	}


	/**
	 * Sets whether this particular node's children are sortable.  Usually,
	 * only tree nodes containing only "leaves" should be sorted (for example,
	 * a "types" node).
	 *
	 * @param sortable Whether this node's children are sortable.
	 * @see #isSortable()
	 */
	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}


	/**
	 * Sets whether this tree node (and any child sortable tree nodes) are
	 * sorting their children.
	 *
	 * @param sorted Whether sorting is enabled.
	 * @see #isSorted()
	 */
	public void setSorted(boolean sorted) {
		if (sorted!=this.sorted) {
			// We must keep this state, even if we're not sortable, so that
			// we can know when to toggle the sortable state of our children.
			this.sorted = sorted;
			// This individual node may not be sortable...
			if (sortable) {
				refreshVisibleChildren();
			}
			// But its children could still be.
			for (int i=0; i<super.getChildCount(); i++) {
				Object child = children.get(i);
				if (child instanceof SourceTreeNode) {
					((SourceTreeNode)child).setSorted(sorted);
				}
			}
		}
	}


	/**
	 * Sets the relative sort priority of this tree node when it is compared
	 * against others (lower is higher priority).
	 *
	 * @param priority The relative priority.
	 * @see #getSortPriority()
	 */
	public void setSortPriority(int priority) {
		this.sortPriority = priority;
	}


}