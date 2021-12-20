package com.github.ohle.ideaswag;

import java.util.HashSet;
import java.util.Set;

import javax.management.Notification;
import javax.management.NotificationListener;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;

import de.eudaemon.swag.ComponentInfoMBean;

public class SwagHotkeyListener implements Disposable {

    private final ComponentInfoMBean componentInfo;
    private final Listener listener;
    private final Project project;

    private static final Set<Content> openedTabs = new HashSet<>();

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
                                Util.removeComponentTab(openedTab);
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
            openedTabs.add(Util.openComponentTab(new RunningComponent(componentInfo, id, project)));
        }
    }
}
