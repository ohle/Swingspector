package de.eudaemon.ideaswag;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Timer;

import com.intellij.execution.impl.EditConfigurationsDialog;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import de.eudaemon.swag.ComponentInfoMBean;
import org.jetbrains.annotations.NotNull;

class SwagConnectListener extends ProcessAdapter {

    private static final Logger LOG = Logger.getInstance(SwagConnectListener.class);
    private final int port;
    private final Project project;
    private final CompletableFuture<ComponentInfoMBean> infoBeanFuture;
    boolean cancelled = false;
    boolean connected = false;

    private static final int RETRY_DELAY_MS = 500;
    private final AtomicInteger tries = new AtomicInteger(0);
    private final Timer retryTimer = new Timer(RETRY_DELAY_MS, a -> tryToConnect());
    private Disposable disposer;
    private final int maxRetries;

    public SwagConnectListener(
            int port_,
            Project project_,
            CompletableFuture<ComponentInfoMBean> infoBeanFuture_,
            Disposable disposer_,
            double timeout) {
        port = port_;
        project = project_;
        infoBeanFuture = infoBeanFuture_;
        disposer = disposer_;
        maxRetries = (int) (timeout * 1.0e3 / RETRY_DELAY_MS);
    }

    @Override
    public void startNotified(@NotNull ProcessEvent event) {
        retryTimer.start();
    }

    private void tryToConnect() {
        if (tries.getAndIncrement() > maxRetries) {
            retryTimer.stop();
            Notification errorNotification =
                    new Notification(
                            "Swingspector Connection Errors",
                            "Failed " + "to connect",
                            "Timeout trying to connect to Swing Agent",
                            NotificationType.ERROR);
            errorNotification.addAction(
                    NotificationAction.createSimple(
                            "Edit Settings", () -> new EditConfigurationsDialog(project).show()));
            Notifications.Bus.notify(errorNotification, project);
            LOG.info("Failed to connect", new TimeoutException());
        }
        if (!cancelled && !connected) {
            ComponentInfoMBean infoBean =
                    SwagNotificationHandler.getInstance().startListeningTo(port, project, disposer);
            connected = infoBean != null;
            if (connected) {
                retryTimer.stop();
                infoBeanFuture.complete(infoBean);
            }
        }
    }

    @Override
    public void processTerminated(@NotNull ProcessEvent event) {
        cancelled = true;
        SwagNotificationHandler.getInstance().cleanup(port);
        ApplicationManager.getApplication().invokeLater(() -> Disposer.dispose(disposer));
    }
}
