package com.github.ohle.ideaswag;

import javax.management.Notification;
import javax.management.NotificationListener;

import javax.swing.JPanel;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
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

    public SwagHotkeyListener(ComponentInfoMBean componentInfo_, Project project_) {
        componentInfo = componentInfo_;
        listener = new Listener();
        project = project_;
        componentInfo.addNotificationListener(listener, null, null);
    }

    @Override
    public void dispose() {
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
            System.out.println(componentInfo.getPlacementInfo(id));
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
            Content tab =
                    contentManager
                            .getFactory()
                            .createContent(
                                    new ComponentInfoPanel(componentInfo, id),
                                    generateTabName(description),
                                    true);
            tab.setTabName(tabId);
            contentManager.addContent(tab);
            contentManager.addSelectedContent(tab);
            toolWindow.activate(() -> {});
        }

        private void registerToolWindow() {
            ToolWindowFactory factory =
                    (project, toolWindow) -> System.out.println("Listener.createToolWindowContent");
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
                                            factory,
                                            Actions.Search,
                                            () -> "Swing Components"));
        }
    }

    private String generateTabName(ComponentDescription description) {
        StringBuilder sb = new StringBuilder();
        if (description.name != null) {
            sb.append(description.name).append(" (");
        }
        sb.append(description.simpleClassName);
        if (description.name != null) {
            sb.append((")"));
        }
        return sb.toString();
    }
}
