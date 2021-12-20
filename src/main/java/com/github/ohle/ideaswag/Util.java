package com.github.ohle.ideaswag;

import java.util.concurrent.CompletableFuture;

import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import com.intellij.icons.AllIcons.Actions;
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

    public static void openTreeTab(RunningComponent component) {
        throw new UnsupportedOperationException("not implemented!");
    }

    public static Content openComponentTab(RunningComponent component) {
        if (componentToolWindow == null) {
            registerComponentToolWindow(component.getProject());
        }
        ContentManager contentManager = componentToolWindow.getContentManager();
        String tabId = String.valueOf(component.getId());
        for (int i = 0; i < contentManager.getContentCount(); i++) {
            Content tab = contentManager.getContent(i);
            if (tabId.equals(tab.getTabName())) {
                contentManager.setSelectedContent(tab);
                componentToolWindow.activate(() -> {});
                return tab;
            }
        }
        ComponentInfoPanel infoPanel = new ComponentInfoPanel(component);
        Content tab =
                contentManager.getFactory().createContent(infoPanel, infoPanel.getTitle(), true);
        tab.setTabName(tabId);
        contentManager.addContent(tab);
        contentManager.setSelectedContent(tab);
        componentToolWindow.activate(() -> {});
        return tab;
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

    public static void removeComponentTab(Content tab) {
        componentToolWindow.getContentManager().removeContent(tab, true);
    }
}
