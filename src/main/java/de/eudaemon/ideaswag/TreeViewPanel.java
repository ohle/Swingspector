package de.eudaemon.ideaswag;

import java.util.Optional;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import javax.swing.tree.TreeSelectionModel;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;

public class TreeViewPanel extends JPanel {

    private final RunningComponent root;
    private final Tree tree;
    private boolean autoLocateOn = false;

    public TreeViewPanel(RunningComponent component_) {
        root = component_;
        ComponentTreeNode rootNode = new ComponentTreeNode(root);
        tree = new Tree(rootNode);
        tree.setCellRenderer(new ComponentTreeNodeRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(
                e -> {
                    if (autoLocateOn) {
                        locateSelected();
                    }
                });
        setLayout(new BorderLayout());
        add(new JBScrollPane(tree), BorderLayout.CENTER);

        AnAction actionGroup = ActionManager.getInstance().getAction("IdeaSWAG.TreeView");
        ActionToolbar toolBar =
                ActionManager.getInstance()
                        .createActionToolbar(
                                ActionPlaces.TOOLWINDOW_CONTENT, (ActionGroup) actionGroup, true);
        toolBar.setTargetComponent(this);
        add(toolBar.getComponent(), BorderLayout.NORTH);
    }

    public void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    public void collapseAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.collapseRow(i);
        }
    }

    public void locateSelected() {
        getSelectedComponent().ifPresent(Util::openComponentTab);
    }

    private Optional<RunningComponent> getSelectedComponent() {
        ComponentTreeNode[] selectedNodes = tree.getSelectedNodes(ComponentTreeNode.class, null);
        if (selectedNodes.length != 0) {
            return Optional.ofNullable(selectedNodes[0].getComponent());
        } else {
            return Optional.empty();
        }
    }

    public boolean isAutoLocateOn() {
        return autoLocateOn;
    }

    public void setAutoLocate(boolean state) {
        autoLocateOn = state;
    }
}
