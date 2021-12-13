package com.github.ohle.ideaswag;

import java.io.IOException;

import java.net.ServerSocket;

import java.nio.file.Path;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwagConfiguration extends ApplicationConfiguration {

    protected SwagConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory) {
        super("Swag Application", project, factory);
    }

    @Override
    protected @NotNull SwagConfigurationOptions getOptions() {
        return (SwagConfigurationOptions) super.getOptions();
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        super.checkConfiguration();
        if (!getOptions().getFoo().equals("foo")) {
            throw new RuntimeConfigurationError("The foo was frobnicated!");
        }
        JavaRunConfigurationExtensionManager.checkConfigurationIsValid(this);
    }

    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env)
            throws ExecutionException {
        final JavaCommandLineState state =
                new JavaApplicationCommandLineState<>(this, env) {
                    @Override
                    protected JavaParameters createJavaParameters() throws ExecutionException {
                        JavaParameters parameters = super.createJavaParameters();
                        ParametersList vmOptions = parameters.getVMParametersList();
                        int port = findFreePort();
                        vmOptions.add("-Dcom.sun.management.jmxremote.port=" + port);
                        vmOptions.add("-Dcom.sun.management.jmxremote.authenticate=false");
                        vmOptions.add("-Dcom.sun.management.jmxremote.ssl=false");
                        String agentJar = findAgentJar();
                        vmOptions.add("-javaagent:" + agentJar + "=" + agentJar);
                        return parameters;
                    }
                };
        state.setConsoleBuilder(
                TextConsoleBuilderFactory.getInstance()
                        .createBuilder(getProject(), getConfigurationModule().getSearchScope()));
        return state;
    }

    private String findAgentJar() {
        Path agentJarPath =
                Path.of(
                        System.getProperty("idea.plugins.path"),
                        "idea-swag",
                        "lib",
                        "swag-1.0.jar");
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

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new SwagConfigurable(getProject());
    }

    public void setFoo(String foo_) {
        getOptions().setFoo(foo_);
    }

    public String getFoo() {
        return getOptions().getFoo();
    }

    public static class Factory extends ConfigurationFactory {
        public Factory(@NotNull ConfigurationType type) {
            super(type);
        }

        @Override
        public @Nullable Class<? extends BaseState> getOptionsClass() {
            return SwagConfigurationOptions.class;
        }

        @Override
        public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
            return new SwagConfiguration(project, this);
        }
    }
}
