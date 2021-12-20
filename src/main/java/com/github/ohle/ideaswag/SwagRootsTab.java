package com.github.ohle.ideaswag;

import java.util.concurrent.CompletableFuture;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;

import com.intellij.diagnostic.logging.AdditionalTabComponent;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.NlsContexts.TabTitle;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil.ComponentStyle;

import de.eudaemon.swag.ComponentInfoMBean;
import icons.IdeaSwagIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwagRootsTab extends AdditionalTabComponent {

    private final JBList<RunningComponent> rootsList;
    private final DefaultListModel<RunningComponent> roots = new DefaultListModel<>();

    public SwagRootsTab(CompletableFuture<ComponentInfoMBean> infoBean) {
        infoBean.thenAcceptAsync(this::init, ApplicationManager.getApplication()::invokeLater);
        setLayout(new BorderLayout());
        rootsList = createList();
        add(new JBLabel("Roots", ComponentStyle.LARGE), BorderLayout.NORTH);
        add(new JBScrollPane(rootsList), BorderLayout.CENTER);
    }

    private void init(ComponentInfoMBean infoBean) {
        roots.addAll(RunningComponent.getRoots(infoBean));
        rootsList.setPaintBusy(false);
    }

    private JBList<RunningComponent> createList() {
        JBList<RunningComponent> list = new JBList<>(roots);
        list.setCellRenderer(new Renderer());
        list.setPaintBusy(true);
        return list;
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
    public void dispose() {}

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
            setText(Util.generateTitle(component.getDescription()));
            setIcon(IdeaSwagIcons.Window);
            return this;
        }
    }
}
