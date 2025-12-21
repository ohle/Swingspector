package de.eudaemon.ideaswag;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tree.TreeVisitor.Action;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;

import icons.IdeaSwagIcons;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;

public class TreeViewPanel extends JPanel implements Refreshable {

    private final RunningComponent root;
    private Tree tree;
    private final Disposable disposer;
    private boolean autoLocateOn = false;
    private ComponentTreeNodeRenderer cellRenderer = new ComponentTreeNodeRenderer(false, false);

    public TreeViewPanel(RunningComponent component_, Disposable disposer_) {
        root = component_;
        disposer = disposer_;
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();
        ComponentTreeNode rootNode = new ComponentTreeNode(root);
        tree = new Tree(rootNode);
        tree.setCellRenderer(cellRenderer);
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
        ActionToolbar toolBar =
                ActionManager.getInstance()
                        .createActionToolbar(
                                ActionPlaces.TOOLWINDOW_CONTENT, createToolbarActions(), true);
        toolBar.setTargetComponent(this);
        add(toolBar.getComponent(), BorderLayout.NORTH);
    }

    private ActionGroup createToolbarActions() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(
                Util.actionBuilder()
                        .description("Refresh view")
                        .icon(Actions.Refresh)
                        .build(this::refresh));
        group.add(
                Util.actionBuilder()
                        .description("Expand all tree nodes")
                        .text("Expand All Tree Nodes")
                        .icon(Actions.Expandall)
                        .build(this::expandAll));
        group.addSeparator();
        group.add(
                Util.actionBuilder()
                        .description("Open selected component")
                        .text("Open Selected Component")
                        .icon(General.Locate)
                        .build(this::locateSelected));
        group.add(
                Util.actionBuilder()
                        .description("Automatically open selected component")
                        .text("Automatically Open Selected Component")
                        .icon(General.AutoscrollToSource)
                        .buildToggle(this::isAutoLocateOn, this::setAutoLocate));
        group.addSeparator();
        group.add(
                Util.actionBuilder()
                        .description("Show widths")
                        .icon(IdeaSwagIcons.Width)
                        .buildToggle(cellRenderer::isShowWidths, this::setShowWidths));
        group.add(
                Util.actionBuilder()
                        .description("Show heights")
                        .icon(IdeaSwagIcons.Height)
                        .buildToggle(cellRenderer::isShowHeights, this::setShowHeights));
        return group;
    }

    private void setShowWidths(boolean show) {
        cellRenderer.setShowWidths(show);
        repaint();
    }

    private void setShowHeights(boolean show) {
        cellRenderer.setShowHeights(show);
        repaint();
    }

    private void autoLocate() {
        if (autoLocateOn) {
            getSelectedComponent()
                    .ifPresent(c -> Util.openComponentTab(c, tree::requestFocus, disposer));
        }
    }

    public void expandAll() {
        TreeUtil.expandAll(tree);
    }

    public void collapseAll() {
        TreeUtil.collapseAll(tree, -1);
    }

    public void locateSelected() {
        getSelectedComponent().ifPresent(component -> Util.openComponentTab(component, disposer));
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
