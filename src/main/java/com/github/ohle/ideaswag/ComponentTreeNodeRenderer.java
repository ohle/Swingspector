package com.github.ohle.ideaswag;

import java.awt.Component;

import javax.swing.JTree;

import javax.swing.tree.DefaultTreeCellRenderer;

import de.eudaemon.swag.ComponentDescription;
import icons.IdeaSwagIcons;

public class ComponentTreeNodeRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
        ComponentTreeNode node = (ComponentTreeNode) value;
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        ComponentDescription description = node.getComponent().getDescription();
        setText(Util.generateTitle(description));
        setIcon(IdeaSwagIcons.fromDescription(description));
        return this;
    }
}
