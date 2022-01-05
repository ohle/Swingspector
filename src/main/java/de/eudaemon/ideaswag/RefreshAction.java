package de.eudaemon.ideaswag;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;

import org.jetbrains.annotations.NotNull;

public class RefreshAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ((Refreshable) e.getDataContext().getData(PlatformDataKeys.CONTEXT_COMPONENT)).refresh();
    }
}
