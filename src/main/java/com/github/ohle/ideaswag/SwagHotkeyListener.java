package com.github.ohle.ideaswag;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationListener;

import com.intellij.openapi.Disposable;

import de.eudaemon.swag.ComponentInfoMBean;

public class SwagHotkeyListener implements Disposable {

    private final ComponentInfoMBean componentInfo;
    private final Listener listener;

    public SwagHotkeyListener(ComponentInfoMBean componentInfo_) {
        componentInfo = componentInfo_;
        listener = new Listener();
        componentInfo.addNotificationListener(listener, null, null);
    }

    @Override
    public void dispose() {
        try {
            componentInfo.removeNotificationListener(listener);
        } catch (ListenerNotFoundException ignored) {
        }
    }

    private class Listener implements NotificationListener {
        @Override
        public void handleNotification(Notification notification, Object handback) {
            int id = (Integer) notification.getUserData();
            System.out.println(componentInfo.getPlacementInfo(id));
        }
    }
}
