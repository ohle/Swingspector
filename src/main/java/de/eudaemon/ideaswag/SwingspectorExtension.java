package de.eudaemon.ideaswag;

import java.util.List;
import java.util.Objects;

import java.util.concurrent.CompletableFuture;

import java.io.IOException;

import java.nio.file.Path;

import javax.swing.KeyStroke;

import com.intellij.execution.ExecutionException;
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
import com.intellij.openapi.util.NlsContexts.TabTitle;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.net.NetUtils;

import de.eudaemon.swag.ComponentInfoMBean;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwingspectorExtension extends RunConfigurationExtension {

    private final int port;
    private final String agentJar;

    public SwingspectorExtension() {
        port = findFreePort();
        agentJar = findAgentJar();
    }

    @Override
    public <T extends RunConfigurationBase<?>> void updateJavaParameters(@NotNull T configuration,
            @NotNull JavaParameters params, @Nullable RunnerSettings runnerSettings) {
        if (!isApplicableFor(configuration)
                || !isActive(configuration)) {
            return;
        }
        KeyStroke hotKey =
                configuration.getCopyableUserData(SwingspectorSettingsEditorFragment.HOTKEY);
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
                || !isActive(configuration)) {
            return;
        }
        CompletableFuture<ComponentInfoMBean> infoBeanFuture = new CompletableFuture<>();
        Double timeout =
                configuration.getCopyableUserData(SwingspectorSettingsEditorFragment.TIMEOUT);
        Disposable disposer = createProcessDisposer();
        handler.putUserData(Util.INFO_BEAN_KEY, infoBeanFuture);
        handler.addProcessListener(
                new SwagConnectListener(
                        port, configuration.getProject(), infoBeanFuture, disposer, timeout));
        ApplicationManager.getApplication()
                .invokeLater(() -> openRootsWindow(configuration.getProject(), handler, disposer));
    }

    private void openRootsWindow(Project project, ProcessHandler handler, Disposable disposer) {
        ToolWindow componentToolWindow =
                ToolWindowManager.getInstance(project).getToolWindow("Swing Roots");
        ContentManager contentManager = componentToolWindow.getContentManager();
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
        return true;
    }

    @Override
    protected List<SettingsEditorFragment> createFragments(
            @NotNull RunConfigurationBase configuration) {
        return List.of(
                new SettingsEditorFragment<>(
                        "swingspector",
                        "Swingspector",
                        "Swing",
                        new SwingspectorSettingsEditorFragment(configuration.getProject()),
                        (o, c) -> c.resetFrom(o),
                        (o, c) -> c.applyTo(o),
                        SwingspectorExtension::isActive));
    }

    @Override
    protected @Nullable @TabTitle String getEditorTitle() {
        return "Swingspector";
    }

    @Override
    protected void writeExternal(
            @NotNull RunConfigurationBase<?> runConfiguration, @NotNull Element element) {
        super.writeExternal(runConfiguration, element);
        if (!isApplicableFor(runConfiguration)) {
            return;
        }
        KeyStroke key =
                runConfiguration.getCopyableUserData(SwingspectorSettingsEditorFragment.HOTKEY);

        if (key == null) {
            return;
        }
        Element swingSpector = new Element("Swingspector");
        Element keyStroke = new Element("keystroke");
        keyStroke.setAttribute("code", String.valueOf(key.getKeyCode()));
        keyStroke.setAttribute("modifiers", String.valueOf(key.getModifiers()));
        swingSpector.addContent(keyStroke);
        Element active = new Element("active");
        active.setAttribute(
                "value", String.valueOf(isActive(runConfiguration)));
        swingSpector.addContent(active);
        Element timeout = new Element("timeout");
        timeout.addContent(
                String.valueOf(
                        runConfiguration.getCopyableUserData(
                                SwingspectorSettingsEditorFragment.TIMEOUT)));
        swingSpector.addContent(timeout);
        element.addContent(swingSpector);
    }

    @Override
    protected void readExternal(
            @NotNull RunConfigurationBase<?> runConfiguration, @NotNull Element element) {
        super.readExternal(runConfiguration, element);
        if (!isApplicableFor(runConfiguration)) {
            return;
        }
        Element swingSpector = element.getChild("Swingspector");
        if (swingSpector == null) {
            return;
        }
        Element keystroke = swingSpector.getChild("keystroke");
        KeyStroke keyStroke =
                KeyStroke.getKeyStroke(
                        Integer.parseInt(keystroke.getAttributeValue("code")),
                        Integer.parseInt(keystroke.getAttributeValue("modifiers")));
        boolean active =
                Boolean.parseBoolean(swingSpector.getChild("active").getAttributeValue("value"));
        runConfiguration.putCopyableUserData(SwingspectorSettingsEditorFragment.ACTIVE_KEY, active);
        runConfiguration.putCopyableUserData(SwingspectorSettingsEditorFragment.HOTKEY, keyStroke);
        double timeout;
        if (swingSpector.getChildText("timeout") != null) {
            timeout = Double.parseDouble(swingSpector.getChildText("timeout"));
        } else {
            timeout = 5.0;
        }
        runConfiguration.putCopyableUserData(SwingspectorSettingsEditorFragment.TIMEOUT, timeout);
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
                Path.of(PathManager.getPluginsPath(), "Swingspector", "lib", "swag-1.2.3.jar");
        return agentJarPath.toFile().getAbsolutePath();
    }

    private static boolean isActive(RunConfigurationBase<?> config) {
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
