package de.eudaemon.ideaswag;

import java.util.concurrent.CompletableFuture;

import java.util.concurrent.atomic.AtomicInteger;

import java.io.IOException;

import java.nio.file.Path;

import javax.swing.KeyStroke;
import javax.swing.Timer;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.application.ApplicationConfiguration.JavaApplicationCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.net.NetUtils;

import de.eudaemon.swag.ComponentInfoMBean;
import org.jetbrains.annotations.NotNull;

public class SwagApplicationCommandLineState
        extends JavaApplicationCommandLineState<SwagConfiguration> {

    private static final Logger LOG = Logger.getInstance(SwagApplicationCommandLineState.class);
    private final int port;
    private final String agentJar;
    private final KeyStroke hotKey;

    public SwagApplicationCommandLineState(
            @NotNull SwagConfiguration configuration, ExecutionEnvironment environment) {
        super(configuration, environment);
        port = findFreePort();
        agentJar = findAgentJar();
        hotKey = configuration.getKeyStroke();
    }

    @Override
    protected @NotNull OSProcessHandler startProcess() throws ExecutionException {
        OSProcessHandler processHandler = super.startProcess();
        CompletableFuture<ComponentInfoMBean> infoBeanFuture = new CompletableFuture<>();
        processHandler.putUserData(Util.INFO_BEAN_KEY, infoBeanFuture);
        processHandler.addProcessListener(
                new ConnectListener(port, getConfiguration().getProject(), infoBeanFuture));
        return processHandler;
    }

    @Override
    protected JavaParameters createJavaParameters() throws ExecutionException {
        JavaParameters parameters = super.createJavaParameters();
        ParametersList vmOptions = parameters.getVMParametersList();
        vmOptions.add("-Dcom.sun.management.jmxremote.port=" + port);
        vmOptions.add("-Dcom.sun.management.jmxremote.authenticate=false");
        vmOptions.add("-Dcom.sun.management.jmxremote.ssl=false");
        vmOptions.add(
                "-javaagent:"
                        + agentJar
                        + "=agentJar:"
                        + agentJar
                        + ",keyCode:"
                        + hotKey.getKeyCode()
                        + ",modifiers:"
                        + hotKey.getModifiers());
        return parameters;
    }

    private String findAgentJar() {
        Path agentJarPath =
                Path.of(PathManager.getPluginsPath(), "idea-swag", "lib", "swag-1.2.jar");
        return agentJarPath.toFile().getAbsolutePath();
    }

    private int findFreePort() {
        try {
            return NetUtils.findAvailableSocketPort();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't find a free port", e);
        }
    }

    private static class ConnectListener extends ProcessAdapter {
        private final int port;
        private final Project project;
        private final CompletableFuture<ComponentInfoMBean> infoBeanFuture;
        boolean cancelled = false;
        boolean connected = false;

        private final AtomicInteger tries = new AtomicInteger(0);
        private final Timer retryTimer = new Timer(500, a -> tryToConect());

        public ConnectListener(
                int port_,
                Project project_,
                CompletableFuture<ComponentInfoMBean> infoBeanFuture_) {
            port = port_;
            project = project_;
            infoBeanFuture = infoBeanFuture_;
        }

        @Override
        public void startNotified(@NotNull ProcessEvent event) {
            retryTimer.start();
        }

        private void tryToConect() {
            if (tries.getAndIncrement() > 10) {
                retryTimer.stop();
                LOG.error("Failed to connect");
            }
            if (!cancelled && !connected) {
                ComponentInfoMBean infoBean =
                        SwagNotificationHandler.getInstance().startListeningTo(port, project);
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
        }
    }
}
