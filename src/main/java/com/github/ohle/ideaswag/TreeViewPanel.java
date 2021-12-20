package com.github.ohle.ideaswag;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;

public class TreeViewPanel extends JPanel {

    private final RunningComponent root;

    public TreeViewPanel(RunningComponent component_) {
        root = component_;
        ComponentTreeNode rootNode = new ComponentTreeNode(root);
        Tree tree = new Tree(rootNode);
        tree.setCellRenderer(new ComponentTreeNodeRenderer());
        setLayout(new BorderLayout());
        add(new JBScrollPane(tree), BorderLayout.CENTER);
    }
}
