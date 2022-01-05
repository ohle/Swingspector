package de.eudaemon.ideaswag;

import java.util.Objects;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.AdditionalTabComponentManager;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.KeyWithDefaultValue;

import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwagConfiguration extends ApplicationConfiguration {

    public static Key<Disposable> PROCESS_DISPOSER =
            KeyWithDefaultValue.create("swag-process-disposer", Disposer::newDisposable);

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
        if (getKeyStroke().getKeyCode() == KeyEvent.VK_UNDEFINED) {
            throw new RuntimeConfigurationError("Keyboard Shortcut must be set.");
        }
        JavaRunConfigurationExtensionManager.checkConfigurationIsValid(this);
    }

    @Override
    public void createAdditionalTabComponents(
            AdditionalTabComponentManager manager, ProcessHandler startedProcess) {
        Disposable disposer = startedProcess.getUserData(PROCESS_DISPOSER);
        SwagRootsTab rootsTab =
                new SwagRootsTab(
                        Objects.requireNonNull(startedProcess.getUserData(Util.INFO_BEAN_KEY)),
                        getProject(),
                        disposer);
        startedProcess.addProcessListener(
                new ProcessAdapter() {
                    @Override
                    public void processTerminated(@NotNull ProcessEvent event) {
                        ApplicationManager.getApplication()
                                .invokeLater(
                                        () -> {
                                            rootsTab.dispose();
                                            manager.removeAdditionalTabComponent(rootsTab);
                                        });
                    }
                });
        manager.addAdditionalTabComponent(rootsTab, "SwagTab");
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

    @Override
    public void writeExternal(@NotNull Element element) {
        super.writeExternal(element);
        // Apparently, SwagConfigurationOptions' StoredProperties aren't persisted automatically
        Element keyStroke = new Element("keyStroke");
        KeyStroke ks = getOptions().getKeyStroke();
        keyStroke.setAttribute("code", String.valueOf(ks.getKeyCode()));
        keyStroke.setAttribute("modifiers", String.valueOf(ks.getModifiers()));
        element.addContent(keyStroke);
    }

    @Override
    public void readExternal(@NotNull Element element) {
        super.readExternal(element);
        Element keyStroke = element.getChild("keyStroke");
        if (keyStroke == null) {
            return;
        }
        int code = Integer.parseInt(keyStroke.getAttributeValue("code"));
        int mods = Integer.parseInt(keyStroke.getAttributeValue("modifiers"));
        //noinspection MagicConstant
        setKeyStroke(KeyStroke.getKeyStroke(code, mods));
    }

    public KeyStroke getKeyStroke() {
        return getOptions().getKeyStroke();
    }

    public void setKeyStroke(KeyStroke keyStroke) {
        getOptions().setKeyStroke(keyStroke);
    }

    public static class Factory extends ConfigurationFactory {
        public Factory(@NotNull ConfigurationType type) {
            super(type);
        }

        @Override
        public @NotNull @NonNls String getId() {
            return "de.eudaemon.ideaswag";
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
