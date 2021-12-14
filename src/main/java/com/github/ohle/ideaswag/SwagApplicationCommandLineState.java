package com.github.ohle.ideaswag;

import java.util.concurrent.atomic.AtomicInteger;

import java.io.IOException;

import java.net.ServerSocket;

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
import com.intellij.openapi.diagnostic.Logger;

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
        processHandler.addProcessListener(
                new ProcessAdapter() {
                    boolean cancelled = false;
                    boolean connected = false;

                    private final AtomicInteger tries = new AtomicInteger(0);
                    private final Timer retryTimer = new Timer(500, a -> tryToConect());

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
                            connected =
                                    SwagNotificationHandler.getInstance()
                                            .startListeningTo(
                                                    port, getConfiguration().getProject());
                            if (connected) {
                                retryTimer.stop();
                            }
                        }
                    }

                    @Override
                    public void processTerminated(@NotNull ProcessEvent event) {
                        cancelled = true;
                        SwagNotificationHandler.getInstance().cleanup(port);
                    }
                });
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
                Path.of(
                        System.getProperty("idea.plugins.path"),
                        "idea-swag",
                        "lib",
                        "swag-1.1.jar");
        return agentJarPath.toFile().getAbsolutePath();
    }

    private int findFreePort() {
        try {
            final ServerSocket socket = new ServerSocket(0);
            try {
                return socket.getLocalPort();
            } finally {
                synchronized (socket) {
                    try {
                        socket.wait(1);
                    } catch (Throwable ignored) {
                    }
                }
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't find a free port", e);
        }
    }
}
