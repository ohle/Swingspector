package de.eudaemon.ideaswag;

import java.util.Optional;

import java.awt.BorderLayout;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import javax.swing.tree.TreeSelectionModel;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tree.TreeVisitor.Action;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;

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
        tree.addTreeSelectionListener(e -> autoLocate());
        setLayout(new BorderLayout());
        add(new JBScrollPane(tree), BorderLayout.CENTER);

        tree.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            locateSelected();
                        }
                    }
                });
        tree.addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            locateSelected();
                        }
                    }
                });
        AnAction actionGroup = ActionManager.getInstance().getAction("IdeaSWAG.TreeView");
        ActionToolbar toolBar =
                ActionManager.getInstance()
                        .createActionToolbar(
                                ActionPlaces.TOOLWINDOW_CONTENT, (ActionGroup) actionGroup, true);
        toolBar.setTargetComponent(this);
        add(toolBar.getComponent(), BorderLayout.NORTH);
    }

    private void autoLocate() {
        if (autoLocateOn) {
            getSelectedComponent().ifPresent(c -> Util.openComponentTab(c, tree::requestFocus));
        }
    }

    public void expandAll() {
        TreeUtil.expandAll(tree);
    }

    public void collapseAll() {
        TreeUtil.collapseAll(tree, -1);
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
        autoLocate();
    }

    public void selectComponent(RunningComponent rc) {
        TreeUtil.promiseSelect(
                tree,
                path -> {
                    if (((ComponentTreeNode) path.getLastPathComponent()).getComponent().getId()
                            == rc.getId()) {
                        tree.setSelectionPath(path);
                        return Action.INTERRUPT;
                    }
                    return Action.CONTINUE;
                });
    }
}
