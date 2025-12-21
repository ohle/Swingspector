package de.eudaemon.ideaswag;

import de.eudaemon.swag.ComponentDescription;
import de.eudaemon.swag.SizeInfo;
import de.eudaemon.swag.SizeInfos;

import icons.IdeaSwagIcons;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class ComponentTreeNodeRenderer extends DefaultTreeCellRenderer {
    private boolean showWidths;
    private boolean showHeights;

    public ComponentTreeNodeRenderer(boolean showWidths_, boolean showHeights_) {
        showWidths = showWidths_;
        showHeights = showHeights_;
    }

    public boolean isShowWidths() {
        return showWidths;
    }

    public void setShowWidths(boolean show) {
        showWidths = show;
    }

    public boolean isShowHeights() {
        return showHeights;
    }

    public void setShowHeights(boolean show) {
        showHeights = show;
    }

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
            StringBuilder titleBuilder = new StringBuilder("<html>").append(title);
            node.getComponent()
                    .getProperty("layoutManagerClassName")
                    .ifPresent(
                            layout -> {
                                titleBuilder
                                        .append(" (")
                                        .append(layout.valueDescription)
                                        .append(")");
                            });
            if (showWidths) {
                titleBuilder.append(" ");
                SizeInfos sizes = node.getComponent().getSizeInfos();
                titleBuilder
                        .append(highlighted(sizes, SizeVariant.MIN, Direction.WIDTH))
                        .append(" ");
                int actual = sizes.actualSize.width;
                String preferred = highlighted(sizes, SizeVariant.PREFERRED, Direction.WIDTH);
                if (actual < sizes.preferredSize.size.width) {
                    titleBuilder.append(actual).append(" ").append(preferred).append(" ");
                } else {
                    titleBuilder.append(preferred).append(" ").append(actual).append(" ");
                }
                titleBuilder.append(highlighted(sizes, SizeVariant.MAX, Direction.WIDTH));
            }
            if (showHeights) {
                titleBuilder.append(showWidths ? " | " : " ");
                SizeInfos sizes = node.getComponent().getSizeInfos();
                titleBuilder
                        .append(highlighted(sizes, SizeVariant.MIN, Direction.HEIGHT))
                        .append(" ");
                int actual = sizes.actualSize.height;
                String preferred = highlighted(sizes, SizeVariant.PREFERRED, Direction.HEIGHT);
                if (actual < sizes.preferredSize.size.height) {
                    titleBuilder.append(actual).append(" ").append(preferred).append(" ");
                } else {
                    titleBuilder.append(preferred).append(" ").append(actual).append(" ");
                }
                titleBuilder.append(highlighted(sizes, SizeVariant.MAX, Direction.HEIGHT));
            }
            titleBuilder.append("</html>");
            setText(titleBuilder.toString());
        } catch (Throwable ignored) {
            // happens while removing tree on dispose, when connection is already down
        }
        return this;
    }

    enum SizeVariant {
        MIN,
        MAX,
        PREFERRED
    }

    enum Direction {
        WIDTH,
        HEIGHT
    }

    private static String highlighted(SizeInfos sizes, SizeVariant variant, Direction direction) {
        Color color =
                switch (variant) {
                    case MIN -> Util.MIN_SIZE_COLOR;
                    case MAX -> Util.MAX_SIZE_COLOR;
                    case PREFERRED -> Util.PREF_SIZE_COLOR;
                };
        SizeInfo si =
                switch (variant) {
                    case MIN -> sizes.minimumSize;
                    case MAX -> sizes.maximumSize;
                    case PREFERRED -> sizes.preferredSize;
                };
        int size =
                switch (direction) {
                    case WIDTH -> si.size.width;
                    case HEIGHT -> si.size.height;
                };
        return "<font color='%s'>%s%d%s</font>"
                .formatted(Util.hexColor(color), si.set ? "<u>" : "", size, si.set ? "</u>" : "");
    }
}
