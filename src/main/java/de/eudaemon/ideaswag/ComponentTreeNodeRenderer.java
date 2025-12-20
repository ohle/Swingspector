package de.eudaemon.ideaswag;

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
        try {
            ComponentDescription description = node.getComponent().getDescription();
            String title = Util.generateTitle(description);
            setIcon(IdeaSwagIcons.fromDescription(description));
            StringBuilder titleBuilder = new StringBuilder(title);
            node.getComponent().getProperty("layoutManagerClassName").ifPresent(layout -> {
                titleBuilder.append(" (").append(layout.valueDescription).append(")");
            });
            setText(titleBuilder.toString());
        } catch (Throwable ignored) {
            // happens while removing tree on dispose, when connection is already down
        }
        return this;
    }
}
