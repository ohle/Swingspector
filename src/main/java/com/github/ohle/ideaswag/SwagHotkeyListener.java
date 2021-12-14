package com.github.ohle.ideaswag;

import java.util.HashSet;
import java.util.Set;

import javax.management.Notification;
import javax.management.NotificationListener;

import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

import de.eudaemon.swag.ComponentDescription;
import de.eudaemon.swag.ComponentInfoMBean;

public class SwagHotkeyListener implements Disposable {

    private final ComponentInfoMBean componentInfo;
    private final Listener listener;
    private final Project project;

    private static ToolWindow toolWindow = null;

    private final Set<Content> openedTabs = new HashSet<>();

    public SwagHotkeyListener(ComponentInfoMBean componentInfo_, Project project_) {
        componentInfo = componentInfo_;
        listener = new Listener();
        project = project_;
        componentInfo.addNotificationListener(listener, null, null);
    }

    @Override
    public void dispose() {
        ApplicationManager.getApplication()
                .invokeLater(
                        () -> {
                            for (Content openedTab : openedTabs) {
                                toolWindow.getContentManager().removeContent(openedTab, true);
                            }
                        });
        try {
            componentInfo.removeNotificationListener(listener);
        } catch (Throwable ignored) {
        }
    }

    private class Listener implements NotificationListener {
        @Override
        public void handleNotification(Notification notification, Object handback) {
            int id = (Integer) notification.getUserData();
            ApplicationManager.getApplication().invokeLater(() -> openToolWindow(id));
        }

        private void openToolWindow(int id) {
            if (toolWindow == null) {
                registerToolWindow();
            }
            ContentManager contentManager = toolWindow.getContentManager();
            String tabId = String.valueOf(id);
            for (int i = 0; i < contentManager.getContentCount(); i++) {
                Content tab = contentManager.getContent(i);
                if (tabId.equals(tab.getTabName())) {
                    contentManager.setSelectedContent(tab);
                    toolWindow.activate(() -> {});
                    return;
                }
            }
            ComponentDescription description = componentInfo.getDescription(id);
            String title = generateTabTitle(description);
            Content tab =
                    contentManager
                            .getFactory()
                            .createContent(
                                    new ComponentInfoPanel(project, componentInfo, title, id),
                                    title,
                                    true);
            tab.setTabName(tabId);
            contentManager.addContent(tab);
            openedTabs.add(tab);
            contentManager.addSelectedContent(tab);
            toolWindow.activate(() -> {});
        }

        private void registerToolWindow() {
            toolWindow =
                    ToolWindowManager.getInstance(project)
                            .registerToolWindow(
                                    new RegisterToolWindowTask(
                                            "Swing Components",
                                            ToolWindowAnchor.BOTTOM,
                                            new JPanel(),
                                            false,
                                            true,
                                            true,
                                            true,
                                            (project1, toolWindow1) -> {},
                                            Actions.Search,
                                            () -> "Swing Components"));
        }
    }

    private String generateTabTitle(ComponentDescription description) {
        StringBuilder sb = new StringBuilder();
        boolean hasPrefix = false;
        if (description.name != null) {
            sb.append(description.name).append(" (");
            hasPrefix = true;
        } else if (description.text != null) {
            sb.append(StringUtils.abbreviate(description.text, 15)).append(" (");
            hasPrefix = true;
        }
        sb.append(description.simpleClassName);
        if (hasPrefix) {
            sb.append((")"));
        }
        return sb.toString();
    }
}
