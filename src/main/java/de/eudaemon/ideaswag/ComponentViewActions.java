package de.eudaemon.ideaswag;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;

import org.jetbrains.annotations.NotNull;

public class ComponentViewActions {

    public static class OpenInTree extends AnAction {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            ComponentInfoPanel source =
                    (ComponentInfoPanel)
                            e.getDataContext().getData(PlatformDataKeys.CONTEXT_COMPONENT);
            Util.getOpenComponentTab()
                    .ifPresent(
                            rc -> {
                                Util.openTreeTab(rc.getRoot(), source.getDisposer());
                                Util.getOpenTreeTab().ifPresent(tp -> tp.selectComponent(rc));
                            });
        }
    }
}
