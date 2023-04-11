package de.eudaemon.ideaswag;

import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

public class RefreshAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            DebugProcessImpl debugProcess =
                    DebuggerManagerEx.getInstanceEx(project).getContext().getDebugProcess();
            e.getPresentation()
                    .setEnabled(debugProcess == null || debugProcess.getSession().isRunning());
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ((Refreshable) e.getDataContext().getData(PlatformDataKeys.CONTEXT_COMPONENT)).refresh();
    }
}
