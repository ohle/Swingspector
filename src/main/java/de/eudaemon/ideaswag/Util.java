package de.eudaemon.ideaswag;

import java.util.Objects;
import java.util.Optional;

import java.util.concurrent.CompletableFuture;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.Hierarchy;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

import de.eudaemon.swag.ComponentDescription;
import de.eudaemon.swag.ComponentInfoMBean;

public class Util {
    private static ToolWindow componentToolWindow = null;
    private static ToolWindow treeToolWindow = null;
    public static final Key<CompletableFuture<ComponentInfoMBean>> INFO_BEAN_KEY =
            Key.create("de.eudaemon.ideaswag.info-bean");

    public static String generateTitle(ComponentDescription description) {
        StringBuilder sb = new StringBuilder();
        boolean hasPrefix = false;
        if (description.name != null) {
            sb.append(description.name).append(" (");
            hasPrefix = true;
        } else if (description.text != null) {
            sb.append(StringUtils.abbreviate(description.text, 15)).append(" (");
            hasPrefix = true;
        }
        sb.append(description.simpleClassName);
        if (hasPrefix) {
            sb.append((")"));
        }
        return sb.toString();
    }

    public static Optional<RunningComponent> getOpenComponentTab() {
        return Optional.ofNullable(componentToolWindow.getContentManager().getSelectedContent())
                .map(Content::getComponent)
                .map(ComponentInfoPanel.class::cast)
                .map(ComponentInfoPanel::getRunningComponent);
    }

    public static Optional<TreeViewPanel> getOpenTreeTab() {
        return Optional.ofNullable(treeToolWindow.getContentManager().getSelectedContent())
                .map(Content::getComponent)
                .map(TreeViewPanel.class::cast);
    }

    public static Content openTreeTab(RunningComponent component, Disposable disposer) {
        if (treeToolWindow == null) {
            registerTreeToolWindow(component.getProject());
        }
        ContentManager contentManager = treeToolWindow.getContentManager();
        Optional<Content> existingTab = getExistingTab(contentManager, component);
        if (existingTab.isPresent()) {
            treeToolWindow.activate(() -> {});
            return existingTab.get();
        } else {
            Content tab =
                    createOrReplaceTab(
                            component,
                            contentManager,
                            new TreeViewPanel(component, disposer),
                            disposer);
            treeToolWindow.activate(() -> {});
            return tab;
        }
    }

    public static Content openComponentTab(RunningComponent component, Disposable parentDisposer) {
        return openComponentTab(component, () -> {}, parentDisposer);
    }

    public static Content openComponentTab(
            RunningComponent component, Runnable runnable, Disposable parentDisposer) {
        if (componentToolWindow == null) {
            registerComponentToolWindow(component.getProject());
        }
        ContentManager contentManager = componentToolWindow.getContentManager();
        Optional<Content> existingTab = getExistingTab(contentManager, component);
        if (existingTab.isPresent()) {
            componentToolWindow.activate(runnable);
            return existingTab.get();
        } else {
            Content tab =
                    createOrReplaceTab(
                            component,
                            contentManager,
                            new ComponentInfoPanel(component, parentDisposer),
                            parentDisposer);
            componentToolWindow.activate(runnable);
            return tab;
        }
    }

    private static Content createOrReplaceTab(
            RunningComponent component,
            ContentManager contentManager,
            JComponent content,
            Disposable parentDisposer) {
        Content newContent =
                contentManager
                        .getFactory()
                        .createContent(content, generateTitle(component.getDescription()), true);
        Disposer.register(parentDisposer, new ToolWindowDisposer(contentManager, newContent));
        newContent.setTabName(String.valueOf(component.getId()));
        boolean foundExisting = false;
        for (int i = 0; i < contentManager.getContentCount(); i++) {
            Content tab = Objects.requireNonNull(contentManager.getContent(i));
            if (!tab.isPinned()) {
                contentManager.removeContent(tab, true);
                contentManager.addContent(newContent, i);
                foundExisting = true;
                break;
            }
        }
        if (!foundExisting) {
            contentManager.addContent(newContent);
        }
        contentManager.setSelectedContent(newContent);
        return newContent;
    }

    private static Optional<Content> getExistingTab(
            ContentManager manager, RunningComponent component) {
        String tabId = String.valueOf(component.getId());
        for (int i = 0; i < manager.getContentCount(); i++) {
            Content tab = Objects.requireNonNull(manager.getContent(i));
            if (tabId.equals(tab.getTabName())) {
                manager.setSelectedContent(tab);
                return Optional.of(tab);
            }
        }
        return Optional.empty();
    }

    private static void registerComponentToolWindow(Project project) {
        componentToolWindow =
                ToolWindowManager.getInstance(project)
                        .registerToolWindow(
                                new RegisterToolWindowTask(
                                        "Swing Components",
                                        ToolWindowAnchor.BOTTOM,
                                        new JPanel(),
                                        false,
                                        true,
                                        true,
                                        true,
                                        (project1, toolWindow1) -> {},
                                        Actions.Search,
                                        () -> "Swing Components"));
    }

    private static void registerTreeToolWindow(Project project) {
        treeToolWindow =
                ToolWindowManager.getInstance(project)
                        .registerToolWindow(
                                new RegisterToolWindowTask(
                                        "Swing Hierarchy",
                                        ToolWindowAnchor.LEFT,
                                        new JPanel(),
                                        true,
                                        true,
                                        true,
                                        true,
                                        (project1, toolWindow) -> {},
                                        Hierarchy.Class,
                                        () -> "Swing Hierarchy"));
    }

    private static final class ToolWindowDisposer implements Disposable {
        private final ContentManager contentManager;
        private final Content content;

        private ToolWindowDisposer(ContentManager contentManager_, Content content_) {
            contentManager = contentManager_;
            content = content_;
        }

        @Override
        public void dispose() {
            contentManager.removeContent(content, true);
        }
    }
}
