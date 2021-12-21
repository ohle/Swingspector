package de.eudaemon.ideaswag;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import org.jetbrains.annotations.NotNull;

public class ComponentViewActions {

    public static class OpenInTree extends AnAction {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Util.getOpenComponentTab()
                    .ifPresent(
                            rc -> {
                                Util.openTreeTab(rc.getRoot());
                                Util.getOpenTreeTab().ifPresent(tp -> tp.selectComponent(rc));
                            });
        }
    }
}
