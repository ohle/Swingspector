package de.eudaemon.ideaswag;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;

import org.jetbrains.annotations.NotNull;

public class RootsViewActions {
    public static final class Refresh extends AnAction {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            ((SwagRootsTab) e.getDataContext().getData(PlatformDataKeys.CONTEXT_COMPONENT))
                    .refresh();
        }
    }
}
