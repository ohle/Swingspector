package de.eudaemon.ideaswag;

import java.util.List;
import java.util.Objects;

import java.util.concurrent.CompletableFuture;

import java.io.IOException;

import java.nio.file.Path;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.SettingsEditorFragment;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.KeyWithDefaultValue;
import com.intellij.openapi.util.NlsContexts.TabTitle;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.net.NetUtils;

import de.eudaemon.swag.ComponentInfoMBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwingspectorExtension extends RunConfigurationExtension {

    public static Key<Disposable> PROCESS_DISPOSER =
            KeyWithDefaultValue.create("swag-process-disposer", createProcessDisposer());

    private final int port;
    private final String agentJar;
    private final KeyStroke hotKey = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);

    public SwingspectorExtension() {
        port = findFreePort();
        agentJar = findAgentJar();
    }

    @Override
    public <T extends RunConfigurationBase> void updateJavaParameters(
            @NotNull T configuration,
            @NotNull JavaParameters params,
            RunnerSettings runnerSettings) {
        if (!isApplicableFor(configuration)
                || !isActive((ApplicationConfiguration) configuration)) {
            return;
        }
        ParametersList vmOptions = params.getVMParametersList();
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
    }

    @Override
    protected void attachToProcess(
            @NotNull RunConfigurationBase<?> configuration,
            @NotNull ProcessHandler handler,
            @Nullable RunnerSettings runnerSettings) {
        if (!isApplicableFor(configuration)
                || !isActive((ApplicationConfiguration) configuration)) {
            return;
        }
        CompletableFuture<ComponentInfoMBean> infoBeanFuture = new CompletableFuture<>();
        handler.putUserData(Util.INFO_BEAN_KEY, infoBeanFuture);
        handler.addProcessListener(
                new SwagConnectListener(port, configuration.getProject(), infoBeanFuture));
        openRootsWindow(configuration.getProject(), handler);
    }

    private void openRootsWindow(Project project, ProcessHandler handler) {
        ToolWindow componentToolWindow =
                ToolWindowManager.getInstance(project).getToolWindow("Swing Roots");
        ContentManager contentManager = componentToolWindow.getContentManager();
        Disposable disposer = handler.getUserData(PROCESS_DISPOSER);
        SwingRoots roots =
                new SwingRoots(
                        Objects.requireNonNull(handler.getUserData(Util.INFO_BEAN_KEY)),
                        project,
                        disposer);
        Content swingRoots = contentManager.getFactory().createContent(roots, "Swing Roots", false);

        contentManager.removeAllContents(true);
        contentManager.addContent(swingRoots);
    }

    @Override
    public boolean isApplicableFor(@NotNull RunConfigurationBase<?> configuration) {
        return configuration instanceof ApplicationConfiguration;
    }

    @Override
    protected List<SettingsEditorFragment> createFragments(
            @NotNull RunConfigurationBase configuration) {
        return List.of(
                new SettingsEditorFragment<>(
                        "swingspector",
                        "Swingspector",
                        "Swing",
                        new SwingspectorSettingsEditorFragment(),
                        (o, c) -> c.resetFrom(o),
                        (o, c) -> c.applyTo(o),
                        SwingspectorExtension::isActive));
    }

    @Override
    protected @Nullable @TabTitle String getEditorTitle() {
        return "Swingspector";
    }

    private int findFreePort() {
        try {
            return NetUtils.findAvailableSocketPort();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't find a free port", e);
        }
    }

    private String findAgentJar() {
        Path agentJarPath =
                Path.of(PathManager.getPluginsPath(), "Swingspector", "lib", "swag-1.2.1.jar");
        return agentJarPath.toFile().getAbsolutePath();
    }

    private static boolean isActive(ApplicationConfiguration config) {
        Boolean active = config.getCopyableUserData(SwingspectorSettingsEditorFragment.ACTIVE_KEY);
        return active != null && active;
    }

    private static Disposable createProcessDisposer() {
        Disposable disposable = Disposer.newDisposable();
        Disposer.register(
                ApplicationManager.getApplication().getService(SwagApplicationService.class),
                disposable);
        return disposable;
    }
}
