package com.github.ohle.ideaswag;

import java.util.Optional;

import java.util.concurrent.CompletableFuture;

import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.icons.AllIcons.Hierarchy;
import com.intellij.openapi.project.Project;
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
            Key.create("com.github.ohle.ideaswag.info-bean");

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

    public static Content openTreeTab(RunningComponent component) {
        if (treeToolWindow == null) {
            registerTreeToolWindow(component.getProject());
        }
        ContentManager contentManager = treeToolWindow.getContentManager();
        Optional<Content> existingTab = getExistingTab(contentManager, component);
        if (existingTab.isPresent()) {
            treeToolWindow.activate(() -> {});
            return existingTab.get();
        } else {
            String tabId = String.valueOf(component);
            String title = generateTitle(component.getDescription());
            Content tab =
                    contentManager
                            .getFactory()
                            .createContent(new TreeViewPanel(component), title, true);
            tab.setTabName(tabId);
            contentManager.addContent(tab);
            contentManager.setSelectedContent(tab);
            treeToolWindow.activate(() -> {});
            return tab;
        }
    }

    public static Content openComponentTab(RunningComponent component) {
        if (componentToolWindow == null) {
            registerComponentToolWindow(component.getProject());
        }
        ContentManager contentManager = componentToolWindow.getContentManager();
        Optional<Content> existingTab = getExistingTab(contentManager, component);
        if (existingTab.isPresent()) {
            componentToolWindow.activate(() -> {});
            return existingTab.get();
        } else {
            String tabId = String.valueOf(component.getId());
            ComponentInfoPanel infoPanel = new ComponentInfoPanel(component);
            Content tab =
                    contentManager
                            .getFactory()
                            .createContent(infoPanel, infoPanel.getTitle(), true);
            tab.setTabName(tabId);
            contentManager.addContent(tab);
            contentManager.setSelectedContent(tab);
            componentToolWindow.activate(() -> {});
            return tab;
        }
    }

    private static Optional<Content> getExistingTab(
            ContentManager manager, RunningComponent component) {
        String tabId = String.valueOf(component.getId());
        for (int i = 0; i < manager.getContentCount(); i++) {
            Content tab = manager.getContent(i);
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

    public static void removeComponentTab(Content tab) {
        componentToolWindow.getContentManager().removeContent(tab, true);
    }

    public static void removeTreeTab(Content tab) {
        treeToolWindow.getContentManager().removeContent(tab, true);
    }
}
