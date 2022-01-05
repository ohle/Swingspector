package de.eudaemon.ideaswag;

import javax.management.Notification;
import javax.management.NotificationListener;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

import de.eudaemon.swag.ComponentInfoMBean;

public class SwagHotkeyListener implements Disposable {

    private final ComponentInfoMBean componentInfo;
    private final Listener listener;
    private final Project project;

    private final Disposable disposer;

    public SwagHotkeyListener(
            ComponentInfoMBean componentInfo_, Project project_, Disposable disposer_) {
        componentInfo = componentInfo_;
        disposer = disposer_;
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
        }

        private void openToolWindow(int id) {
            Util.openComponentTab(new RunningComponent(componentInfo, id, project), disposer);
        }
    }
}
