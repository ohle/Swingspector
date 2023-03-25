package de.eudaemon.ideaswag;

import java.util.concurrent.CompletableFuture;

import java.awt.BorderLayout;
import java.awt.Component;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil.ComponentStyle;

import de.eudaemon.swag.ComponentInfoMBean;
import icons.IdeaSwagIcons;

public class SwingRoots extends JPanel implements Refreshable {

    private ComponentInfoMBean infoBean = null;
    private JBList<RunningComponent> rootsList;
    private final DefaultListModel<RunningComponent> roots = new DefaultListModel<>();
    private final Project project;
    private final Disposable disposer;

    public SwingRoots(
            CompletableFuture<ComponentInfoMBean> infoBean_,
            Project project_,
            Disposable disposer_) {
        project = project_;
        disposer = disposer_;
        infoBean_.thenAcceptAsync(this::init, ApplicationManager.getApplication()::invokeLater);
        setLayout(new BorderLayout());
        AnAction actionGroup = ActionManager.getInstance().getAction("IdeaSWAG.RootsView");
        ActionToolbar toolBar =
                ActionManager.getInstance()
                        .createActionToolbar(
                                ActionPlaces.TOOLWINDOW_CONTENT, (ActionGroup) actionGroup, false);
        toolBar.setTargetComponent(this);
        add(toolBar.getComponent(), BorderLayout.WEST);
        rootsList = createList();
        add(new JBLabel("Swingspector root windows", ComponentStyle.LARGE), BorderLayout.NORTH);
        add(new JBScrollPane(rootsList), BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        if (infoBean == null) {
            return;
        }
        rootsList.setPaintBusy(true);
        roots.removeAllElements();
        roots.addAll(RunningComponent.getRoots(infoBean, project));
        rootsList.setPaintBusy(false);
    }

    private void init(ComponentInfoMBean infoBean_) {
        infoBean = infoBean_;
        roots.addAll(RunningComponent.getRoots(infoBean, project));
        rootsList.setPaintBusy(false);
    }

    private JBList<RunningComponent> createList() {
        JBList<RunningComponent> list = new JBList<>(roots);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new Renderer());
        list.setPaintBusy(true);
        list.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            openSelectedTree();
                        }
                    }
                });
        list.addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            openSelectedTree();
                        }
                    }
                });
        return list;
    }

    private void openSelectedTree() {
        Util.openTreeTab(roots.get(rootsList.getSelectedIndex()), disposer);
    }

    private static class Renderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            RunningComponent component = (RunningComponent) value;
            try {
                setText(Util.generateTitle(component.getDescription()));
                setIcon(IdeaSwagIcons.Window);
            } catch (Throwable ignored) {
                // happens while removing, when connection is already closed
            }
            return this;
        }
    }
}
