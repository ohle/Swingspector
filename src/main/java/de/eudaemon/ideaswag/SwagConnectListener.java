package de.eudaemon.ideaswag;

import java.util.concurrent.CompletableFuture;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Timer;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
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

    private final AtomicInteger tries = new AtomicInteger(0);
    private final Timer retryTimer = new Timer(500, a -> tryToConnect());
    private Disposable disposer;

    public SwagConnectListener(
            int port_, Project project_, CompletableFuture<ComponentInfoMBean> infoBeanFuture_) {
        port = port_;
        project = project_;
        infoBeanFuture = infoBeanFuture_;
    }

    @Override
    public void startNotified(@NotNull ProcessEvent event) {
        disposer = event.getProcessHandler().getUserData(SwingspectorExtension.PROCESS_DISPOSER);
        retryTimer.start();
    }

    private void tryToConnect() {
        if (tries.getAndIncrement() > 10) {
            retryTimer.stop();
            LOG.error("Failed to connect");
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
