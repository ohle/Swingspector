package com.github.ohle.ideaswag;

import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NonNls;
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
        if (!"foo".equals(getOptions().getFoo())) {
            throw new RuntimeConfigurationError("The foo was frobnicated!");
        }
        JavaRunConfigurationExtensionManager.checkConfigurationIsValid(this);
    }

    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
        final JavaCommandLineState state = new SwagApplicationCommandLineState(this, env);
        state.setConsoleBuilder(
                TextConsoleBuilderFactory.getInstance()
                        .createBuilder(getProject(), getConfigurationModule().getSearchScope()));
        return state;
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
        public @NotNull @NonNls String getId() {
            return "com.github.ohle.ideaswag";
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
