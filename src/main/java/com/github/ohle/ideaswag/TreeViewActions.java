package com.github.ohle.ideaswag;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;

import org.jetbrains.annotations.NotNull;

public class TreeViewActions {
    private static TreeViewPanel extractPanel(AnActionEvent e) {
        return (TreeViewPanel) e.getDataContext().getData(PlatformDataKeys.CONTEXT_COMPONENT);
    }

    public static class CollapseAll extends AnAction {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            extractPanel(e).collapseAll();
        }
    }

    public static class ExpandAll extends AnAction {

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            extractPanel(e).expandAll();
        }
    }

    public static class LocateSelected extends AnAction {

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            extractPanel(e).locateSelected();
        }
    }

    public static class AutoLocate extends ToggleAction {

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return extractPanel(e).isAutoLocateOn();
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            extractPanel(e).setAutoLocate(state);
        }
    }
}
