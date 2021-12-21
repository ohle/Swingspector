package de.eudaemon.ideaswag;

import java.util.HashSet;
import java.util.Set;

import java.util.concurrent.CompletableFuture;

import java.awt.BorderLayout;
import java.awt.Component;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import com.intellij.diagnostic.logging.AdditionalTabComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts.TabTitle;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.UIUtil.ComponentStyle;

import de.eudaemon.swag.ComponentInfoMBean;
import icons.IdeaSwagIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwagRootsTab extends AdditionalTabComponent implements Disposable {

    private final JBList<RunningComponent> rootsList;
    private final DefaultListModel<RunningComponent> roots = new DefaultListModel<>();
    private final Project project;

    private final Set<Content> openedTabs = new HashSet<>();

    public SwagRootsTab(CompletableFuture<ComponentInfoMBean> infoBean, Project project_) {
        project = project_;
        rootsList = createList();
        infoBean.thenAcceptAsync(this::init, ApplicationManager.getApplication()::invokeLater);
        setLayout(new BorderLayout());
        add(new JBLabel("Roots", ComponentStyle.LARGE), BorderLayout.NORTH);
        add(new JBScrollPane(rootsList), BorderLayout.CENTER);
    }

    private void init(ComponentInfoMBean infoBean) {
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
        openedTabs.add(Util.openTreeTab(roots.get(rootsList.getSelectedIndex())));
    }

    @Override
    public @NotNull @TabTitle String getTabTitle() {
        return "Swag";
    }

    @Override
    public JComponent getPreferredFocusableComponent() {
        return rootsList;
    }

    @Override
    public void dispose() {
        openedTabs.forEach(Util::removeTreeTab);
    }

    @Override
    public @Nullable ActionGroup getToolbarActions() {
        return null;
    }

    @Override
    public @Nullable JComponent getSearchComponent() {
        return null;
    }

    @Override
    public @NonNls @Nullable String getToolbarPlace() {
        return null;
    }

    @Override
    public @Nullable JComponent getToolbarContextComponent() {
        return null;
    }

    @Override
    public boolean isContentBuiltIn() {
        return false;
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
